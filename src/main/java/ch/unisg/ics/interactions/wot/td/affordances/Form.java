package ch.unisg.ics.interactions.wot.td.affordances;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class Form {

  private final String target;
  private final String contentType;
  private final Set<String> operationTypes;
  private final Optional<String> subprotocol;
  private Optional<String> methodName;

  private Form(String href, Optional<String> methodName, String mediaType, Set<String> operationTypes,
               Optional<String> subprotocol) {
    this.methodName = methodName;
    this.target = href;
    this.contentType = mediaType;
    this.operationTypes = operationTypes;
    this.subprotocol = subprotocol;
  }

  public Optional<String> getMethodName() {
    return methodName;
  }

  // Package-level access, used for setting affordance-specific default values after instantiation
  void setMethodName(String methodName) {
    this.methodName = Optional.of(methodName);
  }

  public Optional<String> getMethodName(String operationType) {
    if (!operationTypes.contains(operationType)) {
      throw new IllegalArgumentException("Unknown operation type: " + operationType);
    }

    if (methodName.isPresent()) {
      return methodName;
    }

    return ProtocolBinding.getDefaultMethod(target, operationType);
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

  public Optional<String> getSubprotocol() {
    return subprotocol;
  }

  // Package-level access, used for setting affordance-specific default values after instantiation
  // Reserved for event affordances of op subscribeevent
  /*
  void addSubprotocol(String subprotocol) {
    this.subprotocol = Optional.of(subprotocol);
  }
  */

  public Optional<String> getSubprotocol(String operationType) {
    if (!operationTypes.contains(operationType)) {
      throw new IllegalArgumentException("Unknown operation type: " + operationType);
    }

    if (subprotocol.isPresent()) {
      return subprotocol;
    }

    return ProtocolBinding.getDefaultSubprotocol(target, operationType);
  }

  // Package-level access, used for setting affordance-specific default values after instantiation
  void addOperationType(String operationType) {
    this.operationTypes.add(operationType);
  }

  public static class Builder {
    private final String target;
    private final Set<String> operationTypes;
    private Optional<String> methodName;
    private String contentType;
    private Optional<String> subprotocol;

    public Builder(String target) {
      this.target = target;
      this.methodName = Optional.empty();
      this.contentType = "application/json";
      this.operationTypes = new HashSet<String>();
      this.subprotocol = Optional.empty();
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

    public Builder addSubProtocol(String subprotocol) {
      this.subprotocol = Optional.of(subprotocol);
      return this;
    }

    public Form build() {
      return new Form(this.target, this.methodName, this.contentType, this.operationTypes,
        this.subprotocol);
    }

  }

}
