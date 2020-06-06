package ch.unisg.ics.interactions.wot.td.vocabularies;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public final class HCTL {
  public static final String PREFIX = "https://www.w3.org/2019/wot/hypermedia#";
  
  public static final IRI hasTarget = createIRI("hasTarget");
  public static final IRI forContentType = createIRI("forContentType");
  public static final IRI hasOperationType = createIRI("hasOperationType");
  
  public static IRI createIRI(String fragment) {
    return SimpleValueFactory.getInstance().createIRI(PREFIX + fragment);
  }
  
  private HCTL() { }
}
