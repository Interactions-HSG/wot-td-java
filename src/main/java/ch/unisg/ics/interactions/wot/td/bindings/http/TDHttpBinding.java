package ch.unisg.ics.interactions.wot.td.bindings.http;

import ch.unisg.ics.interactions.wot.td.affordances.Form;
import ch.unisg.ics.interactions.wot.td.bindings.BaseProtocolBinding;
import ch.unisg.ics.interactions.wot.td.bindings.Operation;
import ch.unisg.ics.interactions.wot.td.vocabularies.TD;

import java.util.*;

public class TDHttpBinding extends BaseProtocolBinding {

  private final static String HTTP_PROTOCOL = "HTTP";

  private final static Collection<String> SUPPORTED_SCHEMES = new HashSet<>();

  private final static Map<String, String> DEFAULT_METHODS = new HashMap<>();

  static {
    SUPPORTED_SCHEMES.add("http");
    SUPPORTED_SCHEMES.add("https");

    DEFAULT_METHODS.put(TD.readProperty, "GET");
    DEFAULT_METHODS.put(TD.writeProperty, "PUT");
    DEFAULT_METHODS.put(TD.invokeAction, "POST");
  }

  @Override
  public String getProtocol() {
    return HTTP_PROTOCOL;
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
    return Optional.empty();
  }

  @Override
  public Operation bind(Form form, String operationType) {
    return new TDHttpOperation(form, operationType);
  }

}
