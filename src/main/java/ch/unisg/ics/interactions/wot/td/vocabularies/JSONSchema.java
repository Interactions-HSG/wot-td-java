package ch.unisg.ics.interactions.wot.td.vocabularies;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class JSONSchema {
  
  private static final String PREFIX = "https://www.w3.org/2019/wot/json-schema#";
  
  public static IRI createIRI(String fragment) {
    return SimpleValueFactory.getInstance().createIRI(PREFIX + fragment);
  }
  
  /* Classes */
  public static final IRI ArraySchema = createIRI("ArraySchema");
  public static final IRI BooleanSchema = createIRI("BooleanSchema");
  public static final IRI DataSchema = createIRI("DataSchema");
  public static final IRI IntegerSchema = createIRI("IntegerSchema");
  public static final IRI NullSchema = createIRI("NullSchema");
  public static final IRI NumberSchema = createIRI("NumberSchema");
  public static final IRI ObjectSchema = createIRI("ObjectSchema");
  public static final IRI StringSchema = createIRI("StringSchema");
  
  /* Object properties */
  public static final IRI allOf = createIRI("allOf");
  public static final IRI anyOf = createIRI("anyOf");
  public static final IRI items = createIRI("items");
  public static final IRI oneOf = createIRI("oneOf");
  public static final IRI properties = createIRI("properties");
  
  /* Datatype properties */
  public static final IRI constant = createIRI("constant");
  public static final IRI enumeration = createIRI("enumeration");
  public static final IRI format = createIRI("format");
  public static final IRI maxItems = createIRI("maxItems");
  public static final IRI maximum = createIRI("maximum");
  public static final IRI minItems = createIRI("minItems");
  public static final IRI minimum = createIRI("minimum");
  public static final IRI propertyName = createIRI("propertyName");
  public static final IRI readOnly = createIRI("readOnly");
  public static final IRI required = createIRI("required");
  public static final IRI writeOnly = createIRI("writeOnly");
}
