package ch.unisg.ics.interactions.wot.td.vocabularies;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public final class HTV {
  public static final String PREFIX = "http://www.w3.org/2011/http#";
  
  public static final IRI methodName = createIRI("methodName");
  
  public static IRI createIRI(String fragment) {
    return SimpleValueFactory.getInstance().createIRI(PREFIX + fragment);
  }
  
  private HTV() { }
}
