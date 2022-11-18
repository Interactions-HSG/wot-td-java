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

    ProtocolBindings.registerBinding(httpBindingClass);
    ProtocolBindings.registerBinding(httpBindingClass);

    String coapBindingClass = TDCoapBinding.class.getName();

    ProtocolBindings.registerBinding(coapBindingClass);
    ProtocolBindings.registerBinding(coapBindingClass);
  }

  public static ProtocolBinding getBinding(Form form) throws BindingNotFoundException {
    String scheme = getScheme(form.getTarget());

    if (!registeredBindings.containsKey(scheme)) throw new BindingNotFoundException();
    else return registeredBindings.get(scheme);
  }

  public static void registerBinding(String bindingClass) throws BindingNotRegisteredException {
    for (Map.Entry<String, ProtocolBinding> entry : registeredBindings.entrySet()) {
      if (entry.getValue().getClass().getName().equals(bindingClass)) {
        // TODO warn that no change is performed
        return;
      }
    }

    Map<String, ProtocolBinding> newBindings = new HashMap<>();

    try {
      ProtocolBinding binding = (ProtocolBinding) Class.forName(bindingClass).newInstance();

      for (String scheme : binding.getSupportedSchemes()) {
        if (registeredBindings.containsKey(scheme)) {
          // TODO warn that bindings have conflict
        }

        newBindings.put(scheme, binding);
      }
    } catch (Exception e) {
      throw new BindingNotRegisteredException(e);
    }

    registeredBindings.putAll(newBindings);
  }

  private static String getScheme(String uriOrTemplate) {
    int i = uriOrTemplate.indexOf(":");

    if (i < 0) return null;
    else return uriOrTemplate.substring(0, i);
  }

  private ProtocolBindings() {};

}
