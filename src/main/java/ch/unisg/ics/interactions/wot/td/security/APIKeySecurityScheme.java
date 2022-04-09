package ch.unisg.ics.interactions.wot.td.security;

import ch.unisg.ics.interactions.wot.td.vocabularies.WoTSec;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class APIKeySecurityScheme extends TokenBasedSecurityScheme {

  protected APIKeySecurityScheme(Map<String, Object> configuration, Set<String> semanticTypes,
                                 TokenLocation in, Optional<String> name) {
    super(SecurityScheme.APIKEY, configuration, semanticTypes, in, name);
  }

  public static class Builder extends TokenBasedSecurityScheme.Builder<APIKeySecurityScheme,
    APIKeySecurityScheme.Builder> {

    public Builder() {
      this.name = Optional.empty();
      this.semanticTypes.add(WoTSec.APIKeySecurityScheme);
      this.addTokenLocation(TokenLocation.QUERY);
    }

    @Override
    public APIKeySecurityScheme build() {
      return new APIKeySecurityScheme(configuration, semanticTypes, in, name);
    }
  }
}
