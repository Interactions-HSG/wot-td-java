package ch.unisg.ics.interactions.wot.td.schemas;

import java.util.Set;

import com.google.gson.JsonElement;

public class NullSchema extends DataSchema {

  private NullSchema(Set<String> semanticTypes) {
    super(DataSchema.NULL, semanticTypes);
  }
  
  @Override
  public Object parseJson(JsonElement element) {
    if (element == null || !element.isJsonNull()) {
      throw new IllegalArgumentException("JSON element is not a null value.");
    }
    
    return null;
  }
  
  public static class Builder extends DataSchema.Builder<NullSchema, NullSchema.Builder> {

    @Override
    public NullSchema build() {
      return new NullSchema(semanticTypes);
    }
  }
}
