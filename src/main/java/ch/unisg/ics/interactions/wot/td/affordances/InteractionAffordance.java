package ch.unisg.ics.interactions.wot.td.affordances;

import ch.unisg.ics.interactions.wot.td.schemas.DataSchema;

import javax.xml.crypto.Data;
import java.util.*;
import java.util.stream.Collectors;

/**
 * TODO: add javadoc
 *
 * @author Andrei Ciortea
 *
 */
public class InteractionAffordance {
  public static final String PROPERTY = "property";
  public static final String EVENT = "event";
  public static final String ACTION = "action";

  protected Optional<String> name;
  protected Optional<String> title;
  protected List<String> types;
  protected List<Form> forms;
  protected Optional<Map<String,DataSchema>> uriVariables;

  protected InteractionAffordance(Optional<String> name, Optional<String> title, List<String> types,
      List<Form> forms, Optional<Map<String,DataSchema>> uriVariables) {
    this.name = name;
    this.title = title;
    this.types = types;
    this.forms = forms;
    this.uriVariables = uriVariables;
  }

  public Optional<String> getName() {
    return name;
  }

  public Optional<String> getTitle() {
    return title;
  }

  public List<String> getSemanticTypes() {
    return types;
  }

  public List<Form> getForms() {
    return forms;
  }

  public Optional<Map<String, DataSchema>> getUriVariables() { return uriVariables; }

  public boolean hasFormWithOperationType(String operationType) {
    return !forms.stream().filter(form -> form.hasOperationType(operationType))
        .collect(Collectors.toList()).isEmpty();
  }

  public Optional<Form> getFirstFormForOperationType(String operationType) {
    if (hasFormWithOperationType(operationType)) {
      Form firstForm = forms.stream().filter(form -> form.hasOperationType(operationType))
          .collect(Collectors.toList()).get(0);

      return Optional.of(firstForm);
    }

    return Optional.empty();
  }

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

  /** Abstract builder for interaction affordances. */
  public static abstract class Builder<T extends InteractionAffordance, S extends Builder<T,S>> {
    protected Optional<String> name;
    protected Optional<String> title;
    protected List<String> types;
    protected List<Form> forms;
    protected Optional<Map<String,DataSchema>> uriVariables;

    protected Builder(Form form) {
      this(new ArrayList<Form>(Arrays.asList(form)));
    }

    protected Builder(List<Form> forms) {
      this.name = Optional.empty();
      this.title = Optional.empty();
      this.types = new ArrayList<String>();
      this.forms = forms;
      this.uriVariables = Optional.empty();
    }

    @SuppressWarnings("unchecked")
    public S addName(String name) {
      this.name = Optional.of(name);
      return (S) this;
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
    public S addForm(Form form) {
      this.forms.add(form);
      return (S) this;
    }

    @SuppressWarnings("unchecked")
    public S addForms(List<Form> forms) {
      this.forms.addAll(forms);
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

    /*
    if (this.uriVariables.isPresent()) {
        this.uriVariables.get().putAll(variables);
      }
      else {
        this.uriVariables = Optional.of(variables);
      }
      return (S) this;
    }
     */

    public abstract T build();
  }
}
