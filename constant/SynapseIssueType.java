package com.go2group.synapse.constant;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager.CONSTANT_TYPE;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.I18nHelper.BeanFactory;
import org.apache.log4j.Logger;

public enum SynapseIssueType
{
  REQUIREMENT("synapse.config.requirement.name"),  TEST_CASE("synapse.config.test.case.name"),  TEST_PLAN("synapse.config.test.plan.name"),  BUG("synapse.config.bug.name");
  
  private String name;
  private String id;
  private String key;
  private I18nHelper i18nHelper;
  private static final Logger log = Logger.getLogger(SynapseIssueType.class);
  
  private SynapseIssueType(String key) { if (i18nHelper == null) {
      i18nHelper = ComponentAccessor.getI18nHelperFactory().getInstance(ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser());
    }
    this.key = key;
    name = i18nHelper.getText(key);
    id = (key + ".id");
  }
  
  public String getName()
  {
    ApplicationProperties properties = ComponentAccessor.getApplicationProperties();
    String id = properties.getString(key);
    
    if (org.apache.commons.lang3.StringUtils.isBlank(id)) {
      i18nHelper = ComponentAccessor.getI18nHelperFactory().getInstance(ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser());
      if (i18nHelper != null) {
        name = i18nHelper.getText(key);
      }
    }
    else {
      com.atlassian.jira.issue.IssueConstant issueConstant = ComponentAccessor.getConstantsManager().getConstantObject(ConstantsManager.CONSTANT_TYPE.ISSUE_TYPE.getType(), id);
      if (issueConstant != null) {
        name = issueConstant.getName();
      }
    }
    


    if ((org.apache.commons.lang3.StringUtils.isNotBlank(name)) && (name.equals(key)))
    {
      i18nHelper = ComponentAccessor.getI18nHelperFactory().getInstance(ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser());
      if (i18nHelper != null) {
        name = i18nHelper.getText(key);
      }
      
      log.debug("name : " + name);
    }
    return name;
  }
  
  public String getKey() {
    return key;
  }
  
  public String getId() {
    return id;
  }
  
  public static SynapseIssueType getSynapseIssueType(String name) {
    for (SynapseIssueType issueType : ) {
      if (issueType.getName().equals(name)) {
        return issueType;
      }
    }
    return null;
  }
}
