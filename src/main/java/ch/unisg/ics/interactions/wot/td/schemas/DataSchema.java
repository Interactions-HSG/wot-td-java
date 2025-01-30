package ch.unisg.ics.interactions.wot.td.schemas;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.lang.reflect.ParameterizedType;
import java.util.*;

public class DataSchema {
  public static final String OBJECT = "object";
  public static final String ARRAY = "array";

  public static final String STRING = "string";
  public static final String NUMBER = "number";
  public static final String INTEGER = "integer";
  public static final String BOOLEAN = "boolean";
  public static final String NULL = "null";

  public static final String DATA = "data";

  final private String datatype;
  final private Set<String> semanticTypes;
  final private Set<String> enumeration;
  private final Optional<String> contentMediaType;
  private final List<DataSchema> dataSchemas;

  protected DataSchema(Set<String> semanticTypes, Set<String> enumeration,
                       Optional<String> contentMediaType, List<DataSchema> dataSchemas) {
    this(DataSchema.DATA, semanticTypes, enumeration, contentMediaType, dataSchemas);
  }

  protected DataSchema(String datatype, Set<String> semanticTypes, Set<String> enumeration,
                       Optional<String> contentMediaType, List<DataSchema> dataSchemas) {
    this.datatype = datatype;
    this.semanticTypes = semanticTypes;
    this.enumeration = enumeration;
    this.contentMediaType = contentMediaType;
    this.dataSchemas = dataSchemas;
  }

  public Object parseJson(JsonElement element) {
    Object data = null;
    if (dataSchemas.isEmpty()) {
      if (element.equals(new JsonObject())) {
        data = Optional.empty();
      }
      else {
        throw new IllegalArgumentException("JSON element should be an empty JSON object when " +
          "no subschemas are provided for a generic schema of type data");
      }
    } else {
      for (DataSchema validSchema : this.getValidSchemas()) {
        try {
          data = validSchema.parseJson(element);
          break;
        } catch (IllegalArgumentException ignored) {
        }
      }
      if (data == null) {
        throw new IllegalArgumentException("JSON element is not valid against any of available subschemas");
      }
    }
    return data;
  }

  public String getDatatype() {
    return datatype;
  }

  public Set<String> getSemanticTypes() {
    return semanticTypes;
  }

  public Set<String> getEnumeration() {
    return enumeration;
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

  public static DataSchema getEmptySchema() {
    Set<String> semanticTypes = Collections.unmodifiableSet(new HashSet<String>());
    Set<String> enumeration = Collections.unmodifiableSet(new HashSet<String>());
    List<DataSchema> dataSchemas = Collections.unmodifiableList(new ArrayList<>());

    return new DataSchema(semanticTypes, enumeration, Optional.empty(), dataSchemas);
  }

  public static class Builder extends JsonSchemaBuilder<DataSchema,DataSchema.Builder> {

    @Override
    public final DataSchema build() {
      return new DataSchema(semanticTypes, enumeration, contentMediaType, dataSchemas);
    }
  }

  public static abstract class JsonSchemaBuilder<T extends DataSchema, S extends JsonSchemaBuilder<T,S>> {
    protected Set<String> semanticTypes;
    protected Set<String> enumeration;
    protected Optional<String> contentMediaType;
    protected List<DataSchema> dataSchemas;

    protected JsonSchemaBuilder() {
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
