package ch.unisg.ics.interactions.wot.td.templates;

import ch.unisg.ics.interactions.wot.td.schemas.DataSchema;

import java.util.*;

/**
 * TODO: add javadoc
 *
 * @author Jérémy Lemée
 */
public class ActionAffordanceTemplate extends InteractionAffordanceTemplate {

  // TODO: add safe, idempotent

  private ActionAffordanceTemplate(String name, Optional<String> title, List<String> types,
                                     Optional<Map<String,DataSchema>> uriVariables) {
    super(name, title, types, uriVariables);
  }




  public static class Builder
    extends InteractionAffordanceTemplate.Builder<ActionAffordanceTemplate, ActionAffordanceTemplate.Builder> {



    public Builder(String name) {
      super(name);
    }


    @Override
    public ActionAffordanceTemplate build() {
      return new ActionAffordanceTemplate(name, title, types, uriVariables);
    }
  }
}
