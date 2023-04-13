package ch.unisg.ics.interactions.wot.td.bindings.coap;

import ch.unisg.ics.interactions.wot.td.affordances.Form;
import ch.unisg.ics.interactions.wot.td.bindings.BaseOperation;
import ch.unisg.ics.interactions.wot.td.vocabularies.COV;
import ch.unisg.ics.interactions.wot.td.vocabularies.TD;
import com.google.gson.Gson;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Wrapper for constructing and executing a CoAP request based on a given <code>ThingDescription</code>.
 * When constructing the request, clients can set payloads that conform to a <code>DataSchema</code>.
 */
public class TDCoapOperation extends BaseOperation {
  private final static Logger LOGGER = Logger.getLogger(TDCoapOperation.class.getCanonicalName());

  private class TDCoapHandler implements CoapHandler {

    @Override
    public void onLoad(CoapResponse response) {
      TDCoapOperation.this.onResponse(new TDCoapResponse(response.advanced()));
    }

    @Override
    public void onError() {
      // TODO not if the server rejected the request (server error to be notified as response)?
      TDCoapOperation.this.onError();
    }

  }

  private final Request request;

  private final List<CoapClient> executors = new ArrayList<>();
  private final ReentrantLock executorsLock = new ReentrantLock();

  private final TDCoapHandler handler;

  private final String target;

  public TDCoapOperation(Form form, String operationType) {
    this.handler = new TDCoapHandler();
    this.target = form.getTarget();

    Optional<String> methodName = form.getMethodName(operationType);
    Optional<String> subProtocol = form.getSubProtocol(operationType);

    if (methodName.isPresent()) {
      this.request = new Request(CoAP.Code.valueOf(methodName.get()));
      this.request.setURI(this.target);
    } else {
      throw new IllegalArgumentException("No default binding for the given operation type: "
        + operationType);
    }

    if (subProtocol.isPresent() && subProtocol.get().equals(COV.observe)) {
      if (operationType.equals(TD.observeProperty)) {
        this.request.setObserve();
      }
      if (operationType.equals(TD.unobserveProperty)) {
        this.request.setObserveCancel();
      }
    }
    this.request.getOptions().setContentFormat(MediaTypeRegistry.parse(form.getContentType()));
  }

  public String getTarget() {
    return target;
  }

  @Override
  public void sendRequest() {
    CoapClient client = new CoapClient();
    client.advanced(handler, request);
    addExecutor(client);
  }

  public void shutdownExecutors() {
    try {
      executorsLock.lock();
      if (!executors.isEmpty()) {
        for (CoapClient client : executors) {
          client.shutdown();
        }
        executors.clear();
      }
    } finally {
      executorsLock.unlock();
    }
  }

  public TDCoapOperation addOption(String key, String value) {
    // TODO Support CoAP options e.g. for observation flag
    return null;
  }

  @Override
  protected void setBooleanPayload(Boolean value) {
    request.setPayload(String.valueOf(value));
  }

  @Override
  protected void setStringPayload(String value) {
    request.setPayload(String.valueOf(value));
  }

  @Override
  protected void setIntegerPayload(Long value) {
    request.setPayload(String.valueOf(value));
  }

  @Override
  protected void setNumberPayload(Double value) {
    request.setPayload(String.valueOf(value));
  }

  @Override
  protected void setObjectPayload(Map<String, Object> payload) {
    String body = new Gson().toJson(payload);
    request.setPayload(body);
  }

  @Override
  protected void setArrayPayload(List<Object> payload) {
    String body = new Gson().toJson(payload);
    request.setPayload(body);
  }

  public String getPayloadAsString() {
    return request.getPayloadString();
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("[TDCoapRequest] Method: " + request.getCode().name());

    try {
      builder.append(", Target: " + request.getURI());
      builder.append(", " + request.getOptions().toString());

      if (request.getPayload() != null) {
        builder.append(", Payload: " + request.getPayloadString());
      }
    } catch (UnsupportedOperationException e) {
      LOGGER.log(Level.WARNING, e.getMessage());
    }

    return builder.toString();
  }

  Request getRequest() {
    return this.request;
  }

  // TODO expose CoapObserveRelation if cov:observe declared in form

  private void addExecutor(CoapClient client) {
    try {
      executorsLock.lock();
      if (client != null && !executors.contains(client)) {
        executors.add(client);
      }
    } finally {
      executorsLock.unlock();
    }
  }

  private void removeExecutor(CoapClient client) {
    try {
      executorsLock.lock();
      if (client != null) {
        executors.remove(client);
      }
    } finally {
      executorsLock.unlock();
    }
  }

}
