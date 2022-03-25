package ch.unisg.ics.interactions.wot.td.templates;

import ch.unisg.ics.interactions.wot.td.schemas.DataSchema;

import java.util.*;

/**
 * TODO: add javadoc
 *
 * @author Jérémy Lemée
 */
public class IOActionAffordanceTemplate extends InteractionAffordanceTemplate {
  final private Optional<DataSchema> input;
  final private Optional<DataSchema> output;

  // TODO: add safe, idempotent

  private IOActionAffordanceTemplate(String name, Optional<String> title, List<String> types,
                            Optional<Map<String,DataSchema>> uriVariables, Optional<DataSchema> input, Optional<DataSchema> output) {
    super(name, title, types, uriVariables);
    this.input = input;
    this.output = output;
  }


  public Optional<DataSchema> getInputSchema() {
    return input;
  }

  public Optional<DataSchema> getOutputSchema() {
    return output;
  }

  public static class Builder
    extends InteractionAffordanceTemplate.Builder<IOActionAffordanceTemplate, IOActionAffordanceTemplate.Builder> {

    private Optional<DataSchema> inputSchema;
    private Optional<DataSchema> outputSchema;

    public Builder(String name) {
      super(name);

      this.inputSchema = Optional.empty();
      this.outputSchema = Optional.empty();
    }


    public Builder addInputSchema(DataSchema inputSchema) {
      this.inputSchema = Optional.of(inputSchema);
      return this;
    }

    public Builder addOutputSchema(DataSchema outputSchema) {
      this.outputSchema = Optional.of(outputSchema);
      return this;
    }

    @Override
    public IOActionAffordanceTemplate build() {
      return new IOActionAffordanceTemplate(name, title, types, uriVariables, inputSchema, outputSchema);
    }
  }
}
