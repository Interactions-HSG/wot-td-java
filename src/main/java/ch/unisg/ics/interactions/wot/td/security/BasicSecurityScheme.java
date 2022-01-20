package ch.unisg.ics.interactions.wot.td.security;

import ch.unisg.ics.interactions.wot.td.vocabularies.WoTSec;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class BasicSecurityScheme extends TokenBasedSecurityScheme {

  protected BasicSecurityScheme(TokenLocation in, Optional<String> name,
                                Map<String, Object> configuration, Set<String> semanticTypes) {
    super(in, name, SecurityScheme.BASIC, configuration, semanticTypes);
  }

  public static class Builder extends TokenBasedSecurityScheme.Builder<BasicSecurityScheme,
    BasicSecurityScheme.Builder> {

    public Builder() {
      this.name = Optional.empty();
      this.semanticTypes.add(WoTSec.BasicSecurityScheme);
      this.addTokenLocation(TokenLocation.HEADER);
    }

    @Override
    public BasicSecurityScheme build() {
      return new BasicSecurityScheme(in, name, configuration, semanticTypes);
    }
  }

}
