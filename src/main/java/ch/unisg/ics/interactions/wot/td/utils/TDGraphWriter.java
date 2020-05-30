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
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
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
  
  private Model model;
  
  public TDGraphWriter(Model model) {
    this.model = model;
  }
  
  public Model getModel() {
    return model;
  }
  
  public String write(RDFFormat format) {
    OutputStream out = new ByteArrayOutputStream();
    
    try {
      Rio.write(model, out, format);
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
    TDGraphWriter tdWriter = (new TDGraphWriter.Builder(td))
        .addTypes()
        .addTitle()
        .addSecurity()
        .addBaseURI()
        .addActions()
        .build();
    
    return tdWriter.write(format);
  }
  
  public static class Builder {
    private Model model;
    private ValueFactory rdfFactory;
    
    private Resource thingIRI;
    private ThingDescription td;
    
    public Builder(ThingDescription td) {
      this.model = new LinkedHashModel();
      this.rdfFactory = SimpleValueFactory.getInstance();
      
      this.thingIRI = (td.getThingURI().isEmpty()) ? rdfFactory.createBNode()
          : rdfFactory.createIRI(td.getThingURI().get());
      
      this.td = td;
    }
    
    public Builder addSecurity() {
      Set<String> securitySchemas = td.getSecurity();
      
      for (String schema : securitySchemas) {
        model.add(rdfFactory.createStatement(thingIRI, TD.security, 
            rdfFactory.createLiteral(schema)));
      }
      
      return this;
    }
    
    public Builder addTypes() {
      // Always add td:Thing as a type
      model.add(rdfFactory.createStatement(thingIRI, RDF.TYPE, TD.Thing));
      
      for (String type : td.getTypes()) {
        model.add(rdfFactory.createStatement(thingIRI, RDF.TYPE, rdfFactory.createIRI(type)));
      }
      
      return this;
    }
    
    public Builder addTitle() {
      model.add(rdfFactory.createStatement(thingIRI, TD.title, 
          rdfFactory.createLiteral(td.getTitle())));
      
      return this;
    }
    
    public Builder addBaseURI() {
      if (td.getBaseURI().isPresent()) {
        model.add(rdfFactory.createStatement(thingIRI, TD.base, 
            rdfFactory.createIRI(td.getBaseURI().get())));
      }
      
      return this;
    }
    
    public Builder addActions() {
      for (Action action : td.getActions()) {
        BNode actionId = rdfFactory.createBNode();
        
        model.add(rdfFactory.createStatement(thingIRI, TD.interaction, actionId));
        
        model.add(rdfFactory.createStatement(actionId, RDF.TYPE, TD.ActionAffordance));
        
        for (String type : action.getTypes()) {
          model.add(rdfFactory.createStatement(actionId, RDF.TYPE, rdfFactory.createIRI(type)));
        }
        
        if (action.getTitle().isPresent()) {
          model.add(rdfFactory.createStatement(actionId, TD.title, 
              rdfFactory.createLiteral(action.getTitle().get())));
        }
        
        addFormsForInteraction(actionId, action);
        
        if (action.getInputSchema().isPresent()) {
          DataSchema schema = action.getInputSchema().get();
          
          Resource inputId = rdfFactory.createBNode();
          model.add(rdfFactory.createStatement(actionId, TD.input, inputId));
          
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
      model.add(rdfFactory.createStatement(nodeId, RDF.TYPE, JSONSchema.ObjectSchema));
      
      Map<String, DataSchema> properties = schema.getProperties();
      
      for (String propertyName : properties.keySet()) {
        Resource propertyId = rdfFactory.createBNode();
        
        model.add(rdfFactory.createStatement(nodeId, JSONSchema.properties, propertyId));
        model.add(rdfFactory.createStatement(propertyId, JSONSchema.propertyName, 
            rdfFactory.createLiteral(propertyName)));
        
        addDataSchema(propertyId, properties.get(propertyName));
      }
      
      for (String required : schema.getRequiredProperties()) {
        model.add(rdfFactory.createStatement(nodeId, JSONSchema.required, 
            rdfFactory.createLiteral(required)));
      }
    }
    
    private void addNumberSchema(Resource nodeId, NumberSchema schema) {
      model.add(rdfFactory.createStatement(nodeId, RDF.TYPE, JSONSchema.NumberSchema));
      
      if (schema.getMinimum().isPresent()) {
        model.add(rdfFactory.createStatement(nodeId, JSONSchema.minimum, 
            rdfFactory.createLiteral(schema.getMinimum().get())));
      }
      
      if (schema.getMaximum().isPresent()) {
        model.add(rdfFactory.createStatement(nodeId, JSONSchema.maximum, 
            rdfFactory.createLiteral(schema.getMaximum().get())));
      }
    }
    
    private void addFormsForInteraction(BNode interactionId, InteractionAffordance interaction) {
      for (HTTPForm form : interaction.getForms()) {
        BNode formId = rdfFactory.createBNode();
        
        model.add(rdfFactory.createStatement(interactionId, TD.form, formId));
        
        model.add(rdfFactory.createStatement(formId, HTV.methodName, 
            rdfFactory.createLiteral(form.getMethodName())));
        model.add(rdfFactory.createStatement(formId, TD.href, 
            rdfFactory.createIRI(form.getHref())));
        model.add(rdfFactory.createStatement(formId, TD.contentType, 
            rdfFactory.createLiteral(form.getContentType())));
        
        // TODO: refactor when adding other interaction affordances
        if (interaction instanceof Action) {
          model.add(rdfFactory.createStatement(formId, TD.op, 
              rdfFactory.createLiteral("invokeaction")));
        }
      }
    }
    
    public TDGraphWriter build() {
      return new TDGraphWriter(this.model);
    }
  }
}
