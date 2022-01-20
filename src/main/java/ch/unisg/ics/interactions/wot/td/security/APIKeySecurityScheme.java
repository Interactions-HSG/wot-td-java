package ch.unisg.ics.interactions.wot.td.security;

import ch.unisg.ics.interactions.wot.td.vocabularies.WoTSec;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class APIKeySecurityScheme extends TokenBasedSecurityScheme {

  protected APIKeySecurityScheme(TokenLocation in, Optional<String> name,
                                 Map<String, Object> configuration, Set<String> semanticTypes) {
    super(in, name, SecurityScheme.APIKEY, configuration, semanticTypes);
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
      return new APIKeySecurityScheme(in, name, configuration, semanticTypes);
    }
  }
}
