package ch.unisg.ics.interactions.wot.td.schemas;

import com.google.gson.JsonElement;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class BooleanSchema extends DataSchema {

  private BooleanSchema(Set<String> semanticTypes, Set<String> enumeration,
                        Optional<String> contentMediaType, List<DataSchema> dataSchemas) {
    super(DataSchema.BOOLEAN, semanticTypes, enumeration, contentMediaType, dataSchemas);
  }

  @Override
  public Object parseJson(JsonElement element) {
    if (element == null || !element.isJsonPrimitive()) {
      throw new IllegalArgumentException("JSON element is not a primitive type.");
    }

    return element.getAsBoolean();
  }

  public static class Builder extends DataSchema.JsonSchemaBuilder<BooleanSchema, BooleanSchema.Builder> {

    @Override
    public BooleanSchema build() {
      return new BooleanSchema(semanticTypes, enumeration, contentMediaType,
        dataSchemas);
    }
  }
}
