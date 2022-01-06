package ch.unisg.ics.interactions.wot.td.bindings;

import ch.unisg.ics.interactions.wot.td.affordances.Form;

public interface ProtocolBinding {

  Operation bind(Form form, String operationType);

  // TODO add bind method with URI template variables

}
