package ch.unisg.ics.interactions.wot.td.affordances;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import ch.unisg.ics.interactions.wot.td.schemas.DataSchema;
import ch.unisg.ics.interactions.wot.td.vocabularies.TD;

public class PropertyAffordance extends InteractionAffordance {
  private final DataSchema schema;
  private final boolean observable;
  
  private PropertyAffordance(Optional<String> name, DataSchema schema, boolean observable, 
      Optional<String> title, List<String> types, List<Form> forms) {
    super(name, title, types, forms);
    this.schema = schema;
    this.observable = observable;
  }
  
  public DataSchema getDataSchema() {
    return schema;
  }
  
  public boolean isObservable() {
    return observable;
  }
  
  public static class Builder 
      extends InteractionAffordance.Builder<PropertyAffordance, PropertyAffordance.Builder> {

    private final DataSchema schema;
    private boolean observable;
    
    public Builder(DataSchema schema, List<Form> forms) {
      super(forms);
      
      for (Form form : this.forms) {
        if (form.getOperationTypes().isEmpty()) {
          form.addOperationType(TD.readProperty);
          form.addOperationType(TD.writeProperty);
        }
      }
      
      this.schema = schema;
      this.observable = false;
    }
    
    public Builder(DataSchema schema, Form form) {
      this(schema, new ArrayList<Form>(Arrays.asList(form)));
    }
    
    public Builder addObserve() {
      this.observable = true;
      return this;
    }
    
    @Override
    public PropertyAffordance build() {
      return new PropertyAffordance(name, schema, observable, title, types, forms);
    }
  }
}
