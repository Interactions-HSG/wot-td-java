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
    String httpBindingClass = TDHttpBinding.class.getName();

    ProtocolBindings.registerBinding("http", httpBindingClass);
    ProtocolBindings.registerBinding("https", httpBindingClass);

    String coapBindingClass = TDCoapBinding.class.getName();

    ProtocolBindings.registerBinding("coap", coapBindingClass);
    ProtocolBindings.registerBinding("coaps", coapBindingClass);
  }

  public static ProtocolBinding getBinding(Form form) {
    String scheme = getScheme(form.getTarget());

    if (!registeredBindings.containsKey(scheme)) throw new BindingNotFoundException();
    else return registeredBindings.get(scheme);
  }

  public static void registerBinding(String scheme, String bindingClass) throws BindingNotRegisteredException {
    if (registeredBindings.containsKey(scheme)) {
      // TODO log warning
    }

    for (Map.Entry<String, ProtocolBinding> entry : registeredBindings.entrySet()) {
      if (entry.getValue().getClass().getName().equals(bindingClass)) {
        // reuse existing instance of the given class
        registeredBindings.put(scheme, entry.getValue());
        return;
      }
    }

    try {
      ProtocolBinding binding = (ProtocolBinding) Class.forName(bindingClass).newInstance();
      registeredBindings.put(scheme, binding);
    } catch (Exception e) {
      throw new BindingNotRegisteredException(e);
    }
  }

  private static String getScheme(String uriOrTemplate) {
    int i = uriOrTemplate.indexOf(":");

    if (i < 0) return null;
    else return uriOrTemplate.substring(0, i);
  }

  private ProtocolBindings() {};

}
