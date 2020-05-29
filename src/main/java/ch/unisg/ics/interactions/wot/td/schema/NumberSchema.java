package ch.unisg.ics.interactions.wot.td.schema;

import java.util.Optional;

public class NumberSchema extends DataSchema {
  private Optional<Double> minimum;
  private Optional<Double> maximum;
  
  private NumberSchema(String type, Optional<Double> minimum, Optional<Double> maximum) {
    super(type);
    
    this.minimum = minimum;
    this.maximum = maximum;
  }

  public Optional<Double> getMinimum() {
    return minimum;
  }

  public Optional<Double> getMaximum() {
    return maximum;
  }
  
  public static class Builder {
    private Optional<Double> minimum;
    private Optional<Double> maximum;
    
    public Builder addMinimum(double minimum) {
      this.minimum = Optional.of(minimum);
      return this;
    }
    
    public Builder addMaximum(double maximum) {
      this.maximum = Optional.of(maximum);
      return this;
    }
    
    public NumberSchema build() {
      return new NumberSchema(DataSchema.SCHEMA_NUMBER_TYPE, minimum, maximum);
    }
  }
}
