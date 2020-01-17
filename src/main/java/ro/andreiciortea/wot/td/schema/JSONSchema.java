package ro.andreiciortea.wot.td.schema;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Literal;
import org.apache.commons.rdf.rdf4j.RDF4J;
import org.eclipse.rdf4j.model.vocabulary.RDF;

import ro.andreiciortea.wot.vocabularies.TDVocab;

public class JSONSchema extends Schema {

  public JSONSchema(BlankNodeOrIRI schemaIRI, Graph graph) {
    super(schemaIRI, graph);
  }

  @Override
  public String instantiate(Map<IRI,Object> input) {
    Optional<IRI> schemaType = graph.stream(this.schemaIRI, TDVocab.schemaType, null)
                                    .filter(triple -> (triple.getObject() instanceof IRI))
                                    .map(triple -> (IRI) triple.getObject())
                                    .findFirst();

    if (!schemaType.isPresent()) {
//      System.out.println("Malformed JSON schema: schema type not present.");
      throw new IllegalArgumentException("Malformed JSON schema: schema type not present.");
    }

    if (schemaType.get().equals(TDVocab.Object)) {
//      System.out.println("Instantiating object schema!");
      return instantiateObject(schemaType.get(), input);
    } else if (schemaType.get().equals(TDVocab.Array)) {
//      System.out.println("Instantiating array schema!");
      return instantiateArray(schemaType.get(), input);
    } else if (schemaType.get().equals(TDVocab.Number)) {
//      System.out.println("Instantiating number schema!");
      return instantiateValue(TDVocab.Number, input);
    } else if (schemaType.get().equals(TDVocab.Boolean)) {
//      System.out.println("Instantiating boolean schema!");
      return instantiateValue(TDVocab.Boolean, input);
      } else if (schemaType.get().equals(TDVocab.String)) {
//      System.out.println("Instantiating string schema!");
      return instantiateValue(TDVocab.String, input);
    } else {
//      System.out.println("Malformed JSON schema, unknown schema type: " + schemaType.get());
      throw new IllegalArgumentException("Malformed JSON schema, unknown schema type " + schemaType.get());
    }
  }

  private String instantiateObject(IRI schemaType, Map<IRI,Object> input) {
    JSONSchemaBuilder builder = new JSONSchemaBuilder(schemaType);

    List<BlankNodeOrIRI> fields = graph.stream(this.schemaIRI, TDVocab.field, null)
        .filter(triple -> triple.getObject() instanceof BlankNodeOrIRI)
        .map(triple -> (BlankNodeOrIRI) triple.getObject())
        .collect(Collectors.toList());

    for (BlankNodeOrIRI field : fields) {
      Optional<String> fieldName = graph.stream(field, TDVocab.name, null)
              .filter(triple -> (triple.getObject() instanceof Literal))
              .map(triple -> ((Literal) triple.getObject()).getLexicalForm())
              .findFirst();

      if (!fieldName.isPresent()) {
//        System.out.println("Malformed JSON schema: field name missing.");
        throw new IllegalArgumentException("Malformed JSON schema: field name missing.");
      }

      Optional<BlankNodeOrIRI> fieldSchemaIRI = graph.stream(field, TDVocab.schema, null)
                            .filter(triple -> triple.getObject() instanceof BlankNodeOrIRI)
                            .map(triple -> (BlankNodeOrIRI) triple.getObject())
                            .findFirst();

      if (!fieldSchemaIRI.isPresent()) {
//        System.out.println("Malformed JSON schema: field schema missing.");
        throw new IllegalArgumentException("Malformed JSON schema: field schema missing.");
      }

      String fieldValue = new JSONSchema(fieldSchemaIRI.get(), graph).instantiate(input);

      builder.addField(fieldName.get(), fieldValue);
    }

    return builder.build();
  }

  private String instantiateArray(IRI schemaType, Map<IRI,Object> input) {
    JSONSchemaBuilder builder = new JSONSchemaBuilder(schemaType);

    List<BlankNodeOrIRI> items = graph.stream(this.schemaIRI, TDVocab.items, null)
                                        .filter(triple -> triple.getObject() instanceof BlankNodeOrIRI)
                                        .map(triple -> (BlankNodeOrIRI) triple.getObject())
                                        .collect(Collectors.toList());

    for (BlankNodeOrIRI item : items) {
      builder.addItem(new JSONSchema(item, graph).instantiate(input));
    }

    return builder.build();
  }

  private String instantiateValue(IRI valueType, Map<IRI,Object> input) {
    Optional<Literal> constValueLiteral = graph.stream(this.schemaIRI, TDVocab.constant, null)
                                                .filter(triple -> (triple.getObject() instanceof Literal))
                                                .map(triple -> (Literal) triple.getObject())
                                                .findFirst();

    if (constValueLiteral.isPresent()) {
//      System.out.println("Const value is present!" + constValueLiteral.get().getLexicalForm());
    //Literal constValue = constValueLiteral.get();
    //IRI valueDataType = constValue.getDatatype();
    //if (valueDataType == XMLSchema.BOOLEAN) {
      return constValueLiteral.get().getLexicalForm();
    //}
    }

//    System.out.println("No const value, checking for typed parameters");

    Optional<IRI> dataType =
        graph.stream(this.schemaIRI, (new RDF4J()).createIRI(RDF.TYPE.stringValue()), null)
              .filter(triple -> (triple.getObject() instanceof IRI))
              .map(triple -> (IRI) triple.getObject())
              .findFirst();

    if (dataType.isPresent() && input.containsKey(dataType.get())) {
      Object value = input.get(dataType.get());

      if (valueType.equals(TDVocab.Number) && (value instanceof Number)) {
//        System.out.println("Found a typed number [" + dataType + "]: " + value.toString());
        return ((Number) value).toString();
      }

      if (valueType.equals(TDVocab.Boolean) && (value instanceof Boolean)) {
//        System.out.println("Found a typed boolean [" + dataType + "]: " + value.toString());
        return ((Boolean) value).toString();
      }

      if (valueType.equals(TDVocab.String) && (value instanceof String)) {
//        System.out.println("Found a typed string [" + dataType + "]: " + value);
        return (String) value;
      }

//      System.out.println("Malformed JSON schema: value semantics missing or incompatible with parameter data type.");
      throw new IllegalArgumentException("Malformed JSON schema: value semantics missing or incompatible with parameter data type.");

    }

//    System.out.println("Malformed JSON schema: could not instantiate value.");
    throw new IllegalArgumentException("Malformed JSON schema: could not instantiate value.");
  }


  class JSONSchemaBuilder {
    IRI schemaType;
    StringBuilder builder;

    boolean hasPrevious = false;

    JSONSchemaBuilder(IRI schemaType) {
      this.schemaType = schemaType;
      this.builder = new StringBuilder();

      if (schemaType.equals(TDVocab.Object)) {
        builder.append('{');
      } else if (schemaType.equals(TDVocab.Array)) {
        builder.append('[');
      }
    }

    void addField(String name, String value) {
      if (hasPrevious) {
        builder.append(", ");
      }

      builder.append("\"" + name + "\" : " + value);

      hasPrevious = true;
    }

    void addItem(String value) {
      if (hasPrevious) {
        builder.append(", ");
      }

      builder.append(value);
      hasPrevious = true;
    }

    String build() {
      if (schemaType.equals(TDVocab.Object)) {
        builder.append('}');
      } else if (schemaType.equals(TDVocab.Array)) {
        builder.append(']');
      }

      return builder.toString();
    }
  }
}
