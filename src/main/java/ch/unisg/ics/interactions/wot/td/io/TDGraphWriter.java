package ch.unisg.ics.interactions.wot.td.io;

import java.util.List;
import java.util.Optional;

import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.rio.RDFFormat;

import ch.unisg.ics.interactions.wot.td.ThingDescription;
import ch.unisg.ics.interactions.wot.td.affordances.ActionAffordance;
import ch.unisg.ics.interactions.wot.td.affordances.Form;
import ch.unisg.ics.interactions.wot.td.affordances.InteractionAffordance;
import ch.unisg.ics.interactions.wot.td.affordances.PropertyAffordance;
import ch.unisg.ics.interactions.wot.td.schemas.DataSchema;
import ch.unisg.ics.interactions.wot.td.security.SecurityScheme;
import ch.unisg.ics.interactions.wot.td.vocabularies.DCT;
import ch.unisg.ics.interactions.wot.td.vocabularies.HCTL;
import ch.unisg.ics.interactions.wot.td.vocabularies.HTV;
import ch.unisg.ics.interactions.wot.td.vocabularies.TD;

/**
 * A writer for serializing TDs as RDF graphs. Provides a fluent API for adding prefix bindings to be
 * used in the serialization.
 *
 */
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
    return new TDGraphWriter(td).write();
  }

  /**
   * Sets a prefix binding for a given namespace.
   *
   * @param prefix the prefix to be used in the serialized representation
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
        .addGraph()
        .write(RDFFormat.TURTLE);
  }

  private Model getModel() {
    return graphBuilder.build();
  }

  private TDGraphWriter addSecurity() {
    List<SecurityScheme> securitySchemes = td.getSecuritySchemes();

    for (SecurityScheme scheme : securitySchemes) {
      BNode schemeId = rdf.createBNode();
      graphBuilder.add(thingId, rdf.createIRI(TD.hasSecurityConfiguration), schemeId);

      Model schemeGraph = scheme.toRDF(schemeId);
      for (Statement s : schemeGraph) {
        graphBuilder.add(s.getSubject(), s.getPredicate(), s.getObject());
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

  private TDGraphWriter addGraph() {
  	if(td.getGraph().isPresent()) {
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

    for (String type : affordance.getSemanticTypes()) {
      graphBuilder.add(affordanceId, RDF.TYPE, rdf.createIRI(type));
    }

    for (DataSchema uriVariable : affordance.getUriVariables()){
      Resource inputId = rdf.createBNode();
      graphBuilder.add(affordanceId, rdf.createIRI(TD.hasUriTemplateSchema), inputId);

      SchemaGraphWriter.write(graphBuilder, inputId, uriVariable);

    }

    if (affordance.getName().isPresent()) {
      graphBuilder.add(affordanceId, rdf.createIRI(TD.name),
          rdf.createLiteral(affordance.getName().get()));
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
        graphBuilder.add(formId, rdf.createIRI(HTV.methodName), form.getMethodName().get());
      }
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

      Optional<String> subprotocol = form.getSubProtocol();
      if (subprotocol.isPresent()) {
        graphBuilder.add(formId, rdf.createIRI(HCTL.forSubProtocol), subprotocol.get());
      }
    }
  }

  private String write(RDFFormat format) {
    return ReadWriteUtils.writeToString(format, getModel());
  }
}
