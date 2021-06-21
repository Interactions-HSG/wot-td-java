package ch.unisg.ics.interactions.wot.td.clients;

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
import java.util.Optional;

import static org.junit.Assert.assertFalse;

public class TDCoapResponseTest {
  private static final String PREFIX = "coap://example.org/";
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

    BASE_URL = "coap://localhost:" + port;
    server.start();
  }

  @After
  public void tearDown() throws Exception {
    server.stop();
  }

  @Test
  public void testNoPayload() throws ConnectorException, IOException {

    Request request = new Request(CoAP.Code.GET);
    request.setURI(BASE_URL + "/testNoResponsePayload");

    CoapResponse response = client.advanced(request);
    Optional<String> payload = new TDCoapResponse(response).getPayload();

    assertFalse(payload.isPresent());
  }

  @Test
  public void testBooleanPayload() throws ConnectorException, IOException {
    Request request = new Request(CoAP.Code.GET);
    request.setURI(BASE_URL + "/testBooleanResponsePayload");

    CoapResponse coapResponse = client.advanced(request);
    Optional<String> payload = new TDCoapResponse(coapResponse).getPayload();

    assertFalse(new TDCoapResponse(coapResponse).getPayloadAsBoolean());
  }

  public static int getFreePort() throws IOException {
    ServerSocket serverSocket = new ServerSocket(0);
    int port = serverSocket.getLocalPort();
    serverSocket.close();
    return port;
  }

}
