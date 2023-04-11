package ch.unisg.ics.interactions.wot.td.bindings.coap;

import ch.unisg.ics.interactions.wot.td.affordances.Form;
import ch.unisg.ics.interactions.wot.td.bindings.BaseProtocolBinding;
import ch.unisg.ics.interactions.wot.td.bindings.Operation;
import ch.unisg.ics.interactions.wot.td.vocabularies.COV;
import ch.unisg.ics.interactions.wot.td.vocabularies.TD;

import java.util.*;

public class TDCoapBinding extends BaseProtocolBinding {

  private final static String COAP_PROTOCOL = "CoAP";

  private final static Collection<String> SUPPORTED_SCHEMES = new HashSet<>();

  private final static Map<String, String> DEFAULT_METHODS = new HashMap<>();

  private final static Map<String, String> DEFAULT_SUBPROTOCOLS = new HashMap<>();

  static {
    SUPPORTED_SCHEMES.add("coap");
    SUPPORTED_SCHEMES.add("coaps");

    DEFAULT_METHODS.put(TD.readProperty, "GET");
    DEFAULT_METHODS.put(TD.observeProperty, "GET");
    DEFAULT_METHODS.put(TD.unobserveProperty, "GET");
    DEFAULT_METHODS.put(TD.writeProperty, "PUT");
    DEFAULT_METHODS.put(TD.invokeAction, "POST");
    DEFAULT_METHODS.put(TD.subscribeEvent, "GET");
    DEFAULT_METHODS.put(TD.unsubscribeEvent, "GET");

    DEFAULT_SUBPROTOCOLS.put(TD.observeProperty, COV.observe);
    DEFAULT_SUBPROTOCOLS.put(TD.unobserveProperty, COV.observe);
    DEFAULT_SUBPROTOCOLS.put(TD.subscribeEvent, COV.observe);
    DEFAULT_SUBPROTOCOLS.put(TD.unsubscribeEvent, COV.observe);
  }

  @Override
  public String getProtocol() {
    return COAP_PROTOCOL;
  }

  @Override
  public Collection<String> getSupportedSchemes() {
    return SUPPORTED_SCHEMES;
  }

  @Override
  public Optional<String> getDefaultMethod(String operationType) {
    if (DEFAULT_METHODS.containsKey(operationType)) return Optional.of(DEFAULT_METHODS.get(operationType));
    else return Optional.empty();
  }

  @Override
  public Optional<String> getDefaultSubProtocol(String operationType) {
    if (DEFAULT_SUBPROTOCOLS.containsKey(operationType)) return Optional.of(DEFAULT_SUBPROTOCOLS.get(operationType));
    else return Optional.empty();
  }

  @Override
  public Operation bind(Form form, String operationType) {
    return new TDCoapOperation(form, operationType);
  }

}
