package ch.unisg.ics.interactions.wot.td.clients;

import ch.unisg.ics.interactions.wot.td.affordances.Form;
import ch.unisg.ics.interactions.wot.td.schemas.ArraySchema;
import ch.unisg.ics.interactions.wot.td.schemas.DataSchema;
import ch.unisg.ics.interactions.wot.td.schemas.ObjectSchema;
import ch.unisg.ics.interactions.wot.td.vocabularies.COV;
import ch.unisg.ics.interactions.wot.td.vocabularies.TD;
import com.google.gson.Gson;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.elements.exception.ConnectorException;

import java.io.IOException;
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
public class TDCoapRequest {
  private final static Logger LOGGER = Logger.getLogger(TDHttpRequest.class.getCanonicalName());

  private final Form form;
  private final Request request;

  private final List<CoapClient> executors = new ArrayList<>();
  private final ReentrantLock executorsLock = new ReentrantLock();

  public TDCoapRequest(Form form, String operationType) {
    this.form = form;

    Optional<String> methodName = form.getMethodName(operationType);
    Optional<String> subProtocol = form.getSubProtocol(operationType);

    if (methodName.isPresent()) {
      this.request = new Request(CoAP.Code.valueOf(methodName.get()));
      this.request.setURI(form.getTarget());
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

  /**
   * Sends a synchronous CoAP request.
   *
   * @return the CoAP response
   * @throws IOException if any issue occurred
   */
  public TDCoapResponse execute() throws IOException {
    CoapClient client = new CoapClient();
    CoapResponse response = null;

    try {
      response = client.advanced(request);
    } catch (ConnectorException e) {
      throw new IOException(e);
    }
    addExecutor(client);
    return new TDCoapResponse(response.advanced());
  }

  /**
   * Sends an asynchronous CoAP request and invokes the specified
   * <code>TDCoAPHandler</code> each time a notification arrives.
   *
   * @param handler the Response handler
   */
  public void execute(TDCoapHandler handler) {
    CoapClient client = new CoapClient();
    client.advanced(handler.getCoapHandler(), request);
    addExecutor(client);
  }

  /**
   * Sends an asynchronous observe CoAP request and invokes the specified
   * <code>TDCoAPHandler</code> each time a notification arrives.
   *
   * @param handler the CoAP Response handler
   * @return the CoAP observe relation
   * @throws IllegalArgumentException if no form is found for the subprotocol "cov:observe"
   */
  public TDCoapObserveRelation establishRelation(TDCoapHandler handler) {

    if (!request.getOptions().hasObserve()) {
      throw new IllegalArgumentException("No form for subprotocol: " + COV.observe
        + "for the given operation type.");
    }

    CoapClient client = new CoapClient(form.getTarget());
    CoapObserveRelation relation = client.observe(request, handler.getCoapHandler());
    TDCoapObserveRelation establishedRelation = new TDCoapObserveRelation(relation);
    addExecutor(client);
    return establishedRelation;
  }

  /**
   * Sends a synchronous observe request and waits until it has been established
   * whereupon the specified CoAP handler is invoked when a notification arrives.
   *
   * @param handler the CoAP Response handler
   * @return the CoAP observe relation
   * @throws IllegalArgumentException if no form is found for the subprotocol "cov:observe"
   * @throws IOException              if any other issue occurred
   */
  public TDCoapObserveRelation establishRelationAndWait(TDCoapHandler handler) throws IOException {
    if (!request.getOptions().hasObserve()) {
      throw new IllegalArgumentException("No form for subprotocol: " + COV.observe + "for the given operation type.");
    }

    CoapObserveRelation relation;
    CoapClient client = new CoapClient(form.getTarget());
    try {
      relation = client.observeAndWait(request, handler.getCoapHandler());
    } catch (ConnectorException e) {
      throw new IOException(e);
    }
    addExecutor(client);
    return new TDCoapObserveRelation(relation);
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

  public TDCoapRequest addOption(String key, String value) {
    // TODO Support CoAP options e.g. for observation flag
    return null;
  }

  public TDCoapRequest setPrimitivePayload(DataSchema dataSchema, boolean value)
    throws IllegalArgumentException {
    if (dataSchema.getDatatype().equals(DataSchema.BOOLEAN)) {
      request.setPayload(String.valueOf(value));
    } else {
      throw new IllegalArgumentException("The payload's datatype does not match BooleanSchema "
        + "(payload datatype: " + dataSchema.getDatatype() + ")");
    }

    return this;
  }

  public TDCoapRequest setPrimitivePayload(DataSchema dataSchema, String value)
    throws IllegalArgumentException {
    if (dataSchema.getDatatype().equals(DataSchema.STRING)) {
      request.setPayload(String.valueOf(value));
    } else {
      throw new IllegalArgumentException("The payload's datatype does not match StringSchema "
        + "(payload datatype: " + dataSchema.getDatatype() + ")");
    }

    return this;
  }

  public TDCoapRequest setPrimitivePayload(DataSchema dataSchema, long value)
    throws IllegalArgumentException {
    if (dataSchema.getDatatype().equals(DataSchema.INTEGER)
      || dataSchema.getDatatype().equals(DataSchema.NUMBER)) {
      request.setPayload(String.valueOf(value));
    } else {
      throw new IllegalArgumentException("The payload's datatype does not match IntegerSchema or "
        + "NumberSchema (payload datatype: " + dataSchema.getDatatype() + ")");
    }

    return this;
  }

  public TDCoapRequest setPrimitivePayload(DataSchema dataSchema, double value)
    throws IllegalArgumentException {
    if (dataSchema.getDatatype().equals(DataSchema.NUMBER)) {
      request.setPayload(String.valueOf(value));
    } else {
      throw new IllegalArgumentException("The payload's datatype does not match NumberSchema "
        + "(payload datatype: " + dataSchema.getDatatype() + ")");
    }

    return this;
  }

  /**
   * Sets a payload of type <code>ObjectSchema</code>. The object payload is given as a map where:
   * <ul>
   * <li>a key is a string that represents either a semantic type or an object property name</li>
   * <li>a value can be a primitive, an object represented as a <code>Map&lt;String,Object&gt;</code>
   * (that is, a nested object), or an ordered list of values of type <code>List&lt;Object&gt;</code></li>
   * </ul>
   *
   * @param objectSchema schema to be used for validating the payload and constructing the body of
   *                     the request
   * @param payload      the actual payload
   * @return this <code>TDCoapRequest</code>
   */
  public TDCoapRequest setObjectPayload(ObjectSchema objectSchema, Map<String, Object> payload) {
    if (objectSchema.validate(payload)) {
      Map<String, Object> instance = objectSchema.instantiate(payload);
      String body = new Gson().toJson(instance);
      request.setPayload(body);
    }

    return this;
  }

  /**
   * Sets a payload of type <code>ArraySchema</code>. The payload is given as an ordered list of
   * values of type <code>List&lt;Object&gt;</code>. Values can be primitives, objects represented
   * as <code>Map&lt;String,Object&gt;</code>, or lists of values (that is, nested lists).
   *
   * @param arraySchema schema used for validating the payload and constructing the body of
   *                    the request
   * @param payload     the actual payload
   * @return this <code>TDCoapRequest</code>
   */
  public TDCoapRequest setArrayPayload(ArraySchema arraySchema, List<Object> payload) {
    if (arraySchema.validate(payload)) {
      String body = new Gson().toJson(payload);
      request.setPayload(body);
    }

    return this;
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
