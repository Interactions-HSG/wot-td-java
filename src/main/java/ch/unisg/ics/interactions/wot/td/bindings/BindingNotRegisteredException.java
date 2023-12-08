package ch.unisg.ics.interactions.wot.td.bindings;

public class BindingNotRegisteredException extends RuntimeException {

  public BindingNotRegisteredException(Throwable cause) {
    super(cause);
  }

  public BindingNotRegisteredException(String message, Throwable cause) {
    super(message, cause);
  }

}
