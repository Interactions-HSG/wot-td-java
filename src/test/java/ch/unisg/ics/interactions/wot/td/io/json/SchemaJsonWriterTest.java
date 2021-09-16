package ch.unisg.ics.interactions.wot.td.io.json;

import ch.unisg.ics.interactions.wot.td.schemas.*;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import javax.json.Json;
import javax.json.JsonObject;

public class SchemaJsonWriterTest extends TestCase {

  private final static String PREFIX = "https://example.org/#";

  private static DataSchema semanticObjectSchema;

  @Test
  public void testWriteStringSchema() {
    JsonObject expected = Json.createObjectBuilder()
      .add("type", "string")
      .build();
    JsonObject test =  SchemaJsonWriter.getDataSchema(
      new StringSchema.Builder().build()
    ).build();
    assertEquals(expected, test);
  }

  @Test
  public void testBooleanSchema() {
    JsonObject expected = Json.createObjectBuilder()
      .add("type", "boolean")
      .build();
    JsonObject test =  SchemaJsonWriter.getDataSchema(
      new BooleanSchema.Builder().build()
    ).build();
    assertEquals(expected, test);
  }

  @Test
  public void testNullSchema() {
    JsonObject expected = Json.createObjectBuilder()
      .add("type", "null")
      .build();
    JsonObject test =  SchemaJsonWriter.getDataSchema(
      new NullSchema.Builder().build()
    ).build();
    assertEquals(expected, test);
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
    assertEquals(expected, test);
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
    assertEquals(expected, test);
  }


}
