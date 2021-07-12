package ch.unisg.ics.interactions.wot.td.clients;

import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.Response;
import org.junit.Before;
import org.junit.Test;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.*;

public class TDCoapObserveRelationTest {

  private final CoapObserveRelation relationMock = mock(CoapObserveRelation.class);
  private final TDCoapObserveRelation relation = new TDCoapObserveRelation(relationMock);

  private String mockHolder = "no_invocation";

  @Before
  public void init() {
    Response response = new Response(CoAP.ResponseCode.VALID);
    response.setPayload("current");

    when(relationMock.getCurrentResponse()).thenReturn(response);
    when(relationMock.isCanceled()).thenReturn(false);

    doAnswer((Answer<Void>) invocation -> {
      mockHolder = "reactive_cancel";
      return null;
    }).when(relationMock).reactiveCancel();

    doAnswer((Answer<Void>) invocation -> {
      mockHolder = "proactive_cancel";
      return null;
    }).when(relationMock).proactiveCancel();

    doAnswer((Answer<Void>) invocation -> {
      mockHolder = "reregister";
      return null;
    }).when(relationMock).reregister();
  }

  @Test
  public void testGetCurrent() {
    TDCoapResponse response = relation.getCurrent();
    assertEquals("current", response.getPayloadAsString());
  }

  @Test
  public void testIsCanceled() {
    assertEquals(false, relation.isCanceled());
  }

  @Test
  public void testReactiveCancel() {
    assertNotEquals("reactive_cancel", mockHolder);
    relation.reactiveCancel();
    assertEquals("reactive_cancel", mockHolder);
  }

  @Test
  public void testProactiveCancel() {
    assertNotEquals("proactive_cancel", mockHolder);
    relation.proactiveCancel();
    assertEquals("proactive_cancel", mockHolder);
  }

  @Test
  public void testReregister() {
    assertNotEquals("reregister", mockHolder);
    relation.reregister();
    assertEquals("reregister", mockHolder);
  }
}
