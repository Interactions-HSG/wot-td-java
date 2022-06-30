package ch.unisg.ics.interactions.wot.td.io;

import ch.unisg.ics.interactions.wot.td.templates.IOThingDescriptionTemplate;
import ch.unisg.ics.interactions.wot.td.ThingDescription.TDFormat;
import ch.unisg.ics.interactions.wot.td.schemas.DataSchema;
import ch.unisg.ics.interactions.wot.td.templates.*;
import ch.unisg.ics.interactions.wot.td.vocabularies.*;
import org.apache.hc.client5.http.fluent.Request;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.rio.*;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A reader for deserializing TDs from RDF representations. The created <code>ThingDescription</code>
 * maintains the full RDF graph read as input, which can be retrieved with the <code>getGraph</code>
 * method.
 */
public class IOTDTGraphReader {
  private static final String[] HTTP_URI_SCHEMES = new String[]{"http", "https"};
  private static final String[] COAP_URI_SCHEMES = new String[]{"coap", "coaps"};

  private final Resource thingId;
  private final ValueFactory rdf = SimpleValueFactory.getInstance();
  private Model model;

  public static IOThingDescriptionTemplate readFromURL(TDFormat format, String url) throws IOException {
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
  public static IOThingDescriptionTemplate readFromFile(TDFormat format, String path) throws IOException {
    String content = new String(Files.readAllBytes(Paths.get(path)));
    return readFromString(format, content);
  }

  public static IOThingDescriptionTemplate readFromString(TDFormat format, String representation) {
    IOTDTGraphReader reader;

    if (format == TDFormat.RDF_TURTLE) {
      reader = new IOTDTGraphReader(RDFFormat.TURTLE, representation);
    } else {
      reader = new IOTDTGraphReader(RDFFormat.JSONLD, representation);
    }

    IOThingDescriptionTemplate.Builder tdBuilder = new IOThingDescriptionTemplate.Builder(reader.readThingTitle())
      .addSemanticTypes(reader.readThingTypes())
      .addProperties(reader.readProperties())
      .addActions(reader.readActions())
      .addEvents(reader.readEvents())
      .addGraph(reader.getGraph());





    return tdBuilder.build();
  }

  IOTDTGraphReader(RDFFormat format, String representation) {
    loadModel(format, representation, "");


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
    try (StringReader stringReader = conversion(representation, format)) {
      parser.parse(stringReader, baseURI);
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
    Literal thingTitle;

    try {
      thingTitle = Models.objectLiteral(model.filter(thingId, rdf.createIRI(TD.title), null)).get();
    } catch (NoSuchElementException e) {
      throw new InvalidTDException("Missing mandatory title.", e);
    }

    return thingTitle.stringValue();
  }

  Set<String> readThingTypes() {
    Set<IRI> thingTypes = Models.objectIRIs(model.filter(thingId, RDF.TYPE, null));

    return thingTypes.stream()
      .map(iri -> iri.stringValue())
      .collect(Collectors.toSet());
  }




  public List<IOPropertyAffordanceTemplate> readProperties() {
    List<IOPropertyAffordanceTemplate> properties = new ArrayList<>();

    Set<Resource> propertyIds = Models.objectResources(model.filter(thingId,
      rdf.createIRI(TD.hasPropertyAffordance), null));

    for (Resource propertyId : propertyIds) {
      try {
        String name = readAffordanceName(propertyId);
        IOPropertyAffordanceTemplate.Builder builder = new IOPropertyAffordanceTemplate.Builder(name);

        Optional<DataSchema> schema = SchemaGraphReader.readDataSchema(propertyId, model);

        if (schema.isPresent()) {
          builder.addDataSchema(schema.get());
        }
        else {
          builder.addDataSchema(new DataSchema.Builder().build());
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
        throw new InvalidTDException("Invalid property definition.", e);
      }
    }

    return properties;
  }

  List<IOActionAffordanceTemplate> readActions() {
    List<IOActionAffordanceTemplate> actions = new ArrayList<>();

    Set<Resource> affordanceIds = Models.objectResources(model.filter(thingId,
      rdf.createIRI(TD.hasActionAffordance), null));

    for (Resource affordanceId : affordanceIds) {
      if (!model.contains(affordanceId, RDF.TYPE, rdf.createIRI(TD.ActionAffordance))) {
        continue;
      }

      try {
        IOActionAffordanceTemplate action = readAction(affordanceId);
        actions.add(action);
      } catch (InvalidTDException e) {
        throw new InvalidTDException("Invalid action definition.", e);
      }
    }

    return actions;
  }

  private IOActionAffordanceTemplate readAction(Resource affordanceId) {
    String name = readAffordanceName(affordanceId);
    IOActionAffordanceTemplate.Builder actionBuilder = new IOActionAffordanceTemplate.Builder(name);

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

  List<IOEventAffordanceTemplate> readEvents() {
    List<IOEventAffordanceTemplate> events = new ArrayList<>();

    Set<Resource> affordanceIds = Models.objectResources(model.filter(thingId,
      rdf.createIRI(TD.hasEventAffordance), null));

    for (Resource affordanceId : affordanceIds) {
      if (!model.contains(affordanceId, RDF.TYPE, rdf.createIRI(TD.EventAffordance))) {
        continue;
      }

      try {
        IOEventAffordanceTemplate event = readEvent(affordanceId);
        events.add(event);
      } catch (InvalidTDException e) {
        throw new InvalidTDException("Invalid event definition.", e);
      }
    }

    return events;
  }

  private IOEventAffordanceTemplate readEvent(Resource affordanceId) {
    String name = readAffordanceName(affordanceId);
    IOEventAffordanceTemplate.Builder eventBuilder = new IOEventAffordanceTemplate.Builder(name);

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
      throw new InvalidTDException("Invalid event definition.", e);
    }

    return eventBuilder.build();
  }

  private String readAffordanceName(Resource affordanceId) {
    Literal affordanceName;

    try {
      affordanceName = Models.objectLiteral(model.filter(affordanceId, rdf.createIRI(TD.name),
        null)).get();
    } catch (NoSuchElementException e) {
      throw new InvalidTDException("Missing mandatory affordance name.", e);
    }

    return affordanceName.stringValue();
  }

  private void readAffordanceMetadata(InteractionAffordanceTemplate.Builder builder, Resource affordanceId) {
    /* Read semantic types */
    Set<IRI> types = Models.objectIRIs(model.filter(affordanceId, RDF.TYPE, null));
    builder.addSemanticTypes(types.stream().map(type -> type.stringValue())
      .collect(Collectors.toList()));

    /* Read title */
    Optional<Literal> title = Models.objectLiteral(model.filter(affordanceId,
      rdf.createIRI(TD.title), null));

    if (title.isPresent()) {
      builder.addTitle(title.get().stringValue());
    }
  }

  private void readUriVariables(InteractionAffordanceTemplate
                                  .Builder<?, ? extends InteractionAffordanceTemplate.Builder<?, ?>> builder, Resource affordanceId){
    Set<Resource> uriVariableIds = Models.objectResources(model.filter(affordanceId, rdf.createIRI(TD.hasUriTemplateSchema), null));
    for (Resource uriVariableId : uriVariableIds){
      readUriVariable(builder, uriVariableId);
    }
  }

  private void readUriVariable(InteractionAffordanceTemplate
                                 .Builder<?, ? extends InteractionAffordanceTemplate.Builder<?, ?>> builder, Resource uriVariableId) {
    Optional<DataSchema> opDataSchema = SchemaGraphReader.readDataSchema(uriVariableId, model);
    Optional<Literal> opNameLiteral = Models.objectLiteral(model.filter(uriVariableId, rdf.createIRI(TD.name), null));
    if (opDataSchema.isPresent() && opNameLiteral.isPresent()){
      String name = opNameLiteral.get().stringValue();
      DataSchema schema = opDataSchema.get();
      builder.addUriVariable(name, schema);
    }
  }

  private StringReader conversion(String str, RDFFormat format){
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

}
