package ch.unisg.ics.interactions.wot.td.interaction;

public enum InteractionTypes {
  PROPERTY("property"),
  ACTION("action"),
  EVENT("event");

  private final String type;

  InteractionTypes(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }
}
