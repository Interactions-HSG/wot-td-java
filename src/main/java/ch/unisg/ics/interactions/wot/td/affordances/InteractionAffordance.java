package ch.unisg.ics.interactions.wot.td.affordances;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
  
  protected Optional<String> title;
  protected List<String> types;
  protected List<Form> forms;
  
  protected InteractionAffordance(Optional<String> title, List<String> types, List<Form> forms) {
    this.title = title;
    this.types = types;
    this.forms = forms;
  }
  
  public Optional<String> getTitle() {
    return title;
  }
  
  public List<String> getTypes() {
    return types;
  }
  
  public List<Form> getForms() {
    return forms;
  }
  
  /** Abstract builder for interaction affordances. */
  public static abstract class Builder<T extends InteractionAffordance, S extends Builder<T,S>> {
    protected Optional<String> title;
    protected List<String> types;
    protected List<Form> forms;
    
    protected Builder(List<Form> forms) {
      this.title = Optional.empty();
      this.types = new ArrayList<String>();
      this.forms = forms;
    }
    
    protected Builder(Form form) {
      this(new ArrayList<Form>(Arrays.asList(form)));
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
