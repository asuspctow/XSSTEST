package com.go2group.synapse.enums;

public enum AutomationTestTypeEnum {
  SELENIUM("SELENIUM"), 
  TESTNG("TESTNG"), 
  JUNIT("JUNIT"), 
  ROBOT("ROBOT");
  
  private final String key;
  
  private AutomationTestTypeEnum(String key) { this.key = key; }
  
  public String getKey()
  {
    return key;
  }
}
