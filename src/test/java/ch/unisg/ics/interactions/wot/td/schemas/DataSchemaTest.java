package ch.unisg.ics.interactions.wot.td.schemas;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.Test;

public class DataSchemaTest {

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
    IntegerSchema integerSchema = (new IntegerSchema.Builder())
        .addMinimum(-100)
        .addMaximum(100)
        .build();
    assertEquals("integer", integerSchema.getDatatype());
    assertEquals(-100, integerSchema.getMinimum().get().longValue());
    assertEquals(100, integerSchema.getMaximum().get().longValue());
  }
  
  @Test
  public void testIntegerSchemaNoLimits() {
    IntegerSchema integerSchema = (new IntegerSchema.Builder()).build();
    
    assertEquals("integer", integerSchema.getDatatype());
    assertTrue(integerSchema.getMaximum().isEmpty());
    assertTrue(integerSchema.getMinimum().isEmpty());
  }
  
  @Test
  public void testNumberSchema() {
    NumberSchema numberSchema = (new NumberSchema.Builder())
        .addMinimum(-100.05)
        .addMaximum(100.05)
        .build();
    assertEquals("number", numberSchema.getDatatype());
    assertEquals(-100.05, numberSchema.getMinimum().get().doubleValue(), 0.01);
    assertEquals(100.05, numberSchema.getMaximum().get().doubleValue(), 0.01);
  }
  
  @Test
  public void testNumberSchemaNoLimits() {
    NumberSchema numberSchema = (new NumberSchema.Builder()).build();
    
    assertEquals("number", numberSchema.getDatatype());
    assertTrue(numberSchema.getMaximum().isEmpty());
    assertTrue(numberSchema.getMinimum().isEmpty());
  }
  
  @Test
  public void testObjectSchema() {
    ObjectSchema schema = (new ObjectSchema.Builder())
        .addProperty("id", (new IntegerSchema.Builder()).build())
        .addProperty("active", new BooleanSchema.Builder().build())
        .addProperty("first_name", new StringSchema.Builder().build())
        .addProperty("last_name", new StringSchema.Builder().build())
        .addProperty("age", (new IntegerSchema.Builder()).addMaximum(18).addMaximum(150).build())
        .addProperty("height", (new NumberSchema.Builder()).addMaximum(299.99).addMaximum(100.0).build())
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
  
  @Test(expected = IllegalArgumentException.class)
  public void testObjectSchemaMissingRequired() {
    (new ObjectSchema.Builder())
        .addProperty("full_name", new StringSchema.Builder().build())
        .addRequiredProperties("id")
        .build();
  }
  
  @Test
  public void testSchemaNestedObjects() {
    ObjectSchema userSchema = (new ObjectSchema.Builder())
        .addProperty("id", (new IntegerSchema.Builder()).build())
        .addProperty("full_name", new StringSchema.Builder().build())
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
    DataSchema stringSchema = new StringSchema.Builder().build();
    DataSchema booleanSchema = new BooleanSchema.Builder().build();
    DataSchema nullSchema = new NullSchema.Builder().build();
    
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
        .addProperty("full_name", new StringSchema.Builder().build())
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
    assertEquals(DataSchema.INTEGER, userGroup.getProperty("count").get().getDatatype());
    assertEquals(DataSchema.ARRAY, userGroup.getProperty("users").get().getDatatype());
    assertEquals(userSchema, ((ArraySchema) userGroup.getProperty("users").get()).getItems().get(0));
  }
}
