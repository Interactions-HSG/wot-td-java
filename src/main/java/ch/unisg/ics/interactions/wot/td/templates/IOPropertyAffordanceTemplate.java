package ch.unisg.ics.interactions.wot.td.templates;

import ch.unisg.ics.interactions.wot.td.affordances.ActionAffordance;
import ch.unisg.ics.interactions.wot.td.affordances.PropertyAffordance;
import ch.unisg.ics.interactions.wot.td.schemas.DataSchema;
import ch.unisg.ics.interactions.wot.td.vocabularies.TD;

import java.util.*;

public class IOPropertyAffordanceTemplate extends InteractionAffordanceTemplate implements Template {
  private final DataSchema schema;
  private final boolean observable;

  private IOPropertyAffordanceTemplate(String name, DataSchema schema, boolean observable,
                             Optional<String> title, List<String> types, Optional<Map<String,DataSchema>> uriVariables) {
    super(name, title, types,  uriVariables);
    this.schema = schema;
    this.observable = observable;
  }

  public DataSchema getDataSchema() {
    return schema;
  }

  public boolean isObservable() {
    return observable;
  }

  @Override
  public boolean isTemplateOf(Object obj) {
    boolean b = false;
    if ( obj instanceof PropertyAffordance){
      PropertyAffordance property = (PropertyAffordance) obj;
      if ( this.title.equals(property.getTitle()) && getName().equals(property.getName()) && getDataSchema().equals(property.getDataSchema()) && isObservable() == property.isObservable()){
        b = true;
      }
    }
    return b;
  }

  public static class Builder
    extends InteractionAffordanceTemplate.Builder<IOPropertyAffordanceTemplate, IOPropertyAffordanceTemplate.Builder> {

    private DataSchema schema;
    private boolean observable;

    public Builder(String name) {
      super(name);


      this.schema = DataSchema.getEmptySchema();
      this.observable = false;
    }


    public Builder addDataSchema(DataSchema schema) {
      this.schema = schema;
      return this;
    }

    public Builder addObserve() {
      this.observable = true;
      return this;
    }

    @Override
    public IOPropertyAffordanceTemplate build() {
      return new IOPropertyAffordanceTemplate(name, schema, observable, title, types, uriVariables);
    }
  }
}
