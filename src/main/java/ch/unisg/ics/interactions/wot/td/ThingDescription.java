package ch.unisg.ics.interactions.wot.td;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.eclipse.rdf4j.model.IRI;

import ch.unisg.ics.interactions.wot.td.affordances.ActionAffordance;
import ch.unisg.ics.interactions.wot.td.vocabularies.WoTSec;

/**
 * TODO: add javadoc
 * ThingDescription is immutable (hence the builder pattern)
 * can be extended
 * fields follow the W3C WoT spec: https://www.w3.org/TR/wot-thing-description/#thing
 * 
 * @author Andrei Ciortea
 *
 */
public class ThingDescription {
  // A human-readable title of the Thing (required)
  final private String title;
  final private Set<IRI> security;
  
  // Identifier of the Thing in form of a URI
  final private Optional<String> uri;
  // Semantic types of the Thing
  final private Set<String> types;
  // The base URI that is used for all relative URI references throughout a TD document
  final private Optional<String> baseURI;
  // All Action-based interaction affordances of the Thing
  final private List<ActionAffordance> actions;
  
  protected ThingDescription(String title, Set<IRI> security, Optional<String> uri, Set<String> types, 
      Optional<String> baseURI, List<ActionAffordance> actions) {
    
    this.title = title;
    
    if (security.isEmpty()) {
      security.add(WoTSec.NoSecurityScheme);
    }
    this.security = security;

    this.uri = uri;
    this.types = types;
    this.baseURI = baseURI;
    this.actions = actions;
  }
  
  public String getTitle() {
    return title;
  }
  
  public Set<IRI> getSecurity() {
    return security;
  }
  
  public Optional<String> getThingURI() {
    return uri;
  }
  
  public Set<String> getSemanticTypes() {
    return types;
  }
  
  public Optional<String> getBaseURI() {
    return baseURI;
  }
  
  public Set<String> getSupportedActionTypes() {
    Set<String> supportedActionTypes = new HashSet<String>();
    
    for (ActionAffordance action : actions) {
      supportedActionTypes.addAll(action.getTypes());
    }
    
    return supportedActionTypes;
  }
  
  // TODO: returns only the first action of a given type
  public Optional<ActionAffordance> getActionBySemanticType(String actionType) {
    for (ActionAffordance action : actions) {
      if (action.getTypes().contains(actionType)) {
        return Optional.of(action);
      }
    }
    
    return Optional.empty();
  }
  
  public List<ActionAffordance> getActions() {
    return this.actions;
  }
  
  public static class Builder {
    final private String title;
    final private Set<IRI> security;
    
    private Optional<String> uri;
    final private Set<String> types;
    
    private Optional<String> baseURI;
    final private List<ActionAffordance> actions;
    
    public Builder(String title) {
      this.title = title;
      this.security = new HashSet<IRI>();
      
      this.uri = Optional.empty();
      this.types= new HashSet<String>();
      
      this.baseURI = Optional.empty();
      this.actions = new ArrayList<ActionAffordance>();
    }
    
    public Builder addSecurity(IRI security) {
      this.security.add(security);
      return this;
    }
    
    public Builder addSecurity(Set<IRI> security) {
      this.security.addAll(security);
      return this;
    }
    
    public Builder addThingURI(String uri) {
      this.uri = Optional.of(uri);
      return this;
    }
    
    public Builder addBaseURI(String baseURI) {
      this.baseURI = Optional.of(baseURI);
      return this;
    }
    
    public Builder addSemanticType(String type) {
      this.types.add(type);
      return this;
    }
    
    public Builder addSemanticTypes(Set<String> thingTypes) {
      this.types.addAll(thingTypes);
      return this;
    }
    
    public Builder addAction(ActionAffordance action) {
      this.actions.add(action);
      return this;
    }
    
    public Builder addActions(List<ActionAffordance> actions) {
      this.actions.addAll(actions);
      return this;
    }
    
    public ThingDescription build() {
      return new ThingDescription(title, security, uri, types, baseURI, actions);
    }
    
  }
}
