package ch.unisg.ics.interactions.wot.td.security;

import ch.unisg.ics.interactions.wot.td.vocabularies.WoTSec;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class BasicSecurityScheme extends TokenBasedSecurityScheme {

  protected BasicSecurityScheme(Map<String, Object> configuration, Set<String> semanticTypes, TokenLocation in,
                                Optional<String> name) {
    super(SecurityScheme.BASIC, configuration, semanticTypes, in, name);
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
      return new BasicSecurityScheme(configuration, semanticTypes, in, name);
    }
  }
}
