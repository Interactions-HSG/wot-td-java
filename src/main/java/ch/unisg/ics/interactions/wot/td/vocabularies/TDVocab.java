package ch.unisg.ics.interactions.wot.td.vocabularies;

import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDF;
import org.apache.commons.rdf.rdf4j.RDF4J;

public class TDVocab {

  private static final String PREFIX = "http://www.w3.org/ns/td#";
  private static RDF rdfImpl = new RDF4J();
  
  public static IRI createIRI(String fragment) {
    return rdfImpl.createIRI(PREFIX + fragment);
  }
  
  public static final IRI Thing         = createIRI("Thing");
  public static final IRI Action        = createIRI("Action");
  
  public static final IRI title         = createIRI("title");
  public static final IRI base          = createIRI("base");
  public static final IRI interaction   = createIRI("interaction");
  public static final IRI security      = createIRI("security");
  
  public static final IRI form          = createIRI("form");
  public static final IRI methodName    = createIRI("methodName");
  public static final IRI href          = createIRI("href");
  public static final IRI contentType   = createIRI("contentType");
  public static final IRI op            = createIRI("op");
  
  public static final IRI Object        = createIRI("Object");
  public static final IRI Array         = createIRI("Array");
  public static final IRI Number        = createIRI("Number");
  public static final IRI Boolean       = createIRI("Boolean");
  public static final IRI String        = createIRI("String");
  
  public static final IRI schema        = createIRI("schema");
  public static final IRI input         = createIRI("input");
  public static final IRI output        = createIRI("output");
  public static final IRI schemaType    = createIRI("schemaType");
  public static final IRI field         = createIRI("field");
  public static final IRI items         = createIRI("items");
  public static final IRI constant      = createIRI("const");
}
