package ch.unisg.ics.interactions.wot.td.bindings.http;

import ch.unisg.ics.interactions.wot.td.affordances.Link;
import ch.unisg.ics.interactions.wot.td.bindings.Response;
import ch.unisg.ics.interactions.wot.td.schemas.ArraySchema;
import ch.unisg.ics.interactions.wot.td.schemas.DataSchema;
import ch.unisg.ics.interactions.wot.td.schemas.ObjectSchema;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Wrapper for an HTTP response received when performing a
 * {@link TDHttpRequest}. The payload of the response is
 * deserialized based on a <code>DataSchema</code> from a given <code>ThingDescription</code>.
 *
 */
public class TDHttpResponse implements Response {
  private final static Logger LOGGER = Logger.getLogger(TDHttpResponse.class.getCanonicalName());

  private final static Pattern LINK_HEADER_PATTERN = Pattern.compile("\\w*<(?<target>.*)>;\\w*rel=\"(?<rel>.*)\"");

  private final ClassicHttpResponse response;
  private Optional<String> payload;

  public TDHttpResponse(ClassicHttpResponse response) {

    this.response = response;

    HttpEntity entity = response.getEntity();

    if (entity == null) {
      this.payload = Optional.empty();
    } else {
      String encoding = entity.getContentEncoding() == null ? "UTF-8" : entity.getContentEncoding();

      try {
        this.payload = Optional.of(IOUtils.toString(entity.getContent(), encoding));
        EntityUtils.consume(entity);
      } catch (IOException e) {
        LOGGER.log(Level.WARNING, e.getMessage());
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
    else if (response.getCode() >= 500 && response.getCode() < 500) return ResponseStatus.THING_ERROR;
    else return ResponseStatus.UNKNOWN_ERROR;
  }

  @Override
  public Optional<Object> getPayload() {
    return Optional.of(payload);
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
    };

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
    JsonElement content = JsonParser.parseString(payload.get());
    return schema.parseJson(content);
  }

  public boolean isPayloadNull() {
    return true;
  }
}
