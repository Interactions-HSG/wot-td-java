package ch.unisg.ics.interactions.wot.td.bindings;

import ch.unisg.ics.interactions.wot.td.affordances.Form;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory class to generate generic operations from TD forms.
 */
public class ProtocolBindings {

  // TODO register HTTP binding
  private static List<ProtocolBinding> registeredBindings = new ArrayList<>();

  public static Operation bind(Form form, String operationType) {
    for (ProtocolBinding binding : registeredBindings) {
      try {
        return binding.bind(form, operationType);
      } catch (BindingNotFoundException e) {
        // move on to next binding
      }
    }

    throw new BindingNotFoundException();
  }

  public static void registerBinding(ProtocolBinding binding) {
    // TODO check binding isn't already registered
    registeredBindings.add(binding);
  }

}
