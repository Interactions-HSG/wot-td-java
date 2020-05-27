package ch.unisg.ics.interactions.wot.td.schema;

import java.util.Map;

import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.api.IRI;

public abstract class Schema {
  
  protected BlankNodeOrIRI schemaIRI;
  protected Graph graph;
  
  public Schema(BlankNodeOrIRI schemaIRI, Graph graph) {
    this.schemaIRI = schemaIRI;
    this.graph = graph;
  }
  
  public abstract String instantiate(Map<IRI,Object> input);
  
}
