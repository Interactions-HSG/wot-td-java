package ch.unisg.ics.interactions.wot.td.bindings;

import ch.unisg.ics.interactions.wot.td.affordances.Form;
import ch.unisg.ics.interactions.wot.td.schemas.DataSchema;

import java.util.Map;
import java.util.Optional;

public interface ProtocolBinding {

  String getProtocol();

  Optional<String> getDefaultMethod(String operationType);

  Optional<String> getDefaultSubProtocol(String operationType);

  Operation bind(Form form, String operationType);

  Operation bind(Form form, String operationType, Map<String, DataSchema> uriVariables, Map<String, Object> values);

}
