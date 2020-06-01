package ch.unisg.ics.interactions.wot.td.vocabularies;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class TD {

  private static final String PREFIX = "http://www.w3.org/ns/td#";
  
  public static IRI createIRI(String fragment) {
    return SimpleValueFactory.getInstance().createIRI(PREFIX + fragment);
  }
  
  /* Classes */
  public static final IRI Thing = createIRI("Thing");
  public static final IRI ActionAffordance = createIRI("ActionAffordance");
  
  /* Object properties */
  public static final IRI title = createIRI("title");
  
  /* td:Thing properties */
  public static final IRI base = createIRI("base");
  public static final IRI interaction = createIRI("interaction");
  public static final IRI security = createIRI("security");
  
  /* td:ActionAffordance properties */
  public static final IRI input = createIRI("input");
  public static final IRI output = createIRI("output");
  
  /* td:Form properties */
  public static final IRI form = createIRI("form");
  public static final IRI href = createIRI("href");
  public static final IRI contentType = createIRI("contentType");
  public static final IRI op = createIRI("op");
}
