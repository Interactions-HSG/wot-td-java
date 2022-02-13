package ch.unisg.ics.interactions.wot.td.io;

import ch.unisg.ics.interactions.wot.td.ThingDescription;
import ch.unisg.ics.interactions.wot.td.affordances.ActionAffordance;
import ch.unisg.ics.interactions.wot.td.affordances.EventAffordance;
import ch.unisg.ics.interactions.wot.td.affordances.Form;
import ch.unisg.ics.interactions.wot.td.affordances.PropertyAffordance;
import ch.unisg.ics.interactions.wot.td.schemas.*;
import ch.unisg.ics.interactions.wot.td.security.*;
import ch.unisg.ics.interactions.wot.td.security.TokenBasedSecurityScheme.TokenLocation;
import ch.unisg.ics.interactions.wot.td.vocabularies.COV;
import ch.unisg.ics.interactions.wot.td.vocabularies.TD;
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
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class TDGraphWriterTest {
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
      "@prefix ex: <https://example.org#> .\n" +
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
        .addSecurityScheme("nodec_sc", SecurityScheme.getNoSecurityScheme())
        .build();

    assertIsomorphicGraphs(testTD, td);
  }

  @Test
  public void testWriteTitle() throws RDFParseException, RDFHandlerException, IOException {
    String testTD = PREFIXES +
        "<http://example.org/#thing> a td:Thing ;\n" +
        "    dct:title \"My Thing\" ;\n" +
        "    td:hasSecurityConfiguration [ a wotsec:NoSecurityScheme ] .\n" ;

    ThingDescription td = new ThingDescription.Builder(THING_TITLE)
        .addThingURI(THING_IRI)
        .addSecurityScheme("nosec_sc", SecurityScheme.getNoSecurityScheme())
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
        .addSecurityScheme("nosec_sc", SecurityScheme.getNoSecurityScheme())
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
      .addSecurityScheme("nosec_sc", SecurityScheme.getNoSecurityScheme())
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
        "    ] ." ;

    PropertyAffordance property = new PropertyAffordance.Builder("my_property",
            new Form.Builder("http://example.org/count").build())
        .addDataSchema(new IntegerSchema.Builder().build())
        .addSemanticType("http://iotschema.org/MyProperty")
        .addObserve()
        .build();

    ThingDescription td = new ThingDescription.Builder(THING_TITLE)
        .addThingURI(THING_IRI)
        .addSecurityScheme("nosec_sc", SecurityScheme.getNoSecurityScheme())
        .addProperty(property)
        .build();

    assertIsomorphicGraphs(testTD, td);
  }

  @Test
  public void testWriteOnePropertyNoSchema() throws IOException {
    String testTD = PREFIXES +
        "@prefix iot: <http://iotschema.org/> .\n" +
        "<http://example.org/#thing> a td:Thing ;\n" +
        "    dct:title \"My Thing\" ;\n" +
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

    PropertyAffordance property = new PropertyAffordance.Builder("my_property",
            new Form.Builder("http://example.org/count")
                .setContentType("video/mpeg")
                .addOperationType(TD.readProperty)
                .build())
        .addSemanticType("http://iotschema.org/MyProperty")
        .build();

    ThingDescription td = new ThingDescription.Builder(THING_TITLE)
        .addThingURI(THING_IRI)
        .addSecurityScheme("nosec_sc", SecurityScheme.getNoSecurityScheme())
        .addProperty(property)
        .build();

    assertIsomorphicGraphs(testTD, td);
  }

  @Test
  public void testWriteOnePropertyOneOperationTypeNoMethod() throws RDFParseException, RDFHandlerException,
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
      "            hctl:hasOperationType td:writeProperty;\n" +
      "        ] ;\n" +
      "    ] .";

    PropertyAffordance property = new PropertyAffordance.Builder("my_property",
      new Form.Builder("http://example.org/count")
        .addOperationType(TD.writeProperty)
        .build())
      .addSemanticType("http://iotschema.org/MyProperty")
      .addDataSchema(new IntegerSchema.Builder().build())
      .addObserve()
      .build();

    ThingDescription td = new ThingDescription.Builder(THING_TITLE)
      .addThingURI(THING_IRI)
      .addSecurityScheme("nosec_sc", SecurityScheme.getNoSecurityScheme())
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
      "    ] ." ;

    PropertyAffordance property = new PropertyAffordance.Builder("my_property",
      new Form.Builder("http://example.org/count")
        .addSubProtocol("websub")
        .build())
      .addDataSchema(new IntegerSchema.Builder().build())
      .addObserve()
      .build();

    ThingDescription td = constructThingDescription(new ArrayList<>(Collections.singletonList(property)),
      new ArrayList<>(Collections.emptyList()));

    assertIsomorphicGraphs(testTD, td);
  }

  @Test
  public void testWritePropertySubprotocolIRI() throws RDFParseException, RDFHandlerException, IOException {
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
      "            hctl:hasTarget <coap://example.org/count> ;\n" +
      "            cov:methodName \"GET\" ;\n" +
      "            hctl:forContentType \"application/json\";\n" +
      "            hctl:hasOperationType td:observeProperty;\n" +
      "            hctl:forSubProtocol cov:observe;\n" +
      "        ] ;\n" +
      "    ] .";


    PropertyAffordance property = new PropertyAffordance.Builder("my_property",
      new Form.Builder("coap://example.org/count")
        .addSubProtocol(COV.observe)
        .setMethodName("GET")
        .addOperationType(TD.observeProperty)
        .build())
      .addDataSchema(new IntegerSchema.Builder().build())
      .addObserve()
      .build();

    ThingDescription td = constructThingDescription(new ArrayList<>(Collections.singletonList(property)),
      new ArrayList<>(Collections.emptyList()));

    assertIsomorphicGraphs(testTD, td);
  }

  @Test
  public void testWriteActionDefaultMethodValues() throws IOException {
    String testTD = PREFIXES +
      "@prefix iot: <http://iotschema.org/> .\n" +
      "<http://example.org/#thing> a td:Thing ;\n" +
      "    dct:title \"My Thing\" ;\n" +
      "    td:hasSecurityConfiguration [ a wotsec:NoSecurityScheme ] ;\n" +
      "    td:hasActionAffordance [\n" +
      "        a td:ActionAffordance, iot:MyAction ;\n" +
      "        td:name \"my_action\" ;\n" +
      "        td:hasForm [\n" +
      "            htv:methodName \"POST\";\n" +
      "            hctl:hasTarget <http://example.org/count> ;\n" +
      "            hctl:forContentType \"application/json\";\n" +
      "            hctl:hasOperationType td:invokeAction;\n" +
      "        ] ,[\n" +
      "            cov:methodName \"POST\";\n" +
      "            hctl:hasTarget <coap://example.org/count> ;\n" +
      "            hctl:forContentType \"application/json\";\n" +
      "            hctl:hasOperationType td:invokeAction;\n" +
      "        ] ;\n" +
      "    ] .";

    Form httpForm = new Form.Builder("http://example.org/count")
      .addOperationType(TD.invokeAction)
      .build();

    Form coapForm = new Form.Builder("coap://example.org/count")
      .addOperationType(TD.invokeAction)
      .build();

    ActionAffordance action = new ActionAffordance.Builder("my_action", Arrays.asList(httpForm, coapForm))
      .addSemanticType("http://iotschema.org/MyAction")
      .build();

    ThingDescription td = new ThingDescription.Builder(THING_TITLE)
      .addThingURI(THING_IRI)
      .addSecurityScheme("nosec_sc", SecurityScheme.getNoSecurityScheme())
      .addAction(action)
      .build();

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
        "    ] ." ;

    ActionAffordance simpleAction = new ActionAffordance.Builder("my_action",
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

    ThingDescription td = constructThingDescription(new ArrayList<>(),
      new ArrayList<>(Collections.singletonList(simpleAction)));

    assertIsomorphicGraphs(testTD, td);
  }

  @Test
  public void testWriteEventDefaultOperationTypes() throws IOException {
    String testTD = PREFIXES +
      "@prefix iot: <http://iotschema.org/> .\n" +
      "<http://example.org/#thing> a td:Thing ;\n" +
      "    dct:title \"My Thing\" ;\n" +
      "    td:hasSecurityConfiguration [ a wotsec:NoSecurityScheme ] ;\n" +
      "    td:hasEventAffordance [\n" +
      "        a td:EventAffordance, iot:MyEvent ;\n" +
      "        td:name \"my_event\" ;\n" +
      "        td:hasForm [\n" +
      "            hctl:hasTarget <http://example.org/event> ;\n" +
      "            hctl:forContentType \"application/json\";\n" +
      "            hctl:hasOperationType td:subscribeEvent, td:unsubscribeEvent;\n" +
      "        ] ,[\n" +
      "            hctl:hasTarget <coap://example.org/event> ;\n" +
      "            hctl:forContentType \"application/json\";\n" +
      "            hctl:hasOperationType td:subscribeEvent, td:unsubscribeEvent;\n" +
      "        ] \n" +
      "    ] .";

    Form httpForm = new Form.Builder("http://example.org/event")
      .build();

    Form coapForm = new Form.Builder("coap://example.org/event")
      .build();

    EventAffordance event = new EventAffordance.Builder("my_event", Arrays.asList(httpForm, coapForm))
      .addSemanticType("http://iotschema.org/MyEvent")
      .build();

    ThingDescription td = new ThingDescription.Builder(THING_TITLE)
      .addThingURI(THING_IRI)
      .addSecurityScheme("nosec_sc", SecurityScheme.getNoSecurityScheme())
      .addEvent(event)
      .build();

    assertIsomorphicGraphs(testTD, td);
  }

  @Test
  public void testWriteEvent() throws IOException {
    String testTD = PREFIXES +
      "@prefix iot: <http://iotschema.org/> .\n" +
      "<http://example.org/#thing> a td:Thing ;\n" +
      "    dct:title \"My Thing\" ;\n" +
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

    Form form = new Form.Builder("http://example.org/event")
      .build();

    EventAffordance event = new EventAffordance.Builder("my_event", form)
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

    ThingDescription td = new ThingDescription.Builder(THING_TITLE)
      .addThingURI(THING_IRI)
      .addSecurityScheme("nosec_sc", SecurityScheme.getNoSecurityScheme())
      .addEvent(event)
      .build();

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
  	metadata.add(rdf.createIRI("http://example.org/lamp123"), rdf.createIRI(NS,"hasManual"), manualId);
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
  	    .addTriple(protocolId, rdf.createIRI(NS,"hasLanguage"), rdf.createIRI("http://jason.sourceforge.net/wp/description/"))
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

  @Test
  public void writeURIVariable() throws IOException {
    String testTD = PREFIXES +
      "<http://example.org/lamp123> a td:Thing, saref:LightSwitch;\n" +
      "  td:hasSecurityConfiguration [ a wotsec:NoSecurityScheme ];\n" +
      "  dct:title \"My Lamp Thing\" ;\n" +
      "  td:hasActionAffordance [ a td:ActionAffordance, saref:ToggleCommand;\n" +
      "  td:name   \"toggleAffordance\";  "+
      "  td:hasUriTemplateSchema [ a js:StringSchema;\n"+
      "  td:name  \"name\" ];  "+
      "      dct:title \"Toggle\";\n" +
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
    Form toggleForm = new Form.Builder("http://mylamp.example.org/{name}/toggle")
      .setMethodName("PUT")
      .build();

    ActionAffordance toggle = new ActionAffordance.Builder("toggleAffordance", toggleForm)
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
    ThingDescription td=new ThingDescription.Builder("My Lamp Thing")
      .addThingURI("http://example.org/lamp123")
      .addSemanticType("https://saref.etsi.org/core/LightSwitch")
      .addAction(toggle)
      .build();
    assertIsomorphicGraphs(testTD,td);
  }

  @Test
  public void writeManyUriVariables() throws IOException {
    String testTD = PREFIXES +
      "<http://example.org/lamp123> a td:Thing, saref:LightSwitch;\n" +
      "  td:hasSecurityConfiguration [ a wotsec:NoSecurityScheme ];\n" +
      "  dct:title \"My Lamp Thing\" ;\n" +
      "  td:hasActionAffordance [ a td:ActionAffordance, saref:ToggleCommand;\n" +
      "  td:name   \"toggleAffordance\";  "+
      "      dct:title \"Toggle\";\n" +
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
    Form toggleForm = new Form.Builder("http://mylamp.example.org/{name}/{number}/toggle")
      .setMethodName("PUT")
      .build();

    ActionAffordance toggle = new ActionAffordance.Builder("toggleAffordance", toggleForm)
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
    ThingDescription td=new ThingDescription.Builder("My Lamp Thing")
      .addThingURI("http://example.org/lamp123")
      .addSemanticType("https://saref.etsi.org/core/LightSwitch")
      .addAction(toggle)
      .build();
    assertIsomorphicGraphs(testTD,td);
  }


  @Test
  public void writeURIVariablePropertyAffordance() throws IOException {
    String testTD = PREFIXES +
      "<http://example.org/lamp123> a td:Thing;\n" +
      "  td:hasSecurityConfiguration [ a wotsec:NoSecurityScheme ];\n" +
      "  dct:title \"My Lamp Thing\" ;\n" +
      "  td:hasPropertyAffordance [ a td:PropertyAffordance, js:StringSchema;\n" +
      "  td:name  \"lightAffordance\"; "+
      "       td:isObservable false;  "+
      "      dct:title \"Light\";\n" +
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
    Form lightForm = new Form.Builder("http://mylamp.example.org/{name}/{number}/light")
      .setMethodName("GET")
      .addOperationType(TD.readProperty)
      .build();
    DataSchema property=new StringSchema.Builder().build();
    PropertyAffordance light = new PropertyAffordance.Builder("lightAffordance", lightForm)
      .addDataSchema(property)
      .addTitle("Light")
      .addUriVariable("name",uriVariable1)
      .addUriVariable("number", uriVariable2)
      .build();
    ThingDescription td=new ThingDescription.Builder("My Lamp Thing")
      .addThingURI("http://example.org/lamp123")
      .addProperty(light)
      .build();
    assertIsomorphicGraphs(testTD,td);
  }

  @Test
  public void testFormWithUnknownProtocolBinding() throws IOException {
    String testTD =
      "@prefix td: <https://www.w3.org/2019/wot/td#> .\n" +
        "@prefix htv: <http://www.w3.org/2011/http#> .\n" +
        "@prefix hctl: <https://www.w3.org/2019/wot/hypermedia#> . \n" +
        "@prefix wotsec: <https://www.w3.org/2019/wot/security#> .\n" +
        "@prefix dct: <http://purl.org/dc/terms/> .\n" +
        "@prefix js: <https://www.w3.org/2019/wot/json-schema#> .\n" +
        "\n" +
        "<http://example.org/#thing> a td:Thing ;\n" +
        "    dct:title \"My Thing\" ;\n" +
        "    td:hasSecurityConfiguration [ a wotsec:NoSecurityScheme ] ;\n" +
        "    td:hasPropertyAffordance [\n" +
        "        a td:PropertyAffordance, js:IntegerSchema ;\n" +
        "        td:name \"my_property\" ;\n" +
        "        td:hasForm [\n" +
        "            hctl:hasTarget <x://example.org/property> ;\n" +
        "            hctl:forContentType \"application/json\";\n" +
        "            hctl:hasOperationType td:readProperty;\n" +
        "        ] ;\n" +
        "        td:isObservable false \n" +
        "    ] .";

    Form form = new Form.Builder("x://example.org/property")
      .addOperationType(TD.readProperty)
      .build();

    PropertyAffordance prop = new PropertyAffordance.Builder("my_property", form)
      .addDataSchema(new IntegerSchema.Builder().build())
      .build();

    ThingDescription td = new ThingDescription.Builder("My Thing")
      .addThingURI("http://example.org/#thing")
      .addProperty(prop)
      .build();

    assertIsomorphicGraphs(testTD,td);
  }

  // Test APIKeySecurityScheme
  @Test
  public void testWriteAPIKeySecurityScheme() throws RDFParseException, RDFHandlerException, IOException {
    String testTD = PREFIXES +
      "<http://example.org/#thing> a td:Thing ;\n" +
      "    dct:title \"My Thing\" ;\n" +
      "    td:hasSecurityConfiguration [ a wotsec:APIKeySecurityScheme, ex:Type ;\n" +
      "        wotsec:in \"header\" ;\n" +
      "        wotsec:name \"X-API-Key\" ;\n" +
      "    ] .\n";

    ThingDescription tdSimple = new ThingDescription.Builder(THING_TITLE)
      .addThingURI(THING_IRI)
      .addSecurityScheme("apikey_sc", new APIKeySecurityScheme.Builder()
        .addSemanticType("https://example.org#Type")
        .addToken(TokenLocation.HEADER, "X-API-Key").build())
      .build();

    ThingDescription tdVerbose = new ThingDescription.Builder(THING_TITLE)
      .addThingURI(THING_IRI)
      .addSecurityScheme("apikey_sc", new APIKeySecurityScheme.Builder()
        .addSemanticType("https://example.org#Type")
        .addTokenLocation(TokenLocation.HEADER)
        .addTokenName("X-API-Key").build())
      .build();

    assertIsomorphicGraphs(testTD, tdSimple);
    assertIsomorphicGraphs(testTD, tdVerbose);
  }

  // Test BasicSecurityScheme
  @Test
  public void testWriteBasicSecurityScheme() throws RDFParseException, RDFHandlerException, IOException {
    String testTD = PREFIXES +
      "<http://example.org/#thing> a td:Thing ;\n" +
      "    dct:title \"My Thing\" ;\n" +
      "    td:hasSecurityConfiguration [ a wotsec:BasicSecurityScheme, ex:Type ;\n" +
      "        wotsec:in \"header\" ;\n" +
      "        wotsec:name \"Authorization\" ;\n" +
      "    ] .\n";

    ThingDescription td = new ThingDescription.Builder(THING_TITLE)
      .addThingURI(THING_IRI)
      .addSecurityScheme("basic_sc", new BasicSecurityScheme.Builder()
        .addSemanticType("https://example.org#Type")
        .addToken(TokenLocation.HEADER, "Authorization").build())
      .build();

    assertIsomorphicGraphs(testTD, td);
  }

  // Test DigestSecurityScheme
  @Test
  public void testWriteDigestSecurityScheme() throws RDFParseException, RDFHandlerException, IOException {
    String testTD = PREFIXES +
      "<http://example.org/#thing> a td:Thing ;\n" +
      "    dct:title \"My Thing\" ;\n" +
      "    td:hasSecurityConfiguration [ a wotsec:DigestSecurityScheme, ex:Type ;\n" +
      "        wotsec:in \"header\" ;\n" +
      "        wotsec:name \"nonce\" ;\n" +
      "        wotsec:qop \"auth-int\" ;\n" +
      "    ] .\n";

    ThingDescription td = new ThingDescription.Builder(THING_TITLE)
      .addThingURI(THING_IRI)
      .addSecurityScheme("digest_sc", new DigestSecurityScheme.Builder()
        .addSemanticType("https://example.org#Type")
        .addToken(TokenLocation.HEADER, "nonce")
        .addQoP(DigestSecurityScheme.QualityOfProtection.AUTH_INT)
        .build())
      .build();

    assertIsomorphicGraphs(testTD, td);
  }

  // Test BearerSecurityScheme
  @Test
  public void testWriteBearerSecurityScheme() throws RDFParseException, RDFHandlerException, IOException {
    String testTD = PREFIXES +
      "<http://example.org/#thing> a td:Thing ;\n" +
      "    dct:title \"My Thing\" ;\n" +
      "    td:hasSecurityConfiguration [ a wotsec:BearerSecurityScheme, ex:Type ;\n" +
      "        wotsec:in \"header\" ;\n" +
      "        wotsec:name \"Authorization\" ;\n" +
      "        wotsec:authorization \"server.example.com\" ;\n" +
      "        wotsec:alg \"ECDSA 256\" ;\n" +
      "        wotsec:format \"cwt\" ;\n" +
      "    ] .\n";

    ThingDescription td = new ThingDescription.Builder(THING_TITLE)
      .addThingURI(THING_IRI)
      .addSecurityScheme("bearer_sc", new BearerSecurityScheme.Builder()
        .addSemanticType("https://example.org#Type")
        .addToken(TokenLocation.HEADER, "Authorization")
        .addAuthorization("server.example.com")
        .addAlg("ECDSA 256")
        .addFormat("cwt")
        .build())
      .build();

    assertIsomorphicGraphs(testTD, td);
  }

  // Test PSKSecurityScheme
  @Test
  public void testWritePSKSecurityScheme() throws RDFParseException, RDFHandlerException, IOException {
    String testTD = PREFIXES +
      "<http://example.org/#thing> a td:Thing ;\n" +
      "    dct:title \"My Thing\" ;\n" +
      "    td:hasSecurityConfiguration [ a wotsec:PSKSecurityScheme, ex:Type ;\n" +
      "        wotsec:identity \"192.0.2.1\" ;\n" +
      "    ] .\n";

    ThingDescription td = new ThingDescription.Builder(THING_TITLE)
      .addThingURI(THING_IRI)
      .addSecurityScheme("psk_sc", new PSKSecurityScheme.Builder()
        .addSemanticType("https://example.org#Type")
        .addIdentity("192.0.2.1")
        .build())
      .build();

    assertIsomorphicGraphs(testTD, td);
  }

  private void assertIsomorphicGraphs(String expectedTD, ThingDescription td) throws RDFParseException,
    RDFHandlerException, IOException {
    Model expectedModel = ReadWriteUtils.readModelFromString(RDFFormat.TURTLE, expectedTD, IO_BASE_IRI);

    String description = new TDGraphWriter(td)
      .setNamespace("td", "https://www.w3.org/2019/wot/td#")
      .setNamespace("htv", "http://www.w3.org/2011/http#")
      .setNamespace("cov", "http://www.example.org/coap-binding#")
      .setNamespace("hctl", "https://www.w3.org/2019/wot/hypermedia#")
      .setNamespace("wotsec", "https://www.w3.org/2019/wot/security#")
      .setNamespace("dct", "http://purl.org/dc/terms/")
      .setNamespace("js", "https://www.w3.org/2019/wot/json-schema#")
      .setNamespace("ex", "https://example.org#")
      .setNamespace("saref", "https://saref.etsi.org/core/")
      .write();

    System.out.println(description);

    Model tdModel = ReadWriteUtils.readModelFromString(RDFFormat.TURTLE, description, IO_BASE_IRI);

    assertTrue(Models.isomorphic(expectedModel, tdModel));
  }

  private ThingDescription constructThingDescription(List<PropertyAffordance> properties,
      List<ActionAffordance> actions) {
    ThingDescription.Builder builder = new ThingDescription.Builder(THING_TITLE)
        .addThingURI(THING_IRI)
        .addBaseURI("http://example.org/")
      .addSecurityScheme("nosec_sc", SecurityScheme.getNoSecurityScheme());

    for (PropertyAffordance property : properties) {
      builder.addProperty(property);
    }

    for (ActionAffordance action : actions) {
      builder.addAction(action);
    }

    return builder.build();
  }
}
