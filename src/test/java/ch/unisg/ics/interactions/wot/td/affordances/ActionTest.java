package ch.unisg.ics.interactions.wot.td.affordances;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;

public class ActionTest {

  @Test
  public void testOneForm() {
    Form form = new Form("GET", "http://example.org", "application/json", 
        new HashSet<String>());
    
    ActionAffordance action = (new ActionAffordance.Builder(form)).build();
    
    List<Form> forms = action.getForms();
    
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
    assertEquals(1, action.getTypes().size());
    assertEquals("iot:TurnOn", action.getTypes().get(0));
  }
}
