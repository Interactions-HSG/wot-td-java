package ch.unisg.ics.interactions.wot.td.schemas;

import com.google.gson.JsonElement;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class StringSchema extends DataSchema {

  private StringSchema(Set<String> semanticTypes, Set<String> enumeration,
                       Optional<String> contentMediaType, List<DataSchema> dataSchemas) {
    super(DataSchema.STRING, semanticTypes, enumeration, contentMediaType, dataSchemas);
  }

  @Override
  public Object parseJson(JsonElement element) {
    if (element == null || !element.isJsonPrimitive()) {
      throw new IllegalArgumentException("JSON element is not a primitive type.");
    }

    return element.getAsString();
  }

  public static final class Builder extends DataSchema.JsonSchemaBuilder<StringSchema, StringSchema.Builder> {

    @Override
    public StringSchema build() {
      return new StringSchema(semanticTypes, enumeration, contentMediaType,
        dataSchemas);
    }
  }
}
