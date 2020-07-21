package ch.unisg.ics.interactions.wot.td.clients;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicClassicHttpResponse;
import org.junit.Test;

import ch.unisg.ics.interactions.wot.td.schemas.ArraySchema;
import ch.unisg.ics.interactions.wot.td.schemas.BooleanSchema;
import ch.unisg.ics.interactions.wot.td.schemas.IntegerSchema;
import ch.unisg.ics.interactions.wot.td.schemas.NullSchema;
import ch.unisg.ics.interactions.wot.td.schemas.NumberSchema;
import ch.unisg.ics.interactions.wot.td.schemas.ObjectSchema;
import ch.unisg.ics.interactions.wot.td.schemas.StringSchema;

public class TDHttpResponseTest {
  private static final String PREFIX = "http://example.org/";
  private static final String USER_PAYLOAD = "{\"first_name\" : \"Andrei\", \"last_name\" : \"Ciortea\"}";
  
  @Test
  public void testNoPayload() {
    ClassicHttpResponse response = new BasicClassicHttpResponse(HttpStatus.SC_OK);
    Optional<String> payload = new TDHttpResponse(response).getPayload();
    assertFalse(payload.isPresent());
  }
  
  @Test
  public void testBooleanPayload() {
    ClassicHttpResponse response = constructHttpResponse(false);
    assertFalse(new TDHttpResponse(response).getPayloadAsBoolean());
  }
  
  @Test
  public void testStringPayload() {
    ClassicHttpResponse response = constructHttpResponse("test");
    assertEquals("test", new TDHttpResponse(response).getPayloadAsString());
  }
  
  @Test
  public void testIntegerPayload() {
    ClassicHttpResponse response = constructHttpResponse("101");
    assertEquals(101, new TDHttpResponse(response).getPayloadAsInteger().intValue());
  }
  
  @Test
  public void testDoublePayload() {
    ClassicHttpResponse response = constructHttpResponse("101.005");
    assertEquals(101.005, new TDHttpResponse(response).getPayloadAsDouble().doubleValue(), 0.001);
  }
  
  @Test
  public void testObjectPayload() {
    ClassicHttpResponse response = constructHttpResponse(USER_PAYLOAD);
    
    ObjectSchema schema = TDHttpRequestTest.USER_SCHEMA;
    Map<String, Object> payload = new TDHttpResponse(response).getPayloadAsObject(schema);
    
    assertEquals(2, payload.size());
    assertEquals("Andrei", payload.get(PREFIX + "FirstName"));
    assertEquals("Ciortea", payload.get(PREFIX + "LastName"));
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testObjectRequiredPayload() {
    ClassicHttpResponse response = constructHttpResponse("{\"first_name\" : \"Andrei\"}");
    ObjectSchema schema = TDHttpRequestTest.USER_SCHEMA;
    new TDHttpResponse(response).getPayloadAsObject(schema);
  }
  
  @Test
  public void testNestedObjectPayload() {
    ClassicHttpResponse response = constructHttpResponse("{\n" + 
        "  \"count\" : 1,\n" + 
        "  \"user\" : " + USER_PAYLOAD + "\n" + 
        "}");
    
    String prefix = "http://example.org/";
    ObjectSchema schema = new ObjectSchema.Builder()
        .addProperty("count", new IntegerSchema.Builder()
            .addSemanticType(prefix + "Count")
            .build())
        .addProperty("user", TDHttpRequestTest.USER_SCHEMA)
        .build();
    
    Map<String, Object> payload = new TDHttpResponse(response).getPayloadAsObject(schema);
    assertEquals(2, payload.size());
    assertEquals(1, payload.get(prefix + "Count"));
    
    @SuppressWarnings("unchecked")
    Map<String, Object> user = (Map<String, Object>) payload.get(prefix + "User");
    assertEquals(2, user.size());
    assertEquals("Andrei", user.get(prefix + "FirstName"));
    assertEquals("Ciortea", user.get(prefix + "LastName"));
  }
  
  @Test
  public void testPrimitiveArrayPayload() {
    ClassicHttpResponse response = constructHttpResponse("[\"my_string\", 1.5, 2, true, null]");
    
    ArraySchema schema = new ArraySchema.Builder()
        .addItem(new StringSchema.Builder().build())
        .addItem(new IntegerSchema.Builder().build())
        .addItem(new NumberSchema.Builder().build())
        .addItem(new BooleanSchema.Builder().build())
        .addItem(new NullSchema.Builder().build())
        .build();
    
    List<Object> payload = new TDHttpResponse(response).getPayloadAsArray(schema);
    assertEquals(5, payload.size());
    assertTrue(payload.contains("my_string"));
    assertTrue(payload.contains(1.5));
    assertTrue(payload.contains(2.0));
    assertTrue(payload.contains(true));
    assertTrue(payload.contains(null));
  }
  
  @Test
  public void testIntegerArrayPayload() {
    ClassicHttpResponse response = constructHttpResponse("[1, 2, 3]");
    
    ArraySchema schema = new ArraySchema.Builder()
        .addItem(new IntegerSchema.Builder().build())
        .build();
    
    List<Object> payload = new TDHttpResponse(response).getPayloadAsArray(schema);
    assertEquals(3, payload.size());
    assertTrue(payload.contains(1));
    assertTrue(payload.contains(2));
    assertTrue(payload.contains(3));
  }
  
  @Test
  public void testObjectArrayPayload() {
    ClassicHttpResponse response = constructHttpResponse("[" + USER_PAYLOAD + "]");
    
    String prefix = "http://example.org/";
    ArraySchema schema = new ArraySchema.Builder()
        .addItem(TDHttpRequestTest.USER_SCHEMA)
        .build();
    
    List<Object> payload = new TDHttpResponse(response).getPayloadAsArray(schema);
    assertEquals(1, payload.size());
    
    @SuppressWarnings("unchecked")
    Map<String, Object> user = (Map<String, Object>) payload.get(0);
    assertEquals("Andrei", user.get(prefix + "FirstName"));
    assertEquals("Ciortea", user.get(prefix + "LastName"));
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testArrayMinItemsPayload() {
    ClassicHttpResponse response = constructHttpResponse("[1, 2]");
    
    ArraySchema schema = new ArraySchema.Builder()
        .addItem(new IntegerSchema.Builder().build())
        .addMinItems(3)
        .build();
    
    new TDHttpResponse(response).getPayloadAsArray(schema);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testArrayMaxItemsPayload() {
    ClassicHttpResponse response = constructHttpResponse("[1, 2, 3]");
    
    ArraySchema schema = new ArraySchema.Builder()
        .addItem(new IntegerSchema.Builder().build())
        .addMaxItems(2)
        .build();
    
    new TDHttpResponse(response).getPayloadAsArray(schema);
  }
  
  private ClassicHttpResponse constructHttpResponse(Object payload) {
    ClassicHttpResponse response = new BasicClassicHttpResponse(HttpStatus.SC_OK);
    response.setEntity(new StringEntity(String.valueOf(payload)));
    return response;
  }
  
}
