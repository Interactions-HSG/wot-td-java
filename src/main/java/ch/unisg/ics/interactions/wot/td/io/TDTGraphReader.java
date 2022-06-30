package ch.unisg.ics.interactions.wot.td.io;

import ch.unisg.ics.interactions.wot.td.ThingDescription.TDFormat;
import ch.unisg.ics.interactions.wot.td.templates.ThingDescriptionTemplate;
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
public class TDTGraphReader {
  private static final String[] HTTP_URI_SCHEMES = new String[]{"http", "https"};
  private static final String[] COAP_URI_SCHEMES = new String[]{"coap", "coaps"};

  private final Resource thingId;
  private final ValueFactory rdf = SimpleValueFactory.getInstance();
  private Model model;

  public static ThingDescriptionTemplate readFromURL(TDFormat format, String url) throws IOException {
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
  public static ThingDescriptionTemplate readFromFile(TDFormat format, String path) throws IOException {
    String content = new String(Files.readAllBytes(Paths.get(path)));
    return readFromString(format, content);
  }

  public static ThingDescriptionTemplate readFromString(TDFormat format, String representation) {
    TDTGraphReader reader;

    if (format == TDFormat.RDF_TURTLE) {
      reader = new TDTGraphReader(RDFFormat.TURTLE, representation);
    } else {
      reader = new TDTGraphReader(RDFFormat.JSONLD, representation);
    }

    ThingDescriptionTemplate.Builder tdBuilder = new ThingDescriptionTemplate.Builder(reader.readThingTitle())
      .addSemanticTypes(reader.readThingTypes())
      .addProperties(reader.readProperties())
      .addActions(reader.readActions())
      .addEvents(reader.readEvents())
      .addGraph(reader.getGraph());





    return tdBuilder.build();
  }

  TDTGraphReader(RDFFormat format, String representation) {
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





  List<PropertyAffordanceTemplate> readProperties() {
    List<PropertyAffordanceTemplate> properties = new ArrayList<>();

    Set<Resource> propertyIds = Models.objectResources(model.filter(thingId,
      rdf.createIRI(TD.hasPropertyAffordance), null));

    for (Resource propertyId : propertyIds) {
      try {
        String name = readAffordanceName(propertyId);
        PropertyAffordanceTemplate.Builder builder = new PropertyAffordanceTemplate.Builder(name);

        Optional<DataSchema> schema = SchemaGraphReader.readDataSchema(propertyId, model);


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

  List<ActionAffordanceTemplate> readActions() {
    List<ActionAffordanceTemplate> actions = new ArrayList<>();

    Set<Resource> affordanceIds = Models.objectResources(model.filter(thingId,
      rdf.createIRI(TD.hasActionAffordance), null));

    for (Resource affordanceId : affordanceIds) {
      if (!model.contains(affordanceId, RDF.TYPE, rdf.createIRI(TD.ActionAffordance))) {
        continue;
      }

      try {
        ActionAffordanceTemplate action = readAction(affordanceId);
        actions.add(action);
      } catch (InvalidTDException e) {
        throw new InvalidTDException("Invalid action definition.", e);
      }
    }

    return actions;
  }

  private ActionAffordanceTemplate readAction(Resource affordanceId) {
    String name = readAffordanceName(affordanceId);
    ActionAffordanceTemplate.Builder actionBuilder = new ActionAffordanceTemplate.Builder(name);

    readAffordanceMetadata(actionBuilder, affordanceId);
    readUriVariables(actionBuilder, affordanceId);


    return actionBuilder.build();
  }

  List<EventAffordanceTemplate> readEvents() {
    List<EventAffordanceTemplate> events = new ArrayList<>();

    Set<Resource> affordanceIds = Models.objectResources(model.filter(thingId,
      rdf.createIRI(TD.hasEventAffordance), null));

    for (Resource affordanceId : affordanceIds) {
      if (!model.contains(affordanceId, RDF.TYPE, rdf.createIRI(TD.EventAffordance))) {
        continue;
      }

      try {
        EventAffordanceTemplate event = readEvent(affordanceId);
        events.add(event);
      } catch (InvalidTDException e) {
        throw new InvalidTDException("Invalid event definition.", e);
      }
    }

    return events;
  }

  private EventAffordanceTemplate readEvent(Resource affordanceId) {
    String name = readAffordanceName(affordanceId);
    EventAffordanceTemplate.Builder eventBuilder = new EventAffordanceTemplate.Builder(name);

    readAffordanceMetadata(eventBuilder, affordanceId);
    readUriVariables(eventBuilder, affordanceId);


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
