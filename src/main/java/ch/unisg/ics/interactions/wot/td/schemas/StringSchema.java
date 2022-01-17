package ch.unisg.ics.interactions.wot.td.schemas;

import java.util.Optional;
import java.util.Set;

import com.google.gson.JsonElement;

public class StringSchema extends DataSchema {

  private StringSchema(Set<String> semanticTypes, Set<String> enumeration,
                       Optional<String> contentMediaType) {
    super(DataSchema.STRING, semanticTypes, enumeration, contentMediaType);
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
      return new StringSchema(semanticTypes, enumeration, contentMediaType);
    }
  }
}
