package ch.unisg.ics.interactions.wot.td.bindings.coap;

import ch.unisg.ics.interactions.wot.td.affordances.Link;
import ch.unisg.ics.interactions.wot.td.bindings.BaseResponse;
import ch.unisg.ics.interactions.wot.td.bindings.Operation;
import ch.unisg.ics.interactions.wot.td.schemas.ArraySchema;
import ch.unisg.ics.interactions.wot.td.schemas.DataSchema;
import ch.unisg.ics.interactions.wot.td.schemas.ObjectSchema;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.eclipse.californium.core.coap.Option;
import org.eclipse.californium.core.coap.OptionNumberRegistry;
import org.eclipse.californium.core.coap.Response;

import java.util.*;
import java.util.logging.Logger;

/**
 * Wrapper for a CoAP response received when performing a
 * {@link TDCoapOperation}. The payload of the response is
 * deserialized based on a <code>DataSchema</code> from a given <code>ThingDescription</code>.
 */
public class TDCoapResponse extends BaseResponse {
  private final static Logger LOGGER = Logger.getLogger(TDCoapResponse.class.getCanonicalName());

  private final Response response;
  private Optional<String> payload;

  public TDCoapResponse(Response response, Operation op) {
    super(op);

    this.response = response;

    if (response.getPayload() == null) {
      this.payload = Optional.empty();
    } else {
      this.payload = Optional.ofNullable(response.getPayloadString());
    }
  }

  public int getResponseCode() {
    return response.getRawCode();
  }

  public Map<String, String> getOptions(){
    Map<String, String> optionMap = new Hashtable<>();
    List<Option> optionList = response.getOptions().asSortedList();
    for (Option option: optionList){
      String key = OptionNumberRegistry.toString(option.getNumber());
      String value = option.getStringValue();
      optionMap.put(key, value);
    }
    return optionMap;
  }

  public String getResponseCodeName() {
    return response.getCode().name();
  }

  @Override
  public ResponseStatus getStatus() {
    switch (response.getCode().codeClass) {
      case 2: return ResponseStatus.OK;
      case 4: return ResponseStatus.CONSUMER_ERROR;
      case 5: return ResponseStatus.THING_ERROR;
      default: return ResponseStatus.UNKNOWN_ERROR;
    }
  }

  public Optional<Object> getPayload() {
    if (payload.isPresent()) return Optional.of(payload.get());
    else return Optional.empty();
  }

  @Override
  public Collection<Link> getLinks() {
    Set<Link> links = new HashSet<>();

    if (response.getRawCode() == 201) {
      String p = response.getOptions().getLocationPathString();
      String q = response.getOptions().getLocationQueryString();

      Link link = new Link(String.format("%s?%s", p, q), "");
      links.add(link);
    }

    return links;
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
   * @throws IllegalArgumentException if the payload of the response does not conform to the
   * provided schema
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
