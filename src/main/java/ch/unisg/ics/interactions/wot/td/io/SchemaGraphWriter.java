package ch.unisg.ics.interactions.wot.td.io;

import ch.unisg.ics.interactions.wot.td.schemas.*;
import ch.unisg.ics.interactions.wot.td.vocabularies.JSONSchema;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;

import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

class SchemaGraphWriter {
  private final static Logger LOGGER = Logger.getLogger(SchemaGraphWriter.class.getCanonicalName());

  private final ModelBuilder graphBuilder;
  private final ValueFactory rdf = SimpleValueFactory.getInstance();


  SchemaGraphWriter(ModelBuilder builder) {
    this.graphBuilder = builder;
  }

  static void write(ModelBuilder builder, Resource nodeId, DataSchema schema) {
    SchemaGraphWriter writer = new SchemaGraphWriter(builder);
    writer.addDataSchema(nodeId, schema);
  }

  private void addDataSchema(Resource nodeId, DataSchema schema) {
    switch (schema.getDatatype()) {
      case DataSchema.OBJECT:
        addObjectSchema(nodeId, (ObjectSchema) schema);
        break;
      case DataSchema.ARRAY:
        addArraySchema(nodeId, (ArraySchema) schema);
        break;
      case DataSchema.BOOLEAN:
        addSimpleSchema(nodeId, schema, rdf.createIRI(JSONSchema.BooleanSchema));
        break;
      case DataSchema.INTEGER:
      case DataSchema.NUMBER:
        addNumberSchema(nodeId, (NumberSchema) schema);
        break;
      case DataSchema.STRING:
        addSimpleSchema(nodeId, schema, rdf.createIRI(JSONSchema.StringSchema));
        break;
      case DataSchema.NULL:
        addSimpleSchema(nodeId, schema, rdf.createIRI(JSONSchema.NullSchema));
        break;
      case DataSchema.DATA:
        addBaseSchema(nodeId, schema);
        break;
      default:
        LOGGER.info("Ignoring a DataSchema of unknown type: " + schema.getDatatype());
        break;
    }
  }

  private void addDataSchemaMetadata(Resource nodeId, DataSchema schema) {
    /* Add semantic types */
    addObjectIRIs(nodeId, RDF.TYPE, schema.getSemanticTypes());

    /* Add enumeration */
    addObjectIRIs(nodeId, rdf.createIRI(JSONSchema.enumeration), schema.getEnumeration());

    /* Add content media type */
    if (schema.getContentMediaType().isPresent()) {
      graphBuilder.add(nodeId, rdf.createIRI(JSONSchema.contentMediaType), schema.getContentMediaType().get());
    }

    /* Add one of schemas */
    for (DataSchema oneSchema : schema.getValidSchemas()) {
      Resource oneSchemaId = rdf.createBNode();
      graphBuilder.add(nodeId, rdf.createIRI(JSONSchema.oneOf), oneSchemaId);
      addDataSchema(oneSchemaId, oneSchema);
    }
  }

  private void addObjectSchema(Resource nodeId, ObjectSchema schema) {
    graphBuilder.add(nodeId, RDF.TYPE, rdf.createIRI(JSONSchema.ObjectSchema));
    addDataSchemaMetadata(nodeId, schema);

    /* Add object properties */
    Map<String, DataSchema> properties = schema.getProperties();

    for (String propertyName : properties.keySet()) {
      Resource propertyId = rdf.createBNode();

      graphBuilder.add(nodeId, rdf.createIRI(JSONSchema.properties), propertyId);
      graphBuilder.add(propertyId, rdf.createIRI(JSONSchema.propertyName), propertyName);

      addDataSchema(propertyId, properties.get(propertyName));
    }

    /* Add names of required properties */
    for (String required : schema.getRequiredProperties()) {
      graphBuilder.add(nodeId, rdf.createIRI(JSONSchema.required), required);
    }
  }

  private void addArraySchema(Resource nodeId, ArraySchema schema) {
    graphBuilder.add(nodeId, RDF.TYPE, rdf.createIRI(JSONSchema.ArraySchema));
    addDataSchemaMetadata(nodeId, schema);

    if (schema.getMinItems().isPresent()) {
      graphBuilder.add(nodeId, rdf.createIRI(JSONSchema.minItems),
          schema.getMinItems().get().intValue());
    }

    if (schema.getMaxItems().isPresent()) {
      graphBuilder.add(nodeId, rdf.createIRI(JSONSchema.maxItems), schema.getMaxItems().get()
          .intValue());
    }

    for (DataSchema item : schema.getItems()) {
      BNode itemId = rdf.createBNode();
      graphBuilder.add(nodeId, rdf.createIRI(JSONSchema.items), itemId);
      addDataSchema(itemId, item);
    }
  }

  private void addBaseSchema(Resource nodeId, DataSchema schema) {
    if (!schema.getValidSchemas().isEmpty()){
      addSimpleSchema(nodeId, schema, rdf.createIRI(JSONSchema.DataSchema));
    }
  }

  private void addSimpleSchema(Resource nodeId, DataSchema schema, IRI schemaType) {
    graphBuilder.add(nodeId, RDF.TYPE, schemaType);
    addDataSchemaMetadata(nodeId, schema);
  }

  private void addNumberSchema(Resource nodeId, NumberSchema numberSchema) {
    if (numberSchema.getDatatype().equals(DataSchema.INTEGER)) {
      graphBuilder.add(nodeId, RDF.TYPE, rdf.createIRI(JSONSchema.IntegerSchema));
    } else {
      graphBuilder.add(nodeId, RDF.TYPE, rdf.createIRI(JSONSchema.NumberSchema));
    }
    addDataSchemaMetadata(nodeId, numberSchema);

    if (numberSchema.getMinimum().isPresent()) {
      if (numberSchema.getDatatype().equals(DataSchema.INTEGER)) {
        graphBuilder.add(nodeId, rdf.createIRI(JSONSchema.minimum),
            ((IntegerSchema) numberSchema).getMinimumAsInteger().get());
      } else {
        graphBuilder.add(nodeId, rdf.createIRI(JSONSchema.minimum), numberSchema.getMinimum().get());
      }
    }

    if (numberSchema.getMaximum().isPresent()) {
      if (numberSchema.getDatatype().equals(DataSchema.INTEGER)) {
        graphBuilder.add(nodeId, rdf.createIRI(JSONSchema.maximum),
            ((IntegerSchema) numberSchema).getMaximumAsInteger().get());
      } else {
        graphBuilder.add(nodeId, rdf.createIRI(JSONSchema.maximum), numberSchema.getMaximum().get());
      }
    }
  }

  private void addObjectIRIs(Resource nodeId, IRI property, Set<String> objects) {
    for (String type : objects) {
      try {
        graphBuilder.add(nodeId, property, rdf.createIRI(type));
      } catch (IllegalArgumentException e) {
        // The object is not an URI, but add it as a string
        graphBuilder.add(nodeId, property, type);
      }
    }
  }
}
