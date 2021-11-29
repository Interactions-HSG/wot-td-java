package ch.unisg.ics.interactions.wot.td.security;
import ch.unisg.ics.interactions.wot.td.vocabularies.WoTSec;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class APIKeySecurityScheme extends TokenBasedSecurityScheme {

  protected APIKeySecurityScheme(TokenLocation in, Optional<String> name,
                                 Map<String, String> configuration, Set<String> semanticTypes) {
    super(in, name, SecurityScheme.APIKEY, configuration, semanticTypes);
  }

  public static class Builder extends TokenBasedSecurityScheme.Builder<APIKeySecurityScheme> {

    public Builder() {
      this.in = TokenLocation.QUERY;
      this.name = Optional.empty();
      this.configuration.put(WoTSec.in, in.toString().toLowerCase(Locale.ENGLISH));
      this.semanticTypes.add(WoTSec.APIKeySecurityScheme);
    }

    @Override
    public APIKeySecurityScheme build() {
      return new APIKeySecurityScheme(in, name, configuration, semanticTypes);
    }
  }
}
