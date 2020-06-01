package ch.unisg.ics.interactions.wot.td.affordances;

import java.util.HashSet;
import java.util.Set;

public class Form {
  private String methodName;
  private String href;
  private String mediaType;
  private Set<String> ops;
  
  public Form(String methodName, String href) {
    this(methodName, href, "application/json", new HashSet<String>());
  }
  
  public Form(String methodName, String href, String mediaType, Set<String> op) {
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
