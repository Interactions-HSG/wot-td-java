package ch.unisg.ics.interactions.wot.td.bindings;

public class InvalidFormException extends RuntimeException {

  public InvalidFormException(Throwable cause) {
    super(cause);
  }

  public InvalidFormException(String message, Throwable cause) {
    super(message, cause);
  }

}
