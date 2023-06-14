package com.go2group.synapse.enums;

public enum AutomationErrorTypeEnum {
  SYNAPSE_ERROR("SynapseError");
  
  private String name;
  
  private AutomationErrorTypeEnum(String name) { this.name = name; }
  
  public String getName() {
    return name;
  }
}
