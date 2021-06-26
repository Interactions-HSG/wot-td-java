package ch.unisg.ics.interactions.wot.td.clients;

import ch.unisg.ics.interactions.wot.td.schemas.ArraySchema;
import ch.unisg.ics.interactions.wot.td.schemas.DataSchema;
import ch.unisg.ics.interactions.wot.td.schemas.ObjectSchema;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.eclipse.californium.core.coap.Response;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Wrapper for a CoAP response received when performing a
 * {@link ch.unisg.ics.interactions.wot.td.clients.TDCoapRequest}. The payload of the response is
 * deserialized based on a <code>DataSchema</code> from a given <code>ThingDescription</code>.
 */
public class TDCoapResponse {
  private final static Logger LOGGER = Logger.getLogger(TDCoapResponse.class.getCanonicalName());

  private final Response response;
  private final Optional<String> payload;

  public TDCoapResponse(Response response) {

    this.response = response;

    if (response.getPayload() == null) {
      this.payload = Optional.empty();
    } else {
      this.payload = Optional.ofNullable(response.getPayloadString());
    }
  }

  public String getResponseCode() {
    return response.getCode().name();
  }

  public Optional<String> getPayload() {
    return payload;
  }

  public Boolean getPayloadAsBoolean() {
    return new Gson().fromJson(payload.get(), Boolean.class);
  }

  public Integer getPayloadAsInteger() {
    return new Gson().fromJson(payload.get(), Integer.class);
  }

  public Double getPayloadAsDouble() {
    return new Gson().fromJson(payload.get(), Double.class);
  }

  public String getPayloadAsString() {
    return new Gson().fromJson(payload.get(), String.class);
  }

  /**
   * Gets the payload of the response as an object that conforms to a given <code>ObjectSchema</code>.
   * The object payload is represented as a map where:
   * <ul>
   * <li>a key is a string that represents either a semantic type or an object property name</li>
   * <li>a value can be a primitive, an object represented as a <code>Map&lt;String,Object&gt;</code>
   * (that is, a nested object), or an ordered list of values of type <code>List&lt;Object&gt;</code></li>
   * </ul>
   * <p>
   * Note: in the current implementation, the payload of the response is not yet validated against
   * the provided schema.
   *
   * @param schema schema to be used for validating the payload and constructing the map
   * @return the constructed map
   * @throws IllegalArgumentException if the payload of the response does not conform to the provided
   *                                  schema
   */
  @SuppressWarnings("unchecked")
  public Map<String, Object> getPayloadAsObject(ObjectSchema schema)
    throws IllegalArgumentException {
    return (Map<String, Object>) getPayloadWithSchema(schema);
  }

  /**
   * Gets the payload of the response as an array that conforms to a given <code>ArraySchema</code>.
   * The array payload is represented as an ordered list of
   * values of type <code>List&lt;Object&gt;</code>. Values can be primitives, objects represented
   * as <code>Map&lt;String,Object&gt;</code>, or lists of values (that is, nested lists).
   * <p>
   * Note: in the current implementation, the payload of the response is not yet validated against
   * the provided schema.
   *
   * @param schema schema to be used for validating the payload and constructing the list
   * @return the constructed list
   * @throws IllegalArgumentException if the payload of the response does not conform to the provided
   *                                  schema
   */
  @SuppressWarnings("unchecked")
  public List<Object> getPayloadAsArray(ArraySchema schema) throws IllegalArgumentException {
    return (List<Object>) getPayloadWithSchema(schema);
  }

  public Object getPayloadWithSchema(DataSchema schema) throws IllegalArgumentException {
    JsonElement content = JsonParser.parseString(payload.get());
    return schema.parseJson(content);
  }

}
