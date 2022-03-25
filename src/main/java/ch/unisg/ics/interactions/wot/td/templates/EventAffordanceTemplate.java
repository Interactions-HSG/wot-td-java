package ch.unisg.ics.interactions.wot.td.templates;

import ch.unisg.ics.interactions.wot.td.schemas.DataSchema;
import ch.unisg.ics.interactions.wot.td.templates.InteractionAffordanceTemplate;
import ch.unisg.ics.interactions.wot.td.vocabularies.TD;

import java.util.*;

public class EventAffordanceTemplate extends InteractionAffordanceTemplate {

  private EventAffordanceTemplate(String name, Optional<String> title, List<String> types,
                                    Optional<Map<String,DataSchema>> uriVariables) {
    super(name, title, types, uriVariables);
  }


  public static class Builder
    extends InteractionAffordanceTemplate.Builder<EventAffordanceTemplate, EventAffordanceTemplate.Builder> {



    public Builder(String name) {
      super(name);

    }




    @Override
    public EventAffordanceTemplate build() {
      return new EventAffordanceTemplate(name, title, types, uriVariables);
    }
  }
}
