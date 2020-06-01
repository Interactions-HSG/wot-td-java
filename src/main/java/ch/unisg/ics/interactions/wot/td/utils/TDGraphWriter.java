package ch.unisg.ics.interactions.wot.td.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
import ch.unisg.ics.interactions.wot.td.affordances.ActionAffordance;
import ch.unisg.ics.interactions.wot.td.affordances.HTTPForm;
import ch.unisg.ics.interactions.wot.td.affordances.InteractionAffordance;
import ch.unisg.ics.interactions.wot.td.schemas.DataSchema;
import ch.unisg.ics.interactions.wot.td.vocabularies.HTV;
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
  
  public static String write(ThingDescription td) {
    TDGraphWriter tdWriter = new TDGraphWriter(td)
        .addTypes()
        .addTitle()
        .addSecurity()
        .addBaseURI()
        .addActions();
    
    return tdWriter.write(RDFFormat.TURTLE);
  }
  
  private TDGraphWriter(ThingDescription td) {
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
    
    for (ActionAffordance action : td.getActions()) {
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
        
        SchemaGraphWriter.write(graphBuilder, inputId, schema);
      }
    }
    
    return this;
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
      if (interaction instanceof ActionAffordance) {
        graphBuilder.add(formId, TD.op, "invokeaction");
      }
    }
  }
  
  private String write(RDFFormat format) {
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
}
