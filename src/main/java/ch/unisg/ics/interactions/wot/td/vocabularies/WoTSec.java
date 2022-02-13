package ch.unisg.ics.interactions.wot.td.vocabularies;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public final class WoTSec {
  public static final String PREFIX = "https://www.w3.org/2019/wot/security#";

  /* Classes */
  public static final String NoSecurityScheme = PREFIX + "NoSecurityScheme";
  public static final String APIKeySecurityScheme = PREFIX + "APIKeySecurityScheme";
  public static final String BasicSecurityScheme = PREFIX + "BasicSecurityScheme";
  public static final String DigestSecurityScheme = PREFIX + "DigestSecurityScheme";
  public static final String BearerSecurityScheme = PREFIX + "BearerSecurityScheme";
  public static final String PSKSecurityScheme = PREFIX + "PSKSecurityScheme";

  /* Datatype properties */
  public static final String in = PREFIX + "in";
  public static final String name = PREFIX + "name";
  public static final String qop = PREFIX + "qop";
  public static final String authorization = PREFIX + "authorization";
  public static final String alg = PREFIX + "alg";
  public static final String format = PREFIX + "format";
  public static final String identity = PREFIX + "identity";

  public static IRI createIRI(String fragment) {
    return SimpleValueFactory.getInstance().createIRI(PREFIX + fragment);
  }

  private WoTSec() { }
}
