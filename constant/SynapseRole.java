package com.go2group.synapse.constant;

public enum SynapseRole
{
  TESTER("tester"),  TEST_LEAD("test-lead"),  ANYONE("anyone");
  
  private String key;
  
  private SynapseRole(String key) {
    this.key = key;
  }
  
  public String getKey() {
    return key;
  }
  
  public static SynapseRole getRole(String key) {
    for (SynapseRole role : ) {
      if (role.getKey().equals(key)) {
        return role;
      }
    }
    
    return null;
  }
}
