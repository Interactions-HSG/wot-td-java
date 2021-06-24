package ch.unisg.ics.interactions.wot.td.clients;

import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;

public abstract class TDCoAPHandler implements CoapHandler {

  public abstract void onResponse(TDCoapResponse response);

  public abstract void onFail();

  @Override
  public void onLoad(CoapResponse response) {
    this.onResponse(new TDCoapResponse(response));
  }

  @Override
  public void onError() {
    this.onFail();
  }


}
