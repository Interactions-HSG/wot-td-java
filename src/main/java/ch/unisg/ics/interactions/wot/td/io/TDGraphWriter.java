package ch.unisg.ics.interactions.wot.td.io;

import ch.unisg.ics.interactions.wot.td.ThingDescription;
import ch.unisg.ics.interactions.wot.td.affordances.*;
import ch.unisg.ics.interactions.wot.td.schemas.DataSchema;
import ch.unisg.ics.interactions.wot.td.security.SecurityScheme;
import ch.unisg.ics.interactions.wot.td.vocabularies.*;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.rio.RDFFormat;

import java.util.*;

/**
 * A writer for serializing TDs as RDF graphs. Provides a fluent API for adding prefix bindings to be
 * used in the serialization.
 */
public class TDGraphWriter {
  private static final String[] HTTP_URI_SCHEMES = new String[]{"http:", "https:"};
  private static final String[] COAP_URI_SCHEMES = new String[]{"coap:", "coaps:"};

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
    return new TDGraphWriter(td).write();
  }

  /**
   * Sets a prefix binding for a given namespace.
   *
   * @param prefix    the prefix to be used in the serialized representation
   * @param namespace the given namespace
   * @return this <code>TDGraphWriter</code>
   */
  public TDGraphWriter setNamespace(String prefix, String namespace) {
    this.graphBuilder.setNamespace(prefix, namespace);
    return this;
  }

  public String write() {
    return this.addTypes()
      .addTitle()
      .addSecurity()
      .addBaseURI()
      .addProperties()
      .addActions()
      .addEvents()
      .addGraph()
      .write(RDFFormat.TURTLE);
  }

  private Model getModel() {
    return graphBuilder.build();
  }

  private TDGraphWriter addSecurity() {
    Map<String, SecurityScheme> securitySchemes = td.getSecurityDefinitions();

    for (SecurityScheme scheme : securitySchemes.values()) {
      BNode schemeId = rdf.createBNode();
      graphBuilder.add(thingId, rdf.createIRI(TD.hasSecurityConfiguration), schemeId);

      Map<String, Object> configuration = scheme.getConfiguration();

      for (String semanticType : scheme.getSemanticTypes()) {
        graphBuilder.add(schemeId, RDF.TYPE, rdf.createIRI(semanticType));
      }

      for (Map.Entry<String,Object> configurationEntry : configuration.entrySet()) {
        IRI confTypeIri = rdf.createIRI(configurationEntry.getKey());
        Object confValue = configurationEntry.getValue();
        List<Object> confValues = new ArrayList<>();
        if (confValue instanceof Set) {
          confValues.addAll((Collection<?>) confValue);
        }
        else {
          confValues.add(confValue);
        }
        for (Object objConfValue : confValues) {
          graphBuilder.add(schemeId, confTypeIri, objConfValue);
        }
      }
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

  private TDGraphWriter addProperties() {
    for (PropertyAffordance property : td.getProperties()) {
      Resource propertyId = addAffordance(property, TD.hasPropertyAffordance, TD.PropertyAffordance);
      graphBuilder.add(propertyId, rdf.createIRI(TD.isObservable), property.isObservable());

      SchemaGraphWriter.write(graphBuilder, propertyId, property.getDataSchema());
    }

    return this;
  }

  private TDGraphWriter addActions() {
    for (ActionAffordance action : td.getActions()) {
      Resource actionId = addAffordance(action, TD.hasActionAffordance, TD.ActionAffordance);

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

  private TDGraphWriter addEvents() {
    for (EventAffordance event : td.getEvents()) {
      Resource eventId = addAffordance(event, TD.hasEventAffordance, TD.EventAffordance);

      if (event.getSubscriptionSchema().isPresent()) {
        DataSchema schema = event.getSubscriptionSchema().get();

        Resource subscriptionId = rdf.createBNode();
        graphBuilder.add(eventId, rdf.createIRI(TD.hasSubscriptionSchema), subscriptionId);

        SchemaGraphWriter.write(graphBuilder, subscriptionId, schema);
      }

      if (event.getNotificationSchema().isPresent()) {
        DataSchema schema = event.getNotificationSchema().get();

        Resource notificationId = rdf.createBNode();
        graphBuilder.add(eventId, rdf.createIRI(TD.hasNotificationSchema), notificationId);

        SchemaGraphWriter.write(graphBuilder, notificationId, schema);
      }

      if (event.getCancellationSchema().isPresent()) {
        DataSchema schema = event.getCancellationSchema().get();

        Resource cancellationId = rdf.createBNode();
        graphBuilder.add(eventId, rdf.createIRI(TD.hasCancellationSchema), cancellationId);

        SchemaGraphWriter.write(graphBuilder, cancellationId, schema);
      }
    }

    return this;
  }

  private TDGraphWriter addGraph() {
    if (td.getGraph().isPresent()) {
      getModel().addAll(td.getGraph().get());

      td.getGraph().get().getNamespaces().stream()
        .filter(ns -> !getModel().getNamespace(ns.getPrefix()).isPresent())
        .forEach(graphBuilder::setNamespace);
    }
    return this;
  }

  private Resource addAffordance(InteractionAffordance affordance, String affordanceProp,
                                 String affordanceClass) {
    BNode affordanceId = rdf.createBNode();

    graphBuilder.add(thingId, rdf.createIRI(affordanceProp), affordanceId);
    graphBuilder.add(affordanceId, RDF.TYPE, rdf.createIRI(affordanceClass));
    graphBuilder.add(affordanceId, rdf.createIRI(TD.name), rdf.createLiteral(affordance.getName()));

    for (String type : affordance.getSemanticTypes()) {
      graphBuilder.add(affordanceId, RDF.TYPE, rdf.createIRI(type));
    }

    Optional<Map<String,DataSchema>> uriVariable = affordance.getUriVariables();
    if (uriVariable.isPresent()){
      Map<String,DataSchema> map=uriVariable.get();
      for (String key: map.keySet()){
        DataSchema value = map.get(key);
        Resource uriId = rdf.createBNode();
        graphBuilder.add(affordanceId, rdf.createIRI(TD.hasUriTemplateSchema), uriId);
        SchemaGraphWriter.write(graphBuilder, uriId, value);
        graphBuilder.add(uriId, rdf.createIRI(TD.name), key);
      }

    }

    if (affordance.getTitle().isPresent()) {
      graphBuilder.add(affordanceId, rdf.createIRI(DCT.title), affordance.getTitle().get());
    }

    addFormsForInteraction(affordanceId, affordance);

    return affordanceId;
  }

  private void addFormsForInteraction(Resource interactionId, InteractionAffordance interaction) {
    for (Form form : interaction.getForms()) {
      BNode formId = rdf.createBNode();

      graphBuilder.add(interactionId, rdf.createIRI(TD.hasForm), formId);

      // Only writes the method name for forms with one operation type (to avoid ambiguity)
      if (form.getMethodName().isPresent() && form.getOperationTypes().size() == 1) {
        if (Arrays.stream(HTTP_URI_SCHEMES).anyMatch(form.getTarget()::contains)) {
          graphBuilder.add(formId, rdf.createIRI(HTV.methodName), form.getMethodName().get());
        } else if (Arrays.stream(COAP_URI_SCHEMES).anyMatch(form.getTarget()::contains)) {
          graphBuilder.add(formId, rdf.createIRI(COV.methodName), form.getMethodName().get());
        }
      }
      graphBuilder.add(formId, rdf.createIRI(HCTL.hasTarget), rdf.createIRI(conversion(form.getTarget())));
      graphBuilder.add(formId, rdf.createIRI(HCTL.forContentType), form.getContentType());

      for (String opType : form.getOperationTypes()) {
        try {
          IRI opTypeIri = rdf.createIRI(opType);
          graphBuilder.add(formId, rdf.createIRI(HCTL.hasOperationType), opTypeIri);
        } catch (IllegalArgumentException e) {
          graphBuilder.add(formId, rdf.createIRI(HCTL.hasOperationType), opType);
        }
      }

      Optional<String> subProtocol = form.getSubProtocol();
      if (subProtocol.isPresent()) {
        try {
          IRI subProtocolIri = rdf.createIRI(subProtocol.get());
          graphBuilder.add(formId, rdf.createIRI(HCTL.forSubProtocol), subProtocolIri);
        } catch (IllegalArgumentException e) {
          graphBuilder.add(formId, rdf.createIRI(HCTL.forSubProtocol), subProtocol.get());
        }
      }
    }
  }

  private String write(RDFFormat format) {
    return ReadWriteUtils.writeToString(format, getModel());
  }

  private String conversion(String str){
    String newStr = "";
    for (int i = 0; i<str.length();i++){
      char c = str.charAt(i);
      if (c == '{'){
        newStr = newStr + "%7B";
      }
      else if (c == '}'){
        newStr = newStr + "%7D";
      }
      else {
        newStr = newStr + c;
      }
    }
    return newStr;
  }
}
