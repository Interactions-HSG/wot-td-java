package ch.unisg.ics.interactions.wot.td.templates;

import ch.unisg.ics.interactions.wot.td.schemas.DataSchema;
import ch.unisg.ics.interactions.wot.td.vocabularies.TD;

import java.util.*;

public class PropertyAffordanceTemplate extends InteractionAffordanceTemplate {
  private final boolean observable;

  private PropertyAffordanceTemplate(String name, boolean observable,
                                       Optional<String> title, List<String> types, Optional<Map<String,DataSchema>> uriVariables) {
    super(name, title, types,  uriVariables);
    this.observable = observable;
  }


  public boolean isObservable() {
    return observable;
  }

  public static class Builder
    extends InteractionAffordanceTemplate.Builder<PropertyAffordanceTemplate, PropertyAffordanceTemplate.Builder> {


    private boolean observable;

    public Builder(String name) {
      super(name);


      this.observable = false;
    }

    public Builder addObserve() {
      this.observable = true;
      return this;
    }

    @Override
    public PropertyAffordanceTemplate build() {
      return new PropertyAffordanceTemplate(name, observable, title, types, uriVariables);
    }
  }
}
