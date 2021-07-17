package ch.unisg.ics.interactions.wot.td.schemas;

import org.apache.commons.collections4.map.HashedMap;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DataSchemaValidatorTest {

  @Test
  public void testValidateStringSchema() {

    DataSchema stringSchema = new StringSchema.Builder().build();

    assertTrue(DataSchemaValidator.validate(stringSchema,"1"));

    assertFalse(DataSchemaValidator.validate(stringSchema,1));
    assertFalse(DataSchemaValidator.validate(stringSchema,1.5));
    assertFalse(DataSchemaValidator.validate(stringSchema,true));
    assertFalse(DataSchemaValidator.validate(stringSchema,null));

    List<String> arrayValue = new ArrayList<>();
    arrayValue.add("1");
    assertFalse(DataSchemaValidator.validate(stringSchema,arrayValue));

    Map<String,Object> objectValue = new HashedMap<>();
    objectValue.put("name","Rimuru");
    assertFalse(DataSchemaValidator.validate(stringSchema,objectValue));
  }

  @Test
  public void testValidateNullSchema() {

    DataSchema nullSchema = new NullSchema.Builder().build();

    assertTrue(DataSchemaValidator.validate(nullSchema,null));

    assertFalse(DataSchemaValidator.validate(nullSchema,"1"));
    assertFalse(DataSchemaValidator.validate(nullSchema,1));
    assertFalse(DataSchemaValidator.validate(nullSchema,1.5));
    assertFalse(DataSchemaValidator.validate(nullSchema,true));

    List<String> arrayValue = new ArrayList<>();
    arrayValue.add("Rimuru");
    assertFalse(DataSchemaValidator.validate(nullSchema,arrayValue));

    Map<String,Object> objectValue = new HashedMap<>();
    objectValue.put("name","Rimuru");
    assertFalse(DataSchemaValidator.validate(nullSchema,objectValue));
  }
}
