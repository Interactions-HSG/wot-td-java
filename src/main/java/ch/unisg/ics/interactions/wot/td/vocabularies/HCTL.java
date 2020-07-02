package ch.unisg.ics.interactions.wot.td.vocabularies;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public final class HCTL {
  public static final String PREFIX = "https://www.w3.org/2019/wot/hypermedia#";
  
  public static final String hasTarget = PREFIX + "hasTarget";
  public static final String hasOperationType = PREFIX + "hasOperationType";

  public static final String forContentType = PREFIX + "forContentType";
  public static final String forSubProtocol = PREFIX + "forSubProtocol";
  
  public static IRI createIRI(String fragment) {
    return SimpleValueFactory.getInstance().createIRI(PREFIX + fragment);
  }
  
  private HCTL() { }
}
