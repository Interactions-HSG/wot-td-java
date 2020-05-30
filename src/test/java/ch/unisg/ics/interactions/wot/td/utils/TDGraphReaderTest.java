package ch.unisg.ics.interactions.wot.td.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Test;

import ch.unisg.ics.interactions.wot.td.ThingDescription;
import ch.unisg.ics.interactions.wot.td.affordances.Action;
import ch.unisg.ics.interactions.wot.td.affordances.HTTPForm;
import ch.unisg.ics.interactions.wot.td.schema.DataSchema;
import ch.unisg.ics.interactions.wot.td.schema.ObjectSchema;
import ch.unisg.ics.interactions.wot.td.vocabularies.TD;

public class TDGraphReaderTest {
  
  private static final String TEST_TD =
      "@prefix td: <http://www.w3.org/ns/td#> .\n" +
      "@prefix htv: <http://www.w3.org/2011/http#> .\n" +
      "@prefix js: <https://www.w3.org/2019/wot/json-schema#> .\n" +
      "\n" +
      "<http://example.org/#thing> a td:Thing ;\n" + 
      "    td:title \"My Thing\" ;\n" +
      "    td:security \"nosec_sc\" ;\n" +
      "    td:base <http://example.org/> ;\n" + 
      "    td:interaction [\n" + 
      "        a td:ActionAffordance ;\n" + 
      "        td:title \"My Action\" ;\n" + 
      "        td:form [\n" + 
      "            htv:methodName \"PUT\" ;\n" + 
      "            td:href <http://example.org/action> ;\n" + 
      "            td:contentType \"application/json\";\n" + 
      "            td:op \"invokeaction\";\n" + 
      "        ] ;\n" + 
      "        td:input [\n" + 
      "            a js:ObjectSchema ;\n" + 
      "            js:properties [\n" + 
      "                a js:NumberSchema ;\n" + 
      "                js:propertyName \"value\";\n" + 
      "            ] ;\n" +
      "            js:required \"value\" ;\n" +
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
    assertTrue(reader.readThingTypes().contains(TD.Thing.stringValue()));
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
    assertEquals(TD.ActionAffordance.stringValue(), action.getTypes().get(0));
    
    assertEquals(1, action.getForms().size());
    HTTPForm form = action.getForms().get(0);
    
    assertEquals("PUT", form.getMethodName());
    assertEquals("http://example.org/action", form.getHref());
    assertEquals("application/json", form.getContentType());
    assertEquals(1, form.getOperations().size());
    assertTrue(form.getOperations().contains("invokeaction"));
  }
  
  @Test
  public void testReadOneActionWithKeyValueInput() {
    TDGraphReader reader = new TDGraphReader(TEST_TD);
    
    Action action = reader.readActions().get(0);
    
    Optional<DataSchema> input = action.getInputSchema();
    assertTrue(input.isPresent());
    assertEquals(DataSchema.OBJECT, input.get().getType());
    
    ObjectSchema schema = (ObjectSchema) input.get();
    assertEquals(1, schema.getProperties().size());
    assertEquals(DataSchema.NUMBER, schema.getProperties().get("value").getType());
    
    assertEquals(1, schema.getRequiredProperties().size());
    assertEquals("value", schema.getRequiredProperties().get(0));
  }
  
  @Test
  public void testFullTDRead() {
    ThingDescription td = TDGraphReader.readFromString(TEST_TD);
    
    // Check metadata
    assertEquals("My Thing", td.getTitle());
    assertEquals("http://example.org/#thing", td.getThingURI().get());
    assertEquals(1, td.getTypes().size());
    assertTrue(td.getTypes().contains("http://www.w3.org/ns/td#Thing"));
    assertTrue(td.getSecurity().contains(ThingDescription.DEFAULT_SECURITY_SCHEMA));
    assertEquals(1, td.getActions().size());
    
    // Check action metadata
    Action action = td.getActions().get(0);
    assertEquals("My Action", action.getTitle().get());
    assertEquals(1, action.getForms().size());
    
    // Check action form
    HTTPForm form = action.getForms().get(0);
    assertEquals("PUT", form.getMethodName());
    assertEquals("http://example.org/action", form.getHref());
    assertEquals("application/json", form.getContentType());
    assertTrue(form.getOperations().contains("invokeaction"));
    
    // Check action input data schema
    ObjectSchema input = (ObjectSchema) action.getInputSchema().get();
    assertEquals(DataSchema.OBJECT, input.getType());
    assertEquals(1, input.getProperties().size());
    assertEquals(1, input.getRequiredProperties().size());
    
    assertEquals(DataSchema.NUMBER, input.getProperties().get("value").getType());
    assertTrue(input.getRequiredProperties().contains("value"));
  }
}
