package ch.unisg.ics.interactions.wot.td.io;

import ch.unisg.ics.interactions.wot.td.ThingDescription;
import ch.unisg.ics.interactions.wot.td.affordances.ActionAffordance;
import ch.unisg.ics.interactions.wot.td.affordances.Form;
import ch.unisg.ics.interactions.wot.td.affordances.PropertyAffordance;
import ch.unisg.ics.interactions.wot.td.schemas.BooleanSchema;
import ch.unisg.ics.interactions.wot.td.schemas.IntegerSchema;
import ch.unisg.ics.interactions.wot.td.schemas.NumberSchema;
import ch.unisg.ics.interactions.wot.td.schemas.ObjectSchema;
import ch.unisg.ics.interactions.wot.td.security.APIKeySecurityScheme;
import ch.unisg.ics.interactions.wot.td.security.APIKeySecurityScheme.TokenLocation;
import ch.unisg.ics.interactions.wot.td.security.NoSecurityScheme;
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
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class TDGraphWriterTest {
  private static final String THING_TITLE = "My Thing";
  private static final String THING_IRI = "http://example.org/#thing";
  private static final String IO_BASE_IRI = "http://example.org/";

  private static final String PREFIXES =
    "@prefix td: <https://www.w3.org/2019/wot/td#> .\n" +
      "@prefix htv: <http://www.w3.org/2011/http#> .\n" +
      "@prefix hctl: <https://www.w3.org/2019/wot/hypermedia#> .\n" +
      "@prefix dct: <http://purl.org/dc/terms/> .\n" +
      "@prefix wotsec: <https://www.w3.org/2019/wot/security#> .\n" +
      "@prefix js: <https://www.w3.org/2019/wot/json-schema#> .\n" +
      "@prefix saref: <https://saref.etsi.org/core/> .\n" +
      "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n";

  @Test
  public void testNoThingURI() throws RDFParseException, RDFHandlerException, IOException {
    String testTD =
      PREFIXES +
        "\n" +
        "[] a td:Thing ;\n" +
        "    dct:title \"My Thing\" ;\n" +
        "    td:hasSecurityConfiguration [ a wotsec:NoSecurityScheme ] .\n";

    ThingDescription td = new ThingDescription.Builder(THING_TITLE)
      .addSecurityScheme(new NoSecurityScheme())
      .build();

    assertIsomorphicGraphs(testTD, td);
  }

  @Test
  public void testWriteTitle() throws RDFParseException, RDFHandlerException, IOException {
    String testTD = PREFIXES +
      "<http://example.org/#thing> a td:Thing ;\n" +
      "    dct:title \"My Thing\" ;\n" +
      "    td:hasSecurityConfiguration [ a wotsec:NoSecurityScheme ] .\n";

    ThingDescription td = new ThingDescription.Builder(THING_TITLE)
      .addThingURI(THING_IRI)
      .addSecurityScheme(new NoSecurityScheme())
      .build();

    assertIsomorphicGraphs(testTD, td);
  }

  @Test
  public void testWriteAPIKeySecurityScheme() throws RDFParseException, RDFHandlerException, IOException {
    String testTD = PREFIXES +
      "<http://example.org/#thing> a td:Thing ;\n" +
      "    dct:title \"My Thing\" ;\n" +
      "    td:hasSecurityConfiguration [ a wotsec:APIKeySecurityScheme ;\n" +
      "        wotsec:in \"HEADER\" ;\n" +
      "        wotsec:name \"X-API-Key\" ;\n" +
      "    ] .\n";

    ThingDescription td = new ThingDescription.Builder(THING_TITLE)
      .addThingURI(THING_IRI)
      .addSecurityScheme(new APIKeySecurityScheme(TokenLocation.HEADER, "X-API-Key"))
      .build();

    assertIsomorphicGraphs(testTD, td);
  }

  @Test
  public void testWriteAdditionalTypes() throws RDFParseException, RDFHandlerException, IOException {
    String testTD =
      PREFIXES +
        "@prefix eve: <http://w3id.org/eve#> .\n" +
        "@prefix iot: <http://iotschema.org/> .\n" +
        "\n" +
        "<http://example.org/#thing> a td:Thing, eve:Artifact, iot:Light ;\n" +
        "    dct:title \"My Thing\" ;\n" +
        "    td:hasSecurityConfiguration [ a wotsec:NoSecurityScheme ] .\n";

    ThingDescription td = new ThingDescription.Builder(THING_TITLE)
      .addThingURI(THING_IRI)
      .addSemanticType("http://w3id.org/eve#Artifact")
      .addSemanticType("http://iotschema.org/Light")
      .addSecurityScheme(new NoSecurityScheme())
      .build();

    assertIsomorphicGraphs(testTD, td);
  }

  @Test
  public void testWriteTypesDeduplication() throws RDFParseException, RDFHandlerException,
    IOException {

    String testTD =
      PREFIXES +
        "@prefix eve: <http://w3id.org/eve#> .\n" +
        "\n" +
        "<http://example.org/#thing> a td:Thing, eve:Artifact ;\n" +
        "    dct:title \"My Thing\" ;\n" +
        "    td:hasSecurityConfiguration [ a wotsec:NoSecurityScheme ] .\n";

    ThingDescription td = new ThingDescription.Builder(THING_TITLE)
      .addThingURI(THING_IRI)
      .addSemanticType("http://w3id.org/eve#Artifact")
      .addSemanticType("http://w3id.org/eve#Artifact")
      .addSecurityScheme(new NoSecurityScheme())
      .build();

    assertIsomorphicGraphs(testTD, td);
  }

  @Test
  public void testWriteBaseURI() throws RDFParseException, RDFHandlerException, IOException {
    String testTD =
      PREFIXES +
        "\n" +
        "<http://example.org/#thing> a td:Thing ;\n" +
        "    td:hasSecurityConfiguration [ a wotsec:NoSecurityScheme ];\n" +
        "    dct:title \"My Thing\" ;\n" +
        "    td:hasBase <http://example.org/> .\n";

    ThingDescription td = new ThingDescription.Builder(THING_TITLE)
      .addThingURI(THING_IRI)
      .addBaseURI("http://example.org/")
      .build();

    assertIsomorphicGraphs(testTD, td);
  }

  @Test
  public void testWriteOnePropertyDefaultValues() throws RDFParseException, RDFHandlerException,
    IOException {
    String testTD = PREFIXES +
      "@prefix iot: <http://iotschema.org/> .\n" +
      "<http://example.org/#thing> a td:Thing ;\n" +
      "    dct:title \"My Thing\" ;\n" +
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
      "    ] .";


    PropertyAffordance property = new PropertyAffordance.Builder("my_property",
      new IntegerSchema.Builder().build(),
      new Form.Builder("http://example.org/count").build())
      .addSemanticType("http://iotschema.org/MyProperty")
      .addObserve()
      .build();

    ThingDescription td = new ThingDescription.Builder(THING_TITLE)
      .addThingURI(THING_IRI)
      .addSecurityScheme(new NoSecurityScheme())
      .addProperty(property)
      .build();

    assertIsomorphicGraphs(testTD, td);
  }

  @Test
  public void testWritePropertySubprotocol() throws RDFParseException, RDFHandlerException,
    IOException {
    String testTD = PREFIXES +
      "<http://example.org/#thing> a td:Thing ;\n" +
      "    dct:title \"My Thing\" ;\n" +
      "    td:hasSecurityConfiguration [ a wotsec:NoSecurityScheme ] ;\n" +
      "    td:hasBase <http://example.org/> ;\n" +
      "    td:hasPropertyAffordance [\n" +
      "        a td:PropertyAffordance, js:IntegerSchema ;\n" +
      "        td:name \"my_property\" ;\n" +
      "        td:isObservable true ;\n" +
      "        td:hasForm [\n" +
      "            hctl:hasTarget <http://example.org/count> ;\n" +
      "            hctl:forContentType \"application/json\";\n" +
      "            hctl:hasOperationType td:readProperty, td:writeProperty;\n" +
      "            hctl:forSubProtocol \"websub\";\n" +
      "        ] ;\n" +
      "    ] .";


    PropertyAffordance property = new PropertyAffordance.Builder("my_property",
      new IntegerSchema.Builder().build(),
      new Form.Builder("http://example.org/count")
        .addSubProtocol("websub")
        .build())
      .addObserve()
      .build();

    ThingDescription td = constructThingDescription(new ArrayList<PropertyAffordance>(Arrays.asList(property)),
      new ArrayList<ActionAffordance>(Arrays.asList()));

    assertIsomorphicGraphs(testTD, td);
  }

  @Test
  public void testWriteOneAction() throws RDFParseException, RDFHandlerException, IOException {
    String testTD = PREFIXES +
      "@prefix iot: <http://iotschema.org/> .\n" +
      "\n" +
      "<http://example.org/#thing> a td:Thing ;\n" +
      "    dct:title \"My Thing\" ;\n" +
      "    td:hasSecurityConfiguration [ a wotsec:NoSecurityScheme ] ;\n" +
      "    td:hasBase <http://example.org/> ;\n" +
      "    td:hasActionAffordance [\n" +
      "        a td:ActionAffordance, iot:MyAction ;\n" +
      "        td:name \"my_action\" ;\n" +
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
      "    ] .";

    ActionAffordance simpleAction = new ActionAffordance.Builder("my_action",
      new Form.Builder("http://example.org/action")
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

    ThingDescription td = constructThingDescription(new ArrayList<PropertyAffordance>(),
      new ArrayList<ActionAffordance>(Arrays.asList(simpleAction)));

    assertIsomorphicGraphs(testTD, td);
  }

  @Test
  public void testWriteAdditionalMetadata() throws RDFParseException, RDFHandlerException, IOException {
    String testTD = PREFIXES +
      "@prefix eve: <http://w3id.org/eve#> .\n" +
      "<http://example.org/lamp123> a td:Thing, saref:LightSwitch;\n" +
      "    td:hasSecurityConfiguration [ a wotsec:NoSecurityScheme ];\n" +
      "    dct:title \"My Lamp Thing\" ;\n" +
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
    metadata.add(rdf.createIRI("http://example.org/lamp123"), rdf.createIRI(NS, "hasManual"), manualId);
    metadata.add(manualId, RDF.TYPE, rdf.createIRI(NS, "Manual"));
    metadata.add(manualId, DCTERMS.TITLE, rdf.createLiteral("My Lamp Manual"));

    ThingDescription td = new ThingDescription.Builder("My Lamp Thing")
      .addThingURI("http://example.org/lamp123")
      .addSemanticType("https://saref.etsi.org/core/LightSwitch")
      .addTriple(protocolId, RDF.TYPE, rdf.createIRI(NS, "UsageProtocol"))
      .addTriple(protocolId, DCTERMS.TITLE, rdf.createLiteral("Party Light"))
      .addGraph(metadata)
      .addGraph(new ModelBuilder()
        .add(manualId, rdf.createIRI(NS, "hasUsageProtocol"), protocolId)
        .build())
      .addTriple(protocolId, rdf.createIRI(NS, "hasLanguage"), rdf.createIRI("http://jason.sourceforge.net/wp/description/"))
      .build();

    assertIsomorphicGraphs(testTD, td);
  }

  @Test
  public void testWriteReadmeExample() throws RDFParseException, RDFHandlerException, IOException {
    String testTD = PREFIXES +
      "<http://example.org/lamp123> a td:Thing, saref:LightSwitch;\n" +
      "  td:hasSecurityConfiguration [ a wotsec:NoSecurityScheme ];\n" +
      "  dct:title \"My Lamp Thing\" ;\n" +
      "  td:hasActionAffordance [ a td:ActionAffordance, saref:ToggleCommand;\n" +
      "      td:name \"toggle\";\n" +
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

    Form toggleForm = new Form.Builder("http://mylamp.example.org/toggle")
      .setMethodName("PUT")
      .build();

    ActionAffordance toggle = new ActionAffordance.Builder("toggle", toggleForm)
      .addTitle("Toggle")
      .addSemanticType("https://saref.etsi.org/core/ToggleCommand")
      .addInputSchema(new ObjectSchema.Builder()
        .addSemanticType("https://saref.etsi.org/core/OnOffState")
        .addProperty("status", new BooleanSchema.Builder()
          .build())
        .addRequiredProperties("status")
        .build())
      .build();

    ThingDescription td = new ThingDescription.Builder("My Lamp Thing")
      .addThingURI("http://example.org/lamp123")
      .addSemanticType("https://saref.etsi.org/core/LightSwitch")
      .addAction(toggle)
      .build();

    assertIsomorphicGraphs(testTD, td);
  }

  private void assertIsomorphicGraphs(String expectedTD, ThingDescription td) throws RDFParseException,
    RDFHandlerException, IOException {
    Model expectedModel = ReadWriteUtils.readModelFromString(RDFFormat.TURTLE, expectedTD,
      IO_BASE_IRI);

    String description = new TDGraphWriter(td)
      .setNamespace("td", "https://www.w3.org/2019/wot/td#")
      .setNamespace("htv", "http://www.w3.org/2011/http#")
      .setNamespace("hctl", "https://www.w3.org/2019/wot/hypermedia#")
      .setNamespace("wotsec", "https://www.w3.org/2019/wot/security#")
      .setNamespace("dct", "http://purl.org/dc/terms/")
      .setNamespace("js", "https://www.w3.org/2019/wot/json-schema#")
      .setNamespace("saref", "https://saref.etsi.org/core/")
      .write();

    System.out.println(description);

    Model tdModel = ReadWriteUtils.readModelFromString(RDFFormat.TURTLE, description,
      IO_BASE_IRI);

    assertTrue(Models.isomorphic(expectedModel, tdModel));

  }

  private ThingDescription constructThingDescription(List<PropertyAffordance> properties,
                                                     List<ActionAffordance> actions) {
    ThingDescription.Builder builder = new ThingDescription.Builder(THING_TITLE)
      .addThingURI(THING_IRI)
      .addBaseURI("http://example.org/")
      .addSecurityScheme(new NoSecurityScheme());

    for (PropertyAffordance property : properties) {
      builder.addProperty(property);
    }

    for (ActionAffordance action : actions) {
      builder.addAction(action);
    }

    return builder.build();
  }
}
