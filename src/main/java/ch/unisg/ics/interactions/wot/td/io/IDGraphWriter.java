package ch.unisg.ics.interactions.wot.td.io;

import ch.unisg.ics.interactions.wot.td.affordances.Form;
import ch.unisg.ics.interactions.wot.td.interaction.InteractionDescription;
import ch.unisg.ics.interactions.wot.td.interaction.InteractionTypes;
import ch.unisg.ics.interactions.wot.td.schemas.DataSchema;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.rio.RDFFormat;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

/**
 * A writer for serializing Interaction Descriptions as RDF graphs.
 * Provides a fluent API for adding prefix bindings to be used in the serialization.
 */
public class IDGraphWriter {
  private static final String[] HTTP_URI_SCHEMES = new String[]{"http:", "https:"};
  private final static String hctlNS = "https://www.w3.org/2019/wot/hypermedia#";
  private final static String htvNS = "http://www.w3.org/2011/http#";
  private final static String logNS = "https://example.org/log#";

  private final static String jsNS = "https://www.w3.org/2019/wot/json-schema#";

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
    graphBuilder.setNamespace("hctl", hctlNS);
    graphBuilder.setNamespace("htv", htvNS);
    graphBuilder.setNamespace("log", logNS);
    graphBuilder.setNamespace("js", jsNS);
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
      .addForm()
      .write(RDFFormat.TURTLE);
  }

  private String write(RDFFormat format) {
    return ReadWriteUtils.writeToString(format, getModel());
  }

  /**
   * Sets a prefix binding for a given namespace.
   *
   * @param prefix    the prefix to be used in the serialized representation
   * @param namespace the given namespace
   * @return this <code>IDGraphWriter</code>
   */
  public IDGraphWriter setNamespace(String prefix, String namespace) {
    this.graphBuilder.setNamespace(prefix, namespace);
    return this;
  }

  private IDGraphWriter addTitle() {
    graphBuilder.add(intdId, rdf.createIRI(logNS, "title"), intd.getTitle());
    return this;
  }

  private IDGraphWriter addURI() {
    if (intd.getUri() != null && !intd.getUri().isEmpty()) {
      graphBuilder.add(intdId, rdf.createIRI(logNS, "uri"), rdf.createIRI(intd.getUri()));
    }

    return this;
  }

  private IDGraphWriter addTimestamp() {
    OffsetDateTime dateTime = OffsetDateTime.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    String formattedTime = dateTime.format(formatter);
    graphBuilder.add(intdId, rdf.createIRI(logNS, "created"), formattedTime);
    return this;
  }

  /**
   * Adds the interaction input to the graph.
   * @return this <code>IDGraphWriter</code>
   */
  private IDGraphWriter addInput() {
    if(intd.getInput() == null) {
      return this;
    }

    BNode inputId = rdf.createBNode();
    graphBuilder.add(intdId, rdf.createIRI(logNS, "hasInput"), inputId);

    // Not all requests have a request body with a data schema
    if(intd.getInput().getValue() != null) {
      addValue(inputId, intd.getInput().getValue());
      addSchema(inputId, intd.getInput().getSchema());
    }

    graphBuilder.add(inputId, RDF.TYPE, rdf.createIRI(logNS, "Input"));
    return this;
  }

  /**
   * Adds the interaction output to the graph.
   * @return this <code>IDGraphWriter</code>
   */
  private IDGraphWriter addOutput() {
    if(intd.getOutput() == null) {
      return this;
    }

    BNode outputId = rdf.createBNode();
    graphBuilder.add(intdId, rdf.createIRI(logNS, "hasOutput"), outputId);

    // Not all interactions have a response body with a data schema
    if(intd.getOutput().getValue() != null) {
      addValue(outputId, intd.getOutput().getValue());
      addSchema(outputId, intd.getOutput().getSchema());
    }

    graphBuilder.add(outputId, RDF.TYPE, rdf.createIRI(logNS, "Output"));
    return this;
  }

  private void addValue(BNode nodeId, Object value) {
    graphBuilder.add(nodeId, rdf.createIRI(logNS, "hasValue"), value);
  }
  private void addSchema(Resource nodeId, DataSchema schema) {
    BNode schemaId = rdf.createBNode();
    graphBuilder.add(nodeId, rdf.createIRI(logNS, "hasSchema"), schemaId);
    SchemaGraphWriter.write(graphBuilder, schemaId, schema);
  }

  /**
   *
   */
  private IDGraphWriter addForm() {
    // Not all requests have a form
    if(intd.getForm() == null) {
      return this;
    }

    BNode formId = rdf.createBNode();
    graphBuilder.add(intdId, rdf.createIRI(logNS, "hasForm"), formId);

    Form form = intd.getForm();

    // Only writes the method name for forms with one operation type (to avoid ambiguity)
    if (form.getMethodName().isPresent() && form.getOperationTypes().size() == 1) {
      if (Arrays.stream(HTTP_URI_SCHEMES).anyMatch(form.getTarget()::contains)) {
        graphBuilder.add(formId, rdf.createIRI(htvNS, "methodName"), form.getMethodName().get());
      }
    }

    FormGraphWriter.write(graphBuilder, formId, form);
    return this;
  }

  private Model getModel() {
    return graphBuilder.build();
  }

  /**
   * Adds the type of the interaction to the graph.
   * @return this <code>IDGraphWriter</code>
   */
  private IDGraphWriter addType() {
    if (intd.getType() != null) {
      if (intd.getType().equals(InteractionTypes.PROPERTY)) {
        graphBuilder.add(intdId, RDF.TYPE, rdf.createIRI(logNS, "PropertyObservation"));
      } else if (intd.getType().equals(InteractionTypes.ACTION)) {
        graphBuilder.add(intdId, RDF.TYPE, rdf.createIRI(logNS, "ActionExecution"));
      } else if (intd.getType().equals(InteractionTypes.EVENT)) {
        graphBuilder.add(intdId, RDF.TYPE, rdf.createIRI(logNS, "Event"));
      }
    }
    return this;
  }
}
