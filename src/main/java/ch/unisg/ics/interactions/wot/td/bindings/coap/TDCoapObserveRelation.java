package ch.unisg.ics.interactions.wot.td.bindings.coap;

import ch.unisg.ics.interactions.wot.td.bindings.Operation;
import org.eclipse.californium.core.CoapObserveRelation;

/**
 * TODO replace with TDCoapRequest which handle {@code unobserveProperty} and other cancellation operations.
 */
public class TDCoapObserveRelation {

  private final Operation operation;

  private final CoapObserveRelation observeRelation;

  protected TDCoapObserveRelation(CoapObserveRelation observeRelation, Operation op) {
    this.observeRelation = observeRelation;
    this.operation = op;
  }

  /**
   * Gets the current {@link TDCoapResponse}.
   *
   * @return the current notification wrapped in a <code>TDCoapResponse</code>
   */
  public TDCoapResponse getCurrent() {
    return new TDCoapResponse(observeRelation.getCurrentResponse(), operation);
  }

  /**
   * Checks if the observe relation has been canceled.
   *
   * @return true is the relation is canceled.
   */
  public boolean isCanceled() {
    return observeRelation.isCanceled();
  }

  /**
   * Cancels the observe relation, by sending a CoAP GET request
   * with an Observe Option set to 1.
   */
  public void proactiveCancel() {
    observeRelation.proactiveCancel();
  }

  /**
   * Cancels the observe relation, by "forgetting" the observation.
   * This will make the client to return a Reset message upon receiving
   * a new notification.
   */
  public void reactiveCancel() {
    observeRelation.reactiveCancel();
  }

  /**
   * Refreshes the observe relation by sending a CoAP GET request
   * with the same token and options.
   */
  public void reregister() {
    observeRelation.reregister();
  }
}
