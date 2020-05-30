package ch.unisg.ics.interactions.wot.td.schema;

import java.util.Optional;

public class IntegerSchema extends DataSchema {
  private Optional<Integer> minimum;
  private Optional<Integer> maximum;
  
  private IntegerSchema(String type, Optional<Integer> minimum, Optional<Integer> maximum) {
    super(type);
    
    this.minimum = minimum;
    this.maximum = maximum;
  }

  public Optional<Integer> getMinimum() {
    return minimum;
  }

  public Optional<Integer> getMaximum() {
    return maximum;
  }
  
  public static class Builder {
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
      return new IntegerSchema(DataSchema.INTEGER, minimum, maximum);
    }
  }
}
