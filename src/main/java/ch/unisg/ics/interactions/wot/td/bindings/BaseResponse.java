package ch.unisg.ics.interactions.wot.td.bindings;

import ch.unisg.ics.interactions.wot.td.affordances.Link;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    builder.append(String.format("[%s] %s", this.getClass().getSimpleName(), getStatus()));

    builder.append(String.format(", Links: "));

    if (!getLinks().isEmpty()) {
      for (Link lnk : getLinks()) {
        builder.append(String.format("<%s>; rel=\"%s\", ", lnk.getTarget(), lnk.getRelationType()));
      }
    } else {
      builder.append("<none>, ");
    }

    builder.append("Payload: ");

    if (getPayload().isPresent()) {
      String str = getOneLineString(getPayload().get());
      builder.append(str);
    } else {
      builder.append("<none>");
    }

    return builder.toString();
  }

  private String getOneLineString(Object obj) {
    Pattern p = Pattern.compile("([^\\r\\n]*)\\r?\\n");
    Matcher m = p.matcher(obj.toString());

    return m.find() ? m.group(1) + "..." : obj.toString();
  }

}
