package ch.unisg.ics.interactions.wot.td.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Optional;

import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.junit.Test;

import ch.unisg.ics.interactions.wot.td.ThingDescription;
import ch.unisg.ics.interactions.wot.td.affordances.ActionAffordance;
import ch.unisg.ics.interactions.wot.td.affordances.HTTPForm;
import ch.unisg.ics.interactions.wot.td.schemas.BooleanSchema;
import ch.unisg.ics.interactions.wot.td.schemas.DataSchema;
import ch.unisg.ics.interactions.wot.td.schemas.IntegerSchema;
import ch.unisg.ics.interactions.wot.td.schemas.NullSchema;
import ch.unisg.ics.interactions.wot.td.schemas.NumberSchema;
import ch.unisg.ics.interactions.wot.td.schemas.ObjectSchema;
import ch.unisg.ics.interactions.wot.td.schemas.StringSchema;
import ch.unisg.ics.interactions.wot.td.vocabularies.JSONSchema;

public class TDGraphWriterTest {
  private final static String THING_TITLE = "My Thing";
  private final static String THING_IRI = "http://example.org/#thing";
  private final static String IO_BASE_IRI = "http://example.org/";
  
  @Test
  public void testNoThingURI() throws RDFParseException, RDFHandlerException, IOException {
    String testTD = 
        "@prefix td: <http://www.w3.org/ns/td#> .\n" +
        "\n" +
        "[] a td:Thing ;\n" + 
        "    td:title \"My Thing\" ;\n" +
        "    td:security \"nosec_sc\" .\n";
    
    Model testModel = readModelFromString(RDFFormat.TURTLE, testTD);
    
    ThingDescription td = (new ThingDescription.Builder(THING_TITLE))
        .addSecurity(ThingDescription.DEFAULT_SECURITY_SCHEMA)
        .build();
    
    String description = TDGraphWriter.write(td);
    Model tdModel = readModelFromString(RDFFormat.TURTLE, description);
    
    assertEquals(testModel, tdModel);
    
    assertTrue(Models.isomorphic(testModel, tdModel));
  }
  
  @Test
  public void testWriteTitle() throws RDFParseException, RDFHandlerException, IOException {
    String testTD = 
        "@prefix td: <http://www.w3.org/ns/td#> .\n" +
        "\n" +
        "<http://example.org/#thing> a td:Thing ;\n" + 
        "    td:title \"My Thing\" ;\n" +
        "    td:security \"nosec_sc\" .\n";
    
    Model testModel = readModelFromString(RDFFormat.TURTLE, testTD);
    
    ThingDescription td = (new ThingDescription.Builder(THING_TITLE))
        .addThingURI(THING_IRI)
        .addSecurity(ThingDescription.DEFAULT_SECURITY_SCHEMA)
        .build();
    
    String description = TDGraphWriter.write(td);
    Model tdModel = readModelFromString(RDFFormat.TURTLE, description);
    
    assertTrue(Models.isomorphic(testModel, tdModel));
  }
  
  @Test
  public void testWriteAdditionalTypes() throws RDFParseException, RDFHandlerException, IOException {
    String testTD = 
        "@prefix td: <http://www.w3.org/ns/td#> .\n" +
        "@prefix eve: <http://w3id.org/eve#> .\n" +
        "@prefix iot: <http://iotschema.org/> .\n" +
        "\n" +
        "<http://example.org/#thing> a td:Thing, eve:Artifact, iot:Light ;\n" + 
        "    td:title \"My Thing\" ;\n" +
        "    td:security \"nosec_sc\" .\n";
    
    Model testModel = readModelFromString(RDFFormat.TURTLE, testTD);
    
    ThingDescription td = (new ThingDescription.Builder(THING_TITLE))
        .addThingURI(THING_IRI)
        .addType("http://w3id.org/eve#Artifact")
        .addType("http://iotschema.org/Light")
        .addSecurity(ThingDescription.DEFAULT_SECURITY_SCHEMA)
        .build();
    
    String description = TDGraphWriter.write(td);
    Model tdModel = readModelFromString(RDFFormat.TURTLE, description);
    
    assertEquals(testModel, tdModel);
    assertTrue(Models.isomorphic(testModel, tdModel));
  }
  
  @Test
  public void testWriteTypesDeduplication() throws RDFParseException, RDFHandlerException, 
      IOException {
    
    String testTD = 
        "@prefix td: <http://www.w3.org/ns/td#> .\n" +
        "@prefix eve: <http://w3id.org/eve#> .\n" +
        "\n" +
        "<http://example.org/#thing> a td:Thing, eve:Artifact ;\n" + 
        "    td:title \"My Thing\" ;\n" +
        "    td:security \"nosec_sc\" .\n";
    
    Model testModel = readModelFromString(RDFFormat.TURTLE, testTD);
    
    ThingDescription td = (new ThingDescription.Builder(THING_TITLE))
        .addThingURI(THING_IRI)
        .addType("http://w3id.org/eve#Artifact")
        .addType("http://w3id.org/eve#Artifact")
        .addSecurity(ThingDescription.DEFAULT_SECURITY_SCHEMA)
        .build();
    
    String description = TDGraphWriter.write(td);
    Model tdModel = readModelFromString(RDFFormat.TURTLE, description);
    
    assertTrue(Models.isomorphic(testModel, tdModel));
  }
  
  @Test
  public void testWriteBaseURI() throws RDFParseException, RDFHandlerException, IOException {
    String testTD = 
        "@prefix td: <http://www.w3.org/ns/td#> .\n" +
        "\n" +
        "<http://example.org/#thing> a td:Thing ;\n" + 
        "    td:title \"My Thing\" ;\n" +
        "    td:security \"nosec_sc\" ;\n" +
        "    td:base <http://example.org/> .\n";
    
    Model testModel = readModelFromString(RDFFormat.TURTLE, testTD);
    
    ThingDescription td = (new ThingDescription.Builder(THING_TITLE))
        .addThingURI(THING_IRI)
        .addBaseURI("http://example.org/")
        .build();
    
    String description = TDGraphWriter.write(td);
    Model tdModel = readModelFromString(RDFFormat.TURTLE, description);
    
    assertTrue(Models.isomorphic(testModel, tdModel));
  }
  
  @Test
  public void testWriteOneSimpleAction() throws RDFParseException, RDFHandlerException, IOException {
    String testTD = 
        "@prefix td: <http://www.w3.org/ns/td#> .\n" +
        "@prefix htv: <http://www.w3.org/2011/http#> .\n" +
        "@prefix js: <https://www.w3.org/2019/wot/json-schema#> .\n" +
        "@prefix iot: <http://iotschema.org/> .\n" +
        "\n" +
        "<http://example.org/#thing> a td:Thing ;\n" + 
        "    td:title \"My Thing\" ;\n" +
        "    td:security \"nosec_sc\" ;\n" +
        "    td:base <http://example.org/> ;\n" + 
        "    td:interaction [\n" + 
        "        a td:ActionAffordance, iot:MyAction ;\n" + 
        "        td:title \"My Action\" ;\n" + 
        "        td:form [\n" + 
        "            htv:methodName \"PUT\" ;\n" + 
        "            td:href <http://example.org/action> ;\n" + 
        "            td:contentType \"application/json\";\n" + 
        "            td:op \"invokeaction\";\n" + 
        "        ] ;\n" + 
        "    ] ." ;
    
    Model testModel = readModelFromString(RDFFormat.TURTLE, testTD);
    
    ActionAffordance simpleAction = new ActionAffordance.Builder(new HTTPForm("PUT", "http://example.org/action", 
        "application/json", new HashSet<String>()))
        .addTitle("My Action")
        .addType("http://iotschema.org/MyAction")
        .build();
    
    ThingDescription td = (new ThingDescription.Builder(THING_TITLE))
        .addThingURI(THING_IRI)
        .addSecurity(ThingDescription.DEFAULT_SECURITY_SCHEMA)
        .addBaseURI("http://example.org/")
        .addAction(simpleAction)
        .build();
    
    String description = TDGraphWriter.write(td);
    Model tdModel = readModelFromString(RDFFormat.TURTLE, description);
    
    assertEquals(testModel, tdModel);
    assertTrue(Models.isomorphic(testModel, tdModel));
  }
  
  @Test
  public void testWriteOneSemanticObjectInputNoDecimals() throws RDFParseException, 
      RDFHandlerException, IOException {
    // Serialization of decimal values requires specific testing
    String testTD = 
        "@prefix td: <http://www.w3.org/ns/td#> .\n" +
        "@prefix htv: <http://www.w3.org/2011/http#> .\n" +
        "@prefix js: <https://www.w3.org/2019/wot/json-schema#> .\n" +
        "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n" +
        "@prefix ex: <https://example.org/#> .\n" +
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
        "            a js:ObjectSchema, ex:SemObject ;\n" +  
        "            js:properties [\n" + 
        "                a js:BooleanSchema, ex:SemBoolean ;\n" + 
        "                js:propertyName \"boolean_value\";\n" +
        "            ] ;\n" +
        "            js:properties [\n" +
        "                a js:IntegerSchema, ex:SemInteger ;\n" + 
        "                js:propertyName \"integer_value\" ;\n" +
        "                js:minimum \"-1000\"^^xsd:int ;\n" +
        "                js:maximum \"1000\"^^xsd:int ;\n" +
        "            ] ;\n" +
        "            js:properties [\n" + 
        "                a js:NumberSchema, ex:SemNumber ;\n" + 
        "                js:propertyName \"number_value\";\n" +
        "            ] ;\n" +
        "            js:properties [\n" + 
        "                a js:StringSchema, ex:SemString ;\n" + 
        "                js:propertyName \"string_value\";\n" +
        "            ] ;\n" +
        "            js:properties [\n" + 
        "                a js:NullSchema, ex:SemNull ;\n" + 
        "                js:propertyName \"null_value\";\n" +
        "            ] ;\n" +
        "            js:required \"string_value\" ;\n" +
        "        ]\n" + 
        "    ] ." ;
    
    String prefix = "https://example.org/#";
    
    Model testModel = readModelFromString(RDFFormat.TURTLE, testTD);
    
    DataSchema schema = new ObjectSchema.Builder()
        .addSemanticType(prefix + "SemObject")
        .addProperty("boolean_value", (new BooleanSchema.Builder()
            .addSemanticType(prefix + "SemBoolean")
            .build()))
        .addProperty("integer_value", (new IntegerSchema.Builder()
            .addSemanticType(prefix + "SemInteger")
            .addMinimum(-1000)
            .addMaximum(1000)
            .build()))
        .addProperty("number_value", (new NumberSchema.Builder()
            .addSemanticType(prefix + "SemNumber")
            .build()))
        .addProperty("string_value", (new StringSchema.Builder()
            .addSemanticType(prefix + "SemString")
            .build()))
        .addProperty("null_value", (new NullSchema.Builder()
            .addSemanticType(prefix + "SemNull")
            .build()))
        .addRequiredProperties("string_value")
        .build();
    
    ActionAffordance actionWithInput = new ActionAffordance.Builder(new HTTPForm("PUT", 
        "http://example.org/action", "application/json", new HashSet<String>()))
        .addTitle("My Action")
        .addInputSchema(schema)
        .build();
    
    ThingDescription td = (new ThingDescription.Builder(THING_TITLE))
        .addThingURI(THING_IRI)
        .addSecurity(ThingDescription.DEFAULT_SECURITY_SCHEMA)
        .addBaseURI("http://example.org/")
        .addAction(actionWithInput)
        .build();
    
    String description = TDGraphWriter.write(td);
    Model tdModel = readModelFromString(RDFFormat.TURTLE, description);
    
    assertTrue(Models.isomorphic(testModel, tdModel));
  }
  
  // Serialization of decimal values requires specific testing
  @Test
  public void testWriteOneSemanticObjectInputWithDecimals() throws RDFParseException, 
      RDFHandlerException, IOException {
    
    String prefix = "https://example.org/#";
    
    DataSchema schema = new ObjectSchema.Builder()
        .addSemanticType(prefix + "SemObject")
        .addProperty("number_value", (new NumberSchema.Builder()
            .addSemanticType(prefix + "SemNumber")
            .addMinimum(-1000.005)
            .addMaximum(1000.005)
            .build()))
        .build();
    
    ActionAffordance actionWithInput = new ActionAffordance.Builder(new HTTPForm("PUT", 
        "http://example.org/action", "application/json", new HashSet<String>()))
        .addTitle("My Action")
        .addInputSchema(schema)
        .build();
    
    ThingDescription td = (new ThingDescription.Builder(THING_TITLE))
        .addThingURI(THING_IRI)
        .addSecurity(ThingDescription.DEFAULT_SECURITY_SCHEMA)
        .addBaseURI("http://example.org/")
        .addAction(actionWithInput)
        .build();
    
    String description = TDGraphWriter.write(td);
    Model tdModel = readModelFromString(RDFFormat.TURTLE, description);
    
    Optional<Literal> minimum = Models.objectLiteral(tdModel.filter(null, JSONSchema.minimum, null));
    assertTrue(minimum.isPresent());
    assertEquals(-1000.005, minimum.get().doubleValue(), 0.001);
    
    Optional<Literal> maximum = Models.objectLiteral(tdModel.filter(null, JSONSchema.minimum, null));
    assertTrue(maximum.isPresent());
    assertEquals(-1000.005, maximum.get().doubleValue(), 0.001);
  }
  
  @Test
  public void testWriteNestedSemanticObjectInput() throws RDFParseException, RDFHandlerException, 
      IOException {
    
    String testTD = 
        "@prefix td: <http://www.w3.org/ns/td#> .\n" +
        "@prefix htv: <http://www.w3.org/2011/http#> .\n" +
        "@prefix js: <https://www.w3.org/2019/wot/json-schema#> .\n" +
        "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n" +
        "@prefix ex: <https://example.org/#> .\n" +
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
        "            a js:ObjectSchema, ex:SemObject ;\n" +
        "            js:properties [\n" + 
        "                a js:StringSchema, ex:SemString ;\n" + 
        "                js:propertyName \"string_value\";\n" +
        "            ] ;\n" +
        "            js:properties [\n" + 
        "                a js:ObjectSchema, ex:AnotherSemObject ;\n" + 
        "                js:propertyName \"inner_object\";\n" +
        "                js:properties [\n" + 
        "                    a js:BooleanSchema, ex:SemBoolean ;\n" + 
        "                    js:propertyName \"boolean_value\";\n" +
        "                ] ;\n" +
        "                js:properties [\n" +
        "                    a js:IntegerSchema, ex:SemInteger ;\n" + 
        "                    js:propertyName \"integer_value\" ;\n" +
        "                ] ;\n" +
        "                js:properties [\n" + 
        "                    a js:NumberSchema, ex:SemNumber ;\n" + 
        "                    js:propertyName \"number_value\";\n" +
        "                ] ;\n" +
        "                js:properties [\n" + 
        "                    a js:NullSchema, ex:SemNull ;\n" + 
        "                    js:propertyName \"null_value\";\n" +
        "                ] ;\n" +
        "                js:required \"integer_value\" ;\n" +
        "            ] ;\n" +
        "            js:required \"string_value\" ;\n" +
        "        ]\n" + 
        "    ] ." ;
    
    String prefix = "https://example.org/#";
    
    Model testModel = readModelFromString(RDFFormat.TURTLE, testTD);
    
    DataSchema schema = new ObjectSchema.Builder()
        .addSemanticType(prefix + "SemObject")
        .addProperty("string_value", (new StringSchema.Builder()
          .addSemanticType(prefix + "SemString")
          .build()))
        .addProperty("inner_object", (new ObjectSchema.Builder()
          .addSemanticType(prefix + "AnotherSemObject")
          .addProperty("boolean_value", (new BooleanSchema.Builder()
              .addSemanticType(prefix + "SemBoolean")
              .build()))
          .addProperty("integer_value", (new IntegerSchema.Builder()
              .addSemanticType(prefix + "SemInteger")
              .build()))
          .addProperty("number_value", (new NumberSchema.Builder()
            .addSemanticType(prefix + "SemNumber")
            .build()))
          .addProperty("null_value", (new NullSchema.Builder()
            .addSemanticType(prefix + "SemNull")
            .build()))
          .addRequiredProperties("integer_value")
          .build()))
        .addRequiredProperties("string_value")
        .build();
    
    ActionAffordance actionWithInput = new ActionAffordance.Builder(new HTTPForm("PUT", 
        "http://example.org/action", "application/json", new HashSet<String>()))
        .addTitle("My Action")
        .addInputSchema(schema)
        .build();
    
    ThingDescription td = (new ThingDescription.Builder(THING_TITLE))
        .addThingURI(THING_IRI)
        .addSecurity(ThingDescription.DEFAULT_SECURITY_SCHEMA)
        .addBaseURI("http://example.org/")
        .addAction(actionWithInput)
        .build();
    
    String description = TDGraphWriter.write(td);
    Model tdModel = readModelFromString(RDFFormat.TURTLE, description);
    
    assertTrue(Models.isomorphic(testModel, tdModel));
  }
  
  private Model readModelFromString(RDFFormat format, String description) 
      throws RDFParseException, RDFHandlerException, IOException {
    StringReader stringReader = new StringReader(description);
    
    RDFParser rdfParser = Rio.createParser(RDFFormat.TURTLE);
    Model model = new LinkedHashModel();
    rdfParser.setRDFHandler(new StatementCollector(model));
    
    rdfParser.parse(stringReader, IO_BASE_IRI);
    
    return model;
  }
}
