package ch.unisg.ics.interactions.wot.td.security;

import ch.unisg.ics.interactions.wot.td.vocabularies.WoTSec;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class BearerSecurityScheme extends TokenBasedSecurityScheme {

  private final Optional<String> authorization;
  private final String alg;
  private final String format;

  protected BearerSecurityScheme(Optional<String> authorization, String alg,
                                 String format, TokenLocation in,
                                 Optional<String> name,
                                 Map<String, String> configuration, Set<String> semanticTypes) {
    super(in, name, SecurityScheme.BEARER, configuration, semanticTypes);
    this.authorization = authorization;
    this.alg = alg;
    this.format = format;
  }

  public Optional<String> getAuthorization() {
    return this.authorization;
  }

  public String getAlg() {
    return this.alg;
  }

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

    public BearerSecurityScheme.Builder addAuthorization(String authorization) {
      this.authorization = Optional.of(authorization);
      this.configuration.put(WoTSec.authorization, authorization);
      return this;
    }

    public BearerSecurityScheme.Builder addAlg(String alg) {
      this.alg = alg;
      this.configuration.put(WoTSec.alg, alg);
      return this;
    }

    public BearerSecurityScheme.Builder addFormat(String format) {
      this.format = format;
      this.configuration.put(WoTSec.format, format);
      return this;
    }

    /**
     * Specifies the security configuration, which can be used in security definitions
     * of a <code>Thing Description</code>.
     *
     * @param configuration the security configuration
     * @return the builder
     */
    @Override
    public BearerSecurityScheme.Builder addConfiguration(Map<String, String> configuration) {
      super.addConfiguration(configuration);
      if (configuration.containsKey(WoTSec.authorization)) {
        this.addAuthorization(configuration.get(WoTSec.authorization));
      }
      if (configuration.containsKey(WoTSec.alg)) {
        this.addAlg(configuration.get(WoTSec.alg));
      }
      if (configuration.containsKey(WoTSec.format)) {
        this.addFormat(configuration.get(WoTSec.format));
      }
      return this;
    }

    @Override
    public BearerSecurityScheme build() {
      return new BearerSecurityScheme(authorization, alg, format, in, name, configuration,
        semanticTypes);
    }
  }

}
