package ch.unisg.ics.interactions.wot.td.bindings.http;

import ch.unisg.ics.interactions.wot.td.affordances.Form;
import ch.unisg.ics.interactions.wot.td.bindings.BaseOperation;
import ch.unisg.ics.interactions.wot.td.security.APIKeySecurityScheme;
import ch.unisg.ics.interactions.wot.td.security.TokenBasedSecurityScheme.TokenLocation;
import com.google.gson.Gson;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.io.CloseMode;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Wrapper for constructing and executing an HTTP request based on a given <code>ThingDescription</code>.
 * When constructing the request, clients can set payloads that conform to a <code>DataSchema</code>.
 */
public class TDHttpOperation extends BaseOperation {
  private final static Logger LOGGER = Logger.getLogger(TDHttpOperation.class.getCanonicalName());

  private final class TDHttpHandler implements FutureCallback<SimpleHttpResponse> {

    @Override
    public void completed(SimpleHttpResponse r) {
      onResponse(new TDHttpResponse(r));
      client.close(CloseMode.GRACEFUL);
    }

    @Override
    public void failed(Exception ex) {
      onError();
      client.close(CloseMode.GRACEFUL);
    }

    @Override
    public void cancelled() {
      client.close(CloseMode.GRACEFUL);
    }

  }

  private final Form form;
  private final String target;

  private final TDHttpHandler handler;
  private final SimpleHttpRequest request;

  private CloseableHttpAsyncClient client;

  public TDHttpOperation(Form form, String operationType) {
    this.form = form;
    this.target = form.getTarget();
    this.handler = new TDHttpHandler();
    this.client = HttpAsyncClients.createDefault();

    this.client.start();

    Optional<String> methodName = form.getMethodName(operationType);

    if (methodName.isPresent()) {
      this.request = SimpleHttpRequest.create(methodName.get(), form.getTarget());
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
    client.execute(request, handler);
  }

  public TDHttpOperation setAPIKey(APIKeySecurityScheme scheme, String token) {
    if (scheme.getTokenLocation() == TokenLocation.HEADER) {
      this.request.setHeader(scheme.getTokenName().get(), token);
    } else {
      LOGGER.info("API key could not be added in " + scheme.getTokenLocation().name());
    }

    return this;
  }

  public TDHttpOperation addHeader(String key, String value) {
    this.request.addHeader(key, value);
    return this;
  }

  @Override
  protected void setBooleanPayload(Boolean value) {
    request.setBody(String.valueOf(value), ContentType.create(form.getContentType()));
  }

  @Override
  protected void setStringPayload(String value) {
    request.setBody(value, ContentType.create(form.getContentType()));
  }

  @Override
  protected void setIntegerPayload(Long value) {
    request.setBody(String.valueOf(value), ContentType.create(form.getContentType()));
  }

  @Override
  protected void setNumberPayload(Double value) {
    request.setBody(String.valueOf(value), ContentType.create(form.getContentType()));
  }

  @Override
  protected void setObjectPayload(Map<String, Object> payload) {
    String body = new Gson().toJson(payload);
    request.setBody(body, ContentType.create(form.getContentType()));
  }

  @Override
  protected void setArrayPayload(List<Object> payload) {
    String body = new Gson().toJson(payload);
    request.setBody(body, ContentType.create(form.getContentType()));
  }

  public String getPayloadAsString() {
    return request.getBodyText();
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

      if (request.getBodyText() != null) {
        builder.append(", Payload: " + request.getBodyText());
      }
    } catch (UnsupportedOperationException | URISyntaxException e) {
      LOGGER.log(Level.WARNING, e.getMessage());
    }

    return builder.toString();
  }

  SimpleHttpRequest getRequest() {
    return this.request;
  }

}
