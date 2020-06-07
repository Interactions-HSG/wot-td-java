package ch.unisg.ics.interactions.wot.td.io;

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
import ch.unisg.ics.interactions.wot.td.io.TDGraphWriter;
import ch.unisg.ics.interactions.wot.td.schemas.BooleanSchema;
import ch.unisg.ics.interactions.wot.td.schemas.NumberSchema;
import ch.unisg.ics.interactions.wot.td.schemas.ObjectSchema;
import ch.unisg.ics.interactions.wot.td.vocabularies.WoTSec;

public class TDGraphWriterTest {
  private final static String THING_TITLE = "My Thing";
  private final static String THING_IRI = "http://example.org/#thing";
  private final static String IO_BASE_IRI = "http://example.org/";
  
  @Test
  public void testNoThingURI() throws RDFParseException, RDFHandlerException, IOException {
    String testTD = 
        "@prefix td: <https://www.w3.org/2019/wot/td#> .\n" +
        "@prefix dct: <http://purl.org/dc/terms/> .\n" +
        "@prefix wotsec: <https://www.w3.org/2019/wot/security#> .\n" +
        "\n" +
        "[] a td:Thing ;\n" + 
        "    dct:title \"My Thing\" ;\n" +
        "    td:hasSecurityConfiguration [ a wotsec:NoSecurityScheme ] .\n";
    
    Model testModel = ReadWriteTestUtils.readModelFromString(RDFFormat.TURTLE, testTD, IO_BASE_IRI);
    
    ThingDescription td = (new ThingDescription.Builder(THING_TITLE))
        .addSecurity(WoTSec.NoSecurityScheme)
        .build();
    
    String description = TDGraphWriter.write(td);
    Model tdModel = ReadWriteTestUtils.readModelFromString(RDFFormat.TURTLE, description, 
        IO_BASE_IRI);
    
    assertTrue(Models.isomorphic(testModel, tdModel));
  }
  
  @Test
  public void testWriteTitle() throws RDFParseException, RDFHandlerException, IOException {
    String testTD = 
        "@prefix td: <https://www.w3.org/2019/wot/td#> .\n" +
        "@prefix wotsec: <https://www.w3.org/2019/wot/security#> .\n" +
        "@prefix dct: <http://purl.org/dc/terms/> .\n" +
        "\n" +
        "<http://example.org/#thing> a td:Thing ;\n" + 
        "    dct:title \"My Thing\" ;\n" +
        "    td:hasSecurityConfiguration [ a wotsec:NoSecurityScheme ] .\n" ;
    
    Model testModel = ReadWriteTestUtils.readModelFromString(RDFFormat.TURTLE, testTD, IO_BASE_IRI);
    
    ThingDescription td = (new ThingDescription.Builder(THING_TITLE))
        .addThingURI(THING_IRI)
        .addSecurity(WoTSec.NoSecurityScheme)
        .build();
    
    String description = TDGraphWriter.write(td);
    
    Model tdModel = ReadWriteTestUtils.readModelFromString(RDFFormat.TURTLE, description, 
        IO_BASE_IRI);
    
    assertTrue(Models.isomorphic(testModel, tdModel));
  }
  
  @Test
  public void testWriteAdditionalTypes() throws RDFParseException, RDFHandlerException, IOException {
    String testTD = 
        "@prefix td: <https://www.w3.org/2019/wot/td#> .\n" +
        "@prefix dct: <http://purl.org/dc/terms/> .\n" +
        "@prefix wotsec: <https://www.w3.org/2019/wot/security#> .\n" +
        "@prefix eve: <http://w3id.org/eve#> .\n" +
        "@prefix iot: <http://iotschema.org/> .\n" +
        "\n" +
        "<http://example.org/#thing> a td:Thing, eve:Artifact, iot:Light ;\n" + 
        "    dct:title \"My Thing\" ;\n" +
        "    td:hasSecurityConfiguration [ a wotsec:NoSecurityScheme ] .\n";
    
    Model testModel = ReadWriteTestUtils.readModelFromString(RDFFormat.TURTLE, testTD, IO_BASE_IRI);
    
    ThingDescription td = (new ThingDescription.Builder(THING_TITLE))
        .addThingURI(THING_IRI)
        .addSemanticType("http://w3id.org/eve#Artifact")
        .addSemanticType("http://iotschema.org/Light")
        .addSecurity(WoTSec.NoSecurityScheme)
        .build();
    
    String description = TDGraphWriter.write(td);
    Model tdModel = ReadWriteTestUtils.readModelFromString(RDFFormat.TURTLE, description, 
        IO_BASE_IRI);
    
    assertTrue(Models.isomorphic(testModel, tdModel));
  }
  
  @Test
  public void testWriteTypesDeduplication() throws RDFParseException, RDFHandlerException, 
      IOException {
    
    String testTD = 
        "@prefix td: <https://www.w3.org/2019/wot/td#> .\n" +
        "@prefix dct: <http://purl.org/dc/terms/> .\n" +
        "@prefix wotsec: <https://www.w3.org/2019/wot/security#> .\n" +
        "@prefix eve: <http://w3id.org/eve#> .\n" +
        "\n" +
        "<http://example.org/#thing> a td:Thing, eve:Artifact ;\n" + 
        "    dct:title \"My Thing\" ;\n" +
        "    td:hasSecurityConfiguration [ a wotsec:NoSecurityScheme ] .\n";
    
    Model testModel = ReadWriteTestUtils.readModelFromString(RDFFormat.TURTLE, testTD, IO_BASE_IRI);
    
    ThingDescription td = (new ThingDescription.Builder(THING_TITLE))
        .addThingURI(THING_IRI)
        .addSemanticType("http://w3id.org/eve#Artifact")
        .addSemanticType("http://w3id.org/eve#Artifact")
        .addSecurity(WoTSec.NoSecurityScheme)
        .build();
    
    String description = TDGraphWriter.write(td);
    Model tdModel = ReadWriteTestUtils.readModelFromString(RDFFormat.TURTLE, description, 
        IO_BASE_IRI);
    
    assertTrue(Models.isomorphic(testModel, tdModel));
  }
  
  @Test
  public void testWriteBaseURI() throws RDFParseException, RDFHandlerException, IOException {
    String testTD = 
        "@prefix td: <https://www.w3.org/2019/wot/td#> .\n" +
        "@prefix dct: <http://purl.org/dc/terms/> .\n" +
        "@prefix wotsec: <https://www.w3.org/2019/wot/security#> .\n" +
        "\n" +
        "<http://example.org/#thing> a td:Thing ;\n" +
        "    td:hasSecurityConfiguration [ a wotsec:NoSecurityScheme ];\n" +
        "    dct:title \"My Thing\" ;\n" +
        "    td:hasBase <http://example.org/> .\n";
    
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
        "@prefix td: <https://www.w3.org/2019/wot/td#> .\n" +
        "@prefix htv: <http://www.w3.org/2011/http#> .\n" +
        "@prefix hctl: <https://www.w3.org/2019/wot/hypermedia#> .\n" +
        "@prefix wotsec: <https://www.w3.org/2019/wot/security#> .\n" +
        "@prefix dct: <http://purl.org/dc/terms/> .\n" +
        "@prefix js: <https://www.w3.org/2019/wot/json-schema#> .\n" +
        "@prefix iot: <http://iotschema.org/> .\n" +
        "\n" +
        "<http://example.org/#thing> a td:Thing ;\n" + 
        "    dct:title \"My Thing\" ;\n" +
        "    td:hasSecurityConfiguration [ a wotsec:NoSecurityScheme ] ;\n" +
        "    td:hasBase <http://example.org/> ;\n" + 
        "    td:hasActionAffordance [\n" + 
        "        a td:ActionAffordance, iot:MyAction ;\n" + 
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
    
    Model testModel = ReadWriteTestUtils.readModelFromString(RDFFormat.TURTLE, testTD, IO_BASE_IRI);
    
    ActionAffordance simpleAction = new ActionAffordance.Builder(
            new Form.Builder( "http://example.org/action")
              .setMethodName("PUT")
              .build())
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
        .addSecurity(WoTSec.NoSecurityScheme)
        .addBaseURI("http://example.org/")
        .addAction(simpleAction)
        .build();
    
    String description = new TDGraphWriter(td)
        .setNamespace("td", "https://www.w3.org/2019/wot/td#")
        .setNamespace("htv", "http://www.w3.org/2011/http#")
        .setNamespace("hctl", "https://www.w3.org/2019/wot/hypermedia#")
        .setNamespace("wotsec", "https://www.w3.org/2019/wot/security#")
        .setNamespace("dct", "http://purl.org/dc/terms/")
        .setNamespace("js", "https://www.w3.org/2019/wot/json-schema#")
        .setNamespace("iot", "http://iotschema.org/")
        .write();
    
    Model tdModel = ReadWriteTestUtils.readModelFromString(RDFFormat.TURTLE, description, 
        IO_BASE_IRI);
    
    assertTrue(Models.isomorphic(testModel, tdModel));
  }
  
  @Test
  public void testWriteReadmeExample() throws RDFParseException, RDFHandlerException, IOException {
    String testTD =
        "@prefix td: <https://www.w3.org/2019/wot/td#> .\n" +
        "@prefix htv: <http://www.w3.org/2011/http#> .\n" +
        "@prefix hctl: <https://www.w3.org/2019/wot/hypermedia#> .\n" +
        "@prefix dct: <http://purl.org/dc/terms/> .\n" +
        "@prefix wotsec: <https://www.w3.org/2019/wot/security#> .\n" +
        "@prefix js: <https://www.w3.org/2019/wot/json-schema#> .\n" + 
        "@prefix saref: <https://w3id.org/saref#> .\n" + 
        "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n" + 
        "\n" + 
        "<http://example.org/lamp123> a td:Thing, saref:LightSwitch;\n" + 
        "  td:hasSecurityConfiguration [ a wotsec:NoSecurityScheme ];\n" + 
        "  dct:title \"My Lamp Thing\" ;\n" + 
        "  td:hasActionAffordance [ a td:ActionAffordance, saref:ToggleCommand;\n" + 
        "      dct:title \"Toggle\";\n" +
        "      td:hasForm [\n" + 
        "          htv:methodName \"PUT\";\n" + 
        "          hctl:forContentType \"application/json\";\n" + 
        "          hctl:hasTarget <http://mylamp.example.org/toggle>;\n" + 
        "          hctl:hasOperationType td:invokeAction\n" + 
        "        ];\n" + 
        "      td:hasInputSchema [ a saref:OnOffState, js:ObjectSchema;\n" +
        "          js:properties [ a js:BooleanSchema;\n" + 
        "              js:propertyName \"status\"\n" + 
        "            ];\n" + 
        "          js:required \"status\"\n" + 
        "        ];\n" + 
        "    ].";
    
    Model testModel = ReadWriteTestUtils.readModelFromString(RDFFormat.TURTLE, 
        testTD, "http://example.org/");
    
    Form toggleForm = new Form.Builder("http://mylamp.example.org/toggle")
        .setMethodName("PUT")
        .build();
    
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
        .setNamespace("td", "https://www.w3.org/2019/wot/td#")
        .setNamespace("htv", "http://www.w3.org/2011/http#")
        .setNamespace("hctl", "https://www.w3.org/2019/wot/hypermedia#")
        .setNamespace("wotsec", "https://www.w3.org/2019/wot/security#")
        .setNamespace("dct", "http://purl.org/dc/terms/")
        .setNamespace("js", "https://www.w3.org/2019/wot/json-schema#")
        .setNamespace("saref", "https://w3id.org/saref#")
        .write();
    
    Model tdModel = ReadWriteTestUtils.readModelFromString(RDFFormat.TURTLE, description, 
        "http://example.org/");
    
    assertTrue(Models.isomorphic(testModel, tdModel));
  }
}
