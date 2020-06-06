package ch.unisg.ics.interactions.wot.td.clients;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequests;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpStatus;

import com.google.common.net.HttpHeaders;
import com.google.gson.Gson;

import ch.unisg.ics.interactions.wot.td.ThingDescription;
import ch.unisg.ics.interactions.wot.td.affordances.ActionAffordance;
import ch.unisg.ics.interactions.wot.td.affordances.Form;
import ch.unisg.ics.interactions.wot.td.schemas.DataSchema;
import ch.unisg.ics.interactions.wot.td.schemas.ObjectSchema;

public class TDHttpClient {
  private final ThingDescription td;
  
  public TDHttpClient(ThingDescription td) {
    this.td = td;
  }
  
  public int invokeActionBySemanticType(String type, Map<String, Object> payload) {
    HttpClient client = HttpClients.createDefault();
    
    HttpRequest request = requestActionBySemanticType(type, payload);
    
    try {
      return client.execute((ClassicHttpRequest) request).getCode();
    } catch (IOException e) {
      e.printStackTrace();
    }
    
    return HttpStatus.SC_BAD_REQUEST;
  }
  
  HttpRequest requestActionBySemanticType(String type, Map<String, Object> payload) {
    Optional<ActionAffordance> action = td.getActionBySemanticType(type);
    
    if (action.isPresent()) {
//      if (action.get().getForms().isEmpty()) {
//        throw new InvalidTDException("No form available for action affordance of type " + type);
//      }
      
      Form form = action.get().getForms().get(0);
      
      return buildHttpRequest(form, Optional.empty(), new HashMap<String, Object>(), 
          action.get().getInputSchema(), payload);
      
    }
    
    throw new IllegalArgumentException("Unknown action type: " + type);
  }
  
  static HttpRequest buildHttpRequest(Form form, Optional<DataSchema> uriSchema, 
      Map<String, Object> uriVariables, Optional<DataSchema> payloadSchema, 
      Map<String, Object> payload) {
    // TODO: add query parameters
    SimpleHttpRequest request = SimpleHttpRequests.create(form.getMethodName(), form.getHref());
    
    if (payloadSchema.isPresent() && !payload.isEmpty()) {
      DataSchema schema = payloadSchema.get();
      
      if (schema instanceof ObjectSchema) {
        Map<String, Object> instance = ((ObjectSchema) schema).instantiate(payload);
        
        String body = new Gson().toJson(instance);
        
        request.setBody(body, ContentType.create(form.getContentType()));
        request.setHeader(HttpHeaders.CONTENT_TYPE, form.getContentType());
      }
    }
    
    return request;
  }
}
