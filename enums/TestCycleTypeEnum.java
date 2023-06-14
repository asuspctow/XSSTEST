package com.go2group.synapse.enums;

public enum TestCycleTypeEnum {
  STANDARD_TEST_CYCLE_TYPE("Standard"), 
  ADVANCED_TEST_CYCLE_TYPE("Advanced");
  
  private final String key;
  
  private TestCycleTypeEnum(String key) { this.key = key; }
  
  public String getKey()
  {
    return key;
  }
}
