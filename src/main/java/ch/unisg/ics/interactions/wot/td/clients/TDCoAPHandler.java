package ch.unisg.ics.interactions.wot.td.clients;

import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;

public abstract class TDCoAPHandler {

  private final CoapHandler coapHandler = new CoapHandler() {
    @Override
    public void onLoad(CoapResponse response) {
      onResponse(new TDCoapResponse(response));
    }

    @Override
    public void onError() {
      onFail();
    }
  };

  public abstract void onResponse(TDCoapResponse response);

  public abstract void onFail();

  CoapHandler getCoapHandler(){
    return this.coapHandler;
  }

}
