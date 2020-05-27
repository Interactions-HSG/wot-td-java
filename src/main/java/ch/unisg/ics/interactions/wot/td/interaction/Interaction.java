package ch.unisg.ics.interactions.wot.td.interaction;

import java.util.List;
import java.util.Optional;

/**
 * TODO: add javadoc
 * 
 * @author andreiciortea
 *
 */
public class Interaction {
  protected Optional<String> title;
  protected List<String> types;
  protected List<HTTPForm> forms;
  
  protected Interaction(Optional<String> title, List<String> types, List<HTTPForm> forms) {
    this.title = title;
    this.types = types;
    this.forms = forms;
  }
  
  public Optional<String> getTitle() {
    return title;
  }
  
  public List<String> getTypes() {
    return types;
  }
  
  public List<HTTPForm> getForms() {
    return forms;
  }
}
