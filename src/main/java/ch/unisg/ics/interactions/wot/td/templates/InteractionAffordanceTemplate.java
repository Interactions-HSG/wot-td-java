package ch.unisg.ics.interactions.wot.td.templates;

import ch.unisg.ics.interactions.wot.td.io.InvalidTDException;
import ch.unisg.ics.interactions.wot.td.schemas.DataSchema;

import java.util.*;
import java.util.stream.Collectors;

/**
 * TODO: add javadoc
 *
 * @author Andrei Ciortea
 */
public class InteractionAffordanceTemplate {
  public static final String PROPERTY = "property";
  public static final String EVENT = "event";
  public static final String ACTION = "action";

  protected final String name;
  protected Optional<String> title;
  protected List<String> types;

  protected Optional<Map<String, DataSchema>> uriVariables;

  protected InteractionAffordanceTemplate(String name, Optional<String> title, List<String> types,
                                   Optional<Map<String,DataSchema>> uriVariables) {
    if (name == null) {
      throw new InvalidTDException("The name of an affordance cannot be null.");
    }
    this.name = name;
    this.title = title;
    this.types = types;
    this.uriVariables = uriVariables;
  }

  public String getName() {
    return name;
  }

  public Optional<String> getTitle() {
    return title;
  }

  public List<String> getSemanticTypes() {
    return types;
  }




  public Optional<Map<String, DataSchema>> getUriVariables() { return uriVariables; }





  public boolean hasSemanticType(String type) {
    return this.types.contains(type);
  }

  public boolean hasOneSemanticType(List<String> types) {
    for (String type : types) {
      if (this.types.contains(type)) {
        return true;
      }
    }

    return false;
  }

  public boolean hasAllSemanticTypes(List<String> types) {
    for (String type : types) {
      if (!this.types.contains(type)) {
        return false;
      }
    }

    return true;
  }

  /**
   * Abstract builder for interaction affordances.
   */
  public static abstract class Builder<T extends InteractionAffordanceTemplate, S extends Builder<T, S>> {
    protected final String name;
    protected Optional<String> title;
    protected List<String> types;
    protected Optional<Map<String,DataSchema>> uriVariables;



    protected Builder(String name) {
      this.name = name;
      this.title = Optional.empty();
      this.types = new ArrayList<String>();
      this.uriVariables = Optional.empty();
    }

    @SuppressWarnings("unchecked")
    public S addTitle(String title) {
      this.title = Optional.of(title);
      return (S) this;
    }

    @SuppressWarnings("unchecked")
    public S addSemanticType(String type) {
      this.types.add(type);
      return (S) this;
    }

    @SuppressWarnings("unchecked")
    public S addSemanticTypes(List<String> types) {
      this.types.addAll(types);
      return (S) this;
    }



    @SuppressWarnings("unchecked")
    public S addUriVariable(String name,DataSchema dataSchema) throws IllegalArgumentException{
      if (dataSchema.getDatatype().equals(DataSchema.OBJECT) || dataSchema.getDatatype().equals(DataSchema.ARRAY)) {
        throw new IllegalArgumentException();
      } else {
        if (this.uriVariables.isPresent()) {
          this.uriVariables.get().put(name, dataSchema);
        } else {
          Map<String, DataSchema> map = new HashMap<>();
          this.uriVariables = Optional.of(map);
          this.uriVariables.get().put(name, dataSchema);
        }
        return (S) this;
      }
    }

    @SuppressWarnings("unchecked")
    public S addUriVariables(Map<String,DataSchema> variables) throws IllegalArgumentException{
      for (String key : variables.keySet()){
        addUriVariable(key,variables.get(key));

      }
      return (S) this;
    }

    public abstract T build();
  }
}
