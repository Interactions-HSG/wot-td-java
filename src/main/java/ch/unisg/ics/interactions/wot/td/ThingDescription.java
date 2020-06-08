package ch.unisg.ics.interactions.wot.td;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import ch.unisg.ics.interactions.wot.td.affordances.ActionAffordance;
import ch.unisg.ics.interactions.wot.td.affordances.PropertyAffordance;
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
  private final String title;
  private final Set<IRI> security;
  
  private final Optional<String> uri;
  private final Set<String> types;
  private final Optional<String> baseURI;
  
  private final List<PropertyAffordance> properties;
  private final List<ActionAffordance> actions;
  
  public enum TDFormat {
    RDF_TURTLE,
    RDF_JSONLD
  };
  
  protected ThingDescription(String title, Set<IRI> security, Optional<String> uri, Set<String> types, 
      Optional<String> baseURI, List<PropertyAffordance> properties, List<ActionAffordance> actions) {
    
    this.title = title;
    
    if (security.isEmpty()) {
      security.add(SimpleValueFactory.getInstance().createIRI(WoTSec.NoSecurityScheme));
    }
    this.security = security;

    this.uri = uri;
    this.types = types;
    this.baseURI = baseURI;
    
    this.properties = properties;
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
      supportedActionTypes.addAll(action.getSemanticTypes());
    }
    
    return supportedActionTypes;
  }
  
  public List<PropertyAffordance> getPropertiesByOperationType(String operationType) {
    return properties.stream().filter(property -> property.hasFormWithOperationType(operationType))
        .collect(Collectors.toList());
  }
  
  public Optional<PropertyAffordance> getFirstPropertyBySemanticType(String propertyType) {
    for (PropertyAffordance property : properties) {
      if (property.getSemanticTypes().contains(propertyType)) {
        return Optional.of(property);
      }
    }
    
    return Optional.empty();
  }
  
  public List<ActionAffordance> getActionsByOperationType(String operationType) {
    return actions.stream().filter(action -> action.hasFormWithOperationType(operationType))
        .collect(Collectors.toList());
  }
  
  public Optional<ActionAffordance> getFirstActionBySemanticType(String actionType) {
    for (ActionAffordance action : actions) {
      if (action.getSemanticTypes().contains(actionType)) {
        return Optional.of(action);
      }
    }
    
    return Optional.empty();
  }
  
  public List<PropertyAffordance> getProperties() {
    return this.properties;
  }
  
  public List<ActionAffordance> getActions() {
    return this.actions;
  }
  
  public static class Builder {
    private final String title;
    private final Set<IRI> security;
    
    private Optional<String> uri;
    private Optional<String> baseURI;
    private final Set<String> types;
    
    private final List<PropertyAffordance> properties;
    private final List<ActionAffordance> actions;
    
    public Builder(String title) {
      this.title = title;
      this.security = new HashSet<IRI>();
      
      this.uri = Optional.empty();
      this.baseURI = Optional.empty();
      this.types= new HashSet<String>();
      
      this.properties = new ArrayList<PropertyAffordance>();
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
    
    public Builder addProperty(PropertyAffordance property) {
      this.properties.add(property);
      return this;
    }
    
    public Builder addProperties(List<PropertyAffordance> properties) {
      this.properties.addAll(properties);
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
      return new ThingDescription(title, security, uri, types, baseURI, properties, actions);
    }
  }
}
