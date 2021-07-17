package ch.unisg.ics.interactions.wot.td.schemas;

import java.util.List;
import java.util.Objects;
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
        if (value instanceof List) {
          List<Object> values = ((List<?>) value)
            .stream()
            .map(Object.class::cast)
            .collect(Collectors.toList());
          return validate((ArraySchema) schema, values);
        }
        break;
      case DataSchema.OBJECT:
        return false;
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

  public static boolean validate(ArraySchema schema, List<Object> value) {
    /* Array size validation */
    if (schema.getMinItems().isPresent() && value.size() < schema.getMinItems().get()) {
      return false;
    }
    return !schema.getMaxItems().isPresent() || value.size() <= schema.getMaxItems().get();
    // TODO validate against enum
  }
}
