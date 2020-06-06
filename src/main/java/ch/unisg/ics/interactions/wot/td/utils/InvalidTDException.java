package ch.unisg.ics.interactions.wot.td.utils;

public class InvalidTDException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public InvalidTDException(String errorMessage) {
    super(errorMessage);
  }
  
  public InvalidTDException(String errorMessage, Exception e) {
    super(errorMessage, e);
  }
}
