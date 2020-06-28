package ch.unisg.ics.interactions.wot.td.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

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
import ch.unisg.ics.interactions.wot.td.security.SecurityScheme;
import ch.unisg.ics.interactions.wot.td.vocabularies.DCT;
import ch.unisg.ics.interactions.wot.td.vocabularies.HCTL;
import ch.unisg.ics.interactions.wot.td.vocabularies.HTV;
import ch.unisg.ics.interactions.wot.td.vocabularies.TD;

public class TDGraphWriter {
  private final Resource thingId;
  private final ThingDescription td;
  private final ModelBuilder graphBuilder;
  private final ValueFactory rdf = SimpleValueFactory.getInstance();
  
  public TDGraphWriter(ThingDescription td) {
    this.thingId = td.getThingURI().isPresent() ? rdf.createIRI(td.getThingURI().get())
        : rdf.createBNode();
    
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
    List<SecurityScheme> securitySchemas = td.getSecuritySchemes();
    
    for (SecurityScheme schema : securitySchemas) {
      BNode schemaId = rdf.createBNode();
      graphBuilder.add(thingId, rdf.createIRI(TD.hasSecurityConfiguration), schemaId);
      // TODO: complete serialization of security schemes (not just the type)
      graphBuilder.add(schemaId, RDF.TYPE, rdf.createIRI(schema.getSchemaType()));
    }
    
    return this;
  }
  
  private TDGraphWriter addTypes() {
    graphBuilder.add(thingId, RDF.TYPE, rdf.createIRI(TD.Thing));
    
    for (String type : td.getSemanticTypes()) {
      graphBuilder.add(thingId, RDF.TYPE, rdf.createIRI(type));
    }
    
    return this;
  }
  
  private TDGraphWriter addTitle() {
    graphBuilder.add(thingId, rdf.createIRI(DCT.title), td.getTitle());
    return this;
  }
    
  private TDGraphWriter addBaseURI() {
    if (td.getBaseURI().isPresent()) {
      graphBuilder.add(thingId, rdf.createIRI(TD.hasBase), 
          rdf.createIRI(td.getBaseURI().get()));
    }
    
    return this;
  }
    
  private TDGraphWriter addActions() {
    for (ActionAffordance action : td.getActions()) {
      BNode actionId = rdf.createBNode();
      
      graphBuilder.add(thingId, rdf.createIRI(TD.hasActionAffordance), actionId);
      graphBuilder.add(actionId, RDF.TYPE, rdf.createIRI(TD.ActionAffordance));
      
      for (String type : action.getSemanticTypes()) {
        graphBuilder.add(actionId, RDF.TYPE, rdf.createIRI(type));
      }
      
      if (action.getTitle().isPresent()) {
        graphBuilder.add(actionId, rdf.createIRI(DCT.title), action.getTitle().get());
      }
      
      addFormsForInteraction(actionId, action);
      
      if (action.getInputSchema().isPresent()) {
        DataSchema schema = action.getInputSchema().get();
        
        Resource inputId = rdf.createBNode();
        graphBuilder.add(actionId, rdf.createIRI(TD.hasInputSchema), inputId);
        
        SchemaGraphWriter.write(graphBuilder, inputId, schema);
      }
      
      if (action.getOutputSchema().isPresent()) {
        DataSchema schema = action.getOutputSchema().get();
        
        Resource outputId = rdf.createBNode();
        graphBuilder.add(actionId, rdf.createIRI(TD.hasOutputSchema), outputId);
        
        SchemaGraphWriter.write(graphBuilder, outputId, schema);
      }
    }
    
    return this;
  }
    
  private void addFormsForInteraction(BNode interactionId, InteractionAffordance interaction) {
    for (Form form : interaction.getForms()) {
      BNode formId = rdf.createBNode();
      
      graphBuilder.add(interactionId, rdf.createIRI(TD.hasForm), formId);
      
      graphBuilder.add(formId, rdf.createIRI(HTV.methodName), form.getMethodName().get());
      graphBuilder.add(formId, rdf.createIRI(HCTL.hasTarget), rdf.createIRI(form.getTarget()));
      graphBuilder.add(formId, rdf.createIRI(HCTL.forContentType), form.getContentType());
      
      for (String opType : form.getOperationTypes()) {
        try {
          IRI opTypeIri = rdf.createIRI(opType);
          graphBuilder.add(formId, rdf.createIRI(HCTL.hasOperationType), opTypeIri);
        } catch (IllegalArgumentException e) {
          graphBuilder.add(formId, rdf.createIRI(HCTL.hasOperationType), opType);
        }
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
