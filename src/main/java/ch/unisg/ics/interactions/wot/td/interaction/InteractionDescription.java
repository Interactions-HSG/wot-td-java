package ch.unisg.ics.interactions.wot.td.interaction;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

/**
 * This class represents an interaction description,
 * which is used to describe the interaction with a Thing in a machine-readable format (see also {@link ch.unisg.ics.interactions.wot.td.io.IDGraphWriter}).
 * Interactions can be an {@link InteractionTypes#ACTION}, an {@link InteractionTypes#EVENT}, or accessing a Thing {@link InteractionTypes#PROPERTY}.
 * They could be used e.g. to log a request to a Thing resource.
 */
@Builder
@Getter
public class InteractionDescription {
  private final String title;

  /**
   * The base URI of the interaction artifact.
   */
  private final String uri;

  /**
   * The input/initializing request for an interaction.
   */
  private final InteractionInput input;

  /**
   * The output/response of an interaction.
   */
  private final InteractionOutput output;

  /**
   * The type of the interaction e.g. {@link InteractionTypes#ACTION}.
   */
  private final InteractionTypes type;

  protected InteractionDescription(@NonNull String title, String uri, InteractionInput input, InteractionOutput output, InteractionTypes type) {
    this.title = title;
    this.uri = uri;
    this.input = input;
    this.output = output;
    this.type = type;
  }
}
