package ch.unisg.ics.interactions.wot.td;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import ch.unisg.ics.interactions.wot.td.interaction.Action;

/**
 * TODO: add javadoc
 * ThingDescription is immutable (hence the builder pattern)
 * can be extended
 * fields follow the W3C WoT spec: https://www.w3.org/TR/wot-thing-description/#thing
 * 
 * @author andreiciortea
 *
 */
public class ThingDescription {
  // A human-readbale title of the Thing (required)
  private String title;
  
  // Identifier of the Thing in form of a URI
  private Optional<String> uri;
  // The base URI that is used for all relative URI references throughout a TD document
  private Optional<String> baseURI;
  // All Action-based interaction affordances of the Thing
  private List<Action> actions;
  
  protected ThingDescription(String title, Optional<String> uri, Optional<String> baseURI, 
      List<Action> actions) {
    
    this.uri = uri;
    this.title = title;
    
    this.baseURI = baseURI;
    this.actions = actions;
  }
  
  public String getTitle() {
    return title;
  }
  
  public Optional<String> getThingURI() {
    return uri;
  }
  
  public Optional<String> getBaseURI() {
    return baseURI;
  }
  
  public Set<String> getSupportedActionTypes() {
    Set<String> supportedActionTypes = new HashSet<String>();
    
    for (Action action : actions) {
      supportedActionTypes.addAll(action.getTypes());
    }
    
    return supportedActionTypes;
  }
  
  // TODO: returns only the first action of a given type
  public Optional<Action> getAction(String actionType) {
    for (Action action : actions) {
      if (action.getTypes().contains(actionType)) {
        return Optional.of(action);
      }
    }
    
    return Optional.empty();
  }
  
  public static class Builder {
    private String title;
    private Optional<String> uri;
    private Optional<String> baseURI;
    private List<Action> actions;
    
    public Builder(String title) {
      this.title = title;
      this.baseURI = Optional.empty();
      
      this.actions = new ArrayList<Action>();
    }
    
    public Builder addURI(String uri) {
      this.uri = Optional.of(uri);
      return this;
    }
    
    public Builder addBaseURI(String baseURI) {
      this.baseURI = Optional.of(baseURI);
      return this;
    }
    
    public Builder addAction(Action action) {
      this.actions.add(action);
      return this;
    }
    
    public Builder addActions(List<Action> actions) {
      this.actions.addAll(actions);
      return this;
    }
    
    public ThingDescription build() {
      return new ThingDescription(title, uri, baseURI, actions);
    }
    
  }
}
