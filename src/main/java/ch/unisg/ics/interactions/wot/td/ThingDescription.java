package ch.unisg.ics.interactions.wot.td;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.api.IRI;

import ch.unisg.ics.interactions.wot.td.interaction.Action;

public class ThingDescription {
  
  private IRI thingIRI;
  private Graph tdGraph;
  
  private Optional<String> thingName;
  private Optional<IRI> baseIRI;
  
  private Map<BlankNodeOrIRI,Action> actions;
  
  public ThingDescription(IRI thingIRI, Optional<String> thingName, Optional<IRI> baseIRI, 
      Map<BlankNodeOrIRI, Action> actions, Graph tdGraph) {
    
    this.thingIRI = thingIRI;
    this.thingName = thingName;
    
    this.baseIRI = baseIRI;
    this.actions = actions;
    
    this.tdGraph = tdGraph;
  }
  
  public IRI getThingIRI() {
    return thingIRI;
  }
  
  public Optional<String> getName() {
    return thingName;
  }
  
  public Optional<IRI> getBaseIRI() {
    return baseIRI;
  }
  
  public Set<IRI> getSupportedActionTypes() {
    Set<IRI> supportedActionTypes = new HashSet<IRI>();
    
    for (Entry<BlankNodeOrIRI, Action> entry : actions.entrySet()) {
      supportedActionTypes.addAll(entry.getValue().getTypes());
    }
    
    return supportedActionTypes;
  }
  
  public Optional<Action> getAction(IRI actionTypeIRI) {
    for (Entry<BlankNodeOrIRI, Action> entry : actions.entrySet()) {
      if (entry.getValue().getTypes().contains(actionTypeIRI)) {
        return Optional.of(entry.getValue());
      }
    }
    
    return Optional.empty();
  }
  
  public Graph getGraph() {
    return tdGraph;
  }
}
