package com.go2group.synapse.enums;

public enum TestRunTypeEnum {
  MANUAL(Integer.valueOf(0)),  AUTOMATION(Integer.valueOf(1));
  
  private Integer value;
  
  private TestRunTypeEnum(Integer value) { this.value = value; }
  
  public Integer getValue()
  {
    return value;
  }
  
  public static TestRunTypeEnum getTestRunEnum(Integer value) {
    if (MANUAL.getValue().equals(value)) {
      return MANUAL;
    }
    return AUTOMATION;
  }
}
