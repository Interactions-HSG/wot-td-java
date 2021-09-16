package ch.unisg.ics.interactions.wot.td.io.json;

import ch.unisg.ics.interactions.wot.td.schemas.*;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;

public class SchemaJsonWriter {

  public static JsonObjectBuilder getDataSchema(DataSchema schema) {
    switch (schema.getDatatype()) {
      case DataSchema.OBJECT:
        return getObjectSchema((ObjectSchema) schema);
      case DataSchema.ARRAY:
        return getArraySchema((ArraySchema) schema);
      case DataSchema.BOOLEAN:
        return getSimpleSchema(DataSchema.BOOLEAN);
      case DataSchema.INTEGER:
        return getIntegerSchema((IntegerSchema) schema, DataSchema.INTEGER);
      case DataSchema.NUMBER:
        return getNumberSchema((NumberSchema) schema, DataSchema.NUMBER);
      case DataSchema.STRING:
        return getSimpleSchema(DataSchema.STRING);
      case DataSchema.NULL:
        return getSimpleSchema(DataSchema.NULL);
      default:
        return Json.createObjectBuilder();
    }
  }

  private static JsonObjectBuilder getSimpleSchema(String schemaType) {
    return Json.createObjectBuilder().add(JWot.TYPE, schemaType);
  }

  private static JsonObjectBuilder getNumberSchema(NumberSchema schema, String type) {
    JsonObjectBuilder schemaObj = getSimpleSchema(type);
    schema.getMaximum().ifPresent(max -> schemaObj.add("maximum", max));
    schema.getMinimum().ifPresent(min -> schemaObj.add("minimum", min));
    return schemaObj;
  }

  private static JsonObjectBuilder getIntegerSchema(IntegerSchema schema, String type) {
    JsonObjectBuilder schemaObj = getSimpleSchema(type);
    schema.getMaximumAsInteger().ifPresent(max -> schemaObj.add("maximum", max));
    schema.getMinimumAsInteger().ifPresent(min -> schemaObj.add("minimum", min));
    return schemaObj;
  }

  private static JsonObjectBuilder getArraySchema(ArraySchema schema) {
    JsonObjectBuilder schemaObj = getSimpleSchema(DataSchema.ARRAY);
    schema.getMinItems().ifPresent(min -> schemaObj.add("minItems", min));
    schema.getMaxItems().ifPresent(max -> schemaObj.add("maxItems", max));

    JsonArrayBuilder itemsArray = Json.createArrayBuilder();
    schema.getItems().forEach(d -> itemsArray.add(getDataSchema(d)));
    schemaObj.add("items", itemsArray);
    return schemaObj;
  }

  private static JsonObjectBuilder getObjectSchema(ObjectSchema schema) {
    JsonObjectBuilder schemaObj = getSimpleSchema(DataSchema.OBJECT);

    JsonObjectBuilder propObj = Json.createObjectBuilder();
    schema.getProperties().forEach((k,v) -> propObj.add(k, getDataSchema(v)));
    schemaObj.add("properties", propObj);

    JsonArrayBuilder requiredArray = Json.createArrayBuilder();
    schema.getRequiredProperties().forEach(requiredArray::add);
    schemaObj.add("required", requiredArray);

    return schemaObj;
  }
}
