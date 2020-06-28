package ch.unisg.ics.interactions.wot.td.io;

import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
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
        addIntegerSchema(nodeId, (IntegerSchema) schema);
        break;
      case DataSchema.NUMBER:
        addNumberSchema(nodeId, (NumberSchema) schema);
        break;
      case DataSchema.STRING:
        addSimpleSchema(nodeId, schema, rdf.createIRI(JSONSchema.StringSchema));
        break;
      case DataSchema.NULL:
        addSimpleSchema(nodeId, schema, rdf.createIRI(JSONSchema.NullSchema));
        break;
      default:
        LOGGER.info("Ignoring a DataSchema of unknown type: " + schema.getDatatype());
        break;
    }
  }
  
  private void addDataSchemaMetadata(Resource nodeId, DataSchema schema) {
    addObjectIRIs(nodeId, RDF.TYPE, schema.getSemanticTypes());
    addObjectIRIs(nodeId, rdf.createIRI(JSONSchema.enumeration), schema.getEnumeration());
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
  
  private void addSimpleSchema(Resource nodeId, DataSchema schema, IRI schemaType) {
    graphBuilder.add(nodeId, RDF.TYPE, schemaType);
    addDataSchemaMetadata(nodeId, schema);
  }
  
  private void addIntegerSchema(Resource nodeId, IntegerSchema integerSchema) {
    graphBuilder.add(nodeId, RDF.TYPE, rdf.createIRI(JSONSchema.IntegerSchema));
    addDataSchemaMetadata(nodeId, integerSchema);
    
    if (integerSchema.getMinimum().isPresent()) {
      graphBuilder.add(nodeId, rdf.createIRI(JSONSchema.minimum), integerSchema.getMinimum().get());
    }
    
    if (integerSchema.getMaximum().isPresent()) {
      graphBuilder.add(nodeId, rdf.createIRI(JSONSchema.maximum), integerSchema.getMaximum().get());
    }
  }
  
  private void addNumberSchema(Resource nodeId, NumberSchema numberSchema) {
    graphBuilder.add(nodeId, RDF.TYPE, rdf.createIRI(JSONSchema.NumberSchema));
    addDataSchemaMetadata(nodeId, numberSchema);
    
    if (numberSchema.getMinimum().isPresent()) {
      graphBuilder.add(nodeId, rdf.createIRI(JSONSchema.minimum), numberSchema.getMinimum().get());
    }
    
    if (numberSchema.getMaximum().isPresent()) {
      graphBuilder.add(nodeId, rdf.createIRI(JSONSchema.maximum), numberSchema.getMaximum().get());
    }
  }
  
//  private void addSemanticTypesforDataSchema(Resource nodeId, DataSchema schema) {
//    for (String type : schema.getSemanticTypes()) {
//      try {
//        graphBuilder.add(nodeId, RDF.TYPE, rdf.createIRI(type));
//      } catch (IllegalArgumentException e) {
//        // The semantic type is not an URI, but a string label
//        graphBuilder.add(nodeId, RDF.TYPE, type);
//      }
//    }
//  }
  
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
