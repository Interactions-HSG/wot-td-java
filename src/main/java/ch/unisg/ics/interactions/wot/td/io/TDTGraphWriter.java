package ch.unisg.ics.interactions.wot.td.io;


import ch.unisg.ics.interactions.wot.td.ThingDescription;
import ch.unisg.ics.interactions.wot.td.templates.ThingDescriptionTemplate;
import ch.unisg.ics.interactions.wot.td.schemas.DataSchema;
import ch.unisg.ics.interactions.wot.td.templates.*;
import ch.unisg.ics.interactions.wot.td.vocabularies.*;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.rio.RDFFormat;

import java.util.Map;
import java.util.Optional;

/**
 * A writer for serializing TDs as RDF graphs. Provides a fluent API for adding prefix bindings to be
 * used in the serialization.
 */
public class TDTGraphWriter {
  private static final String[] HTTP_URI_SCHEMES = new String[]{"http:", "https:"};
  private static final String[] COAP_URI_SCHEMES = new String[]{"coap:", "coaps:"};

  private final Resource tdtId;
  private final ThingDescriptionTemplate tdt;
  private final ModelBuilder graphBuilder;
  private final ValueFactory rdf = SimpleValueFactory.getInstance();

  public TDTGraphWriter(ThingDescriptionTemplate tdt) {

    this.tdtId = rdf.createBNode();
    this.tdt = tdt;
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
  public TDTGraphWriter setNamespace(String prefix, String namespace) {
    this.graphBuilder.setNamespace(prefix, namespace);
    return this;
  }

  public String write() {
    return this.addTypes()
      .addTitle()
      .addProperties()
      .addActions()
      .addEvents()
      .addGraph()
      .write(RDFFormat.TURTLE);
  }

  private Model getModel() {
    return graphBuilder.build();
  }



  private TDTGraphWriter addTypes() {
    graphBuilder.add(tdtId, RDF.TYPE, rdf.createIRI(TD.Thing));

    for (String type : tdt.getSemanticTypes()) {
      graphBuilder.add(tdtId, RDF.TYPE, rdf.createIRI(type));
    }

    return this;
  }

  private TDTGraphWriter addTitle() {
    graphBuilder.add(tdtId, rdf.createIRI(TD.title), tdt.getTitle());
    return this;
  }



  private TDTGraphWriter addProperties() {
    for (PropertyAffordanceTemplate property : tdt.getProperties()) {
      Resource propertyId = addAffordance(property, TD.hasPropertyAffordance, TD.PropertyAffordance);
      graphBuilder.add(propertyId, rdf.createIRI(TD.isObservable), property.isObservable());

    }

    return this;
  }

  private TDTGraphWriter addActions() {
    for (ActionAffordanceTemplate action : tdt.getActions()) {
      Resource actionId = addAffordance(action, TD.hasActionAffordance, TD.ActionAffordance);

    }

    return this;
  }

  private TDTGraphWriter addEvents() {
    for (EventAffordanceTemplate event : tdt.getEvents()) {
      Resource eventId = addAffordance(event, TD.hasEventAffordance, TD.EventAffordance);

    }

    return this;
  }

  private TDTGraphWriter addGraph() {
    if (tdt.getGraph().isPresent()) {
      getModel().addAll(tdt.getGraph().get());

      tdt.getGraph().get().getNamespaces().stream()
        .filter(ns -> !getModel().getNamespace(ns.getPrefix()).isPresent())
        .forEach(graphBuilder::setNamespace);
    }
    return this;
  }

  private Resource addAffordance(InteractionAffordanceTemplate affordance, String affordanceProp,
                                 String affordanceClass) {
    BNode affordanceId = rdf.createBNode();

    graphBuilder.add(tdtId, rdf.createIRI(affordanceProp), affordanceId);
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
      graphBuilder.add(affordanceId, rdf.createIRI(TD.title), affordance.getTitle().get());
    }


    return affordanceId;
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
