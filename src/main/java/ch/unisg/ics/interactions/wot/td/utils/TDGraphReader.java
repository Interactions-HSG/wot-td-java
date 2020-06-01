package ch.unisg.ics.interactions.wot.td.utils;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;

import ch.unisg.ics.interactions.wot.td.ThingDescription;
import ch.unisg.ics.interactions.wot.td.affordances.ActionAffordance;
import ch.unisg.ics.interactions.wot.td.affordances.HTTPForm;
import ch.unisg.ics.interactions.wot.td.affordances.InteractionAffordance;
import ch.unisg.ics.interactions.wot.td.schemas.DataSchema;
import ch.unisg.ics.interactions.wot.td.vocabularies.HTV;
import ch.unisg.ics.interactions.wot.td.vocabularies.TD;

/**
 * TODO: add javadoc
 * 
 * @author Andrei Ciortea
 *
 */
public class TDGraphReader {
  private Resource thingId;
  private Model model;
  
  public static ThingDescription readFromString(String representation) {
    
    TDGraphReader reader = new TDGraphReader(representation);
    
    ThingDescription.Builder tdBuilder = new ThingDescription.Builder(reader.readThingTitle())
        .addTypes(reader.readThingTypes())
        .addSecurity(reader.readSecuritySchemas())
        .addActions(reader.readActions());
    
    Optional<String> thingURI = reader.getThingURI();
    if (thingURI.isPresent()) {
      tdBuilder.addThingURI(thingURI.get());
    }
    
    Optional<String> base = reader.readBaseURI();
    if (base.isPresent()) {
      tdBuilder.addBaseURI(base.get());
    }
    
    return tdBuilder.build();
  }
  
  TDGraphReader(String representation) {
    loadModel(representation, "");
    
    Optional<String> baseURI = readBaseURI();
    if (baseURI.isPresent()) {
      loadModel(representation, baseURI.get());
    }
    
    try {
      thingId = Models.subject(model.filter(null, TD.security, null)).get();
    } catch (NoSuchElementException e) {
      throw new InvalidTDException("Missing mandatory security definitions.");
    }
  }
  
  private void loadModel(String representation, String baseURI) {
    this.model = new LinkedHashModel();
    
    RDFParser parser = Rio.createParser(RDFFormat.TURTLE);
    parser.setRDFHandler(new StatementCollector(model));

    StringReader stringReader = new StringReader(representation);
    
    try {
      parser.parse(stringReader, baseURI);
    } catch (RDFParseException | RDFHandlerException | IOException e) {
      throw new InvalidTDException(e.getMessage());
    }
  }
  
  Optional<String> getThingURI() {
    if (thingId instanceof IRI) {
      return Optional.of(thingId.stringValue());
    }
    
    return Optional.empty();
  }
  
  String readThingTitle() {
    Optional<Literal> thingTitle = Models.objectLiteral(model.filter(thingId, TD.title, null));
    
    return thingTitle.get().stringValue();
  }
  
  Set<String> readThingTypes() {
    Set<IRI> thingTypes = Models.objectIRIs(model.filter(thingId, RDF.TYPE, null));
    
    return thingTypes.stream()
        .map(iri -> iri.stringValue())
        .collect(Collectors.toSet());
  }
  
  Optional<String> readBaseURI() {
    Optional<IRI> baseURI = Models.objectIRI(model.filter(thingId, TD.base, null));
    
    if (baseURI.isPresent()) {
      return Optional.of(baseURI.get().stringValue());
    }
    
    return Optional.empty();
  }
  
  Set<String> readSecuritySchemas() {
    return Models.objectStrings(model.filter(thingId, TD.security, null));
  }
  
  List<ActionAffordance> readActions() {
    List<ActionAffordance> actions = new ArrayList<ActionAffordance>();
    
    Set<Resource> affordanceIds = Models.objectResources(model.filter(thingId, 
        TD.interaction, null));
    
    for (Resource affordanceId : affordanceIds) {
      if (!model.contains(affordanceId, RDF.TYPE, TD.ActionAffordance)) {
        continue;
      }
      
      List<HTTPForm> forms = readForms(affordanceId, InteractionAffordance.ACTION);
      ActionAffordance.Builder actionBuilder = new ActionAffordance.Builder(forms);
      
      Set<IRI> actionTypes = Models.objectIRIs(model.filter(affordanceId, RDF.TYPE, null));
      actionBuilder.addTypes(actionTypes.stream().map(type -> type.stringValue())
          .collect(Collectors.toList()));
      
      Optional<Literal> actionTitle = Models.objectLiteral(model.filter(affordanceId, TD.title, 
          null));
      if (actionTitle.isPresent()) {
        actionBuilder.addTitle(actionTitle.get().stringValue());
      }
      
      Optional<Resource> schemaId = Models.objectResource(model.filter(affordanceId, TD.input, 
          null));
      if (schemaId.isPresent()) {
        try {
          Optional<DataSchema> input = SchemaGraphReader.readDataSchema(schemaId.get(), model);
          if (input.isPresent()) {
            actionBuilder.addInputSchema(input.get());
          }
        } catch (InvalidTDException e) {
          throw new InvalidTDException("Invalid input schema for action: " + 
              ((actionTitle.isPresent()) ? actionTitle.get().stringValue() : "anonymous") + 
              ". " + e.getMessage());
        }
      }
      // TODO: output schema
      
      actions.add(actionBuilder.build());
    }
    
    return actions;
  }
  
  private List<HTTPForm> readForms(Resource affordanceId, String affordanceType) {
    List<HTTPForm> forms = new ArrayList<HTTPForm>();
    
    Set<Resource> formIdSet = Models.objectResources(model.filter(affordanceId, TD.form, null));
    
    for (Resource formId : formIdSet) {
      Optional<IRI> hrefOpt = Models.objectIRI(model.filter(formId, TD.href, null));
      
      if (hrefOpt.isEmpty()) {
        continue;
      }
      
      // TODO: refactor to avoid hard coding the method name
      Optional<Literal> methodNameOpt = Models.objectLiteral(model.filter(formId, HTV.methodName,
          null));      
      String methodName = (methodNameOpt.isPresent()) ? methodNameOpt.get().stringValue() : "POST";
      
      Optional<Literal> contentTypeOpt = Models.objectLiteral(model.filter(formId, 
          TD.contentType, null));
      String contentType = (contentTypeOpt.isEmpty()) ? "application/json" 
          : contentTypeOpt.get().stringValue();
      
      Set<Literal> opsLiterals = Models.objectLiterals(model.filter(formId, TD.op, null));
      
      Set<String> ops = opsLiterals.stream().map(op -> op.stringValue()).collect(Collectors.toSet());
      
      if (opsLiterals.isEmpty()) {
        switch (affordanceType) {
          case InteractionAffordance.PROPERTY:
            ops.add("readproperty");
            ops.add("writeproperty");
            break;
          case InteractionAffordance.ACTION:
            ops.add("invokeaction");
            break;
          case InteractionAffordance.EVENT:
            ops.add("subscribeevent");
            break;
        }
      }
      
      forms.add(new HTTPForm(methodName, hrefOpt.get().stringValue(), contentType, ops));
    }
    
    if (forms.size() == 0) {
      throw new InvalidTDException("All interaction affordances should have at least one "
          + "valid form.");
    }
    
    return forms;
  }
}
