package ch.unisg.ics.interactions.wot.td.io;

import ch.unisg.ics.interactions.wot.td.templates.IOThingDescriptionTemplate;
import ch.unisg.ics.interactions.wot.td.schemas.*;
import ch.unisg.ics.interactions.wot.td.templates.IOActionAffordanceTemplate;
import ch.unisg.ics.interactions.wot.td.templates.IOEventAffordanceTemplate;
import ch.unisg.ics.interactions.wot.td.templates.IOPropertyAffordanceTemplate;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class IOTDTGraphWriterTest {
  private static final String THING_TITLE = "My Thing";
  private static final String THING_IRI = "http://example.org/#thing";
  private static final String IO_BASE_IRI = "http://example.org/";

  private static final String PREFIXES =
    "@prefix td: <https://www.w3.org/2019/wot/td#> .\n" +
      "@prefix htv: <http://www.w3.org/2011/http#> .\n" +
      "@prefix cov: <http://www.example.org/coap-binding#> .\n" +
      "@prefix hctl: <https://www.w3.org/2019/wot/hypermedia#> .\n" +
      "@prefix dct: <http://purl.org/dc/terms/> .\n" +
      "@prefix wotsec: <https://www.w3.org/2019/wot/security#> .\n" +
      "@prefix js: <https://www.w3.org/2019/wot/json-schema#> .\n" +
      "@prefix saref: <https://saref.etsi.org/core/> .\n" +
      "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n";

  @Test
  public void testNoThingURI() throws RDFParseException, RDFHandlerException, IOException {
    String testTDT =
      PREFIXES +
        "\n" +
        "[] a td:Thing ;\n" +
        "    td:title \"My Thing\" .\n";

    IOThingDescriptionTemplate tdt = new IOThingDescriptionTemplate.Builder(THING_TITLE)
      .build();

    assertIsomorphicGraphs(testTDT, tdt);
  }

  @Test
  public void testWriteTitle() throws RDFParseException, RDFHandlerException, IOException {
    String testTDT = PREFIXES +
      "<http://example.org/#thing> a td:Thing ;\n" +
      "    td:title \"My Thing\" .\n";

    IOThingDescriptionTemplate tdt = new IOThingDescriptionTemplate.Builder(THING_TITLE)
      .build();
    String writeTDT = IOTDTGraphWriter.write(tdt);
    System.out.println(writeTDT);

    assertIsomorphicGraphs(testTDT, tdt);
  }



  @Test
  public void testWriteAdditionalTypes() throws RDFParseException, RDFHandlerException, IOException {
    String testTDT =
      PREFIXES +
        "@prefix eve: <http://w3id.org/eve#> .\n" +
        "@prefix iot: <http://iotschema.org/> .\n" +
        "\n" +
        "<http://example.org/#thing> a td:Thing, eve:Artifact, iot:Light ;\n" +
        "    td:title \"My Thing\" ;\n" +
        "    td:hasSecurityConfiguration [ a wotsec:NoSecurityScheme ] .\n";

    IOThingDescriptionTemplate tdt = new IOThingDescriptionTemplate.Builder(THING_TITLE)
      .addSemanticType("http://w3id.org/eve#Artifact")
      .addSemanticType("http://iotschema.org/Light")
      .build();

    assertIsomorphicGraphs(testTDT, tdt);
  }

  @Test
  public void testWriteTypesDeduplication() throws RDFParseException, RDFHandlerException,
    IOException {

    String testTDT =
      PREFIXES +
        "@prefix eve: <http://w3id.org/eve#> .\n" +
        "\n" +
        "<http://example.org/#thing> a td:Thing, eve:Artifact ;\n" +
        "    td:title \"My Thing\" ;\n" +
        "    td:hasSecurityConfiguration [ a wotsec:NoSecurityScheme ] .\n";

    IOThingDescriptionTemplate tdt = new IOThingDescriptionTemplate.Builder(THING_TITLE)
      .addSemanticType("http://w3id.org/eve#Artifact")
      .addSemanticType("http://w3id.org/eve#Artifact")
      .build();

    assertIsomorphicGraphs(testTDT, tdt);
  }



  @Test
  public void testWriteOnePropertyDefaultValues() throws RDFParseException, RDFHandlerException,
    IOException {
    String testTDT = PREFIXES +
      "@prefix iot: <http://iotschema.org/> .\n" +
      "<http://example.org/#thing> a td:Thing ;\n" +
      "    td:title \"My Thing\" ;\n" +
      "    td:hasSecurityConfiguration [ a wotsec:NoSecurityScheme ] ;\n" +
      "    td:hasPropertyAffordance [\n" +
      "        a td:PropertyAffordance, js:IntegerSchema, iot:MyProperty ;\n" +
      "        td:name \"my_property\" ;\n" +
      "        td:isObservable true ;\n" +
      "        td:hasForm [\n" +
      "            hctl:hasTarget <http://example.org/count> ;\n" +
      "            hctl:forContentType \"application/json\";\n" +
      "            hctl:hasOperationType td:readProperty, td:writeProperty;\n" +
      "        ] ;\n" +
      "    ] ." ;

    IOPropertyAffordanceTemplate property = new IOPropertyAffordanceTemplate.Builder("my_property")
      .addDataSchema(new IntegerSchema.Builder().build())
      .addSemanticType("http://iotschema.org/MyProperty")
      .addObserve()
      .build();

    IOThingDescriptionTemplate tdt = new IOThingDescriptionTemplate.Builder(THING_TITLE)
      .addProperty(property)
      .build();

    assertIsomorphicGraphs(testTDT, tdt);
  }

  @Test
  public void testWriteOnePropertyNoSchema() throws IOException {
    String testTDT = PREFIXES +
      "@prefix iot: <http://iotschema.org/> .\n" +
      "<http://example.org/#thing> a td:Thing ;\n" +
      "    td:title \"My Thing\" ;\n" +
      "    td:hasSecurityConfiguration [ a wotsec:NoSecurityScheme ] ;\n" +
      "    td:hasPropertyAffordance [\n" +
      "        a td:PropertyAffordance, iot:MyProperty ;\n" +
      "        td:name \"my_property\" ;\n" +
      "        td:isObservable false ;\n" +
      "        td:hasForm [\n" +
      "            hctl:hasTarget <http://example.org/count> ;\n" +
      "            hctl:forContentType \"video/mpeg\";\n" +
      "            hctl:hasOperationType td:readProperty;\n" +
      "        ] ;\n" +
      "    ] .";

    IOPropertyAffordanceTemplate property = new IOPropertyAffordanceTemplate.Builder("my_property")
      .addSemanticType("http://iotschema.org/MyProperty")
      .build();

    IOThingDescriptionTemplate tdt = new IOThingDescriptionTemplate.Builder(THING_TITLE)
      .addProperty(property)
      .build();

    assertIsomorphicGraphs(testTDT, tdt);
  }



  @Test
  public void testWriteOneAction() throws RDFParseException, RDFHandlerException, IOException {
    String testTDT = PREFIXES +
      "@prefix iot: <http://iotschema.org/> .\n" +
      "\n" +
      "<http://example.org/#thing> a td:Thing ;\n" +
      "    td:title \"My Thing\" ;\n" +
      "    td:hasSecurityConfiguration [ a wotsec:NoSecurityScheme ] ;\n" +
      "    td:hasBase <http://example.org/> ;\n" +
      "    td:hasActionAffordance [\n" +
      "        a td:ActionAffordance, iot:MyAction ;\n" +
      "        td:name \"my_action\" ;\n" +
      "        td:title \"My Action\" ;\n" +
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

    IOActionAffordanceTemplate simpleAction = new IOActionAffordanceTemplate.Builder("my_action")
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

    IOThingDescriptionTemplate tdt = constructThingDescriptionTemplate(new ArrayList<>(),
      new ArrayList<>(Collections.singletonList(simpleAction)));

    assertIsomorphicGraphs(testTDT, tdt);
  }


  @Test
  public void testWriteEvent() throws IOException {
    String testTDT = PREFIXES +
      "@prefix iot: <http://iotschema.org/> .\n" +
      "<http://example.org/#thing> a td:Thing ;\n" +
      "    td:title \"My Thing\" ;\n" +
      "    td:hasSecurityConfiguration [ a wotsec:NoSecurityScheme ] ;\n" +
      "    td:hasEventAffordance [\n" +
      "        a td:EventAffordance, iot:MyEvent ;\n" +
      "        td:name \"my_event\" ;\n" +
      "        td:hasForm [\n" +
      "            hctl:hasTarget <http://example.org/event> ;\n" +
      "            hctl:forContentType \"application/json\";\n" +
      "            hctl:hasOperationType td:subscribeEvent, td:unsubscribeEvent;\n" +
      "        ];\n" +
      "        td:hasSubscriptionSchema [\n" +
      "            a js:ObjectSchema ;\n" +
      "            js:properties [\n" +
      "                a js:NumberSchema ;\n" +
      "                js:propertyName \"number_value\";\n" +
      "            ] ;\n" +
      "            js:required \"number_value\" ;\n" +
      "        ] ;\n" +
      "        td:hasNotificationSchema [\n" +
      "            a js:ObjectSchema ;\n" +
      "            js:properties [\n" +
      "                a js:BooleanSchema ;\n" +
      "                js:propertyName \"boolean_value\";\n" +
      "            ] ;\n" +
      "            js:required \"boolean_value\" ;\n" +
      "        ] ;\n" +
      "        td:hasCancellationSchema [\n" +
      "            a js:ObjectSchema ;\n" +
      "            js:properties [\n" +
      "                a js:StringSchema ;\n" +
      "                js:propertyName \"string_value\";\n" +
      "            ] ;\n" +
      "            js:required \"string_value\" ;\n" +
      "        ] ;\n" +
      "    ] .";



    IOEventAffordanceTemplate event = new IOEventAffordanceTemplate.Builder("my_event")
      .addSemanticType("http://iotschema.org/MyEvent")
      .addSubscriptionSchema(new ObjectSchema.Builder()
        .addProperty("number_value", new NumberSchema.Builder().build())
        .addRequiredProperties("number_value")
        .build())
      .addNotificationSchema(new ObjectSchema.Builder()
        .addProperty("boolean_value", new BooleanSchema.Builder().build())
        .addRequiredProperties("boolean_value")
        .build())
      .addCancellationSchema(new ObjectSchema.Builder()
        .addProperty("string_value", new StringSchema.Builder().build())
        .addRequiredProperties("string_value")
        .build())
      .build();

    IOThingDescriptionTemplate tdt = new IOThingDescriptionTemplate.Builder(THING_TITLE)
      .addEvent(event)
      .build();

    assertIsomorphicGraphs(testTDT, tdt);
  }

  @Test
  public void testWriteAdditionalMetadata() throws RDFParseException, RDFHandlerException, IOException {
    String testTDT = PREFIXES +
      "@prefix eve: <http://w3id.org/eve#> .\n" +
      "<http://example.org/lamp123> a td:Thing, saref:LightSwitch;\n" +
      "    td:hasSecurityConfiguration [ a wotsec:NoSecurityScheme ];\n" +
      "    td:title \"My Lamp Thing\" ;\n" +
      "    eve:hasManual [ a eve:Manual;\n" +
      "        dct:title \"My Lamp Manual\";\n" +
      "        eve:hasUsageProtocol [ a eve:UsageProtocol;\n" +
      "            dct:title \"Party Light\";\n" +
      "            eve:hasLanguage <http://jason.sourceforge.net/wp/description/>\n" +
      "        ]\n" +
      "    ].\n";

    ValueFactory rdf = SimpleValueFactory.getInstance();
    Model metadata = new LinkedHashModel();

    final String NS = "http://w3id.org/eve#";
    metadata.setNamespace("eve", NS);

    BNode manualId = rdf.createBNode();
    BNode protocolId = rdf.createBNode();
    metadata.add(rdf.createIRI("http://example.org/lamp123"), rdf.createIRI(NS,"hasManual"), manualId);
    metadata.add(manualId, RDF.TYPE, rdf.createIRI(NS, "Manual"));
    metadata.add(manualId, DCTERMS.TITLE, rdf.createLiteral("My Lamp Manual"));

    IOThingDescriptionTemplate tdt = new IOThingDescriptionTemplate.Builder("My Lamp Thing")
      .addSemanticType("https://saref.etsi.org/core/LightSwitch")
      .addTriple(protocolId, RDF.TYPE, rdf.createIRI(NS, "UsageProtocol"))
      .addTriple(protocolId, DCTERMS.TITLE, rdf.createLiteral("Party Light"))
      .addGraph(metadata)
      .addGraph(new ModelBuilder()
        .add(manualId, rdf.createIRI(NS, "hasUsageProtocol"), protocolId)
        .build())
      .addTriple(protocolId, rdf.createIRI(NS,"hasLanguage"), rdf.createIRI("http://jason.sourceforge.net/wp/description/"))
      .build();

    assertIsomorphicGraphs(testTDT, tdt);
  }

  @Test
  public void testWriteReadmeExample() throws RDFParseException, RDFHandlerException, IOException {
    String testTDT = PREFIXES +
      "<http://example.org/lamp123> a td:Thing, saref:LightSwitch;\n" +
      "  td:hasSecurityConfiguration [ a wotsec:NoSecurityScheme ];\n" +
      "  td:title \"My Lamp Thing\" ;\n" +
      "  td:hasActionAffordance [ a td:ActionAffordance, saref:ToggleCommand;\n" +
      "      td:name \"toggle\";\n" +
      "      td:title \"Toggle\";\n" +
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



    IOActionAffordanceTemplate toggle = new IOActionAffordanceTemplate.Builder("toggle")
      .addTitle("Toggle")
      .addSemanticType("https://saref.etsi.org/core/ToggleCommand")
      .addInputSchema(new ObjectSchema.Builder()
        .addSemanticType("https://saref.etsi.org/core/OnOffState")
        .addProperty("status", new BooleanSchema.Builder()
          .build())
        .addRequiredProperties("status")
        .build())
      .build();

    IOThingDescriptionTemplate tdt = new IOThingDescriptionTemplate.Builder("My Lamp Thing")
      .addSemanticType("https://saref.etsi.org/core/LightSwitch")
      .addAction(toggle)
      .build();

    assertIsomorphicGraphs(testTDT, tdt);
  }

  @Test
  public void writeURIVariable() throws IOException {
    String testTDT = PREFIXES +
      "<http://example.org/lamp123> a td:Thing, saref:LightSwitch;\n" +
      "  td:hasSecurityConfiguration [ a wotsec:NoSecurityScheme ];\n" +
      "  td:title \"My Lamp Thing\" ;\n" +
      "  td:hasActionAffordance [ a td:ActionAffordance, saref:ToggleCommand;\n" +
      "  td:name   \"toggleAffordance\";  "+
      "  td:hasUriTemplateSchema [ a js:StringSchema;\n"+
      "  td:name  \"name\" ];  "+
      "      td:title \"Toggle\";\n" +
      "      td:hasForm [\n" +
      "          htv:methodName \"PUT\";\n" +
      "          hctl:forContentType \"application/json\";\n" +
      "          hctl:hasTarget <http://mylamp.example.org/%7Bname%7D/toggle>;\n" +
      "          hctl:hasOperationType td:invokeAction\n" +
      "        ];\n" +
      "      td:hasInputSchema [ a saref:OnOffState, js:ObjectSchema;\n" +
      "          js:properties [ a js:BooleanSchema;\n" +
      "              js:propertyName \"status\"\n" +
      "            ];\n" +
      "          js:required \"status\"\n" +
      "        ];\n" +
      "    ].";
    DataSchema uriVariable = new StringSchema.Builder().build();

    IOActionAffordanceTemplate toggle = new IOActionAffordanceTemplate.Builder("toggleAffordance")
      .addTitle("Toggle")
      .addSemanticType("https://saref.etsi.org/core/ToggleCommand")
      .addUriVariable("name",uriVariable)
      .addInputSchema(new ObjectSchema.Builder()
        .addSemanticType("https://saref.etsi.org/core/OnOffState")
        .addProperty("status", new BooleanSchema.Builder()
          .build())
        .addRequiredProperties("status")
        .build())
      .build();
    IOThingDescriptionTemplate tdt = new IOThingDescriptionTemplate.Builder("My Lamp Thing")
      .addSemanticType("https://saref.etsi.org/core/LightSwitch")
      .addAction(toggle)
      .build();
    assertIsomorphicGraphs(testTDT,tdt);
  }

  @Test
  public void writeManyUriVariables() throws IOException {
    String testTDT = PREFIXES +
      "<http://example.org/lamp123> a td:Thing, saref:LightSwitch;\n" +
      "  td:hasSecurityConfiguration [ a wotsec:NoSecurityScheme ];\n" +
      "  td:title \"My Lamp Thing\" ;\n" +
      "  td:hasActionAffordance [ a td:ActionAffordance, saref:ToggleCommand;\n" +
      "  td:name   \"toggleAffordance\";  "+
      "      td:title \"Toggle\";\n" +
      "      td:hasForm [\n" +
      "          htv:methodName \"PUT\";\n" +
      "          hctl:forContentType \"application/json\";\n" +
      "          hctl:hasTarget <http://mylamp.example.org/%7Bname%7D/%7Bnumber%7D/toggle>;\n" +
      "          hctl:hasOperationType td:invokeAction\n" +
      "        ];\n" +
      "      td:hasInputSchema [ a saref:OnOffState, js:ObjectSchema;\n" +
      "          js:properties [ a js:BooleanSchema;\n" +
      "              js:propertyName \"status\"\n" +
      "            ];\n" +
      "          js:required \"status\"\n" +
      "        ];\n" +
      "      td:hasUriTemplateSchema [ a js:StringSchema;\n"+
      "      td:name \"name\" ]; "+
      "      td:hasUriTemplateSchema [ a js:NumberSchema;\n"+
      "      td:name \"number\" ]; "+
      "    ].";
    DataSchema uriVariable1 = new StringSchema.Builder().build();
    DataSchema uriVariable2 = new NumberSchema.Builder().build();

    IOActionAffordanceTemplate toggle = new IOActionAffordanceTemplate.Builder("toggleAffordance")
      .addTitle("Toggle")
      .addSemanticType("https://saref.etsi.org/core/ToggleCommand")
      .addUriVariable("name", uriVariable1)
      .addUriVariable("number", uriVariable2)
      .addInputSchema(new ObjectSchema.Builder()
        .addSemanticType("https://saref.etsi.org/core/OnOffState")
        .addProperty("status", new BooleanSchema.Builder()
          .build())
        .addRequiredProperties("status")
        .build())
      .build();
    IOThingDescriptionTemplate tdt = new IOThingDescriptionTemplate.Builder("My Lamp Thing")
      .addSemanticType("https://saref.etsi.org/core/LightSwitch")
      .addAction(toggle)
      .build();
    assertIsomorphicGraphs(testTDT,tdt);
  }


  @Test
  public void writeURIVariablePropertyAffordance() throws IOException {
    String testTDT = PREFIXES +
      "<http://example.org/lamp123> a td:Thing;\n" +
      "  td:hasSecurityConfiguration [ a wotsec:NoSecurityScheme ];\n" +
      "  td:title \"My Lamp Thing\" ;\n" +
      "  td:hasPropertyAffordance [ a td:PropertyAffordance, js:StringSchema;\n" +
      "  td:name  \"lightAffordance\"; "+
      "       td:isObservable false;  "+
      "      td:title \"Light\";\n" +
      "      td:hasForm [\n" +
      "          htv:methodName \"GET\";\n" +
      "          hctl:forContentType \"application/json\";\n" +
      "          hctl:hasTarget <http://mylamp.example.org/%7Bname%7D/%7Bnumber%7D/light>;\n" +
      "          hctl:hasOperationType td:readProperty\n" +
      "        ];\n" +
      "      td:hasUriTemplateSchema [ a js:StringSchema;\n"+
      "      td:name   \"name\" ]; "+
      "      td:hasUriTemplateSchema [ a js:NumberSchema;\n"+
      "      td:name   \"number\" ]; "+
      "    ].";
    DataSchema uriVariable1 = new StringSchema.Builder().build();
    DataSchema uriVariable2 = new NumberSchema.Builder().build();
    DataSchema property=new StringSchema.Builder().build();
    IOPropertyAffordanceTemplate light = new IOPropertyAffordanceTemplate.Builder("lightAffordance")
      .addDataSchema(property)
      .addTitle("Light")
      .addUriVariable("name",uriVariable1)
      .addUriVariable("number", uriVariable2)
      .build();
    IOThingDescriptionTemplate tdt = new IOThingDescriptionTemplate.Builder("My Lamp Thing")
      .addProperty(light)
      .build();
    assertIsomorphicGraphs(testTDT,tdt);
  }




  private void assertIsomorphicGraphs(String expectedTD, IOThingDescriptionTemplate tdt) throws RDFParseException,
    RDFHandlerException, IOException {
    Model expectedModel = ReadWriteUtils.readModelFromString(RDFFormat.TURTLE, expectedTD, IO_BASE_IRI);

    String description = new IOTDTGraphWriter(tdt)
      .setNamespace("td", "https://www.w3.org/2019/wot/td#")
      .setNamespace("htv", "http://www.w3.org/2011/http#")
      .setNamespace("cov", "http://www.example.org/coap-binding#")
      .setNamespace("hctl", "https://www.w3.org/2019/wot/hypermedia#")
      .setNamespace("wotsec", "https://www.w3.org/2019/wot/security#")
      .setNamespace("dct", "http://purl.org/dc/terms/")
      .setNamespace("js", "https://www.w3.org/2019/wot/json-schema#")
      .setNamespace("saref", "https://saref.etsi.org/core/")
      .write();

    System.out.println(description);

    Model tdModel = ReadWriteUtils.readModelFromString(RDFFormat.TURTLE, description, IO_BASE_IRI);

    assertTrue(Models.isomorphic(expectedModel, tdModel));
  }

  private IOThingDescriptionTemplate constructThingDescriptionTemplate(List<IOPropertyAffordanceTemplate> properties,
                                                     List<IOActionAffordanceTemplate> actions) {
    IOThingDescriptionTemplate.Builder builder = new IOThingDescriptionTemplate.Builder(THING_TITLE);

    for (IOPropertyAffordanceTemplate property : properties) {
      builder.addProperty(property);
    }

    for (IOActionAffordanceTemplate action : actions) {
      builder.addAction(action);
    }

    return builder.build();
  }
}
