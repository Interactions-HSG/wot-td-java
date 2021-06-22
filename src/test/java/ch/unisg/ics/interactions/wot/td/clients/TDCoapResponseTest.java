package ch.unisg.ics.interactions.wot.td.clients;

import ch.unisg.ics.interactions.wot.td.schemas.*;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.elements.exception.ConnectorException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;

public class TDCoapResponseTest {
  private static final String PREFIX = "http://example.org/";
  private static final String USER_PAYLOAD = "{\"first_name\" : \"Andrei\", \"last_name\" : \"Ciortea\"}";

  private final CoapClient client = new CoapClient();

  private static String BASE_URL;
  private CoapServer server;


  @Before
  public void setUp() throws Exception {
    int port = getFreePort();
    server = new CoapServer(NetworkConfig.createStandardWithoutFile(), port);
    server.add(new CoapResource("testNoResponsePayload") {

      @Override
      public void handleGET(CoapExchange exchange) {
        exchange.respond(CoAP.ResponseCode.VALID);
      }
    });

    server.add(new CoapResource("testBooleanResponsePayload") {

      @Override
      public void handleGET(CoapExchange exchange) {
        exchange.respond(CoAP.ResponseCode.VALID, String.valueOf(false));
      }
    });

    server.add(new CoapResource("testStringResponsePayload") {

      @Override
      public void handleGET(CoapExchange exchange) {
        exchange.respond(CoAP.ResponseCode.VALID, "test");
      }
    });

    server.add(new CoapResource("testIntegerResponsePayload") {

      @Override
      public void handleGET(CoapExchange exchange) {
        exchange.respond(CoAP.ResponseCode.VALID, String.valueOf(101));
      }
    });

    server.add(new CoapResource("testDoubleResponsePayload") {

      @Override
      public void handleGET(CoapExchange exchange) {
        exchange.respond(CoAP.ResponseCode.VALID, String.valueOf(101.005));
      }
    });

    server.add(new CoapResource("testObjectResponsePayload") {

      @Override
      public void handleGET(CoapExchange exchange) {
        exchange.respond(CoAP.ResponseCode.VALID, USER_PAYLOAD);
      }
    });

    server.add(new CoapResource("testObjectRequiredResponsePayload") {

      @Override
      public void handleGET(CoapExchange exchange) {
        exchange.respond(CoAP.ResponseCode.VALID, "{\"first_name\" : \"Andrei\"}");
      }
    });

    server.add(new CoapResource("testNestedObjectResponsePayload") {

      @Override
      public void handleGET(CoapExchange exchange) {
        String nestedPayload = "{\n" +
          "  \"count\" : 1,\n" +
          "  \"user\" : " + USER_PAYLOAD + "\n" +
          "}";
        exchange.respond(CoAP.ResponseCode.VALID, nestedPayload);
      }
    });

    server.add(new CoapResource("testArrayResponsePayload") {

      @Override
      public void handleGET(CoapExchange exchange) {
        exchange.respond(CoAP.ResponseCode.VALID, "[\"my_string\", 1.5, 2, true, null]");
      }
    });

    server.add(new CoapResource("testIntegerArrayResponsePayload") {

      @Override
      public void handleGET(CoapExchange exchange) {
        exchange.respond(CoAP.ResponseCode.VALID, "[1, 2, 3]");
      }
    });

    server.add(new CoapResource("testObjectArrayResponsePayload") {

      @Override
      public void handleGET(CoapExchange exchange) {
        exchange.respond(CoAP.ResponseCode.VALID, "[" + USER_PAYLOAD + "]");
      }
    });

    BASE_URL = "coap://localhost:" + port;
    server.start();
  }

  @After
  public void tearDown() throws Exception {
    server.stop();
  }

  @Test
  public void testNoPayload() throws ConnectorException, IOException {
    CoapResponse response = makeRequest(CoAP.Code.GET, "/testNoResponsePayload");
    TDCoapResponse coapResponse = new TDCoapResponse(response);
    Optional<String> payload = coapResponse.getPayload();

    assertEquals("VALID", coapResponse.getResponseCode());
    assertFalse(payload.isPresent());

  }

  @Test
  public void testBooleanPayload() throws ConnectorException, IOException {
    CoapResponse response = makeRequest(CoAP.Code.GET, "/testBooleanResponsePayload");
    assertFalse(new TDCoapResponse(response).getPayloadAsBoolean());
  }

  @Test
  public void testStringPayload() throws ConnectorException, IOException {
    CoapResponse response = makeRequest(CoAP.Code.GET, "/testStringResponsePayload");
    assertEquals("test", new TDCoapResponse(response).getPayloadAsString());
  }

  @Test
  public void testIntegerPayload() throws ConnectorException, IOException {
    CoapResponse response = makeRequest(CoAP.Code.GET, "/testIntegerResponsePayload");
    assertEquals(101, new TDCoapResponse(response).getPayloadAsInteger().intValue());
  }

  @Test
  public void testDoublePayload() throws ConnectorException, IOException {
    CoapResponse response = makeRequest(CoAP.Code.GET, "/testDoubleResponsePayload");
    assertEquals(101.005, new TDCoapResponse(response).getPayloadAsDouble().doubleValue(), 0.001);
  }

  @Test
  public void testObjectPayload() throws ConnectorException, IOException {
    ObjectSchema schema = TDCoapRequestTest.USER_SCHEMA;
    CoapResponse response = makeRequest(CoAP.Code.GET, "/testObjectResponsePayload");
    Map<String, Object> payload = new TDCoapResponse(response).getPayloadAsObject(schema);

    assertEquals(2, payload.size());
    assertEquals("Andrei", payload.get(PREFIX + "FirstName"));
    assertEquals("Ciortea", payload.get(PREFIX + "LastName"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testObjectRequiredPayload() throws ConnectorException, IOException {
    ObjectSchema schema = TDCoapRequestTest.USER_SCHEMA;
    CoapResponse response = makeRequest(CoAP.Code.GET, "/testObjectRequiredResponsePayload");
    new TDCoapResponse(response).getPayloadAsObject(schema);
  }

  @Test
  public void testNestedObjectPayload() throws ConnectorException, IOException {

    String prefix = "http://example.org/";
    ObjectSchema schema = new ObjectSchema.Builder()
      .addProperty("count", new IntegerSchema.Builder()
        .addSemanticType(prefix + "Count")
        .build())
      .addProperty("user", TDCoapRequestTest.USER_SCHEMA)
      .build();

    CoapResponse response = makeRequest(CoAP.Code.GET, "/testNestedObjectResponsePayload");
    Map<String, Object> payload = new TDCoapResponse(response).getPayloadAsObject(schema);
    assertEquals(2, payload.size());
    assertEquals(1, payload.get(prefix + "Count"));

    @SuppressWarnings("unchecked")
    Map<String, Object> user = (Map<String, Object>) payload.get(prefix + "User");
    assertEquals(2, user.size());
    assertEquals("Andrei", user.get(prefix + "FirstName"));
    assertEquals("Ciortea", user.get(prefix + "LastName"));
  }

  @Test
  public void testPrimitiveArrayPayload() throws ConnectorException, IOException {

    ArraySchema schema = new ArraySchema.Builder()
      .addItem(new StringSchema.Builder().build())
      .addItem(new IntegerSchema.Builder().build())
      .addItem(new NumberSchema.Builder().build())
      .addItem(new BooleanSchema.Builder().build())
      .addItem(new NullSchema.Builder().build())
      .build();

    CoapResponse response = makeRequest(CoAP.Code.GET, "/testArrayResponsePayload");
    List<Object> payload = new TDCoapResponse(response).getPayloadAsArray(schema);
    assertEquals(5, payload.size());
    assertTrue(payload.contains("my_string"));
    assertTrue(payload.contains(1.5));
    assertTrue(payload.contains(2.0));
    assertTrue(payload.contains(true));
    assertTrue(payload.contains(null));
  }

  @Test
  public void testIntegerArrayPayload() throws ConnectorException, IOException {

    ArraySchema schema = new ArraySchema.Builder()
      .addItem(new IntegerSchema.Builder().build())
      .build();

    CoapResponse response = makeRequest(CoAP.Code.GET, "/testIntegerArrayResponsePayload");
    List<Object> payload = new TDCoapResponse(response).getPayloadAsArray(schema);
    assertEquals(3, payload.size());
    assertTrue(payload.contains(1));
    assertTrue(payload.contains(2));
    assertTrue(payload.contains(3));
  }

  @Test
  public void testObjectArrayPayload() throws ConnectorException, IOException {

    String prefix = "http://example.org/";
    ArraySchema schema = new ArraySchema.Builder()
      .addItem(TDCoapRequestTest.USER_SCHEMA)
      .build();

    CoapResponse response = makeRequest(CoAP.Code.GET, "/testObjectArrayResponsePayload");
    List<Object> payload = new TDCoapResponse(response).getPayloadAsArray(schema);
    assertEquals(1, payload.size());

    @SuppressWarnings("unchecked")
    Map<String, Object> user = (Map<String, Object>) payload.get(0);
    assertEquals("Andrei", user.get(prefix + "FirstName"));
    assertEquals("Ciortea", user.get(prefix + "LastName"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testArrayMinItemsPayload() throws ConnectorException, IOException {

    ArraySchema schema = new ArraySchema.Builder()
      .addItem(new IntegerSchema.Builder().build())
      .addMinItems(4)
      .build();

    CoapResponse response = makeRequest(CoAP.Code.GET, "/testIntegerArrayResponsePayload");
    new TDCoapResponse(response).getPayloadAsArray(schema);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testArrayMaxItemsPayload() throws ConnectorException, IOException {

    ArraySchema schema = new ArraySchema.Builder()
      .addItem(new IntegerSchema.Builder().build())
      .addMaxItems(2)
      .build();

    CoapResponse response = makeRequest(CoAP.Code.GET, "/testIntegerArrayResponsePayload");
    new TDCoapResponse(response).getPayloadAsArray(schema);
  }

  private CoapResponse makeRequest(CoAP.Code code, String resourceUrl) throws ConnectorException, IOException {
    Request request = new Request(code);
    request.setURI(BASE_URL + resourceUrl);
    return client.advanced(request);
  }

  private static int getFreePort() throws IOException {
    ServerSocket serverSocket = new ServerSocket(0);
    int port = serverSocket.getLocalPort();
    serverSocket.close();
    return port;
  }

}
