package ch.unisg.ics.interactions.wot.td.schema;

public class DataSchema {
  public static final String OBJECT = "object";
  public static final String ARRAY = "array";
  
  public static final String STRING = "string";
  public static final String NUMBER = "number";
  public static final String INTEGER = "integer";
  public static final String BOOLEAN = "boolean";
  public static final String NULL = "null";
  
  private String type;
  
  public DataSchema(String type) {
    this.type = type;
  }
  
  public String getType() {
    return type;
  }
}
