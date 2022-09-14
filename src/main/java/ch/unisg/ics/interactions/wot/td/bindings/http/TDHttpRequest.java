package ch.unisg.ics.interactions.wot.td.bindings.http;

import ch.unisg.ics.interactions.wot.td.affordances.Form;
import ch.unisg.ics.interactions.wot.td.bindings.BaseOperation;
import ch.unisg.ics.interactions.wot.td.bindings.Response;
import ch.unisg.ics.interactions.wot.td.clients.UriTemplate;
import ch.unisg.ics.interactions.wot.td.schemas.DataSchema;
import ch.unisg.ics.interactions.wot.td.security.APIKeySecurityScheme;
import ch.unisg.ics.interactions.wot.td.security.APIKeySecurityScheme.TokenLocation;
import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.*;
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
public class TDHttpRequest extends BaseOperation {
  private final static Logger LOGGER = Logger.getLogger(TDHttpRequest.class.getCanonicalName());

  private final Form form;
  private final String target;
  private final BasicClassicHttpRequest request;

  public TDHttpRequest(Form form, String operationType) {
    this.form = form;
    this.target = form.getTarget();

    Optional<String> methodName = form.getMethodName(operationType);

    if (methodName.isPresent()) {
      this.request = new BasicClassicHttpRequest(methodName.get(), form.getTarget());
    } else {
      throw new IllegalArgumentException("No default binding for the given operation type: "
        + operationType);
    }

    this.request.setHeader(HttpHeaders.CONTENT_TYPE, form.getContentType());
  }

  /**
   * TODO redundant with {@link ch.unisg.ics.interactions.wot.td.bindings.BaseProtocolBinding}
   *
   * @param form
   * @param operationType
   * @param uriVariables
   * @param values
   */
  public TDHttpRequest(Form form, String operationType, Map<String, DataSchema> uriVariables, Map<String, Object> values) {
    this.form = form;
    this.target = new UriTemplate(form.getTarget()).createUri(uriVariables, values);

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

  @Override
  public void sendRequest() throws IOException {
    HttpClient client = HttpClients.createDefault();
    try {
      // TODO use async API instead
      // https://github.com/apache/httpcomponents-client/blob/5.1.x/httpclient5/src/test/java/org/apache/hc/client5/http/examples/AsyncClientHttpExchange.java
      HttpResponse httpResponse = client.execute(request);
      Response r = new TDHttpResponse((ClassicHttpResponse) httpResponse);
      onResponse(r);
    } catch (IOException e) {
      onError();
    }
  }

  public TDHttpRequest setAPIKey(APIKeySecurityScheme scheme, String token) {
    if (scheme.getIn() == TokenLocation.HEADER) {
      this.request.setHeader(scheme.getName().get(), token);
    } else {
      LOGGER.info("API key could not be added in " + scheme.getIn().name());
    }

    return this;
  }

  public TDHttpRequest addHeader(String key, String value) {
    this.request.addHeader(key, value);
    return this;
  }

  @Override
  protected void setBooleanPayload(Boolean value) {
    request.setEntity(new StringEntity(String.valueOf(value), ContentType.create(form.getContentType())));
  }

  @Override
  protected void setStringPayload(String value) {
    request.setEntity(new StringEntity(value, ContentType.create(form.getContentType())));
  }

  @Override
  protected void setIntegerPayload(Long value) {
    request.setEntity(new StringEntity(String.valueOf(value), ContentType.create(form.getContentType())));
  }

  @Override
  protected void setNumberPayload(Double value) {
    request.setEntity(new StringEntity(String.valueOf(value), ContentType.create(form.getContentType())));
  }

  @Override
  protected void setObjectPayload(Map<String, Object> payload) {
    String body = new Gson().toJson(payload);
    request.setEntity(new StringEntity(body, ContentType.create(form.getContentType())));
  }

  @Override
  protected void setArrayPayload(List<Object> payload) {
    String body = new Gson().toJson(payload);
    request.setEntity(new StringEntity(body, ContentType.create(form.getContentType())));
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
