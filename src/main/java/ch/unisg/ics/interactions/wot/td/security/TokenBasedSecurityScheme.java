package ch.unisg.ics.interactions.wot.td.security;

import ch.unisg.ics.interactions.wot.td.io.InvalidTDException;
import ch.unisg.ics.interactions.wot.td.vocabularies.WoTSec;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public abstract class TokenBasedSecurityScheme extends SecurityScheme {

  private final TokenLocation in;
  private final Optional<String> name;

  protected TokenBasedSecurityScheme(TokenLocation in, Optional<String> name, String schemeName, Map<String, Object> configuration, Set<String> semanticTypes) {
    super(schemeName, configuration, semanticTypes);
    this.in = in;
    this.name = name;
  }

  /**
   * Gets the location of security authentication information. The location
   * must be one of those specified in the enum
   * {@link ch.unisg.ics.interactions.wot.td.security.TokenBasedSecurityScheme.TokenLocation}, i.e.
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
    HEADER, QUERY, BODY, COOKIE
  }

  public static abstract class Builder<T extends TokenBasedSecurityScheme,
    S extends TokenBasedSecurityScheme.Builder>
    extends SecurityScheme.Builder<TokenBasedSecurityScheme, TokenBasedSecurityScheme.Builder> {
    protected APIKeySecurityScheme.TokenLocation in;
    protected Optional<String> name;

    protected Builder() {
      this.name = Optional.empty();
      this.addTokenLocation(TokenLocation.HEADER);
    }

    /**
     * Specifies the location of security authentication information. The location
     * must be one of those specified in the enum
     * {@link ch.unisg.ics.interactions.wot.td.security.TokenBasedSecurityScheme.TokenLocation},
     * i.e. header, query, body, or cookie.
     *
     * @param in the location of security authentication information
     * @return the builder
     */
    @SuppressWarnings("unchecked")
    public S addTokenLocation(TokenLocation in) {
      this.in = in;
      this.configuration.put(WoTSec.in, in.toString().toLowerCase(Locale.ENGLISH));
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
     * Specifies the security configuration, which can be used in security definitions
     * of a <code>Thing Description</code>.
     *
     * @param configuration the security configuration
     * @return the builder
     */
    @Override
    @SuppressWarnings("unchecked")
    public S addConfiguration(Map<String, Object> configuration) {
      this.configuration.putAll(configuration);
      if (configuration.containsKey(WoTSec.in)) {
        try {
          this.addTokenLocation(TokenLocation.valueOf(String.valueOf(configuration.get(WoTSec.in))
            .toUpperCase(Locale.ENGLISH)));
        } catch (IllegalArgumentException e) {
          throw new InvalidTDException("Invalid token location", e);
        }
      }
      if (configuration.containsKey(WoTSec.name)) {
        this.addTokenName((String) configuration.get(WoTSec.name));
      }
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
      this.in = in;
      this.name = Optional.of(name);
      this.configuration.put(WoTSec.in, in.toString().toLowerCase(Locale.ENGLISH));
      this.configuration.put(WoTSec.name, name);
      return (S) this;
    }

    public abstract T build();
  }

}
