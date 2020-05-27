package ch.unisg.ics.interactions.wot.td.schema;

import java.util.List;

public class FieldValueListBinding extends FieldBinding {
  private List<Object> fieldValueList;
  
  public FieldValueListBinding(String name, List<Object> valueList) {
    super(name);
    this.fieldValueList = valueList;
  }
  
  public List<Object> getValueList() {
    return fieldValueList;
  }
}
