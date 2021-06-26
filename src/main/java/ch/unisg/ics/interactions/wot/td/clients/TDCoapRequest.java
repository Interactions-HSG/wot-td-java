package ch.unisg.ics.interactions.wot.td.clients;

import ch.unisg.ics.interactions.wot.td.affordances.Form;
import ch.unisg.ics.interactions.wot.td.schemas.ArraySchema;
import ch.unisg.ics.interactions.wot.td.schemas.DataSchema;
import ch.unisg.ics.interactions.wot.td.schemas.ObjectSchema;
import ch.unisg.ics.interactions.wot.td.vocabularies.COV;
import com.google.gson.Gson;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.elements.exception.ConnectorException;

import java.io.IOException;
import java.util.*;
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

  private final HashMap<TDCoapObserveRelation, CoapClient> connectedClients = new HashMap<>();
  private final ReentrantLock connectedClientsLock = new ReentrantLock();

  public TDCoapRequest(Form form, String operationType) {
    this.form = form;

    Optional<String> methodName = form.getMethodName(operationType);

    if (methodName.isPresent()) {
      this.request = new Request(CoAP.Code.valueOf(methodName.get()));
      this.request.setURI(form.getTarget());
    } else {
      throw new IllegalArgumentException("No default binding for the given operation type: "
        + operationType);
    }

    if (form.getSubProtocol().isPresent() && form.getSubProtocol().get().equals(COV.observe)) {
      this.request.setObserve();
    }

    this.request.getOptions().setContentFormat(MediaTypeRegistry.parse(form.getContentType()));
  }

  /**
   * Sends an advanced synchronous CoAP request.
   *
   * @return a {@link ch.unisg.ics.interactions.wot.td.clients.TDCoapResponse}
   * @throws IOException if any issue occurred
   */
  public TDCoapResponse execute() throws IOException {
    CoapClient client = new CoapClient();
    CoapResponse response = null;

    try {
      response = client.advanced(request);
    } catch (ConnectorException e) {
      throw new IOException(e.getMessage());
    }
    client.shutdown();
    return new TDCoapResponse(response);
  }

  /**
   * Sends an advanced asynchronous CoAP request and invokes the specified
   * <code>TDCoAPHandler</code> each time a notification arrives.
   *
   * @param handler the Response handler
   */
  public void execute(TDCoAPHandler handler) {
    CoapClient client = new CoapClient();
    client.advanced(handler.getCoapHandler(), request);
  }

  /**
   * Sends an asynchronous observe CoAP request and invokes the specified
   * <code>TDCoAPHandler</code> each time a notification arrives.
   *
   * @param handler the CoAP Response handler
   * @return the CoAP observe relation
   * @throws IllegalArgumentException if no form is found for the subprotocol "cov:observe"
   */
  public TDCoapObserveRelation establishRelation(TDCoAPHandler handler) {
    // Exception needs be removed if it imposes an additional constraint
    // See editor`s note: https://www.w3.org/TR/wot-binding-templates/#coap-default-vocabulary-terms
    if (!form.getSubProtocol().isPresent() || !form.getSubProtocol().get().equals(COV.observe)) {
      throw new IllegalArgumentException("No form for subprotocol: " + COV.observe + "for the given operation type.");
    }

    CoapClient client = new CoapClient(form.getTarget());
    request.setObserve();
    CoapObserveRelation relation = client.observe(request, handler.getCoapHandler());
    TDCoapObserveRelation establishedRelation = new TDCoapObserveRelation(relation);
    addConnectedClient(establishedRelation, client);
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
  public TDCoapObserveRelation establishRelationAndWait(TDCoAPHandler handler) throws IOException {
    // Exception needs be removed if it imposes an additional constraint
    // See editor`s note: https://www.w3.org/TR/wot-binding-templates/#coap-default-vocabulary-terms
    if (!form.getSubProtocol().isPresent() || !form.getSubProtocol().get().equals(COV.observe)) {
      throw new IllegalArgumentException("No form for subprotocol: " + COV.observe + "for the given operation type.");
    }

    CoapClient client = new CoapClient(form.getTarget());
    request.setObserve();
    CoapObserveRelation relation = null;
    try {
      relation = client.observeAndWait(request, handler.getCoapHandler());
    } catch (ConnectorException e) {
      throw new IOException(e.getMessage());
    }
    TDCoapObserveRelation establishedRelation = new TDCoapObserveRelation(relation);
    addConnectedClient(establishedRelation, client);
    return establishedRelation;
  }

  public void shutdownClientForRelation(TDCoapObserveRelation relation) {
    removeConnectedClient(relation);
    connectedClients.get(relation).shutdown();
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

  private void addConnectedClient(TDCoapObserveRelation relation, CoapClient client) {
    try {
      connectedClientsLock.lock();
      if (relation != null) {
        connectedClients.put(relation, client);
      }
    } finally {
      connectedClientsLock.unlock();
    }
  }

  private void removeConnectedClient(TDCoapObserveRelation relation) {
    try {
      connectedClientsLock.lock();
      if (relation == null) {
        throw new NullPointerException();
      }
      if (!connectedClients.containsKey(relation)) {
        throw new NoSuchElementException();
      }
      connectedClients.remove(relation);
    } finally {
      connectedClientsLock.unlock();
    }
  }

}
