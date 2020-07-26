package ch.unisg.ics.interactions.wot.td.security;

import java.util.Optional;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;

import ch.unisg.ics.interactions.wot.td.vocabularies.WoTSec;

public abstract class SecurityScheme {
  
  public abstract String getSchemeType();
  
  public static Optional<SecurityScheme> fromRDF(String type, Model model, Resource node) {
    if (type.equals(WoTSec.NoSecurityScheme)) {
      return Optional.of(new NoSecurityScheme());
    }
    
    if (type.equals(WoTSec.APIKeySecurityScheme)) {
      return Optional.of(new APIKeySecurityScheme(model,node));
    }
    
    return Optional.empty();
  }
  
  public Model toRDF(Resource schemeId) {
    ModelBuilder builder = new ModelBuilder();
    builder.add(schemeId, RDF.TYPE, SimpleValueFactory.getInstance().createIRI(getSchemeType()));
    
    return builder.build();
  }
}
