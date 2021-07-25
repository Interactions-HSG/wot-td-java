package ch.unisg.ics.interactions.wot.td.affordances;

import ch.unisg.ics.interactions.wot.td.vocabularies.COV;
import ch.unisg.ics.interactions.wot.td.vocabularies.TD;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.*;

public class ProtocolBindingTest {

  @Test
  public void testDefaultProtocolBinding() {
    String href = "coap://example.org/1";
    Optional<String> readPropertyMethodBinding = ProtocolBinding.getDefaultMethod(href, TD.readProperty);
    Optional<String> readPropertySubprotocolBinding = ProtocolBinding.getDefaultSubprotocol(href,
      TD.readProperty);

    assertTrue(readPropertyMethodBinding.isPresent());
    assertEquals("GET", readPropertyMethodBinding.get());
    assertFalse(readPropertySubprotocolBinding.isPresent());

    Optional<String> observePropertyMethodBinding = ProtocolBinding.getDefaultMethod(href,
      TD.observeProperty);
    Optional<String> observePropertySubprotocolBinding = ProtocolBinding.getDefaultSubprotocol(href,
      TD.observeProperty);

    assertTrue(observePropertyMethodBinding.isPresent());
    assertEquals("GET", observePropertyMethodBinding.get());
    assertTrue(observePropertySubprotocolBinding.isPresent());
    assertEquals(COV.observe, observePropertySubprotocolBinding.get());
  }

  @Test
  public void testUnknownUriScheme() {
    String href = "unknown://example.org/1";
    assertFalse(ProtocolBinding.getDefaultMethod(href, TD.readProperty).isPresent());
    assertFalse(ProtocolBinding.getDefaultSubprotocol(href, TD.readProperty).isPresent());
  }

  @Test
  public void testMissingDefaultBinding() {
    String href = "http://example.org/1";
    String operationType = "http://example.org#unknownOp";
    assertFalse(ProtocolBinding.getDefaultMethod(href, operationType).isPresent());
    assertFalse(ProtocolBinding.getDefaultSubprotocol(href, operationType).isPresent());
  }
}
