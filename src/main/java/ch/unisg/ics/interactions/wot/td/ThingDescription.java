package ch.unisg.ics.interactions.wot.td;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.util.ModelBuilder;

import ch.unisg.ics.interactions.wot.td.affordances.ActionAffordance;
import ch.unisg.ics.interactions.wot.td.affordances.PropertyAffordance;
import ch.unisg.ics.interactions.wot.td.security.NoSecurityScheme;
import ch.unisg.ics.interactions.wot.td.security.SecurityScheme;

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
  private final List<SecurityScheme> security;
  
  private final Optional<String> uri;
  private final Set<String> types;
  private final Optional<String> baseURI;
  
  private final List<PropertyAffordance> properties;
  private final List<ActionAffordance> actions;
  
  // Present if the TD was created with the TDGraphReader. The graph may contain RDF triples in
  // addition to the ones representation on the object model.
  private Optional<Model> graph;
  
  public enum TDFormat {
    RDF_TURTLE,
    RDF_JSONLD
  };
  
  protected ThingDescription(String title, List<SecurityScheme> security, Optional<String> uri, 
      Set<String> types, Optional<String> baseURI, List<PropertyAffordance> properties, 
      List<ActionAffordance> actions, Optional<Model> graph) {
    
    this.title = title;
    
    if (security.isEmpty()) {
      security.add(new NoSecurityScheme());
    }
    this.security = security;

    this.uri = uri;
    this.types = types;
    this.baseURI = baseURI;
    
    this.properties = properties;
    this.actions = actions;
    
    this.graph = graph;
  }
  
  public String getTitle() {
    return title;
  }
  
  public List<SecurityScheme> getSecuritySchemes() {
    return security;
  }
  
  public Optional<SecurityScheme> getSecuritySchemeByType(String type) {
    return security.stream().filter(security -> security.getSchemaType().equals(type)).findFirst();
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
  
  public Optional<PropertyAffordance> getProperty(String name) {
    for (PropertyAffordance property : properties) {
      if (property.getName().equals(name)) {
        return Optional.of(property);
      }
    }

    return Optional.empty();
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
  
  public Optional<Model> getGraph() {
    return graph;
  }
  
  public static class Builder {
    private final String title;
    private final List<SecurityScheme> security;
    
    private Optional<String> uri;
    private Optional<String> baseURI;
    private final Set<String> types;
    
    private final List<PropertyAffordance> properties;
    private final List<ActionAffordance> actions;
    
    private Optional<Model> graph;
    
    public Builder(String title) {
      this.title = title;
      this.security = new ArrayList<SecurityScheme>();
      
      this.uri = Optional.empty();
      this.baseURI = Optional.empty();
      this.types= new HashSet<String>();
      
      this.properties = new ArrayList<PropertyAffordance>();
      this.actions = new ArrayList<ActionAffordance>();
      
      this.graph = Optional.empty();
    }
    
    public Builder addSecurityScheme(SecurityScheme security) {
      this.security.add(security);
      return this;
    }
    
    public Builder addSecuritySchemes(List<SecurityScheme> security) {
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
    
    public Builder addGraph(Model graph) {
      if (this.graph.isPresent()) {
        this.graph.get().addAll(graph);
      } else {
        this.graph = Optional.of(graph);
      }
      
      return this;
    }
    
    public Builder addTriple(Resource subject, IRI predicate, Value object) {
      if (this.graph.isPresent()) {
        this.graph.get().add(subject, predicate, object);
      } else {
        this.graph = Optional.of(new ModelBuilder().add(subject, predicate, object).build());
      }
      
      return this;
    }
    
    public ThingDescription build() {
      return new ThingDescription(title, security, uri, types, baseURI, properties, actions, graph);
    }
  }
}
