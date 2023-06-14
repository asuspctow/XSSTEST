package com.go2group.synapse.constant;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;

public enum SynapseCustomField
{
  TESTSTEP("synapse.field.customfield.teststep.name", "synapse.field.customfield.teststep.desc", "com.go2group.jira.plugin.synapse:testStepField"), 
  REQUIREMENT("synapse.field.customfield.requirement.name", "synapse.field.customfield.requirement.desc", "com.go2group.jira.plugin.synapse:requirementField"), 
  REQUIREMENT_LINK("synapse.field.customfield.requirement.link.name", "synapse.field.customfield.requirement.link.desc", "com.atlassian.jira.plugin.system.customfieldtypes:datetime"), 
  TESTSUITE("synapse.field.customfield.testsuite.name", "synapse.field.customfield.testsuite.desc", "com.go2group.jira.plugin.synapse:testSuiteField"), 
  RUNSTATUS("synapse.field.customfield.run.status.name", "synapse.field.customfield.run.status.desc", "com.atlassian.jira.plugin.system.customfieldtypes:select"), 
  TESTERSOFCYCLE("synapse.field.customfield.tester.name", "synapse.field.customfield.tester.desc", "com.atlassian.jira.plugin.system.customfieldtypes:userpicker"), 
  RESULTSOFCYCLE("synapse.field.customfield.result.name", "synapse.field.customfield.result.desc", "com.atlassian.jira.plugin.system.customfieldtypes:select"), 
  URGENCYOFRUN("synapse.field.customfield.urgency.name", "synapse.field.customfield.urgency.desc", "com.atlassian.jira.plugin.system.customfieldtypes:select"), 
  TEST_INFO("synapse.field.customfield.test.info.name", "synapse.field.customfield.test.info.desc", "com.go2group.jira.plugin.synapse:testInfoFieldType"), 
  EXECUTION_COUNT("synapse.field.customfield.execution.count.name", "synapse.field.customfield.execution.count.desc", "com.go2group.jira.plugin.synapse:executionCountFieldType"), 
  TESTCASE("synapse.field.customfield.testcase.name", "synapse.field.customfield.testcase.desc", "com.go2group.jira.plugin.synapse:testcaseField"), 
  PARENT_REQUIREMENT("synapse.field.customfield.parentrequirement.name", "synapse.field.customfield.parentrequirement.desc", "com.go2group.jira.plugin.synapse:requirementField"), 
  CHILD_REQUIREMENT("synapse.field.customfield.childrequirement.name", "synapse.field.customfield.childrequirement.desc", "com.go2group.jira.plugin.synapse:requirementField"), 
  REQUIREMENTSUITE("synapse.field.customfield.requirementsuite.name", "synapse.field.customfield.requirementsuite.desc", "com.go2group.jira.plugin.synapse:requirementSuiteField"), 
  RUNATTRIBUTES("synapse.field.customfield.runattributes.name", "synapse.field.customfield.runattributes.desc", "com.atlassian.jira.plugin.system.customfieldtypes:select");
  
  private String name;
  private String id;
  private String key;
  private I18nHelper i18nHelper;
  private String descriptionKey;
  private String description;
  private String fieldTypeKey;
  
  private SynapseCustomField(String key, String descriptionKey, String fieldTypeKey) {
    if (i18nHelper == null)
    {
      i18nHelper = ComponentAccessor.getI18nHelperFactory().getInstance(ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser());
    }
    this.key = key;
    name = i18nHelper.getText(key);
    id = (key + ".id");
    this.descriptionKey = descriptionKey;
    description = i18nHelper.getText(descriptionKey);
    this.fieldTypeKey = fieldTypeKey;
  }
  
  public String getName() {
    if ((org.apache.commons.lang3.StringUtils.isNotBlank(name)) && (name.equals(key)))
    {
      i18nHelper = ComponentAccessor.getI18nHelperFactory().getInstance(ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser());
      if (i18nHelper != null) {
        name = i18nHelper.getText(key);
      }
    }
    return name;
  }
  
  public String getKey() {
    return key;
  }
  
  public String getId() {
    return id;
  }
  
  public static SynapseCustomField getSynpaseCustomField(String name) {
    for (SynapseCustomField customField : ) {
      if (customField.getName().equals(name)) {
        return customField;
      }
    }
    return null;
  }
  
  public String getDescription() {
    return description;
  }
  
  public String getFieldTypeKey() {
    return fieldTypeKey;
  }
  
  public String getDescriptionKey() {
    return descriptionKey;
  }
}
