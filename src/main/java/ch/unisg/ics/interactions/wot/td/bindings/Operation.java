package ch.unisg.ics.interactions.wot.td.bindings;

import ch.unisg.ics.interactions.wot.td.affordances.Form;
import ch.unisg.ics.interactions.wot.td.schemas.DataSchema;

import java.io.IOException;

/**
 * A WoT operation is a temporal entity (~time interval) that starts with a request sent
 * by the Consumer to the Thing and that remains active as long as the Thing returns responses.
 * A Thing is e.g. expected to return a single response during a {@code readProperty} operation
 * and several responses during an {@code observeProperty} operation.
 */
public interface Operation {

  /**
   * Return the form used to initiate the operation.
   *
   * @return a TD form
   */
  Form getForm();

  /**
   * Return the operation type for the operation, which must be one of the operation types listed in the form.
   *
   * @return one of the form's operation types, represented as a full URI (e.g. <code>https://www.w3.org/2019/wot/td#readProperty</code>)
   */
  String getOperationType();

  /**
   * Fill out the form with a payload that should be a proper instance of the given (JSON) schema.
   *
   * @param schema JSON schema of the payload
   * @param payload payload to send to the Thing
   */
  void setPayload(DataSchema schema, Object payload);

  /**
   * Start the operation by sending a message to the Thing with payload.
   * When the method returns, the Consumer may assume the request was received by the Thing.
   * This doesn't imply that the Thing already responded, though.
   * To synchronously wait for a response, use {@link Operation#getResponse()}.
   *
   * @throws IOException if connection to the Thing is lost or if the request is never received the Thing.
   */
  void sendRequest() throws IOException;

  // TODO should the operation fail if request is sent several times?

  /**
   * Wait synchronously for a response from the Thing and return it.
   * If the method is called after the Thing responded, it immediately returns the cached response.
   *
   * @return the unique response sent by the Thing
   * @throws NoResponseException if no response has been received after some timeout
   * or if connection to the Thing was lost
   */
  Response getResponse() throws NoResponseException;

  /**
   * Register a callback for asynchronous responses sent by the Thing.
   *
   * @param callback response callback exposing a method with a {@link Response} as argument
   */
  void registerResponseCallback(ResponseCallback callback);

  /**
   * Remove a callback from the list of registered callbacks for the operation.
   *
   * @param callback response callback already registered via
   * {@link Operation#registerResponseCallback(ResponseCallback)}
   */
  void unregisterResponseCallback(ResponseCallback callback);

}
