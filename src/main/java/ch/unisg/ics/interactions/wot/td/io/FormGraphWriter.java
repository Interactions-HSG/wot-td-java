package ch.unisg.ics.interactions.wot.td.io;

import ch.unisg.ics.interactions.wot.td.affordances.Form;
import ch.unisg.ics.interactions.wot.td.vocabularies.HCTL;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;

import java.util.Optional;

import static ch.unisg.ics.interactions.wot.td.io.ReadWriteUtils.conversion;

/**
 * A writer for serializing Thing Description or Interaction Description Forms as RDF graphs.
 */
class FormGraphWriter {
  private final ModelBuilder graphBuilder;
  private final ValueFactory rdf = SimpleValueFactory.getInstance();


  FormGraphWriter(ModelBuilder builder) {
    this.graphBuilder = builder;
  }

  static void write(ModelBuilder builder, BNode nodeId, Form form) {
    FormGraphWriter writer = new FormGraphWriter(builder);
    writer.addForm(nodeId, form);
  }

  protected void addForm(BNode formId, Form form) {
    this.graphBuilder.add(formId, rdf.createIRI(HCTL.hasTarget), rdf.createIRI(conversion(form.getTarget())));
    this.graphBuilder.add(formId, rdf.createIRI(HCTL.forContentType), form.getContentType());

    for (String opType : form.getOperationTypes()) {
      try {
        IRI opTypeIri = rdf.createIRI(opType);
        this.graphBuilder.add(formId, rdf.createIRI(HCTL.hasOperationType), opTypeIri);
      } catch (IllegalArgumentException e) {
        this.graphBuilder.add(formId, rdf.createIRI(HCTL.hasOperationType), opType);
      }
    }

    Optional<String> subProtocol = form.getSubProtocol();
    if (subProtocol.isPresent()) {
      try {
        IRI subProtocolIri = rdf.createIRI(subProtocol.get());
        this.graphBuilder.add(formId, rdf.createIRI(HCTL.forSubProtocol), subProtocolIri);
      } catch (IllegalArgumentException e) {
        this.graphBuilder.add(formId, rdf.createIRI(HCTL.forSubProtocol), subProtocol.get());
      }
    }
  }
}
