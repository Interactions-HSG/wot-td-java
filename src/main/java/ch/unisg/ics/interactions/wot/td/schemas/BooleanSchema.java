package ch.unisg.ics.interactions.wot.td.schemas;

import java.util.Set;

import com.google.gson.JsonElement;

public class BooleanSchema extends DataSchema {

  private BooleanSchema(Set<String> semanticTypes, Set<String> enumeration) {
    super(DataSchema.BOOLEAN, semanticTypes, enumeration);
  }
  
  @Override
  public Object parseJson(JsonElement element) {
    if (element == null || !element.isJsonPrimitive()) {
      throw new IllegalArgumentException("JSON element is not a primitive type.");
    }
    
    return element.getAsBoolean();
  }
  
  public static class Builder extends DataSchema.Builder<BooleanSchema, BooleanSchema.Builder> {

    @Override
    public BooleanSchema build() {
      return new BooleanSchema(semanticTypes, enumeration);
    }
  }
}
