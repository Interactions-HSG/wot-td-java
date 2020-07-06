package ch.unisg.ics.interactions.wot.td.clients;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicClassicHttpRequest;

import com.google.gson.Gson;

import ch.unisg.ics.interactions.wot.td.affordances.Form;
import ch.unisg.ics.interactions.wot.td.schemas.ArraySchema;
import ch.unisg.ics.interactions.wot.td.schemas.DataSchema;
import ch.unisg.ics.interactions.wot.td.schemas.ObjectSchema;
import ch.unisg.ics.interactions.wot.td.security.APIKeySecurityScheme;

public class TDHttpRequest {
  private final static Logger LOGGER = Logger.getLogger(TDHttpRequest.class.getCanonicalName());
  
  private final Form form;
  private BasicClassicHttpRequest request;
  
  public TDHttpRequest(Form form, String operationType) {
    this.form = form;
    
    Optional<String> methodName = form.getMethodName(operationType);
    
    if (methodName.isPresent()) {
      this.request = new BasicClassicHttpRequest(methodName.get(), form.getTarget());
    } else {
      throw new IllegalArgumentException("No default binding for the given operation type: " 
          + operationType);
    }
    
    this.request.setHeader(HttpHeaders.CONTENT_TYPE, form.getContentType());
  }
  
  public TDHttpResponse execute() throws IOException {
    HttpClient client = HttpClients.createDefault();
    HttpResponse response = client.execute(request);
    return new TDHttpResponse((ClassicHttpResponse) response);
  }
  
  public TDHttpRequest setAPIKey(APIKeySecurityScheme scheme, String token) {
    switch (scheme.getIn()) {
      case HEADER:
        this.request.setHeader(scheme.getName().get(), token);
        break;
      default:
        LOGGER.info("API key could not be added in " + scheme.getIn().name());
    }
    
    return this;
  }
  
  public TDHttpRequest addHeader(String key, String value) {
    this.request.addHeader(key, value);
    return this;
  }
  
  public TDHttpRequest setPrimitivePayload(DataSchema dataSchema, boolean value) 
      throws IllegalArgumentException {
    if (dataSchema.getDatatype() == DataSchema.BOOLEAN) {
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
    if (dataSchema.getDatatype() == DataSchema.STRING) {
      request.setEntity(new StringEntity(value, ContentType.create(form.getContentType())));
    } else {
      throw new IllegalArgumentException("The payload's datatype does not match StringSchema "
          + "(payload datatype: " + dataSchema.getDatatype() + ")");
    }
    
    return this;
  }
  
  public TDHttpRequest setPrimitivePayload(DataSchema dataSchema, long value) 
      throws IllegalArgumentException {
    if (dataSchema.getDatatype() == DataSchema.INTEGER) {
      request.setEntity(new StringEntity(String.valueOf(value), 
          ContentType.create(form.getContentType())));
    } else {
      throw new IllegalArgumentException("The payload's datatype does not match IntegerSchema "
          + "(payload datatype: " + dataSchema.getDatatype() + ")");
    }
    
    return this;
  }
  
  public TDHttpRequest setPrimitivePayload(DataSchema dataSchema, double value) 
      throws IllegalArgumentException {
    if (dataSchema.getDatatype() == DataSchema.NUMBER) {
      request.setEntity(new StringEntity(String.valueOf(value), 
          ContentType.create(form.getContentType())));
    } else {
      throw new IllegalArgumentException("The payload's datatype does not match NumberSchema "
          + "(payload datatype: " + dataSchema.getDatatype() + ")");
    }
    
    return this;
  }
  
  public TDHttpRequest setObjectPayload(ObjectSchema objectSchema, Map<String, Object> payload) {
    if (objectSchema.validate(payload)) {
      Map<String, Object> instance = objectSchema.instantiate(payload);
      String body = new Gson().toJson(instance);
      request.setEntity(new StringEntity(body, ContentType.create(form.getContentType())));
    }
    
    return this;
  }
  
  public TDHttpRequest setArrayPayload(ArraySchema arraySchema, List<Object> payload) {
    if (arraySchema.validate(payload)) {
      String body = new Gson().toJson(payload);
      request.setEntity(new StringEntity(body, ContentType.create(form.getContentType())));
    }
    
    return this;
  }
  
  public String getPayload() throws ParseException, IOException {
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
      e.printStackTrace();
    }
    
    return builder.toString();
  }
  
  BasicClassicHttpRequest getRequest() {
    return this.request;
  }
}
