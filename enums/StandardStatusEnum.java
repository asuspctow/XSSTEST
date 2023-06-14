package com.go2group.synapse.enums;

public enum StandardStatusEnum {
  PASSED("synapse.status.testrun.passed", "Passed"), 
  FAILED("synapse.status.testrun.failed", "Failed"), 
  BLOCKED("synapse.status.testrun.blocked", "Blocked"), 
  NOT_TESTED("synapse.status.testrun.not.tested", "Not Tested"), 
  NOT_APPLICABLE("synapse.status.testrun.na", "NA");
  
  private final String name;
  private final String key;
  
  private StandardStatusEnum(String key, String name) {
    this.key = key;
    this.name = name;
  }
  
  public String getName() {
    return name;
  }
  
  public String getKey() {
    return key;
  }
  
  public static StandardStatusEnum getEnum(String name) {
    for (StandardStatusEnum status : ) {
      if (name.equals(status.getName())) {
        return status;
      }
    }
    return null;
  }
}
