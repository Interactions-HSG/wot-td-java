package ch.unisg.ics.interactions.wot.td.schemas;

import com.google.gson.JsonElement;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class NullSchema extends DataSchema {

  private NullSchema(Set<String> semanticTypes, Set<String> enumeration,
                     Optional<String> contentMediaType, List<DataSchema> dataSchemas) {
    super(DataSchema.NULL, semanticTypes, enumeration, contentMediaType, dataSchemas);
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
      return new NullSchema(semanticTypes, enumeration, contentMediaType, dataSchemas);
    }
  }
}
