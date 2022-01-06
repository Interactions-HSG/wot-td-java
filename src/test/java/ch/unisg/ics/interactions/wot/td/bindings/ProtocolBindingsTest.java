package ch.unisg.ics.interactions.wot.td.bindings;

import ch.unisg.ics.interactions.wot.td.affordances.Form;
import ch.unisg.ics.interactions.wot.td.bindings.coap.TDCoapRequest;
import ch.unisg.ics.interactions.wot.td.bindings.http.TDHttpRequest;
import ch.unisg.ics.interactions.wot.td.schemas.DataSchema;
import ch.unisg.ics.interactions.wot.td.vocabularies.TD;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class ProtocolBindingsTest {

  public static final String UNKNOWN_URI = "unknown://example.org/property";

  private static class DummyOperation implements Operation {

    @Override
    public void setPayload(DataSchema schema, Object payload) {
      // do nothing
    }

    @Override
    public Response execute() throws IOException {
      return null;
    }

  }

  @Test
  public void testHttpBinding() {
    Form f = new Form.Builder("http://example.org/action")
      .addOperationType(TD.invokeAction).build();

    Operation op = ProtocolBindings.bind(f, TD.invokeAction);
    assertEquals(op.getClass(), TDHttpRequest.class);
  }

  @Test
  public void testCoapBinding() {
    Form f = new Form.Builder("coap://example.org/action")
      .addOperationType(TD.invokeAction).build();

    Operation op = ProtocolBindings.bind(f, TD.invokeAction);
    assertEquals(op.getClass(), TDCoapRequest.class);
  }

  @Test(expected = BindingNotFoundException.class)
  public void testBindingNotFound() {
    Form f = new Form.Builder("unknown://example.org").build();

    ProtocolBindings.bind(f, TD.readProperty);
  }

  @Test
  public void testRegisteredBinding() {
    ProtocolBindings.registerBinding("unknown", (form, operationType) -> new DummyOperation());

    Form f = new Form.Builder(UNKNOWN_URI).build();

    Operation op = ProtocolBindings.bind(f, TD.readProperty);
    assertEquals(op.getClass(), DummyOperation.class);
  }

}
