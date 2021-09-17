package ch.unisg.ics.interactions.wot.td.io.json;

import ch.unisg.ics.interactions.wot.td.schemas.*;
import org.junit.Assert;
import org.junit.Test;

import javax.json.Json;
import javax.json.JsonObject;

public class SchemaJsonWriterTest {

  @Test
  public void testWriteStringSchema() {
    JsonObject expected = Json.createObjectBuilder()
      .add("type", "string")
      .build();
    JsonObject test =  SchemaJsonWriter.getDataSchema(
      new StringSchema.Builder().build()
    ).build();
    Assert.assertEquals(expected, test);
  }

  @Test
  public void testBooleanSchema() {
    JsonObject expected = Json.createObjectBuilder()
      .add("type", "boolean")
      .build();
    JsonObject test =  SchemaJsonWriter.getDataSchema(
      new BooleanSchema.Builder().build()
    ).build();
    Assert.assertEquals(expected, test);
  }

  @Test
  public void testNullSchema() {
    JsonObject expected = Json.createObjectBuilder()
      .add("type", "null")
      .build();
    JsonObject test =  SchemaJsonWriter.getDataSchema(
      new NullSchema.Builder().build()
    ).build();
    Assert.assertEquals(expected, test);
  }

  @Test
  public void testIntegerSchema() {
    JsonObject expected = Json.createObjectBuilder()
      .add("type", "integer")
      .add("minimum", 5)
      .add("maximum", 10)
      .build();
    JsonObject test =  SchemaJsonWriter.getDataSchema(
      new IntegerSchema.Builder()
      .addMaximum(10)
      .addMinimum(5)
      .build()
    ).build();
    Assert.assertEquals(expected, test);
  }

  @Test
  public void testNumberSchema() {
    JsonObject expected = Json.createObjectBuilder()
      .add("type", "number")
      .add("minimum", 5.5)
      .add("maximum", 10.5)
      .build();
    JsonObject test =  SchemaJsonWriter.getDataSchema(
      new NumberSchema.Builder()
        .addMaximum(10.5)
        .addMinimum(5.5)
        .build()
    ).build();
    Assert.assertEquals(expected, test);
  }

  @Test
  public void testSimpleArraySchema(){
    JsonObject expected = Json.createObjectBuilder()
      .add("type", "array")
      .add("items", Json.createObjectBuilder().add("type", "string"))
      .add("minItems", 1)
      .add("maxItems", 10)
      .build();
    JsonObject test =  SchemaJsonWriter.getDataSchema(
      new ArraySchema.Builder()
        .addItem(new StringSchema.Builder().build())
        .addMaxItems(10)
        .addMinItems(1)
        .build()
    ).build();

    Assert.assertEquals(expected, test);
  }

  @Test
  public void testSimpleObjectSchema() {
    JsonObject expected = Json.createObjectBuilder()
      .add("type", "object")
      .add("properties", Json.createObjectBuilder()
        .add("name", Json.createObjectBuilder()
        .add("type", "string"))
      ).build();

    JsonObject test = SchemaJsonWriter.getDataSchema(
      new ObjectSchema.Builder()
        .addProperty("name", new StringSchema.Builder().build())
      .build()
    ).build();
    Assert.assertEquals(expected, test);
  }

  @Test
  public void testSimpleObjectSchemaWithRequired() {
    JsonObject expected = Json.createObjectBuilder()
      .add("type", "object")
      .add("properties", Json.createObjectBuilder()
        .add("name", Json.createObjectBuilder()
          .add("type", "string")
        ).add("age", Json.createObjectBuilder()
          .add("type", "integer")
          .add("miniumum", 0)
        )
      ).add("required", Json.createArrayBuilder()
      .add("name").add("age")
      ).build();

    JsonObject test = SchemaJsonWriter.getDataSchema(
      new ObjectSchema.Builder()
        .addProperty("name", new StringSchema.Builder().build())
        .addProperty("age", new IntegerSchema.Builder().addMinimum(0).build())
        .addRequiredProperties("name", "age")
        .build()
    ).build();

    Assert.assertEquals(expected, test);
  }

  @Test
  public void testSemanticObject(){
    JsonObject expected = Json.createObjectBuilder()
      .add("@type", Json.createArrayBuilder().add("sem:employee").add("sem:person"))
      .add("type", "object")
      .add("properties", Json.createObjectBuilder()
        .add("name", Json.createObjectBuilder()
          .add("@type", "sem:name")
          .add("type", "string")
        ).add("age", Json.createObjectBuilder()
          .add("@type", "sem:age")
          .add("type", "integer")
          .add("minimum", 0)
        )
      ).add("required", Json.createArrayBuilder()
        .add("name").add("age")
      ).build();

    JsonObject test = SchemaJsonWriter.getDataSchema(
      new ObjectSchema.Builder()
        .addSemanticType("sem:person")
        .addSemanticType("sem:employee")
        .addProperty("name", new StringSchema.Builder().addSemanticType("sem:name").build())
        .addProperty("age", new IntegerSchema.Builder().addSemanticType("sem:age").addMinimum(0).build())
        .addRequiredProperties("name", "age")
        .build()
    ).build();

    Assert.assertEquals(expected, test);
  }


}
