package ch.unisg.ics.interactions.wot.td.bindings;

import ch.unisg.ics.interactions.wot.td.affordances.Form;
import ch.unisg.ics.interactions.wot.td.clients.UriTemplate;
import ch.unisg.ics.interactions.wot.td.schemas.DataSchema;

import java.util.Map;

abstract public class BaseProtocolBinding implements ProtocolBinding {

  @Override
  public Operation bind(Form form, String operationType, Map<String, DataSchema> uriVariables, Map<String, Object> values) {
    String target = new UriTemplate(form.getTarget()).createUri(uriVariables, values);

    Form instantiatedForm = new Form.Builder(target, form).build();
    return bind(instantiatedForm, operationType);
  }

}
