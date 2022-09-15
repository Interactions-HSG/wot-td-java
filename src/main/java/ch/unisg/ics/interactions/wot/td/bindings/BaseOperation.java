package ch.unisg.ics.interactions.wot.td.bindings;

import ch.unisg.ics.interactions.wot.td.schemas.DataSchema;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of basic operation features, including:
 * <ul>
 *   <li>validation of JSON payload</li>
 *   <li>management of asynchronous calls and blocking calls</li>
 * </ul>
 */
public abstract class BaseOperation implements Operation {

  /**
   * Default value for {@link BaseOperation#timeout}
   */
  public static final long DEFAULT_TIMEOUT = 60l;

  /**
   * Semaphore to block getter if no response sent before call
   * (if an error occurs, an empty value is passed to the semaphore)
   */
  private BlockingDeque<Optional<Response>> lastResponse = new LinkedBlockingDeque<>(1);

  /**
   * Callbacks registered for the pending request
   */
  private Collection<ResponseCallback> callbacks = new LinkedList<>();

  /**
   * Response timeout (in seconds): after request was sent,
   * the Thing has {@code timeout} seconds to send a response
   */
  private long timeout = DEFAULT_TIMEOUT;

  /**
   * Set timeout between request and (first) response.
   *
   * @param timeout timeout (in seconds)
   */
  public void setTimeout(long timeout) {
    this.timeout = timeout;
  }

  /**
   * Validate the given payload against its schema and call an internal method to set the payload
   * ({@link BaseOperation#setPayload(Object)}).
   */
  @Override
  public void setPayload(DataSchema schema, Object payload) {
    if (!validatePayload(schema, payload)) {
      String msg = String.format("Given payload is of type %s but expected %s", payload.getClass(), schema.getDatatype());
      throw new IllegalArgumentException(msg);
    }

    setPayload(payload);
  }

  /**
   * Implementations are protocol binding-dependent.
   * See {@link Operation#sendRequest()} for expected behavior.
   */
  @Override
  public abstract void sendRequest() throws IOException;

  /**
   * Use a semaphore ({@link BlockingDeque} of size 1) to implement
   * the expected behavior of {@link Operation#getResponse()}.
   */
  @Override
  public Response getResponse() throws NoResponseException {
    try {
      Optional<Response> r = lastResponse.poll(timeout, TimeUnit.SECONDS);

      if (r != null && r.isPresent()) return r.get();
      else throw new NoResponseException();
    } catch (InterruptedException e) {
      throw new NoResponseException(e);
    }
  }

  @Override
  public void registerResponseCallback(ResponseCallback callback) {
    callbacks.add(callback);
  }

  @Override
  public void unregisterResponseCallback(ResponseCallback callback) {
    callbacks.remove(callback);
  }

  protected boolean validatePayload(DataSchema schema, Object payload) {
    // TODO use schema.validate() instead
    switch (schema.getDatatype()) {
      case DataSchema.OBJECT: return payload instanceof Map;
      case DataSchema.ARRAY: return payload instanceof List;
      case DataSchema.STRING: return payload instanceof String;
      case DataSchema.BOOLEAN: return payload instanceof Boolean;
      case DataSchema.INTEGER: return payload instanceof Integer;
      case DataSchema.NUMBER: return payload instanceof Double;
      case DataSchema.EMPTY: return true;
      default: return false;
    }
  }

  /**
   * Check the type of the input payload and defer setting the payload to methods with typed signatures
   * corresponding each to a particular JSON value type:
   * <ul>
   *   <li>{@link BaseOperation#setArrayPayload(List)}</li>
   *   <li>{@link BaseOperation#setObjectPayload(Map)}</li>
   *   <li>{@link BaseOperation#setStringPayload(String)}</li>
   *   <li>...</li>
   * </ul>
   * These methods are to be implemented per protocol binding. If a protocol binding allows arbitrary payloads,
   * {@code setPayload(Object)} may be overridden as well.
   *
   * @param payload a payload expected to be equivalent to a JSON value (object, array, string, ...)
   */
  protected void setPayload(Object payload) {
    if (payload instanceof Map) setObjectPayload((Map<String, Object>) payload);
    else if (payload instanceof List) setArrayPayload((List<Object>) payload);
    else if (payload instanceof String) setStringPayload((String) payload);
    else if (payload instanceof Boolean) setBooleanPayload((Boolean) payload);
    else if (payload instanceof Long) setIntegerPayload((Long) payload);
    else if (payload instanceof Double) setNumberPayload((Double) payload);
    else throw new IllegalArgumentException(String.format("Given payload type isn't supported: %s", payload.getClass()));
  }

  protected abstract void setObjectPayload(Map<String, Object> payload);

  protected abstract void setArrayPayload(List<Object> payload);

  protected abstract void setStringPayload(String payload);

  protected abstract void setBooleanPayload(Boolean payload);

  protected abstract void setIntegerPayload(Long payload);

  protected abstract void setNumberPayload(Double payload);

  /**
   * Pass the input response to the semaphore and notify registered callbacks.
   *
   * @param r a response received by the Thing during the operation
   */
  protected void onResponse(Response r) {
    lastResponse.clear();

    lastResponse.push(Optional.of(r));
    callbacks.forEach(cb -> cb.onResponse(r));
  }

  /**
   * Pass an empty value to the semaphore and notify registered callbacks of an error.
   */
  protected void onError() {
    lastResponse.clear();

    lastResponse.push(Optional.empty());
    callbacks.forEach(cb -> cb.onError());
  }

}
