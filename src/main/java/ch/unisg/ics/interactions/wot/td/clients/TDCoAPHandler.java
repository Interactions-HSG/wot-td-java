package ch.unisg.ics.interactions.wot.td.clients;

import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;

/**
 * An abstract class for reacting to responses from
 * asynchronous CoAP requests. A concrete <code>TDCoAPHandler</code>
 * instance needs to be used when using the methods
 * {@link TDCoapRequest#execute(TDCoAPHandler)} and
 * {@link TDCoapRequest#establishRelation(TDCoAPHandler)}.
 */
public abstract class TDCoAPHandler {

  private final CoapHandler coapHandler = new CoapHandler() {
    @Override
    public void onLoad(CoapResponse response) {
      handleLoad(new TDCoapResponse(response));
    }

    @Override
    public void onError() {
      handleError();
    }
  };

  /**
   * Invoked when a CoAP response or notification has arrived.
   *
   * @param response the response
   */
  public abstract void handleLoad(TDCoapResponse response);

  /**
   * Invoked when a request timeouts or has been rejected by the server.
   */
  public abstract void handleError();

  CoapHandler getCoapHandler() {
    return this.coapHandler;
  }

}
