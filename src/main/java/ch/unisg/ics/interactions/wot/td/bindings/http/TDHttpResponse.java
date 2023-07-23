package ch.unisg.ics.interactions.wot.td.bindings.http;

import ch.unisg.ics.interactions.wot.td.affordances.Link;
import ch.unisg.ics.interactions.wot.td.bindings.BaseResponse;
import ch.unisg.ics.interactions.wot.td.bindings.Operation;
import ch.unisg.ics.interactions.wot.td.schemas.ArraySchema;
import ch.unisg.ics.interactions.wot.td.schemas.DataSchema;
import ch.unisg.ics.interactions.wot.td.schemas.ObjectSchema;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.core5.http.Header;

import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Wrapper for an HTTP response received when performing a
 * {@link TDHttpOperation}. The payload of the response is
 * deserialized based on a <code>DataSchema</code> from a given <code>ThingDescription</code>.
 *
 */
public class TDHttpResponse extends BaseResponse {
  private final static Logger LOGGER = Logger.getLogger(TDHttpResponse.class.getCanonicalName());

  private final static Pattern LINK_HEADER_PATTERN = Pattern.compile("\\w*<(?<target>.*)>;\\w*rel=\"(?<rel>.*)\"");

  private final SimpleHttpResponse response;

  private Optional<JsonElement> jsonPayload;

  private Optional<Object> payload;

  public TDHttpResponse(SimpleHttpResponse response, Operation op) {
    super(op);

    this.response = response;

    if (response.getBodyText() == null) {
      this.jsonPayload = Optional.empty();
      this.payload = Optional.empty();
    } else {
      String txt = response.getBodyText();

      try {
        JsonElement val = JsonParser.parseString(txt);

        this.jsonPayload = Optional.of(val);
        this.payload = Optional.of(asJavaObject(val));
      } catch (JsonSyntaxException e) {
        // assuming textual content
        this.jsonPayload = Optional.of(new JsonPrimitive(txt));
        this.payload = Optional.of(txt);
      }
    }
  }

  public int getStatusCode() {
    return response.getCode();
  }

  public Map<String, String> getHeaders(){
    Header[] headers = response.getHeaders();
    Map<String, String> headerMap = new Hashtable<>();
    for (int i = 0; i< headers.length; i++){
      String key = headers[i].getName();
      String value = headers[i].getValue();
      headerMap.put(key, value);
    }
    return headerMap;
  }

  @Override
  public ResponseStatus getStatus() {
    if (response.getCode() >= 200 && response.getCode() < 300) return ResponseStatus.OK;
    else if (response.getCode() >= 400 && response.getCode() < 500) return ResponseStatus.CONSUMER_ERROR;
    else if (response.getCode() >= 500 && response.getCode() < 600) return ResponseStatus.THING_ERROR;
    else return ResponseStatus.UNKNOWN_ERROR;
  }

  @Override
  public Optional<Object> getPayload() {
    return payload;
  }

  @Override
  public Collection<Link> getLinks() {
    HashSet<Link> links = new HashSet<>();

    for (Header h : response.getHeaders()) {
      if (h.getName().equals("Location") && response.getCode() == 201) {
        links.add(new Link(h.getValue(), ""));
      } else if (h.getName().equals("Link")) {
        Matcher m = LINK_HEADER_PATTERN.matcher(h.getValue());
        if (m.matches()) links.add(new Link(m.group("target"), m.group("rel")));
      }
    }

    return links;
  }

  public Boolean getPayloadAsBoolean() {
    return (Boolean) payload.get();
  }

  public Long getPayloadAsInteger() {
    return (Long) payload.get();
  }

  public Double getPayloadAsDouble() {
    return (Double) payload.get();
  }

  public String getPayloadAsString() {
    return (String) payload.get();
  }

  /**
   * Gets the payload of the response as an object that conforms to a given <code>ObjectSchema</code>.
   * The object payload is represented as a map where:
   * <ul>
   * <li>a key is a string that represents either a semantic type or an object property name</li>
   * <li>a value can be a primitive, an object represented as a <code>Map&lt;String,Object&gt;</code>
   * (that is, a nested object), or an ordered list of values of type <code>List&lt;Object&gt;</code></li>
   * </ul>
   *
   * Note: in the current implementation, the payload of the response is not yet validated against
   * the provided schema.
   *
   * @param schema schema to be used for validating the payload and constructing the map
   * @return the constructed map
   * @throws IllegalArgumentException if the payload of the response does not conform to the provided
   * schema
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
   *
   * Note: in the current implementation, the payload of the response is not yet validated against
   * the provided schema.
   *
   * @param schema schema to be used for validating the payload and constructing the list
   * @return the constructed list
   * @throws IllegalArgumentException if the payload of the response does not conform to the provided
   * schema
   */
  @SuppressWarnings("unchecked")
  public List<Object> getPayloadAsArray(ArraySchema schema) throws IllegalArgumentException {
    return (List<Object>) getPayloadWithSchema(schema);
  }

  public Object getPayloadWithSchema(DataSchema schema) throws IllegalArgumentException {
    return schema.parseJson(jsonPayload.get());
  }

  public boolean isPayloadNull() {
    return true;
  }

  /**
   * TODO share implementation with CoAP (and classes in package "schemas").
   *
   * @param val a JSON value
   * @return a Java object using only the Collection API
   */
  private Object asJavaObject(JsonElement val) {
    if (val.isJsonPrimitive()) {
      JsonPrimitive primitiveVal = val.getAsJsonPrimitive();

      if (primitiveVal.isBoolean()) return val.getAsBoolean();
      if (primitiveVal.isNumber()) return asDoubleOrLong(val.getAsNumber());
      else return val.getAsString();
    } else if (val.isJsonObject()) {
      Map<String, Object> obj = new HashMap<>();

      for (Map.Entry<String, JsonElement> kv : val.getAsJsonObject().entrySet()) {
        obj.put(kv.getKey(), asJavaObject(kv.getValue()));
      }

      return obj;
    } else if (val.isJsonArray()) {
      List<Object> array = new ArrayList<>();

      for (JsonElement v : val.getAsJsonArray().asList()) {
        array.add(asJavaObject(v));
      }

      return array;
    } else {
      return null;
    }
  }

  private Number asDoubleOrLong(Number nb) {
    try {
      return Long.parseLong(nb.toString());
    } catch (NumberFormatException e) {
      return nb.doubleValue();
    }
  }

}
