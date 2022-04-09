package ch.unisg.ics.interactions.wot.td.vocabularies;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public final class JSONSchema {

  public static final String PREFIX = "https://www.w3.org/2019/wot/json-schema#";

  /* Classes */
  public static final String ArraySchema = PREFIX + "ArraySchema";
  public static final String BooleanSchema = PREFIX + "BooleanSchema";
  public static final String DataSchema = PREFIX + "DataSchema";
  public static final String IntegerSchema = PREFIX + "IntegerSchema";
  public static final String NullSchema = PREFIX + "NullSchema";
  public static final String NumberSchema = PREFIX + "NumberSchema";
  public static final String ObjectSchema = PREFIX + "ObjectSchema";
  public static final String StringSchema = PREFIX + "StringSchema";

  /* Object properties */
  public static final String allOf = PREFIX + "allOf";
  public static final String anyOf = PREFIX + "anyOf";
  public static final String items = PREFIX + "items";
  public static final String oneOf = PREFIX + "oneOf";
  public static final String properties = PREFIX + "properties";

  /* Datatype properties */
  public static final String constant = PREFIX + "constant";
  public static final String enumeration = PREFIX + "enum";
  public static final String format = PREFIX + "format";
  public static final String maxItems = PREFIX + "maxItems";
  public static final String maximum = PREFIX + "maximum";
  public static final String minItems = PREFIX + "minItems";
  public static final String minimum = PREFIX + "minimum";
  public static final String propertyName = PREFIX + "propertyName";
  public static final String readOnly = PREFIX + "readOnly";
  public static final String required = PREFIX + "required";
  public static final String writeOnly = PREFIX + "writeOnly";
  public static final String contentMediaType = PREFIX + "contentMediaType";

  public static IRI createIRI(String fragment) {
    return SimpleValueFactory.getInstance().createIRI(PREFIX + fragment);
  }

  private JSONSchema() { }
}
