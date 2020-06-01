package ch.unisg.ics.interactions.wot.td.schemas;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ArraySchema extends DataSchema {
  private List<DataSchema> items;
  private Optional<Integer> minItems;
  private Optional<Integer> maxItems;
  
  protected ArraySchema(Set<String> semanticTypes, List<DataSchema> items, Optional<Integer> minItems, 
      Optional<Integer> maxItems) {
    super(DataSchema.ARRAY, semanticTypes);
    
    this.items = items;
    this.minItems = minItems;
    this.maxItems = maxItems;
  }
  
  public List<DataSchema> getItems() {
    return items;
  }

  public Optional<Integer> getMinItems() {
    return minItems;
  }

  public Optional<Integer> getMaxItems() {
    return maxItems;
  }
  
  public static class Builder extends DataSchema.Builder<ArraySchema, ArraySchema.Builder> {
    private List<DataSchema> items;
    private Optional<Integer> minItems;
    private Optional<Integer> maxItems;
    
    public Builder() {
      this.items = new ArrayList<DataSchema>();
      this.minItems = Optional.empty();
      this.maxItems = Optional.empty();
    }
    
    public Builder addItem(DataSchema item) {
      this.items.add(item);
      return this;
    }
    
    public Builder addMinItems(Integer minItems) throws IllegalArgumentException {
      if (minItems < 0) {
        throw new IllegalArgumentException("The number of minimum items of an array cannot be negative.");
      }
      
      this.minItems = Optional.of(minItems);
      return this;
    }
    
    public Builder addMaxItems(Integer maxItems) throws IllegalArgumentException {
      if (maxItems < 0) {
        throw new IllegalArgumentException("The number of minimum items of an array cannot be negative.");
      }
      
      this.maxItems = Optional.of(maxItems);
      return this;
    }
    
    public ArraySchema build() {
      return new ArraySchema(semanticTypes, items, minItems, maxItems);
    }
  }
}
