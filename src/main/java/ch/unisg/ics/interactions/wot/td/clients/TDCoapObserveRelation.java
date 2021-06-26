package ch.unisg.ics.interactions.wot.td.clients;

import org.eclipse.californium.core.CoapObserveRelation;

public class TDCoapObserveRelation {

  private final CoapObserveRelation observeRelation;

  public TDCoapObserveRelation(CoapObserveRelation observeRelation) {
    this.observeRelation = observeRelation;
  }

  public TDCoapResponse getCurrent() {
    return new TDCoapResponse(observeRelation.getCurrent());
  }

  public boolean isCanceled() {
    return observeRelation.isCanceled();
  }

  public void proactiveCancel() {
    observeRelation.proactiveCancel();
  }

  public void reactiveCancel() {
    observeRelation.reactiveCancel();
  }
}
