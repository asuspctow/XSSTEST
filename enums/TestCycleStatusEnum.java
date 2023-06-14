package com.go2group.synapse.enums;

public enum TestCycleStatusEnum {
  CYCLE_STATUS_DRAFT("Draft"),  CYCLE_STATUS_ACTIVE("Active"), 
  CYCLE_STATUS_COMPLETED("Completed"),  CYCLE_STATUS_ABORTED("Aborted");
  
  private final String key;
  
  private TestCycleStatusEnum(String key) { this.key = key; }
  
  public String getKey()
  {
    return key;
  }
}
