package ch.unisg.ics.interactions.wot.td.security;

import ch.unisg.ics.interactions.wot.td.io.InvalidTDException;
import ch.unisg.ics.interactions.wot.td.vocabularies.WoTSec;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class DigestSecurityScheme extends SecurityScheme {

  private final QualityOfProtection qop;
  private final TokenLocation in;
  private final Optional<String> name;

  protected DigestSecurityScheme(QualityOfProtection qop, TokenLocation in, Optional<String> name,
                                Map<String, String> configuration, Set<String> semanticTypes) {
    super(SecurityScheme.DIGEST, configuration, semanticTypes);
    this.qop = qop;
    this.in = in;
    this.name = name;
  }

  public enum QualityOfProtection {
    AUTH("AUTH"),
    AUTH_INT("AUTH-INT");

    private String strValue;

    QualityOfProtection(String strValue) { this.strValue = strValue; }

    public String toString() {
      return this.strValue;
    }

    public static QualityOfProtection fromString(String strValue) {
      for (QualityOfProtection qop : values()) {
        if (strValue.equals(qop.strValue)) {
          return qop;
        }
      }
      String msg = "No enum constant ch.unisg.ics.interactions.wot.td.security" +
        ".DigestSecurityScheme.QualityOfProtection." + strValue;
      throw new IllegalArgumentException(msg);
    }
  }

  public enum TokenLocation {
    HEADER, QUERY, BODY, COOKIE
  }

  /**
   * Gets the quality of protection, i.e. auth, or auth-int.
   * @return the quality of protection
   */
  public QualityOfProtection getQoP() {
    return qop;
  }

  /**
   * Gets the location of security authentication information. The location
   * must be one of those specified in the enum
   * {@link ch.unisg.ics.interactions.wot.td.security.DigestSecurityScheme.TokenLocation}, i.e.
   * header, query, body, or cookie
   * @return the location of security authentication information
   */
  public TokenLocation getTokenLocation() {
    return in;
  }

  /**
   * Gets the name for query, header, or cookie parameters.
   * @return the name of the token
   */
  public Optional<String> getTokenName() {
    return name;
  }

  public static class Builder extends SecurityScheme.Builder<DigestSecurityScheme,
    DigestSecurityScheme.Builder> {

    private QualityOfProtection qop;
    private TokenLocation in;
    private Optional<String> name;

    public Builder() {
      this.qop = QualityOfProtection.AUTH;
      this.in = TokenLocation.HEADER;
      this.name = Optional.empty();
      this.configuration.put(WoTSec.in, in.toString().toLowerCase(Locale.ENGLISH));
      this.configuration.put(WoTSec.qop, qop.toString().toLowerCase(Locale.ENGLISH));
      this.semanticTypes.add(WoTSec.DigestSecurityScheme);
    }

    /**
     * Specifies the location of security authentication information. The location
     * must be one of those specified in the enum
     * {@link ch.unisg.ics.interactions.wot.td.security.DigestSecurityScheme.TokenLocation}, i.e.
     * header, query, body, or cookie.
     * @param in the location of security authentication information
     */
    public DigestSecurityScheme.Builder addTokenLocation(TokenLocation in) {
      this.in = in;
      this.configuration.put(WoTSec.in, in.toString().toLowerCase(Locale.ENGLISH));
      return this;
    }

    /**
     * Specifies the name for query, header, or cookie parameters.
     * @param name the name of the token
     */
    public DigestSecurityScheme.Builder addTokenName(String name) {
      this.name = Optional.of(name);
      this.configuration.put(WoTSec.name, name);
      return this;
    }

    public DigestSecurityScheme.Builder addQoP(QualityOfProtection qop) {
      this.qop = qop;
      this.configuration.put(WoTSec.qop, qop.toString().toLowerCase(Locale.ENGLISH));
      return this;
    }

    /**
     * Specifies the security configuration, which can be used in security definitions
     * of a <code>Thing Description</code>.
     * @param configuration the security configuration
     */
    @Override
    public DigestSecurityScheme.Builder addConfiguration(Map<String, String> configuration) {
      this.configuration.putAll(configuration);
      if (configuration.containsKey(WoTSec.in)) {
        try {
          addTokenLocation(TokenLocation.valueOf(configuration.get(WoTSec.in)
            .toUpperCase(Locale.ENGLISH)));
        } catch (IllegalArgumentException e) {
          throw new InvalidTDException("Invalid token location", e);
        }
      }
      if (configuration.containsKey(WoTSec.qop)) {
        try {
          addQoP(QualityOfProtection.fromString(configuration.get(WoTSec.qop)
            .toUpperCase(Locale.ENGLISH)));
        } catch (IllegalArgumentException e) {
          throw new InvalidTDException("Invalid token location", e);
        }
      }
      if (configuration.containsKey(WoTSec.name)) {
        addTokenName(configuration.get(WoTSec.name));
      }
      return this;
    }

    /**
     * Specifies the values of the security configuration, which can be used in security definitions
     * of a <code>Thing Description</code>.
     * The location must be one of those specified in the enum <code>TokenLocation</code>, i.e.
     * header, query, body, or cookie.
     * @param in the name of the token
     * @param name the name of the token
     */
    public DigestSecurityScheme.Builder addToken(TokenLocation in, String name) {
      this.in = in;
      this.name = Optional.of(name);
      this.configuration.put(WoTSec.in, in.toString().toLowerCase(Locale.ENGLISH));
      this.configuration.put(WoTSec.name, name);
      return this;
    }

    @Override
    public DigestSecurityScheme build() {
      return new DigestSecurityScheme(qop, in, name, configuration, semanticTypes);
    }
  }
}
