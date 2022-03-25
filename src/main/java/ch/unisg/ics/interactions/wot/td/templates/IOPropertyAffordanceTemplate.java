package ch.unisg.ics.interactions.wot.td.templates;

import ch.unisg.ics.interactions.wot.td.schemas.DataSchema;
import ch.unisg.ics.interactions.wot.td.vocabularies.TD;

import java.util.*;

public class IOPropertyAffordanceTemplate extends InteractionAffordanceTemplate {
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
