package com.go2group.synapse.issuetabpanels;

import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.plugin.module.ModuleFactory;
import com.go2group.synapse.config.SynapseConfig;
import com.go2group.synapserm.issuetabpanels.VersionsTabPanel;
import com.go2group.synapserm.service.VersionService;
import java.util.List;
import org.apache.log4j.Logger;


public class SynapseVersionsTabPanel
  extends VersionsTabPanel
{
  private SynapseConfig synapseConfig;
  private static final Logger log = Logger.getLogger(SynapseVersionsTabPanel.class);
  
  public SynapseVersionsTabPanel(JiraAuthenticationContext authenticationContext, ModuleFactory moduleFactory, VersionService versionService, SynapseConfig synapseConfig, DateTimeFormatterFactory dateTimeFormatterFactory, DateTimeFormatter dateTimeFormatter, I18nHelper helper) {
    super(authenticationContext, moduleFactory, versionService, dateTimeFormatterFactory, dateTimeFormatter, helper);
    this.synapseConfig = synapseConfig;
  }
  
  public boolean showPanel(Issue issue, ApplicationUser user)
  {
    log.debug("Requirement WebCondition evaluated for issue :" + issue);
    if (issue != null) {
      log.debug("Issuetype of the issue :" + issue.getIssueType().getName());
      
      if ((synapseConfig.getIssueTypeIds("Requirement").contains(issue.getIssueType().getId())) && (shouldDisplay(issue.getProjectObject()))) {
        log.debug("Requirement type! Condition evaluated to true to display the panel");
        return true;
      }
      return false;
    }
    
    return false;
  }
  
  public boolean shouldDisplay(Project currentProject)
  {
    if (currentProject != null) {
      log.debug("Project in context : " + currentProject.getKey());
      

      List<String> configuredProjects = synapseConfig.getPropertyValues("synapse.config.project.mapping");
      
      if ((configuredProjects.contains("synapse.config.project.mapping")) || (configuredProjects.contains("-1")) || (configuredProjects.contains(currentProject.getId().toString()))) {
        return true;
      }
      return false;
    }
    
    return false;
  }
}
