package ro.andreiciortea.wot.td.schema;

public class FieldValueBinding extends FieldBinding {
  private Object fieldValue;
  
  public FieldValueBinding(String name, Object value) {
    super(name);
    fieldValue = value;
  }
  
  public Object getValue() {
    return fieldValue;
  }
}
