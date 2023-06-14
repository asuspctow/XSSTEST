package com.go2group.synapse.job;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.managedconfiguration.ManagedConfigurationItemService;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.scheduler.JobRunnerRequest;
import com.atlassian.scheduler.JobRunnerResponse;
import com.go2group.synapse.constant.SynapseIssueType;
import com.go2group.synapse.manager.ConfigManager;
import java.util.Collection;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CreateIssueTypeJob implements CreateCustomFieldJobRunner
{
  private static final Logger log = Logger.getLogger(CreateIssueTypeJob.class);
  private final ConfigManager configManager;
  private final I18nHelper i18n;
  private CustomFieldSearcher customFieldSearcher;
  private final ManagedConfigurationItemService managedConfigurationItemService;
  
  @Autowired
  public CreateIssueTypeJob(@ComponentImport I18nHelper i18n, @ComponentImport ManagedConfigurationItemService managedConfigurationItemService, ConfigManager configManager)
  {
    this.configManager = configManager;
    this.i18n = i18n;
    this.managedConfigurationItemService = managedConfigurationItemService;
  }
  
  public JobRunnerResponse runJob(JobRunnerRequest request)
  {
    log.debug("issue type creation - starts..");
    try {
      setupIssueTypes();
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
    log.debug("issue type creation - ends..");
    return JobRunnerResponse.success();
  }
  
  public void setupIssueTypes() {
    try { String fieldId = SynapseIssueType.REQUIREMENT.getId();
      String fieldName = SynapseIssueType.REQUIREMENT.getName();
      if (fieldName.equals(SynapseIssueType.REQUIREMENT.getKey())) {
        fieldName = "Requirement";
      }
      String avatarName = "/download/resources/com.go2group.jira.plugin.synapse/synapse/images/icon-requirement.png";
      createIssueType(fieldId, SynapseIssueType.REQUIREMENT.getKey(), fieldName, "For Go2Group SYNAPSE Requirement issue type", false, avatarName);
      
      fieldId = SynapseIssueType.TEST_CASE.getId();
      fieldName = SynapseIssueType.TEST_CASE.getName();
      if (fieldName.equals(SynapseIssueType.TEST_CASE.getKey())) {
        fieldName = "Test Case";
      }
      avatarName = "/download/resources/com.go2group.jira.plugin.synapse/synapse/images/icon-testcase.png";
      createIssueType(fieldId, SynapseIssueType.TEST_CASE.getKey(), fieldName, "For Go2Group SYNAPSE Test Case issue type", false, avatarName);
      
      fieldId = SynapseIssueType.TEST_PLAN.getId();
      fieldName = SynapseIssueType.TEST_PLAN.getName();
      if (fieldName.equals(SynapseIssueType.TEST_PLAN.getKey())) {
        fieldName = "Test Plan";
      }
      avatarName = "/download/resources/com.go2group.jira.plugin.synapse/synapse/images/icon-testplan.png";
      createIssueType(fieldId, SynapseIssueType.TEST_PLAN.getKey(), fieldName, "For Go2Group SYNAPSE Test Plan issue type", false, avatarName);
      
      fieldId = SynapseIssueType.BUG.getId();
      fieldName = SynapseIssueType.BUG.getName();
      if (fieldName.equals(SynapseIssueType.BUG.getKey())) {
        fieldName = "Bug";
      }
      avatarName = "/download/resources/com.go2group.jira.plugin.synapse/synapse/images/icon-bug.png";
      createIssueType(fieldId, SynapseIssueType.BUG.getKey(), fieldName, "For Go2Group SYNAPSE Bug issue type", false, avatarName);
    }
    catch (CreateException e)
    {
      e.printStackTrace();
    }
  }
  











  private String createIssueType(String issueTypeId, String propertiesKey, String defaultIssueTypeName, String defaultDescription, boolean isSubTask, String avatarName)
    throws CreateException
  {
    ApplicationProperties properties = ComponentAccessor.getApplicationProperties();
    log.debug("Create issue type " + defaultIssueTypeName + " [" + issueTypeId + "]; properties key " + propertiesKey);
    
    Collection<IssueType> issueTypes = ComponentAccessor.getConstantsManager().getAllIssueTypeObjects();
    IssueTypeSchemeManager issueTypeSchemeManager = ComponentAccessor.getIssueTypeSchemeManager();
    IssueType value = null;
    if (StringUtils.isNotBlank(issueTypeId)) {
      log.debug("Issue type id not null");
      boolean issueTypeFound = false;
      for (IssueType issueType : issueTypes) {
        log.debug("Check issue type " + issueType.getName() + "[" + issueType.getId() + "]");
        if (issueType.getId().equals(issueTypeId)) {
          log.debug("Found target issue type, write to properties");
          properties.setString(propertiesKey, issueTypeId);
          
          FieldConfig oneAndOnly = issueTypeSchemeManager.getDefaultIssueTypeScheme().getOneAndOnlyConfig();
          OptionsManager optionManager = ComponentAccessor.getOptionsManager();
          Collection<Option> issueTypeOptions = optionManager.getOptions(oneAndOnly);
          
          if ((issueTypeOptions != null) && (!issueTypeOptions.isEmpty())) {
            boolean assignedToDefaultScheme = false;
            for (Option option : issueTypeOptions) {
              log.debug("Default scheme has option: " + option.getValue() + " " + option.getOptionId());
              if (String.valueOf(option.getOptionId()).equalsIgnoreCase(issueTypeId)) {
                log.debug("Found synapse issue type in default scheme, ok");
                assignedToDefaultScheme = true;
              }
            }
            if (!assignedToDefaultScheme) {
              log.debug("Issue is not assigned to default scheme, fixing");
              issueTypeSchemeManager.addOptionToDefault(issueType.getId());
            }
          }
          issueTypeFound = true;
          break;
        }
      }
      if (!issueTypeFound) {
        issueTypeId = null;
      }
    }
    if (StringUtils.isBlank(issueTypeId)) {
      log.debug("Issue type id is null");
      boolean nameFound = false;
      for (IssueType issueType : issueTypes) {
        log.debug("Check issue type " + issueType.getName() + "[" + issueType.getId() + "]");
        if (issueType.getName().equals(defaultIssueTypeName)) {
          log.debug("Found target issue type");
          nameFound = true;
          value = issueType;
          break;
        }
      }
      boolean issueTypeAvailable = false;
      String storedIssueTypeId = properties.getString(propertiesKey);
      if (StringUtils.isNotBlank(storedIssueTypeId)) {
        IssueType issueType = ComponentAccessor.getConstantsManager().getIssueType(storedIssueTypeId);
        if (issueType != null) {
          issueTypeAvailable = true;
        }
      }
      if ((!nameFound) && (!issueTypeAvailable)) {
        log.debug("Issue type not found, create new one");
        value = ComponentAccessor.getConstantsManager().insertIssueType(defaultIssueTypeName, Long.valueOf(issueTypes.size() + 1), isSubTask ? "jira_subtask" : null, defaultDescription, avatarName);
        

        log.debug("created, add option to default");
        
        issueTypeSchemeManager.addOptionToDefault(value.getId());
      }
      
      if (value != null) {
        issueTypeId = value.getId();
        properties.setString(propertiesKey, issueTypeId);
      }
    }
    return issueTypeId;
  }
  
  public List<Project> getProjects()
  {
    log.debug("getProjects : Started");
    List<Project> allProjects = ComponentAccessor.getProjectManager().getProjectObjects();
    log.debug("getProjects : End after getting all the projects : " + allProjects);
    return allProjects;
  }
}
