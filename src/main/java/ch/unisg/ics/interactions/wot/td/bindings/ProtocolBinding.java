package ch.unisg.ics.interactions.wot.td.bindings;

import ch.unisg.ics.interactions.wot.td.affordances.Form;

import java.util.Optional;

public interface ProtocolBinding {

  String getProtocol();

  Optional<String> getDefaultMethod(String operationType);

  Optional<String> getDefaultSubProtocol(String operationType);

  Operation bind(Form form, String operationType);

  // TODO add bind method with URI template variables

}
