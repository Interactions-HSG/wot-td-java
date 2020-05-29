package ch.unisg.ics.interactions.wot.td.schema;

public class DataSchema {
  public static final String SCHEMA_OBJECT_TYPE = "object";
  public static final String SCHEMA_ARRAY_TYPE = "array";
  
  public static final String SCHEMA_STRING_TYPE = "string";
  public static final String SCHEMA_NUMBER_TYPE = "number";
  public static final String SCHEMA_INTEGER_TYPE = "integer";
  public static final String SCHEMA_BOOLEAN_TYPE = "boolean";
  public static final String SCHEMA_NULL_TYPE = "null";
  
  private String type;
  
  public DataSchema(String type) {
    this.type = type;
  }
  
  public String getType() {
    return type;
  }
}
