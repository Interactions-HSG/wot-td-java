package ch.unisg.ics.interactions.wot.td.interaction;

import ch.unisg.ics.interactions.wot.td.affordances.Form;
import ch.unisg.ics.interactions.wot.td.schemas.DataSchema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class InteractionInput {

  public InteractionInput(Form form) {
    this.form = form;
    this.value = null;
    this.schema = null;
  }
  private final Object value;
  private final Form form;
  private final DataSchema schema;
}
