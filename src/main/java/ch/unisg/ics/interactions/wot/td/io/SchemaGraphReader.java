package ch.unisg.ics.interactions.wot.td.io;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
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
  private final Model model;
  private final ValueFactory rdf = SimpleValueFactory.getInstance();
  
  SchemaGraphReader(Model model) {
    this.model = model;
  }
  
  static Optional<DataSchema> readDataSchema(Resource nodeId, Model model) {
    SchemaGraphReader reader = new SchemaGraphReader(model);
    return reader.readDataSchema(nodeId);
  }
  
  private Optional<DataSchema> readDataSchema(Resource schemaId) {
    Set<IRI> types = Models.objectIRIs(model.filter(schemaId, RDF.TYPE, null));
    
    if (!types.isEmpty()) {
      if (types.contains(rdf.createIRI(JSONSchema.ObjectSchema))) {
        return readObjectSchema(schemaId);
      } else if (types.contains(rdf.createIRI(JSONSchema.ArraySchema))) {
        return readArraySchema(schemaId);
      } else if (types.contains(rdf.createIRI(JSONSchema.BooleanSchema))) {
        BooleanSchema.Builder builder = new BooleanSchema.Builder();
        readDataSchemaMetadata(builder, schemaId);
        return Optional.of(builder.build());
      } else if (types.contains(rdf.createIRI(JSONSchema.NumberSchema))) {
        return readNumberSchema(schemaId);
      } else if (types.contains(rdf.createIRI(JSONSchema.IntegerSchema))) {
        return readIntegerSchema(schemaId);
      } else if (types.contains(rdf.createIRI(JSONSchema.StringSchema))) {
        StringSchema.Builder builder = new StringSchema.Builder();
        readDataSchemaMetadata(builder, schemaId);
        return Optional.of(builder.build());
      } else if (types.contains(rdf.createIRI(JSONSchema.NullSchema))) {
        NullSchema.Builder builder = new NullSchema.Builder();
        readDataSchemaMetadata(builder, schemaId);
        return Optional.of(builder.build());
      }
    }
    
    return Optional.empty();
  }
  
  private Optional<DataSchema> readObjectSchema(Resource schemaId) {
    ObjectSchema.Builder builder = new ObjectSchema.Builder();
    readDataSchemaMetadata(builder, schemaId);
    
    /* Read properties */
    Set<Resource> propertyIds = Models.objectResources(model.filter(schemaId, 
        rdf.createIRI(JSONSchema.properties), null));
    for (Resource property : propertyIds) {
      Optional<DataSchema> propertySchema = readDataSchema(property);
      if (propertySchema.isPresent()) {
        // Each property of an object should also have an associated property name
        Optional<Literal> propertyName = Models.objectLiteral(model.filter(property, 
            rdf.createIRI(JSONSchema.propertyName), null));
        if (!propertyName.isPresent()) {
          throw new InvalidTDException("ObjectSchema property is missing a property name.");
        }
        builder.addProperty(propertyName.get().stringValue(), propertySchema.get());
      }
    }
    
    /* Read required properties */
    Set<Literal> requiredProperties = Models.objectLiterals(model.filter(schemaId, 
        rdf.createIRI(JSONSchema.required), null));
    for (Literal requiredProp : requiredProperties) {
      builder.addRequiredProperties(requiredProp.stringValue());
    }
    
    return Optional.of(builder.build());
  }
  
  private Optional<DataSchema> readArraySchema(Resource schemaId) {
    ArraySchema.Builder builder = new ArraySchema.Builder();
    readDataSchemaMetadata(builder, schemaId);
    
    /* Read minItems */
    Optional<Literal> minItems = Models.objectLiteral(model.filter(schemaId, 
        rdf.createIRI(JSONSchema.minItems), null));
    if (minItems.isPresent()) {
      builder.addMinItems(minItems.get().intValue());
    }
    
    /* Read maxItems */
    Optional<Literal> maxItems = Models.objectLiteral(model.filter(schemaId, 
        rdf.createIRI(JSONSchema.maxItems), null));
    if (maxItems.isPresent()) {
      builder.addMaxItems(maxItems.get().intValue());
    }
    
    /* Read items */
    Set<Resource> itemIds = Models.objectResources(model.filter(schemaId, 
        rdf.createIRI(JSONSchema.items), null));
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
    
    readDataSchemaMetadata(builder, schemaId);
    
    Optional<Literal> maximum = Models.objectLiteral(model.filter(schemaId, 
        rdf.createIRI(JSONSchema.maximum), null));
    if (maximum.isPresent()) {
      builder.addMaximum(maximum.get().intValue());
    }
    
    Optional<Literal> minimum = Models.objectLiteral(model.filter(schemaId, 
        rdf.createIRI(JSONSchema.minimum), null));
    if (minimum.isPresent()) {
      builder.addMinimum(minimum.get().intValue());
    }
    
    return Optional.of(builder.build());
  }
  
  private Optional<DataSchema> readNumberSchema(Resource schemaId) {
    NumberSchema.Builder builder = new NumberSchema.Builder();
    
    readDataSchemaMetadata(builder, schemaId);
    
    Optional<Literal> maximum = Models.objectLiteral(model.filter(schemaId, 
        rdf.createIRI(JSONSchema.maximum), null));
    if (maximum.isPresent()) {
      builder.addMaximum(maximum.get().doubleValue());
    }
    
    Optional<Literal> minimum = Models.objectLiteral(model.filter(schemaId, 
        rdf.createIRI(JSONSchema.minimum), null));
    if (minimum.isPresent()) {
      builder.addMinimum(minimum.get().doubleValue());
    }
    
    return Optional.of(builder.build());
  }
    
  @SuppressWarnings({ "rawtypes", "unchecked" })
  private void readDataSchemaMetadata(DataSchema.Builder builder, Resource schemaId) {
    /* Read semantic types (IRIs) */
    Set<IRI> semIRIs = Models.objectIRIs(model.filter(schemaId, RDF.TYPE, null));
    builder.addSemanticTypes(semIRIs.stream().map(iri -> iri.stringValue())
        .collect(Collectors.toSet()));
    
    /* Read semantic types (strings) */
    Set<String> semTags = Models.objectStrings(model.filter(schemaId, RDF.TYPE, null));
    builder.addSemanticTypes(semTags);
    
    /* Read enumeration */
    Set<String> enumeration = Models.objectStrings(model.filter(schemaId, rdf.createIRI(JSONSchema.enumeration), null));
    builder.addEnum(enumeration);
  }
  
}
