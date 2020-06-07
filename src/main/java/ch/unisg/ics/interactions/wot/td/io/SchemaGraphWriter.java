package ch.unisg.ics.interactions.wot.td.io;

import java.util.Map;
import java.util.logging.Logger;

import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;

import ch.unisg.ics.interactions.wot.td.schemas.ArraySchema;
import ch.unisg.ics.interactions.wot.td.schemas.DataSchema;
import ch.unisg.ics.interactions.wot.td.schemas.IntegerSchema;
import ch.unisg.ics.interactions.wot.td.schemas.NumberSchema;
import ch.unisg.ics.interactions.wot.td.schemas.ObjectSchema;
import ch.unisg.ics.interactions.wot.td.vocabularies.JSONSchema;

class SchemaGraphWriter {
  private final static Logger LOGGER = Logger.getLogger(SchemaGraphWriter.class.getCanonicalName());
  
  final private ModelBuilder graphBuilder;
  
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
        addSimpleSchema(nodeId, schema, JSONSchema.BooleanSchema);
        break;
      case DataSchema.INTEGER:
        addIntegerSchema(nodeId, (IntegerSchema) schema);
        break;
      case DataSchema.NUMBER:
        addNumberSchema(nodeId, (NumberSchema) schema);
        break;
      case DataSchema.STRING:
        addSimpleSchema(nodeId, schema, JSONSchema.StringSchema);
        break;
      case DataSchema.NULL:
        addSimpleSchema(nodeId, schema, JSONSchema.NullSchema);
        break;
      default:
        LOGGER.info("Ignoring a DataSchema of unknown type: " + schema.getDatatype());
        break;
    }
  }
    
  private void addObjectSchema(Resource nodeId, ObjectSchema schema) {
    graphBuilder.add(nodeId, RDF.TYPE, JSONSchema.ObjectSchema);
    addSemanticTypesforDataSchema(nodeId, schema);
    
    /* Add object properties */
    Map<String, DataSchema> properties = schema.getProperties();
    
    for (String propertyName : properties.keySet()) {
      Resource propertyId = SimpleValueFactory.getInstance().createBNode();
      
      graphBuilder.add(nodeId, JSONSchema.properties, propertyId);
      graphBuilder.add(propertyId, JSONSchema.propertyName, propertyName);
      
      addDataSchema(propertyId, properties.get(propertyName));
    }
      
    /* Add names of required properties */
    for (String required : schema.getRequiredProperties()) {
      graphBuilder.add(nodeId, JSONSchema.required, required);
    }
  }
  
  private void addArraySchema(Resource nodeId, ArraySchema schema) {
    graphBuilder.add(nodeId, RDF.TYPE, JSONSchema.ArraySchema);
    addSemanticTypesforDataSchema(nodeId, schema);
    
    if (schema.getMinItems().isPresent()) {
      graphBuilder.add(nodeId, JSONSchema.minItems, schema.getMinItems().get().intValue());
    }
    
    if (schema.getMaxItems().isPresent()) {
      graphBuilder.add(nodeId, JSONSchema.maxItems, schema.getMaxItems().get().intValue());
    }
    
    for (DataSchema item : schema.getItems()) {
      BNode itemId = SimpleValueFactory.getInstance().createBNode();
      graphBuilder.add(nodeId, JSONSchema.items, itemId);
      addDataSchema(itemId, item);
    }
  }
  
  private void addSimpleSchema(Resource nodeId, DataSchema schema, IRI schemaType) {
    graphBuilder.add(nodeId, RDF.TYPE, schemaType);
    addSemanticTypesforDataSchema(nodeId, schema);
  }
  
  private void addIntegerSchema(Resource nodeId, IntegerSchema schema) {
    graphBuilder.add(nodeId, RDF.TYPE, JSONSchema.IntegerSchema);
    addSemanticTypesforDataSchema(nodeId, schema);
    
    if (schema.getMinimum().isPresent()) {
      graphBuilder.add(nodeId, JSONSchema.minimum, schema.getMinimum().get());
    }
    
    if (schema.getMaximum().isPresent()) {
      graphBuilder.add(nodeId, JSONSchema.maximum, schema.getMaximum().get());
    }
  }
  
  private void addNumberSchema(Resource nodeId, NumberSchema schema) {
    graphBuilder.add(nodeId, RDF.TYPE, JSONSchema.NumberSchema);
    addSemanticTypesforDataSchema(nodeId, schema);
    
    if (schema.getMinimum().isPresent()) {
      graphBuilder.add(nodeId, JSONSchema.minimum, schema.getMinimum().get());
    }
    
    if (schema.getMaximum().isPresent()) {
      graphBuilder.add(nodeId, JSONSchema.maximum, schema.getMaximum().get());
    }
  }
  
  private void addSemanticTypesforDataSchema(Resource nodeId, DataSchema schema) {
    for (String type : schema.getSemanticTypes()) {
      try {
        graphBuilder.add(nodeId, RDF.TYPE, SimpleValueFactory.getInstance().createIRI(type));
      } catch (IllegalArgumentException e) {
        // The semantic type is not an URI, but a string label
        graphBuilder.add(nodeId, RDF.TYPE, type);
      }
    }
  }
}
