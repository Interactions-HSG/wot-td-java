package ch.unisg.ics.interactions.wot.td.affordances;

import java.util.List;
import java.util.Optional;

public class PropertyAffordance extends InteractionAffordance {
  
  private final boolean observable;
  
  private PropertyAffordance(boolean observable, Optional<String> title, List<String> types, 
      List<Form> forms) {
    super(title, types, forms);
    this.observable = observable;
  }
  
  public boolean isObservable() {
    return observable;
  }
  
  public static class Builder 
      extends InteractionAffordance.Builder<PropertyAffordance, PropertyAffordance.Builder> {

    private boolean observable;
    
    protected Builder(List<Form> forms) {
      super(forms);
      this.observable = false;
    }
    
    public Builder addObserve() {
      this.observable = true;
      return this;
    }
    
    @Override
    public PropertyAffordance build() {
      return new PropertyAffordance(observable, title, types, forms);
    }
  }
}
