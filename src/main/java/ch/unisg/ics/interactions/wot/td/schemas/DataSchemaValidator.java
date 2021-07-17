package ch.unisg.ics.interactions.wot.td.schemas;

import java.util.*;
import java.util.stream.Collectors;

public class DataSchemaValidator {

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
    // TODO validate against enum
    return true;
  }

  public static boolean validate(NumberSchema schema, double value) {
    // TODO validate against enum
    return true;
  }

  public static boolean validate(IntegerSchema schema, int value) {
    // TODO validate against enum
    return true;
  }

  public static boolean validate(BooleanSchema schema, boolean value) {
    // TODO validate against enum
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
      for (int i=0 ; i<items.size(); i++) {
        if (!validate(items.get(i), values.get(i))) {
          return false;
        }
      }
    } else return items.isEmpty();

    // TODO validate against enum

    return true;
  }

  public static boolean validate(ObjectSchema schema, Map<String,Object> values) {
    // TODO validate against enum

    return true;
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
      .map(Object.class::cast)
      .collect(Collectors.toList());
  }
}
