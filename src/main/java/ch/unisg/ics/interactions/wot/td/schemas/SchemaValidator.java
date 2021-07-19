package ch.unisg.ics.interactions.wot.td.schemas;

import java.util.*;
import java.util.stream.Collectors;

public class SchemaValidator {

  public static boolean validate(DataSchema schema, Object value) {
    switch (schema.getDatatype()) {
      case DataSchema.STRING:
        if (value instanceof String) {
          return validate((StringSchema) schema, (String) value);
        }
        break;
      case DataSchema.NUMBER:
        if (value instanceof Number) {
          return validate((NumberSchema) schema, ((Number) value).doubleValue());
        }
        break;
      case DataSchema.INTEGER:
        if (value instanceof Integer) {
          return validate((IntegerSchema) schema, ((Integer) value).intValue());
        }
        break;
      case DataSchema.BOOLEAN:
        if (value instanceof Boolean) {
          return validate((BooleanSchema) schema, ((Boolean) value).booleanValue());
        }
        break;
      case DataSchema.ARRAY:
        if (value instanceof List<?>) {
          List<Object> values = getValidObjects((List<?>) value);
          return validate((ArraySchema) schema, values);
        }
        break;
      case DataSchema.OBJECT:
        if (value instanceof Map<?, ?>) {
          List<String> names = getValidNames((Map<?, ?>) value);
          if (names.size() == ((Map<?, ?>) value).size()) {
            Map<String, Object> values = new HashMap<>();
            names.forEach(name -> values.put(name, ((Map<?, ?>) value).get(name)));
            return validate((ObjectSchema) schema, values);
          }
        }
        break;
      case DataSchema.NULL:
        if (Objects.isNull(value)) {
          return true;
        }
        break;
      default:
        break;
    }
    return false;
  }

  public static boolean validate(StringSchema schema, String value) {
    /* TODO validate against enum */
    return true;
  }

  public static boolean validate(NumberSchema schema, double value) {
    /* TODO validate against enum */
    return true;
  }

  public static boolean validate(IntegerSchema schema, int value) {
    /* TODO validate against enum */
    return true;
  }

  public static boolean validate(BooleanSchema schema, boolean value) {
    /* TODO validate against enum */
    return true;
  }

  public static boolean validate(ArraySchema schema, List<Object> values) {
    if (schema.getMinItems().isPresent() && values.size() < schema.getMinItems().get()) {
      return false;
    }
    if (schema.getMaxItems().isPresent() && values.size() > schema.getMaxItems().get()) {
      return false;
    }

    List<DataSchema> items = schema.getItems();

    if (items.size() == 1) {
      for (Object itemValue : values) {
        if (!validate(items.get(0), itemValue)) {
          return false;
        }
      }
    } else if (items.size() == values.size()) {
      for (int i = 0; i < items.size(); i++) {
        if (!validate(items.get(i), values.get(i))) {
          return false;
        }
      }
    } else return items.isEmpty();

    /* TODO validate against enum */

    return true;
  }

  public static boolean validate(ObjectSchema schema, Map<String, Object> values) {
    return (validateByPropertyNames(schema, values)
      || validateByPropertySemanticTypes(schema, values));

  }

  public static boolean validateByPropertyNames(ObjectSchema schema, Map<String, Object> values) {
    Map<String, DataSchema> properties = schema.getProperties();
    List<String> requiredPropertyNames = schema.getRequiredProperties();

    // if there are no defined properties, return true
    if (schema.getProperties().isEmpty()) {
      return true;
    }

    // if there are less values than the number of required properties, return false
    if (values.size() < schema.getRequiredProperties().size()) {
      return false;
    }

    // if all value names are specified in the object schema,
    // and all required property names are in values,
    // then validate against property names
    if (properties.keySet().containsAll(values.keySet())
      && values.keySet().containsAll(requiredPropertyNames)) {
      for (String name : values.keySet()) {
        DataSchema property = properties.get(name);
        if (!validate(property, values.get(name))) {
          return false;
        }
      }
      return true;
    }

    return false;
  }

  public static boolean validateByPropertySemanticTypes(ObjectSchema schema, Map<String, Object> values) {
    Map<String, DataSchema> properties = schema.getProperties();
    List<String> requiredPropertyNames = schema.getRequiredProperties();

    // if there are no defined properties, return true
    if (schema.getProperties().isEmpty()) {
      return true;
    }

    System.out.println(values.size() < schema.getRequiredProperties().size());
    // if there are less values than the number of required properties, return false
    if (values.size() < schema.getRequiredProperties().size()) {
      return false;
    }

    //validate against semantic types of properties
    List<DataSchema> requiredSchemas = requiredPropertyNames.stream()
      .map(properties::get).collect(Collectors.toList());


    List<String> semanticTypes = new ArrayList<>();
    requiredSchemas.stream().map(DataSchema::getSemanticTypes)
      .forEach(semanticTypes::addAll);

    // If a semantic type in values appears among the semantic types of more
    // than one property, then values are considered invalid to avoid ambiguity
    if (values.keySet().stream().anyMatch(type -> semanticTypes.contains(type)
      && Collections.frequency(semanticTypes, type) > 1)) {
      return false;
    }

    Map<String, Object> valuesByPropertyNames = new HashMap<>();
    for (String semanticType : values.keySet()) {
      Optional<String> name = schema.getFirstPropertyNameBySemnaticType(semanticType);
      if (!name.isPresent()) {
        System.out.println(semanticType);
        return false;
      }
      System.out.println(name.get());
      System.out.println(values.get(semanticType));
      valuesByPropertyNames.put(name.get(), values.get(semanticType));
    }

    return validate(schema, valuesByPropertyNames);
  }

  private static List<String> getValidNames(Map<?, ?> value) {
    return value.keySet()
      .stream()
      .filter(String.class::isInstance)
      .map(String.class::cast)
      .collect(Collectors.toList());
  }

  private static List<Object> getValidObjects(List<?> value) {
    return ((List<?>) value)
      .stream()
      .filter(Objects::nonNull)
      .map(Object.class::cast)
      .collect(Collectors.toList());
  }
}
