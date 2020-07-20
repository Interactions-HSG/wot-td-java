package ch.unisg.ics.interactions.wot.td.clients;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.ProtocolException;
import org.apache.hc.core5.http.message.BasicClassicHttpRequest;
import org.apache.http.ParseException;
import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import ch.unisg.ics.interactions.wot.td.affordances.Form;
import ch.unisg.ics.interactions.wot.td.schemas.ArraySchema;
import ch.unisg.ics.interactions.wot.td.schemas.BooleanSchema;
import ch.unisg.ics.interactions.wot.td.schemas.IntegerSchema;
import ch.unisg.ics.interactions.wot.td.schemas.NumberSchema;
import ch.unisg.ics.interactions.wot.td.schemas.ObjectSchema;
import ch.unisg.ics.interactions.wot.td.schemas.StringSchema;
import ch.unisg.ics.interactions.wot.td.vocabularies.TD;

public class PayloadCompositionTest {

  private final static String PREFIX = "http://example.org/";
  private final static Form FORM = new Form.Builder(PREFIX + "toggle")
      .setMethodName("PUT")
      .addOperationType(TD.invokeAction)
      .build();
  
  @Test
  public void testNoPayload() {
    BasicClassicHttpRequest request = new TDHttpRequest(FORM, TD.invokeAction)
        .getRequest();
    assertNull(request.getEntity());
  }
  
  @Test
  public void testSimpleObjectPayload() throws ProtocolException, URISyntaxException, 
      JsonSyntaxException, ParseException, IOException {
    ObjectSchema payloadSchema = new ObjectSchema.Builder()
        .addProperty("first_name", new StringSchema.Builder().build())
        .addProperty("last_name", new StringSchema.Builder().build())
        .build();
    
    Map<String, Object> payloadVariables = new HashMap<String, Object>();
    payloadVariables.put("first_name", "Andrei");
    payloadVariables.put("last_name", "Ciortea");
    
    BasicClassicHttpRequest request = new TDHttpRequest(FORM, TD.invokeAction)
        .setObjectPayload(payloadSchema, payloadVariables)
        .getRequest();
    
    StringWriter writer = new StringWriter();
    IOUtils.copy(request.getEntity().getContent(), writer, StandardCharsets.UTF_8.name());
    
    assertEquals("application/json", request.getHeader(HttpHeaders.CONTENT_TYPE).getValue());
    
    JsonObject payload = JsonParser.parseString(writer.toString()).getAsJsonObject();
    assertEquals("Andrei", payload.get("first_name").getAsString());
    assertEquals("Ciortea", payload.get("last_name").getAsString());
  }
  
  @Test
  public void testSimpleSemanticObjectPayload() throws ProtocolException, URISyntaxException, 
      JsonSyntaxException, ParseException, IOException {
    ObjectSchema payloadSchema = new ObjectSchema.Builder()
        .addProperty("first_name", new StringSchema.Builder()
            .addSemanticType(PREFIX + "FirstName")
            .build())
        .addProperty("last_name", new StringSchema.Builder()
            .addSemanticType(PREFIX + "LastName")
            .build())
        .build();
    
    Map<String, Object> payloadVariables = new HashMap<String, Object>();
    payloadVariables.put(PREFIX + "FirstName", "Andrei");
    payloadVariables.put(PREFIX + "LastName", "Ciortea");
    
    BasicClassicHttpRequest request = new TDHttpRequest(FORM, TD.invokeAction)
        .setObjectPayload(payloadSchema, payloadVariables)
        .getRequest();
    
    StringWriter writer = new StringWriter();
    IOUtils.copy(request.getEntity().getContent(), writer, StandardCharsets.UTF_8.name());
    
    assertEquals("PUT", request.getMethod());
    assertEquals(0, request.getUri().compareTo(URI.create(PREFIX + "toggle")));
    assertEquals("application/json", request.getHeader(HttpHeaders.CONTENT_TYPE).getValue());
    
    JsonObject payload = JsonParser.parseString(writer.toString()).getAsJsonObject();
    assertEquals("Andrei", payload.get("first_name").getAsString());
    assertEquals("Ciortea", payload.get("last_name").getAsString());
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testInvalidBooleanPayload() {
    new TDHttpRequest(FORM, TD.invokeAction)
    .setPrimitivePayload(new BooleanSchema.Builder().build(), "string")
    .getRequest();
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testInvalidIntegerPayload() {
    new TDHttpRequest(FORM, TD.invokeAction)
    .setPrimitivePayload(new IntegerSchema.Builder().build(), 0.5)
    .getRequest();
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testInvalidStringPayload() {
    new TDHttpRequest(FORM, TD.invokeAction)
    .setPrimitivePayload(new StringSchema.Builder().build(), true)
    .getRequest();
  }
  
  @Test
  public void testArrayPayload() throws UnsupportedOperationException, IOException {
    ArraySchema payloadSchema = new ArraySchema.Builder()
        .addItem(new NumberSchema.Builder().build())
        .build();
    
    List<Object> payloadVariables = new ArrayList<Object>();
    payloadVariables.add(1);
    payloadVariables.add(3);
    payloadVariables.add(5);
    
    BasicClassicHttpRequest request = new TDHttpRequest(FORM, TD.invokeAction)
        .setArrayPayload(payloadSchema, payloadVariables)
        .getRequest();
    
    StringWriter writer = new StringWriter();
    IOUtils.copy(request.getEntity().getContent(), writer, StandardCharsets.UTF_8.name());
    
    JsonArray payload = JsonParser.parseString(writer.toString()).getAsJsonArray();
    assertEquals(3, payload.size());
    assertEquals(1, payload.get(0).getAsInt());
    assertEquals(3, payload.get(1).getAsInt());
    assertEquals(5, payload.get(2).getAsInt());
  }
  
  @Test
  public void testSemanticObjectWithOneArrayPayload() throws UnsupportedOperationException, 
      IOException {
    ObjectSchema payloadSchema = new ObjectSchema.Builder()
        .addProperty("speed", new NumberSchema.Builder()
            .addSemanticType(PREFIX + "Speed")
            .build())
        .addProperty("coordinates", new ArraySchema.Builder()
            .addSemanticType(PREFIX + "3DCoordinates")
            .addItem(new IntegerSchema.Builder().build())
            .build())
        .build();
    
    List<Object> coordinates = new ArrayList<Object>();
    coordinates.add(30);
    coordinates.add(50);
    coordinates.add(70);
    
    Map<String, Object> payloadVariables = new HashMap<String, Object>();
    payloadVariables.put(PREFIX + "Speed", 3.5);
    payloadVariables.put(PREFIX + "3DCoordinates", coordinates);
    
    BasicClassicHttpRequest request = new TDHttpRequest(FORM, TD.invokeAction)
        .setObjectPayload(payloadSchema, payloadVariables)
        .getRequest();
    
    StringWriter writer = new StringWriter();
    IOUtils.copy(request.getEntity().getContent(), writer, StandardCharsets.UTF_8.name());
    
    JsonObject payload = JsonParser.parseString(writer.toString()).getAsJsonObject();
    assertEquals(3.5, payload.get("speed").getAsDouble(), 0.01);
    
    JsonArray coordinatesArray = payload.getAsJsonArray("coordinates");
    assertEquals(3, coordinatesArray.size());
    assertEquals(30, coordinatesArray.get(0).getAsInt());
    assertEquals(50, coordinatesArray.get(1).getAsInt());
    assertEquals(70, coordinatesArray.get(2).getAsInt());
  }
  
  @Test
  public void testArrayOfSemanticObjectsPayload() {
    // TODO
  }
  
  @Test
  public void testSemanticObjectWithArrayOfSemanticObjectsPayload() {
    // TODO
  }
  
  @Test//(expected = IllegalArgumentException.class) // TODO
  public void testInvalidObjectPayload() throws UnsupportedOperationException, IOException {
    ObjectSchema payloadSchema = new ObjectSchema.Builder()
        .addProperty("first_name", new StringSchema.Builder()
            .addSemanticType(PREFIX + "FirstName")
            .build())
        .addProperty("last_name", new StringSchema.Builder()
            .addSemanticType(PREFIX + "LastName")
            .build())
        .addProperty("email", new StringSchema.Builder()
            .addSemanticType(PREFIX + "Email")
            .build())
        .addRequiredProperties("email")
        .build();
    
    Map<String, Object> payloadVariables = new HashMap<String, Object>();
    payloadVariables.put(PREFIX + "FirstName", "Andrei");
    payloadVariables.put(PREFIX + "LastName", "Ciortea");
    
    new TDHttpRequest(FORM, TD.invokeAction)
        .setObjectPayload(payloadSchema, payloadVariables)
        .getRequest();
  }
  
  @Test
  public void testValidateArrayPayload() {
    // TODO
  }
}
