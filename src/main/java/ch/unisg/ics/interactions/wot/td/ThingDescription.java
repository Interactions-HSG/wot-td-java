package ch.unisg.ics.interactions.wot.td;

import ch.unisg.ics.interactions.wot.td.affordances.ActionAffordance;
import ch.unisg.ics.interactions.wot.td.affordances.EventAffordance;
import ch.unisg.ics.interactions.wot.td.affordances.PropertyAffordance;
import ch.unisg.ics.interactions.wot.td.io.InvalidTDException;
import ch.unisg.ics.interactions.wot.td.security.SecurityScheme;
import ch.unisg.ics.interactions.wot.td.vocabularies.WoTSec;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.util.ModelBuilder;

import java.util.*;
import java.util.stream.Collectors;

/**
 * An immutable representation of a <a href="https://www.w3.org/TR/wot-thing-description/">W3C Web of
 * Things Thing Description (TD)</a>. A <code>ThingDescription</code> is instantiated using a
 * <code>ThingDescription.Builder</code>.
 * <p>
 * The current version does not yet implement all the core vocabulary terms defined by the
 * W3C Recommendation.
 */
public class ThingDescription {
  private final String title;
  private final Set<SecurityScheme> security;
  private final Map<String, SecurityScheme> securityDefinitions;

  private final Optional<String> uri;
  private final Set<String> types;
  private final Optional<String> baseURI;

  private final List<PropertyAffordance> properties;
  private final List<ActionAffordance> actions;
  private final List<EventAffordance> events;

  private final Optional<Model> graph;

  protected ThingDescription(String title, Set<SecurityScheme> security, Map<String,
    SecurityScheme> securityDefinitions, Optional<String> uri, Set<String> types, Optional<String> baseURI,
                             List<PropertyAffordance> properties, List<ActionAffordance> actions,
                             List<EventAffordance> events, Optional<Model> graph) {

    if (title == null) {
      throw new InvalidTDException("The title of a Thing cannot be null.");
    }
    this.title = title;

    this.security = security;
    this.securityDefinitions = securityDefinitions;

    // Set up nosec security
    if (this.security.isEmpty()) {
      if (getFirstSecuritySchemeByType(WoTSec.NoSecurityScheme).isPresent()) {
        this.security.add(getFirstSecuritySchemeByType(WoTSec.NoSecurityScheme).get());
      } else {
        SecurityScheme nosec = SecurityScheme.getNoSecurityScheme();
        this.security.add(nosec);
        this.securityDefinitions.put("nosec", nosec);
      }
    }

    this.uri = uri;
    this.types = types;
    this.baseURI = baseURI;

    this.properties = properties;
    this.actions = actions;
    this.events = events;

    this.graph = graph;
  }

  public String getTitle() {
    return title;
  }

  public Set<SecurityScheme> getSecuritySchemes() {
    return security;
  }

  public Map<String, SecurityScheme> getSecurityDefinitions() {
    return securityDefinitions;
  }

  /**
   * Gets the {@link SecurityScheme} that matches a given security definition name.
   *
   * @param name the name of the security definition
   * @return an <code>Optional</code> with the security scheme (empty if not found)
   */
  public Optional<SecurityScheme> getSecuritySchemeByDefinition(String name) {
    if (securityDefinitions.containsKey(name)) {
      return Optional.of(securityDefinitions.get(name));
    }
    return Optional.empty();
  }

  /**
   * Gets the {@link SecurityScheme} that matches a given security scheme name.
   *
   * @param name the name of the security scheme
   * @return an <code>Optional</code> with the security scheme (empty if not found)
   */
  public Optional<SecurityScheme> getFirstSecuritySchemeByName(String name) {
    for (SecurityScheme securityScheme : securityDefinitions.values()) {

      if (securityScheme.getSchemeName().equals(name)) {
        return Optional.of(securityScheme);
      }
    }
    return Optional.empty();
  }

  /**
   * Gets the first {@link SecurityScheme} that matches a given semantic type.
   *
   * @param type the semantic type of the security scheme
   * @return an <code>Optional</code> with the security scheme (empty if not found)
   */
  public Optional<SecurityScheme> getFirstSecuritySchemeByType(String type) {
    for (SecurityScheme securityScheme : securityDefinitions.values()) {

      if (securityScheme.getSemanticTypes().contains(type)) {
        return Optional.of(securityScheme);
      }
    }
    return Optional.empty();
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

  /**
   * Gets a set with all the semantic types of the action affordances provided by the described
   * thing.
   *
   * @return The set of semantic types, can be empty.
   */
  public Set<String> getSupportedActionTypes() {
    Set<String> supportedActionTypes = new HashSet<String>();

    for (ActionAffordance action : actions) {
      supportedActionTypes.addAll(action.getSemanticTypes());
    }

    return supportedActionTypes;
  }

  /**
   * Gets a property affordance by name, which is specified using the <code>td:name</code> data
   * property defined by the TD vocabulary. Names are mandatory in JSON-based representations. If a
   * name is present, it is unique within the scope of a TD.
   *
   * @param name the name of the property affordance
   * @return an <code>Optional</code> with the property affordance (empty if not found)
   */
  public Optional<PropertyAffordance> getPropertyByName(String name) {
    for (PropertyAffordance property : properties) {
      String propertyName = property.getName();
      if (propertyName.equals(name)) {
        return Optional.of(property);
      }
    }

    return Optional.empty();
  }

  /**
   * Gets a list of property affordances that have a {@link ch.unisg.ics.interactions.wot.td.affordances.Form}
   * hypermedia control for the given operation type.
   * <p>
   * The current implementation supports two operation types for properties: <code>td:readProperty</code>
   * and <code>td:writeProperty</code>.
   *
   * @param operationType a string that captures the operation type
   * @return the list of property affordances
   */
  public List<PropertyAffordance> getPropertiesByOperationType(String operationType) {
    return properties.stream().filter(property -> property.hasFormWithOperationType(operationType))
      .collect(Collectors.toList());
  }

  /**
   * Gets the first {@link PropertyAffordance} annotated with a given semantic type.
   *
   * @param propertyType the semantic type, typically an IRI defined in some ontology
   * @return an <code>Optional</code> with the property affordance (empty if not found)
   */
  public Optional<PropertyAffordance> getFirstPropertyBySemanticType(String propertyType) {
    for (PropertyAffordance property : properties) {
      if (property.getSemanticTypes().contains(propertyType)) {
        return Optional.of(property);
      }
    }

    return Optional.empty();
  }

  /**
   * Gets an action affordance by name, which is specified using the <code>td:name</code> data
   * property defined by the TD vocabulary. Names are mandatory in JSON-based representations. If a
   * name is present, it is unique within the scope of a TD.
   *
   * @param name the name of the action affordance
   * @return an <code>Optional</code> with the action affordance (empty if not found)
   */
  public Optional<ActionAffordance> getActionByName(String name) {
    for (ActionAffordance action : actions) {
      String actionName = action.getName();
      if (actionName.equals(name)) {
        return Optional.of(action);
      }
    }

    return Optional.empty();
  }

  /**
   * Gets a list of action affordances that have a {@link ch.unisg.ics.interactions.wot.td.affordances.Form}
   * hypermedia control for the given operation type.
   * <p>
   * There is one operation type available actions: <code>td:invokeAction</code>. The API will be
   * simplified in future iterations.
   *
   * @param operationType a string that captures the operation type
   * @return the list of action affordances
   */
  public List<ActionAffordance> getActionsByOperationType(String operationType) {
    return actions.stream().filter(action -> action.hasFormWithOperationType(operationType))
      .collect(Collectors.toList());
  }

  /**
   * Gets the first {@link ActionAffordance} annotated with a given semantic type.
   *
   * @param actionType the semantic type, typically an IRI defined in some ontology
   * @return an <code>Optional</code> with the action affordance (empty if not found)
   */
  public Optional<ActionAffordance> getFirstActionBySemanticType(String actionType) {
    for (ActionAffordance action : actions) {
      if (action.getSemanticTypes().contains(actionType)) {
        return Optional.of(action);
      }
    }

    return Optional.empty();
  }

  /**
   * Gets an event affordance by name, which is specified using the <code>td:name</code> data
   * property defined by the TD vocabulary. Names are mandatory in JSON-based representations. If a
   * name is present, it is unique within the scope of a TD.
   *
   * @param name the name of the event affordance
   * @return an <code>Optional</code> with the event affordance (empty if not found)
   */
  public Optional<EventAffordance> getEventByName(String name) {
    for (EventAffordance event : events) {
      String eventName = event.getName();
      if (eventName.equals(name)) {
        return Optional.of(event);
      }
    }

    return Optional.empty();
  }

  /**
   * Gets a list of event affordances that have a {@link ch.unisg.ics.interactions.wot.td.affordances.Form}
   * hypermedia control for the given operation type.
   * <p>
   * The current implementation supports two operation types for properties: <code>td:subscribeEvent</code>
   * and <code>td:unsubscribeEvent</code>.
   *
   * @param operationType a string that captures the operation type
   * @return the list of event affordances
   */
  public List<EventAffordance> getEventsByOperationType(String operationType) {
    return events.stream().filter(event -> event.hasFormWithOperationType(operationType))
      .collect(Collectors.toList());
  }

  /**
   * Gets the first {@link EventAffordance} annotated with a given semantic type.
   *
   * @param eventType the semantic type, typically an IRI defined in some ontology
   * @return an <code>Optional</code> with the event affordance (empty if not found)
   */
  public Optional<EventAffordance> getFirstEventBySemanticType(String eventType) {
    for (EventAffordance event : events) {
      if (event.getSemanticTypes().contains(eventType)) {
        return Optional.of(event);
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

  public List<EventAffordance> getEvents() {
    return this.events;
  }

  public Optional<Model> getGraph() {
    return graph;
  }

  /**
   * Supported serialization formats -- currently only RDF serialization formats, namely Turtle and
   * JSON-LD 1.0. The version of JSON-LD currently supported is the one provided by RDF4J.
   */
  public enum TDFormat {
    RDF_TURTLE,
    RDF_JSONLD
  }

  /**
   * Helper class used to construct a <code>ThingDescription</code>. All TDs should have a mandatory
   * <code>title</code> field. In addition to the optional fields defined by the W3C Recommendation,
   * the <code>addGraph</code> method allows to add any other metadata as an RDF graph.
   * <p>
   * Implements a fluent API.
   */
  public static class Builder {
    private final String title;
    private final Set<SecurityScheme> security;
    private final HashMap<String, SecurityScheme> securityDefinitions;
    private final Set<String> types;
    private final List<PropertyAffordance> properties;
    private final List<ActionAffordance> actions;
    private final List<EventAffordance> events;
    private Optional<String> uri;
    private Optional<String> baseURI;
    private Optional<Model> graph;

    public Builder(String title) {
      this.title = title;
      this.security = new HashSet<SecurityScheme>();
      this.securityDefinitions = new HashMap<String, SecurityScheme>();

      this.uri = Optional.empty();
      this.baseURI = Optional.empty();
      this.types = new HashSet<String>();

      this.properties = new ArrayList<PropertyAffordance>();
      this.actions = new ArrayList<ActionAffordance>();
      this.events = new ArrayList<EventAffordance>();

      this.graph = Optional.empty();
    }

    public Builder addSecurityScheme(String name, SecurityScheme security, boolean applied) {
      if (applied) {
        this.security.add(security);
      }
      this.securityDefinitions.put(name, security);
      return this;
    }

    public Builder addSecurityScheme(String name, SecurityScheme security) {
      return addSecurityScheme(name, security, true);
    }

    public Builder addSecuritySchemes(Map<String, SecurityScheme> security, boolean applied) {
      if (applied) {
        this.security.addAll(security.values());
      }
      this.securityDefinitions.putAll(security);
      return this;
    }

    public Builder addSecuritySchemes(Map<String, SecurityScheme> security) {
      return addSecuritySchemes(security, true);
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

    public Builder addEvent(EventAffordance event) {
      this.events.add(event);
      return this;
    }

    public Builder addEvents(List<EventAffordance> events) {
      this.events.addAll(events);
      return this;
    }

    /**
     * Adds an RDF graph. If an RDF graph is already present, it will be merged with the new graph.
     *
     * @param graph the RDF graph to be added
     * @return this <code>Builder</code>
     */
    public Builder addGraph(Model graph) {
      if (this.graph.isPresent()) {
        this.graph.get().addAll(graph);
      } else {
        this.graph = Optional.of(graph);
      }

      return this;
    }

    /**
     * Convenience method used to add a single triple. If an RDF graph is already present, the triple
     * will be added to the existing graph.
     *
     * @param subject   the subject
     * @param predicate the predicate
     * @param object    the object
     * @return this <code>Builder</code>
     */
    public Builder addTriple(Resource subject, IRI predicate, Value object) {
      if (this.graph.isPresent()) {
        this.graph.get().add(subject, predicate, object);
      } else {
        this.graph = Optional.of(new ModelBuilder().add(subject, predicate, object).build());
      }

      return this;
    }

    /**
     * Constructs and returns a <code>ThingDescription</code>.
     *
     * @return the constructed <code>ThingDescription</code>
     */
    public ThingDescription build() {
      return new ThingDescription(title, security, securityDefinitions, uri, types, baseURI, properties, actions,
        events, graph);
    }
  }
}
