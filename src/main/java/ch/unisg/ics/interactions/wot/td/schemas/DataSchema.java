package ch.unisg.ics.interactions.wot.td.schemas;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.lang.reflect.ParameterizedType;
import java.util.*;

public abstract class DataSchema {
  public static final String OBJECT = "object";
  public static final String ARRAY = "array";

  public static final String STRING = "string";
  public static final String NUMBER = "number";
  public static final String INTEGER = "integer";
  public static final String BOOLEAN = "boolean";
  public static final String NULL = "null";

  public static final String EMPTY = "empty";
  public static final String SUPER = "super";

  private final String datatype;
  private final Set<String> semanticTypes;
  private final Set<String> enumeration;
  private final Optional<String> contentMediaType;
  private final List<DataSchema> dataSchemas;

  protected DataSchema(String datatype, Set<String> semanticTypes, Set<String> enumeration,
                       Optional<String> contentMediaType, List<DataSchema> dataSchemas) {
    this.datatype = datatype;
    this.semanticTypes = semanticTypes;
    this.enumeration = enumeration;
    this.contentMediaType = contentMediaType;
    this.dataSchemas = dataSchemas;
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

  public static DataSchema getEmptySchema() {
    Set<String> semanticTypes = Collections.unmodifiableSet(new HashSet<String>());
    Set<String> enumeration = Collections.unmodifiableSet(new HashSet<String>());
    List<DataSchema> dataSchemas = Collections.unmodifiableList(new ArrayList<>());

    return new DataSchema(DataSchema.EMPTY, semanticTypes, enumeration,
      Optional.empty(), dataSchemas) {

      @Override
      public Object parseJson(JsonElement element) {
        if (!element.equals(new JsonObject())) {
          throw new IllegalArgumentException("JSON element is not an empty JSON object");
        }
        return Optional.empty();
      }
    };
  }

  public static DataSchema getSuperSchema(List<DataSchema> dataSchemas) {
    Set<String> semanticTypes = Collections.unmodifiableSet(new HashSet<String>());
    Set<String> enumeration = Collections.unmodifiableSet(new HashSet<String>());

    if (dataSchemas.isEmpty()) {
      throw new IllegalArgumentException("No subschemas found");
    }

    return new DataSchema(DataSchema.SUPER, semanticTypes, enumeration,
      Optional.empty(), Collections.unmodifiableList(dataSchemas)) {

      @Override
      public Object parseJson(JsonElement element) {
        Object data = Optional.empty();

        for (DataSchema validSchema : this.getValidSchemas()) {
          try {
            System.out.println(validSchema.getDatatype());
            data = validSchema.parseJson(element);
            break;
          } catch (IllegalArgumentException e) {
          }
        }

        if (data.equals(Optional.empty())) {
          throw new IllegalArgumentException("JSON element is not valid against any of available subschemas");
        }
        return data;
      }
    };
  }

  public Optional<String> getContentMediaType() {
    return contentMediaType;
  }

  public List<DataSchema> getValidSchemas() {
    return dataSchemas;
  }

  public List<DataSchema> getValidSchemasBySemanticType(String type) {
    List<DataSchema> schemas = new ArrayList<>();
    for (DataSchema schema : dataSchemas) {
      if (schema.getSemanticTypes().contains(type)) {
        schemas.add(schema);
      }
    }
    return schemas;
  }

  public boolean isA(String type) {
    return semanticTypes.contains(type);
  }

  public List<DataSchema> getValidSchemasByContentMediaType(String contentMediaType) {
    List<DataSchema> schemas = new ArrayList<>();
    for (DataSchema schema : dataSchemas) {
      if (schema.getContentMediaType().isPresent() && contentMediaType.equals(schema.getContentMediaType().get())) {
        schemas.add(schema);
      }
    }
    return schemas;
  }

  public static abstract class Builder<T extends DataSchema, S extends Builder<T,S>> {
    protected Set<String> semanticTypes;
    protected Set<String> enumeration;
    protected Optional<String> contentMediaType;
    protected List<DataSchema> dataSchemas;

    protected Builder() {
      this.semanticTypes = new HashSet<String>();
      this.enumeration = new HashSet<String>();
      this.contentMediaType = Optional.empty();
      this.dataSchemas = new ArrayList<>();
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

    @SuppressWarnings("unchecked")
    public S oneOf(DataSchema... dataSchemas) {
      Class<T> type = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass())
        .getActualTypeArguments()[0];
      for (DataSchema dataSchema : dataSchemas) {
        if (!type.isInstance(dataSchema)) {
          throw new IllegalArgumentException("Schema cannot be validated against subschema " +
            "of type " + dataSchema.getDatatype());
        }
      }
      this.dataSchemas.addAll(Arrays.asList(dataSchemas));
      return (S) this;
    }

    public abstract T build();
  }
}
