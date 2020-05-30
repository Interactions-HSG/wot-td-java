package ch.unisg.ics.interactions.wot.td.schema;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class DataSchemaTest {

  @Test
  public void testBasicSchemas() {
    DataSchema stringSchema = new DataSchema(DataSchema.SCHEMA_STRING_TYPE);
    assertEquals("string", stringSchema.getType());
    
    DataSchema booleanSchema = new DataSchema(DataSchema.SCHEMA_BOOLEAN_TYPE);
    assertEquals("boolean", booleanSchema.getType());
    
    DataSchema nullSchema = new DataSchema(DataSchema.SCHEMA_NULL_TYPE);
    assertEquals("null", nullSchema.getType());
  }
  
  @Test
  public void testIntegerSchema() {
    IntegerSchema integerSchema = (new IntegerSchema.Builder())
        .addMinimum(-100)
        .addMaximum(100)
        .build();
    assertEquals("integer", integerSchema.getType());
    assertEquals(-100, integerSchema.getMinimum().get().longValue());
    assertEquals(100, integerSchema.getMaximum().get().longValue());
  }
  
  @Test
  public void testIntegerSchemaNoLimits() {
    IntegerSchema integerSchema = (new IntegerSchema.Builder()).build();
    
    assertEquals("integer", integerSchema.getType());
    assertTrue(integerSchema.getMaximum().isEmpty());
    assertTrue(integerSchema.getMinimum().isEmpty());
  }
  
  @Test
  public void testNumberSchema() {
    NumberSchema numberSchema = (new NumberSchema.Builder())
        .addMinimum(-100.05)
        .addMaximum(100.05)
        .build();
    assertEquals("number", numberSchema.getType());
    assertEquals(-100.05, numberSchema.getMinimum().get().doubleValue(), 0.01);
    assertEquals(100.05, numberSchema.getMaximum().get().doubleValue(), 0.01);
  }
  
  @Test
  public void testNumberSchemaNoLimits() {
    NumberSchema numberSchema = (new NumberSchema.Builder()).build();
    
    assertEquals("number", numberSchema.getType());
    assertTrue(numberSchema.getMaximum().isEmpty());
    assertTrue(numberSchema.getMinimum().isEmpty());
  }
  
  @Test
  public void testObjectSchema() {
    ObjectSchema schema = (new ObjectSchema.Builder())
        .addProperty("id", (new IntegerSchema.Builder()).build())
        .addProperty("active", new DataSchema(DataSchema.SCHEMA_BOOLEAN_TYPE))
        .addProperty("first_name", new DataSchema(DataSchema.SCHEMA_STRING_TYPE))
        .addProperty("last_name", new DataSchema(DataSchema.SCHEMA_STRING_TYPE))
        .addProperty("age", (new IntegerSchema.Builder()).addMaximum(18).addMaximum(150).build())
        .addProperty("height", (new NumberSchema.Builder()).addMaximum(299.99).addMaximum(100.0).build())
        .addProperty("connections", new DataSchema(DataSchema.SCHEMA_NULL_TYPE))
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
    
    assertEquals(DataSchema.SCHEMA_INTEGER_TYPE, schema.getProperty("id").get().getType());
    assertEquals(DataSchema.SCHEMA_BOOLEAN_TYPE, schema.getProperty("active").get().getType());
    assertEquals(DataSchema.SCHEMA_STRING_TYPE, schema.getProperty("first_name").get().getType());
    assertEquals(DataSchema.SCHEMA_STRING_TYPE, schema.getProperty("last_name").get().getType());
    assertEquals(DataSchema.SCHEMA_INTEGER_TYPE, schema.getProperty("age").get().getType());
    assertEquals(DataSchema.SCHEMA_NUMBER_TYPE, schema.getProperty("height").get().getType());
    assertEquals(DataSchema.SCHEMA_NULL_TYPE, schema.getProperty("connections").get().getType());
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testObjectSchemaMissingRequired() {
    (new ObjectSchema.Builder())
        .addProperty("full_name", new DataSchema(DataSchema.SCHEMA_STRING_TYPE))
        .addRequiredProperties("id")
        .build();
  }
  
  @Test
  public void testSchemaNestedObjects() {
    ObjectSchema userSchema = (new ObjectSchema.Builder())
        .addProperty("id", (new IntegerSchema.Builder()).build())
        .addProperty("full_name", new DataSchema(DataSchema.SCHEMA_STRING_TYPE))
        .addRequiredProperties("id")
        .build();
    
    ObjectSchema userGroup = (new ObjectSchema.Builder())
        .addProperty("count", (new IntegerSchema.Builder()).addMinimum(0).addMaximum(100).build())
        .addProperty("admin", userSchema)
        .build();
    
    assertEquals(2, userGroup.getProperties().size());
    assertEquals(userSchema, userGroup.getProperty("admin").get());
  }
  
  @Test
  public void testArraySchema() {
    IntegerSchema itemSchema = (new IntegerSchema.Builder())
        .addMinimum(-100)
        .addMaximum(100)
        .build();
    
    ArraySchema array = (new ArraySchema.Builder())
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
    IntegerSchema integerSchema = (new IntegerSchema.Builder()).build();
    NumberSchema numberSchema = new NumberSchema.Builder().build();
    DataSchema stringSchema = new DataSchema(DataSchema.SCHEMA_STRING_TYPE);
    DataSchema booleanSchema = new DataSchema(DataSchema.SCHEMA_BOOLEAN_TYPE);
    DataSchema nullSchema = new DataSchema(DataSchema.SCHEMA_NULL_TYPE);
    
    ArraySchema array = (new ArraySchema.Builder())
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
    (new ArraySchema.Builder())
        .addItem((new IntegerSchema.Builder()).build())
        .addMinItems(-10)
        .build();
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testArraySchemaNegativeMaxItem() {
    (new ArraySchema.Builder())
        .addItem((new IntegerSchema.Builder()).build())
        .addMaxItems(-10)
        .build();
  }
  
  @Test
  public void testSchemaArrayOfObjects() {
    ObjectSchema userSchema = (new ObjectSchema.Builder())
        .addProperty("id", (new IntegerSchema.Builder()).build())
        .addProperty("full_name", new DataSchema(DataSchema.SCHEMA_STRING_TYPE))
        .addRequiredProperties("id")
        .build();
    
    ArraySchema userArray = (new ArraySchema.Builder())
        .addItem(userSchema)
        .build();
    
    ObjectSchema userGroup = (new ObjectSchema.Builder())
        .addProperty("count", (new IntegerSchema.Builder()).addMinimum(0).addMaximum(100).build())
        .addProperty("users", userArray)
        .build();
    
    assertEquals(2, userGroup.getProperties().size());
    assertEquals(DataSchema.SCHEMA_INTEGER_TYPE, userGroup.getProperty("count").get().getType());
    assertEquals(DataSchema.SCHEMA_ARRAY_TYPE, userGroup.getProperty("users").get().getType());
    assertEquals(userSchema, ((ArraySchema) userGroup.getProperty("users").get()).getItems().get(0));
  }
}
