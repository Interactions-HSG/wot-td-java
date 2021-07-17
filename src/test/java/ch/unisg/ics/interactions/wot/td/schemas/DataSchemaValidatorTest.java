package ch.unisg.ics.interactions.wot.td.schemas;

import org.apache.commons.collections4.map.HashedMap;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DataSchemaValidatorTest {

  @Test
  public void testValidateStringSchema() {

    DataSchema stringSchema = new StringSchema.Builder().build();

    assertTrue(DataSchemaValidator.validate(stringSchema, "1"));

    assertFalse(DataSchemaValidator.validate(stringSchema, 1));
    assertFalse(DataSchemaValidator.validate(stringSchema, 1.5));
    assertFalse(DataSchemaValidator.validate(stringSchema, true));
    assertFalse(DataSchemaValidator.validate(stringSchema, null));

    List<String> arrayValue = new ArrayList<>();
    arrayValue.add("1");
    assertFalse(DataSchemaValidator.validate(stringSchema, arrayValue));

    Map<String, Object> objectValue = new HashedMap<>();
    objectValue.put("name", "Rimuru");
    assertFalse(DataSchemaValidator.validate(stringSchema, objectValue));
  }

  @Test
  public void testValidateNumberSchema() {

    DataSchema numberSchema = new NumberSchema.Builder().build();

    assertTrue(DataSchemaValidator.validate(numberSchema, 1));
    assertTrue(DataSchemaValidator.validate(numberSchema, (float) 1.5));
    assertTrue(DataSchemaValidator.validate(numberSchema, (long) 1.5));

    assertFalse(DataSchemaValidator.validate(numberSchema, "1"));
    assertFalse(DataSchemaValidator.validate(numberSchema, true));
    assertFalse(DataSchemaValidator.validate(numberSchema, null));

    List<String> arrayValue = new ArrayList<>();
    arrayValue.add("1");
    assertFalse(DataSchemaValidator.validate(numberSchema, arrayValue));

    Map<String, Object> objectValue = new HashedMap<>();
    objectValue.put("name", "Rimuru");
    assertFalse(DataSchemaValidator.validate(numberSchema, objectValue));
  }

  @Test
  public void testValidateIntegerSchema() {

    DataSchema integerSchema = new IntegerSchema.Builder().build();

    assertTrue(DataSchemaValidator.validate(integerSchema, 1));

    assertFalse(DataSchemaValidator.validate(integerSchema, (float) 1.5));
    assertFalse(DataSchemaValidator.validate(integerSchema, (long) 1.5));
    assertFalse(DataSchemaValidator.validate(integerSchema, "1"));
    assertFalse(DataSchemaValidator.validate(integerSchema, true));
    assertFalse(DataSchemaValidator.validate(integerSchema, null));

    List<String> arrayValue = new ArrayList<>();
    arrayValue.add("1");
    assertFalse(DataSchemaValidator.validate(integerSchema, arrayValue));

    Map<String, Object> objectValue = new HashedMap<>();
    objectValue.put("name", "Rimuru");
    assertFalse(DataSchemaValidator.validate(integerSchema, objectValue));
  }

  @Test
  public void testValidateBooleanSchema() {

    DataSchema booleanSchema = new BooleanSchema.Builder().build();

    assertTrue(DataSchemaValidator.validate(booleanSchema, true));

    assertFalse(DataSchemaValidator.validate(booleanSchema, 1));
    assertFalse(DataSchemaValidator.validate(booleanSchema, 1.5));
    assertFalse(DataSchemaValidator.validate(booleanSchema, "1"));
    assertFalse(DataSchemaValidator.validate(booleanSchema, null));

    List<String> arrayValue = new ArrayList<>();
    arrayValue.add("1");
    assertFalse(DataSchemaValidator.validate(booleanSchema, arrayValue));

    Map<String, Object> objectValue = new HashedMap<>();
    objectValue.put("name", "Rimuru");
    assertFalse(DataSchemaValidator.validate(booleanSchema, objectValue));
  }

  @Test
  public void testValidateArraySchemaSize() {

    DataSchema arraySchema = new ArraySchema.Builder()
      .addMinItems(2)
      .addMaxItems(2)
      .build();

    List<String> arrayValueEqualItems = new ArrayList<>(Arrays.asList("1","2"));
    assertTrue(DataSchemaValidator.validate(arraySchema, arrayValueEqualItems));

    List<String> arrayValueLessItems = new ArrayList<>(Arrays.asList("1"));
    assertFalse(DataSchemaValidator.validate(arraySchema, arrayValueLessItems));

    List<String> arrayValueMoreItems = new ArrayList<>(Arrays.asList("1", "2", "3"));
    assertFalse(DataSchemaValidator.validate(arraySchema, arrayValueMoreItems));
  }

  @Test
  public void testValidateArraySchema() {

    DataSchema arraySchema = new ArraySchema.Builder().build();
    List<String> arrayValue = new ArrayList<>();
    arrayValue.add("1");
    assertTrue(DataSchemaValidator.validate(arraySchema, arrayValue));

    assertFalse(DataSchemaValidator.validate(arraySchema, 1));
    assertFalse(DataSchemaValidator.validate(arraySchema, 1.5));
    assertFalse(DataSchemaValidator.validate(arraySchema, "1"));
    assertFalse(DataSchemaValidator.validate(arraySchema, true));
    assertFalse(DataSchemaValidator.validate(arraySchema, null));

    Map<String, Object> objectValue = new HashedMap<>();
    objectValue.put("name", "Rimuru");
    assertFalse(DataSchemaValidator.validate(arraySchema, objectValue));
  }

  @Test
  public void testValidateNullSchema() {

    DataSchema nullSchema = new NullSchema.Builder().build();

    assertTrue(DataSchemaValidator.validate(nullSchema, null));

    assertFalse(DataSchemaValidator.validate(nullSchema, "1"));
    assertFalse(DataSchemaValidator.validate(nullSchema, 1));
    assertFalse(DataSchemaValidator.validate(nullSchema, 1.5));
    assertFalse(DataSchemaValidator.validate(nullSchema, true));

    List<String> arrayValue = new ArrayList<>();
    arrayValue.add("Rimuru");
    assertFalse(DataSchemaValidator.validate(nullSchema, arrayValue));

    Map<String, Object> objectValue = new HashedMap<>();
    objectValue.put("name", "Rimuru");
    assertFalse(DataSchemaValidator.validate(nullSchema, objectValue));
  }
}
