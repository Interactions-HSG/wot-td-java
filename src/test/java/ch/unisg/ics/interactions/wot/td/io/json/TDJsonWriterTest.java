package ch.unisg.ics.interactions.wot.td.io.json;

import ch.unisg.ics.interactions.wot.td.ThingDescription;
import ch.unisg.ics.interactions.wot.td.affordances.ActionAffordance;
import ch.unisg.ics.interactions.wot.td.affordances.Form;
import ch.unisg.ics.interactions.wot.td.affordances.PropertyAffordance;
import ch.unisg.ics.interactions.wot.td.schemas.ObjectSchema;
import ch.unisg.ics.interactions.wot.td.schemas.StringSchema;
import ch.unisg.ics.interactions.wot.td.security.APIKeySecurityScheme;
import ch.unisg.ics.interactions.wot.td.security.BasicSecurityScheme;
import ch.unisg.ics.interactions.wot.td.security.NoSecurityScheme;
import ch.unisg.ics.interactions.wot.td.vocabularies.TD;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.junit.Assert;
import org.junit.Test;

import javax.json.Json;
import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.List;

public class TDJsonWriterTest {

  private static final String THING_TITLE = "My Thing";
  private static final String THING_IRI = "http://example.org/#thing";
  private static final String IO_BASE_IRI = "http://example.org/";

  @Test
  public void testEmptyThing() {
    ThingDescription td = new ThingDescription.Builder(THING_TITLE)
      .build();

    JsonObject expected = Json.createObjectBuilder()
      .add("@context", "https://www.w3.org/2019/wot/td/v1")
      .add("title", THING_TITLE)
      .add("securityDefinitions", Json.createObjectBuilder().add("nosec", Json.createObjectBuilder().add("scheme", "nosec")))
      .add("security", Json.createArrayBuilder().add("nosec"))
      .build();

    JsonObject test = new TDJsonWriter(td).getJson();
    Assert.assertEquals(expected, test);
  }

  @Test
  public void testThingWithTDOntologyPrefix() {
    ThingDescription td = new ThingDescription.Builder(THING_TITLE)
      .addSemanticType(TD.Thing)
      .build();

    JsonObject expected = Json.createObjectBuilder()
      .add("@context", "https://www.w3.org/2019/wot/td/v1")
      .add("@type", "Thing")
      .add("title", THING_TITLE)
      .add("securityDefinitions", Json.createObjectBuilder().add("nosec", Json.createObjectBuilder().add("scheme", "nosec")))
      .add("security", Json.createArrayBuilder().add("nosec"))
      .build();

    JsonObject test = new TDJsonWriter(td).getJson();
    Assert.assertEquals(expected, test);
  }

  @Test
  public void testThingWithSemanticType() {
    ThingDescription td = new ThingDescription.Builder(THING_TITLE)
      .addSemanticType("http://w3id.org/eve#Artifact")
      .build();

    JsonObject expected = Json.createObjectBuilder()
      .add("@context", "https://www.w3.org/2019/wot/td/v1")
      .add("@type", "http://w3id.org/eve#Artifact")
      .add("title", THING_TITLE)
      .add("securityDefinitions", Json.createObjectBuilder().add("nosec", Json.createObjectBuilder().add("scheme", "nosec")))
      .add("security", Json.createArrayBuilder().add("nosec"))
      .build();

    JsonObject test = new TDJsonWriter(td).getJson();
    Assert.assertEquals(expected, test);
  }

  @Test
  public void testThingWithSemanticTypes() {
    ThingDescription td = new ThingDescription.Builder(THING_TITLE)
      .addSemanticType("http://w3id.org/eve#Artifact")
      .addSemanticType("http://iotschema.org/Light")
      .build();

    JsonObject expected = Json.createObjectBuilder()
      .add("@context", Json.createArrayBuilder()
        .add("https://www.w3.org/2019/wot/td/v1")
        .add(Json.createObjectBuilder()
          .add("eve", "http://w3id.org/eve#")
          .add("iot", "http://iotschema.org/")))
      .add("@type", Json.createArrayBuilder().add("eve:Artifact").add("iot:Light"))
      .add("title", THING_TITLE)
      .add("securityDefinitions", Json.createObjectBuilder().add("nosec", Json.createObjectBuilder().add("scheme", "nosec")))
      .add("security", Json.createArrayBuilder().add("nosec"))
      .build();

    JsonObject test = new TDJsonWriter(td)
      .setNamespace("eve", "http://w3id.org/eve#")
      .setNamespace("iot", "http://iotschema.org/")
      .getJson();

    Assert.assertEquals(expected, test);
  }

  @Test
  public void testThingWithNameSpace() {
    ThingDescription td = new ThingDescription.Builder(THING_TITLE)
      .addSemanticType("http://w3id.org/eve#Artifact")
      .build();

    JsonObject test = new TDJsonWriter(td).setNamespace("eve", "http://w3id.org/eve#").getJson();

    JsonObject expected = Json.createObjectBuilder()
      .add("@context", Json.createArrayBuilder().add("https://www.w3.org/2019/wot/td/v1")
        .add(Json.createObjectBuilder().add("eve", "http://w3id.org/eve#"))
      ).add("@type", "eve:Artifact")
      .add("title", THING_TITLE)
      .add("securityDefinitions", Json.createObjectBuilder().add("nosec", Json.createObjectBuilder().add("scheme", "nosec")))
      .add("security", Json.createArrayBuilder().add("nosec"))
      .build();

    Assert.assertEquals(expected, test);
  }

  @Test
  public void testThingWithOverlappingNameSpaces() {
    ThingDescription td = new ThingDescription.Builder(THING_TITLE)
      .addSemanticType("http://example.org/Type1")
      .addSemanticType("http://example.org/overlapping/Type2")
      .build();

    JsonObject test = new TDJsonWriter(td)
      .setNamespace("ex1", "http://example.org/")
      .setNamespace("ex2", "http://example.org/overlapping/")
      .getJson();

    JsonObject expected = Json.createObjectBuilder()
      .add("@context", Json.createArrayBuilder()
        .add("https://www.w3.org/2019/wot/td/v1")
        .add(Json.createObjectBuilder()
          .add("ex1", "http://example.org/")
          .add("ex2", "http://example.org/overlapping/")))
      .add("@type", Json.createArrayBuilder().add("ex1:Type1").add("ex2:Type2"))
      .add("title", THING_TITLE)
      .add("securityDefinitions", Json.createObjectBuilder().add("nosec", Json.createObjectBuilder().add("scheme", "nosec")))
      .add("security", Json.createArrayBuilder().add("nosec"))
      .build();

    Assert.assertEquals(expected, test);
  }

  @Test
  public void testThingWithIRI() {
    ThingDescription td = new ThingDescription.Builder(THING_TITLE)
      .addThingURI(THING_IRI)
      .build();

    JsonObject expected = Json.createObjectBuilder()
      .add("@context", "https://www.w3.org/2019/wot/td/v1")
      .add("title", THING_TITLE)
      .add("id", THING_IRI)
      .add("securityDefinitions", Json.createObjectBuilder().add("nosec", Json.createObjectBuilder().add("scheme", "nosec")))
      .add("security", Json.createArrayBuilder().add("nosec"))
      .build();

    JsonObject test = new TDJsonWriter(td).getJson();
    Assert.assertEquals(expected, test);
  }

  @Test
  public void testThingWithBase() {
    ThingDescription td = new ThingDescription.Builder(THING_TITLE)
      .addBaseURI(IO_BASE_IRI)
      .build();

    JsonObject expected = Json.createObjectBuilder()
      .add("@context", "https://www.w3.org/2019/wot/td/v1")
      .add("title", THING_TITLE)
      .add("base", IO_BASE_IRI)
      .add("securityDefinitions", Json.createObjectBuilder().add("nosec", Json.createObjectBuilder().add("scheme", "nosec")))
      .add("security", Json.createArrayBuilder().add("nosec"))
      .build();

    JsonObject test = new TDJsonWriter(td).getJson();
    Assert.assertEquals(expected, test);
  }

  @Test
  public void testThingWithProperties() {
    List<PropertyAffordance> properties = new ArrayList<>();
    properties.add(new PropertyAffordance.Builder(
      new StringSchema.Builder().build(),
      new Form.Builder(THING_IRI + "/status")
        .setMethodName("GET")
        .addOperationType(TD.readProperty).build()
    ).addObserve().addName("status").build());

    ThingDescription td = new ThingDescription.Builder(THING_TITLE)
      .addBaseURI(IO_BASE_IRI)
      .addProperties(properties)
      .build();

    JsonObject expected = Json.createObjectBuilder()
      .add("@context", "https://www.w3.org/2019/wot/td/v1")
      .add("title", THING_TITLE)
      .add("securityDefinitions", Json.createObjectBuilder().add("nosec", Json.createObjectBuilder().add("scheme", "nosec")))
      .add("security", Json.createArrayBuilder().add("nosec"))
      .add("base", IO_BASE_IRI)
      .add("properties", Json.createObjectBuilder().add("status",
        Json.createObjectBuilder()
          .add("type", "string")
          .add("observable", true)
          .add("forms", Json.createArrayBuilder().add(
            Json.createObjectBuilder()
              .add("href", THING_IRI + "/status")
              .add("htv:methodName", "GET")
              .add("contentType", "application/json")
              .add("op", Json.createArrayBuilder().add("readproperty"))
            )
          )
        )
      )
      .build();

    JsonObject test = new TDJsonWriter(td).getJson();
    Assert.assertEquals(expected, test);
  }

  @Test
  public void testThingPropertiesWithOneSemanticType() {
    List<PropertyAffordance> properties = new ArrayList<>();
    properties.add(new PropertyAffordance.Builder(
      new StringSchema.Builder().build(),
      new Form.Builder(THING_IRI + "/status").build())
      .addObserve()
      .addName("status")
      .addSemanticType("http://example.org/Status")
      .build());

    ThingDescription td = new ThingDescription.Builder(THING_TITLE)
      .addBaseURI(IO_BASE_IRI)
      .addProperties(properties)
      .build();

    JsonObject expected = Json.createObjectBuilder()
      .add("@context", Json.createArrayBuilder()
        .add("https://www.w3.org/2019/wot/td/v1")
        .add(Json.createObjectBuilder()
          .add("ex", "http://example.org/")))
      .add("title", THING_TITLE)
      .add("securityDefinitions", Json.createObjectBuilder().add("nosec", Json.createObjectBuilder().add("scheme", "nosec")))
      .add("security", Json.createArrayBuilder().add("nosec"))
      .add("base", IO_BASE_IRI)
      .add("properties", Json.createObjectBuilder().add("status",
        Json.createObjectBuilder()
          .add("type", "string")
          .add("@type", "ex:Status")
          .add("observable", true)
          .add("forms", Json.createArrayBuilder().add(
            Json.createObjectBuilder()
              .add("href", THING_IRI + "/status")
              .add("contentType", "application/json")
              .add("op", Json.createArrayBuilder().add("readproperty").add("writeproperty"))
            )
          )
        )
      )
      .build();

    JsonObject test = new TDJsonWriter(td)
      .setNamespace("ex", "http://example.org/")
      .getJson();

    Assert.assertEquals(expected, test);
  }


  @Test
  public void testThingWithActions() {
    List<ActionAffordance> actions = new ArrayList<>();
    actions.add(new ActionAffordance.Builder(
      new Form.Builder(THING_IRI + "/changeColor")
        .setMethodName("POST").build()
    ).addName("changeColor")
      .addInputSchema(new ObjectSchema.Builder()
        .addProperty("color", new StringSchema.Builder().build())
        .build())
      .addOutputSchema(new ObjectSchema.Builder()
        .addProperty("color", new StringSchema.Builder().build())
        .build())
      .build());
    actions.add(new ActionAffordance.Builder(
        new Form.Builder(THING_IRI + "/changeState")
          .setMethodName("POST").build()
      ).addName("changeState").build()
    );


    ThingDescription td = new ThingDescription.Builder(THING_TITLE)
      .addBaseURI(IO_BASE_IRI)
      .addActions(actions)
      .build();

    JsonObject expected = Json.createObjectBuilder()
      .add("@context", "https://www.w3.org/2019/wot/td/v1")
      .add("title", THING_TITLE)
      .add("securityDefinitions", Json.createObjectBuilder().add("nosec", Json.createObjectBuilder().add("scheme", "nosec")))
      .add("security", Json.createArrayBuilder().add("nosec"))
      .add("base", IO_BASE_IRI)
      .add("actions", Json.createObjectBuilder().add("changeColor", Json.createObjectBuilder()
          .add("input", Json.createObjectBuilder()
            .add("type", "object").add("properties", Json.createObjectBuilder()
              .add("color", Json.createObjectBuilder().add("type", "string")))
          ).add("output", Json.createObjectBuilder()
            .add("type", "object").add("properties", Json.createObjectBuilder()
              .add("color", Json.createObjectBuilder().add("type", "string")))
          ).add("forms", Json.createArrayBuilder().add(
          Json.createObjectBuilder()
            .add("href", THING_IRI + "/changeColor")
            .add("htv:methodName", "POST")
            .add("contentType", "application/json")
            .add("op", Json.createArrayBuilder().add("invokeaction"))
          )
          )
        ).add("changeState", Json.createObjectBuilder()
          .add("forms", Json.createArrayBuilder().add(
            Json.createObjectBuilder()
              .add("href", THING_IRI + "/changeState")
              .add("htv:methodName", "POST")
              .add("contentType", "application/json")
              .add("op", Json.createArrayBuilder().add("invokeaction"))
            )
          )
        )
      )
      .build();

    JsonObject test = new TDJsonWriter(td).getJson();
    Assert.assertEquals(expected, test);
  }

  @Test
  public void testThingActionsWithSemanticTypes() {
    List<ActionAffordance> actions = new ArrayList<>();
    actions.add(new ActionAffordance.Builder(
      new Form.Builder(THING_IRI + "/changeColor")
        .build())
      .addName("changeColor")
      .addSemanticType("http://example.org/1/SetColor1")
      .addSemanticType("http://example.org/2/SetColor2")
      .build());
    actions.add(new ActionAffordance.Builder(
      new Form.Builder(THING_IRI + "/changeState").build())
      .addName("changeState")
      .addSemanticType("http://example.org/1/SetState1")
      .addSemanticType("http://example.org/2/SetState2")
      .build());


    ThingDescription td = new ThingDescription.Builder(THING_TITLE)
      .addBaseURI(IO_BASE_IRI)
      .addActions(actions)
      .build();

    JsonObject expected = Json.createObjectBuilder()
      .add("@context", Json.createArrayBuilder()
        .add("https://www.w3.org/2019/wot/td/v1")
        .add(Json.createObjectBuilder()
          .add("ex1", "http://example.org/1/")
          .add("ex2", "http://example.org/2/")))
      .add("title", THING_TITLE)
      .add("securityDefinitions", Json.createObjectBuilder().add("nosec", Json.createObjectBuilder().add("scheme", "nosec")))
      .add("security", Json.createArrayBuilder().add("nosec"))
      .add("base", IO_BASE_IRI)
      .add("actions", Json.createObjectBuilder()
        .add("changeColor", Json.createObjectBuilder()
          .add("@type", Json.createArrayBuilder()
            .add("ex1:SetColor1")
            .add("ex2:SetColor2"))
          .add("forms", Json.createArrayBuilder()
            .add(Json.createObjectBuilder()
              .add("href", THING_IRI + "/changeColor")
              .add("contentType", "application/json")
              .add("htv:methodName", "POST")
              .add("op", Json.createArrayBuilder().add("invokeaction")))))
        .add("changeState", Json.createObjectBuilder()
          .add("@type", Json.createArrayBuilder()
            .add("ex1:SetState1")
            .add("ex2:SetState2"))
          .add("forms", Json.createArrayBuilder().add(
            Json.createObjectBuilder()
              .add("href", THING_IRI + "/changeState")
              .add("contentType", "application/json")
              .add("htv:methodName", "POST")
              .add("op", Json.createArrayBuilder().add("invokeaction"))
            )
          )
        )
      )
      .build();

    JsonObject test = new TDJsonWriter(td)
      .setNamespace("ex1", "http://example.org/1/")
      .setNamespace("ex2", "http://example.org/2/")
      .getJson();

    Assert.assertEquals(expected, test);
  }

  @Test
  public void testWriteAdditionalMetadata() {

    ValueFactory rdf = SimpleValueFactory.getInstance();
    Model metadata = new LinkedHashModel();

    final String NS = "http://w3id.org/eve#";
    metadata.setNamespace("eve", NS);

    BNode manualId = rdf.createBNode();
    BNode protocolId = rdf.createBNode();
    metadata.add(rdf.createIRI("http://example.org/lamp123"), rdf.createIRI(NS,"hasManual"), manualId);
    metadata.add(manualId, RDF.TYPE, rdf.createIRI(NS, "Manual"));

    ThingDescription td = new ThingDescription.Builder("My Thing")
      .addThingURI("http://example.org/lamp123")
      .addSemanticType("https://saref.etsi.org/core/LightSwitch")
      .addTriple(rdf.createIRI("http://example.org/lamp123"), RDF.TYPE, rdf.createIRI(NS,
        "Artifact"))
      .addTriple(protocolId, RDF.TYPE, rdf.createIRI(NS, "UsageProtocol"))
      .addTriple(rdf.createIRI("http://example.org/lamp123"),rdf.createIRI(NS,"hasManual"),
        rdf.createIRI("http://example.org/manuals/anotherManual"))
      .addGraph(metadata)
      .addGraph(new ModelBuilder()
        .add(manualId, rdf.createIRI(NS, "hasUsageProtocol"), protocolId)
        .build())
      .addTriple(protocolId, rdf.createIRI(NS,"hasLanguage"), rdf.createIRI("http://jason.sourceforge.net/wp/description/"))
      .build();

    JsonObject expected = Json.createObjectBuilder()
      .add("@context", Json.createArrayBuilder()
        .add("https://www.w3.org/2019/wot/td/v1")
        .add(Json.createObjectBuilder()
          .add("saref", "https://saref.etsi.org/core/")
          .add( "eve", "http://w3id.org/eve#")))
      .add("title", THING_TITLE)
      .add("id", "http://example.org/lamp123")
      .add("@type", Json.createArrayBuilder()
        .add("eve:Artifact")
        .add("saref:LightSwitch"))
      .add("securityDefinitions", Json.createObjectBuilder().add("nosec", Json.createObjectBuilder().add("scheme", "nosec")))
      .add("security", Json.createArrayBuilder().add("nosec"))
      .add("eve:hasManual" , Json.createArrayBuilder().add("http://example.org/manuals/anotherManual").add(Json.createObjectBuilder()
        .add("@type","eve:Manual")
        .add("eve:hasUsageProtocol", Json.createObjectBuilder()
          .add("@type", "eve:UsageProtocol")
          .add("eve:hasLanguage", "http://jason.sourceforge.net/wp/description/"))))
      .build();

    JsonObject test = new TDJsonWriter(td)
      .setNamespace("saref", "https://saref.etsi.org/core/")
      .getJson();

    System.out.println(test);
    Assert.assertEquals(expected, test);
  }

  @Test
  public void testWriteAPIKeySecurityScheme() {
    ThingDescription td = new ThingDescription.Builder(THING_TITLE)
      .addThingURI(THING_IRI)
      .addSecurityScheme("apikey", new APIKeySecurityScheme.Builder()
        .addTokenLocation(APIKeySecurityScheme.TokenLocation.HEADER)
        .addTokenName("X-API-Key")
        .build())
      .build();

    JsonObject expected = Json.createObjectBuilder()
      .add("@context", "https://www.w3.org/2019/wot/td/v1")
      .add("title", THING_TITLE)
      .add("id", THING_IRI)
      .add("securityDefinitions", Json.createObjectBuilder().add("apikey",
        Json.createObjectBuilder()
          .add("scheme", "apikey")
          .add("in", "header")
          .add("name", "X-API-Key")))
      .add("security", Json.createArrayBuilder().add("apikey"))
      .build();

    JsonObject test = new TDJsonWriter(td).getJson();

    System.out.println(test);
    Assert.assertEquals(expected, test);

  }

  @Test
  public void testWriteDefaultAPIKeySecurityScheme() {
    ThingDescription td = new ThingDescription.Builder(THING_TITLE)
      .addThingURI(THING_IRI)
      .addSecurityScheme("apikey", new APIKeySecurityScheme.Builder()
        .addTokenName("X-API-Key")
        .build())
      .build();

    JsonObject expected = Json.createObjectBuilder()
      .add("@context", "https://www.w3.org/2019/wot/td/v1")
      .add("title", THING_TITLE)
      .add("id", THING_IRI)
      .add("securityDefinitions", Json.createObjectBuilder().add("apikey",
        Json.createObjectBuilder()
          .add("scheme", "apikey")
          .add("in", "query")
          .add("name", "X-API-Key")))
      .add("security", Json.createArrayBuilder().add("apikey"))
      .build();

    JsonObject test = new TDJsonWriter(td).getJson();

    System.out.println(test);
    Assert.assertEquals(expected, test);

  }

  @Test
  public void testWriteBasicSecurityScheme() {
    ThingDescription td = new ThingDescription.Builder(THING_TITLE)
      .addThingURI(THING_IRI)
      .addSecurityScheme("basic", new BasicSecurityScheme.Builder()
        .addTokenLocation(BasicSecurityScheme.TokenLocation.HEADER)
        .addTokenName("Authorization")
        .build())
      .build();

    JsonObject expected = Json.createObjectBuilder()
      .add("@context", "https://www.w3.org/2019/wot/td/v1")
      .add("title", THING_TITLE)
      .add("id", THING_IRI)
      .add("securityDefinitions", Json.createObjectBuilder().add("basic",
        Json.createObjectBuilder()
          .add("scheme", "basic")
          .add("in", "header")
          .add("name", "Authorization")))
      .add("security", Json.createArrayBuilder().add("basic"))
      .build();

    JsonObject test = new TDJsonWriter(td).getJson();

    System.out.println(test);
    Assert.assertEquals(expected, test);

  }

  @Test
  public void testWriteDefaultBasicSecurityScheme() {
    ThingDescription td = new ThingDescription.Builder(THING_TITLE)
      .addThingURI(THING_IRI)
      .addSecurityScheme("basic", new BasicSecurityScheme.Builder()
        .build())
      .build();

    JsonObject expected = Json.createObjectBuilder()
      .add("@context", "https://www.w3.org/2019/wot/td/v1")
      .add("title", THING_TITLE)
      .add("id", THING_IRI)
      .add("securityDefinitions", Json.createObjectBuilder().add("basic",
        Json.createObjectBuilder()
          .add("scheme", "basic")
          .add("in", "header")))
      .add("security", Json.createArrayBuilder().add("basic"))
      .build();

    JsonObject test = new TDJsonWriter(td).getJson();

    System.out.println(test);
    Assert.assertEquals(expected, test);

  }

}
