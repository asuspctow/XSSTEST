package com.go2group.synapse.enums;

public enum AutomationTestCaseStatusEnum {
  TEST_CASE_STATUS_PASSED("Passed"),  TEST_CASE_STATUS_FAILED("Failed"),  TEST_CASE_STATUS_ERROR("Error");
  
  private String name;
  
  private AutomationTestCaseStatusEnum(String name) { this.name = name; }
  
  public String getName()
  {
    return name;
  }
}
