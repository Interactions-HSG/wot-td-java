package ch.unisg.ics.interactions.wot.td.interaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * TODO: add javadoc
 * 
 * @author Andrei Ciortea
 *
 */
public class Interaction {
  protected Optional<String> title;
  protected List<String> types;
  protected List<HTTPForm> forms;
  
  protected Interaction(Optional<String> title, List<String> types, List<HTTPForm> forms) {
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
  
  public List<HTTPForm> getForms() {
    return forms;
  }
  
  /** Abstract builder for interaction affordances, intended to be extended. */
  public static abstract class Builder<T extends Interaction, S extends Builder<T,S>> {
    protected Optional<String> title;
    protected List<String> types;
    protected List<HTTPForm> forms;
    
    protected Builder() {
      this.title = Optional.empty();
      this.types = new ArrayList<String>();
      this.forms = new ArrayList<HTTPForm>();
    }
    
    @SuppressWarnings("unchecked")
    public S addTitle(String title) {
      this.title = Optional.of(title);
      return (S) this;
    }
    
    @SuppressWarnings("unchecked")
    public S addType(String type) {
      this.types.add(type);
      return (S) this;
    }
    
    @SuppressWarnings("unchecked")
    public S addTypes(List<String> types) {
      this.types.addAll(types);
      return (S) this;
    }
    
    @SuppressWarnings("unchecked")
    public S addForm(HTTPForm form) {
      this.forms.add(form);
      return (S) this;
    }
    
    @SuppressWarnings("unchecked")
    public S addForms(List<HTTPForm> forms) {
      this.forms.addAll(forms);
      return (S) this;
    }
    
    public abstract T build();
  }
}
