package ch.unisg.ics.interactions.wot.td.security;

import ch.unisg.ics.interactions.wot.td.io.InvalidTDException;
import ch.unisg.ics.interactions.wot.td.vocabularies.WoTSec;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class APIKeySecurityScheme extends SecurityScheme {

  private final TokenLocation in;
  private final Optional<String> name;
  protected APIKeySecurityScheme(TokenLocation in, Optional<String> name,
                                 Map<String, String> configuration, Set<String> semanticTypes) {
    super(SecurityScheme.APIKEY, configuration, semanticTypes);
    this.in = in;
    this.name = name;
  }

  public TokenLocation getTokenLocation() {
    return in;
  }

  public Optional<String> getTokenName() {
    return name;
  }

  public static class Builder extends SecurityScheme.Builder<APIKeySecurityScheme,
    APIKeySecurityScheme.Builder> {

    private TokenLocation in;
    private Optional<String> name;

    public Builder() {
      this.in = TokenLocation.QUERY;
      this.name = Optional.empty();
      this.configuration.put(WoTSec.in, in.toString().toLowerCase(Locale.ENGLISH));
      this.semanticTypes.add(WoTSec.APIKeySecurityScheme);
    }

    public APIKeySecurityScheme.Builder addTokenLocation(TokenLocation in) {
      this.in = in;
      this.configuration.put(WoTSec.in, in.toString().toLowerCase(Locale.ENGLISH));
      return this;
    }

    public APIKeySecurityScheme.Builder addTokenName(String name) {
      this.name = Optional.of(name);
      this.configuration.put(WoTSec.name, name);
      return this;
    }

    @Override
    public APIKeySecurityScheme.Builder addConfiguration(Map<String, String> configuration) {
      this.configuration.putAll(configuration);
      if (configuration.containsKey(WoTSec.in)) {
        try {
          addTokenLocation(TokenLocation.valueOf(configuration.get(WoTSec.in)
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

    public APIKeySecurityScheme.Builder addToken(TokenLocation in, String name) {
      this.in = in;
      this.name = Optional.of(name);
      this.configuration.put(WoTSec.in, in.toString().toLowerCase(Locale.ENGLISH));
      this.configuration.put(WoTSec.name, name);
      return this;
    }

    @Override
    public APIKeySecurityScheme build() {
      return new APIKeySecurityScheme(in, name, configuration, semanticTypes);
    }
  }
}
