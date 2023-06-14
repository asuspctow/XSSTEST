package com.go2group.synapse.enums;

public enum TestParamScopeEnum {
  PROJECT_LEVEL(0),  TEST_CASE_LEVEL(1);
  
  private int scope;
  
  private TestParamScopeEnum(int scope) { this.scope = scope; }
  
  public int getScope()
  {
    return scope;
  }
  
  public static TestParamScopeEnum getTestParamScopeEnum(int scope) {
    switch (scope) {
    case 0: 
      return PROJECT_LEVEL;
    case 1: 
      return TEST_CASE_LEVEL;
    }
    return null;
  }
}
