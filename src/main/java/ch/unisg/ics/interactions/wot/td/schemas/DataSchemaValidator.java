package ch.unisg.ics.interactions.wot.td.schemas;

import java.util.Objects;

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
        return false;
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
}
