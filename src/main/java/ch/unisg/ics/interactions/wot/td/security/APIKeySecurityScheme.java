package ch.unisg.ics.interactions.wot.td.security;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.Models;

import ch.unisg.ics.interactions.wot.td.io.InvalidTDException;
import ch.unisg.ics.interactions.wot.td.vocabularies.WoTSec;

public class APIKeySecurityScheme extends TokenBasedSecurityScheme {

  protected APIKeySecurityScheme(TokenBasedSecurityScheme.TokenLocation in, Optional<String> name,
                                 Map<String, Object> configuration, Set<String> semanticTypes) {
    super(in, name, SecurityScheme.APIKEY, configuration, semanticTypes);
  }

  public static class Builder extends TokenBasedSecurityScheme.Builder<APIKeySecurityScheme,
    APIKeySecurityScheme.Builder> {

    public Builder() {
      this.name = Optional.empty();
      this.semanticTypes.add(WoTSec.APIKeySecurityScheme);
      this.addTokenLocation(TokenBasedSecurityScheme.TokenLocation.QUERY);
    }

    @Override
    public APIKeySecurityScheme build() {
      return new APIKeySecurityScheme(in, name, configuration, semanticTypes);
    }
  }
}
