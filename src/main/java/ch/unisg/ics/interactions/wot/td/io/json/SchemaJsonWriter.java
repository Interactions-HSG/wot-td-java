package ch.unisg.ics.interactions.wot.td.io.json;

import ch.unisg.ics.interactions.wot.td.schemas.*;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;

public final class SchemaJsonWriter {

  private SchemaJsonWriter(){
    throw new AssertionError();
  }

  /***
   * This methods returns the correct JsonObjectBuilder Object that is generate from the DataSchema.
   * @param schema a DataSchema object to convert.
   * @return The JsonObjectBuilder that when built will result in a JsonObject representation of the schema.
   */
  public static JsonObjectBuilder getDataSchema(DataSchema schema) {
    switch (schema.getDatatype()) {
      case DataSchema.OBJECT:
        return getObjectSchema((ObjectSchema) schema);
      case DataSchema.ARRAY:
        return getArraySchema((ArraySchema) schema);
      case DataSchema.BOOLEAN:
        return getSimpleSchema(schema, DataSchema.BOOLEAN);
      case DataSchema.INTEGER:
        return getIntegerSchema((IntegerSchema) schema);
      case DataSchema.NUMBER:
        return getNumberSchema((NumberSchema) schema);
      case DataSchema.STRING:
        return getSimpleSchema(schema, DataSchema.STRING);
      case DataSchema.NULL:
        return getSimpleSchema(schema, DataSchema.NULL);
      default:
        return Json.createObjectBuilder();
    }
  }

  private static JsonObjectBuilder getSimpleSchema(DataSchema schema, String schemaType) {
    JsonObjectBuilder obj = Json.createObjectBuilder();
    if(schema.getSemanticTypes().size() > 1){
      JsonArrayBuilder typeArray = Json.createArrayBuilder();
      schema.getSemanticTypes().forEach(typeArray::add);
      obj.add(JWot.SEMANTIC_TYPE, typeArray);
    } else if (schema.getSemanticTypes().size() > 0){
      obj.add(JWot.SEMANTIC_TYPE, schema.getSemanticTypes().stream().findFirst().orElse(""));
    }
    return obj.add(JWot.TYPE, schemaType);
  }

  private static JsonObjectBuilder getNumberSchema(NumberSchema schema) {
    JsonObjectBuilder schemaObj = getSimpleSchema(schema, DataSchema.NUMBER);
    schema.getMaximum().ifPresent(max -> schemaObj.add("maximum", max));
    schema.getMinimum().ifPresent(min -> schemaObj.add("minimum", min));
    return schemaObj;
  }

  private static JsonObjectBuilder getIntegerSchema(IntegerSchema schema) {
    JsonObjectBuilder schemaObj = getSimpleSchema(schema, DataSchema.INTEGER);
    schema.getMaximumAsInteger().ifPresent(max -> schemaObj.add("maximum", max));
    schema.getMinimumAsInteger().ifPresent(min -> schemaObj.add("minimum", min));
    return schemaObj;
  }

  private static JsonObjectBuilder getArraySchema(ArraySchema schema) {
    JsonObjectBuilder schemaObj = getSimpleSchema(schema, DataSchema.ARRAY);
    schema.getMinItems().ifPresent(min -> schemaObj.add("minItems", min));
    schema.getMaxItems().ifPresent(max -> schemaObj.add("maxItems", max));

    if(schema.getItems().size() > 1){
      JsonArrayBuilder itemsArray = Json.createArrayBuilder();
      schema.getItems().forEach(d -> itemsArray.add(getDataSchema(d)));
    } else if(schema.getItems().size() > 0){
      schemaObj.add("items", getDataSchema(schema.getItems().get(0)));
    }

    return schemaObj;
  }

  private static JsonObjectBuilder getObjectSchema(ObjectSchema schema) {
    JsonObjectBuilder schemaObj = getSimpleSchema(schema, DataSchema.OBJECT);

    JsonObjectBuilder propObj = Json.createObjectBuilder();
    schema.getProperties().forEach((k,v) -> propObj.add(k, getDataSchema(v)));
    schemaObj.add("properties", propObj);

    if(schema.getRequiredProperties().size()>0) {
      JsonArrayBuilder requiredArray = Json.createArrayBuilder();
      schema.getRequiredProperties().forEach(requiredArray::add);
      schemaObj.add("required", requiredArray);
    }
    return schemaObj;
  }
}
