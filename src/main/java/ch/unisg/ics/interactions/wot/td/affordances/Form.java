package ch.unisg.ics.interactions.wot.td.affordances;

import ch.unisg.ics.interactions.wot.td.bindings.BindingNotFoundException;
import ch.unisg.ics.interactions.wot.td.bindings.ProtocolBindings;

import java.util.*;

public class Form {

  private final String target;
  private final String contentType;
  private final Set<String> operationTypes;
  private final Optional<String> subProtocol;
  private final Map<String, Object> additionalProperties = new HashMap<>();
  private Optional<String> methodName;

  private Form(String href, Optional<String> methodName, String mediaType, Set<String> operationTypes,
               Optional<String> subProtocol) {
    this.methodName = methodName;
    this.target = href;
    this.contentType = mediaType;
    this.operationTypes = operationTypes;
    this.subProtocol = subProtocol;
  }

  private Form(String href, Optional<String> methodName, String mediaType, Set<String> operationTypes,
               Optional<String> subProtocol, Map<String, Object> additionalProperties) {
    this(href, methodName, mediaType, operationTypes, subProtocol);
    this.additionalProperties.putAll(additionalProperties);
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

    try {
      return ProtocolBindings.getBinding(this).getDefaultMethod(operationType);
    } catch (BindingNotFoundException e) {
      return Optional.empty();
    }
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

  public Optional<String> getSubProtocol() {
    return subProtocol;
  }

  public Map<String, Object> getAdditionalProperties() {
    return additionalProperties;
  }

  // Package-level access, used for setting affordance-specific default values after instantiation
  // Reserved for event affordances of op subscribeevent
  /*
  void addSubProtocol(String subProtocol) {
    this.subProtocol = Optional.of(subProtocol);
  }
  */

  public boolean hasSubProtocol(String operationType, String subProtocol) {
    Optional<String> targetSubProtocol = getSubProtocol(operationType);
    return targetSubProtocol.isPresent() && subProtocol.equals(targetSubProtocol.get());
  }

  public Optional<String> getSubProtocol(String operationType) {
    if (!operationTypes.contains(operationType)) {
      throw new IllegalArgumentException("Unknown operation type: " + operationType);
    }

    if (subProtocol.isPresent()) {
      return subProtocol;
    }

    try {
      return ProtocolBindings.getBinding(this).getDefaultSubProtocol(operationType);
    } catch (BindingNotFoundException e) {
      return Optional.empty();
    }
  }

  public boolean hasProtocol(String protocol) {
    try {
      return protocol.equals(ProtocolBindings.getBinding(this).getProtocol());
    } catch (BindingNotFoundException e) {
      return false;
    }
  }

  public Optional<String> getProtocol() {
    try {
      return Optional.of(ProtocolBindings.getBinding(this).getProtocol());
    } catch (BindingNotFoundException e) {
      return Optional.empty();
    }
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
    private Optional<String> subProtocol;
    private Map<String, Object> additionalProperties;

    public Builder(String target) {
      this.target = target;
      this.methodName = Optional.empty();
      this.contentType = "application/json";
      this.operationTypes = new HashSet<String>();
      this.subProtocol = Optional.empty();
      this.additionalProperties = new HashMap<>();
    }

    public Builder(String target, Form model) {
      this.target = target;
      this.methodName = model.getMethodName();
      this.contentType = model.getContentType();
      this.operationTypes = model.getOperationTypes();
      this.subProtocol = model.getSubProtocol();
      this.additionalProperties = model.getAdditionalProperties();
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

    public Builder addSubProtocol(String subProtocol) {
      this.subProtocol = Optional.of(subProtocol);
      return this;
    }

    public Builder addProperty(String key, Object value) {
      this.additionalProperties.put(key, value);
      return this;
    }

    public Form build() {
      return new Form(this.target, this.methodName, this.contentType, this.operationTypes,
        this.subProtocol, this.additionalProperties);
    }

  }

}
