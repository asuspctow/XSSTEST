package com.go2group.synapse.enums;

public enum AutomationRunStepEnum {
  READY(Integer.valueOf(0)),  RUNNING(Integer.valueOf(1)),  FINISHED(Integer.valueOf(2)),  ERRORED(Integer.valueOf(3));
  
  private Integer value;
  
  private AutomationRunStepEnum(Integer value) { this.value = value; }
  
  public Integer getValue() {
    return value;
  }
  
  public static AutomationRunStepEnum getAutomationRunStepEnum(Integer value) {
    switch (value.intValue()) {
    case 0: 
      return READY;
    case 1: 
      return RUNNING;
    case 2: 
      return FINISHED;
    }
    return null;
  }
}
