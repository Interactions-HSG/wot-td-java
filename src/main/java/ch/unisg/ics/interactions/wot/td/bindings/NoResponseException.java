package ch.unisg.ics.interactions.wot.td.bindings;

import java.io.IOException;

/**
 * Exception thrown whenever the connection with the Thing is closed or broken before it responded
 * to a Consumer request during some WoT operation. Note that the Thing may respond with errors,
 * in which case this exception is not thrown. Instead, an instance of {@link Response} is returned
 * with an error response status.
 */
public class NoResponseException extends IOException {

  public NoResponseException() {
    super();
  }

  public NoResponseException(Throwable cause) {
    super(cause);
  }

}
