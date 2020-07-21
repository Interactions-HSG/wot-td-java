package ch.unisg.ics.interactions.wot.td.clients;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import ch.unisg.ics.interactions.wot.td.schemas.ArraySchema;
import ch.unisg.ics.interactions.wot.td.schemas.DataSchema;
import ch.unisg.ics.interactions.wot.td.schemas.ObjectSchema;

public class TDHttpResponse {
  private final static Logger LOGGER = Logger.getLogger(TDHttpResponse.class.getCanonicalName());
  
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
  
  @SuppressWarnings("unchecked")
  public Map<String, Object> getPayloadAsObject(ObjectSchema schema) 
      throws IllegalArgumentException {
    return (Map<String, Object>) getPayloadWithSchema(schema);
  }
  
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
