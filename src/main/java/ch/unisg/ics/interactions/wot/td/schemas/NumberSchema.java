package ch.unisg.ics.interactions.wot.td.schemas;

import com.google.gson.JsonElement;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class NumberSchema extends DataSchema {
  final protected Optional<Double> minimum;
  final protected Optional<Double> maximum;

  protected NumberSchema(Set<String> semanticTypes, Set<String> enumeration,
                         Optional<String> contentMediaType, List<DataSchema> dataSchemas,
                         Optional<Double> minimum, Optional<Double> maximum) {
    this(DataSchema.NUMBER, semanticTypes, enumeration, contentMediaType, dataSchemas, minimum, maximum);
  }

  protected NumberSchema(String numberType, Set<String> semanticTypes, Set<String> enumeration,
                         Optional<String> contentMediaType, List<DataSchema> dataSchemas,
                         Optional<Double> minimum, Optional<Double> maximum) {
    super(numberType, semanticTypes, enumeration, contentMediaType, dataSchemas);
    this.minimum = minimum;
    this.maximum = maximum;
  }

  public Optional<Double> getMinimum() {
    return minimum;
  }

  public Optional<Double> getMaximum() {
    return maximum;
  }

  @Override
  public Object parseJson(JsonElement element) {
    if (element == null || !element.isJsonPrimitive()) {
      throw new IllegalArgumentException("JSON element is not a primitive type.");
    }

    return element.getAsDouble();
  }

  public static class Builder extends DataSchema.JsonSchemaBuilder<NumberSchema, NumberSchema.Builder> {
    private Optional<Double> minimum;
    private Optional<Double> maximum;

    public Builder() {
      this.minimum = Optional.empty();
      this.maximum = Optional.empty();
    }

    public Builder addMinimum(Double minimum) {
      this.minimum = Optional.of(minimum);
      return this;
    }

    public Builder addMaximum(Double maximum) {
      this.maximum = Optional.of(maximum);
      return this;
    }

    @Override
    public NumberSchema build() {
      return new NumberSchema(semanticTypes, enumeration, contentMediaType, dataSchemas, minimum, maximum);
    }
  }
}
