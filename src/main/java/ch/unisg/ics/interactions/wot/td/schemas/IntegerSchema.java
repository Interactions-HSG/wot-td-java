package ch.unisg.ics.interactions.wot.td.schemas;

import com.google.gson.JsonElement;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class IntegerSchema extends NumberSchema {

  protected IntegerSchema(Set<String> semanticTypes, Set<String> enumeration,
                          Optional<String> contentMediaType, List<DataSchema> dataSchemas,
                          Optional<Double> minimum, Optional<Double> maximum) {
    super(DataSchema.INTEGER, semanticTypes, enumeration, contentMediaType,
      dataSchemas, minimum, maximum);
  }

  public Optional<Integer> getMinimumAsInteger() {
    return minimum.map(min -> min.intValue());
  }

  public Optional<Integer> getMaximumAsInteger() {
    return maximum.map(max -> max.intValue());
  }

  @Override
  public Object parseJson(JsonElement element) {
    if (element == null || !element.isJsonPrimitive()) {
      throw new IllegalArgumentException("JSON element is not a primitive type.");
    }

    return element.getAsInt();
  }

  public static class Builder extends DataSchema.JsonSchemaBuilder<IntegerSchema, IntegerSchema.Builder> {
    private Optional<Double> minimum;
    private Optional<Double> maximum;

    public Builder() {
      this.minimum = Optional.empty();
      this.maximum = Optional.empty();
    }

    public Builder addMinimum(Integer minimum) {
      this.minimum = Optional.of(minimum.doubleValue());
      return this;
    }

    public Builder addMaximum(Integer maximum) {
      this.maximum = Optional.of(maximum.doubleValue());
      return this;
    }

    @Override
    public IntegerSchema build() {
      return new IntegerSchema(semanticTypes, enumeration, contentMediaType, dataSchemas, minimum, maximum);
    }
  }
}
