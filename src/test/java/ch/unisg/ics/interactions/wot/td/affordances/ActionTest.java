package ch.unisg.ics.interactions.wot.td.affordances;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import ch.unisg.ics.interactions.wot.td.affordances.Action;
import ch.unisg.ics.interactions.wot.td.affordances.HTTPForm;

public class ActionTest {

  @Test
  public void testOneForm() {
    HTTPForm form = new HTTPForm("GET", "http://example.org", "application/json", 
        new ArrayList<String>());
    
    Action action = (new Action.Builder(form)).build();
    
    List<HTTPForm> forms = action.getForms();
    
    assertEquals(1, forms.size());
    assertEquals(form, forms.get(0));
  }
  
  @Test
  public void testMultipleForms() {
    HTTPForm form1 = new HTTPForm("GET", "http://example.org", "application/json", 
        new ArrayList<String>());
    
    HTTPForm form2 = new HTTPForm("POST", "http://example.org", "application/json", 
        new ArrayList<String>());
    
    HTTPForm form3 = new HTTPForm("PUT", "http://example.org", "application/json", 
        new ArrayList<String>());
    
    List<HTTPForm> formList = new ArrayList<HTTPForm>(Arrays.asList(form1, form2, form3));
    
    Action action = (new Action.Builder(formList)).build();
    
    List<HTTPForm> forms = action.getForms();
    
    assertEquals(3, forms.size());
    
    assertEquals(form1, forms.get(0));
    assertEquals(form2, forms.get(1));
    assertEquals(form3, forms.get(2));
  }
  
  @Test
  public void testFullOptionAction() {
    HTTPForm form = new HTTPForm("GET", "http://example.org", "application/json", 
        new ArrayList<String>());
    
    Action action = (new Action.Builder(form))
        .addTitle("Turn on")
        .addType("iot:TurnOn")
        // TODO: add schema as well
        //.addInputSchema(inputSchema)
        .build();
    
    assertEquals("Turn on", action.getTitle().get());
    assertEquals(1, action.getTypes().size());
    assertEquals("iot:TurnOn", action.getTypes().get(0));
  }
}
