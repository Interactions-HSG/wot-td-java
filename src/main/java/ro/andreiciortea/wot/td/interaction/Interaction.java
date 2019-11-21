package ro.andreiciortea.wot.td.interaction;

import java.util.List;
import java.util.Optional;

import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.IRI;

public class Interaction {
  protected BlankNodeOrIRI iri;
  protected Optional<String> name;
  protected List<IRI> types;
  protected List<HTTPForm> forms;
  
  public Interaction(BlankNodeOrIRI iri, Optional<String> name, List<IRI> types, List<HTTPForm> forms) {
    this.iri = iri;
    this.name = name;
    this.types = types;
    this.forms = forms;
  }
  
  public BlankNodeOrIRI getIRI() {
    return iri;
  }

  public Optional<String> getName() {
    return name;
  }

  public List<IRI> getTypes() {
    return types;
  }

  public List<HTTPForm> getForms() {
    return forms;
  }
}
