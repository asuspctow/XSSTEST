package com.go2group.synapse.enums;

public enum TestCycleActionEnum {
  CYCLE_ACTION_START("Start"), 
  CYCLE_ACTION_ABORT("Abort"), 
  CYCLE_ACTION_COMPLET("Complete"), 
  CYCLE_ACTION_RESUME("Resume");
  
  private final String key;
  
  private TestCycleActionEnum(String key) { this.key = key; }
  
  public String getKey()
  {
    return key;
  }
}
