package ch.unisg.ics.interactions.wot.td.vocabularies;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public final class DCT {
  public static final String PREFIX = "http://purl.org/dc/terms/";
  
  public static final IRI title = createIRI("title");
  
  public static IRI createIRI(String fragment) {
    return SimpleValueFactory.getInstance().createIRI(PREFIX + fragment);
  }
  
  private DCT() { }
}
