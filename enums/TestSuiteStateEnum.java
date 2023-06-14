package com.go2group.synapse.enums;

public enum TestSuiteStateEnum {
  ENABLED(Integer.valueOf(1)),  DISABLED(Integer.valueOf(0));
  
  private Integer value;
  
  private TestSuiteStateEnum(Integer value) { this.value = value; }
  
  public Integer getValue() {
    return value;
  }
  
  public static TestSuiteStateEnum getTestSuiteEnum(Integer value) {
    if (value.intValue() == 0) {
      return DISABLED;
    }
    return ENABLED;
  }
}
