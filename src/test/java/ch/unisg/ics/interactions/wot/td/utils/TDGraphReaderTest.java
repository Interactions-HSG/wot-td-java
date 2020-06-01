package ch.unisg.ics.interactions.wot.td.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Test;

import ch.unisg.ics.interactions.wot.td.ThingDescription;
import ch.unisg.ics.interactions.wot.td.affordances.ActionAffordance;
import ch.unisg.ics.interactions.wot.td.affordances.HTTPForm;
import ch.unisg.ics.interactions.wot.td.schemas.DataSchema;
import ch.unisg.ics.interactions.wot.td.schemas.IntegerSchema;
import ch.unisg.ics.interactions.wot.td.schemas.NumberSchema;
import ch.unisg.ics.interactions.wot.td.schemas.ObjectSchema;
import ch.unisg.ics.interactions.wot.td.vocabularies.TD;

public class TDGraphReaderTest {
  
  private static final String TEST_SIMPLE_TD =
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
      "                js:maximum 100.05 ;\n" + 
      "                js:minimum -100.05 ;\n" +
      "            ] ;\n" +
      "            js:required \"value\" ;\n" +
      "        ]\n" + 
      "    ] ." ;
  
  private static final String TEST_IO_HEAD =
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
      "        ] ;\n";
      
  private static final String TEST_IO_TAIL = "    ] ." ;
  
  @Test
  public void testReadTitle() {
    TDGraphReader reader = new TDGraphReader(TEST_SIMPLE_TD);
    
    assertEquals("My Thing", reader.readThingTitle());
  }
  
  @Test
  public void testReadThingTypes() {
    TDGraphReader reader = new TDGraphReader(TEST_SIMPLE_TD);
    
    assertEquals(1, reader.readThingTypes().size());
    assertTrue(reader.readThingTypes().contains(TD.Thing.stringValue()));
  }
  
  @Test
  public void testReadBaseURI() {
    TDGraphReader reader = new TDGraphReader(TEST_SIMPLE_TD);
    
    assertEquals("http://example.org/", reader.readBaseURI().get());
  }
  
  @Test
  public void testReadOneSecuritySchema() {
    TDGraphReader reader = new TDGraphReader(TEST_SIMPLE_TD);
    
    assertEquals(1, reader.readSecuritySchemas().size());
    assertTrue(reader.readSecuritySchemas().contains("nosec_sc"));
  }
  
  @Test
  public void testReadMultipleSecuritySchemas() {
    String testTD =
        "@prefix td: <http://www.w3.org/ns/td#> .\n" +
        "@prefix htv: <http://www.w3.org/2011/http#> .\n" +
        "@prefix js: <https://www.w3.org/2019/wot/json-schema#> .\n" +
        "\n" +
        "<http://example.org/#thing> a td:Thing ;\n" + 
        "    td:title \"My Thing\" ;\n" +
        "    td:security \"nosec_sc\", \"mysec_sc\", \"yoursec_sc\" ;\n" +
        "    td:base <http://example.org/> .";
    
    TDGraphReader reader = new TDGraphReader(testTD);
    
    assertTrue(reader.readSecuritySchemas().contains("nosec_sc"));
    assertTrue(reader.readSecuritySchemas().contains("mysec_sc"));
    assertTrue(reader.readSecuritySchemas().contains("yoursec_sc"));
  }
  
  @Test
  public void testReadOneSimpleAction() {
    TDGraphReader reader = new TDGraphReader(TEST_SIMPLE_TD);
    
    assertEquals(1, reader.readActions().size());
    ActionAffordance action = reader.readActions().get(0);
    
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
  public void testReadMultipleSimpleActions() {
    String testTD =
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
        "        td:title \"First Action\" ;\n" + 
        "        td:form [\n" + 
        "            htv:methodName \"PUT\" ;\n" + 
        "            td:href <http://example.org/action1> ;\n" + 
        "            td:contentType \"application/json\";\n" + 
        "            td:op \"invokeaction\";\n" + 
        "        ] ;\n" + 
        "    ] ;\n" +
        "    td:interaction [\n" + 
        "        a td:ActionAffordance ;\n" + 
        "        td:title \"Second Action\" ;\n" + 
        "        td:form [\n" + 
        "            htv:methodName \"PUT\" ;\n" + 
        "            td:href <http://example.org/action2> ;\n" + 
        "            td:contentType \"application/json\";\n" + 
        "            td:op \"invokeaction\";\n" + 
        "        ] ;\n" +
        "    ] ;\n" +
        "    td:interaction [\n" + 
        "        a td:ActionAffordance ;\n" + 
        "        td:title \"Third Action\" ;\n" + 
        "        td:form [\n" + 
        "            htv:methodName \"PUT\" ;\n" + 
        "            td:href <http://example.org/action3> ;\n" + 
        "            td:contentType \"application/json\";\n" + 
        "            td:op \"invokeaction\";\n" + 
        "        ] ;\n" + 
        "    ] ." ;
    
    TDGraphReader reader = new TDGraphReader(testTD);
    
    assertEquals(3, reader.readActions().size());
    
    List<String> actionTitles = reader.readActions().stream().map(action -> action.getTitle().get())
        .collect(Collectors.toList());
    
    assertTrue(actionTitles.contains("First Action"));
    assertTrue(actionTitles.contains("Second Action"));
    assertTrue(actionTitles.contains("Third Action"));
  }
  
  @Test
  public void testReadOneActionOneObjectInput() {
    String testSimpleObject =
        "        td:input [\n" + 
        "            a js:ObjectSchema ;\n" + 
        "            js:properties [\n" + 
        "                a js:BooleanSchema ;\n" +
        "                js:propertyName \"boolean_value\";\n" +
        "            ] ;\n" +
        "            js:properties [\n" + 
        "                a js:NumberSchema ;\n" +
        "                js:propertyName \"number_value\";\n" +
        "                js:maximum 100.05 ;\n" + 
        "                js:minimum -100.05 ;\n" +
        "            ] ;\n" +
        "            js:properties [\n" + 
        "                a js:IntegerSchema ;\n" +
        "                js:propertyName \"integer_value\";\n" +
        "                js:maximum 100 ;\n" + 
        "                js:minimum -100 ;\n" +
        "            ] ;\n" +
        "            js:properties [\n" + 
        "                a js:StringSchema ;\n" +
        "                js:propertyName \"string_value\";\n" +
        "            ] ;\n" +
        "            js:properties [\n" + 
        "                a js:NullSchema ;\n" +
        "                js:propertyName \"null_value\";\n" +
        "            ] ;\n" +
        "            js:required \"integer_value\", \"number_value\" ;\n" +
        "        ]\n";
    
    TDGraphReader reader = new TDGraphReader(TEST_IO_HEAD + testSimpleObject + TEST_IO_TAIL);
    
    ActionAffordance action = reader.readActions().get(0);
    
    Optional<DataSchema> input = action.getInputSchema();
    assertTrue(input.isPresent());
    assertEquals(DataSchema.OBJECT, input.get().getDatatype());
    
    ObjectSchema schema = (ObjectSchema) input.get();
    assertEquals(5, schema.getProperties().size());
    
    DataSchema booleanProperty = schema.getProperties().get("boolean_value");
    assertEquals(DataSchema.BOOLEAN, booleanProperty.getDatatype());
    
    DataSchema integerProperty = schema.getProperties().get("integer_value");
    assertEquals(DataSchema.INTEGER, integerProperty.getDatatype());
    assertEquals(-100, ((IntegerSchema) integerProperty).getMinimum().get().intValue());
    assertEquals(100, ((IntegerSchema) integerProperty).getMaximum().get().intValue());
    
    DataSchema numberProperty = schema.getProperties().get("number_value");
    assertEquals(DataSchema.NUMBER, numberProperty.getDatatype());
    assertEquals(-100.05, ((NumberSchema) numberProperty).getMinimum().get().doubleValue(), 0.001);
    assertEquals(100.05, ((NumberSchema) numberProperty).getMaximum().get().doubleValue(), 0.001);
    
    DataSchema stringProperty = schema.getProperties().get("string_value");
    assertEquals(DataSchema.STRING, stringProperty.getDatatype());
    
    DataSchema nullProperty = schema.getProperties().get("null_value");
    assertEquals(DataSchema.NULL, nullProperty.getDatatype());
    
    assertEquals(2, schema.getRequiredProperties().size());
    assertTrue(schema.getRequiredProperties().contains("integer_value"));
    assertTrue(schema.getRequiredProperties().contains("number_value"));
  }
  
  @Test
  public void testReadSimpleFullTD() {
    ThingDescription td = TDGraphReader.readFromString(TEST_SIMPLE_TD);
    
    // Check metadata
    assertEquals("My Thing", td.getTitle());
    assertEquals("http://example.org/#thing", td.getThingURI().get());
    assertEquals(1, td.getTypes().size());
    assertTrue(td.getTypes().contains("http://www.w3.org/ns/td#Thing"));
    assertTrue(td.getSecurity().contains(ThingDescription.DEFAULT_SECURITY_SCHEMA));
    assertEquals(1, td.getActions().size());
    
    // Check action metadata
    ActionAffordance action = td.getActions().get(0);
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
    assertEquals(DataSchema.OBJECT, input.getDatatype());
    assertEquals(1, input.getProperties().size());
    assertEquals(1, input.getRequiredProperties().size());
    
    assertEquals(DataSchema.NUMBER, input.getProperties().get("value").getDatatype());
    assertTrue(input.getRequiredProperties().contains("value"));
  }
  
}
