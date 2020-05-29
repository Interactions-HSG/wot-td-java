package ch.unisg.ics.interactions.wot.td.writer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import org.apache.commons.rdf.api.BlankNode;
import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.rdf4j.RDF4J;
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
import ch.unisg.ics.interactions.wot.td.affordances.Action;
import ch.unisg.ics.interactions.wot.td.affordances.HTTPForm;
import ch.unisg.ics.interactions.wot.td.schema.JSONSchema;
import ch.unisg.ics.interactions.wot.td.schema.Schema;
import ch.unisg.ics.interactions.wot.td.vocabularies.TDVocab;

public class TDGraphWriterTest {
  private final static String THING_TITLE = "My Thing";
  private final static String THING_IRI = "http://example.org/#thing";
  private final static String IO_BASE_IRI = "http://example.org/";
  
  @Test
  public void testNoThingURI() throws RDFParseException, RDFHandlerException, IOException {
    String testTD = "[ a <http://www.w3.org/ns/td#Thing> ; " +
        "    <http://www.w3.org/ns/td#name> \"My Thing\" ] .\n"; 
    
    Model testModel = readModelFromString(RDFFormat.TURTLE, testTD);
    
    ThingDescription td = (new ThingDescription.Builder(THING_TITLE))
        .build();
    
    String description = TDGraphWriter.write(RDFFormat.TURTLE, td);
    Model tdModel = readModelFromString(RDFFormat.TURTLE, description);
    
    assertEquals(testModel, tdModel);
    
    assertTrue(Models.isomorphic(testModel, tdModel));
  }
  
  @Test
  public void testWriteTitle() throws RDFParseException, RDFHandlerException, IOException {
    String testTD = "<http://example.org/#thing> a <http://www.w3.org/ns/td#Thing> ; " +
        "    <http://www.w3.org/ns/td#name> \"My Thing\" .\n"; 
    
    Model testModel = readModelFromString(RDFFormat.TURTLE, testTD);
    
    ThingDescription td = (new ThingDescription.Builder(THING_TITLE))
        .addURI(THING_IRI)
        .build();
    
    String description = TDGraphWriter.write(RDFFormat.TURTLE, td);
    Model tdModel = readModelFromString(RDFFormat.TURTLE, description);
    
    assertTrue(Models.isomorphic(testModel, tdModel));
  }
  
  @Test
  public void testWriteAdditionalTypes() throws RDFParseException, RDFHandlerException, IOException {
    String testTD = "<http://example.org/#thing> a <http://www.w3.org/ns/td#Thing>, " +
        "<http://w3id.org/eve#Artifact>, <http://iot-schema.org/eve#Light> ;\n" + 
        "    <http://www.w3.org/ns/td#name> \"My Thing\" .\n"; 
    
    Model testModel = readModelFromString(RDFFormat.TURTLE, testTD);
    
    ThingDescription td = (new ThingDescription.Builder(THING_TITLE))
        .addURI(THING_IRI)
        .addType("http://w3id.org/eve#Artifact")
        .addType("http://iot-schema.org/eve#Light")
        .build();
    
    String description = TDGraphWriter.write(RDFFormat.TURTLE, td);
    Model tdModel = readModelFromString(RDFFormat.TURTLE, description);
    
    assertTrue(Models.isomorphic(testModel, tdModel));
  }
  
  @Test
  public void testWriteTypesDeduplication() throws RDFParseException, RDFHandlerException, 
      IOException {
    
    String testTD = "<http://example.org/#thing> a <http://www.w3.org/ns/td#Thing>, " +
        "<http://w3id.org/eve#Artifact> ;\n" + 
        "    <http://www.w3.org/ns/td#name> \"My Thing\" .\n"; 
    
    Model testModel = readModelFromString(RDFFormat.TURTLE, testTD);
    
    ThingDescription td = (new ThingDescription.Builder(THING_TITLE))
        .addURI(THING_IRI)
        .addType("http://w3id.org/eve#Artifact")
        .addType("http://w3id.org/eve#Artifact")
        .build();
    
    String description = TDGraphWriter.write(RDFFormat.TURTLE, td);
    Model tdModel = readModelFromString(RDFFormat.TURTLE, description);
    
    assertTrue(Models.isomorphic(testModel, tdModel));
  }
  
  @Test
  public void testWriteBaseURI() throws RDFParseException, RDFHandlerException, IOException {
    String testTD = "<http://example.org/#thing> a <http://www.w3.org/ns/td#Thing> ;\n" + 
        "    <http://www.w3.org/ns/td#name> \"My Thing\" ;\n" + 
        "    <http://www.w3.org/ns/td#base> \"http://example.org/\" ." ;
    
    Model testModel = readModelFromString(RDFFormat.TURTLE, testTD);
    
    ThingDescription td = (new ThingDescription.Builder(THING_TITLE))
        .addURI(THING_IRI)
        .addBaseURI("http://example.org/")
        .build();
    
    String description = TDGraphWriter.write(RDFFormat.TURTLE, td);
    Model tdModel = readModelFromString(RDFFormat.TURTLE, description);
    
    assertTrue(Models.isomorphic(testModel, tdModel));
  }
  
  @Test
  public void testWriteOneSimpleAction() throws RDFParseException, RDFHandlerException, IOException {
    String testTD = "<http://example.org/#thing> a <http://www.w3.org/ns/td#Thing> ;\n" + 
        "    <http://www.w3.org/ns/td#name> \"My Thing\" ;\n" + 
        "    <http://www.w3.org/ns/td#base> \"http://example.org/\" ;\n" + 
        "    <http://www.w3.org/ns/td#interaction> [\n" + 
        "        a <http://www.w3.org/ns/td#Action>, <http://iot-schema.org/#MyAction> ;\n" + 
        "        <http://www.w3.org/ns/td#name> \"My Action\" ;\n" + 
        "        <http://www.w3.org/ns/td#form> [\n" + 
        "            <http://www.w3.org/ns/td#methodName> \"PUT\" ;\n" + 
        "            <http://www.w3.org/ns/td#href> \"/action\";\n" + 
        "            <http://www.w3.org/ns/td#mediaType> \"application/json\";\n" + 
        "            <http://www.w3.org/ns/td#rel> \"invokeAction\";\n" + 
        "        ] ;\n" + 
        "    ] ." ;
    
    Model testModel = readModelFromString(RDFFormat.TURTLE, testTD);
    
    Action simpleAction = new Action.Builder(new HTTPForm("PUT", "/action", "application/json",
        new ArrayList<String>()))
        .addTitle("My Action")
        .addType("http://iot-schema.org/#MyAction")
        .build();
    
    ThingDescription td = (new ThingDescription.Builder(THING_TITLE))
        .addURI(THING_IRI)
        .addBaseURI("http://example.org/")
        .addAction(simpleAction)
        .build();
    
    String description = TDGraphWriter.write(RDFFormat.TURTLE, td);
    Model tdModel = readModelFromString(RDFFormat.TURTLE, description);
    
    assertTrue(Models.isomorphic(testModel, tdModel));
  }
  
  @Test
  public void testWriteOneActionWithInput() throws RDFParseException, RDFHandlerException, 
      IOException {
    
    String testTD = "<http://example.org/#thing> a <http://www.w3.org/ns/td#Thing> ;\n" + 
        "    <http://www.w3.org/ns/td#name> \"My Thing\" ;\n" + 
        "    <http://www.w3.org/ns/td#base> \"http://example.org/\" ;\n" + 
        "    <http://www.w3.org/ns/td#interaction> [\n" + 
        "        a <http://www.w3.org/ns/td#Action> ;\n" + 
        "        <http://www.w3.org/ns/td#name> \"My Action\" ;\n" + 
        "        <http://www.w3.org/ns/td#form> [\n" + 
        "            <http://www.w3.org/ns/td#methodName> \"PUT\" ;\n" + 
        "            <http://www.w3.org/ns/td#href> \"/action\";\n" + 
        "            <http://www.w3.org/ns/td#mediaType> \"application/json\";\n" + 
        "            <http://www.w3.org/ns/td#rel> \"invokeAction\";\n" + 
        "        ] ;\n" + 
        "        <http://www.w3.org/ns/td#inputSchema> [\n" + 
        "            <http://www.w3.org/ns/td#schemaType> <http://www.w3.org/ns/td#Object> ;\n" + 
        "            <http://www.w3.org/ns/td#field> [\n" + 
        "                <http://www.w3.org/ns/td#name> \"value\";\n" + 
        "                <http://www.w3.org/ns/td#schema> [\n" + 
        "                    <http://www.w3.org/ns/td#schemaType> <http://www.w3.org/ns/td#Number>\n" + 
        "                ]\n" + 
        "            ]\n" + 
        "        ]\n" + 
        "    ] ." ;
    
    Model testModel = readModelFromString(RDFFormat.TURTLE, testTD);
    
    RDF4J rdf = new RDF4J();
    
    Graph inputGraph = rdf.createGraph();
    
    BlankNode inputNode = rdf.createBlankNode();
    BlankNode keyValueNode = rdf.createBlankNode();
    BlankNode valueNode = rdf.createBlankNode();
    
    inputGraph.add(rdf.createTriple(inputNode, TDVocab.schemaType, TDVocab.Object));
    inputGraph.add(rdf.createTriple(inputNode, TDVocab.field, keyValueNode));
    inputGraph.add(rdf.createTriple(keyValueNode, TDVocab.name, rdf.createLiteral("value")));
    inputGraph.add(rdf.createTriple(keyValueNode, TDVocab.schema, valueNode));
    inputGraph.add(rdf.createTriple(valueNode, TDVocab.schemaType, TDVocab.Number));
    
    Schema schema = new JSONSchema(inputNode, inputGraph);
    
    Action actionWithInput = new Action.Builder(new HTTPForm("PUT", "/action", "application/json",
        new ArrayList<String>()))
        .addTitle("My Action")
        .addInputSchema(schema)
        .build();
    
    ThingDescription td = (new ThingDescription.Builder(THING_TITLE))
        .addURI(THING_IRI)
        .addBaseURI("http://example.org/")
        .addAction(actionWithInput)
        .build();
    
    String description = TDGraphWriter.write(RDFFormat.TURTLE, td);
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
