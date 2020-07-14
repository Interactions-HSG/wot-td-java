package ch.unisg.ics.interactions.wot.td.affordances;

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

  protected String name;
  protected Optional<String> title;
  protected List<String> types;
  protected List<Form> forms;

  protected InteractionAffordance(String name, Optional<String> title, List<String> types, List<Form> forms) {
    this.name = name;
    this.title = title;
    this.types = types;
    this.forms = forms;
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

  public List<Form> getForms() {
    return forms;
  }

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
    protected String name;
    protected Optional<String> title;
    protected List<String> types;
    protected List<Form> forms;

    protected Builder(List<Form> forms) {
      this.name = generateName();
      this.title = Optional.empty();
      this.types = new ArrayList<String>();
      this.forms = forms;
    }

    protected Builder(Form form) {
      this(new ArrayList<Form>(Arrays.asList(form)));
    }

    private String generateName() {
      // FIXME specialized builders should be able to override name generation
      UUID uuid = UUID.randomUUID();
      long lsb = uuid.getLeastSignificantBits();
      long msb = uuid.getMostSignificantBits();

      return "_" + Long.toHexString(lsb) + Long.toHexString(msb);
    }

    public S addName(String name) {
      this.name = name;
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

    public abstract T build();
  }
}
