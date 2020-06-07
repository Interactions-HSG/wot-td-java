package ch.unisg.ics.interactions.wot.td.schemas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import ch.unisg.ics.interactions.wot.td.utils.InvalidTDException;

public class ObjectSchema extends DataSchema {
  final private Map<String, DataSchema> properties;
  final private List<String> required;
  
  protected ObjectSchema(Set<String> semanticTypes, Map<String, DataSchema> properties, 
      List<String> required) {
    super(DataSchema.OBJECT, semanticTypes);
    
    this.properties = properties;
    this.required = required;
  }
  
  public boolean validate(Map<String, Object> values) {
    // TODO
    return true;
  }
  
  public Map<String, Object> instantiate(Map<String, Object> values) {
    Map<String, Object> instance = new HashMap<String, Object>();
    
    // TODO: handle semantic arrays
    // TODO: handle semantic arrays with semantic elements
    
    for (String type : values.keySet()) {
      Optional<String> propertyName = getFirstPropertyNameBySemnaticType(type);
      
      if (propertyName.isPresent()) {
        instance.put(propertyName.get(), values.get(type));
      }
    }
    
    return instance;
  }
  
  public Optional<DataSchema> getProperty(String propertyName) {
    DataSchema schema = properties.get(propertyName);
    return (schema == null) ? Optional.empty() : Optional.of(schema); 
  }
  
  public Optional<String> getFirstPropertyNameBySemnaticType(String type) {
    for (Map.Entry<String, DataSchema> property : properties.entrySet()) {
      if (property.getValue().isA(type)) {
        return Optional.of(property.getKey());
      }
    }
    
    return Optional.empty();
  }
  
  public Map<String, DataSchema> getProperties() {
    return properties;
  }

  public List<String> getRequiredProperties() {
    return required;
  }

  public static class Builder extends DataSchema.Builder<ObjectSchema, ObjectSchema.Builder> {
    final private Map<String, DataSchema> properties;
    final private List<String> required;
    
    public Builder() {
      this.properties = new HashMap<String, DataSchema>();
      this.required = new ArrayList<String>();
    }
    
    public Builder addProperty(String propertyName, DataSchema schema) {
      this.properties.put(propertyName, schema);
      return this;
    }
    
    public Builder addRequiredProperties(String... properties) {
      this.required.addAll(Arrays.asList(properties));
      return this;
    }
    
    public ObjectSchema build() throws InvalidTDException {
      for (String propertyName : required) {
        if (!properties.containsKey(propertyName)) {
          throw new InvalidTDException("Required property is not in the list of properties: " 
              + propertyName);
        }
      }
      
      return new ObjectSchema(semanticTypes, properties, required);
    }
  }
}
