package com.go2group.synapse.enums;

public enum StatusCategoryEnum {
  EXECUTED("synapse.config.run.status.category.executed.name", "Executed"), 
  NOT_EXECUTED("synapse.config.run.status.category.not.executed.name", "Not Executed");
  
  private final String key;
  private final String name;
  
  private StatusCategoryEnum(String key, String name) {
    this.key = key;
    this.name = name;
  }
  
  public String getKey() {
    return key;
  }
  
  public String getName() {
    return name;
  }
  
  public static StatusCategoryEnum getByName(String name) {
    if (name.equalsIgnoreCase("Executed")) {
      return EXECUTED;
    }
    if (name.equalsIgnoreCase("Not Executed")) {
      return NOT_EXECUTED;
    }
    return null;
  }
}
