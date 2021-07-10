package ch.unisg.ics.interactions.wot.td.clients;

import org.eclipse.californium.core.CoapObserveRelation;

/**
 * Wrapper for a control-handle of a CoAP observe relation.
 * <p>
 * An observe relation is established when sending
 * an asynchronous CoAP observe request, by using
 * {@link TDCoapRequest#establishRelation(TDCoAPHandler)} and
 * {@link TDCoapRequest#establishRelationAndWait(TDCoAPHandler)}.
 * The <code>TDCoapRequest</code> instance needs to use
 * a form for the sub-protocol "cov:observe".
 * </p>
 *
 * <p>
 * An observe relation can be cancelled reactively or
 * proactively through the API of <code>TDCoapObserveRelation</code>.
 * The relation can also be cancelled by executing a
 * <code>TDCoapRequest</code> with a form of operation
 * type "unobserveproperty".
 * </p>
 */
public class TDCoapObserveRelation {

  private final CoapObserveRelation observeRelation;

  protected TDCoapObserveRelation(CoapObserveRelation observeRelation) {
    this.observeRelation = observeRelation;
  }

  /**
   * Gets the current {@link TDCoapResponse}.
   *
   * @return the current notification wrapped in a <code>TDCoapResponse</code>
   */
  public TDCoapResponse getCurrent() {
    return new TDCoapResponse(observeRelation.getCurrent());
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
