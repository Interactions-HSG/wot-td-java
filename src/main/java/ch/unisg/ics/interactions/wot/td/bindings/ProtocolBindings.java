package ch.unisg.ics.interactions.wot.td.bindings;

import ch.unisg.ics.interactions.wot.td.affordances.Form;
import ch.unisg.ics.interactions.wot.td.bindings.coap.TDCoapBinding;
import ch.unisg.ics.interactions.wot.td.bindings.http.TDHttpBinding;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory class to generate generic operations from TD forms.
 */
public class ProtocolBindings {

  private static final Map<String, ProtocolBinding> registeredBindings = new HashMap<>();

  static {
    TDHttpBinding httpBinding = new TDHttpBinding();

    ProtocolBindings.registerBinding("http", httpBinding);
    ProtocolBindings.registerBinding("https", httpBinding);

    TDCoapBinding coapBinding = new TDCoapBinding();

    ProtocolBindings.registerBinding("coap", coapBinding);
    ProtocolBindings.registerBinding("coaps", coapBinding);
  }

  public static ProtocolBinding getBinding(Form form) {
    String scheme = getScheme(form.getTarget());

    if (!registeredBindings.containsKey(scheme)) throw new BindingNotFoundException();
    else return registeredBindings.get(scheme);
  }

  public static void registerBinding(String scheme, ProtocolBinding binding) {
    // TODO check binding isn't already registered
    registeredBindings.put(scheme, binding);
  }

  private static String getScheme(String uriOrTemplate) {
    int i = uriOrTemplate.indexOf(":");

    if (i < 0) return null;
    else return uriOrTemplate.substring(0, i);
  }

  private ProtocolBindings() {};

}
