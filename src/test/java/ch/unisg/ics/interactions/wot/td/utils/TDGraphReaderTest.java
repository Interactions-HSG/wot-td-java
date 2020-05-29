package ch.unisg.ics.interactions.wot.td.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ch.unisg.ics.interactions.wot.td.affordances.Action;
import ch.unisg.ics.interactions.wot.td.affordances.HTTPForm;
import ch.unisg.ics.interactions.wot.td.vocabularies.TDVocab;

public class TDGraphReaderTest {
  
  private static final String TEST_TD = "<http://example.org/#thing> a <http://www.w3.org/ns/td#Thing> ;\n" + 
      "    <http://www.w3.org/ns/td#title> \"My Thing\" ;\n" +
      "    <http://www.w3.org/ns/td#security> \"nosec_sc\" ;\n" +
      "    <http://www.w3.org/ns/td#base> <http://example.org/> ;\n" + 
      "    <http://www.w3.org/ns/td#interaction> [\n" + 
      "        a <http://www.w3.org/ns/td#Action> ;\n" + 
      "        <http://www.w3.org/ns/td#title> \"My Action\" ;\n" + 
      "        <http://www.w3.org/ns/td#form> [\n" + 
      "            <http://www.w3.org/ns/td#methodName> \"PUT\" ;\n" + 
      "            <http://www.w3.org/ns/td#href> <http://example.org/action> ;\n" + 
      "            <http://www.w3.org/ns/td#contentType> \"application/json\";\n" + 
      "            <http://www.w3.org/ns/td#op> \"invokeaction\";\n" + 
      "        ] ;\n" + 
      "        <http://www.w3.org/ns/td#input> [\n" + 
      "            <http://www.w3.org/ns/td#schemaType> <http://www.w3.org/ns/td#Object> ;\n" + 
      "            <http://www.w3.org/ns/td#field> [\n" + 
      "                <http://www.w3.org/ns/td#title> \"value\";\n" + 
      "                <http://www.w3.org/ns/td#schema> [\n" + 
      "                    <http://www.w3.org/ns/td#schemaType> <http://www.w3.org/ns/td#Number>\n" + 
      "                ]\n" + 
      "            ]\n" + 
      "        ]\n" + 
      "    ] ." ;
  
  @Test
  public void testReadTitle() {
    TDGraphReader reader = new TDGraphReader(TEST_TD);
    
    assertEquals("My Thing", reader.readThingTitle());
  }
  
  @Test
  public void testReadThingTypes() {
    TDGraphReader reader = new TDGraphReader(TEST_TD);
    
    assertEquals(1, reader.readThingTypes().size());
    assertTrue(reader.readThingTypes().contains(TDVocab.Thing.getIRIString()));
  }
  
  @Test
  public void testReadBaseURI() {
    TDGraphReader reader = new TDGraphReader(TEST_TD);
    
    assertEquals("http://example.org/", reader.readBaseURI().get());
  }
  
  @Test
  public void testReadOneSecuritySchema() {
    TDGraphReader reader = new TDGraphReader(TEST_TD);
    
    assertEquals(1, reader.readSecuritySchemas().size());
    assertTrue(reader.readSecuritySchemas().contains("nosec_sc"));
  }
  
  @Test
  public void testReadOneSimpleAction() {
    TDGraphReader reader = new TDGraphReader(TEST_TD);
    
    assertEquals(1, reader.readActions().size());
    Action action = reader.readActions().get(0);
    
    assertEquals("My Action", action.getTitle().get());
    assertEquals(1, action.getTypes().size());
    assertEquals(TDVocab.Action.getIRIString(), action.getTypes().get(0));
    
    assertEquals(1, action.getForms().size());
    HTTPForm form = action.getForms().get(0);
    
    assertEquals("PUT", form.getMethodName());
    assertEquals("http://example.org/action", form.getHref());
    assertEquals("application/json", form.getContentType());
    assertEquals(1, form.getOperations().size());
    assertTrue(form.getOperations().contains("invokeaction"));
  }
}
