package ch.unisg.ics.interactions.wot.td.schemas;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public abstract class DataSchema {
  public static final String OBJECT = "object";
  public static final String ARRAY = "array";

  public static final String STRING = "string";
  public static final String NUMBER = "number";
  public static final String INTEGER = "integer";
  public static final String BOOLEAN = "boolean";
  public static final String NULL = "null";

  public static final String EMPTY = "empty";

  final private String datatype;
  final private Set<String> semanticTypes;
  final private Set<String> enumeration;
  private final Optional<String> contentMediaType;

  protected DataSchema(String datatype, Set<String> semanticTypes, Set<String> enumeration,
                       Optional<String> contentMediaType) {
    this.datatype = datatype;
    this.semanticTypes = semanticTypes;
    this.enumeration = enumeration;
    this. contentMediaType = contentMediaType;
  }

  public abstract Object parseJson(JsonElement element);

  public String getDatatype() {
    return datatype;
  }

  public Set<String> getSemanticTypes() {
    return semanticTypes;
  }

  public Set<String> getEnumeration() {
    return enumeration;
  }

  public Optional<String> getContentMediaType() { return contentMediaType; }

  public boolean isA(String type) {
    return semanticTypes.contains(type);
  }

  public static DataSchema getEmptySchema() {
    Set<String> semanticTypes = Collections.unmodifiableSet(new HashSet<String>());
    Set<String> enumeration = Collections.unmodifiableSet(new HashSet<String>());

    return new DataSchema(DataSchema.EMPTY, semanticTypes, enumeration, Optional.empty()) {

      @Override
      public Object parseJson(JsonElement element) {
        if (!element.equals(new JsonObject().entrySet())) {
          throw new IllegalArgumentException("JSON element is not empty.");
        }
        return Optional.empty();
      }
    };
  }

  public static abstract class Builder<T extends DataSchema, S extends Builder<T,S>> {
    protected Set<String> semanticTypes;
    protected Set<String> enumeration;
    protected Optional<String> contentMediaType;

    protected Builder() {
      this.semanticTypes = new HashSet<String>();
      this.enumeration = new HashSet<String>();
      this.contentMediaType = Optional.empty();
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

    @SuppressWarnings("unchecked")
    public S addEnum(Set<String> values) {
      this.enumeration.addAll(values);
      return (S) this;
    }

    @SuppressWarnings("unchecked")
    public S setContentMediaType(String contentMediaType) {
      this.contentMediaType = Optional.of(contentMediaType);
      return (S) this;
    }

    public abstract T build();
  }
}
