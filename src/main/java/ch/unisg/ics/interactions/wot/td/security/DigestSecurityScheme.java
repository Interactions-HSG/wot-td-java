package ch.unisg.ics.interactions.wot.td.security;

import ch.unisg.ics.interactions.wot.td.io.InvalidTDException;
import ch.unisg.ics.interactions.wot.td.vocabularies.WoTSec;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class DigestSecurityScheme extends TokenBasedSecurityScheme {

  private final QualityOfProtection qop;

  protected DigestSecurityScheme(QualityOfProtection qop, TokenLocation in, Optional<String> name,
                                 Map<String, String> configuration, Set<String> semanticTypes) {
    super(in, name, SecurityScheme.DIGEST, configuration, semanticTypes);
    this.qop = qop;
  }

  /**
   * Gets the quality of protection, i.e. auth, or auth-int.
   *
   * @return the quality of protection
   */
  public QualityOfProtection getQoP() {
    return qop;
  }

  public enum QualityOfProtection {
    AUTH("AUTH"),
    AUTH_INT("AUTH-INT");

    private final String strValue;

    QualityOfProtection(String strValue) {
      this.strValue = strValue;
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

    public String toString() {
      return this.strValue;
    }
  }

  public static class Builder extends TokenBasedSecurityScheme.Builder<DigestSecurityScheme,
    DigestSecurityScheme.Builder> {

    private QualityOfProtection qop;

    public Builder() {
      this.qop = QualityOfProtection.AUTH;
      this.in = TokenLocation.HEADER;
      this.name = Optional.empty();
      this.configuration.put(WoTSec.in, in.toString().toLowerCase(Locale.ENGLISH));
      this.configuration.put(WoTSec.qop, qop.toString().toLowerCase(Locale.ENGLISH));
      this.semanticTypes.add(WoTSec.DigestSecurityScheme);
    }

    public DigestSecurityScheme.Builder addQoP(QualityOfProtection qop) {
      this.qop = qop;
      this.configuration.put(WoTSec.qop, qop.toString().toLowerCase(Locale.ENGLISH));
      return this;
    }

    /**
     * Specifies the security configuration, which can be used in security definitions
     * of a <code>Thing Description</code>.
     *
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

    @Override
    public DigestSecurityScheme build() {
      return new DigestSecurityScheme(qop, in, name, configuration, semanticTypes);
    }
  }
}
