package ch.unisg.ics.interactions.wot.td.bindings;

import ch.unisg.ics.interactions.wot.td.affordances.Link;

public abstract class BaseResponse implements Response {

  protected final Operation operation;

  public BaseResponse(Operation op) {
    operation = op;
  }

  @Override
  public Operation getOperation() {
    return operation;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();

    builder.append(String.format("[%s] %s", this.getClass().getName(), getStatus()));

    builder.append(String.format(", Links: "));
    for (Link lnk : getLinks()) {
      builder.append(String.format("<%s>; rel=\"%s\", ", lnk.getTarget(), lnk.getRelationType()));
    }

    builder.append(String.format("Payload: %s", getPayload()));

    return builder.toString();
  }

}
