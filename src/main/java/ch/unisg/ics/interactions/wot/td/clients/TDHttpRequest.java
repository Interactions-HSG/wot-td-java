package ch.unisg.ics.interactions.wot.td.clients;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.ProtocolException;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicClassicHttpRequest;

import com.google.gson.Gson;

import ch.unisg.ics.interactions.wot.td.affordances.Form;
import ch.unisg.ics.interactions.wot.td.schemas.ArraySchema;
import ch.unisg.ics.interactions.wot.td.schemas.DataSchema;
import ch.unisg.ics.interactions.wot.td.schemas.ObjectSchema;

public class TDHttpRequest {
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
  
  public int execute() {
    HttpClient client = HttpClients.createDefault();
    
    try {
      return client.execute(request).getCode();
    } catch (IOException e) {
      e.printStackTrace();
    }
    
    return HttpStatus.SC_BAD_REQUEST;
  }
  
//  static HttpRequest buildHttpRequest(Form form, Optional<DataSchema> uriSchema, 
//      Map<String, Object> uriVariables, Optional<DataSchema> payloadSchema, 
//      Map<String, Object> payload) {
//    // TODO: add query parameters
//    SimpleHttpRequest request = SimpleHttpRequests.create(form.getMethodName(), form.getHref());
//    
//    if (payloadSchema.isPresent() && !payload.isEmpty()) {
//      DataSchema schema = payloadSchema.get();
//      
//      if (schema instanceof ObjectSchema) {
//        Map<String, Object> instance = ((ObjectSchema) schema).instantiate(payload);
//        
//        String body = new Gson().toJson(instance);
//        
//        request.setBody(body, ContentType.create(form.getContentType()));
//        request.setHeader(HttpHeaders.CONTENT_TYPE, form.getContentType());
//      }
//    }
//    
//    return request;
//  }
  
  public TDHttpRequest setPrimitivePayload(DataSchema dataSchema, boolean value) 
      throws IllegalArgumentException {
    if (dataSchema.getDatatype() == DataSchema.BOOLEAN) {
      request.setEntity(new StringEntity(String.valueOf(value), 
          ContentType.create(form.getContentType())));
    } else {
      throw new IllegalArgumentException("The expected datatype is not a boolean value (datatype: " 
          + dataSchema.getDatatype());
    }
    
    return this;
  }
  
  public TDHttpRequest setPrimitivePayload(DataSchema dataSchema, String value) 
      throws IllegalArgumentException {
    if (dataSchema.getDatatype() == DataSchema.STRING) {
      request.setEntity(new StringEntity(value, ContentType.create(form.getContentType())));
    } else {
      throw new IllegalArgumentException("The expected datatype is not a boolean value (datatype: " 
          + dataSchema.getDatatype());
    }
    
    return this;
  }
  
  public TDHttpRequest setPrimitivePayload(DataSchema dataSchema, long value) 
      throws IllegalArgumentException {
    if (dataSchema.getDatatype() == DataSchema.INTEGER) {
      request.setEntity(new StringEntity(String.valueOf(value), 
          ContentType.create(form.getContentType())));
    } else {
      throw new IllegalArgumentException("The expected datatype is not a boolean value (datatype: " 
          + dataSchema.getDatatype());
    }
    
    return this;
  }
  
  public TDHttpRequest setPrimitivePayload(DataSchema dataSchema, double value) 
      throws IllegalArgumentException {
    if (dataSchema.getDatatype() == DataSchema.NUMBER) {
      request.setEntity(new StringEntity(String.valueOf(value), 
          ContentType.create(form.getContentType())));
    } else {
      throw new IllegalArgumentException("The expected datatype is not a boolean value (datatype: " 
          + dataSchema.getDatatype());
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
  
  BasicClassicHttpRequest getRequest() {
    return this.request;
  }
  
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("[TDHttpRequest] Method: " + request.getMethod());
    
    try {
      builder.append(", Target: " + request.getUri().toString());
      builder.append(", Content-Type: " + request.getHeader(HttpHeaders.CONTENT_TYPE).getValue());
      
      if (request.getEntity() != null) {
        StringWriter writer = new StringWriter();
        IOUtils.copy(request.getEntity().getContent(), writer, StandardCharsets.UTF_8.name());
        builder.append(", Payload: " + writer.toString());
      }
    } catch (UnsupportedOperationException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (URISyntaxException e) {
      e.printStackTrace();
    } catch (ProtocolException e) {
      e.printStackTrace();
    }
    
    return builder.toString();
  }
  
}
