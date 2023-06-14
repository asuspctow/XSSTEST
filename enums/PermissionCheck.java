package com.go2group.synapse.enums;

public enum PermissionCheck
{
  DO(true),  DO_NOT(false);
  
  boolean value;
  
  private PermissionCheck(boolean permission) { value = permission; }
  
  public boolean getValue() {
    return value;
  }
}
