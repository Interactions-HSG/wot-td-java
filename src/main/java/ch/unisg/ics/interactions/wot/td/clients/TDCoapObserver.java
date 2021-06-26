package ch.unisg.ics.interactions.wot.td.clients;

import org.eclipse.californium.core.coap.MessageObserverAdapter;
import org.eclipse.californium.core.coap.Response;

/**
 * An abstract observer class for wrapping a CoAP message observer adapter.
 * <p>
 * This class has the following methods which are by default empty:
 *   <ul>
 *     <li>{@link #handleResponse(TDCoapResponse)} handles the arrival of a {@link TDCoapResponse}
 *     <li>{@link #handleAcknowledgement()} handles message acknowledgement
 *     <li>{@link #handleReject()} handles message rejection
 *     <li>{@link #handleCancel()} handles message cancellation
 *     <li>{@link #handleTimeout()} handles timemout event
 *     </ul>
 * <p>
 *   An instance of a <code>TDCoapObserver</code> implementation can be
 *   registered to an instance of {@link TDCoapRequest}, using the request's
 *   {@link TDCoapRequest#addObserver(TDCoapObserver)} method.
 * <p>
 *   If the request is executed, the methods of this class will be invoked
 *   to handle events of the request's lifecycle.
 */
public abstract class TDCoapObserver {

  private final MessageObserverAdapter messageObserverAdapter = new MessageObserverAdapter() {
    @Override
    public void onResponse(Response response) {
      handleResponse(new TDCoapResponse(response));
    }

    @Override
    public void onAcknowledgement() {
      handleAcknowledgement();
    }

    @Override
    public void onReject() {
      handleReject();
    }

    @Override
    public void onCancel() {
      handleCancel();
    }

    @Override
    public void onTimeout() {
      handleTimeout();
    }
  };

  public void handleResponse(TDCoapResponse response) {
    // empty default implementation
  }

  public void handleAcknowledgement() {
    // empty default implementation
  }

  public void handleReject() {
    failed();
  }

  public void handleCancel() {
    // empty default implementation
  }

  public void handleTimeout() {
    failed();
  }

  protected void failed() {
    // empty default implementation
  }

  MessageObserverAdapter getMessageObserverAdapter() {
    return this.messageObserverAdapter;
  }

}




