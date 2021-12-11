package ch.unisg.ics.interactions.wot.td.clients;

import ch.unisg.ics.interactions.wot.td.schemas.*;
import org.eclipse.californium.core.coap.Option;
import org.eclipse.californium.core.coap.OptionNumberRegistry;
import org.eclipse.californium.core.coap.OptionSet;
import org.eclipse.californium.core.coap.Response;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.eclipse.californium.core.coap.CoAP.ResponseCode;
import static org.junit.Assert.*;

public class TDCoapResponseTest {
  private static final String PREFIX = "http://example.org/";
  private static final String USER_PAYLOAD = "{\"first_name\" : \"Andrei\", \"last_name\" : \"Ciortea\"}";

  @Test
  public void testNoPayload() {
    Response response = new Response(ResponseCode.VALID);
    TDCoapResponse testResponse = new TDCoapResponse(response);

    assertEquals("VALID", testResponse.getResponseCodeName());
    assertFalse(testResponse.getPayload().isPresent());
  }

  @Test
  public void testBooleanPayload() {
    Response response = new Response(ResponseCode.VALID);
    response.setPayload(String.valueOf(false));

    TDCoapResponse testResponse = new TDCoapResponse(response);

    assertEquals("VALID", testResponse.getResponseCodeName());
    assertTrue(testResponse.getPayload().isPresent());
    assertFalse(testResponse.getPayloadAsBoolean());
  }

  @Test
  public void testStringPayload() {
    Response response = new Response(ResponseCode.VALID);
    response.setPayload("test");

    TDCoapResponse testResponse = new TDCoapResponse(response);

    assertEquals("VALID", testResponse.getResponseCodeName());
    assertTrue(testResponse.getPayload().isPresent());
    assertEquals("test", testResponse.getPayloadAsString());

  }

  @Test
  public void testIntegerPayload() {
    Response response = new Response(ResponseCode.VALID);
    response.setPayload(String.valueOf(101));

    TDCoapResponse testResponse = new TDCoapResponse(response);

    assertEquals("VALID", testResponse.getResponseCodeName());
    assertTrue(testResponse.getPayload().isPresent());
    assertEquals(101, testResponse.getPayloadAsInteger().intValue());
  }

  @Test
  public void testDoublePayload() {
    Response response = new Response(ResponseCode.VALID);
    response.setPayload(String.valueOf(101.005));

    TDCoapResponse testResponse = new TDCoapResponse(response);

    assertEquals("VALID", testResponse.getResponseCodeName());
    assertTrue(testResponse.getPayload().isPresent());
    assertEquals(101.005, testResponse.getPayloadAsDouble(), 0.001);
  }

  @Test
  public void testObjectPayload() {
    Response response = new Response(ResponseCode.VALID);
    response.setPayload(USER_PAYLOAD);

    ObjectSchema schema = TDCoapRequestTest.USER_SCHEMA;
    TDCoapResponse testResponse = new TDCoapResponse(response);

    assertEquals("VALID", testResponse.getResponseCodeName());
    assertTrue(testResponse.getPayload().isPresent());
    Map<String, Object> payload = testResponse.getPayloadAsObject(schema);

    assertEquals(2, payload.size());
    assertEquals("Andrei", payload.get(PREFIX + "FirstName"));
    assertEquals("Ciortea", payload.get(PREFIX + "LastName"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testObjectRequiredPayload() {
    Response response = new Response(ResponseCode.VALID);
    response.setPayload("{\"first_name\" : \"Andrei\"}");

    ObjectSchema schema = TDCoapRequestTest.USER_SCHEMA;

    new TDCoapResponse(response).getPayloadAsObject(schema);
  }

  @Test
  public void testNestedObjectPayload() {

    String prefix = "http://example.org/";
    ObjectSchema schema = new ObjectSchema.Builder()
      .addProperty("count", new IntegerSchema.Builder()
        .addSemanticType(prefix + "Count")
        .build())
      .addProperty("user", TDCoapRequestTest.USER_SCHEMA)
      .build();

    Response response = new Response(ResponseCode.VALID);
    response.setPayload("{\n" +
      "  \"count\" : 1,\n" +
      "  \"user\" : " + USER_PAYLOAD + "\n" +
      "}");

    TDCoapResponse testResponse = new TDCoapResponse(response);

    assertEquals("VALID", testResponse.getResponseCodeName());
    assertTrue(testResponse.getPayload().isPresent());

    Map<String, Object> payload = testResponse.getPayloadAsObject(schema);
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

    ArraySchema schema = new ArraySchema.Builder()
      .addItem(new StringSchema.Builder().build())
      .addItem(new IntegerSchema.Builder().build())
      .addItem(new NumberSchema.Builder().build())
      .addItem(new BooleanSchema.Builder().build())
      .addItem(new NullSchema.Builder().build())
      .build();

    Response response = new Response(ResponseCode.VALID);
    response.setPayload("[\"my_string\", 1.5, 2, true, null]");

    TDCoapResponse testResponse = new TDCoapResponse(response);

    assertEquals("VALID", testResponse.getResponseCodeName());
    assertTrue(testResponse.getPayload().isPresent());

    List<Object> payload = testResponse.getPayloadAsArray(schema);
    assertEquals(5, payload.size());
    assertTrue(payload.contains("my_string"));
    assertTrue(payload.contains(1.5));
    assertTrue(payload.contains(2.0));
    assertTrue(payload.contains(true));
    assertTrue(payload.contains(null));
  }

  @Test
  public void testIntegerArrayPayload() {

    ArraySchema schema = new ArraySchema.Builder()
      .addItem(new IntegerSchema.Builder().build())
      .build();

    Response response = new Response(ResponseCode.VALID);
    response.setPayload("[1, 2, 3]");

    TDCoapResponse testResponse = new TDCoapResponse(response);

    assertEquals("VALID", testResponse.getResponseCodeName());
    assertTrue(testResponse.getPayload().isPresent());

    List<Object> payload = testResponse.getPayloadAsArray(schema);
    assertEquals(3, payload.size());
    assertTrue(payload.contains(1));
    assertTrue(payload.contains(2));
    assertTrue(payload.contains(3));
  }

  @Test
  public void testObjectArrayPayload() {

    String prefix = "http://example.org/";
    ArraySchema schema = new ArraySchema.Builder()
      .addItem(TDCoapRequestTest.USER_SCHEMA)
      .build();

    Response response = new Response(ResponseCode.VALID);
    response.setPayload("[" + USER_PAYLOAD + "]");

    TDCoapResponse testResponse = new TDCoapResponse(response);

    assertEquals("VALID", testResponse.getResponseCodeName());
    assertTrue(testResponse.getPayload().isPresent());

    List<Object> payload = testResponse.getPayloadAsArray(schema);
    assertEquals(1, payload.size());

    @SuppressWarnings("unchecked")
    Map<String, Object> user = (Map<String, Object>) payload.get(0);
    assertEquals("Andrei", user.get(prefix + "FirstName"));
    assertEquals("Ciortea", user.get(prefix + "LastName"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testArrayMinItemsPayload() {

    ArraySchema schema = new ArraySchema.Builder()
      .addItem(new IntegerSchema.Builder().build())
      .addMinItems(4)
      .build();

    Response response = new Response(ResponseCode.VALID);
    response.setPayload("[1, 2, 3]");

    new TDCoapResponse(response).getPayloadAsArray(schema);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testArrayMaxItemsPayload() {

    ArraySchema schema = new ArraySchema.Builder()
      .addItem(new IntegerSchema.Builder().build())
      .addMaxItems(2)
      .build();

    Response response = new Response(ResponseCode.VALID);
    response.setPayload("[1, 2, 3]");

    new TDCoapResponse(response).getPayloadAsArray(schema);
  }


  @Test
  public void testResponseOptions() {
    Response response = new Response(ResponseCode.VALID);
    OptionSet optionSet = new OptionSet();
    optionSet.addLocationPath("http://example.com");
    response.setOptions(optionSet);

    List<Option> optionList = response.getOptions().asSortedList();
    Map <String, String> expectedOptions = new HashMap<>();
    for (Option option : optionList) {
      String key = OptionNumberRegistry.toString(option.getNumber());
      String value = option.getStringValue();
      expectedOptions.put(key, value);
    }

    TDCoapResponse testResponse = new TDCoapResponse(response);
    Map <String, String> testOptions  = testResponse.getOptions();
    assertTrue(testOptions.containsKey("Location-Path"));
    assertEquals(testOptions.get("Location-Path"), "http://example.com");
  }

}
