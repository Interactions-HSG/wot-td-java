package ch.unisg.ics.interactions.wot.td.io;

import ch.unisg.ics.interactions.wot.td.ThingDescription;
import ch.unisg.ics.interactions.wot.td.ThingDescription.TDFormat;
import ch.unisg.ics.interactions.wot.td.affordances.*;
import ch.unisg.ics.interactions.wot.td.schemas.DataSchema;
import ch.unisg.ics.interactions.wot.td.security.*;
import ch.unisg.ics.interactions.wot.td.security.DigestSecurityScheme.QualityOfProtection;
import ch.unisg.ics.interactions.wot.td.security.TokenBasedSecurityScheme.TokenLocation;
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
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A reader for deserializing TDs from RDF representations. The created <code>ThingDescription</code>
 * maintains the full RDF graph read as input, which can be retrieved with the <code>getGraph</code>
 * method.
 */
public class TDGraphReader {
  private static final String[] HTTP_URI_SCHEMES = new String[]{"http", "https"};
  private static final String[] COAP_URI_SCHEMES = new String[]{"coap", "coaps"};

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

  final Optional<String> readBaseURI() {
    Optional<IRI> baseURI = Models.objectIRI(model.filter(thingId, rdf.createIRI(TD.hasBase), null));

    if (baseURI.isPresent()) {
      return Optional.of(baseURI.get().stringValue());
    }

    return Optional.empty();
  }

  Map<String, SecurityScheme> readSecuritySchemes() {
    Set<Resource> schemeIds = Models.objectResources(model.filter(thingId,
      rdf.createIRI(TD.hasSecurityConfiguration), null));

    if (schemeIds.isEmpty()) {
      throw new InvalidTDException("Missing mandatory security configuration.");
    }

    Map<String, SecurityScheme> schemes = new HashMap<String, SecurityScheme>();

    for (Resource schemeId : schemeIds) {
      SecurityScheme scheme;
      Set<IRI> schemeTypeIRIs = Models.objectIRIs(model.filter(schemeId, RDF.TYPE, null));

      Set<String> semanticTypes = schemeTypeIRIs.stream()
        .map(iri -> iri.stringValue())
        .collect(Collectors.toSet());

      try {
        if (semanticTypes.contains(WoTSec.NoSecurityScheme)) {
          scheme = SecurityScheme.getNoSecurityScheme();
        } else if (semanticTypes.contains(WoTSec.APIKeySecurityScheme)) {
          scheme = readAPIKeySecurityScheme(schemeId, semanticTypes);
        } else if (semanticTypes.contains(WoTSec.BasicSecurityScheme)) {
          scheme = readBasicSecurityScheme(schemeId, semanticTypes);
        } else if (semanticTypes.contains(WoTSec.DigestSecurityScheme)) {
          scheme = readDigestSecurityScheme(schemeId, semanticTypes);
        } else if (semanticTypes.contains(WoTSec.BearerSecurityScheme)) {
          scheme = readBearerSecurityScheme(schemeId, semanticTypes);
        } else if (semanticTypes.contains(WoTSec.PSKSecurityScheme)) {
          scheme = readPSKSecurityScheme(schemeId, semanticTypes);
        } else if (semanticTypes.contains(WoTSec.OAuth2SecurityScheme)) {
          scheme = readOAuth2SecurityScheme(schemeId, semanticTypes);
        } else {
          throw new InvalidTDException("Unknown type of security scheme");
        }

        String securityName = getUniqueSecurityName(scheme.getSchemeName());
        schemes.put(securityName, scheme);
      } catch (Exception e) {
        throw new InvalidTDException("Invalid security scheme configuration", e);
      }
    }

    return schemes;
  }

  private SecurityScheme readTokenBasedSecurityScheme(TokenBasedSecurityScheme.Builder<?, ?> schemeBuilder, Resource schemeId,
                                              Set<String> semanticTypes) {
    Optional<Literal> in = Models.objectLiteral(model.filter(schemeId, rdf.createIRI(WoTSec.in), null));
    if (in.isPresent()) {
      schemeBuilder.addTokenLocation(TokenLocation.fromString(in.get().stringValue()));
    }

    Optional<Literal> name = Models.objectLiteral(model.filter(schemeId, rdf.createIRI(WoTSec.name), null));
    if (name.isPresent()) {
      schemeBuilder.addTokenName(name.get().stringValue());
    }

    schemeBuilder.addSemanticTypes(semanticTypes);
    return schemeBuilder.build();
  }

  private SecurityScheme readAPIKeySecurityScheme(Resource schemeId, Set<String> semanticTypes) {
    APIKeySecurityScheme.Builder schemeBuilder = new APIKeySecurityScheme.Builder();
    return readTokenBasedSecurityScheme(schemeBuilder, schemeId, semanticTypes);
  }

  private SecurityScheme readBasicSecurityScheme(Resource schemeId, Set<String> semanticTypes) {
    BasicSecurityScheme.Builder schemeBuilder = new BasicSecurityScheme.Builder();
    return readTokenBasedSecurityScheme(schemeBuilder, schemeId, semanticTypes);
  }

  private SecurityScheme readDigestSecurityScheme(Resource schemeId, Set<String> semanticTypes) {
    DigestSecurityScheme.Builder schemeBuilder = new DigestSecurityScheme.Builder();

    Optional<Literal> qop = Models.objectLiteral(model.filter(schemeId, rdf.createIRI(WoTSec.qop), null));
    if (qop.isPresent()) {
      schemeBuilder.addQoP(QualityOfProtection.fromString(qop.get().stringValue()));
    }

    return readTokenBasedSecurityScheme(schemeBuilder, schemeId, semanticTypes);
  }

  private SecurityScheme readBearerSecurityScheme(Resource schemeId, Set<String> semanticTypes) {
    BearerSecurityScheme.Builder schemeBuilder = new BearerSecurityScheme.Builder();

    Optional<Literal> alg = Models.objectLiteral(model.filter(schemeId, rdf.createIRI(WoTSec.alg), null));
    if (alg.isPresent()) {
      schemeBuilder.addAlg(alg.get().stringValue());
    }

    Optional<IRI> authorization = Models.objectIRI(model.filter(schemeId, rdf.createIRI(WoTSec.authorization),
      null));
    if (authorization.isPresent()) {
      schemeBuilder.addAuthorization(authorization.get().stringValue());
    }

    Optional<Literal> format = Models.objectLiteral(model.filter(schemeId, rdf.createIRI(WoTSec.format), null));
    if (format.isPresent()) {
      schemeBuilder.addFormat(format.get().stringValue());
    }

    return readTokenBasedSecurityScheme(schemeBuilder, schemeId, semanticTypes);
  }

  private SecurityScheme readPSKSecurityScheme(Resource schemeId, Set<String> semanticTypes) {
    PSKSecurityScheme.Builder schemeBuilder = new PSKSecurityScheme.Builder();

    Optional<Literal> identity = Models.objectLiteral(model.filter(schemeId, rdf.createIRI(WoTSec.identity),
      null));
    if (identity.isPresent()) {
      schemeBuilder.addIdentity(identity.get().stringValue());
    }

    schemeBuilder.addSemanticTypes(semanticTypes);
    return schemeBuilder.build();
  }

  private SecurityScheme readOAuth2SecurityScheme(Resource schemeId, Set<String> semanticTypes) {

    Optional<Literal> flow = Models.objectLiteral(model.filter(schemeId, rdf.createIRI(WoTSec.flow),
      null));

    if (flow.isPresent()) {
      OAuth2SecurityScheme.Builder schemeBuilder = new OAuth2SecurityScheme.Builder(flow.get().stringValue());

      Optional<IRI> authorization = Models.objectIRI(model.filter(schemeId, rdf.createIRI(WoTSec.authorization), null));
      if (authorization.isPresent()) {
        schemeBuilder.addAuthorization(authorization.get().stringValue());
      }

      Optional<IRI> token = Models.objectIRI(model.filter(schemeId, rdf.createIRI(WoTSec.token), null));
      if (token.isPresent()) {
        schemeBuilder.addToken(token.get().stringValue());
      }

      Optional<IRI> refresh = Models.objectIRI(model.filter(schemeId, rdf.createIRI(WoTSec.refresh), null));
      if (refresh.isPresent()) {
        schemeBuilder.addRefresh(refresh.get().stringValue());
      }

      Set<String> scopes = Models.objectLiterals(model.filter(schemeId, rdf.createIRI(WoTSec.scopes),
          null))
        .stream()
        .map(scope -> scope.stringValue())
        .collect(Collectors.toSet());

      if (!scopes.isEmpty()) {
        schemeBuilder.addScopes(scopes);
      }

      return schemeBuilder.build();
    } else {
      throw new InvalidTDException("Missing or invalid configuration value of type " + WoTSec.flow +
        " on defining security scheme");
    }
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
        throw new InvalidTDException("Invalid action definition.", e);
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
        throw new InvalidTDException("Invalid event definition.", e);
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

  private void readAffordanceMetadata(InteractionAffordance
                                        .Builder<?, ? extends InteractionAffordance.Builder<?, ?>> builder, Resource affordanceId) {
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

      for (Map.Entry<String, Object> kv : readAdditionalProperties(formId).entrySet()) {
        builder.addProperty(kv.getKey(), kv.getValue());
      }

      forms.add(builder.build());
    }

    if (forms.isEmpty()) {
      throw new InvalidTDException("[" + affordanceType + "] All interaction affordances should have "
        + "at least one valid.");
    }

    return forms;
  }

  private Map<String, Object> readAdditionalProperties(Resource entityId) {
    Map<String, Object> kv = new HashMap<>();

    for (Statement st : model.filter(entityId, null, null)) {
      String k = st.getPredicate().stringValue();
      String v = st.getObject().stringValue(); // TODO get typed literal?

      if (!k.startsWith(TD.PREFIX)
        && !k.startsWith(HCTL.PREFIX)
        && !k.startsWith(WoTSec.PREFIX)
        && !k.startsWith(JSONSchema.PREFIX)
        && !k.startsWith(HTV.PREFIX)
        && !k.startsWith(COV.PREFIX)) {
        kv.put(k, v);
      }
    }

    return kv;
  }

  private void readUriVariables(InteractionAffordance
                                  .Builder<?, ? extends InteractionAffordance.Builder<?, ?>> builder, Resource affordanceId){
    Set<Resource> uriVariableIds = Models.objectResources(model.filter(affordanceId, rdf.createIRI(TD.hasUriTemplateSchema), null));
    for (Resource uriVariableId : uriVariableIds){
      readUriVariable(builder, uriVariableId);
    }
  }

  private void readUriVariable(InteractionAffordance
                                  .Builder<?, ? extends InteractionAffordance.Builder<?, ?>> builder, Resource uriVariableId) {
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

  private String getUniqueSecurityName(String securitySchemeName) {
    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    Date date = new Date();
    return securitySchemeName + "_" + timestamp.getTime();
  }

}
