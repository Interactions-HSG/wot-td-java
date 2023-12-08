package ch.unisg.ics.interactions.wot.td.bindings;

import ch.unisg.ics.interactions.wot.td.affordances.Form;
import ch.unisg.ics.interactions.wot.td.bindings.coap.TDCoapOperation;
import ch.unisg.ics.interactions.wot.td.bindings.http.TDHttpOperation;
import ch.unisg.ics.interactions.wot.td.schemas.DataSchema;
import ch.unisg.ics.interactions.wot.td.vocabularies.TD;
import org.junit.Test;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.*;

public class ProtocolBindingsTest {

  public static final String DUMMY_URI = "dummy://example.org/property";

  public static class DummyBinding extends BaseProtocolBinding {

    public static final String DUMMY_PROTOCOL = "Dummy";

    @Override
    public String getProtocol() {
      return DUMMY_PROTOCOL;
    }

    @Override
    public Collection<String> getSupportedSchemes() {
      Set<String> singleton = new HashSet<>();
      singleton.add("dummy");

      return singleton;
    }

    @Override
    public Optional<String> getDefaultMethod(String operationType) {
      return Optional.empty();
    }

    @Override
    public Optional<String> getDefaultSubProtocol(String operationType) {
      return Optional.empty();
    }

    @Override
    public Operation bind(Form form, String operationType) {
      return new DummyOperation();
    }

  }

  private static class DummyOperation implements Operation {

    @Override
    public Form getForm() {
      return null;
    }

    @Override
    public String getOperationType() {
      return "executeDummyOp";
    }

    @Override
    public void setPayload(DataSchema schema, Object payload) {
      // do nothing
    }

    @Override
    public void setPayload(Object payload) {
      // do nothing
    }

    @Override
    public void sendRequest() throws IOException {
      // do nothing
    }

    @Override
    public Response getResponse() throws NoResponseException {
      return null;
    }

    @Override
    public void registerResponseCallback(ResponseCallback callback) {
      // do nothing
    }

    @Override
    public void unregisterResponseCallback(ResponseCallback callback) {
      // do nothing
    }
  }

  @Test
  public void testHttpBinding() {
    Form f = new Form.Builder("http://example.org/action")
      .addOperationType(TD.invokeAction).build();

    ProtocolBinding b = ProtocolBindings.getBinding(f);
    Operation op = b.bind(f, TD.invokeAction);
    assertEquals(op.getClass(), TDHttpOperation.class);
  }

  @Test
  public void testCoapBinding() {
    Form f = new Form.Builder("coap://example.org/action")
      .addOperationType(TD.invokeAction).build();

    ProtocolBinding b = ProtocolBindings.getBinding(f);
    Operation op = b.bind(f, TD.invokeAction);
    assertEquals(op.getClass(), TDCoapOperation.class);
  }

  @Test(expected = BindingNotFoundException.class)
  public void testBindingNotFound() {
    Form f = new Form.Builder("unknown://example.org").build();

    ProtocolBinding b = ProtocolBindings.getBinding(f);
    b.bind(f, TD.readProperty);
  }

  @Test
  public void testRegisteredBinding() {
    ProtocolBindings.registerBinding(DummyBinding.class.getName());

    Form f = new Form.Builder(DUMMY_URI).build();

    ProtocolBinding b = ProtocolBindings.getBinding(f);

    assertEquals(f.getProtocol().get(), DummyBinding.DUMMY_PROTOCOL);

    Operation op = b.bind(f, TD.readProperty);
    assertEquals(op.getClass(), DummyOperation.class);
  }

}
