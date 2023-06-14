package com.go2group.synapse.enums;

public enum PreferencesModuleEnum
{
  RUN_DIALOG("RunDialog"),  TEST_CASE_WORD("TestCaseWord"),  TEST_CASE_CSV("TestCaseCsv"), 
  REQUIREMENT_WORD("RequirementWord"),  REQUIREMENT_CSV("RequirementCsv"),  BASELINE_WORD("BaselineWord"), 
  REQUIREMENTS_PAGE("RequirementsPage");
  
  private String name;
  
  private PreferencesModuleEnum(String name) { this.name = name; }
  
  public String getName() {
    return name;
  }
}
