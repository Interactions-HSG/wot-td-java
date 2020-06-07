package ch.unisg.ics.interactions.wot.td.affordances;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;

import ch.unisg.ics.interactions.wot.td.vocabularies.TD;

public class ActionAffordanceTest {
  
  private ActionAffordance test_action;
  private Form form;
  
  @Before
  public void init() {
    Set<String> operationTypes = Stream.of(TD.invokeAction.stringValue())
        .collect(Collectors.toCollection(HashSet::new));
    
    form = new Form("PUT", "http://example.org/action", "application/json", operationTypes);
    
    test_action = new ActionAffordance.Builder(form).build();
  }

  @Test
  public void testOneForm() {
    List<Form> forms = test_action.getForms();
    assertEquals(1, forms.size());
    assertEquals(form, forms.get(0));
  }
  
  @Test
  public void testMultipleForms() {
    Form form1 = new Form("GET", "http://example.org", "application/json", 
        new HashSet<String>());
    
    Form form2 = new Form("POST", "http://example.org", "application/json", 
        new HashSet<String>());
    
    Form form3 = new Form("PUT", "http://example.org", "application/json", 
        new HashSet<String>());
    
    List<Form> formList = new ArrayList<Form>(Arrays.asList(form1, form2, form3));
    
    ActionAffordance action = (new ActionAffordance.Builder(formList)).build();
    List<Form> forms = action.getForms();
    
    assertEquals(3, forms.size());
    assertEquals(form1, forms.get(0));
    assertEquals(form2, forms.get(1));
    assertEquals(form3, forms.get(2));
  }
  
  @Test
  public void testDefaultOperationType() {
    // TODO
  }
  
  @Test
  public void testFullOptionAction() {
    Form form = new Form("GET", "http://example.org", "application/json", 
        new HashSet<String>());
    
    ActionAffordance action = (new ActionAffordance.Builder(form))
        .addTitle("Turn on")
        .addSemanticType("iot:TurnOn")
        // TODO: add schema as well
        //.addInputSchema(inputSchema)
        .build();
    
    assertEquals("Turn on", action.getTitle().get());
    assertEquals(1, action.getSemanticTypes().size());
    assertEquals("iot:TurnOn", action.getSemanticTypes().get(0));
  }
}
