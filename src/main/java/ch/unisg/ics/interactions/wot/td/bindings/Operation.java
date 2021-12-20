package ch.unisg.ics.interactions.wot.td.bindings;

import ch.unisg.ics.interactions.wot.td.schemas.DataSchema;

import java.io.IOException;

public interface Operation {

  void setPayload(DataSchema schema, Object payload);

  Response execute() throws IOException;

}
