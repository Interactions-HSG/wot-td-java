package ch.unisg.ics.interactions.wot.td.security;

import ch.unisg.ics.interactions.wot.td.vocabularies.WoTSec;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class BearerSecurityScheme extends TokenBasedSecurityScheme {

  private final Optional<String> authorization;
  private final String alg;
  private final String format;

  protected BearerSecurityScheme(Map<String, Object> configuration, Set<String> semanticTypes,
                                 TokenLocation in, Optional<String> name, Optional<String> authorization, String alg,
                                 String format) {
    super(SecurityScheme.BEARER, configuration, semanticTypes, in, name);
    this.authorization = authorization;
    this.alg = alg;
    this.format = format;
  }

  /**
   * Gets the URI of the authorization server.
   *
   * @return the URI
   */
  public Optional<String> getAuthorization() {
    return this.authorization;
  }

  /**
   * Gets Encoding, encryption, or digest algorithm (e.g.,
   * MD5, ES256, or ES512-256).
   *
   * @return the algorithm
   */
  public String getAlg() {
    return this.alg;
  }

  /**
   * Gets the format of the security authentication information
   * (e.g., jwt, cwt, jwe, or jws).
   *
   * @return the format
   */
  public String getFormat() {
    return this.format;
  }

  public static class Builder extends TokenBasedSecurityScheme.Builder<BearerSecurityScheme,
    BearerSecurityScheme.Builder> {

    private String alg;
    private String format;
    private Optional<String> authorization;

    public Builder() {
      this.authorization = Optional.empty();
      this.name = Optional.empty();
      this.semanticTypes.add(WoTSec.BearerSecurityScheme);
      this.addAlg("ES256");
      this.addFormat("jwt");
      this.addTokenLocation(TokenLocation.HEADER);
    }

    /**
     * Specifies the URI of the authorization server.
     *
     * @param authorization the URI
     * @return the builder
     */
    public BearerSecurityScheme.Builder addAuthorization(String authorization) {
      this.authorization = Optional.of(authorization);
      this.configuration.put(WoTSec.authorization, authorization);
      return this;
    }

    /**
     * Specifies the encoding, encryption, or digest algorithm (e.g.,
     * MD5, ES256, or ES512-256).
     *
     * @param alg the algorithm
     * @return the builder
     */
    public BearerSecurityScheme.Builder addAlg(String alg) {
      this.alg = alg;
      this.configuration.put(WoTSec.alg, alg);
      return this;
    }

    /**
     * Specifies the format of security authentication information
     * (e.g., jwt, cwt, jwe, or jws).
     *
     * @param format the format
     * @return the builder
     */
    public BearerSecurityScheme.Builder addFormat(String format) {
      this.format = format;
      this.configuration.put(WoTSec.format, format);
      return this;
    }

    @Override
    public BearerSecurityScheme build() {
      return new BearerSecurityScheme(configuration, semanticTypes, in, name, authorization, alg, format);
    }
  }

}
