package ch.unisg.ics.interactions.wot.td.vocabularies;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public final class WoTSec {
  public static final String PREFIX = "https://www.w3.org/2019/wot/security#";
  
  public static final String NoSecurityScheme = PREFIX + "NoSecurityScheme";
  
  public static IRI createIRI(String fragment) {
    return SimpleValueFactory.getInstance().createIRI(PREFIX + fragment);
  }
  
  private WoTSec() { }
}
