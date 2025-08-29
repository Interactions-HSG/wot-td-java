package ch.unisg.ics.interactions.wot.td.clients;

import ch.unisg.ics.interactions.wot.td.affordances.Form;
import ch.unisg.ics.interactions.wot.td.schemas.ArraySchema;
import ch.unisg.ics.interactions.wot.td.schemas.DataSchema;
import ch.unisg.ics.interactions.wot.td.schemas.ObjectSchema;
import ch.unisg.ics.interactions.wot.td.security.APIKeySecurityScheme;
import ch.unisg.ics.interactions.wot.td.security.TokenBasedSecurityScheme.TokenLocation;
import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.DefaultConnectionKeepAliveStrategy;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.impl.DefaultConnectionReuseStrategy;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicClassicHttpRequest;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Wrapper for constructing and executing an HTTP request based on a given <code>ThingDescription</code>.
 * When constructing the request, clients can set payloads that conform to a <code>DataSchema</code>.
 */
public class TDHttpRequest {
  private final static Logger LOGGER = Logger.getLogger(TDHttpRequest.class.getCanonicalName());

  private final Form form;
  private final String target;
  private final BasicClassicHttpRequest request;

  public TDHttpRequest(Form form, String operationType) {
    this(form, form.getTarget(), operationType);
  }

  public TDHttpRequest(Form form, String operationType, Map<String, DataSchema> uriVariables, Map<String, Object> values) {
    this(form, new UriTemplate(form.getTarget()).createUri(uriVariables, values), operationType);
  }

  private TDHttpRequest(Form form, String target, String operationType) {
    if (!form.getProtocol().isPresent() || !"HTTP".equals(form.getProtocol().get())) {
      throw new IllegalArgumentException("The HTTP protocol binding cannot be applied with the " +
        "given form");
    }
    this.form = form;
    this.target = target;
    Optional<String> methodName = form.getMethodName(operationType);

    if (methodName.isPresent()) {
      this.request = new BasicClassicHttpRequest(methodName.get(), this.target);
    } else {
      throw new IllegalArgumentException("No default binding for the given operation type: "
        + operationType);
    }

    this.request.setHeader(HttpHeaders.CONTENT_TYPE, form.getContentType());
  }

  public String getTarget() {
    return target;
  }

  public TDHttpResponse execute() throws IOException {
    CloseableHttpClient client = HttpClients.custom()
      .setConnectionManager(new PoolingHttpClientConnectionManager())
      .setConnectionReuseStrategy(DefaultConnectionReuseStrategy.INSTANCE)
      .setKeepAliveStrategy(DefaultConnectionKeepAliveStrategy.INSTANCE)
      .build();
    HttpResponse response = client.execute(request);
    return new TDHttpResponse((ClassicHttpResponse) response);
  }

  public TDHttpRequest setAPIKey(APIKeySecurityScheme scheme, String token) {
    if (scheme.getTokenLocation() == TokenLocation.HEADER) {
      this.request.setHeader(scheme.getTokenName().get(), token);
    } else {
      LOGGER.info("API key could not be added in " + scheme.getTokenName());
    }

    return this;
  }

  public TDHttpRequest addHeader(String key, String value) {
    this.request.addHeader(key, value);
    return this;
  }

  public TDHttpRequest setPrimitivePayload(DataSchema dataSchema, boolean value)
    throws IllegalArgumentException {
    if (dataSchema.getDatatype().equals(DataSchema.BOOLEAN)) {
      request.setEntity(new StringEntity(String.valueOf(value),
        ContentType.create(form.getContentType())));
    } else {
      throw new IllegalArgumentException("The payload's datatype does not match BooleanSchema "
        + "(payload datatype: " + dataSchema.getDatatype() + ")");
    }

    return this;
  }

  public TDHttpRequest setPrimitivePayload(DataSchema dataSchema, String value)
    throws IllegalArgumentException {
    if (dataSchema.getDatatype().equals(DataSchema.STRING)) {
      request.setEntity(new StringEntity(value, ContentType.create(form.getContentType())));
    } else {
      throw new IllegalArgumentException("The payload's datatype does not match StringSchema "
        + "(payload datatype: " + dataSchema.getDatatype() + ")");
    }

    return this;
  }

  public TDHttpRequest setPrimitivePayload(DataSchema dataSchema, long value)
    throws IllegalArgumentException {
    if (dataSchema.getDatatype().equals(DataSchema.INTEGER)
      || dataSchema.getDatatype().equals(DataSchema.NUMBER)) {
      request.setEntity(new StringEntity(String.valueOf(value),
        ContentType.create(form.getContentType())));
    } else {
      throw new IllegalArgumentException("The payload's datatype does not match IntegerSchema or "
        + "NumberSchema (payload datatype: " + dataSchema.getDatatype() + ")");
    }

    return this;
  }

  public TDHttpRequest setPrimitivePayload(DataSchema dataSchema, double value)
    throws IllegalArgumentException {
    if (dataSchema.getDatatype().equals(DataSchema.NUMBER)) {
      request.setEntity(new StringEntity(String.valueOf(value),
        ContentType.create(form.getContentType())));
    } else {
      throw new IllegalArgumentException("The payload's datatype does not match NumberSchema "
        + "(payload datatype: " + dataSchema.getDatatype() + ")");
    }

    return this;
  }

  /**
   * Sets a payload of type <code>ObjectSchema</code>. The object payload is given as a map where:
   * <ul>
   * <li>a key is a string that represents either a semantic type or an object property name</li>
   * <li>a value can be a primitive, an object represented as a <code>Map&lt;String,Object&gt;</code>
   * (that is, a nested object), or an ordered list of values of type <code>List&lt;Object&gt;</code></li>
   * </ul>
   *
   * @param objectSchema schema to be used for validating the payload and constructing the body of
   *                     the request
   * @param payload      the actual payload
   * @return this <code>TDHttpRequest</code>
   */
  public TDHttpRequest setObjectPayload(ObjectSchema objectSchema, Map<String, Object> payload) {
    if (objectSchema.validate(payload)) {
      Map<String, Object> instance = objectSchema.instantiate(payload);
      String body = new Gson().toJson(instance);
      request.setEntity(new StringEntity(body, ContentType.create(form.getContentType())));
    }

    return this;
  }

  /**
   * Sets a payload of type <code>ArraySchema</code>. The payload is given as an ordered list of
   * values of type <code>List&lt;Object&gt;</code>. Values can be primitives, objects represented
   * as <code>Map&lt;String,Object&gt;</code>, or lists of values (that is, nested lists).
   *
   * @param arraySchema schema used for validating the payload and constructing the body of
   *                    the request
   * @param payload     the actual payload
   * @return this <code>TDHttpRequest</code>
   */
  public TDHttpRequest setArrayPayload(ArraySchema arraySchema, List<Object> payload) {
    if (arraySchema.validate(payload)) {
      String body = new Gson().toJson(payload);
      request.setEntity(new StringEntity(body, ContentType.create(form.getContentType())));
    }

    return this;
  }

  public String getPayloadAsString() throws ParseException, IOException {
    return EntityUtils.toString(request.getEntity());
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("[TDHttpRequest] Method: " + request.getMethod());

    try {
      builder.append(", Target: " + request.getUri().toString());

      for (Header header : request.getHeaders()) {
        builder.append(", " + header.getName() + ": " + header.getValue());
      }

      if (request.getEntity() != null) {
        StringWriter writer = new StringWriter();
        IOUtils.copy(request.getEntity().getContent(), writer, StandardCharsets.UTF_8.name());
        builder.append(", Payload: " + writer.toString());
      }
    } catch (UnsupportedOperationException | IOException | URISyntaxException e) {
      LOGGER.log(Level.WARNING, e.getMessage());
    }

    return builder.toString();
  }

  BasicClassicHttpRequest getRequest() {
    return this.request;
  }
}
