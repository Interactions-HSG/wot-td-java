package ch.unisg.ics.interactions.wot.td.clients;

import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.Response;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TDCoapObserveRelationTest {

  private final CoapObserveRelation relationMock = mock(CoapObserveRelation.class);
  private final TDCoapObserveRelation relation = new TDCoapObserveRelation(relationMock);

  @Before
  public void init() {
    Response response = new Response(CoAP.ResponseCode.VALID);
    response.setPayload("current");

    when(relationMock.getCurrentResponse()).thenReturn(response);
    when(relationMock.isCanceled()).thenReturn(false);
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
}
