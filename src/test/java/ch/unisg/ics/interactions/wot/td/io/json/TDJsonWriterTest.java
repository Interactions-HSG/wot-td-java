package ch.unisg.ics.interactions.wot.td.io.json;

import ch.unisg.ics.interactions.wot.td.ThingDescription;
import ch.unisg.ics.interactions.wot.td.affordances.ActionAffordance;
import ch.unisg.ics.interactions.wot.td.affordances.Form;
import ch.unisg.ics.interactions.wot.td.affordances.PropertyAffordance;
import ch.unisg.ics.interactions.wot.td.schemas.ObjectSchema;
import ch.unisg.ics.interactions.wot.td.schemas.StringSchema;
import ch.unisg.ics.interactions.wot.td.security.NoSecurityScheme;
import ch.unisg.ics.interactions.wot.td.vocabularies.TD;
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
      .addSecurityScheme(new NoSecurityScheme())
      .build();

    JsonObject expected = Json.createObjectBuilder()
      .add("@context", "https://www.w3.org/2019/wot/td/v1")
      .add("title", THING_TITLE)
      .add("securityDefinitions", Json.createObjectBuilder().add("nosec_sc", Json.createObjectBuilder().add("scheme", "nosec")))
      .add("security", Json.createArrayBuilder().add("nosec_sc"))
      .build();

    JsonObject test = new TDJsonWriter(td).getJson();
    Assert.assertEquals(expected, test);
  }

  @Test
  public void testThingWithSemanticType() {
    ThingDescription td = new ThingDescription.Builder(THING_TITLE)
      .addSemanticType("http://w3id.org/eve#Artifact")
      .addSecurityScheme(new NoSecurityScheme())
      .build();

    JsonObject expected = Json.createObjectBuilder()
      .add("@context", "https://www.w3.org/2019/wot/td/v1")
      .add("@type", "http://w3id.org/eve#Artifact")
      .add("title", THING_TITLE)
      .add("securityDefinitions", Json.createObjectBuilder().add("nosec_sc", Json.createObjectBuilder().add("scheme", "nosec")))
      .add("security", Json.createArrayBuilder().add("nosec_sc"))
      .build();

    JsonObject test = new TDJsonWriter(td).getJson();
    Assert.assertEquals(expected, test);
  }

  @Test
  public void testThingWithSemanticTypes() {
    ThingDescription td = new ThingDescription.Builder(THING_TITLE)
      .addSemanticType("http://w3id.org/eve#Artifact")
      .addSemanticType("http://iotschema.org/Light")
      .addSecurityScheme(new NoSecurityScheme())
      .build();

    JsonObject expected = Json.createObjectBuilder()
      .add("@context", Json.createArrayBuilder()
        .add("https://www.w3.org/2019/wot/td/v1")
        .add(Json.createObjectBuilder()
          .add("eve", "http://w3id.org/eve#")
          .add("iot", "http://iotschema.org/").build()).build())
      .add("@type", Json.createArrayBuilder().add("eve:Artifact").add("iot:Light"))
      .add("title", THING_TITLE)
      .add("securityDefinitions", Json.createObjectBuilder().add("nosec_sc", Json.createObjectBuilder().add("scheme", "nosec")))
      .add("security", Json.createArrayBuilder().add("nosec_sc"))
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
      .addSecurityScheme(new NoSecurityScheme())
      .build();

    JsonObject test = new TDJsonWriter(td).setNamespace("eve", "http://w3id.org/eve#").getJson();

    JsonObject expected = Json.createObjectBuilder()
      .add("@context", Json.createArrayBuilder().add("https://www.w3.org/2019/wot/td/v1")
        .add(Json.createObjectBuilder().add("eve", "http://w3id.org/eve#"))
      ).add("@type", "eve:Artifact")
      .add("title", THING_TITLE)
      .add("securityDefinitions", Json.createObjectBuilder().add("nosec_sc", Json.createObjectBuilder().add("scheme", "nosec")))
      .add("security", Json.createArrayBuilder().add("nosec_sc"))
      .build();

    Assert.assertEquals(expected, test);
  }

  @Test
  public void testThingWithOverlappingNameSpaces() {
    ThingDescription td = new ThingDescription.Builder(THING_TITLE)
      .addSemanticType("http://example.org/Type1")
      .addSemanticType("http://example.org/overlapping/Type2")
      .addSecurityScheme(new NoSecurityScheme())
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
      .add("securityDefinitions", Json.createObjectBuilder().add("nosec_sc", Json.createObjectBuilder().add("scheme", "nosec")))
      .add("security", Json.createArrayBuilder().add("nosec_sc"))
      .build();

    Assert.assertEquals(expected, test);
  }

  @Test
  public void testThingWithIRI() {
    ThingDescription td = new ThingDescription.Builder(THING_TITLE)
      .addSecurityScheme(new NoSecurityScheme())
      .addThingURI(THING_IRI)
      .build();

    JsonObject expected = Json.createObjectBuilder()
      .add("@context", "https://www.w3.org/2019/wot/td/v1")
      .add("title", THING_TITLE)
      .add("id", THING_IRI)
      .add("securityDefinitions", Json.createObjectBuilder().add("nosec_sc", Json.createObjectBuilder().add("scheme", "nosec")))
      .add("security", Json.createArrayBuilder().add("nosec_sc"))
      .build();

    JsonObject test = new TDJsonWriter(td).getJson();
    Assert.assertEquals(expected, test);
  }

  @Test
  public void testThingWithBase() {
    ThingDescription td = new ThingDescription.Builder(THING_TITLE)
      .addSecurityScheme(new NoSecurityScheme())
      .addBaseURI(IO_BASE_IRI)
      .build();

    JsonObject expected = Json.createObjectBuilder()
      .add("@context", "https://www.w3.org/2019/wot/td/v1")
      .add("title", THING_TITLE)
      .add("base", IO_BASE_IRI)
      .add("securityDefinitions", Json.createObjectBuilder().add("nosec_sc", Json.createObjectBuilder().add("scheme", "nosec")))
      .add("security", Json.createArrayBuilder().add("nosec_sc"))
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
    ).addObserve().addTitle("status").build());

    ThingDescription td = new ThingDescription.Builder(THING_TITLE)
      .addBaseURI(IO_BASE_IRI)
      .addSecurityScheme(new NoSecurityScheme())
      .addProperties(properties)
      .build();

    JsonObject expected = Json.createObjectBuilder()
      .add("@context", "https://www.w3.org/2019/wot/td/v1")
      .add("title", THING_TITLE)
      .add("securityDefinitions", Json.createObjectBuilder().add("nosec_sc", Json.createObjectBuilder().add("scheme", "nosec")))
      .add("security", Json.createArrayBuilder().add("nosec_sc"))
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
      .addTitle("status")
      .addSemanticType("http://example.org/Status")
      .build());

    ThingDescription td = new ThingDescription.Builder(THING_TITLE)
      .addBaseURI(IO_BASE_IRI)
      .addSecurityScheme(new NoSecurityScheme())
      .addProperties(properties)
      .build();

    JsonObject expected = Json.createObjectBuilder()
      .add("@context", Json.createArrayBuilder()
        .add("https://www.w3.org/2019/wot/td/v1")
        .add(Json.createObjectBuilder()
          .add("ex", "http://example.org/")))
      .add("title", THING_TITLE)
      .add("securityDefinitions", Json.createObjectBuilder().add("nosec_sc", Json.createObjectBuilder().add("scheme", "nosec")))
      .add("security", Json.createArrayBuilder().add("nosec_sc"))
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
    ).addTitle("changeColor")
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
      ).addTitle("changeState").build()
    );


    ThingDescription td = new ThingDescription.Builder(THING_TITLE)
      .addBaseURI(IO_BASE_IRI)
      .addSecurityScheme(new NoSecurityScheme())
      .addActions(actions)
      .build();

    JsonObject expected = Json.createObjectBuilder()
      .add("@context", "https://www.w3.org/2019/wot/td/v1")
      .add("title", THING_TITLE)
      .add("securityDefinitions", Json.createObjectBuilder().add("nosec_sc", Json.createObjectBuilder().add("scheme", "nosec")))
      .add("security", Json.createArrayBuilder().add("nosec_sc"))
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
      .addTitle("changeColor")
      .addSemanticType("http://example.org/1/SetColor1")
      .addSemanticType("http://example.org/2/SetColor2")
      .build());
    actions.add(new ActionAffordance.Builder(
      new Form.Builder(THING_IRI + "/changeState").build())
      .addTitle("changeState")
      .addSemanticType("http://example.org/1/SetState1")
      .addSemanticType("http://example.org/2/SetState2")
      .build());


    ThingDescription td = new ThingDescription.Builder(THING_TITLE)
      .addBaseURI(IO_BASE_IRI)
      .addSecurityScheme(new NoSecurityScheme())
      .addActions(actions)
      .build();

    JsonObject expected = Json.createObjectBuilder()
      .add("@context", Json.createArrayBuilder()
        .add("https://www.w3.org/2019/wot/td/v1")
        .add(Json.createObjectBuilder()
          .add("ex1", "http://example.org/1/")
          .add("ex2", "http://example.org/2/")))
      .add("title", THING_TITLE)
      .add("securityDefinitions", Json.createObjectBuilder().add("nosec_sc", Json.createObjectBuilder().add("scheme", "nosec")))
      .add("security", Json.createArrayBuilder().add("nosec_sc"))
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

}
