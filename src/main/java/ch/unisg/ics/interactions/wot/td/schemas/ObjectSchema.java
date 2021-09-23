package ch.unisg.ics.interactions.wot.td.schemas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import ch.unisg.ics.interactions.wot.td.io.InvalidTDException;
import ch.unisg.ics.interactions.wot.td.vocabularies.JSONSchema;

public class ObjectSchema extends DataSchema {
  final private Map<String, DataSchema> properties;
  final private List<String> required;

  protected ObjectSchema(Set<String> semanticTypes, Set<String> enumeration,
      Map<String, DataSchema> properties, List<String> required) {
    super(DataSchema.OBJECT, semanticTypes, enumeration);

    this.properties = properties;
    this.required = required;
  }

  public boolean validate(Map<String, Object> values) {
    // TODO
    return true;
  }

  @Override
  public Object parseJson(JsonElement element) {
    if (!element.isJsonObject()) {
      throw new IllegalArgumentException("The payload is not an object.");
    }

    JsonObject objPayload = element.getAsJsonObject();
    Map<String, Object> data = new HashMap<String, Object>();

    for (String propName : properties.keySet()) {
      JsonElement prop = objPayload.get(propName);
      if (prop == null) {
        if (hasRequiredProperty(propName)) {
          throw new IllegalArgumentException("Missing required property: " + propName);
        }

        continue;
      }

      DataSchema propSchema = properties.get(propName);

      // Filter out data schema tags, if any
      List<String> tags = propSchema.getSemanticTypes().stream().filter(tag ->
          !tag.startsWith(JSONSchema.PREFIX)).collect(Collectors.toList());

      if (tags.isEmpty()) {
        data.put(propName, properties.get(propName).parseJson(prop));
      } else {
        // Currently returns one semantic tag; TODO: handle multiple semantic tags
        String semanticType = tags.get(0);
        data.put(semanticType, properties.get(propName).parseJson(prop));
      }
    }

    return data;
  }

  public Map<String, Object> instantiate(Map<String, Object> values) {
    Map<String, Object> instance = new HashMap<String, Object>();

    // TODO: handle semantic arrays
    // TODO: handle semantic arrays with semantic elements

    for (String tag : values.keySet()) {
      Optional<String> propertyName = getFirstPropertyNameBySemanticType(tag);

      if (propertyName.isPresent()) {
        instance.put(propertyName.get(), values.get(tag));
      } else if (properties.containsKey(tag)) {
        instance.put(tag, values.get(tag));
      }
    }

    return instance;
  }

  public Optional<DataSchema> getProperty(String propertyName) {
    DataSchema schema = properties.get(propertyName);
    return (schema == null) ? Optional.empty() : Optional.of(schema);
  }

  public Optional<String> getFirstPropertyNameBySemanticType(String type) {
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

  public boolean hasRequiredProperty(String propName) {
    return required.contains(propName);
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

      return new ObjectSchema(semanticTypes, enumeration, properties, required);
    }
  }
}
