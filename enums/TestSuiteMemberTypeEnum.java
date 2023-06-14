package com.go2group.synapse.enums;

public enum TestSuiteMemberTypeEnum {
  TESTCASE(Integer.valueOf(0)),  TESTSUITE(Integer.valueOf(1));
  
  private Integer value;
  
  private TestSuiteMemberTypeEnum(Integer value) { this.value = value; }
  
  public Integer getValue() {
    return value;
  }
  
  public static TestSuiteMemberTypeEnum getTestSuiteMemberTypeEnum(Integer value) {
    if (TESTCASE.getValue().equals(value)) {
      return TESTCASE;
    }
    return TESTSUITE;
  }
}
