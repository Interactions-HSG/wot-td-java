package ch.unisg.ics.interactions.wot.td.security;

import ch.unisg.ics.interactions.wot.td.vocabularies.WoTSec;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class DigestSecurityScheme extends TokenBasedSecurityScheme {

  private final QualityOfProtection qop;

  protected DigestSecurityScheme(Map<String, Object> configuration, Set<String> semanticTypes,
                                 TokenLocation in, Optional<String> name, QualityOfProtection qop) {
    super(SecurityScheme.DIGEST, configuration, semanticTypes, in, name);
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
    AUTH, AUTH_INT;

    @Override
    public String toString() {
      return name()
        .replace("_", "-")
        .toLowerCase(Locale.ENGLISH);
    }

    public static QualityOfProtection fromString(String value) {
      for(QualityOfProtection qop : values()) {
        if(qop.name().replace("_","-").equalsIgnoreCase(value)) {
          return qop;
        }
      }
      throw new IllegalArgumentException("No enum constant " + QualityOfProtection.class + " for string "
        + value.toUpperCase(Locale.ENGLISH));
    }
  }

  public static class Builder extends TokenBasedSecurityScheme.Builder<DigestSecurityScheme,
    DigestSecurityScheme.Builder> {

    private QualityOfProtection qop;

    public Builder() {
      this.name = Optional.empty();
      this.semanticTypes.add(WoTSec.DigestSecurityScheme);
      this.addQoP(QualityOfProtection.AUTH);
      this.addTokenLocation(TokenLocation.HEADER);
    }

    public DigestSecurityScheme.Builder addQoP(QualityOfProtection qop) {
      this.qop = qop;
      this.configuration.put(WoTSec.qop, qop);
      return this;
    }

    @Override
    public DigestSecurityScheme build() {
      return new DigestSecurityScheme(configuration, semanticTypes, in, name, qop);
    }
  }
}
