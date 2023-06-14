package com.go2group.synapse.enums;

public enum RequirementsFieldNameEnum
{
  NUMBERING("Numbering"),  COMPONENT("Component"),  FIX_VERSION("FixVersion"),  SPRINT("Sprint"), 
  TEST_CASE("TestCase"),  TEST_PLAN("TestPlan");
  
  private String name;
  
  private RequirementsFieldNameEnum(String name) { this.name = name; }
  
  public String getName()
  {
    return name;
  }
  
  public static RequirementsFieldNameEnum getByName(String name) {
    for (RequirementsFieldNameEnum field : ) {
      if (field.getName().equals(name)) {
        return field;
      }
    }
    return null;
  }
}
