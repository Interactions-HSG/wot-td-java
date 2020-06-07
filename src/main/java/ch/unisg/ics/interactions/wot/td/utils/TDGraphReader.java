package ch.unisg.ics.interactions.wot.td.utils;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
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
import ch.unisg.ics.interactions.wot.td.affordances.Form;
import ch.unisg.ics.interactions.wot.td.affordances.InteractionAffordance;
import ch.unisg.ics.interactions.wot.td.schemas.DataSchema;
import ch.unisg.ics.interactions.wot.td.vocabularies.DCT;
import ch.unisg.ics.interactions.wot.td.vocabularies.HCTL;
import ch.unisg.ics.interactions.wot.td.vocabularies.HTV;
import ch.unisg.ics.interactions.wot.td.vocabularies.TD;

public class TDGraphReader {
  private Resource thingId;
  private Model model;
  
  public static ThingDescription readFromString(RDFFormat format, String representation) {
    
    TDGraphReader reader = new TDGraphReader(format, representation);
    
    ThingDescription.Builder tdBuilder = new ThingDescription.Builder(reader.readThingTitle())
        .addSemanticTypes(reader.readThingTypes())
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
  
  TDGraphReader(RDFFormat format, String representation) {
    loadModel(format, representation, "");
    
    Optional<String> baseURI = readBaseURI();
    if (baseURI.isPresent()) {
      loadModel(format, representation, baseURI.get());
    }
    
    try {
      thingId = Models.subject(model.filter(null, TD.hasSecurityConfiguration, null)).get();
    } catch (NoSuchElementException e) {
      throw new InvalidTDException("Missing mandatory security definitions.", e);
    }
  }
  
  private void loadModel(RDFFormat format, String representation, String baseURI) {
    this.model = new LinkedHashModel();
    
    RDFParser parser = Rio.createParser(format);
    parser.setRDFHandler(new StatementCollector(model));

    StringReader stringReader = new StringReader(representation);
    
    try {
      parser.parse(stringReader, baseURI);
    } catch (RDFParseException | RDFHandlerException | IOException e) {
      throw new InvalidTDException("RDF Syntax Error", e);
    } finally {
      stringReader.close();
    }
  }
  
  Optional<String> getThingURI() {
    if (thingId instanceof IRI) {
      return Optional.of(thingId.stringValue());
    }
    
    return Optional.empty();
  }
  
  String readThingTitle() {
    Optional<Literal> thingTitle = Models.objectLiteral(model.filter(thingId, DCT.title, null));
    
    return thingTitle.get().stringValue();
  }
  
  Set<String> readThingTypes() {
    Set<IRI> thingTypes = Models.objectIRIs(model.filter(thingId, RDF.TYPE, null));
    
    return thingTypes.stream()
        .map(iri -> iri.stringValue())
        .collect(Collectors.toSet());
  }
  
  final Optional<String> readBaseURI() {
    Optional<IRI> baseURI = Models.objectIRI(model.filter(thingId, TD.hasBase, null));
    
    if (baseURI.isPresent()) {
      return Optional.of(baseURI.get().stringValue());
    }
    
    return Optional.empty();
  }
  
  Set<IRI> readSecuritySchemas() {
    Set<Resource> nodeIds = Models.objectResources(model.filter(thingId, TD.hasSecurityConfiguration, 
        null));
    
    Set<IRI> schemes = new HashSet<IRI>();
    
    for (Resource node : nodeIds) {
      Optional<IRI> securityScheme = Models.objectIRI(model.filter(node, RDF.TYPE, null));
      
      if (securityScheme.isPresent()) {
        schemes.add(securityScheme.get());
      }
    }
    
    return schemes;
  }
  
  List<ActionAffordance> readActions() {
    List<ActionAffordance> actions = new ArrayList<ActionAffordance>();
    
    Set<Resource> affordanceIds = Models.objectResources(model.filter(thingId, 
        TD.hasActionAffordance, null));
    
    for (Resource affordanceId : affordanceIds) {
      if (!model.contains(affordanceId, RDF.TYPE, TD.ActionAffordance)) {
        continue;
      }
      
      ActionAffordance action = readAction(affordanceId);
      actions.add(action);
    }
    
    return actions;
  }
  
  private ActionAffordance readAction(Resource affordanceId) {
    List<Form> forms = readForms(affordanceId, InteractionAffordance.ACTION);
    ActionAffordance.Builder actionBuilder = new ActionAffordance.Builder(forms);
    
    Set<IRI> actionTypes = Models.objectIRIs(model.filter(affordanceId, RDF.TYPE, null));
    actionBuilder.addSemanticTypes(actionTypes.stream().map(type -> type.stringValue())
        .collect(Collectors.toList()));
    
    Optional<Literal> actionTitle = Models.objectLiteral(model.filter(affordanceId, DCT.title, 
        null));
    if (actionTitle.isPresent()) {
      actionBuilder.addTitle(actionTitle.get().stringValue());
    }
    
    Optional<Resource> inputSchemaId = Models.objectResource(model.filter(affordanceId, 
        TD.hasInputSchema, null));
    if (inputSchemaId.isPresent()) {
      try {
        Optional<DataSchema> input = SchemaGraphReader.readDataSchema(inputSchemaId.get(), model);
        if (input.isPresent()) {
          actionBuilder.addInputSchema(input.get());
        }
      } catch (InvalidTDException e) {
        throw new InvalidTDException("Invalid input schema for action: " + 
            (actionTitle.isPresent() ? actionTitle.get().stringValue() : "anonymous") + 
            ". " + e.getMessage(), e);
      }
    }
    
    Optional<Resource> outSchemaId = Models.objectResource(model.filter(affordanceId, 
        TD.hasOutputSchema, null));
    if (outSchemaId.isPresent()) {
      try {
        Optional<DataSchema> output = SchemaGraphReader.readDataSchema(outSchemaId.get(), model);
        if (output.isPresent()) {
          actionBuilder.addOutputSchema(output.get());
        }
      } catch (InvalidTDException e) {
        throw new InvalidTDException("Invalid output schema for action: " + 
            (actionTitle.isPresent() ? actionTitle.get().stringValue() : "anonymous") + 
            ". " + e.getMessage(), e);
      }
    }
    
    return actionBuilder.build();
  }
  
  private List<Form> readForms(Resource affordanceId, String affordanceType) {
    List<Form> forms = new ArrayList<Form>();
    
    Set<Resource> formIdSet = Models.objectResources(model.filter(affordanceId, TD.hasForm, null));
    
    for (Resource formId : formIdSet) {
      Optional<IRI> hrefOpt = Models.objectIRI(model.filter(formId, HCTL.hasTarget, null));
      
      if (!hrefOpt.isPresent()) {
        continue;
      }
      
      // TODO: refactor to avoid hard coding the method name
      Optional<Literal> methodNameOpt = Models.objectLiteral(model.filter(formId, HTV.methodName,
          null));      
      String methodName = methodNameOpt.isPresent() ? methodNameOpt.get().stringValue() : "POST";
      
      Optional<Literal> contentTypeOpt = Models.objectLiteral(model.filter(formId, 
          HCTL.forContentType, null));
      String contentType = contentTypeOpt.isPresent() ? contentTypeOpt.get().stringValue() 
          : "application/json";
      
      Set<Literal> opsLiterals = Models.objectLiterals(model.filter(formId, HCTL.hasOperationType, 
          null));
      
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
          default:
            break;
        }
      }
      
      forms.add(new Form(methodName, hrefOpt.get().stringValue(), contentType, ops));
    }
    
    if (forms.isEmpty()) {
      throw new InvalidTDException("All interaction affordances should have at least one "
          + "valid form.");
    }
    
    return forms;
  }
}
