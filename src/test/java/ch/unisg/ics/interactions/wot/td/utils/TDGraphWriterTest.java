package ch.unisg.ics.interactions.wot.td.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashSet;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.junit.Test;

import ch.unisg.ics.interactions.wot.td.ThingDescription;
import ch.unisg.ics.interactions.wot.td.affordances.ActionAffordance;
import ch.unisg.ics.interactions.wot.td.affordances.HTTPForm;

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
    Model tdModel = ReadWriteTestUtils.readModelFromString(RDFFormat.TURTLE, description, IO_BASE_IRI);
    
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
    Model tdModel = ReadWriteTestUtils.readModelFromString(RDFFormat.TURTLE, description, IO_BASE_IRI);
    
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
        .addType("http://w3id.org/eve#Artifact")
        .addType("http://iotschema.org/Light")
        .addSecurity(ThingDescription.DEFAULT_SECURITY_SCHEMA)
        .build();
    
    String description = TDGraphWriter.write(td);
    Model tdModel = ReadWriteTestUtils.readModelFromString(RDFFormat.TURTLE, description, IO_BASE_IRI);
    
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
        .addType("http://w3id.org/eve#Artifact")
        .addType("http://w3id.org/eve#Artifact")
        .addSecurity(ThingDescription.DEFAULT_SECURITY_SCHEMA)
        .build();
    
    String description = TDGraphWriter.write(td);
    Model tdModel = ReadWriteTestUtils.readModelFromString(RDFFormat.TURTLE, description, IO_BASE_IRI);
    
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
    Model tdModel = ReadWriteTestUtils.readModelFromString(RDFFormat.TURTLE, description, IO_BASE_IRI);
    
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
    
    Model testModel = ReadWriteTestUtils.readModelFromString(RDFFormat.TURTLE, testTD, IO_BASE_IRI);
    
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
    Model tdModel = ReadWriteTestUtils.readModelFromString(RDFFormat.TURTLE, description, IO_BASE_IRI);
    
    assertEquals(testModel, tdModel);
    assertTrue(Models.isomorphic(testModel, tdModel));
  }
}
