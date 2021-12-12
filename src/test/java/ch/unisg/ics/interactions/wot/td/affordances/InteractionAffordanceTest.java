package ch.unisg.ics.interactions.wot.td.affordances;

import ch.unisg.ics.interactions.wot.td.io.InvalidTDException;
import ch.unisg.ics.interactions.wot.td.vocabularies.COV;
import ch.unisg.ics.interactions.wot.td.vocabularies.TD;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class InteractionAffordanceTest {
  private static final String prefix = "http://example.org";
  private InteractionAffordance test_affordance;

  @Before
  public void init() {
    Form form1 = new Form.Builder("http://example.org/property1")
      .addOperationType(TD.readProperty)
      .build();

    Form form2 = new Form.Builder("https://example.org/property2")
      .addOperationType(TD.writeProperty)
      .build();

    Form form3 = new Form.Builder("coap://example.org/property3")
      .addOperationType(TD.observeProperty)
      .addSubProtocol(COV.observe)
      .build();

    Form form4 = new Form.Builder("coaps://example.org/property4")
      .addOperationType(TD.unobserveProperty)
      .addSubProtocol(COV.observe)
      .build();

    test_affordance = new InteractionAffordance("my_affordance",
      Optional.of("My Affordance"), Arrays.asList(prefix + "Type1", prefix + "Type2"),
      Arrays.asList(form1, form2, form3, form4));
  }

  @Test(expected = InvalidTDException.class)
  public void testAffordanceWithoutNameThrowsException() {
    new InteractionAffordance(null,
      Optional.of("My Affordance"), Arrays.asList(prefix + "Type1", prefix + "Type2"), null);
  }

  @Test
  public void testHasFormForOperationType() {
    assertTrue(test_affordance.hasFormWithOperationType(TD.readProperty));
    assertTrue(test_affordance.hasFormWithOperationType(TD.writeProperty));
    assertTrue(test_affordance.hasFormWithOperationType(TD.observeProperty));
    assertTrue(test_affordance.hasFormWithOperationType(TD.unobserveProperty));
  }

  @Test
  public void testNoFormForOperationType() {
    assertFalse(test_affordance.hasFormWithOperationType("bla"));
  }

  @Test
  public void testHasFormForSubprotocol() {
    assertFalse(test_affordance.hasFormWithSubProtocol(TD.readProperty, COV.observe));
    assertFalse(test_affordance.hasFormWithSubProtocol(TD.writeProperty, COV.observe));
    assertTrue(test_affordance.hasFormWithSubProtocol(TD.observeProperty, COV.observe));
    assertTrue(test_affordance.hasFormWithSubProtocol(TD.unobserveProperty, COV.observe));
  }

  @Test
  public void testGetFirstFormForSubProtocol() {
    Optional<Form> observeForm1 = test_affordance.getFirstFormForSubProtocol(TD.readProperty,
      COV.observe);
    Optional<Form> observeForm2 = test_affordance.getFirstFormForSubProtocol(TD.writeProperty,
      COV.observe);
    Optional<Form> observeForm3 = test_affordance.getFirstFormForSubProtocol(TD.observeProperty,
      COV.observe);
    Optional<Form> observeForm4 = test_affordance.getFirstFormForSubProtocol(TD.unobserveProperty,
      COV.observe);

    assertFalse(observeForm1.isPresent());
    assertFalse(observeForm2.isPresent());
    assertTrue(observeForm3.isPresent());
    assertTrue(observeForm4.isPresent());

    assertEquals(observeForm3.get().getTarget(),"coap://example.org/property3");
    assertEquals(observeForm4.get().getTarget(),"coaps://example.org/property4");
  }

  @Test
  public void testHasFormForProtocol() {
    assertTrue(test_affordance.hasFormWithProtocol(TD.readProperty, "HTTP"));
    assertTrue(test_affordance.hasFormWithProtocol(TD.writeProperty, "HTTP"));
    assertTrue(test_affordance.hasFormWithProtocol(TD.observeProperty, "CoAP"));
    assertTrue(test_affordance.hasFormWithProtocol(TD.unobserveProperty, "CoAP"));
  }

  @Test
  public void testHasNoFormForProtocol() {
    assertFalse(test_affordance.hasFormWithProtocol(TD.observeProperty, "HTTP"));
    assertFalse(test_affordance.hasFormWithProtocol(TD.readProperty, "CoAP"));
    assertFalse(test_affordance.hasFormWithProtocol(TD.writeProperty, "CoAP"));
  }

  @Test
  public void testGetFirstFormForHttpProtocol() {
    Optional<Form> httpForm1 = test_affordance.getFirstFormForProtocol(TD.readProperty,
      "HTTP");
    Optional<Form> httpForm2 = test_affordance.getFirstFormForProtocol(TD.writeProperty,
      "HTTP");
    Optional<Form> httpForm3 = test_affordance.getFirstFormForProtocol(TD.observeProperty,
      "HTTP");
    Optional<Form> httpForm4 = test_affordance.getFirstFormForSubProtocol(TD.unobserveProperty,
      "HTTP");

    assertTrue(httpForm1.isPresent());
    assertTrue(httpForm2.isPresent());
    assertFalse(httpForm3.isPresent());
    assertFalse(httpForm4.isPresent());

    assertTrue(httpForm1.get().getProtocol().isPresent());
    assertTrue(httpForm2.get().getProtocol().isPresent());

    assertEquals(httpForm1.get().getProtocol().get(), "HTTP");
    assertEquals(httpForm2.get().getProtocol().get(), "HTTP");

    assertEquals(httpForm1.get().getTarget(),"http://example.org/property1");
    assertEquals(httpForm2.get().getTarget(),"https://example.org/property2");
  }

  @Test
  public void testGetFirstFormForCoapProtocol() {
    Optional<Form> coapForm1 = test_affordance.getFirstFormForProtocol(TD.readProperty,
      "CoAP");
    Optional<Form> coapForm2 = test_affordance.getFirstFormForProtocol(TD.writeProperty,
      "CoAP");
    Optional<Form> coapForm3 = test_affordance.getFirstFormForProtocol(TD.observeProperty,
      "CoAP");
    Optional<Form> coapForm4 = test_affordance.getFirstFormForProtocol(TD.unobserveProperty,
      "CoAP");

    assertFalse(coapForm1.isPresent());
    assertFalse(coapForm2.isPresent());
    assertTrue(coapForm3.isPresent());
    assertTrue(coapForm4.isPresent());

    assertTrue(coapForm3.get().getProtocol().isPresent());
    assertTrue(coapForm4.get().getProtocol().isPresent());

    assertEquals(coapForm3.get().getProtocol().get(), "CoAP");
    assertEquals(coapForm4.get().getProtocol().get(), "CoAP");

    assertEquals(coapForm3.get().getTarget(),"coap://example.org/property3");
    assertEquals(coapForm4.get().getTarget(),"coaps://example.org/property4");
  }

  @Test
  public void testHasSemanticType() {
    assertTrue(test_affordance.hasSemanticType(prefix + "Type1"));
  }

  @Test
  public void testHasNotSemanticType() {
    assertFalse(test_affordance.hasSemanticType(prefix + "Type0"));
  }

  @Test
  public void testHasOneSemanticType() {
    assertTrue(test_affordance.hasOneSemanticType(Arrays.asList(prefix + "Type0",
      prefix + "Type1")));
  }

  @Test
  public void testHasNotOneSemanticType() {
    assertFalse(test_affordance.hasOneSemanticType(Arrays.asList(prefix + "Type3",
      prefix + "Type4")));
  }

  @Test
  public void testHasAllSemanticTypes() {
    assertTrue(test_affordance.hasAllSemanticTypes(Arrays.asList(prefix + "Type1",
      prefix + "Type2")));
  }

  @Test
  public void testHasNotAllSemanticTypes() {
    assertFalse(test_affordance.hasAllSemanticTypes(Arrays.asList(prefix + "Type1",
      prefix + "Type2", prefix + "Type3")));
  }

  @Test
  public void testGetFirstFormForOperationType() {
    assertTrue(test_affordance.getFirstFormForOperationType(TD.readProperty)
      .isPresent());
  }

  @Test
  public void testNoFirstFormForOperationType() {
    assertFalse(test_affordance.getFirstFormForOperationType(TD.invokeAction)
      .isPresent());
  }
}
