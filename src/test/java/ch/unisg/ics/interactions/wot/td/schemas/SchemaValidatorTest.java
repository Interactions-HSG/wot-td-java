package ch.unisg.ics.interactions.wot.td.schemas;

import org.apache.commons.collections4.map.HashedMap;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static ch.unisg.ics.interactions.wot.td.schemas.SchemaValidator.validate;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SchemaValidatorTest {

  @Test
  public void testValidateStringSchema() {
    DataSchema stringSchema = new StringSchema.Builder().build();

    assertTrue(validate(stringSchema, "1"));

    assertFalse(validate(stringSchema, 1));
    assertFalse(validate(stringSchema, 1.5));
    assertFalse(validate(stringSchema, true));
    assertFalse(validate(stringSchema, null));

    List<String> arrayValue = new ArrayList<>();
    arrayValue.add("1");
    assertFalse(validate(stringSchema, arrayValue));

    Map<String, Object> objectValue = new HashedMap<>();
    objectValue.put("firstName", "Rimuru");
    assertFalse(validate(stringSchema, objectValue));
  }

  @Test
  public void testValidateNumberSchema() {
    DataSchema numberSchema = new NumberSchema.Builder().build();

    assertTrue(validate(numberSchema, 1));
    assertTrue(validate(numberSchema, (float) 1.5));
    assertTrue(validate(numberSchema, (long) 1.5));

    assertFalse(validate(numberSchema, "1"));
    assertFalse(validate(numberSchema, true));
    assertFalse(validate(numberSchema, null));

    List<String> arrayValue = new ArrayList<>();
    arrayValue.add("1");
    assertFalse(validate(numberSchema, arrayValue));

    Map<String, Object> objectValue = new HashedMap<>();
    objectValue.put("firstName", "Rimuru");
    assertFalse(validate(numberSchema, objectValue));
  }

  @Test
  public void testValidateIntegerSchema() {
    DataSchema integerSchema = new IntegerSchema.Builder().build();

    assertTrue(validate(integerSchema, 1));

    assertFalse(validate(integerSchema, (float) 1.5));
    assertFalse(validate(integerSchema, (long) 1.5));
    assertFalse(validate(integerSchema, "1"));
    assertFalse(validate(integerSchema, true));
    assertFalse(validate(integerSchema, null));

    List<String> arrayValue = new ArrayList<>();
    arrayValue.add("1");
    assertFalse(validate(integerSchema, arrayValue));

    Map<String, Object> objectValue = new HashedMap<>();
    objectValue.put("firstName", "Rimuru");
    assertFalse(validate(integerSchema, objectValue));
  }

  @Test
  public void testValidateBooleanSchema() {
    DataSchema booleanSchema = new BooleanSchema.Builder().build();

    assertTrue(validate(booleanSchema, true));

    assertFalse(validate(booleanSchema, 1));
    assertFalse(validate(booleanSchema, 1.5));
    assertFalse(validate(booleanSchema, "1"));
    assertFalse(validate(booleanSchema, null));

    List<String> arrayValue = new ArrayList<>();
    arrayValue.add("1");
    assertFalse(validate(booleanSchema, arrayValue));

    Map<String, Object> objectValue = new HashedMap<>();
    objectValue.put("firstName", "Rimuru");
    assertFalse(validate(booleanSchema, objectValue));
  }

  @Test
  public void testValidateArraySchema() {
    DataSchema arraySchema = new ArraySchema.Builder().build();
    List<String> arrayValue = new ArrayList<>();
    arrayValue.add("1");
    assertTrue(validate(arraySchema, arrayValue));

    assertFalse(validate(arraySchema, 1));
    assertFalse(validate(arraySchema, 1.5));
    assertFalse(validate(arraySchema, "1"));
    assertFalse(validate(arraySchema, true));
    assertFalse(validate(arraySchema, null));

    Map<String, Object> objectValue = new HashedMap<>();
    objectValue.put("firstName", "Rimuru");
    assertFalse(validate(arraySchema, objectValue));
  }

  @Test
  public void testValidateArraySchemaSize() {
    ArraySchema arraySchema = new ArraySchema.Builder()
      .addMinItems(2)
      .addMaxItems(2)
      .build();

    List<String> arrayValueEqualItems = new ArrayList<>(Arrays.asList("1", "2"));
    assertTrue(validate(arraySchema, arrayValueEqualItems));

    List<String> arrayValueLessItems = new ArrayList<>(Arrays.asList("1"));
    assertFalse(validate(arraySchema, arrayValueLessItems));

    List<String> arrayValueMoreItems = new ArrayList<>(Arrays.asList("1", "2", "3"));
    assertFalse(validate(arraySchema, arrayValueMoreItems));
  }

  @Test
  public void testValidateArraySchemaOneItem() {
    ArraySchema arraySchema = new ArraySchema.Builder()
      .addItem(new StringSchema.Builder().build())
      .build();

    assertTrue(validate(arraySchema, Arrays.asList("1", "2")));

    assertFalse(validate(arraySchema, Arrays.asList(1, 2)));
    assertFalse(validate(arraySchema, Arrays.asList(1.5, 2.5)));
    assertFalse(validate(arraySchema, Arrays.asList(true, false)));
    assertFalse(validate(arraySchema, Arrays.asList("1", null)));
    assertFalse(validate(arraySchema, Arrays.asList("1", 2)));
  }

  @Test
  public void testValidateArraySchemaPrimitiveItems() {
    ArraySchema arraySchema = new ArraySchema.Builder()
      .addItem(new StringSchema.Builder().build())
      .addItem(new NumberSchema.Builder().build())
      .addItem(new IntegerSchema.Builder().build())
      .addItem(new BooleanSchema.Builder().build())
      .addItem(new NullSchema.Builder().build())
      .build();

    assertTrue(validate(arraySchema, Arrays.asList("1", 1, 1, false, null)));

    assertFalse(validate(arraySchema, Arrays.asList("1", 1, 1, false)));
    assertFalse(validate(arraySchema, Arrays.asList("1", 1, 1, false, null, null)));

    assertFalse(validate(arraySchema, Arrays.asList("1", "1", "1", "false", "null")));
  }

  @Test
  public void testValidateArraySchemaNestedArray() {
    ArraySchema arraySchema = new ArraySchema.Builder()
      .addItem(new ArraySchema.Builder()
        .addItem(new StringSchema.Builder().build())
        .build())
      .addItem(new ArraySchema.Builder()
        .addItem(new NumberSchema.Builder().build())
        .build())
      .build();

    List<Object> stringValues = Arrays.asList("1", "2");
    List<Object> numberValues = Arrays.asList(2, 2);

    List<Object> values = Arrays.asList(stringValues, numberValues);
    assertTrue(validate(arraySchema, values));

    List<Object> invertedValues = Arrays.asList(numberValues, stringValues);
    assertFalse(validate(arraySchema, invertedValues));

    assertFalse(validate(arraySchema, stringValues));
    assertFalse(validate(arraySchema, numberValues));
  }

  @Test
  public void testValidateArraySchemaNestedObject() {
    ArraySchema arraySchema = new ArraySchema.Builder()
      .addItem(new ObjectSchema.Builder()
        .addProperty("height", new IntegerSchema.Builder().build())
        .addProperty("age", new IntegerSchema.Builder().build())
        .build())
      .build();

    HashedMap<String, Integer> nestedObjectValue1 = new HashedMap<>();
    HashedMap<String, Integer> nestedObjectValue2 = new HashedMap<>();

    nestedObjectValue1.put("height", 20);
    nestedObjectValue1.put("age", 2);
    nestedObjectValue2.put("height", 120);
    nestedObjectValue2.put("age", 39);

    List<Object> objectValues = Arrays.asList(nestedObjectValue1, nestedObjectValue2);

    assertTrue(validate(arraySchema, objectValues));
  }

  @Test
  public void testValidateObjectSchema() {
    DataSchema objectSchema = new ObjectSchema.Builder().build();

    Map<Object, Object> objectValue = new HashedMap<>();
    objectValue.put("firstName", "Rimuru");
    objectValue.put("lastName", "Tempest");
    assertTrue(validate(objectSchema, objectValue));

    Map<Object, Object> objectValueInvalidName = new HashedMap<>();
    objectValueInvalidName.put("firstName", "Rimuru");
    objectValueInvalidName.put(1, "Tempest");
    assertFalse(validate(objectSchema, objectValueInvalidName));

    assertFalse(validate(objectSchema, "1"));
    assertFalse(validate(objectSchema, 1));
    assertFalse(validate(objectSchema, 1.5));
    assertFalse(validate(objectSchema, true));
    assertFalse(validate(objectSchema, null));

    List<String> arrayValue = new ArrayList<>();
    arrayValue.add("1");
    assertFalse(validate(objectSchema, arrayValue));
  }

  @Test
  public void testValidateObjectSchemaPrimitiveProperties() {
    ObjectSchema objectSchema = new ObjectSchema.Builder()
      .addProperty("stringName", new StringSchema.Builder().build())
      .addProperty("numberName", new NumberSchema.Builder().build())
      .addProperty("integerName", new IntegerSchema.Builder().build())
      .addProperty("booleanName", new BooleanSchema.Builder().build())
      .addProperty("nullName", new NullSchema.Builder().build())
      .build();

    HashedMap<String, Object> objectValue = new HashedMap<>();
    objectValue.put("stringName", "Rimuru");
    objectValue.put("numberName", 1);
    objectValue.put("integerName", 1);
    objectValue.put("booleanName", false);
    objectValue.put("nullName", null);

    assertTrue(validate(objectSchema, objectValue));

    objectValue.put("stringName", 1);
    assertFalse(validate(objectSchema, objectValue));
  }

  @Test
  public void testValidateObjectSchemaPropertiesSize() {
    ObjectSchema objectSchemaNoProperties = new ObjectSchema.Builder().build();
    ObjectSchema objectSchemaTwoProperties = new ObjectSchema.Builder()
      .addProperty("firstName", new StringSchema.Builder().build())
      .addProperty("lastName", new StringSchema.Builder().build())
      .build();

    HashedMap<String, Object> objectValue = new HashedMap<>();
    objectValue.put("firstName", "Rimuru");

    assertTrue(validate(objectSchemaNoProperties, objectValue));
    assertTrue(validate(objectSchemaTwoProperties, objectValue));

    objectValue.put("lastName", "Tempest");
    assertTrue(validate(objectSchemaNoProperties, objectValue));
    assertTrue(validate(objectSchemaTwoProperties, objectValue));

    objectValue.put("species", "Demon Slime");
    assertTrue(validate(objectSchemaNoProperties, objectValue));
    assertFalse(validate(objectSchemaTwoProperties, objectValue));
  }

  @Test
  public void testValidateObjectSchemaRequiredProperties() {
    ObjectSchema objectSchema = new ObjectSchema.Builder()
      .addProperty("requiredName", new StringSchema.Builder().build())
      .addProperty("optionalName", new StringSchema.Builder().build())
      .addRequiredProperties("requiredName")
      .build();

    HashedMap<String, Object> objectValue = new HashedMap<>();

    objectValue.put("optionalName", "optionalValue");
    assertFalse(validate(objectSchema, objectValue));

    objectValue.put("requiredName", "requiredValue");
    assertTrue(validate(objectSchema, objectValue));
  }

  @Test
  public void testValidateObjectSchemaNestedObject() {
    ObjectSchema objectSchema = new ObjectSchema.Builder()
      .addProperty("slimeForm", new ObjectSchema.Builder()
        .addProperty("height", new IntegerSchema.Builder().build())
        .addProperty("age", new IntegerSchema.Builder().build())
        .build())
      .addProperty("humanForm", new ObjectSchema.Builder()
        .addProperty("height", new IntegerSchema.Builder().build())
        .addProperty("age", new IntegerSchema.Builder().build())
        .build())
      .build();

    HashedMap<String, Object> objectValue = new HashedMap<>();
    HashedMap<String, Integer> nestedObjectValue1 = new HashedMap<>();
    HashedMap<String, Integer> nestedObjectValue2 = new HashedMap<>();

    nestedObjectValue1.put("height", 20);
    nestedObjectValue1.put("age", 2);
    nestedObjectValue2.put("height", 120);
    nestedObjectValue2.put("age", 39);
    objectValue.put("slimeForm", nestedObjectValue1);
    objectValue.put("humanForm", nestedObjectValue2);

    assertTrue(validate(objectSchema, objectValue));
  }

  @Test
  public void testValidateObjectSchemaNestedArray() {
    ObjectSchema objectSchema = new ObjectSchema.Builder()
      .addProperty("forms", new ArraySchema.Builder()
        .addItem(new StringSchema.Builder().build())
        .addMinItems(2)
        .build())
      .addProperty("ages", new ArraySchema.Builder()
        .addItem(new IntegerSchema.Builder().build())
        .addItem(new IntegerSchema.Builder().build())
        .build())
      .build();

    HashedMap<String, Object> objectValue = new HashedMap<>();
    objectValue.put("forms", Arrays.asList("human"));
    objectValue.put("ages", Arrays.asList(2, 39));

    assertFalse(validate(objectSchema, objectValue));

    objectValue.put("forms", Arrays.asList("human", "slime"));
    assertTrue(validate(objectSchema, objectValue));
  }

  @Test
  public void testValidateNullSchema() {
    DataSchema nullSchema = new NullSchema.Builder().build();

    assertTrue(validate(nullSchema, null));

    assertFalse(validate(nullSchema, "1"));
    assertFalse(validate(nullSchema, 1));
    assertFalse(validate(nullSchema, 1.5));
    assertFalse(validate(nullSchema, true));

    List<String> arrayValue = new ArrayList<>();
    arrayValue.add("Rimuru");
    assertFalse(validate(nullSchema, arrayValue));

    Map<String, Object> objectValue = new HashedMap<>();
    objectValue.put("firstName", "Rimuru");
    assertFalse(validate(nullSchema, objectValue));
  }

  @Test
  public void testValidateObjectSchemaBySemanticTypesSize() {
    ObjectSchema objectSchemaNoProperties = new ObjectSchema.Builder().build();
    ObjectSchema objectSchemaTwoProperties = new ObjectSchema.Builder()
      .addProperty("firstName", new StringSchema.Builder()
        .addSemanticType("http://example.org#FirstName").build())
      .addProperty("lastName", new StringSchema.Builder()
        .addSemanticType("http://example.org#LastName").build())
      .build();

    HashedMap<String, Object> objectValue = new HashedMap<>();
    objectValue.put("http://example.org#FirstName", "Rimuru");

    assertTrue(validate(objectSchemaNoProperties, objectValue));
    assertTrue(validate(objectSchemaTwoProperties, objectValue));

    objectValue.put("http://example.org#LastName", "Tempest");
    assertTrue(validate(objectSchemaNoProperties, objectValue));
    assertTrue(validate(objectSchemaTwoProperties, objectValue));

    objectValue.put("http://example.org#Species", "Demon Slime");
    assertTrue(validate(objectSchemaNoProperties, objectValue));
    assertFalse(validate(objectSchemaTwoProperties, objectValue));
  }

  @Test
  public void testValidateObjectSchemaSemanticTypesRequiredProperties() {
    ObjectSchema objectSchema = new ObjectSchema.Builder()
      .addProperty("requiredName", new StringSchema.Builder()
        .addSemanticType("http://example.org#Required").build())
      .addProperty("optionalName", new StringSchema.Builder()
        .addSemanticType("http://example.org#Optional").build())
      .addRequiredProperties("requiredName")
      .build();

    HashedMap<String, Object> objectValue = new HashedMap<>();

    objectValue.put("http://example.org#Optional", "optionalValue");
    assertFalse(validate(objectSchema, objectValue));

    objectValue.put("http://example.org#Required", "requiredValue");
    assertTrue(validate(objectSchema, objectValue));
  }
}
