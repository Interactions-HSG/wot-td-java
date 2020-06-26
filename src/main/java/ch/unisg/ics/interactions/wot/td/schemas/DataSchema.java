package ch.unisg.ics.interactions.wot.td.schemas;

import java.util.HashSet;
import java.util.Set;

import com.google.gson.JsonElement;

public abstract class DataSchema {
  public static final String OBJECT = "object";
  public static final String ARRAY = "array";
  
  public static final String STRING = "string";
  public static final String NUMBER = "number";
  public static final String INTEGER = "integer";
  public static final String BOOLEAN = "boolean";
  public static final String NULL = "null";
  
  final private String datatype;
  final private Set<String> semanticTypes;
  
  protected DataSchema(String datatype, Set<String> semanticTypes) {
    this.datatype = datatype;
    this.semanticTypes = semanticTypes;
  }
  
  public abstract Object parseJson(JsonElement element);
  
  public String getDatatype() {
    return datatype;
  }
  
  public Set<String> getSemanticTypes() {
    return semanticTypes;
  }
  
  public boolean isA(String type) {
    return semanticTypes.contains(type);
  }
  
  public static abstract class Builder<T extends DataSchema, S extends Builder<T,S>> {
    protected Set<String> semanticTypes;
    
    protected Builder() {
      this.semanticTypes = new HashSet<String>();
    }
    
    @SuppressWarnings("unchecked")
    public S addSemanticType(String type) {
      this.semanticTypes.add(type);
      return (S) this;
    }
    
    @SuppressWarnings("unchecked")
    public S addSemanticTypes(Set<String> type) {
      this.semanticTypes.addAll(type);
      return (S) this;
    }
    
    public abstract T build();
  }
}
