package ch.unisg.ics.interactions.wot.td.interaction;

import ch.unisg.ics.interactions.wot.td.schemas.DataSchema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class InteractionInput {
  private final Object value;
  private final DataSchema schema;
}
