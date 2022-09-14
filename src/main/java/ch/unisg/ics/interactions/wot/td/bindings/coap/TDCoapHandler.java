package ch.unisg.ics.interactions.wot.td.bindings.coap;

import ch.unisg.ics.interactions.wot.td.bindings.ResponseCallback;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * CoAP handler reacting to notifications from asynchronous CoAP requests.
 * The handler calls registered callbacks whenever a response is received
 * and stores a reference to the response received last.
 */
class TDCoapHandler implements CoapHandler {

  /**
   * Semaphore to block getter if no response sent before call
   * (if an error occurs, an empty value is passed to the semaphore)
   */
  private BlockingDeque<Optional<TDCoapResponse>> lastResponse = new LinkedBlockingDeque<>(1);

  /**
   * Callbacks registered for the pending request
   */
  private Collection<ResponseCallback> callbacks = new LinkedList<>();

  public Optional<TDCoapResponse> getLastResponse() {
    return lastResponse.poll();
  }

  public void registerResponseCallback(ResponseCallback cb) {
    callbacks.add(cb);
  }

  public void unregisterResponseCallback(ResponseCallback cb) {
    callbacks.remove(cb);
  }

  @Override
  public void onLoad(CoapResponse response) {
    TDCoapResponse r = new TDCoapResponse(response.advanced());
    lastResponse.push(Optional.of(r));
    callbacks.forEach(cb -> cb.onResponse(r));
  }

  @Override
  public void onError() {
    lastResponse.push(Optional.empty());
  }

}
