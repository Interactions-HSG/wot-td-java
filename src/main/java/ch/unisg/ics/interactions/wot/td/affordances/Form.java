package ch.unisg.ics.interactions.wot.td.affordances;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ch.unisg.ics.interactions.wot.td.vocabularies.TD;

public class Form {
  private static final Map<String, String> DEFAULT_HTTP_BINDING = Stream.of(new String[][] { 
    { TD.readProperty, "GET" }, 
    { TD.writeProperty, "PUT" },
    { TD.invokeAction, "POST" },
  }).collect(Collectors.toMap(data -> data[0], data -> data[1]));
  
  private Optional<String> methodName;
  private final String target;
  private final String contentType;
  private final Set<String> operationTypes;
  
  private Form(String href, Optional<String> methodName, String mediaType, 
      Set<String> operationTypes) {
    this.methodName = methodName;
    this.target = href;
    this.contentType = mediaType;
    this.operationTypes = operationTypes;
  }

  public Optional<String> getMethodName() {
    return methodName;
  }
  
  public Optional<String> getMethodName(String operationType) {
    if (!operationTypes.contains(operationType)) {
      throw new IllegalArgumentException("Unknown operation type: " + operationType);
    }
    
    if (methodName.isPresent()) {
      return methodName;
    }
    
    if (DEFAULT_HTTP_BINDING.containsKey(operationType)) {
      return Optional.of(DEFAULT_HTTP_BINDING.get(operationType));
    }
    
    return Optional.empty();
  }
  
  public String getTarget() {
    return target;
  }

  public String getContentType() {
    return contentType;
  }
  
  public boolean hasOperationType(String type) {
    return operationTypes.contains(type);
  }
  
  public Set<String> getOperationTypes() {
    return operationTypes;
  }
  
  // Package-level access, used for setting affordance-specific default values after instantiation
  void setMethodName(String methodName) {
    this.methodName = Optional.of(methodName);
  }
  
  // Package-level access, used for setting affordance-specific default values after instantiation
  void addOperationType(String operationType) {
    this.operationTypes.add(operationType);
  }
  
  public static class Builder {
    private final String target;
    private Optional<String> methodName;
    private String contentType;
    private final Set<String> operationTypes;
    
    public Builder(String target) {
      this.target = target;
      this.methodName = Optional.empty();
      this.contentType = "application/json";
      this.operationTypes = new HashSet<String>();
    }
    
    public Builder addOperationType(String operationType) {
      this.operationTypes.add(operationType);
      return this;
    }
    
    public Builder addOperationTypes(Set<String> operationTypes) {
      this.operationTypes.addAll(operationTypes);
      return this;
    }
    
    public Builder setMethodName(String methodName) {
      this.methodName = Optional.of(methodName);
      return this;
    }
    
    public Builder setContentType(String contentType) {
      this.contentType = contentType;
      return this;
    }
    
    public Form build() {
      return new Form(this.target, this.methodName, this.contentType, this.operationTypes);
    }
    
  }
  
}
