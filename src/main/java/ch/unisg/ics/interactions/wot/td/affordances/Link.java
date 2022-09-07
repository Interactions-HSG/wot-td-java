package ch.unisg.ics.interactions.wot.td.affordances;

public class Link {

  private final String target;

  private final String relationType;

  public Link(String target, String rel) {
    this.target = target;
    this.relationType = rel;
  }

  public String getTarget() {
    return this.target;
  }

  public String getRelationType() {
    return relationType;
  }
}
