package ch.unisg.ics.interactions.wot.td.affordances;

import ch.unisg.ics.interactions.wot.td.schemas.DataSchema;
import ch.unisg.ics.interactions.wot.td.vocabularies.TD;

import java.util.*;

public class PropertyAffordance extends InteractionAffordance {
  private final DataSchema schema;
  private final boolean observable;

  private PropertyAffordance(String name, DataSchema schema, boolean observable,
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

    private DataSchema schema;
    private boolean observable;

    public Builder(String name, List<Form> forms) {
      super(name, forms);

      for (Form form : this.forms) {
        if (form.getOperationTypes().isEmpty()) {
          form.addOperationType(TD.readProperty);
          form.addOperationType(TD.writeProperty);
        }
      }

      this.schema = DataSchema.getEmptySchema();
      this.observable = false;
    }

    public Builder(String name, Form form) {
      this(name, new ArrayList<>(Collections.singletonList(form)));
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
    public PropertyAffordance build() {
      return new PropertyAffordance(name, schema, observable, title, types, forms);
    }
  }
}
