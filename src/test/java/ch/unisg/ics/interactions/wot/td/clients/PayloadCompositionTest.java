package ch.unisg.ics.interactions.wot.td.clients;

import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.ProtocolException;
import org.junit.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ch.unisg.ics.interactions.wot.td.affordances.Form;
import ch.unisg.ics.interactions.wot.td.schemas.ObjectSchema;
import ch.unisg.ics.interactions.wot.td.schemas.StringSchema;

public class PayloadCompositionTest {

  @Test
  public void testFormWithObjectSchemaPayload() throws ProtocolException, URISyntaxException {
    String prefix = "http://example.org/#";
    Form form = new Form("PUT", "http://example.org/toggle");
    
    ObjectSchema payloadSchema = new ObjectSchema.Builder()
        .addProperty("first_name", new StringSchema.Builder()
            .addSemanticType(prefix + "FirstName")
            .build())
        .addProperty("last_name", new StringSchema.Builder()
            .addSemanticType(prefix + "LastName")
            .build())
        .build();
    
    Map<String, Object> payloadVariables = new HashMap<String, Object>();
    
    payloadVariables.put(prefix + "FirstName", "Andrei");
    payloadVariables.put(prefix + "LastName", "Ciortea");
    
    SimpleHttpRequest request = (SimpleHttpRequest) TDHttpClient.buildHttpRequest(form, Optional.empty(), 
        new HashMap<String, Object>(), Optional.of(payloadSchema), payloadVariables);
    
    assertEquals("PUT", request.getMethod());
    assertEquals(0, request.getUri().compareTo(URI.create("http://example.org/toggle")));
    assertEquals("application/json", request.getHeader(HttpHeaders.CONTENT_TYPE).getValue());
    
    JsonObject payload = JsonParser.parseString(request.getBodyText()).getAsJsonObject();
    assertEquals("Andrei", payload.get("first_name").getAsString());
    assertEquals("Ciortea", payload.get("last_name").getAsString());
  }
}
