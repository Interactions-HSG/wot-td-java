package ch.unisg.ics.interactions.wot.td.vocabularies;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public final class COV {
  public static final String PREFIX = "http://www.example.org/coap-binding#";

  public static final String methodName = PREFIX + "methodName";

  public static IRI createIRI(String fragment) {
    return SimpleValueFactory.getInstance().createIRI(PREFIX + fragment);
  }

  private COV() { }
}
