package ch.unisg.ics.interactions.wot.td.schemas;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class ArraySchema extends DataSchema {
  final private List<DataSchema> items;
  final private Optional<Integer> minItems;
  final private Optional<Integer> maxItems;
  
  protected ArraySchema(Set<String> semanticTypes, Set<String> enumeration, List<DataSchema> items, 
      Optional<Integer> minItems, Optional<Integer> maxItems) {
    super(DataSchema.ARRAY, semanticTypes, enumeration);
    
    this.items = items;
    this.minItems = minItems;
    this.maxItems = maxItems;
  }
  
  public boolean validate(List<Object> values) {
    // TODO
    return true;
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
  
  public Optional<DataSchema> getFirstItemSchema(String datatype) {
    for (DataSchema schema : items) {
      if (schema.getDatatype() == datatype) {
        return Optional.of(schema);
      }
    }
    
    return Optional.empty();
  }
  
  public boolean containsItemSchema(String datatype) {
    return getFirstItemSchema(datatype).isPresent();
  }
  
  @Override
  public List<Object> parseJson(JsonElement element) {
    if (!element.isJsonArray()) {
      throw new IllegalArgumentException("The payload is not an array.");
    }
    
    JsonArray arrayPayload = element.getAsJsonArray();
    
    /* Array size validation */
    if (minItems.isPresent() && arrayPayload.size() < minItems.get()) {
      throw new IllegalArgumentException("The array has less items than the required minimum.");
    }
    if (maxItems.isPresent() && arrayPayload.size() > maxItems.get()) {
      throw new IllegalArgumentException("The array has more items than the required maximum.");
    }
    
    List<Object> data = new ArrayList<Object>();
    
    for (JsonElement elem : arrayPayload) {
      // TODO: handle array schemas without items
      Optional<DataSchema> itemSchema = getItemSchema(elem);
      if (itemSchema.isPresent()) {
        data.add(itemSchema.get().parseJson(elem));
      }
    }
    
    return data;
  }
  
  private Optional<DataSchema> getItemSchema(JsonElement element) {
    Optional<DataSchema> itemSchema = Optional.empty();
    
    if (element.isJsonObject()) {
      itemSchema = getFirstItemSchema(DataSchema.OBJECT);
    } else if (element.isJsonArray()) {
      itemSchema = getFirstItemSchema(DataSchema.ARRAY);
    } else if (element.isJsonPrimitive()) {
      JsonPrimitive primitive = element.getAsJsonPrimitive();
      if (primitive.isBoolean()) {
        itemSchema = getFirstItemSchema(DataSchema.BOOLEAN);
      } else if (primitive.isString()) {
        itemSchema = getFirstItemSchema(DataSchema.STRING);
      } else if (primitive.isNumber()) {
        // Try number first
        itemSchema = getFirstItemSchema(DataSchema.NUMBER);
        if (!itemSchema.isPresent()) {
          // If both NumberSchema and IntegerSchema are present, NumberSchmea will be kept first
          itemSchema = getFirstItemSchema(DataSchema.INTEGER);
        }
      }
    } else if (element.isJsonNull()) {
      itemSchema = getFirstItemSchema(DataSchema.NULL);
    }
    
    return itemSchema;
  }
  
  public static class Builder extends DataSchema.Builder<ArraySchema, ArraySchema.Builder> {
    final private List<DataSchema> items;
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
      return new ArraySchema(semanticTypes, enumeration, items, minItems, maxItems);
    }
  }
}
