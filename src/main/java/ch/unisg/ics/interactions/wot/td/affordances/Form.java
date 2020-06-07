package ch.unisg.ics.interactions.wot.td.affordances;

import java.util.HashSet;
import java.util.Set;

public class Form {
  private String methodName;
  private String target;
  private String mediaType;
  private Set<String> operationTypes;
  
  public Form(String methodName, String target) {
    this(methodName, target, "application/json", new HashSet<String>());
  }
  
  public Form(String methodName, String href, String mediaType, Set<String> operationTypes) {
    this.methodName = methodName;
    this.target = href;
    this.mediaType = mediaType;
    this.operationTypes = operationTypes;
  }

  public String getMethodName() {
    return methodName;
  }

  public String getTarget() {
    return target;
  }

  public String getContentType() {
    return mediaType;
  }
  
  public Set<String> getOperationTypes() {
    return operationTypes;
  }
  
  public boolean hasOperationType(String type) {
    return operationTypes.contains(type);
  }
}
