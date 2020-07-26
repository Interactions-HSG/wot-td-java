package ch.unisg.ics.interactions.wot.td.security;

import java.util.Locale;
import java.util.Optional;

import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.Models;

import ch.unisg.ics.interactions.wot.td.io.InvalidTDException;
import ch.unisg.ics.interactions.wot.td.vocabularies.WoTSec;

public class APIKeySecurityScheme extends SecurityScheme {
  public enum TokenLocation {
    HEADER, QUERY, BODY, COOKIE
  }
  
  private final TokenLocation in;
  private final Optional<String> name;
  
  public APIKeySecurityScheme(Model model, Resource node) {
    ValueFactory rdf = SimpleValueFactory.getInstance();
    
    Optional<Literal> in = Models.objectLiteral(model.filter(node, rdf.createIRI(WoTSec.in), 
        null));
    if (in.isPresent()) {
      try {
        this.in = TokenLocation.valueOf(in.get().stringValue().toUpperCase(Locale.ENGLISH));
      } catch (IllegalArgumentException e) {
        throw new InvalidTDException("Invalid token location", e);
      }
    } else {
      this.in = TokenLocation.QUERY;
    }
    
    Optional<Literal> name = Models.objectLiteral(model.filter(node, rdf.createIRI(WoTSec.name), 
        null));
    if (name.isPresent()) {
      this.name = Optional.of(name.get().stringValue());
    } else {
      this.name = Optional.empty();
    }
  }
  
  public APIKeySecurityScheme() {
    this(null);
  }
  
  public APIKeySecurityScheme(String name) {
    this(TokenLocation.QUERY, name);
  }
  
  public APIKeySecurityScheme(TokenLocation in, String name) {
    this.in = in;
    
    if (name == null || name.isEmpty()) {
      this.name = Optional.empty();
    } else {
      this.name = Optional.of(name);
    }
  }
  
  public TokenLocation getIn() {
    return in;
  }
  
  public Optional<String> getName() {
    return name;
  }

  @Override
  public String getSchemeType() {
    return WoTSec.APIKeySecurityScheme;
  }
  
  @Override
  public Model toRDF(Resource schemeId) {
    Model model = super.toRDF(schemeId);
    
    ValueFactory rdf = SimpleValueFactory.getInstance(); 
    model.add(schemeId, rdf.createIRI(WoTSec.in), rdf.createLiteral(this.in.name()));
    
    if (this.name.isPresent()) {
      model.add(schemeId, rdf.createIRI(WoTSec.name), rdf.createLiteral(this.name.get()));
    }
    
    return model;
  }
}