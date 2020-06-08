package ch.unisg.ics.interactions.wot.td.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.junit.Test;

import ch.unisg.ics.interactions.wot.td.ThingDescription;
import ch.unisg.ics.interactions.wot.td.affordances.ActionAffordance;
import ch.unisg.ics.interactions.wot.td.affordances.Form;
import ch.unisg.ics.interactions.wot.td.affordances.PropertyAffordance;
import ch.unisg.ics.interactions.wot.td.schemas.DataSchema;
import ch.unisg.ics.interactions.wot.td.schemas.IntegerSchema;
import ch.unisg.ics.interactions.wot.td.schemas.NumberSchema;
import ch.unisg.ics.interactions.wot.td.schemas.ObjectSchema;
import ch.unisg.ics.interactions.wot.td.vocabularies.TD;
import ch.unisg.ics.interactions.wot.td.vocabularies.WoTSec;

public class TDGraphReaderTest {
  
  private static final String TEST_SIMPLE_TD =
      "@prefix td: <https://www.w3.org/2019/wot/td#> .\n" +
      "@prefix htv: <http://www.w3.org/2011/http#> .\n" +
      "@prefix hctl: <https://www.w3.org/2019/wot/hypermedia#> .\n" +
      "@prefix dct: <http://purl.org/dc/terms/> .\n" +
      "@prefix wotsec: <https://www.w3.org/2019/wot/security#> .\n" +
      "@prefix js: <https://www.w3.org/2019/wot/json-schema#> .\n" +
      "\n" +
      "<http://example.org/#thing> a td:Thing ;\n" + 
      "    dct:title \"My Thing\" ;\n" +
      "    td:hasSecurityConfiguration [ a wotsec:NoSecurityScheme ] ;\n" +
      "    td:hasBase <http://example.org/> ;\n" +
      "    td:hasPropertyAffordance [\n" + 
      "        a td:PropertyAffordance, js:NumberSchema ;\n" +
      "        dct:title \"My Property\" ;\n" +
      "        td:isObservable true ;\n" +
      "        td:hasForm [\n" + 
      "            htv:methodName \"PUT\" ;\n" + 
      "            hctl:hasTarget <http://example.org/property> ;\n" + 
      "            hctl:forContentType \"application/json\";\n" + 
      "            hctl:hasOperationType td:writeProperty;\n" + 
      "        ] ;\n" + 
      "        td:hasForm [\n" + 
      "            htv:methodName \"GET\" ;\n" + 
      "            hctl:hasTarget <http://example.org/property> ;\n" + 
      "            hctl:forContentType \"application/json\";\n" + 
      "            hctl:hasOperationType td:readProperty;\n" + 
      "        ] ;\n" + 
      "    ] ;\n" + 
      "    td:hasActionAffordance [\n" + 
      "        a td:ActionAffordance ;\n" + 
      "        dct:title \"My Action\" ;\n" + 
      "        td:hasForm [\n" + 
      "            htv:methodName \"PUT\" ;\n" + 
      "            hctl:hasTarget <http://example.org/action> ;\n" + 
      "            hctl:forContentType \"application/json\";\n" + 
      "            hctl:hasOperationType td:invokeAction;\n" + 
      "        ] ;\n" + 
      "        td:hasInputSchema [\n" + 
      "            a js:ObjectSchema ;\n" + 
      "            js:properties [\n" + 
      "                a js:NumberSchema ;\n" +
      "                js:propertyName \"number_value\";\n" +
      "                js:maximum 100.05 ;\n" + 
      "                js:minimum -100.05 ;\n" +
      "            ] ;\n" +
      "            js:required \"number_value\" ;\n" +
      "        ] ;\n" + 
      "        td:hasOutputSchema [\n" + 
      "            a js:ObjectSchema ;\n" + 
      "            js:properties [\n" + 
      "                a js:BooleanSchema ;\n" +
      "                js:propertyName \"boolean_value\";\n" +
      "            ] ;\n" +
      "            js:required \"boolean_value\" ;\n" +
      "        ]\n" + 
      "    ] ." ;
  
  private static final String TEST_SIMPLE_TD_JSONLD = "[ {\n" + 
      "  \"@id\" : \"_:node1ea75dfphx111\",\n" + 
      "  \"@type\" : [ \"https://www.w3.org/2019/wot/security#NoSecurityScheme\" ]\n" + 
      "}, {\n" + 
      "  \"@id\" : \"_:node1ea75dfphx112\",\n" + 
      "  \"@type\" : [ \"https://www.w3.org/2019/wot/td#ActionAffordance\" ],\n" + 
      "  \"http://purl.org/dc/terms/title\" : [ {\n" + 
      "    \"@value\" : \"My Action\"\n" + 
      "  } ],\n" + 
      "  \"https://www.w3.org/2019/wot/td#hasForm\" : [ {\n" + 
      "    \"@id\" : \"_:node1ea75dfphx113\"\n" + 
      "  } ],\n" + 
      "  \"https://www.w3.org/2019/wot/td#hasInputSchema\" : [ {\n" + 
      "    \"@id\" : \"_:node1ea75dfphx114\"\n" + 
      "  } ],\n" + 
      "  \"https://www.w3.org/2019/wot/td#hasOutputSchema\" : [ {\n" + 
      "    \"@id\" : \"_:node1ea75dfphx116\"\n" + 
      "  } ]\n" + 
      "}, {\n" + 
      "  \"@id\" : \"_:node1ea75dfphx113\",\n" + 
      "  \"http://www.w3.org/2011/http#methodName\" : [ {\n" + 
      "    \"@value\" : \"PUT\"\n" + 
      "  } ],\n" + 
      "  \"https://www.w3.org/2019/wot/hypermedia#forContentType\" : [ {\n" + 
      "    \"@value\" : \"application/json\"\n" + 
      "  } ],\n" + 
      "  \"https://www.w3.org/2019/wot/hypermedia#hasOperationType\" : [ {\n" + 
      "    \"@id\" : \"https://www.w3.org/2019/wot/td#invokeAction\"\n" + 
      "  } ],\n" + 
      "  \"https://www.w3.org/2019/wot/hypermedia#hasTarget\" : [ {\n" + 
      "    \"@id\" : \"http://example.org/action\"\n" + 
      "  } ]\n" + 
      "}, {\n" + 
      "  \"@id\" : \"_:node1ea75dfphx114\",\n" + 
      "  \"@type\" : [ \"https://www.w3.org/2019/wot/json-schema#ObjectSchema\" ],\n" + 
      "  \"https://www.w3.org/2019/wot/json-schema#properties\" : [ {\n" + 
      "    \"@id\" : \"_:node1ea75dfphx115\"\n" + 
      "  } ],\n" + 
      "  \"https://www.w3.org/2019/wot/json-schema#required\" : [ {\n" + 
      "    \"@value\" : \"number_value\"\n" + 
      "  } ]\n" + 
      "}, {\n" + 
      "  \"@id\" : \"_:node1ea75dfphx115\",\n" + 
      "  \"@type\" : [ \"https://www.w3.org/2019/wot/json-schema#NumberSchema\" ],\n" + 
      "  \"https://www.w3.org/2019/wot/json-schema#maximum\" : [ {\n" + 
      "    \"@type\" : \"http://www.w3.org/2001/XMLSchema#decimal\",\n" + 
      "    \"@value\" : \"100.05\"\n" + 
      "  } ],\n" + 
      "  \"https://www.w3.org/2019/wot/json-schema#minimum\" : [ {\n" + 
      "    \"@type\" : \"http://www.w3.org/2001/XMLSchema#decimal\",\n" + 
      "    \"@value\" : \"-100.05\"\n" + 
      "  } ],\n" + 
      "  \"https://www.w3.org/2019/wot/json-schema#propertyName\" : [ {\n" + 
      "    \"@value\" : \"number_value\"\n" + 
      "  } ]\n" + 
      "}, {\n" + 
      "  \"@id\" : \"_:node1ea75dfphx116\",\n" + 
      "  \"@type\" : [ \"https://www.w3.org/2019/wot/json-schema#ObjectSchema\" ],\n" + 
      "  \"https://www.w3.org/2019/wot/json-schema#properties\" : [ {\n" + 
      "    \"@id\" : \"_:node1ea75dfphx117\"\n" + 
      "  } ],\n" + 
      "  \"https://www.w3.org/2019/wot/json-schema#required\" : [ {\n" + 
      "    \"@value\" : \"boolean_value\"\n" + 
      "  } ]\n" + 
      "}, {\n" + 
      "  \"@id\" : \"_:node1ea75dfphx117\",\n" + 
      "  \"@type\" : [ \"https://www.w3.org/2019/wot/json-schema#BooleanSchema\" ],\n" + 
      "  \"https://www.w3.org/2019/wot/json-schema#propertyName\" : [ {\n" + 
      "    \"@value\" : \"boolean_value\"\n" + 
      "  } ]\n" + 
      "}, {\n" + 
      "  \"@id\" : \"http://example.org/#thing\",\n" + 
      "  \"@type\" : [ \"https://www.w3.org/2019/wot/td#Thing\" ],\n" + 
      "  \"http://purl.org/dc/terms/title\" : [ {\n" + 
      "    \"@value\" : \"My Thing\"\n" + 
      "  } ],\n" + 
      "  \"https://www.w3.org/2019/wot/td#hasActionAffordance\" : [ {\n" + 
      "    \"@id\" : \"_:node1ea75dfphx112\"\n" + 
      "  } ],\n" + 
      "  \"https://www.w3.org/2019/wot/td#hasBase\" : [ {\n" + 
      "    \"@id\" : \"http://example.org/\"\n" + 
      "  } ],\n" + 
      "  \"https://www.w3.org/2019/wot/td#hasSecurityConfiguration\" : [ {\n" + 
      "    \"@id\" : \"_:node1ea75dfphx111\"\n" + 
      "  } ]\n" + 
      "} ]";
  
  private static final String TEST_IO_HEAD =
      "@prefix td: <https://www.w3.org/2019/wot/td#> .\n" +
      "@prefix htv: <http://www.w3.org/2011/http#> .\n" +
      "@prefix hctl: <https://www.w3.org/2019/wot/hypermedia#> .\n" +
      "@prefix dct: <http://purl.org/dc/terms/> .\n" +
      "@prefix wotsec: <https://www.w3.org/2019/wot/security#> .\n" +
      "@prefix js: <https://www.w3.org/2019/wot/json-schema#> .\n" +
      "\n" +
      "<http://example.org/#thing> a td:Thing ;\n" + 
      "    dct:title \"My Thing\" ;\n" +
      "    td:hasSecurityConfiguration [ a wotsec:NoSecurityScheme ] ;\n" +
      "    td:hasBase <http://example.org/> ;\n" + 
      "    td:hasActionAffordance [\n" + 
      "        a td:ActionAffordance ;\n" + 
      "        dct:title \"My Action\" ;\n" + 
      "        td:hasForm [\n" + 
      "            htv:methodName \"PUT\" ;\n" + 
      "            hctl:hasTarget <http://example.org/action> ;\n" + 
      "            hctl:forContentType \"application/json\";\n" + 
      "            hctl:hasOperationType td:invokeAction;\n" + 
      "        ] ;\n";
      
  private static final String TEST_IO_TAIL = "    ] ." ;
  
  private static final ValueFactory rdf = SimpleValueFactory.getInstance();
  
  @Test
  public void testReadTitle() {
    TDGraphReader reader = new TDGraphReader(RDFFormat.JSONLD, TEST_SIMPLE_TD_JSONLD);
    
    assertEquals("My Thing", reader.readThingTitle());
  }
  
  @Test
  public void testReadThingTypes() {
    TDGraphReader reader = new TDGraphReader(RDFFormat.TURTLE, TEST_SIMPLE_TD);
    
    assertEquals(1, reader.readThingTypes().size());
    assertTrue(reader.readThingTypes().contains(TD.Thing));
  }
  
  @Test
  public void testReadBaseURI() {
    TDGraphReader reader = new TDGraphReader(RDFFormat.TURTLE, TEST_SIMPLE_TD);
    
    assertEquals("http://example.org/", reader.readBaseURI().get());
  }
  
  @Test
  public void testReadOneSecuritySchema() {
    TDGraphReader reader = new TDGraphReader(RDFFormat.TURTLE, TEST_SIMPLE_TD);
    
    assertEquals(1, reader.readSecuritySchemas().size());
    assertTrue(reader.readSecuritySchemas().contains(rdf.createIRI(WoTSec.NoSecurityScheme)));
  }
  
  @Test
  public void testReadMultipleSecuritySchemas() {
    String prefix = "https://example.org#";
    String testTD =
        "@prefix td: <https://www.w3.org/2019/wot/td#> .\n" +
        "@prefix htv: <http://www.w3.org/2011/http#> .\n" +
        "@prefix wotsec: <https://www.w3.org/2019/wot/security#> .\n" +
        "@prefix dct: <http://purl.org/dc/terms/> .\n" +
        "@prefix js: <https://www.w3.org/2019/wot/json-schema#> .\n" +
        "@prefix ex: <" + prefix + "> .\n" +
        "\n" +
        "<http://example.org/#thing> a td:Thing ;\n" + 
        "    dct:title \"My Thing\" ;\n" +
        "    td:hasSecurityConfiguration [ a wotsec:NoSecurityScheme ] ;\n" +
        "    td:hasSecurityConfiguration [ a ex:MySecurityScheme ] ;\n" +
        "    td:hasSecurityConfiguration [ a ex:YourSecurityScheme ] ;\n" +
        "    td:hasBase <http://example.org/> .";
    
    TDGraphReader reader = new TDGraphReader(RDFFormat.TURTLE, testTD);
    
    ValueFactory rdf = SimpleValueFactory.getInstance();
    
    assertTrue(reader.readSecuritySchemas().contains(rdf.createIRI(WoTSec.NoSecurityScheme)));
    assertTrue(reader.readSecuritySchemas().contains(rdf.createIRI(prefix + "MySecurityScheme")));
    assertTrue(reader.readSecuritySchemas().contains(rdf.createIRI(prefix + "YourSecurityScheme")));
  }
  
  @Test
  public void testReadOneSimpleProperty() {
    TDGraphReader reader = new TDGraphReader(RDFFormat.TURTLE, TEST_SIMPLE_TD);
    
    List<PropertyAffordance> properties = reader.readProperties();
    assertEquals(1, properties.size());
    
    PropertyAffordance property = properties.get(0);
    assertEquals("My Property", property.getTitle().get());
    assertTrue(property.isObservable());
    assertEquals(2, property.getSemanticTypes().size());
    assertEquals(2, property.getForms().size());
  }
  
  @Test
  public void testReadOneSimpleAction() {
    TDGraphReader reader = new TDGraphReader(RDFFormat.TURTLE, TEST_SIMPLE_TD);
    
    assertEquals(1, reader.readActions().size());
    ActionAffordance action = reader.readActions().get(0);
    
    assertEquals("My Action", action.getTitle().get());
    assertEquals(1, action.getSemanticTypes().size());
    assertEquals(TD.ActionAffordance, action.getSemanticTypes().get(0));
    
    assertEquals(1, action.getForms().size());
    Form form = action.getForms().get(0);
    
    assertForm(form, "PUT", "http://example.org/action", "application/json", TD.invokeAction);
  }
  
  @Test
  public void testReadMultipleSimpleActions() {
    String testTD =
        "@prefix td: <https://www.w3.org/2019/wot/td#> .\n" +
        "@prefix htv: <http://www.w3.org/2011/http#> .\n" +
        "@prefix hctl: <https://www.w3.org/2019/wot/hypermedia#> .\n" +
        "@prefix dct: <http://purl.org/dc/terms/> .\n" +
        "@prefix wotsec: <https://www.w3.org/2019/wot/security#> .\n" +
        "@prefix js: <https://www.w3.org/2019/wot/json-schema#> .\n" +
        "\n" +
        "<http://example.org/#thing> a td:Thing ;\n" + 
        "    dct:title \"My Thing\" ;\n" +
        "    td:hasSecurityConfiguration [ a wotsec:NoSecurityScheme ] ;\n" +
        "    td:hasBase <http://example.org/> ;\n" + 
        "    td:hasActionAffordance [\n" + 
        "        a td:ActionAffordance ;\n" + 
        "        dct:title \"First Action\" ;\n" + 
        "        td:hasForm [\n" + 
        "            htv:methodName \"PUT\" ;\n" + 
        "            hctl:hasTarget <http://example.org/action1> ;\n" + 
        "            hctl:forContentType \"application/json\";\n" + 
        "            hctl:hasOperationType td:invokeAction;\n" + 
        "        ] ;\n" + 
        "    ] ;\n" +
        "    td:hasActionAffordance [\n" + 
        "        a td:ActionAffordance ;\n" + 
        "        dct:title \"Second Action\" ;\n" + 
        "        td:hasForm [\n" + 
        "            htv:methodName \"PUT\" ;\n" + 
        "            hctl:hasTarget <http://example.org/action2> ;\n" + 
        "            hctl:forContentType \"application/json\";\n" + 
        "            hctl:hasOperationType td:invokeAction;\n" + 
        "        ] ;\n" +
        "    ] ;\n" +
        "    td:hasActionAffordance [\n" + 
        "        a td:ActionAffordance ;\n" + 
        "        dct:title \"Third Action\" ;\n" + 
        "        td:hasForm [\n" + 
        "            htv:methodName \"PUT\" ;\n" + 
        "            hctl:hasTarget <http://example.org/action3> ;\n" + 
        "            hctl:forContentType \"application/json\";\n" + 
        "            hctl:hasOperationType td:invokeAction;\n" + 
        "        ] ;\n" + 
        "    ] ." ;
    
    TDGraphReader reader = new TDGraphReader(RDFFormat.TURTLE, testTD);
    
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
        "        td:hasInputSchema [\n" + 
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
    
    TDGraphReader reader = new TDGraphReader(RDFFormat.TURTLE, TEST_IO_HEAD + testSimpleObject 
        + TEST_IO_TAIL);
    
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
    assertEquals(1, td.getSemanticTypes().size());
    assertTrue(td.getSemanticTypes().contains("https://www.w3.org/2019/wot/td#Thing"));
    assertTrue(td.getSecurity().contains(rdf.createIRI(WoTSec.NoSecurityScheme)));
    assertEquals(1, td.getActions().size());
    
    // Check action metadata
    ActionAffordance action = td.getActions().get(0);
    assertEquals("My Action", action.getTitle().get());
    assertEquals(1, action.getForms().size());
    
    // Check action form
    Form form = action.getForms().get(0);
    assertForm(form, "PUT", "http://example.org/action", "application/json", TD.invokeAction);
    
    // Check action input data schema
    ObjectSchema input = (ObjectSchema) action.getInputSchema().get();
    assertEquals(DataSchema.OBJECT, input.getDatatype());
    assertEquals(1, input.getProperties().size());
    assertEquals(1, input.getRequiredProperties().size());
    
    assertEquals(DataSchema.NUMBER, input.getProperties().get("number_value").getDatatype());
    assertTrue(input.getRequiredProperties().contains("number_value"));
    
    // Check action output data schema
    ObjectSchema output = (ObjectSchema) action.getOutputSchema().get();
    assertEquals(DataSchema.OBJECT, output.getDatatype());
    assertEquals(1, output.getProperties().size());
    assertEquals(1, output.getRequiredProperties().size());
    
    assertEquals(DataSchema.BOOLEAN, output.getProperties().get("boolean_value").getDatatype());
    assertTrue(output.getRequiredProperties().contains("boolean_value"));
  }
  
  private void assertForm(Form form, String methodName, String target, 
      String contentType, String operationType) {
    assertEquals(methodName, form.getMethodName().get());
    assertEquals(target, form.getTarget());
    assertEquals(contentType, form.getContentType());
    assertTrue(form.hasOperationType(operationType));
  }
  
}
