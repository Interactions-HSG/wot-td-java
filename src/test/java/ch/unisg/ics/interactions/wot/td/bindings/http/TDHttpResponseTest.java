package ch.unisg.ics.interactions.wot.td.bindings.http;

import ch.unisg.ics.interactions.wot.td.schemas.*;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;

public class TDHttpResponseTest {
  private static final String PREFIX = "http://example.org/";
  private static final String USER_PAYLOAD = "{\"first_name\" : \"Andrei\", \"last_name\" : \"Ciortea\"}";

  @Test
  public void testNoPayload() {
    SimpleHttpResponse response = SimpleHttpResponse.create(HttpStatus.SC_OK);
    Optional<Object> payload = new TDHttpResponse(response, null).getPayload();
    assertFalse(payload.isPresent());
  }

  @Test
  public void testBooleanPayload() {
    SimpleHttpResponse response = constructHttpResponse(false);
    assertFalse(new TDHttpResponse(response, null).getPayloadAsBoolean());
  }

  @Test
  public void testStringPayload() {
    SimpleHttpResponse response = constructHttpResponse("test");
    assertEquals("test", new TDHttpResponse(response, null).getPayloadAsString());
  }

  @Test
  public void testIntegerPayload() {
    SimpleHttpResponse response = constructHttpResponse("101");
    assertEquals(101, new TDHttpResponse(response, null).getPayloadAsInteger().intValue());
  }

  @Test
  public void testDoublePayload() {
    SimpleHttpResponse response = constructHttpResponse("101.005");
    assertEquals(101.005, new TDHttpResponse(response, null).getPayloadAsDouble().doubleValue(), 0.001);
  }

  @Test
  public void testObjectPayload() {
    SimpleHttpResponse response = constructHttpResponse(USER_PAYLOAD);

    ObjectSchema schema = TDHttpOperationTest.USER_SCHEMA;
    Map<String, Object> payload = new TDHttpResponse(response, null).getPayloadAsObject(schema);

    assertEquals(2, payload.size());
    assertEquals("Andrei", payload.get(PREFIX + "FirstName"));
    assertEquals("Ciortea", payload.get(PREFIX + "LastName"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testObjectRequiredPayload() {
    SimpleHttpResponse response = constructHttpResponse("{\"first_name\" : \"Andrei\"}");
    ObjectSchema schema = TDHttpOperationTest.USER_SCHEMA;
    new TDHttpResponse(response, null).getPayloadAsObject(schema);
  }

  @Test
  public void testNestedObjectPayload() {
    SimpleHttpResponse response = constructHttpResponse("{\n" +
        "  \"count\" : 1,\n" +
        "  \"user\" : " + USER_PAYLOAD + "\n" +
        "}");

    String prefix = "http://example.org/";
    ObjectSchema schema = new ObjectSchema.Builder()
        .addProperty("count", new IntegerSchema.Builder()
            .addSemanticType(prefix + "Count")
            .build())
        .addProperty("user", TDHttpOperationTest.USER_SCHEMA)
        .build();

    Map<String, Object> payload = new TDHttpResponse(response, null).getPayloadAsObject(schema);
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
    SimpleHttpResponse response = constructHttpResponse("[\"my_string\", 1.5, 2, true, null]");

    ArraySchema schema = new ArraySchema.Builder()
        .addItem(new StringSchema.Builder().build())
        .addItem(new IntegerSchema.Builder().build())
        .addItem(new NumberSchema.Builder().build())
        .addItem(new BooleanSchema.Builder().build())
        .addItem(new NullSchema.Builder().build())
        .build();

    List<Object> payload = new TDHttpResponse(response, null).getPayloadAsArray(schema);
    assertEquals(5, payload.size());
    assertTrue(payload.contains("my_string"));
    assertTrue(payload.contains(1.5));
    assertTrue(payload.contains(2.0));
    assertTrue(payload.contains(true));
    assertTrue(payload.contains(null));
  }

  @Test
  public void testIntegerArrayPayload() {
    SimpleHttpResponse response = constructHttpResponse("[1, 2, 3]");

    ArraySchema schema = new ArraySchema.Builder()
        .addItem(new IntegerSchema.Builder().build())
        .build();

    List<Object> payload = new TDHttpResponse(response, null).getPayloadAsArray(schema);
    assertEquals(3, payload.size());
    assertTrue(payload.contains(1));
    assertTrue(payload.contains(2));
    assertTrue(payload.contains(3));
  }

  @Test
  public void testObjectArrayPayload() {
    SimpleHttpResponse response = constructHttpResponse("[" + USER_PAYLOAD + "]");

    String prefix = "http://example.org/";
    ArraySchema schema = new ArraySchema.Builder()
        .addItem(TDHttpOperationTest.USER_SCHEMA)
        .build();

    List<Object> payload = new TDHttpResponse(response, null).getPayloadAsArray(schema);
    assertEquals(1, payload.size());

    @SuppressWarnings("unchecked")
    Map<String, Object> user = (Map<String, Object>) payload.get(0);
    assertEquals("Andrei", user.get(prefix + "FirstName"));
    assertEquals("Ciortea", user.get(prefix + "LastName"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testArrayMinItemsPayload() {
    SimpleHttpResponse response = constructHttpResponse("[1, 2]");

    ArraySchema schema = new ArraySchema.Builder()
        .addItem(new IntegerSchema.Builder().build())
        .addMinItems(3)
        .build();

    new TDHttpResponse(response, null).getPayloadAsArray(schema);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testArrayMaxItemsPayload() {
    SimpleHttpResponse response = constructHttpResponse("[1, 2, 3]");

    ArraySchema schema = new ArraySchema.Builder()
        .addItem(new IntegerSchema.Builder().build())
        .addMaxItems(2)
        .build();

    new TDHttpResponse(response, null).getPayloadAsArray(schema);
  }

  @Test
  public void testArbitraryJSONPayload() {
    SimpleHttpResponse response = constructHttpResponse("[" + USER_PAYLOAD + "]");

    Object array = new TDHttpResponse(response, null).getPayload().get();
    assertTrue(array instanceof List);

    Object obj = ((List<Object>) array).get(0);
    assertTrue(obj instanceof Map);

    Object str = ((Map<String, Object>) obj).get("first_name");
    assertTrue(str instanceof String);
  }

  @Test
  public void testHeaders(){
    SimpleHttpResponse response = SimpleHttpResponse.create(HttpStatus.SC_OK);
    response.addHeader("Content-Type", "application/json");
    TDHttpResponse tdHttpResponse = new TDHttpResponse(response, null);
    Map<String, String> headers = tdHttpResponse.getHeaders();
    assertEquals("application/json", headers.get("Content-Type"));
  }

  private SimpleHttpResponse constructHttpResponse(Object payload) {
    SimpleHttpResponse response = SimpleHttpResponse.create(HttpStatus.SC_OK);
    response.setBody(String.valueOf(payload), ContentType.APPLICATION_JSON);
    return response;
  }

}
