package ch.unisg.ics.interactions.wot.td.writer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;

import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.rdf4j.RDF4JBlankNodeOrIRI;
import org.apache.commons.rdf.rdf4j.RDF4JGraph;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import ch.unisg.ics.interactions.wot.td.ThingDescription;
import ch.unisg.ics.interactions.wot.td.interaction.Action;
import ch.unisg.ics.interactions.wot.td.interaction.HTTPForm;
import ch.unisg.ics.interactions.wot.td.interaction.Interaction;
import ch.unisg.ics.interactions.wot.td.schema.Schema;
import ch.unisg.ics.interactions.wot.td.vocabularies.TDVocab;

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
    
    public Builder addTypes() {
      // Always add td:Thing as a type
      model.add(rdfFactory.createStatement(thingIRI, RDF.TYPE, rdf4jIRI(TDVocab.Thing)));
      
      for (String type : td.getTypes()) {
        model.add(rdfFactory.createStatement(thingIRI, RDF.TYPE, rdfFactory.createIRI(type)));
      }
      
      return this;
    }
    
    public Builder addTitle() {
      model.add(rdfFactory.createStatement(thingIRI, rdf4jIRI(TDVocab.name), 
          rdfFactory.createLiteral(td.getTitle())));
      
      return this;
    }
    
    public Builder addBaseURI() {
      if (td.getBaseURI().isPresent()) {
        model.add(rdfFactory.createStatement(thingIRI, rdf4jIRI(TDVocab.base), 
            rdfFactory.createLiteral(td.getBaseURI().get())));
      }
      
      return this;
    }
    
    public Builder addActions() {
      for (Action action : td.getActions()) {
        BNode actionId = rdfFactory.createBNode();
        
        model.add(rdfFactory.createStatement(thingIRI, rdf4jIRI(TDVocab.interaction), actionId));
        
        model.add(rdfFactory.createStatement(actionId, RDF.TYPE, rdf4jIRI(TDVocab.Action)));
        
        for (String type : action.getTypes()) {
          model.add(rdfFactory.createStatement(actionId, RDF.TYPE, rdfFactory.createIRI(type)));
        }
        
        if (action.getTitle().isPresent()) {
          model.add(rdfFactory.createStatement(actionId, rdf4jIRI(TDVocab.name), 
              rdfFactory.createLiteral(action.getTitle().get())));
        }
        
        addFormsForInteraction(actionId, action);
        
        if (action.getInputSchema().isPresent()) {
          Schema schema = action.getInputSchema().get();
          Resource schemaId = ((RDF4JBlankNodeOrIRI) schema.getSchemaIRI()).asValue();
          Graph inputGraph = schema.getGraph();
          
          if (inputGraph instanceof RDF4JGraph) {
            Optional<Model> input = ((RDF4JGraph) inputGraph).asModel();
            
            if (input.isPresent()) {
              model.add(rdfFactory.createStatement(actionId, rdf4jIRI(TDVocab.inputSchema), schemaId));
              model.addAll(input.get());
            }
          }
        }
      }
      
      return this;
    }
    
    private void addFormsForInteraction(BNode interactionId, Interaction interaction) {
      for (HTTPForm form : interaction.getForms()) {
        BNode formId = rdfFactory.createBNode();
        
        model.add(rdfFactory.createStatement(interactionId, rdf4jIRI(TDVocab.form), formId));
        
        model.add(rdfFactory.createStatement(formId, rdf4jIRI(TDVocab.methodName), 
            rdfFactory.createLiteral(form.getMethodName())));
        model.add(rdfFactory.createStatement(formId, rdf4jIRI(TDVocab.href), 
            rdfFactory.createLiteral(form.getHref())));
        model.add(rdfFactory.createStatement(formId, rdf4jIRI(TDVocab.mediaType), 
            rdfFactory.createLiteral(form.getMediaType())));
        
        // TODO: refactor when adding other interaction affordances
        if (interaction instanceof Action) {
          model.add(rdfFactory.createStatement(formId, rdf4jIRI(TDVocab.rel), 
              rdfFactory.createLiteral("invokeAction")));
        }
      }
    }
    
    public TDGraphWriter build() {
      return new TDGraphWriter(this.model);
    }
    
    private IRI rdf4jIRI(org.apache.commons.rdf.api.IRI iri) {
      return rdfFactory.createIRI(iri.getIRIString());
    }
  }
}
