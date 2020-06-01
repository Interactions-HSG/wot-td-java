package ch.unisg.ics.interactions.wot.td.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.junit.Test;

import ch.unisg.ics.interactions.wot.td.ThingDescription;
import ch.unisg.ics.interactions.wot.td.affordances.ActionAffordance;
import ch.unisg.ics.interactions.wot.td.affordances.Form;
import ch.unisg.ics.interactions.wot.td.schemas.BooleanSchema;
import ch.unisg.ics.interactions.wot.td.schemas.NumberSchema;
import ch.unisg.ics.interactions.wot.td.schemas.ObjectSchema;

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
    
    Model testModel = ReadWriteTestUtils.readModelFromString(RDFFormat.TURTLE, testTD, IO_BASE_IRI);
    
    ThingDescription td = (new ThingDescription.Builder(THING_TITLE))
        .addSecurity(ThingDescription.DEFAULT_SECURITY_SCHEMA)
        .build();
    
    String description = TDGraphWriter.write(td);
    Model tdModel = ReadWriteTestUtils.readModelFromString(RDFFormat.TURTLE, description, 
        IO_BASE_IRI);
    
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
    
    Model testModel = ReadWriteTestUtils.readModelFromString(RDFFormat.TURTLE, testTD, IO_BASE_IRI);
    
    ThingDescription td = (new ThingDescription.Builder(THING_TITLE))
        .addThingURI(THING_IRI)
        .addSecurity(ThingDescription.DEFAULT_SECURITY_SCHEMA)
        .build();
    
    String description = TDGraphWriter.write(td);
    Model tdModel = ReadWriteTestUtils.readModelFromString(RDFFormat.TURTLE, description, 
        IO_BASE_IRI);
    
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
    
    Model testModel = ReadWriteTestUtils.readModelFromString(RDFFormat.TURTLE, testTD, IO_BASE_IRI);
    
    ThingDescription td = (new ThingDescription.Builder(THING_TITLE))
        .addThingURI(THING_IRI)
        .addSemanticType("http://w3id.org/eve#Artifact")
        .addSemanticType("http://iotschema.org/Light")
        .addSecurity(ThingDescription.DEFAULT_SECURITY_SCHEMA)
        .build();
    
    String description = TDGraphWriter.write(td);
    Model tdModel = ReadWriteTestUtils.readModelFromString(RDFFormat.TURTLE, description, 
        IO_BASE_IRI);
    
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
    
    Model testModel = ReadWriteTestUtils.readModelFromString(RDFFormat.TURTLE, testTD, IO_BASE_IRI);
    
    ThingDescription td = (new ThingDescription.Builder(THING_TITLE))
        .addThingURI(THING_IRI)
        .addSemanticType("http://w3id.org/eve#Artifact")
        .addSemanticType("http://w3id.org/eve#Artifact")
        .addSecurity(ThingDescription.DEFAULT_SECURITY_SCHEMA)
        .build();
    
    String description = TDGraphWriter.write(td);
    Model tdModel = ReadWriteTestUtils.readModelFromString(RDFFormat.TURTLE, description, 
        IO_BASE_IRI);
    
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
    
    Model testModel = ReadWriteTestUtils.readModelFromString(RDFFormat.TURTLE, testTD, IO_BASE_IRI);
    
    ThingDescription td = (new ThingDescription.Builder(THING_TITLE))
        .addThingURI(THING_IRI)
        .addBaseURI("http://example.org/")
        .build();
    
    String description = TDGraphWriter.write(td);
    Model tdModel = ReadWriteTestUtils.readModelFromString(RDFFormat.TURTLE, description, 
        IO_BASE_IRI);
    
    assertTrue(Models.isomorphic(testModel, tdModel));
  }
  
  @Test
  public void testWriteOneAction() throws RDFParseException, RDFHandlerException, IOException {
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
        "        td:input [\n" + 
        "            a js:ObjectSchema ;\n" + 
        "            js:properties [\n" + 
        "                a js:NumberSchema ;\n" +
        "                js:propertyName \"number_value\";\n" +
        "            ] ;\n" +
        "            js:required \"number_value\" ;\n" +
        "        ] ;\n" + 
        "        td:output [\n" + 
        "            a js:ObjectSchema ;\n" + 
        "            js:properties [\n" + 
        "                a js:BooleanSchema ;\n" +
        "                js:propertyName \"boolean_value\";\n" +
        "            ] ;\n" +
        "            js:required \"boolean_value\" ;\n" +
        "        ]\n" + 
        "    ] ." ;
    
    Model testModel = ReadWriteTestUtils.readModelFromString(RDFFormat.TURTLE, testTD, IO_BASE_IRI);
    
    ActionAffordance simpleAction = new ActionAffordance.Builder(new Form("PUT", 
        "http://example.org/action"))
        .addTitle("My Action")
        .addSemanticType("http://iotschema.org/MyAction")
        .addInputSchema(new ObjectSchema.Builder()
            .addProperty("number_value", new NumberSchema.Builder().build())
            .addRequiredProperties("number_value")
            .build())
        .addOutputSchema(new ObjectSchema.Builder()
            .addProperty("boolean_value", new BooleanSchema.Builder().build())
            .addRequiredProperties("boolean_value")
            .build())
        .build();
    
    ThingDescription td = (new ThingDescription.Builder(THING_TITLE))
        .addThingURI(THING_IRI)
        .addSecurity(ThingDescription.DEFAULT_SECURITY_SCHEMA)
        .addBaseURI("http://example.org/")
        .addAction(simpleAction)
        .build();
    
    String description = TDGraphWriter.write(td);
    Model tdModel = ReadWriteTestUtils.readModelFromString(RDFFormat.TURTLE, description, 
        IO_BASE_IRI);
    
    assertEquals(testModel, tdModel);
    assertTrue(Models.isomorphic(testModel, tdModel));
  }
  
  @Test
  public void testWriteReadmeExample() throws RDFParseException, RDFHandlerException, IOException {
    String testTD =
        "@prefix htv: <http://www.w3.org/2011/http#> .\n" + 
        "@prefix js: <https://www.w3.org/2019/wot/json-schema#> .\n" + 
        "@prefix saref: <https://w3id.org/saref#> .\n" + 
        "@prefix td: <http://www.w3.org/ns/td#> .\n" + 
        "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n" + 
        "\n" + 
        "<http://example.org/lamp123> a td:Thing, saref:LightSwitch;\n" + 
        "  td:security \"nosec_sc\";\n" + 
        "  td:title \"My Lamp Thing\" ;\n" + 
        "  td:interaction [ a td:ActionAffordance, saref:ToggleCommand;\n" + 
        "      td:form [\n" + 
        "          htv:methodName \"PUT\";\n" + 
        "          td:contentType \"application/json\";\n" + 
        "          td:href <http://mylamp.example.org/toggle>;\n" + 
        "          td:op \"invokeaction\"\n" + 
        "        ];\n" + 
        "      td:input [ a saref:OnOffState, js:ObjectSchema;\n" + 
        "          js:properties [ a js:BooleanSchema;\n" + 
        "              js:propertyName \"status\"\n" + 
        "            ];\n" + 
        "          js:required \"status\"\n" + 
        "        ];\n" + 
        "      td:title \"Toggle\"\n" + 
        "    ].";
    
    Model testModel = ReadWriteTestUtils.readModelFromString(RDFFormat.TURTLE, 
        testTD, "http://example.org/");
    
    Form toggleForm = new Form("PUT", "http://mylamp.example.org/toggle");
    
    ActionAffordance toggle = new ActionAffordance.Builder(toggleForm)
        .addTitle("Toggle")
        .addSemanticType("https://w3id.org/saref#ToggleCommand")
        .addInputSchema(new ObjectSchema.Builder()
            .addSemanticType("https://w3id.org/saref#OnOffState")
            .addProperty("status", new BooleanSchema.Builder()
                .build())
            .addRequiredProperties("status")
            .build())
        .build();
    
    ThingDescription td = (new ThingDescription.Builder("My Lamp Thing"))
        .addThingURI("http://example.org/lamp123")
        .addSemanticType("https://w3id.org/saref#LightSwitch")
        .addAction(toggle)
        .build();
    
    String description = new TDGraphWriter(td)
        .setNamespace("td", "http://www.w3.org/ns/td#")
        .setNamespace("htv", "http://www.w3.org/2011/http#")
        .setNamespace("js", "https://www.w3.org/2019/wot/json-schema#")
        .setNamespace("saref", "https://w3id.org/saref#")
        .write();
    
    Model tdModel = ReadWriteTestUtils.readModelFromString(RDFFormat.TURTLE, description, 
        "http://example.org/");
    
    assertEquals(testModel, tdModel);
    
    assertTrue(Models.isomorphic(testModel, tdModel));
  }
}
