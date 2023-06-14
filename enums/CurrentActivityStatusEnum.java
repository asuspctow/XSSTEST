package com.go2group.synapse.enums;

public enum CurrentActivityStatusEnum {
  STARTED("STARTED"),  PROCESSING("PROCESSING"),  COMPLETED("COMPLETED");
  
  private String name;
  
  private CurrentActivityStatusEnum(String name) {
    this.name = name;
  }
  
  public String getName() {
    return name;
  }
}
