package ch.unisg.ics.interactions.wot.td.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;

import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.WriterConfig;
import org.eclipse.rdf4j.rio.helpers.BasicWriterSettings;

import ch.unisg.ics.interactions.wot.td.ThingDescription;
import ch.unisg.ics.interactions.wot.td.affordances.ActionAffordance;
import ch.unisg.ics.interactions.wot.td.affordances.Form;
import ch.unisg.ics.interactions.wot.td.affordances.InteractionAffordance;
import ch.unisg.ics.interactions.wot.td.schemas.DataSchema;
import ch.unisg.ics.interactions.wot.td.vocabularies.DCT;
import ch.unisg.ics.interactions.wot.td.vocabularies.HCTL;
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
  
  public TDGraphWriter(ThingDescription td) {
    ValueFactory rdfFactory = SimpleValueFactory.getInstance();
    
    this.thingId = (!td.getThingURI().isPresent()) ? rdfFactory.createBNode()
        : rdfFactory.createIRI(td.getThingURI().get());
    
    this.td = td;
    this.graphBuilder = new ModelBuilder();
  }
  
  public static String write(ThingDescription td) {
    TDGraphWriter tdWriter = new TDGraphWriter(td)
        .addTypes()
        .addTitle()
        .addSecurity()
        .addBaseURI()
        .addActions();
    
    return tdWriter.write(RDFFormat.TURTLE);
  }
  
  public TDGraphWriter setNamespace(String prefix, String namespace) {
    this.graphBuilder.setNamespace(prefix, namespace);
    return this;
  }
  
  public String write() {
    return this.addTypes()
        .addTitle()
        .addSecurity()
        .addBaseURI()
        .addActions()
        .write(RDFFormat.TURTLE);
  }
  
  private Model getModel() {
    return graphBuilder.build();
  }
  
  private TDGraphWriter addSecurity() {
    Set<IRI> securitySchemas = td.getSecurity();
    
    for (IRI schema : securitySchemas) {
      BNode schemaId = SimpleValueFactory.getInstance().createBNode();
      graphBuilder.add(thingId, TD.hasSecurityConfiguration, schemaId);
      graphBuilder.add(schemaId, RDF.TYPE, schema);
    }
    
    return this;
  }
  
  private TDGraphWriter addTypes() {
    // TODO: To be considered: always add td:Thing as a type?
    graphBuilder.add(thingId, RDF.TYPE, TD.Thing);
    
    for (String type : td.getSemanticTypes()) {
      graphBuilder.add(thingId, RDF.TYPE, SimpleValueFactory.getInstance().createIRI(type));
    }
    
    return this;
  }
    
  private TDGraphWriter addTitle() {
    graphBuilder.add(thingId, DCT.title, td.getTitle());
    return this;
  }
    
  private TDGraphWriter addBaseURI() {
    if (td.getBaseURI().isPresent()) {
      ValueFactory rdfFactory = SimpleValueFactory.getInstance();
      graphBuilder.add(thingId, TD.hasBase, rdfFactory.createIRI(td.getBaseURI().get()));
    }
    
    return this;
  }
    
  private TDGraphWriter addActions() {
    ValueFactory rdfFactory = SimpleValueFactory.getInstance();
    
    for (ActionAffordance action : td.getActions()) {
      BNode actionId = rdfFactory.createBNode();
      
      graphBuilder.add(thingId, TD.hasActionAffordance, actionId);
      graphBuilder.add(actionId, RDF.TYPE, TD.ActionAffordance);
      
      for (String type : action.getTypes()) {
        graphBuilder.add(actionId, RDF.TYPE, rdfFactory.createIRI(type));
      }
      
      if (action.getTitle().isPresent()) {
        graphBuilder.add(actionId, DCT.title, action.getTitle().get());
      }
      
      addFormsForInteraction(actionId, action);
      
      if (action.getInputSchema().isPresent()) {
        DataSchema schema = action.getInputSchema().get();
        
        Resource inputId = rdfFactory.createBNode();
        graphBuilder.add(actionId, TD.hasInputSchema, inputId);
        
        SchemaGraphWriter.write(graphBuilder, inputId, schema);
      }
      
      if (action.getOutputSchema().isPresent()) {
        DataSchema schema = action.getOutputSchema().get();
        
        Resource outputId = rdfFactory.createBNode();
        graphBuilder.add(actionId, TD.hasOutputSchema, outputId);
        
        SchemaGraphWriter.write(graphBuilder, outputId, schema);
      }
    }
    
    return this;
  }
    
  private void addFormsForInteraction(BNode interactionId, InteractionAffordance interaction) {
    ValueFactory rdfFactory = SimpleValueFactory.getInstance();
    
    for (Form form : interaction.getForms()) {
      BNode formId = rdfFactory.createBNode();
      
      graphBuilder.add(interactionId, TD.hasForm, formId);
      
      graphBuilder.add(formId, HTV.methodName, form.getMethodName());
      graphBuilder.add(formId, HCTL.hasTarget, rdfFactory.createIRI(form.getHref()));
      graphBuilder.add(formId, HCTL.forContentType, form.getContentType());
      
      // TODO: refactor when adding other interaction affordances
      if (interaction instanceof ActionAffordance) {
        graphBuilder.add(formId, HCTL.hasOperationType, TD.invokeAction);
      }
    }
  }
  
  private String write(RDFFormat format) {
    OutputStream out = new ByteArrayOutputStream();
    
    try {
      Rio.write(getModel(), out, format, 
          new WriterConfig().set(BasicWriterSettings.INLINE_BLANK_NODES, true));
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
