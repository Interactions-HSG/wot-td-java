package ch.unisg.ics.interactions.wot.td.utils;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.RDF;

import ch.unisg.ics.interactions.wot.td.schemas.ArraySchema;
import ch.unisg.ics.interactions.wot.td.schemas.BooleanSchema;
import ch.unisg.ics.interactions.wot.td.schemas.DataSchema;
import ch.unisg.ics.interactions.wot.td.schemas.IntegerSchema;
import ch.unisg.ics.interactions.wot.td.schemas.NullSchema;
import ch.unisg.ics.interactions.wot.td.schemas.NumberSchema;
import ch.unisg.ics.interactions.wot.td.schemas.ObjectSchema;
import ch.unisg.ics.interactions.wot.td.schemas.StringSchema;
import ch.unisg.ics.interactions.wot.td.vocabularies.JSONSchema;

class SchemaGraphReader {
  private Model model;
  
  SchemaGraphReader(Model model) {
    this.model = model;
  }
  
  static Optional<DataSchema> readDataSchema(Resource nodeId, Model model) {
    SchemaGraphReader reader = new SchemaGraphReader(model);
    
    return reader.readDataSchema(nodeId);
  }
  
  private Optional<DataSchema> readDataSchema(Resource schemaId) {
    Optional<IRI> type = Models.objectIRI(model.filter(schemaId, RDF.TYPE, null));
    if (type.isPresent()) {
      if (type.get().equals(JSONSchema.ObjectSchema)) {
        return readObjectSchema(schemaId);
      } else if (type.get().equals(JSONSchema.ArraySchema)) {
        return readArraySchema(schemaId);
      } else if (type.get().equals(JSONSchema.BooleanSchema)) {
        BooleanSchema.Builder builder = new BooleanSchema.Builder();
        readSemanticTypesForDataSchema(builder, schemaId);
        return Optional.of(builder.build());
      } else if (type.get().equals(JSONSchema.NumberSchema)) {
        return readNumberSchema(schemaId);
      } else if (type.get().equals(JSONSchema.IntegerSchema)) {
        return readIntegerSchema(schemaId);
      } else if (type.get().equals(JSONSchema.StringSchema)) {
        StringSchema.Builder builder = new StringSchema.Builder();
        readSemanticTypesForDataSchema(builder, schemaId);
        return Optional.of(builder.build());
      } else if (type.get().equals(JSONSchema.NullSchema)) {
        NullSchema.Builder builder = new NullSchema.Builder();
        readSemanticTypesForDataSchema(builder, schemaId);
        return Optional.of(builder.build());
      }
    }
    
    return Optional.empty();
  }
  
  private Optional<DataSchema> readObjectSchema(Resource schemaId) {
    ObjectSchema.Builder builder = new ObjectSchema.Builder();
    readSemanticTypesForDataSchema(builder, schemaId);
    
    /* Read properties */
    Set<Resource> propertyIds = Models.objectResources(model.filter(schemaId, 
        JSONSchema.properties, null));
    for (Resource property : propertyIds) {
      Optional<DataSchema> propertySchema = readDataSchema(property);
      if (propertySchema.isPresent()) {
        // Each property of an object should also have an associated property name
        Optional<Literal> propertyName = Models.objectLiteral(model.filter(property, 
            JSONSchema.propertyName, null));
        if (propertyName.isEmpty()) {
          throw new InvalidTDException("ObjectSchema property is missing a property name.");
        }
        builder.addProperty(propertyName.get().stringValue(), propertySchema.get());
      }
    }
    
    /* Read required properties */
    Set<Literal> requiredProperties = Models.objectLiterals(model.filter(schemaId, 
        JSONSchema.required, null));
    for (Literal requiredProp : requiredProperties) {
      builder.addRequiredProperties(requiredProp.stringValue());
    }
    
    return Optional.of(builder.build());
  }
  
  private Optional<DataSchema> readArraySchema(Resource schemaId) {
    ArraySchema.Builder builder = new ArraySchema.Builder();
    readSemanticTypesForDataSchema(builder, schemaId);
    
    /* Read minItems */
    Optional<Literal> minItems = Models.objectLiteral(model.filter(schemaId, JSONSchema.minItems, 
        null));
    if (minItems.isPresent()) {
      builder.addMinItems(minItems.get().intValue());
    }
    
    /* Read maxItems */
    Optional<Literal> maxItems = Models.objectLiteral(model.filter(schemaId, JSONSchema.maxItems, 
        null));
    if (maxItems.isPresent()) {
      builder.addMaxItems(maxItems.get().intValue());
    }
    
    /* Read items */
    Set<Resource> itemIds = Models.objectResources(model.filter(schemaId, JSONSchema.items, null));
    for (Resource itemId : itemIds) {
      Optional<DataSchema> item = readDataSchema(itemId);
      if (item.isPresent()) {
        builder.addItem(item.get());
      }
    }
    
    return Optional.of(builder.build());
  }
  
  private Optional<DataSchema> readIntegerSchema(Resource schemaId) {
    IntegerSchema.Builder builder = new IntegerSchema.Builder();
    
    readSemanticTypesForDataSchema(builder, schemaId);
    
    Optional<Literal> maximum = Models.objectLiteral(model.filter(schemaId, JSONSchema.maximum, 
        null));
    if (maximum.isPresent()) {
      builder.addMaximum(maximum.get().intValue());
    }
    
    Optional<Literal> minimum = Models.objectLiteral(model.filter(schemaId, JSONSchema.minimum, 
        null));
    if (minimum.isPresent()) {
      builder.addMinimum(minimum.get().intValue());
    }
    
    return Optional.of(builder.build());
  }
  
  private Optional<DataSchema> readNumberSchema(Resource schemaId) {
    NumberSchema.Builder builder = new NumberSchema.Builder();
    
    readSemanticTypesForDataSchema(builder, schemaId);
    
    Optional<Literal> maximum = Models.objectLiteral(model.filter(schemaId, JSONSchema.maximum, 
        null));
    if (maximum.isPresent()) {
      builder.addMaximum(maximum.get().doubleValue());
    }
    
    Optional<Literal> minimum = Models.objectLiteral(model.filter(schemaId, JSONSchema.minimum, 
        null));
    if (minimum.isPresent()) {
      builder.addMinimum(minimum.get().doubleValue());
    }
    
    return Optional.of(builder.build());
  }
  
  @SuppressWarnings({ "rawtypes", "unchecked" })
  private void readSemanticTypesForDataSchema(DataSchema.Builder builder, Resource schemaId) {
    /* Read semantic types (IRIs) */
    Set<IRI> semIRIs = Models.objectIRIs(model.filter(schemaId, RDF.TYPE, null));
    builder.addSemanticTypes(semIRIs.stream().map(iri -> iri.stringValue())
        .collect(Collectors.toSet()));
    
    /* Read semantic types (strings) */
    Set<String> semTags = Models.objectStrings(model.filter(schemaId, RDF.TYPE, null));
    builder.addSemanticTypes(semTags);
  }
}
