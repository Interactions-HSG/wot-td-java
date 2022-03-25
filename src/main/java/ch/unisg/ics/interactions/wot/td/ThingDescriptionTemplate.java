package ch.unisg.ics.interactions.wot.td;

import ch.unisg.ics.interactions.wot.td.affordances.ActionAffordance;
import ch.unisg.ics.interactions.wot.td.affordances.EventAffordance;
import ch.unisg.ics.interactions.wot.td.affordances.PropertyAffordance;
import ch.unisg.ics.interactions.wot.td.io.InvalidTDException;
import ch.unisg.ics.interactions.wot.td.security.NoSecurityScheme;
import ch.unisg.ics.interactions.wot.td.security.SecurityScheme;
import ch.unisg.ics.interactions.wot.td.templates.ActionAffordanceTemplate;
import ch.unisg.ics.interactions.wot.td.templates.EventAffordanceTemplate;
import ch.unisg.ics.interactions.wot.td.templates.PropertyAffordanceTemplate;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.util.ModelBuilder;

import java.util.*;

public class ThingDescriptionTemplate {

  private final String title;
  private final Set<String> types;

  private final List<PropertyAffordanceTemplate> properties;
  private final List<ActionAffordanceTemplate> actions;
  private final List<EventAffordanceTemplate> events;

  private final Optional<Model> graph;

  protected ThingDescriptionTemplate(String title,
                             Set<String> types, List<PropertyAffordanceTemplate> properties,
                             List<ActionAffordanceTemplate> actions, List<EventAffordanceTemplate> events, Optional<Model> graph) {

    if (title == null) {
      throw new InvalidTDException("The title of a Thing cannot be null.");
    }
    this.title = title;

    this.types = types;

    this.properties = properties;
    this.actions = actions;
    this.events = events;

    this.graph = graph;
  }

  public String getTitle() {
    return title;
  }

  public Set<String> getSemanticTypes() {
    return types;
  }

  public Optional<PropertyAffordanceTemplate> getPropertyByName(String name) {
    for (PropertyAffordanceTemplate property : properties) {
      String propertyName = property.getName();
      if (propertyName.equals(name)) {
        return Optional.of(property);
      }
    }

    return Optional.empty();
  }

  public Optional<PropertyAffordanceTemplate> getFirstPropertyBySemanticType(String propertyType) {
    for (PropertyAffordanceTemplate property : properties) {
      if (property.getSemanticTypes().contains(propertyType)) {
        return Optional.of(property);
      }
    }

    return Optional.empty();
  }

  public Optional<ActionAffordanceTemplate> getActionByName(String name) {
    for (ActionAffordanceTemplate action : actions) {
      String actionName = action.getName();
      if (actionName.equals(name)) {
        return Optional.of(action);
      }
    }

    return Optional.empty();
  }

  public Optional<ActionAffordanceTemplate> getFirstActionBySemanticType(String actionType) {
    for (ActionAffordanceTemplate action : actions) {
      if (action.getSemanticTypes().contains(actionType)) {
        return Optional.of(action);
      }
    }

    return Optional.empty();
  }

  public Optional<EventAffordanceTemplate> getEventByName(String name) {
    for (EventAffordanceTemplate event : events) {
      String eventName = event.getName();
      if (eventName.equals(name)) {
        return Optional.of(event);
      }
    }

    return Optional.empty();
  }

  public Optional<EventAffordanceTemplate> getFirstEventBySemanticType(String eventType) {
    for (EventAffordanceTemplate event : events) {
      if (event.getSemanticTypes().contains(eventType)) {
        return Optional.of(event);
      }
    }

    return Optional.empty();
  }

  public List<PropertyAffordanceTemplate> getProperties() {
    return this.properties;
  }

  public List<ActionAffordanceTemplate> getActions() {
    return this.actions;
  }

  public List<EventAffordanceTemplate> getEvents() {
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

  public static class Builder {
    private final String title;
    private final Set<String> types;
    private final List<PropertyAffordanceTemplate> properties;
    private final List<ActionAffordanceTemplate> actions;
    private final List<EventAffordanceTemplate> events;
    private Optional<Model> graph;

    public Builder(String title) {
      this.title = title;

      this.types = new HashSet<String>();

      this.properties = new ArrayList<PropertyAffordanceTemplate>();
      this.actions = new ArrayList<ActionAffordanceTemplate>();
      this.events = new ArrayList<EventAffordanceTemplate>();

      this.graph = Optional.empty();
    }



    public ThingDescriptionTemplate.Builder addSemanticType(String type) {
      this.types.add(type);
      return this;
    }

    public ThingDescriptionTemplate.Builder addSemanticTypes(Set<String> thingTypes) {
      this.types.addAll(thingTypes);
      return this;
    }

    public ThingDescriptionTemplate.Builder addProperty(PropertyAffordanceTemplate property) {
      this.properties.add(property);
      return this;
    }

    public ThingDescriptionTemplate.Builder addProperties(List<PropertyAffordanceTemplate> properties) {
      this.properties.addAll(properties);
      return this;
    }

    public ThingDescriptionTemplate.Builder addAction(ActionAffordanceTemplate action) {
      this.actions.add(action);
      return this;
    }

    public ThingDescriptionTemplate.Builder addActions(List<ActionAffordanceTemplate> actions) {
      this.actions.addAll(actions);
      return this;
    }

    public ThingDescriptionTemplate.Builder addEvent(EventAffordanceTemplate event) {
      this.events.add(event);
      return this;
    }

    public ThingDescriptionTemplate.Builder addEvents(List<EventAffordanceTemplate> events) {
      this.events.addAll(events);
      return this;
    }

    /**
     * Adds an RDF graph. If an RDF graph is already present, it will be merged with the new graph.
     *
     * @param graph the RDF graph to be added
     * @return this <code>Builder</code>
     */
    public ThingDescriptionTemplate.Builder addGraph(Model graph) {
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
    public ThingDescriptionTemplate.Builder addTriple(Resource subject, IRI predicate, Value object) {
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
    public ThingDescriptionTemplate build() {
      return new ThingDescriptionTemplate(title, types, properties, actions, events, graph);
    }
  }
}
