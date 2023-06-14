package com.go2group.synapse.enums;

public enum ReqTreeItemInfoNameEnum {
  TEST_CASE_COUNT_ENABLED("testCaseCountEnabled"),  TEST_PLAN_COUNT_ENABLED("testPlanCountEnabled");
  
  private String name;
  
  private ReqTreeItemInfoNameEnum(String name) { this.name = name; }
  
  public String getName()
  {
    return name;
  }
}
