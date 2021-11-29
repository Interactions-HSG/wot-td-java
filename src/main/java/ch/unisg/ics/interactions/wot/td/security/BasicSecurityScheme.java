package ch.unisg.ics.interactions.wot.td.security;

import ch.unisg.ics.interactions.wot.td.vocabularies.WoTSec;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class BasicSecurityScheme extends TokenBasedSecurityScheme {

  protected BasicSecurityScheme(TokenLocation in, Optional<String> name,
                                Map<String, String> configuration, Set<String> semanticTypes) {
    super(in, name, SecurityScheme.BASIC, configuration, semanticTypes);
  }

  public static class Builder extends TokenBasedSecurityScheme.Builder<BasicSecurityScheme,
    BasicSecurityScheme.Builder> {

    public Builder() {
      this.in = TokenLocation.HEADER;
      this.name = Optional.empty();
      this.configuration.put(WoTSec.in, in.toString().toLowerCase(Locale.ENGLISH));
      this.semanticTypes.add(WoTSec.BasicSecurityScheme);
    }

    @Override
    public BasicSecurityScheme build() {
      return new BasicSecurityScheme(in, name, configuration, semanticTypes);
    }
  }

}
