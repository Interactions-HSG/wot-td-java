package ch.unisg.ics.interactions.wot.td.security;

import ch.unisg.ics.interactions.wot.td.vocabularies.WoTSec;

import java.util.Map;
import java.util.Set;

public class NoSecurityScheme extends SecurityScheme {

  protected NoSecurityScheme(Map<String, String> configuration, Set<String> semanticTypes) {
    super(SecurityScheme.NOSEC, configuration, semanticTypes);
  }

  public static class Builder extends SecurityScheme.Builder<NoSecurityScheme> {

    public Builder() {
      this.semanticTypes.add(WoTSec.NoSecurityScheme);
    }

    @Override
    public NoSecurityScheme build() {
      return new NoSecurityScheme(configuration, semanticTypes);
    }
  }
}
