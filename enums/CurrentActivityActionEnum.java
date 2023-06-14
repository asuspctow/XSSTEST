package com.go2group.synapse.enums;

public enum CurrentActivityActionEnum {
  ADD_MEMBER_THRU_SUITE("ADD_MEMBER_THRU_SUITE"), 
  ADD_AUDIT_LOG("ADD_AUDIT_LOG");
  
  private String name;
  
  private CurrentActivityActionEnum(String name) { this.name = name; }
  
  public String getName() {
    return name;
  }
}
