package ch.unisg.ics.interactions.wot.td.io;

import ch.unisg.ics.interactions.wot.td.interaction.InteractionDescription;
import ch.unisg.ics.interactions.wot.td.interaction.InteractionTypes;
import ch.unisg.ics.interactions.wot.td.affordances.Form;
import ch.unisg.ics.interactions.wot.td.schemas.DataSchema;
import ch.unisg.ics.interactions.wot.td.vocabularies.HCTL;
import ch.unisg.ics.interactions.wot.td.vocabularies.TD;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.rio.RDFFormat;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Optional;

/**
 * A writer for serializing Interaction Descriptions as RDF graphs.
 * Provides a fluent API for adding prefix bindings to be used in the serialization.
 */
public class IDGraphWriter {
  private static final String[] HTTP_URI_SCHEMES = new String[]{"http:", "https:"};
  private static final String[] COAP_URI_SCHEMES = new String[]{"coap:", "coaps:"};
  private final static String tdNS = TD.PREFIX;
  private final static String hctlNS = "https://www.w3.org/2019/wot/hypermedia#";
  private final static String htvNS = "http://www.w3.org/2011/http#";
  private final static String logNS = "https://example.org/log#";

  private final Resource intdId;
  private final InteractionDescription intd;
  private final ModelBuilder graphBuilder;
  private final ValueFactory rdf = SimpleValueFactory.getInstance();

  /**
   * @param intd the Interaction Description to be serialized
   */
  public IDGraphWriter(InteractionDescription intd) {
    this.intdId = intd.getTitle() != null ? rdf.createIRI(intd.getUri())
      : rdf.createBNode();

    this.intd = intd;
    this.graphBuilder = new ModelBuilder();

    // Default namespace bindings
    graphBuilder.setNamespace("td", tdNS);
    graphBuilder.setNamespace("hctl", hctlNS);
    graphBuilder.setNamespace("log", logNS);
    graphBuilder.setNamespace("htv", htvNS);
  }

  /**
   * @param intd the Interaction Description to be serialized
   * @return the serialized Interaction Description as RDF graph in Turtle format
   */
  public static String write(InteractionDescription intd) {
    return new IDGraphWriter(intd).write();
  }

  public String write() {
    return this
      .addTitle()
      .addURI()
      .addTimestamp()
      .addInput()
      .addOutput()
      .addType()
      .write(RDFFormat.TURTLE);
  }

  private String write(RDFFormat format) {
    return ReadWriteUtils.writeToString(format, getModel());
  }

  private IDGraphWriter addTitle() {
    graphBuilder.add(intdId, rdf.createIRI(TD.title), intd.getTitle());
    return this;
  }

  private IDGraphWriter addURI() {
    if (intd.getUri() != null && !intd.getUri().isEmpty()) {
      graphBuilder.add(intdId, rdf.createIRI(TD.hasBase), rdf.createIRI(intd.getUri()));
    }

    return this;
  }

  private IDGraphWriter addTimestamp() {
    OffsetDateTime dateTime = OffsetDateTime.now();
    String formattedTime = dateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    graphBuilder.add(intdId, rdf.createIRI(logNS, "created"), formattedTime);
    return this;
  }

  private IDGraphWriter addInput() {
    BNode inputId = rdf.createBNode();
    graphBuilder.add(intdId, rdf.createIRI(tdNS, "hasInput"), inputId);
    graphBuilder.add(inputId, rdf.createIRI(tdNS, "value"), intd.getInput().getValue());
    addForm(inputId, intd.getInput().getForm());
    addSchema(inputId, intd.getInput().getSchema());
    return this;
  }

  private IDGraphWriter addOutput() {
    Resource outputId = rdf.createBNode();
    graphBuilder.add(intdId, rdf.createIRI(tdNS, "hasOutput"), outputId);
    graphBuilder.add(outputId, rdf.createIRI(tdNS, "value"), intd.getInput().getValue());
    addSchema(outputId, intd.getInput().getSchema());
    return this;
  }

  private void addSchema(Resource nodeId, DataSchema schema) {
    SchemaGraphWriter.write(graphBuilder, nodeId, schema);
  }

  private void addForm(BNode inputId, Form form) {
    BNode formId = rdf.createBNode();
    graphBuilder.add(inputId, rdf.createIRI(TD.hasForm), formId);

    // Only writes the method name for forms with one operation type (to avoid ambiguity)
    if (form.getMethodName().isPresent() && form.getOperationTypes().size() == 1) {
      if (Arrays.stream(HTTP_URI_SCHEMES).anyMatch(form.getTarget()::contains)) {
        graphBuilder.add(formId, rdf.createIRI(htvNS, "methodName"), form.getMethodName().get());
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

  private Model getModel() {
    return graphBuilder.build();
  }

  private IDGraphWriter addType() {
    if (intd.getType() != null) {
      if (intd.getType().equals(InteractionTypes.PROPERTY)) {
        graphBuilder.add(intdId, RDF.TYPE, rdf.createIRI(logNS, "PropertyLog"));
      } else if (intd.getType().equals(InteractionTypes.ACTION)) {
        graphBuilder.add(intdId, RDF.TYPE, rdf.createIRI(logNS, "ActionLog"));
      } else if (intd.getType().equals(InteractionTypes.EVENT)) {
        graphBuilder.add(intdId, RDF.TYPE, rdf.createIRI(logNS, "EventLog"));
      }
    }
    return this;
  }


  private String conversion(String str) {
    StringBuilder newStr = new StringBuilder();
    for (int i = 0; i < str.length(); i++) {
      char c = str.charAt(i);
      if (c == '{') {
        newStr.append("%7B");
      } else if (c == '}') {
        newStr.append("%7D");
      } else {
        newStr.append(c);
      }
    }
    return newStr.toString();
  }
}
