package ch.unisg.ics.interactions.wot.td.affordances;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import ch.unisg.ics.interactions.wot.td.vocabularies.TD;

public class ActionAffordanceTest {

  private ActionAffordance testAction;
  private Form form;

  @Before
  public void init() {
    form = new Form.Builder("http://example.org/action").build();
    testAction = new ActionAffordance.Builder(form).build();
  }

  @Test
  public void testOneForm() {
    List<Form> forms = testAction.getForms();
    assertEquals(1, forms.size());
    assertEquals(form, forms.get(0));
  }

  @Test
  public void testMultipleForms() {
    Form form1 = new Form.Builder("http://example.org")
        .setMethodName("GET")
        .setContentType("application/json")
        .build();

    Form form2 = new Form.Builder("http://example.org")
        .setMethodName("POST")
        .setContentType("application/json")
        .build();

    Form form3 = new Form.Builder("http://example.org")
        .setMethodName("PUT")
        .setContentType("application/json")
        .build();

    List<Form> formList = new ArrayList<Form>(Arrays.asList(form1, form2, form3));

    ActionAffordance action = (new ActionAffordance.Builder(formList)).build();
    List<Form> forms = action.getForms();

    assertEquals(3, forms.size());
    assertEquals(form1, forms.get(0));
    assertEquals(form2, forms.get(1));
    assertEquals(form3, forms.get(2));
  }

  @Test
  public void testDefaultValues() {
    String invokeAction = TD.invokeAction;
    assertTrue(testAction.hasFormWithOperationType(invokeAction));
    assertEquals("POST", testAction.getForms().get(0).getMethodName(invokeAction).get());
  }

  @Test
  public void testFullOptionAction() {
    Form form = new Form.Builder("http://example.org").setMethodName("GET").build();

    ActionAffordance action = (new ActionAffordance.Builder(form))
        .addName("turnOn")
        .addTitle("Turn on")
        .addSemanticType("iot:TurnOn")
        // TODO: add schema as well
        //.addInputSchema(inputSchema)
        .build();

    assertEquals("turnOn", action.getName());
    assertEquals("Turn on", action.getTitle().get());
    assertEquals(1, action.getSemanticTypes().size());
    assertEquals("iot:TurnOn", action.getSemanticTypes().get(0));
  }
}
