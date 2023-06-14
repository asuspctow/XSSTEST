package com.go2group.synapse.enums.dataintegrity;

public enum DataIntegrityEnum {
  REQUIREMENT_ASSOCIATION("requirement-association"), 
  REQUIREMENT_NUMBERING("requirement-numbering"), 
  TEST_CYCLE_VALIDITY("test-cycle-validity");
  
  String name;
  
  private DataIntegrityEnum(String name) { this.name = name; }
  
  public String getName()
  {
    return name;
  }
  
  public static DataIntegrityEnum getEnum(String name) {
    for (DataIntegrityEnum integrity : ) {
      if (integrity.getName().equals(name)) {
        return integrity;
      }
    }
    return null;
  }
}
