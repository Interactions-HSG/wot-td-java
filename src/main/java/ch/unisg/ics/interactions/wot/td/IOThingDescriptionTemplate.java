package ch.unisg.ics.interactions.wot.td;

import ch.unisg.ics.interactions.wot.td.io.InvalidTDException;
import ch.unisg.ics.interactions.wot.td.templates.*;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.util.ModelBuilder;

import java.util.*;

public class IOThingDescriptionTemplate {

  private final String title;
  private final Set<String> types;

  private final List<IOPropertyAffordanceTemplate> properties;
  private final List<IOActionAffordanceTemplate> actions;
  private final List<IOEventAffordanceTemplate> events;

  private final Optional<Model> graph;

  protected IOThingDescriptionTemplate(String title,
                                     Set<String> types, List<IOPropertyAffordanceTemplate> properties,
                                     List<IOActionAffordanceTemplate> actions, List<IOEventAffordanceTemplate> events, Optional<Model> graph) {

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

  public Optional<IOPropertyAffordanceTemplate> getPropertyByName(String name) {
    for (IOPropertyAffordanceTemplate property : properties) {
      String propertyName = property.getName();
      if (propertyName.equals(name)) {
        return Optional.of(property);
      }
    }

    return Optional.empty();
  }

  public Optional<IOPropertyAffordanceTemplate> getFirstPropertyBySemanticType(String propertyType) {
    for (IOPropertyAffordanceTemplate property : properties) {
      if (property.getSemanticTypes().contains(propertyType)) {
        return Optional.of(property);
      }
    }

    return Optional.empty();
  }

  public Optional<IOActionAffordanceTemplate> getActionByName(String name) {
    for (IOActionAffordanceTemplate action : actions) {
      String actionName = action.getName();
      if (actionName.equals(name)) {
        return Optional.of(action);
      }
    }

    return Optional.empty();
  }

  public Optional<IOActionAffordanceTemplate> getFirstActionBySemanticType(String actionType) {
    for (IOActionAffordanceTemplate action : actions) {
      if (action.getSemanticTypes().contains(actionType)) {
        return Optional.of(action);
      }
    }

    return Optional.empty();
  }

  public Optional<IOEventAffordanceTemplate> getEventByName(String name) {
    for (IOEventAffordanceTemplate event : events) {
      String eventName = event.getName();
      if (eventName.equals(name)) {
        return Optional.of(event);
      }
    }

    return Optional.empty();
  }

  public Optional<IOEventAffordanceTemplate> getFirstEventBySemanticType(String eventType) {
    for (IOEventAffordanceTemplate event : events) {
      if (event.getSemanticTypes().contains(eventType)) {
        return Optional.of(event);
      }
    }

    return Optional.empty();
  }

  public List<IOPropertyAffordanceTemplate> getProperties() {
    return this.properties;
  }

  public List<IOActionAffordanceTemplate> getActions() {
    return this.actions;
  }

  public List<IOEventAffordanceTemplate> getEvents() {
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
    private final List<IOPropertyAffordanceTemplate> properties;
    private final List<IOActionAffordanceTemplate> actions;
    private final List<IOEventAffordanceTemplate> events;
    private Optional<Model> graph;

    public Builder(String title) {
      this.title = title;

      this.types = new HashSet<String>();

      this.properties = new ArrayList<IOPropertyAffordanceTemplate>();
      this.actions = new ArrayList<IOActionAffordanceTemplate>();
      this.events = new ArrayList<IOEventAffordanceTemplate>();

      this.graph = Optional.empty();
    }



    public IOThingDescriptionTemplate.Builder addSemanticType(String type) {
      this.types.add(type);
      return this;
    }

    public IOThingDescriptionTemplate.Builder addSemanticTypes(Set<String> thingTypes) {
      this.types.addAll(thingTypes);
      return this;
    }

    public IOThingDescriptionTemplate.Builder addProperty(IOPropertyAffordanceTemplate property) {
      this.properties.add(property);
      return this;
    }

    public IOThingDescriptionTemplate.Builder addProperties(List<IOPropertyAffordanceTemplate> properties) {
      this.properties.addAll(properties);
      return this;
    }

    public IOThingDescriptionTemplate.Builder addAction(IOActionAffordanceTemplate action) {
      this.actions.add(action);
      return this;
    }

    public IOThingDescriptionTemplate.Builder addActions(List<IOActionAffordanceTemplate> actions) {
      this.actions.addAll(actions);
      return this;
    }

    public IOThingDescriptionTemplate.Builder addEvent(IOEventAffordanceTemplate event) {
      this.events.add(event);
      return this;
    }

    public IOThingDescriptionTemplate.Builder addEvents(List<IOEventAffordanceTemplate> events) {
      this.events.addAll(events);
      return this;
    }

    /**
     * Adds an RDF graph. If an RDF graph is already present, it will be merged with the new graph.
     *
     * @param graph the RDF graph to be added
     * @return this <code>Builder</code>
     */
    public IOThingDescriptionTemplate.Builder addGraph(Model graph) {
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
    public IOThingDescriptionTemplate.Builder addTriple(Resource subject, IRI predicate, Value object) {
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
    public IOThingDescriptionTemplate build() {
      return new IOThingDescriptionTemplate(title, types, properties, actions, events, graph);
    }
  }
}
