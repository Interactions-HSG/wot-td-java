package ch.unisg.ics.interactions.wot.td.interaction;

import java.util.List;
import java.util.Optional;

import ch.unisg.ics.interactions.wot.td.schema.Schema;

/**
 * TODO: add javadoc
 * 
 * @author Andrei Ciortea
 *
 */
public class Action extends Interaction {
  private Optional<Schema> inputSchema;
  
  protected Action(Optional<String> title, List<String> types, List<HTTPForm> forms, 
      Optional<Schema> inputSchema) {
    super(title, types, forms);
    
    this.inputSchema = inputSchema;
  }
  
  public Optional<Schema> getInputSchema() {
    return inputSchema;
  }
  
  public static class Builder extends Interaction.Builder<Action, Action.Builder> {
    private Optional<Schema> inputSchema;
    
    public Builder() {
      super();
      
      this.inputSchema = Optional.empty();
    }
    
    public Builder addInputSchema(Schema inputSchema) {
      this.inputSchema = Optional.of(inputSchema);
      return this;
    }
    
    public Action build() {
      return new Action(this.title, this.types, this.forms, inputSchema);
    }
  }
}
