package ch.unisg.ics.interactions.wot.td.security;

import ch.unisg.ics.interactions.wot.td.io.InvalidTDException;
import ch.unisg.ics.interactions.wot.td.vocabularies.WoTSec;

import java.util.*;

public abstract class TokenBasedSecurityScheme extends SecurityScheme {

  private final TokenLocation in;
  private final Optional<String> name;

  protected TokenBasedSecurityScheme(String schemeName, Map<String, Object> configuration, Set<String> semanticTypes,
                                     TokenLocation in, Optional<String> name) {
    super(schemeName, configuration, semanticTypes);
    this.in = in;
    this.name = name;
  }

  /**
   * Gets the location of security authentication information. The location
   * must be one of those specified in the enum
   * {@link TokenLocation}, i.e.
   * header, query, body, or cookie
   *
   * @return the location of security authentication information
   */
  public TokenLocation getTokenLocation() {
    return in;
  }

  /**
   * Gets the name for query, header, or cookie parameters.
   *
   * @return the name of the token
   */
  public Optional<String> getTokenName() {
    return name;
  }

  public enum TokenLocation {
    HEADER, QUERY, BODY, COOKIE, URI;

    @Override
    public String toString() {
      return name().toLowerCase(Locale.ENGLISH);
    }

    public static TokenLocation fromString(String value) {
      for(TokenLocation tl : values()) {
        if(tl.name().equalsIgnoreCase(value)) {
          return tl;
        }
      }
      throw new IllegalArgumentException("No enum constant " + TokenLocation.class + " for string "
        + value.toUpperCase(Locale.ENGLISH));
    }
  }

  public static abstract class Builder<T extends TokenBasedSecurityScheme,
    S extends Builder>
    extends SecurityScheme.Builder<TokenBasedSecurityScheme, Builder> {
    protected TokenLocation in;
    protected Optional<String> name;

    protected Builder() {
      this.name = Optional.empty();
      this.addTokenLocation(TokenLocation.HEADER);
    }

    /**
     * Specifies the location of security authentication information. The location
     * must be one of those specified in the enum
     * {@link TokenLocation},
     * i.e. header, query, body, or cookie.
     *
     * @param in the location of security authentication information
     * @return the builder
     */
    @SuppressWarnings("unchecked")
    public S addTokenLocation(TokenLocation in) {
      this.in = in;
      this.configuration.put(WoTSec.in, in);
      return (S) this;
    }

    /**
     * Specifies the name for query, header, or cookie parameters.
     *
     * @param name the name of the token
     * @return the builder
     */
    @SuppressWarnings("unchecked")
    public S addTokenName(String name) {
      this.name = Optional.of(name);
      this.configuration.put(WoTSec.name, name);
      return (S) this;
    }

    /**
     * Specifies the values of the security configuration, which can be used in security definitions
     * of a <code>Thing Description</code>.
     * The location must be one of those specified in the enum <code>TokenLocation</code>, i.e.
     * header, query, body, or cookie.
     *
     * @param in   the location of security authentication information
     * @param name the name of the token
     * @return the builder
     */
    @SuppressWarnings("unchecked")
    public S addToken(TokenLocation in, String name) {
      addTokenLocation(in);
      addTokenName(name);
      return (S) this;
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
