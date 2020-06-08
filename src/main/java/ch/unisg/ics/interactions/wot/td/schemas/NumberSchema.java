package ch.unisg.ics.interactions.wot.td.schemas;

import java.util.Optional;
import java.util.Set;

public class NumberSchema extends DataSchema {
  final private Optional<Double> minimum;
  final private Optional<Double> maximum;
  
  protected NumberSchema(Set<String> semanticTypes, Optional<Double> minimum, 
      Optional<Double> maximum) {
    super(DataSchema.NUMBER, semanticTypes);
    
    this.minimum = minimum;
    this.maximum = maximum;
  }

  public Optional<Double> getMinimum() {
    return minimum;
  }

  public Optional<Double> getMaximum() {
    return maximum;
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
    
    public NumberSchema build() {
      return new NumberSchema(semanticTypes, minimum, maximum);
    }
  }
}
