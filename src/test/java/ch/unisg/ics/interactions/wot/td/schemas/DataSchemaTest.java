package ch.unisg.ics.interactions.wot.td.schemas;

import ch.unisg.ics.interactions.wot.td.io.InvalidTDException;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class DataSchemaTest {

  private final ObjectSchema userSchema = new ObjectSchema.Builder()
    .addSemanticType("http://example.com#User")
    .addProperty("id", new IntegerSchema.Builder()
      .addSemanticType("http://example.com#Id")
      .build())
    .addProperty("full_name", new StringSchema.Builder()
      .addSemanticType("http://example.com#FullName")
      .build())
    .addRequiredProperties("id")
    .build();

  private final ObjectSchema userGroupSchema = new ObjectSchema.Builder()
    .addSemanticType("http://example.com#UserGroup")
    .addProperty("count", new IntegerSchema.Builder()
      .addSemanticType("http://example.com#Count")
      .addMinimum(0)
      .addMaximum(100)
      .build())
    .addProperty("admin", userSchema)
    .build();

  @Test
  public void testEmptySchema() {
    DataSchema schema = DataSchema.getEmptySchema();
    assertEquals(DataSchema.EMPTY, schema.getDatatype());
    assertTrue(schema.getSemanticTypes().isEmpty());
    assertTrue(schema.getEnumeration().isEmpty());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testEmptySchemaUnmodifiableSemanticTypes() {
    DataSchema schema = DataSchema.getEmptySchema();
    schema.getSemanticTypes().add("sem1");
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testEmptySchemaUnmodifiableEnumeration() {
    DataSchema schema = DataSchema.getEmptySchema();
    schema.getEnumeration().add("a");
  }

  @Test
  public void testStringSchema() {
    DataSchema stringSchema = new StringSchema.Builder().build();
    assertEquals("string", stringSchema.getDatatype());

    DataSchema semanticString = new StringSchema.Builder()
      .addSemanticType("sem1")
      .addSemanticType("sem2")
      .addSemanticTypes(new HashSet<String>(Arrays.asList("sem3", "sem4")))
      .build();

    assertEquals("string", semanticString.getDatatype());
    assertTrue(semanticString.getSemanticTypes().contains("sem1"));
    assertTrue(semanticString.getSemanticTypes().contains("sem2"));
    assertTrue(semanticString.getSemanticTypes().contains("sem3"));
    assertTrue(semanticString.getSemanticTypes().contains("sem4"));
  }

  @Test
  public void testBooleanSchema() {
    DataSchema booleanSchema = new BooleanSchema.Builder().build();
    assertEquals("boolean", booleanSchema.getDatatype());

    DataSchema semanticBoolean = new BooleanSchema.Builder()
      .addSemanticType("sem1")
      .addSemanticType("sem2")
      .addSemanticTypes(new HashSet<String>(Arrays.asList("sem3", "sem4")))
      .build();

    assertEquals("boolean", semanticBoolean.getDatatype());
    assertTrue(semanticBoolean.getSemanticTypes().contains("sem1"));
    assertTrue(semanticBoolean.getSemanticTypes().contains("sem2"));
    assertTrue(semanticBoolean.getSemanticTypes().contains("sem3"));
    assertTrue(semanticBoolean.getSemanticTypes().contains("sem4"));
  }

  @Test
  public void testNullSchema() {
    DataSchema nullSchema = new NullSchema.Builder().build();
    assertEquals("null", nullSchema.getDatatype());

    // To be discussed: does it make sense to have semantic tags for null schemas?
    DataSchema semanticNull = new NullSchema.Builder()
      .addSemanticType("sem1")
      .addSemanticType("sem2")
      .addSemanticTypes(new HashSet<String>(Arrays.asList("sem3", "sem4")))
      .build();
    assertEquals("null", semanticNull.getDatatype());

    assertTrue(semanticNull.getSemanticTypes().contains("sem1"));
    assertTrue(semanticNull.getSemanticTypes().contains("sem2"));
    assertTrue(semanticNull.getSemanticTypes().contains("sem3"));
    assertTrue(semanticNull.getSemanticTypes().contains("sem4"));
  }

  @Test
  public void testIntegerSchema() {
    IntegerSchema integerSchema = new IntegerSchema.Builder()
      .addMinimum(-100)
      .addMaximum(100)
      .build();
    assertEquals("integer", integerSchema.getDatatype());
    assertEquals(-100, integerSchema.getMinimum().get().longValue());
    assertEquals(100, integerSchema.getMaximum().get().longValue());
  }

  @Test
  public void testIntegerSchemaNoLimits() {
    IntegerSchema integerSchema = new IntegerSchema.Builder().build();

    assertEquals("integer", integerSchema.getDatatype());
    assertTrue(!integerSchema.getMaximum().isPresent());
    assertTrue(!integerSchema.getMinimum().isPresent());
  }

  @Test
  public void testNumberSchema() {
    NumberSchema numberSchema = new NumberSchema.Builder()
      .addMinimum(-100.05)
      .addMaximum(100.05)
      .build();
    assertEquals("number", numberSchema.getDatatype());
    assertEquals(-100.05, numberSchema.getMinimum().get().doubleValue(), 0.01);
    assertEquals(100.05, numberSchema.getMaximum().get().doubleValue(), 0.01);
  }

  @Test
  public void testNumberSchemaNoLimits() {
    NumberSchema numberSchema = new NumberSchema.Builder().build();

    assertEquals("number", numberSchema.getDatatype());
    assertTrue(!numberSchema.getMaximum().isPresent());
    assertTrue(!numberSchema.getMinimum().isPresent());
  }

  @Test
  public void testObjectSchema() {
    ObjectSchema schema = new ObjectSchema.Builder()
      .addProperty("id", new IntegerSchema.Builder().build())
      .addProperty("active", new BooleanSchema.Builder().build())
      .addProperty("first_name", new StringSchema.Builder().build())
      .addProperty("last_name", new StringSchema.Builder().build())
      .addProperty("age", new IntegerSchema.Builder().addMaximum(18).addMaximum(150).build())
      .addProperty("height", new NumberSchema.Builder().addMaximum(299.99).addMaximum(100.0).build())
      .addProperty("connections", new NullSchema.Builder().build())
      .addRequiredProperties("id", "active")
      .build();

    assertEquals(7, schema.getProperties().size());
    assertTrue(schema.getProperties().containsKey("id"));
    assertTrue(schema.getProperties().containsKey("active"));
    assertTrue(schema.getProperties().containsKey("first_name"));
    assertTrue(schema.getProperties().containsKey("last_name"));
    assertTrue(schema.getProperties().containsKey("age"));
    assertTrue(schema.getProperties().containsKey("height"));
    assertTrue(schema.getProperties().containsKey("connections"));

    assertEquals(2, schema.getRequiredProperties().size());
    assertTrue(schema.getRequiredProperties().contains("id"));
    assertTrue(schema.getRequiredProperties().contains("active"));

    assertEquals(DataSchema.INTEGER, schema.getProperty("id").get().getDatatype());
    assertEquals(DataSchema.BOOLEAN, schema.getProperty("active").get().getDatatype());
    assertEquals(DataSchema.STRING, schema.getProperty("first_name").get().getDatatype());
    assertEquals(DataSchema.STRING, schema.getProperty("last_name").get().getDatatype());
    assertEquals(DataSchema.INTEGER, schema.getProperty("age").get().getDatatype());
    assertEquals(DataSchema.NUMBER, schema.getProperty("height").get().getDatatype());
    assertEquals(DataSchema.NULL, schema.getProperty("connections").get().getDatatype());
  }

  @Test(expected = InvalidTDException.class)
  public void testObjectSchemaMissingRequired() {
    new ObjectSchema.Builder()
      .addProperty("full_name", new StringSchema.Builder().build())
      .addRequiredProperties("id")
      .build();
  }

  @Test
  public void testSchemaNestedObjects() {
    assertEquals(2, userGroupSchema.getProperties().size());
    assertEquals(userSchema, userGroupSchema.getProperty("admin").get());
  }

  @Test
  public void testInstantiateObjectWithMismatchingProperties() {
    HashMap<String, Object> user = new HashMap<>();
    user.put("http://example.com#Id", 42);
    user.put("http://example.com#WrongType", "Douglas Adams");

    Map<String, Object> userPayload = userSchema.instantiate(user);

    assertTrue(userPayload.containsKey("id"));
    assertFalse(userPayload.containsKey("full_name"));

    assertEquals(42, userPayload.get("id"));
  }

  @Test
  public void testInstantiateObjectByType() {
    HashMap<String, Object> user = new HashMap<>();
    user.put("http://example.com#Id", 42);
    user.put("http://example.com#FullName", "Douglas Adams");

    Map<String, Object> userPayload = userSchema.instantiate(user);

    assertTrue(userPayload.containsKey("id"));
    assertTrue(userPayload.containsKey("full_name"));

    assertEquals(42, userPayload.get("id"));
    assertEquals("Douglas Adams", userPayload.get("full_name"));
  }

  @Test
  public void testInstantiateNestedObjectsByType() {
    HashMap<String, Object> user = new HashMap<>();
    user.put("http://example.com#Id", 42);
    user.put("http://example.com#FullName", "Douglas Adams");

    HashMap<String, Object> userGroup = new HashMap<>();
    userGroup.put("http://example.com#Count", 2);
    userGroup.put("http://example.com#User", user);

    Map<String, Object> userGroupPayload = userGroupSchema.instantiate(userGroup);

    assertTrue(userGroupPayload.containsKey("count"));
    assertTrue(userGroupPayload.containsKey("admin"));

    assertEquals(2, userGroupPayload.get("count"));

    assertTrue(userGroupPayload.get("admin") instanceof Map);
    Map userPayload = (Map) userGroupPayload.get("admin");
    assertTrue(userPayload.containsKey("id"));
    assertTrue(userPayload.containsKey("full_name"));

    assertEquals(42, userPayload.get("id"));
    assertEquals("Douglas Adams", userPayload.get("full_name"));
  }

  @Test
  public void testArraySchema() {
    IntegerSchema itemSchema = new IntegerSchema.Builder()
      .addMinimum(-100)
      .addMaximum(100)
      .build();

    ArraySchema array = new ArraySchema.Builder()
      .addItem(itemSchema)
      .addMinItems(5)
      .addMaxItems(1000)
      .build();

    assertEquals(1, array.getItems().size());
    assertEquals(5, array.getMinItems().get().longValue());
    assertEquals(1000, array.getMaxItems().get().longValue());
    assertEquals(itemSchema, array.getItems().get(0));
  }

  @Test
  public void testArraySchemaMultipleItems() {
    IntegerSchema integerSchema = new IntegerSchema.Builder().build();
    NumberSchema numberSchema = new NumberSchema.Builder().build();
    DataSchema stringSchema = new StringSchema.Builder().build();
    DataSchema booleanSchema = new BooleanSchema.Builder().build();
    DataSchema nullSchema = new NullSchema.Builder().build();

    ArraySchema array = new ArraySchema.Builder()
      .addItem(integerSchema)
      .addItem(numberSchema)
      .addItem(stringSchema)
      .addItem(booleanSchema)
      .addItem(nullSchema)
      .build();

    assertEquals(5, array.getItems().size());
    assertEquals(integerSchema, array.getItems().get(0));
    assertEquals(numberSchema, array.getItems().get(1));
    assertEquals(stringSchema, array.getItems().get(2));
    assertEquals(booleanSchema, array.getItems().get(3));
    assertEquals(nullSchema, array.getItems().get(4));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testArraySchemaNegativeMinItem() {
    new ArraySchema.Builder()
      .addItem(new IntegerSchema.Builder().build())
      .addMinItems(-10)
      .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testArraySchemaNegativeMaxItem() {
    new ArraySchema.Builder()
      .addItem(new IntegerSchema.Builder().build())
      .addMaxItems(-10)
      .build();
  }

  @Test
  public void testSchemaArrayOfObjects() {
    ArraySchema userArray = new ArraySchema.Builder()
      .addItem(userSchema)
      .build();

    ObjectSchema userGroup = new ObjectSchema.Builder()
      .addProperty("count", new IntegerSchema.Builder()
        .addMinimum(0)
        .addMaximum(100)
        .build())
      .addProperty("users", userArray)
      .build();

    assertEquals(2, userGroup.getProperties().size());
    assertEquals(DataSchema.INTEGER, userGroup.getProperty("count").get().getDatatype());
    assertEquals(DataSchema.ARRAY, userGroup.getProperty("users").get().getDatatype());
    assertEquals(userSchema, ((ArraySchema) userGroup.getProperty("users").get()).getItems().get(0));
  }

  @Test
  public void testContentMediaType() {
    IntegerSchema integerSchema = new IntegerSchema.Builder()
      .setContentMediaType("application/json").build();
    NumberSchema numberSchema = new NumberSchema.Builder()
      .setContentMediaType("application/json").build();
    StringSchema stringSchema = new StringSchema.Builder()
      .setContentMediaType("application/json").build();
    ArraySchema arraySchema = new ArraySchema.Builder()
      .setContentMediaType("application/json").build();
    ObjectSchema objectSchema = new ObjectSchema.Builder()
      .setContentMediaType("application/json").build();
    BooleanSchema booleanSchema = new BooleanSchema.Builder()
      .setContentMediaType("application/json").build();

    assertTrue(integerSchema.getContentMediaType().isPresent());
    assertTrue(numberSchema.getContentMediaType().isPresent());
    assertTrue(stringSchema.getContentMediaType().isPresent());
    assertTrue(arraySchema.getContentMediaType().isPresent());
    assertTrue(objectSchema.getContentMediaType().isPresent());
    assertTrue(booleanSchema.getContentMediaType().isPresent());

    assertEquals("application/json", integerSchema.getContentMediaType().get());
    assertEquals("application/json", numberSchema.getContentMediaType().get());
    assertEquals("application/json", stringSchema.getContentMediaType().get());
    assertEquals("application/json", arraySchema.getContentMediaType().get());
    assertEquals("application/json", objectSchema.getContentMediaType().get());
    assertEquals("application/json", booleanSchema.getContentMediaType().get());
  }

  @Test
  public void testOneOf() {
    ObjectSchema objectSchema0 = new ObjectSchema.Builder()
      .addSemanticType("sem0")
      .setContentMediaType("application/0+json")
      .build();

    ObjectSchema objectSchema1 = new ObjectSchema.Builder()
      .addSemanticType("sem1")
      .setContentMediaType("application/1+json")
      .build();

    ObjectSchema objectSchema = new ObjectSchema.Builder()
      .oneOf(objectSchema0, objectSchema1).build();

    List<DataSchema> dataSchemas = objectSchema.getValidSchemas();
    assertEquals(dataSchemas.size(), 2);
    assertEquals(dataSchemas.get(0), objectSchema0);
    assertEquals(dataSchemas.get(1), objectSchema1);

    List<DataSchema> dataSchemasBySemanticType = objectSchema.getValidSchemasBySemanticType("sem0");
    assertEquals(1, dataSchemasBySemanticType.size());
    assertEquals(objectSchema0, dataSchemasBySemanticType.get(0));

    List<DataSchema> dataSchemasByContentMediaType =
      objectSchema.getValidSchemasByContentMediaType("application/1+json");
    assertEquals(1, dataSchemasByContentMediaType.size());
    assertEquals(objectSchema1, dataSchemasByContentMediaType.get(0));

  }

  @Test
  public void testOneOfInvalidSubSchema() {
    ObjectSchema objectSchema = new ObjectSchema.Builder().build();

    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      new StringSchema.Builder().oneOf(objectSchema).build();
    });
    String expectedMessage = "Schema cannot be validated against subschema of type object";
    String actualMessage = exception.getMessage();
    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void testOneOfIntegerSubSchema() {
    IntegerSchema integerSchema = new IntegerSchema.Builder().build();
    NumberSchema numberSchema = new NumberSchema.Builder().oneOf(integerSchema).build();

    List<DataSchema> dataSchemas = numberSchema.getValidSchemas();
    assertEquals(dataSchemas.size(), 1);
    assertEquals(dataSchemas.get(0), integerSchema);
  }

  @Test
  public void testGetOneOfBySemanticType() {
    ObjectSchema objectSchema0 = new ObjectSchema.Builder()
      .addSemanticType("sem0").build();

    ObjectSchema objectSchema1 = new ObjectSchema.Builder()
      .addSemanticType("sem1").build();

    ObjectSchema objectSchema = new ObjectSchema.Builder()
      .oneOf(objectSchema0, objectSchema1).build();

    List<DataSchema> dataSchemas = objectSchema.getValidSchemas();
    assertEquals(dataSchemas.size(), 2);
    assertEquals(dataSchemas.get(0), objectSchema0);
    assertEquals(dataSchemas.get(1), objectSchema1);
  }

  @Test
  public void testObjectSchemaPayloadUknownProperties() {
    ObjectSchema objectSchema = new ObjectSchema.Builder()
      .addProperty("prop", new IntegerSchema.Builder().build())
      .build();

    Gson gson = new Gson();
    String invalidJsonStr = "{\"unknown-prop\": 1}";
    JsonElement invalidJsonObject = gson.fromJson(invalidJsonStr, JsonElement.class);
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      objectSchema.parseJson(invalidJsonObject);
    });
    String expectedMessage = "The payload contains unknown properties: [unknown-prop]";
    String actualMessage = exception.getMessage();
    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void testSuperDataSchema() {
    StringSchema stringSchema0 = new StringSchema.Builder()
      .addSemanticType("sem0").build();

    ObjectSchema objectSchema1 = new ObjectSchema.Builder()
      .addProperty("prop", new IntegerSchema.Builder().build())
      .addSemanticType("sem1").build();

    DataSchema superSchema = DataSchema.getSuperSchema(Arrays.asList(stringSchema0, objectSchema1));

    List<DataSchema> dataSchemas = superSchema.getValidSchemas();
    assertEquals(dataSchemas.size(), 2);
    assertEquals(dataSchemas.get(0), stringSchema0);
    assertEquals(dataSchemas.get(1), objectSchema1);

    JsonPrimitive jsonPrimitive = new JsonPrimitive("string");
    Object parsedJsonPrimitiveSub = stringSchema0.parseJson(jsonPrimitive);
    Object parsedJsonPrimitiveSuper = superSchema.parseJson(jsonPrimitive);

    assertTrue(parsedJsonPrimitiveSub instanceof String);
    assertTrue(parsedJsonPrimitiveSuper instanceof String);
    assertEquals(parsedJsonPrimitiveSub, parsedJsonPrimitiveSuper);

    String jsonStr = "{\"prop\": 1}";
    Gson gson = new Gson();
    JsonElement jsonObject = gson.fromJson(jsonStr, JsonElement.class);
    Object parsedJsonObjectSub = objectSchema1.parseJson(jsonObject);
    Object parsedJsonObjectSuper = superSchema.parseJson(jsonObject);
    assertTrue(parsedJsonObjectSub instanceof Map);
    assertTrue(parsedJsonObjectSuper instanceof Map);
    assertEquals(parsedJsonObjectSub, parsedJsonObjectSuper);

    String invalidJsonStr = "{\"unknown-prop\": 1}";
    JsonElement invalidJsonObject = gson.fromJson(invalidJsonStr, JsonElement.class);
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      superSchema.parseJson(invalidJsonObject);
    });
    String expectedMessage = "JSON element is not valid against any of available subschemas";
    String actualMessage = exception.getMessage();
    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void testSuperDataSchemaWithNoValidSchemas() {
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      DataSchema.getSuperSchema(new ArrayList<>());
    });
    String expectedMessage = "No subschemas found";
    String actualMessage = exception.getMessage();
    assertTrue(actualMessage.contains(expectedMessage));
  }
}
