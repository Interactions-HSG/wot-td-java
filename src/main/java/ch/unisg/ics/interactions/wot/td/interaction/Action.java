package ch.unisg.ics.interactions.wot.td.interaction;

import java.util.List;
import java.util.Optional;

import ch.unisg.ics.interactions.wot.td.schema.Schema;

/**
 * TODO: add javadoc
 * 
 * @author andreiciortea
 *
 */
public class Action extends Interaction {
  
  private Optional<Schema> inputSchema;
  
  public Action(Optional<String> name, List<String> types, List<HTTPForm> forms, 
      Optional<Schema> inputSchema) {
    super(name, types, forms);
    
    this.inputSchema = inputSchema;
  }
  
  public Optional<Schema> getInputSchema() {
    return inputSchema;
  }
}
