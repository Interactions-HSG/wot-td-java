package ch.unisg.ics.interactions.wot.td.bindings;

import ch.unisg.ics.interactions.wot.td.affordances.Form;
import ch.unisg.ics.interactions.wot.td.bindings.http.TDHttpRequest;
import ch.unisg.ics.interactions.wot.td.schemas.DataSchema;
import ch.unisg.ics.interactions.wot.td.schemas.StringSchema;
import ch.unisg.ics.interactions.wot.td.vocabularies.TD;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class BaseProtocolBindingTest {

  @Test
  public void testBindTemplate() {
    Form f = new Form.Builder("http://example.org/properties/{p}")
      .addOperationType(TD.readProperty)
      .build();

    ProtocolBinding b = ProtocolBindings.getBinding(f);

    Map<String, DataSchema> schemas = new HashMap<>();
    schemas.put("p", new StringSchema.Builder().build());
    Map<String, Object> values = new HashMap<>();
    values.put("p", "temp");

    TDHttpRequest op = (TDHttpRequest) b.bind(f, TD.readProperty, schemas, values);

    assertEquals("http://example.org/properties/temp", op.getTarget());
  }

}
