package ch.unisg.ics.interactions.wot.td.bindings.coap;

import ch.unisg.ics.interactions.wot.td.bindings.coap.TDCoapHandler;
import ch.unisg.ics.interactions.wot.td.bindings.coap.TDCoapResponse;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.Response;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TDCoapHandlerTest {

  @Test
  public void testTDCoapHandler() {
    final String[] testValue = {"initial"};

    Response response = new Response(CoAP.ResponseCode.VALID);
    response.setPayload("handle_load");

    CoapResponse responseMock = mock(CoapResponse.class);
    assertNotNull(responseMock);
    when(responseMock.advanced()).thenReturn(response);

    TDCoapHandler handler = new TDCoapHandler() {
      @Override
      public void handleLoad(TDCoapResponse response) {
        testValue[0] = response.getPayloadAsString();
      }

      @Override
      public void handleError() {
        testValue[0] = "handle_error";
      }
    };

    assertEquals("initial", testValue[0]);

    handler.getCoapHandler().onLoad(responseMock);
    assertEquals("handle_load", testValue[0]);

    handler.getCoapHandler().onError();
    assertEquals("handle_error", testValue[0]);
  }

}
