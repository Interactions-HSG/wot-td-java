package ch.unisg.ics.interactions.wot.td.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Set;

import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import ch.unisg.ics.interactions.wot.td.ThingDescription;
import ch.unisg.ics.interactions.wot.td.affordances.Action;
import ch.unisg.ics.interactions.wot.td.affordances.HTTPForm;
import ch.unisg.ics.interactions.wot.td.affordances.InteractionAffordance;
import ch.unisg.ics.interactions.wot.td.schema.DataSchema;
import ch.unisg.ics.interactions.wot.td.schema.NumberSchema;
import ch.unisg.ics.interactions.wot.td.schema.ObjectSchema;
import ch.unisg.ics.interactions.wot.td.vocabularies.HTV;
import ch.unisg.ics.interactions.wot.td.vocabularies.JSONSchema;
import ch.unisg.ics.interactions.wot.td.vocabularies.TD;

/**
 * TODO: add doc
 * 
 * @author Andrei Ciortea
 *
 */
public class TDGraphWriter {
  private Resource thingId;
  private ThingDescription td;
  private ModelBuilder graphBuilder;
  
  public TDGraphWriter(ThingDescription td) {
    ValueFactory rdfFactory = SimpleValueFactory.getInstance();
    
    this.thingId = (td.getThingURI().isEmpty()) ? rdfFactory.createBNode()
        : rdfFactory.createIRI(td.getThingURI().get());
    
    this.td = td;
    this.graphBuilder = new ModelBuilder();
  }
  
  private Model getModel() {
    return graphBuilder.build();
  }
  
  private TDGraphWriter addSecurity() {
    Set<String> securitySchemas = td.getSecurity();
    
    for (String schema : securitySchemas) {
      graphBuilder.add(thingId, TD.security, schema);
    }
    
    return this;
  }
  
  private TDGraphWriter addTypes() {
    // TODO: To be considered: always add td:Thing as a type?
    graphBuilder.add(thingId, RDF.TYPE, TD.Thing);
    
    for (String type : td.getTypes()) {
      graphBuilder.add(thingId, RDF.TYPE, SimpleValueFactory.getInstance().createIRI(type));
    }
    
    return this;
  }
    
  private TDGraphWriter addTitle() {
    graphBuilder.add(thingId, TD.title, td.getTitle());
    return this;
  }
    
  private TDGraphWriter addBaseURI() {
    if (td.getBaseURI().isPresent()) {
      ValueFactory rdfFactory = SimpleValueFactory.getInstance();
      graphBuilder.add(thingId, TD.base, rdfFactory.createIRI(td.getBaseURI().get()));
    }
    
    return this;
  }
    
  private TDGraphWriter addActions() {
    ValueFactory rdfFactory = SimpleValueFactory.getInstance();
    
    for (Action action : td.getActions()) {
      BNode actionId = rdfFactory.createBNode();
      
      graphBuilder.add(thingId, TD.interaction, actionId);
      graphBuilder.add(actionId, RDF.TYPE, TD.ActionAffordance);
      
      for (String type : action.getTypes()) {
        graphBuilder.add(actionId, RDF.TYPE, rdfFactory.createIRI(type));
      }
      
      if (action.getTitle().isPresent()) {
        graphBuilder.add(actionId, TD.title, action.getTitle().get());
      }
      
      addFormsForInteraction(actionId, action);
      
      if (action.getInputSchema().isPresent()) {
        DataSchema schema = action.getInputSchema().get();
        
        Resource inputId = rdfFactory.createBNode();
        graphBuilder.add(actionId, TD.input, inputId);
        
        addDataSchema(inputId, schema);
      }
    }
    
    return this;
  }
    
  private void addDataSchema(Resource nodeId, DataSchema schema) {
    switch (schema.getType()) {
      case DataSchema.SCHEMA_OBJECT_TYPE:
        addObjectSchema(nodeId, (ObjectSchema) schema);
        break;
      case DataSchema.SCHEMA_NUMBER_TYPE:
        addNumberSchema(nodeId, (NumberSchema) schema);
        break;
    }
  }
    
  private void addObjectSchema(Resource nodeId, ObjectSchema schema) {
    graphBuilder.add(nodeId, RDF.TYPE, JSONSchema.ObjectSchema);
    
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
    
  private void addNumberSchema(Resource nodeId, NumberSchema schema) {
    graphBuilder.add(nodeId, RDF.TYPE, JSONSchema.NumberSchema);
    
    if (schema.getMinimum().isPresent()) {
      graphBuilder.add(nodeId, JSONSchema.minimum, schema.getMinimum().get());
    }
    
    if (schema.getMaximum().isPresent()) {
      graphBuilder.add(nodeId, JSONSchema.maximum, schema.getMaximum().get());
    }
  }
    
  private void addFormsForInteraction(BNode interactionId, InteractionAffordance interaction) {
    ValueFactory rdfFactory = SimpleValueFactory.getInstance();
    
    for (HTTPForm form : interaction.getForms()) {
      BNode formId = rdfFactory.createBNode();
      
      graphBuilder.add(interactionId, TD.form, formId);
      
      graphBuilder.add(formId, HTV.methodName, form.getMethodName());
      graphBuilder.add(formId, TD.href, rdfFactory.createIRI(form.getHref()));
      graphBuilder.add(formId, TD.contentType, form.getContentType());
      
      // TODO: refactor when adding other interaction affordances
      if (interaction instanceof Action) {
        graphBuilder.add(formId, TD.op, "invokeaction");
      }
    }
  }
  
  public String write(RDFFormat format) {
    OutputStream out = new ByteArrayOutputStream();
    
    try {
      Rio.write(getModel(), out, format);
    } finally {
      try {
        out.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    
    return out.toString();
  }
  
  public static String write(RDFFormat format, ThingDescription td) {
    TDGraphWriter tdWriter = new TDGraphWriter(td)
        .addTypes()
        .addTitle()
        .addSecurity()
        .addBaseURI()
        .addActions();
    
    return tdWriter.write(format);
  }
}
