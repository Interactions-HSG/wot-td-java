package ch.unisg.ics.interactions.wot.td.vocabularies;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class WoTSec {

  private static final String PREFIX = "https://www.w3.org/2019/wot/security#";
  
  public static IRI createIRI(String fragment) {
    return SimpleValueFactory.getInstance().createIRI(PREFIX + fragment);
  }
  
  public static final IRI NoSecurityScheme = createIRI("NoSecurityScheme");
}
