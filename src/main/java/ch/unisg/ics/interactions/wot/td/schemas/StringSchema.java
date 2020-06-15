package ch.unisg.ics.interactions.wot.td.schemas;

import java.util.Set;

import com.google.gson.JsonElement;

public class StringSchema extends DataSchema {

  private StringSchema(Set<String> semanticTypes) {
    super(DataSchema.STRING, semanticTypes);
  }
  
  @Override
  public Object parseJson(JsonElement element) {
    if (element == null || !element.isJsonPrimitive()) {
      throw new IllegalArgumentException("JSON element is not a primitive type.");
    }
    
    return element.getAsString();
  }
  
  public static class Builder extends DataSchema.Builder<StringSchema, StringSchema.Builder> {

    @Override
    public StringSchema build() {
      return new StringSchema(this.semanticTypes);
    }
  }
}
