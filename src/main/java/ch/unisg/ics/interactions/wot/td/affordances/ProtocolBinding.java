package ch.unisg.ics.interactions.wot.td.affordances;

import ch.unisg.ics.interactions.wot.td.vocabularies.COV;
import ch.unisg.ics.interactions.wot.td.vocabularies.TD;
import org.apache.commons.collections.map.MultiKeyMap;

import java.util.HashMap;
import java.util.Optional;

public final class ProtocolBinding {

  private static final HashMap<String, String> URI_SCHEMES = new HashMap<>();
  private static final MultiKeyMap DEFAULT_METHOD_BINDING = new MultiKeyMap();
  private static final MultiKeyMap DEFAULT_SUBPROTOCOL_BINDING = new MultiKeyMap();

  static {
    URI_SCHEMES.put("http:", "HTTP");
    URI_SCHEMES.put("https:", "HTTP");
    URI_SCHEMES.put("coap:", "CoAP");
    URI_SCHEMES.put("coaps:", "CoAP");

    DEFAULT_METHOD_BINDING.put("HTTP", TD.readProperty, "GET");
    DEFAULT_METHOD_BINDING.put("HTTP", TD.writeProperty, "PUT");
    DEFAULT_METHOD_BINDING.put("HTTP", TD.invokeAction, "POST");
    DEFAULT_METHOD_BINDING.put("CoAP", TD.readProperty, "GET");
    DEFAULT_METHOD_BINDING.put("CoAP", TD.writeProperty, "PUT");
    DEFAULT_METHOD_BINDING.put("CoAP", TD.invokeAction, "POST");
    DEFAULT_METHOD_BINDING.put("CoAP", TD.observeProperty, "GET");
    DEFAULT_METHOD_BINDING.put("CoAP", TD.unobserveProperty, "GET");
    DEFAULT_METHOD_BINDING.put("CoAP", TD.subscribeEvent, "GET");
    DEFAULT_METHOD_BINDING.put("CoAP", TD.unsubscribeEvent, "GET");

    DEFAULT_SUBPROTOCOL_BINDING.put("CoAP", TD.observeProperty, COV.observe);
    DEFAULT_SUBPROTOCOL_BINDING.put("CoAP", TD.unobserveProperty, COV.observe);
    DEFAULT_SUBPROTOCOL_BINDING.put("CoAP", TD.subscribeEvent, COV.observe);
    DEFAULT_SUBPROTOCOL_BINDING.put("CoAP", TD.unsubscribeEvent, COV.observe);
  }

  private ProtocolBinding() {
  }

  public static Optional<String> getDefaultMethod(String href, String operationType) {
    if (getProtocol(href).isPresent()) {
      String protocol = getProtocol(href).get();

      if (DEFAULT_METHOD_BINDING.containsKey(protocol, operationType)) {
        return Optional.of((String) DEFAULT_METHOD_BINDING.get(protocol, operationType));
      }
    }
    return Optional.empty();
  }

  public static Optional<String> getDefaultSubProtocol(String href, String operationType) {
    if (getProtocol(href).isPresent()) {
      String protocol = getProtocol(href).get();

      if (DEFAULT_SUBPROTOCOL_BINDING.containsKey(protocol, operationType)) {
        return Optional.of((String) DEFAULT_SUBPROTOCOL_BINDING.get(protocol, operationType));
      }
    }
    return Optional.empty();
  }

  static Optional<String> getProtocol(String href) {
    Optional<String> uriScheme = URI_SCHEMES.keySet()
      .stream()
      .filter(scheme -> href.contains(scheme))
      .findFirst();

    return uriScheme.map(URI_SCHEMES::get);
  }
}
