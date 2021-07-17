package ch.unisg.ics.interactions.wot.td.schemas;

import java.util.Objects;

public class DataSchemaValidator {

  public static boolean validate(DataSchema schema, Object value) {
    switch(schema.getDatatype()) {
      case DataSchema.STRING:
        if (value instanceof String) {
          return validate((StringSchema) schema, (String) value);
        }
        break;
      case DataSchema.NUMBER:
        return false;
      case DataSchema.INTEGER:
        return false;
      case DataSchema.BOOLEAN:
        return false;
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
}
