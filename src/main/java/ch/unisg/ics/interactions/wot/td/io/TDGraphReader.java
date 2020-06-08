package ch.unisg.ics.interactions.wot.td.io;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.hc.client5.http.fluent.Request;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
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
import ch.unisg.ics.interactions.wot.td.affordances.PropertyAffordance;
import ch.unisg.ics.interactions.wot.td.schemas.DataSchema;
import ch.unisg.ics.interactions.wot.td.vocabularies.DCT;
import ch.unisg.ics.interactions.wot.td.vocabularies.HCTL;
import ch.unisg.ics.interactions.wot.td.vocabularies.HTV;
import ch.unisg.ics.interactions.wot.td.vocabularies.TD;

public class TDGraphReader {
  private final Resource thingId;
  private Model model;
  private final ValueFactory rdf = SimpleValueFactory.getInstance();
  
  public static ThingDescription readFromURL(String url) throws IOException, InvalidTDException {
    String representation = Request.get(url).execute().returnContent().asString();
    return readFromString(representation);
  }
  
  public static ThingDescription readFromString(String representation) throws InvalidTDException {
    TDGraphReader reader;
    
    try {
      reader = new TDGraphReader(RDFFormat.JSONLD, representation);
    } catch (Exception jsonldException) {
      try {
        reader = new TDGraphReader(RDFFormat.TURTLE, representation);
      } catch (Exception turtleException) {
        throw new InvalidTDException("Invalid TD or unsupported RDF format (supported formats: "
            + "Turtle and JSON-LD.", turtleException);
      }
    }
    
    ThingDescription.Builder tdBuilder = new ThingDescription.Builder(reader.readThingTitle())
        .addSemanticTypes(reader.readThingTypes())
        .addSecurity(reader.readSecuritySchemas())
        .addProperties(reader.readProperties())
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
      thingId = Models.subject(model.filter(null, rdf.createIRI(TD.hasSecurityConfiguration), 
          null)).get();
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
    Optional<Literal> thingTitle = Models.objectLiteral(model.filter(thingId, 
        rdf.createIRI(DCT.title), null));
    
    return thingTitle.get().stringValue();
  }
  
  Set<String> readThingTypes() {
    Set<IRI> thingTypes = Models.objectIRIs(model.filter(thingId, RDF.TYPE, null));
    
    return thingTypes.stream()
        .map(iri -> iri.stringValue())
        .collect(Collectors.toSet());
  }
  
  final Optional<String> readBaseURI() {
    Optional<IRI> baseURI = Models.objectIRI(model.filter(thingId, rdf.createIRI(TD.hasBase), null));
    
    if (baseURI.isPresent()) {
      return Optional.of(baseURI.get().stringValue());
    }
    
    return Optional.empty();
  }
  
  Set<IRI> readSecuritySchemas() {
    Set<Resource> nodeIds = Models.objectResources(model.filter(thingId, 
        rdf.createIRI(TD.hasSecurityConfiguration), null));
    
    Set<IRI> schemes = new HashSet<IRI>();
    
    for (Resource node : nodeIds) {
      Optional<IRI> securityScheme = Models.objectIRI(model.filter(node, RDF.TYPE, null));
      
      if (securityScheme.isPresent()) {
        schemes.add(securityScheme.get());
      }
    }
    
    return schemes;
  }
  
  List<PropertyAffordance> readProperties() {
    List<PropertyAffordance> properties = new ArrayList<PropertyAffordance>();
    
    Set<Resource> propertyIds = Models.objectResources(model.filter(thingId, 
        rdf.createIRI(TD.hasPropertyAffordance), null));
    
    for (Resource propertyId : propertyIds) {
      try {
        Optional<DataSchema> schema = SchemaGraphReader.readDataSchema(propertyId, model);
        
        if (schema.isPresent()) {
          List<Form> forms = readForms(propertyId, InteractionAffordance.PROPERTY);
          PropertyAffordance.Builder builder = new PropertyAffordance.Builder(schema.get(), forms);
          
          readAffordanceTitle(builder, propertyId);
          readAffordanceSemanticTypes(builder, propertyId);
          
          Optional<Literal> observable = Models.objectLiteral(model.filter(propertyId, 
              rdf.createIRI(TD.isObservable), null));
          if (observable.isPresent() && observable.get().booleanValue()) {
            builder.addObserve();
          }
          
          properties.add(builder.build());
        }
      } catch (InvalidTDException e) {
        throw new InvalidTDException("Invalid property definition.", e);
      }
    }
    
    return properties;
  }
  
  List<ActionAffordance> readActions() {
    List<ActionAffordance> actions = new ArrayList<ActionAffordance>();
    
    Set<Resource> affordanceIds = Models.objectResources(model.filter(thingId, 
        rdf.createIRI(TD.hasActionAffordance), null));
    
    for (Resource affordanceId : affordanceIds) {
      if (!model.contains(affordanceId, RDF.TYPE, rdf.createIRI(TD.ActionAffordance))) {
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
    
    readAffordanceTitle(actionBuilder, affordanceId);
    readAffordanceSemanticTypes(actionBuilder, affordanceId);
    
    try {
      Optional<Resource> inputSchemaId = Models.objectResource(model.filter(affordanceId, 
          rdf.createIRI(TD.hasInputSchema), null));
      if (inputSchemaId.isPresent()) {
        try {
          Optional<DataSchema> input = SchemaGraphReader.readDataSchema(inputSchemaId.get(), model);
          if (input.isPresent()) {
            actionBuilder.addInputSchema(input.get());
          }
        } catch (InvalidTDException e) {
          throw new InvalidTDException("Invalid action definition.", e);
        }
      }
      
      Optional<Resource> outSchemaId = Models.objectResource(model.filter(affordanceId, 
          rdf.createIRI(TD.hasOutputSchema), null));
      if (outSchemaId.isPresent()) {
          Optional<DataSchema> output = SchemaGraphReader.readDataSchema(outSchemaId.get(), model);
          if (output.isPresent()) {
            actionBuilder.addOutputSchema(output.get());
          }
      }
    } catch (InvalidTDException e) {
      throw new InvalidTDException("Invalid action definition.", e);
    }
    
    return actionBuilder.build();
  }
  
  private void readAffordanceSemanticTypes(InteractionAffordance
      .Builder<?, ? extends InteractionAffordance.Builder<?,?>> builder, Resource affordanceId) {
    
    Set<IRI> types = Models.objectIRIs(model.filter(affordanceId, RDF.TYPE, null));
    builder.addSemanticTypes(types.stream().map(type -> type.stringValue())
        .collect(Collectors.toList()));
  }
  
  private void readAffordanceTitle(InteractionAffordance
      .Builder<?, ? extends InteractionAffordance.Builder<?,?>> builder, Resource affordanceId) {
    
    Optional<Literal> title = Models.objectLiteral(model.filter(affordanceId, 
        rdf.createIRI(DCT.title), null));
    if (title.isPresent()) {
      builder.addTitle(title.get().stringValue());
    }
  }
  
  private List<Form> readForms(Resource affordanceId, String affordanceType) {
    List<Form> forms = new ArrayList<Form>();
    
    Set<Resource> formIdSet = Models.objectResources(model.filter(affordanceId, 
        rdf.createIRI(TD.hasForm), null));
    
    for (Resource formId : formIdSet) {
      Optional<IRI> targetOpt = Models.objectIRI(model.filter(formId, rdf.createIRI(HCTL.hasTarget), 
          null));
      
      if (!targetOpt.isPresent()) {
        continue;
      }
      
      Optional<Literal> methodNameOpt = Models.objectLiteral(model.filter(formId, 
          rdf.createIRI(HTV.methodName), null));
      
      Optional<Literal> contentTypeOpt = Models.objectLiteral(model.filter(formId, 
          rdf.createIRI(HCTL.forContentType), null));
      String contentType = contentTypeOpt.isPresent() ? contentTypeOpt.get().stringValue() 
          : "application/json";
      
      Set<IRI> opsIRIs = Models.objectIRIs(model.filter(formId, rdf.createIRI(HCTL.hasOperationType), 
          null));
      Set<String> ops = opsIRIs.stream().map(op -> op.stringValue()).collect(Collectors.toSet());
      
      String target = targetOpt.get().stringValue();
      
      Form.Builder builder = new Form.Builder(target)
          .setContentType(contentType)
          .addOperationTypes(ops); 
      
      if (methodNameOpt.isPresent()) {
        builder.setMethodName(methodNameOpt.get().stringValue());
      }
      
      forms.add(builder.build());
    }
    
    if (forms.isEmpty()) {
      throw new InvalidTDException("[" + affordanceType + "] All interaction affordances should have "
          + "at least one valid.");
    }
    
    return forms;
  }
}
