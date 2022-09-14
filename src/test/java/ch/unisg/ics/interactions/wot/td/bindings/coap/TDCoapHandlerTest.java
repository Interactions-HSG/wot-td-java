package ch.unisg.ics.interactions.wot.td.bindings.coap;

import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.Response;
import org.junit.Test;

import static org.junit.Assert.*;
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

    TDCoapHandler handler = new TDCoapHandler();
    handler.registerResponseCallback(r -> {
      if (r.getStatus().equals(ch.unisg.ics.interactions.wot.td.bindings.Response.ResponseStatus.OK)) {
        testValue[0] = (String) r.getPayload().get();
      }
    });

    assertEquals("initial", testValue[0]);

    handler.onLoad(responseMock);
    assertEquals("handle_load", testValue[0]);
    assertTrue(handler.getLastResponse().isPresent());

    handler.onError();
    assertFalse(handler.getLastResponse().isPresent());
  }

}
