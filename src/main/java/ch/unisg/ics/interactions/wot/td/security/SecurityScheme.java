package ch.unisg.ics.interactions.wot.td.security;

import java.util.*;
import java.util.stream.Collectors;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;

import ch.unisg.ics.interactions.wot.td.vocabularies.WoTSec;

public abstract class SecurityScheme {

  public static final String NOSEC = "nosec";
  public static final String BASIC = "basic";
  public static final String DIGEST = "digest";
  public static final String APIKEY = "apikey";
  public static final String BEARER = "bearer";
  public static final String PSK = "psk";
  public static final String OAUTH2 = "oauth2";

  private final String schemeName;
  private final Map<String, Object> configuration;
  private final Set<String> semanticTypes;

  protected SecurityScheme(String schemeName, Map<String, Object> configuration,
                           Set<String> semanticTypes) {
    this.schemeName = schemeName;
    this.configuration = configuration;
    this.semanticTypes = semanticTypes;
  }

  /**
   * Gets the name of the security scheme (i.e. nosec, apikey, basic,
   * digest, bearer, psk, and oauth2).
   *
   * @return the name of the security scheme
   */
  public String getSchemeName() {
    return schemeName;
  }

  /**
   * Gets the security configuration which can be used in security definitions
   * of a <code>Thing Description</code>.
   *
   * @return the security configuration
   */
  public Map<String, Object> getConfiguration() {
    return configuration;
  }

  public Set<String> getSemanticTypes() {
    return semanticTypes;
  }

  public static SecurityScheme getNoSecurityScheme() {
    Set<String> semanticTypes = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(WoTSec.NoSecurityScheme)));
    Map<String, Object> configuration = Collections.unmodifiableMap(new HashMap<>());
    return new SecurityScheme(SecurityScheme.NOSEC, configuration, semanticTypes) {};
  }

  public static abstract class Builder<T extends SecurityScheme,
    S extends Builder> {
    protected Map<String, Object> configuration;
    protected Set<String> semanticTypes;


    protected Builder() {
      this.configuration = new HashMap<>();
      this.semanticTypes = new HashSet<>();
    }

    @SuppressWarnings("unchecked")
    public S addSemanticType(String type) {
      this.semanticTypes.add(type);
      return (S) this;
    }

    @SuppressWarnings("unchecked")
    public S addSemanticTypes(Set<String> type) {
      this.semanticTypes.addAll(type);
      return (S) this;
    }

    public abstract T build();
  }
}
