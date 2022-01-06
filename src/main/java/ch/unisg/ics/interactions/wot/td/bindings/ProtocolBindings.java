package ch.unisg.ics.interactions.wot.td.bindings;

import ch.unisg.ics.interactions.wot.td.affordances.Form;
import ch.unisg.ics.interactions.wot.td.bindings.coap.TDCoapRequest;
import ch.unisg.ics.interactions.wot.td.bindings.http.TDHttpRequest;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Factory class to generate generic operations from TD forms.
 */
public class ProtocolBindings {

  private static final Map<String, ProtocolBinding> registeredBindings = new HashMap<>();

  private static final ProtocolBinding httpBinding = (form, operationType) -> new TDHttpRequest(form, operationType);

  private static final ProtocolBinding coapBinding = (form, operationType) -> new TDCoapRequest(form, operationType);

  static {
    // register HTTP binding
    registerBinding("http", httpBinding);
    registerBinding("https", httpBinding);
    // register CoAP binding
    registerBinding("coap", coapBinding);
    registerBinding("coaps", coapBinding);
  }

  public static Operation bind(Form form, String operationType) {
    String scheme = URI.create(form.getTarget()).getScheme();

    if (registeredBindings.containsKey(scheme)) throw new BindingNotFoundException();

    ProtocolBinding b = registeredBindings.get(scheme);
    return b.bind(form, operationType);
  }

  public static void registerBinding(String scheme, ProtocolBinding binding) {
    // TODO check binding isn't already registered
    registeredBindings.put(scheme, binding);
  }

  private ProtocolBindings() {};

}
