package ch.unisg.ics.interactions.wot.td.schemas;

import java.util.Optional;
import java.util.Set;

import com.google.gson.JsonElement;

public class NumberSchema extends DataSchema {
  final protected Optional<Double> minimum;
  final protected Optional<Double> maximum;

  protected NumberSchema(Set<String> semanticTypes, Set<String> enumeration, Optional<Double> minimum,
      Optional<Double> maximum) {
    this(DataSchema.NUMBER, semanticTypes, enumeration, minimum, maximum);
  }

  protected NumberSchema(String numberType, Set<String> semanticTypes, Set<String> enumeration,
      Optional<Double> minimum, Optional<Double> maximum) {
    super(numberType, semanticTypes, enumeration);
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

  public static class Builder extends DataSchema.Builder<NumberSchema, NumberSchema.Builder> {
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
      return new NumberSchema(semanticTypes, enumeration, minimum, maximum);
    }
  }
}
