package ch.unisg.ics.interactions.wot.td.schemas;

import java.util.Optional;
import java.util.Set;

import com.google.gson.JsonElement;

public class IntegerSchema extends DataSchema {
  final private Optional<Integer> minimum;
  final private Optional<Integer> maximum;
  
  protected IntegerSchema(Set<String> semanticTypes, Optional<Integer> minimum,
      Optional<Integer> maximum) {
    super(DataSchema.INTEGER, semanticTypes);
    
    this.minimum = minimum;
    this.maximum = maximum;
  }
  
  public Optional<Integer> getMinimum() {
    return minimum;
  }

  public Optional<Integer> getMaximum() {
    return maximum;
  }
  
  @Override
  public Object parseJson(JsonElement element) {
    if (element == null || !element.isJsonPrimitive()) {
      throw new IllegalArgumentException("JSON element is not a primitive type.");
    }
    
    return element.getAsInt();
  }
  
  public static class Builder extends DataSchema.Builder<IntegerSchema, IntegerSchema.Builder> {
    private Optional<Integer> minimum;
    private Optional<Integer> maximum;
    
    public Builder() {
      this.minimum = Optional.empty();
      this.maximum = Optional.empty();
    }
    
    public Builder addMinimum(Integer minimum) {
      this.minimum = Optional.of(minimum);
      return this;
    }
    
    public Builder addMaximum(Integer maximum) {
      this.maximum = Optional.of(maximum);
      return this;
    }
    
    public IntegerSchema build() {
      return new IntegerSchema(semanticTypes, minimum, maximum);
    }
  }
}
