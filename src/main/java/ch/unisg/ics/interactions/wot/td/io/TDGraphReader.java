package ch.unisg.ics.interactions.wot.td.io;

import ch.unisg.ics.interactions.wot.td.ThingDescription;
import ch.unisg.ics.interactions.wot.td.ThingDescription.TDFormat;
import ch.unisg.ics.interactions.wot.td.affordances.*;
import ch.unisg.ics.interactions.wot.td.schemas.DataSchema;
import ch.unisg.ics.interactions.wot.td.security.SecurityScheme;
import ch.unisg.ics.interactions.wot.td.vocabularies.*;
import org.apache.hc.client5.http.fluent.Request;
import org.eclipse.rdf4j.common.net.ParsedIRI;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.rio.*;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;

import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A reader for deserializing TDs from RDF representations. The created <code>ThingDescription</code>
 * maintains the full RDF graph read as input, which can be retrieved with the <code>getGraph</code>
 * method.
 */
public class TDGraphReader {
  private static final String[] HTTP_URI_SCHEMES = {"http", "https"};
  private static final String[] COAP_URI_SCHEMES = {"coap", "coaps"};

  private final Resource thingId;
  private final ValueFactory rdf = SimpleValueFactory.getInstance();
  private Model model;

  public static ThingDescription readFromURL(TDFormat format, String url) throws IOException {
    String representation = Request.get(url).execute().returnContent().asString();
    return readFromString(format, representation);
  }

  /**
   * Returns a ThingDescription object based on the path parameter that points to a file. Should the path be invalid
   * or if the file does not exist, an IOException is thrown.
   *
   * @param	format	the file's thing description
   * @param path	the location of the file that contains the thing description
   * @return	the thing description
   * @throws IOException if an I/O error occurs reading from the stream
   */
  public static ThingDescription readFromFile(TDFormat format, String path) throws IOException {
    String content = new String(Files.readAllBytes(Paths.get(path)));
    return readFromString(format, content);
  }

  public static ThingDescription readFromString(TDFormat format, String representation) {
    TDGraphReader reader;

    if (format == TDFormat.RDF_TURTLE) {
      reader = new TDGraphReader(RDFFormat.TURTLE, representation);
    } else {
      reader = new TDGraphReader(RDFFormat.JSONLD, representation);
    }

    ThingDescription.Builder tdBuilder = new ThingDescription.Builder(reader.readThingTitle())
      .addSemanticTypes(reader.readThingTypes())
      .addSecuritySchemes(reader.readSecuritySchemes())
      .addProperties(reader.readProperties())
      .addActions(reader.readActions())
      .addEvents(reader.readEvents())
        .addGraph(reader.getGraph());

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
    loadModel(format, representation);

    try {
      thingId = Models.subject(model.filter(null, rdf.createIRI(TD.hasSecurityConfiguration),
        null)).get();
    } catch (NoSuchElementException e) {
      throw new InvalidTDException("Missing mandatory security definitions", e);
    }
  }

  public static boolean isValidIRI(String iri) {
    try {
      SimpleValueFactory.getInstance().createIRI(iri);
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  private void loadModel(RDFFormat format, String representation) {
    RDFParser parser;

    this.model = new LinkedHashModel();

    if (RDFFormat.TURTLE.equals(format)) {
      parser = new TDTurtleParser();
    } else {
      parser = Rio.createParser(format);
    }

    parser.setRDFHandler(new StatementCollector(model));

    try (StringReader stringReader = conversion(representation, format)) {
      parser.parse(stringReader);

      if (RDFFormat.TURTLE.equals(format)) {
        validateHref(readBaseURI());
      }

    } catch (RDFParseException | RDFHandlerException | IOException e) {
      throw new InvalidTDException("RDF Syntax Error", e);
    }
  }

  Model getGraph() {
    return model;
  }

  Optional<String> getThingURI() {
    if (thingId instanceof IRI) {
      return Optional.of(thingId.stringValue());
    }

    return Optional.empty();
  }

  String readThingTitle() {
    Optional<Literal> thingTitle = Models.objectLiteral(model.filter(thingId, rdf.createIRI(DCT.title), null));
    if (thingTitle.isPresent()) {
      return thingTitle.get().stringValue();
    } else {
      throw new InvalidTDException("Missing mandatory title");
    }
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

  List<SecurityScheme> readSecuritySchemes() {
    Set<Resource> nodeIds = Models.objectResources(model.filter(thingId,
        rdf.createIRI(TD.hasSecurityConfiguration), null));

    List<SecurityScheme> schemes = new ArrayList<>();

    for (Resource node : nodeIds) {
      Optional<IRI> securityScheme = Models.objectIRI(model.filter(node, RDF.TYPE, null));

      if (securityScheme.isPresent()) {
        Optional<SecurityScheme> scheme = SecurityScheme.fromRDF(securityScheme.get().stringValue(),
            model, node);

        if (scheme.isPresent()) {
          schemes.add(scheme.get());
        }
      }
    }

    return schemes;
  }

  List<PropertyAffordance> readProperties() {
    List<PropertyAffordance> properties = new ArrayList<>();

    Set<Resource> propertyIds = Models.objectResources(model.filter(thingId,
        rdf.createIRI(TD.hasPropertyAffordance), null));

    for (Resource propertyId : propertyIds) {
      try {
        List<Form> forms = readForms(propertyId, InteractionAffordance.PROPERTY);
        String name = readAffordanceName(propertyId);
        PropertyAffordance.Builder builder = new PropertyAffordance.Builder(name, forms);

        Optional<DataSchema> schema = SchemaGraphReader.readDataSchema(propertyId, model);

        if (schema.isPresent()) {
          builder.addDataSchema(schema.get());
        }
        else {
          builder.addDataSchema(DataSchema.getEmptySchema());
        }

        readAffordanceMetadata(builder, propertyId);
        readUriVariables(builder, propertyId);

        Optional<Literal> observable = Models.objectLiteral(model.filter(propertyId,
          rdf.createIRI(TD.isObservable), null));
        if (observable.isPresent() && observable.get().booleanValue()) {
          builder.addObserve();
        }

        properties.add(builder.build());
      } catch (InvalidTDException e) {
        throw new InvalidTDException("Invalid property definition", e);
      }
    }

    return properties;
  }

  List<ActionAffordance> readActions() {
    List<ActionAffordance> actions = new ArrayList<>();

    Set<Resource> affordanceIds = Models.objectResources(model.filter(thingId,
        rdf.createIRI(TD.hasActionAffordance), null));

    for (Resource affordanceId : affordanceIds) {
      if (!model.contains(affordanceId, RDF.TYPE, rdf.createIRI(TD.ActionAffordance))) {
        continue;
      }

      try {
        ActionAffordance action = readAction(affordanceId);
        actions.add(action);
      } catch (InvalidTDException e) {
        throw new InvalidTDException("Invalid action definition", e);
      }
    }

    return actions;
  }

  private ActionAffordance readAction(Resource affordanceId) {
    List<Form> forms = readForms(affordanceId, InteractionAffordance.ACTION);
    String name = readAffordanceName(affordanceId);
    ActionAffordance.Builder actionBuilder = new ActionAffordance.Builder(name, forms);

    readAffordanceMetadata(actionBuilder, affordanceId);
    readUriVariables(actionBuilder, affordanceId);

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
          throw new InvalidTDException("Invalid action definition", e);
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
      throw new InvalidTDException("Invalid action definition", e);
    }

    return actionBuilder.build();
  }

  List<EventAffordance> readEvents() {
    List<EventAffordance> events = new ArrayList<>();

    Set<Resource> affordanceIds = Models.objectResources(model.filter(thingId,
      rdf.createIRI(TD.hasEventAffordance), null));

    for (Resource affordanceId : affordanceIds) {
      if (!model.contains(affordanceId, RDF.TYPE, rdf.createIRI(TD.EventAffordance))) {
        continue;
      }

      try {
        EventAffordance event = readEvent(affordanceId);
        events.add(event);
      } catch (InvalidTDException e) {
        throw new InvalidTDException("Invalid event definition", e);
      }
    }

    return events;
  }

  private EventAffordance readEvent(Resource affordanceId) {
    List<Form> forms = readForms(affordanceId, InteractionAffordance.EVENT);
    String name = readAffordanceName(affordanceId);
    EventAffordance.Builder eventBuilder = new EventAffordance.Builder(name, forms);

    readAffordanceMetadata(eventBuilder, affordanceId);
    readUriVariables(eventBuilder, affordanceId);

    try {
      Optional<Resource> subscriptionSchemaId = Models.objectResource(model.filter(affordanceId,
        rdf.createIRI(TD.hasSubscriptionSchema), null));

      if (subscriptionSchemaId.isPresent()) {
        Optional<DataSchema> subscription = SchemaGraphReader.readDataSchema(subscriptionSchemaId.get(), model);
        if (subscription.isPresent()) {
          eventBuilder.addSubscriptionSchema(subscription.get());
        }
      }

      Optional<Resource> notificationSchemaId = Models.objectResource(model.filter(affordanceId,
        rdf.createIRI(TD.hasNotificationSchema), null));

      if (notificationSchemaId.isPresent()) {
        Optional<DataSchema> notification = SchemaGraphReader.readDataSchema(notificationSchemaId.get(), model);
        if (notification.isPresent()) {
          eventBuilder.addNotificationSchema(notification.get());
        }
      }

      Optional<Resource> cancellationSchemaId = Models.objectResource(model.filter(affordanceId,
        rdf.createIRI(TD.hasCancellationSchema), null));

      if (cancellationSchemaId.isPresent()) {
        Optional<DataSchema> cancellation = SchemaGraphReader.readDataSchema(cancellationSchemaId.get(), model);
        if (cancellation.isPresent()) {
          eventBuilder.addCancellationSchema(cancellation.get());
        }
      }

    } catch (InvalidTDException e) {
      throw new InvalidTDException("Invalid event definition", e);
    }

    return eventBuilder.build();
  }

  private String readAffordanceName(Resource affordanceId) {
    Optional<Literal> affordanceName = Models.objectLiteral(model.filter(affordanceId, rdf.createIRI(TD.name),
      null));

    if (affordanceName.isPresent()) {
      return affordanceName.get().stringValue();
    } else {
      throw new InvalidTDException("Missing mandatory affordance name");
    }
  }

  private void readAffordanceMetadata(InteractionAffordance
                                        .Builder<?, ? extends InteractionAffordance.Builder<?, ?>> builder, Resource affordanceId) {
    /* Read semantic types */
    Set<IRI> types = Models.objectIRIs(model.filter(affordanceId, RDF.TYPE, null));
    builder.addSemanticTypes(types.stream().map(type -> type.stringValue())
      .collect(Collectors.toList()));

    /* Read title */
    Optional<Literal> title = Models.objectLiteral(model.filter(affordanceId,
      rdf.createIRI(DCT.title), null));

    if (title.isPresent()) {
      builder.addTitle(title.get().stringValue());
    }
  }

  private void validateHref(Optional<String> baseURI) {
    ValueFactory rdf = SimpleValueFactory.getInstance();

    try {
      List<Statement> validHref = new ArrayList<>();
      List<Statement> inValidHref = new ArrayList<>();

      Optional<ParsedIRI> parsedBaseURI = baseURI.isPresent() ?
        Optional.of(new ParsedIRI(baseURI.get())) : Optional.empty();

      // Look for hctl:hasTarget predicates
      Model hrefModel = model.filter(null, rdf.createIRI(HCTL.hasTarget), null);
      for (Statement hrefSt : hrefModel) {
        // Get href object
        String href = hrefSt.getObject().stringValue();

        // If href is not a valid URI and there is a TD base URI, attempt to resolve href
        if (!isValidIRI(href) && parsedBaseURI.isPresent()) {
          // Store invalid statement
          inValidHref.add(hrefSt);
          // Try to resolve href based on the TD base URI
          String hrefResolved = parsedBaseURI.get().resolve(href);
          // Store valid statement with resolved href
          Statement hrefResolvedSt = rdf.createStatement(hrefSt.getSubject(), hrefSt.getPredicate(),
            rdf.createIRI(hrefResolved));
          validHref.add(hrefResolvedSt);
        } else {
          // Throws an exception if href is not a valid URI
          rdf.createIRI(href);
        }
      }

      // Update the model to contain only valid href
      inValidHref.forEach(model::remove);
      model.addAll(validHref);
    } catch (URISyntaxException | IllegalArgumentException e) {
      throw new InvalidTDException("RDF Syntax Error", e);
    }
  }

  private void readUriVariables(InteractionAffordance
                                  .Builder<?, ? extends InteractionAffordance.Builder<?, ?>> builder, Resource affordanceId) {
    Set<Resource> uriVariableIds = Models.objectResources(model.filter(affordanceId, rdf.createIRI(TD.hasUriTemplateSchema), null));
    for (Resource uriVariableId : uriVariableIds) {
      readUriVariable(builder, uriVariableId);
    }
  }

  private void readUriVariable(InteractionAffordance
                                 .Builder<?, ? extends InteractionAffordance.Builder<?, ?>> builder, Resource uriVariableId) {
    Optional<DataSchema> opDataSchema = SchemaGraphReader.readDataSchema(uriVariableId, model);
    Optional<Literal> opNameLiteral = Models.objectLiteral(model.filter(uriVariableId, rdf.createIRI(TD.name), null));
    if (opDataSchema.isPresent() && opNameLiteral.isPresent()) {
      String name = opNameLiteral.get().stringValue();
      DataSchema schema = opDataSchema.get();
      builder.addUriVariable(name, schema);
    }
  }

  private StringReader conversion(String str, RDFFormat format) {
    String newStr = "";
    if (format.equals(RDFFormat.TURTLE)) {
      for (int i = 0; i < str.length(); i++) {
        char c = str.charAt(i);
        if (c == '{') {
          newStr = newStr + "%7B";
        } else if (c == '}') {
          newStr = newStr + "%7D";
        } else {
          newStr = newStr + c;
        }
      }
    } else {
      newStr = str;
    }
    return new StringReader(newStr);
  }

  private List<Form> readForms(Resource affordanceId, String affordanceType) {
    List<Form> forms = new ArrayList<>();

    Set<Resource> formIdSet = Models.objectResources(model.filter(affordanceId,
      rdf.createIRI(TD.hasForm), null));

    for (Resource formId : formIdSet) {
      Optional<IRI> targetOpt = Models.objectIRI(model.filter(formId, rdf.createIRI(HCTL.hasTarget),
        null));

      if (!targetOpt.isPresent()) {
        continue;
      }

      Optional<Literal> methodNameOpt = Optional.empty();
      if (Arrays.stream(HTTP_URI_SCHEMES).anyMatch(targetOpt.toString()::contains)) {
        methodNameOpt = Models.objectLiteral(model.filter(formId,
          rdf.createIRI(HTV.methodName), null));
      } else if (Arrays.stream(COAP_URI_SCHEMES).anyMatch(targetOpt.toString()::contains)) {
        methodNameOpt = Models.objectLiteral(model.filter(formId,
          rdf.createIRI(COV.methodName), null));
      }

      Optional<Literal> contentTypeOpt = Models.objectLiteral(model.filter(formId,
        rdf.createIRI(HCTL.forContentType), null));
      String contentType = contentTypeOpt.isPresent() ? contentTypeOpt.get().stringValue()
        : "application/json";

      Optional<String> subprotocolOpt = Models.objectString(model.filter(formId,
        rdf.createIRI(HCTL.forSubProtocol), null));

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

      if (subprotocolOpt.isPresent()) {
        builder.addSubProtocol(subprotocolOpt.get());
      }

      forms.add(builder.build());
    }

    if (forms.isEmpty()) {
      throw new InvalidTDException("[" + affordanceType + "] All interaction affordances should have "
        + "at least one valid form");
    }

    return forms;
  }
}

