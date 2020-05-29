package ch.unisg.ics.interactions.wot.td.affordances;

import java.util.Set;

public class HTTPForm {
  private String methodName;
  private String href;
  private String mediaType;
  private Set<String> ops;
  
  public HTTPForm(String methodName, String href, String mediaType, Set<String> op) {
    this.methodName = methodName;
    this.href = href;
    this.mediaType = mediaType;
    this.ops = op;
  }

  public String getMethodName() {
    return methodName;
  }

  public String getHref() {
    return href;
  }

  public String getContentType() {
    return mediaType;
  }

  public Set<String> getOperations() {
    return ops;
  }
}
