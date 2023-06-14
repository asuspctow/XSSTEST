package com.go2group.synapse.helper;

import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.FieldException;
import com.atlassian.jira.issue.label.LabelManager;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.issue.resolution.Resolution;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.go2group.synapse.bean.ImportBean;
import com.google.common.collect.Sets;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class JiraFieldHelper
{
  private static final Logger log = Logger.getLogger(JiraFieldHelper.class);
  private JiraAuthenticationContext jiraAuthenticationContext;
  
  public JiraFieldHelper(JiraAuthenticationContext jiraAuthenticationContext)
  {
    this.jiraAuthenticationContext = jiraAuthenticationContext;
  }
  
  public Map<String, List<String>> getJiraFieldset(String importIssueType) {
    log.debug("Jira fields loading...");
    
    Map<String, List<String>> navFieldToSet = new HashMap();
    List<String> navList = new ArrayList();
    if ((importIssueType != null) && (importIssueType.equalsIgnoreCase("Requirement"))) {
      navList.add("issuetype");
    }
    navList.add("issuekey");
    navList.add("summary");
    navList.add("description");
    navList.add("priority");
    navList.add("assignee");
    navList.add("reporter");
    navList.add("versions");
    navList.add("fixVersions");
    navList.add("components");
    navList.add("labels");
    navList.add("duedate");
    navList.add("environment");
    navList.add("timeoriginalestimate");
    
    navFieldToSet.put("synapse.import.label.jirafield.issue.fields", navList);
    log.debug("Jira fields loaded!");
    return navFieldToSet;
  }
  
  public static Map<String, String> getJiraFieldsIdsToLabels() {
    Map<String, String> fieldIdToLabelsMap = new HashMap();
    
    fieldIdToLabelsMap.put("versions", "Affects Version/s");
    fieldIdToLabelsMap.put("assignee", "Assignee");
    fieldIdToLabelsMap.put("components", "Component/s");
    fieldIdToLabelsMap.put("description", "Description");
    fieldIdToLabelsMap.put("duedate", "Due Date");
    fieldIdToLabelsMap.put("environment", "Environment");
    fieldIdToLabelsMap.put("fixVersions", "Fix Version/s");
    fieldIdToLabelsMap.put("issuekey", "Key");
    fieldIdToLabelsMap.put("labels", "Labels");
    fieldIdToLabelsMap.put("priority", "Priority");
    fieldIdToLabelsMap.put("reporter", "Reporter");
    fieldIdToLabelsMap.put("summary", "Summary");
    fieldIdToLabelsMap.put("timeoriginalestimate", "Original Estimate (in seconds)");
    
    return fieldIdToLabelsMap;
  }
  
  public static List<String> getJiraFieldsetIds() {
    log.debug("Jira fields loading...");
    

    List<String> navList = new ArrayList();
    navList.add("issuekey");
    navList.add("summary");
    navList.add("description");
    navList.add("priority");
    navList.add("assignee");
    navList.add("reporter");
    navList.add("versions");
    navList.add("fixVersions");
    navList.add("components");
    navList.add("labels");
    navList.add("duedate");
    navList.add("environment");
    navList.add("timeoriginalestimate");
    

    log.debug("Jira fields loaded!");
    return navList;
  }
  
  public static List<String> getJiraFields() throws FieldException {
    List<String> synNavList = new ArrayList();
    synNavList.add("Affects Version/s");
    synNavList.add("Assignee");
    synNavList.add("Component/s");
    synNavList.add("Description");
    synNavList.add("Due Date");
    synNavList.add("Environment");
    synNavList.add("Fix Version/s");
    synNavList.add("Key");
    synNavList.add("Labels");
    synNavList.add("Priority");
    synNavList.add("Reporter");
    synNavList.add("Summary");
    synNavList.add("Original Estimate (in seconds)");
    



    return synNavList;
  }
  
  public MutableIssue setSummary(MutableIssue issueObject, String summary) { log.debug("Jira summary field to set : " + summary);
    if (summary != null) {
      issueObject.setSummary(summary);
    }
    return issueObject;
  }
  
  public MutableIssue populateJiraFields(ApplicationUser loggedInUser, MutableIssue issueObject, ImportBean caseBean, Map<String, String> jiraFieldsToUpdate, String dateFormat, String projectId) {
    log.debug("populateJiraFields starts..");
    if ((jiraFieldsToUpdate.containsKey("summary")) && (StringUtils.isNotBlank(caseBean.getSummary()))) {
      setSummary(issueObject, caseBean.getSummary());
    }
    if ((jiraFieldsToUpdate.containsKey("description")) && (StringUtils.isNotBlank(caseBean.getDescription()))) {
      setDescription(issueObject, caseBean.getDescription());
    } else if ((jiraFieldsToUpdate.containsKey("description")) && (StringUtils.isBlank(caseBean.getDescription()))) {
      issueObject.setDescription(null);
    }
    if ((jiraFieldsToUpdate.containsKey("components")) && (StringUtils.isNotBlank(caseBean.getComponent()))) {
      setComponent(issueObject, caseBean.getComponent(), projectId);
    } else if ((jiraFieldsToUpdate.containsKey("components")) && (StringUtils.isBlank(caseBean.getComponent()))) {
      issueObject.setComponent(null);
    }
    if ((jiraFieldsToUpdate.containsKey("assignee")) && (StringUtils.isNotBlank(caseBean.getAssignee()))) {
      setAssignee(issueObject, caseBean.getAssignee());
    } else if ((jiraFieldsToUpdate.containsKey("assignee")) && (StringUtils.isBlank(caseBean.getAssignee()))) {
      issueObject.setAssignee(null);
    }
    if (StringUtils.isNotBlank(caseBean.getReporter())) {
      setReporter(issueObject, caseBean.getReporter());
    } else if (caseBean.getAction().equalsIgnoreCase("create")) {
      setReporter(issueObject, loggedInUser);
    }
    
    if ((jiraFieldsToUpdate.containsKey("priority")) && (StringUtils.isNotBlank(caseBean.getPriority()))) {
      setPriority(issueObject, caseBean.getPriority());
    } else if ((jiraFieldsToUpdate.containsKey("priority")) && (StringUtils.isBlank(caseBean.getPriority()))) {
      issueObject.setPriority(null);
    }
    if ((jiraFieldsToUpdate.containsKey("status")) && (StringUtils.isNotBlank(caseBean.getStatus()))) {
      setStatus(issueObject, caseBean.getStatus());
    } else if ((jiraFieldsToUpdate.containsKey("status")) && (StringUtils.isBlank(caseBean.getStatus()))) {
      issueObject.setStatus(null);
    }
    if ((jiraFieldsToUpdate.containsKey("resolution")) && (StringUtils.isNotBlank(caseBean.getResolution()))) {
      setResolution(issueObject, caseBean.getResolution());
    } else if ((jiraFieldsToUpdate.containsKey("resolution")) && (StringUtils.isBlank(caseBean.getResolution()))) {
      issueObject.setResolution(null);
    }
    if ((jiraFieldsToUpdate.containsKey("versions")) && (StringUtils.isNotBlank(caseBean.getAffectVersion()))) {
      setAffectedVersion(issueObject, caseBean.getAffectVersion(), projectId);
    } else if ((jiraFieldsToUpdate.containsKey("versions")) && (StringUtils.isBlank(caseBean.getAffectVersion()))) {
      issueObject.setAffectedVersions(null);
    }
    if ((jiraFieldsToUpdate.containsKey("fixVersions")) && (StringUtils.isNotBlank(caseBean.getFixVersion()))) {
      setFixedVersion(issueObject, caseBean.getFixVersion(), projectId);
    } else if ((jiraFieldsToUpdate.containsKey("fixVersions")) && (StringUtils.isBlank(caseBean.getFixVersion()))) {
      issueObject.setFixVersions(null);
    }
    if ((jiraFieldsToUpdate.containsKey("environment")) && (StringUtils.isNotBlank(caseBean.getEnvironment()))) {
      setEnvironment(issueObject, caseBean.getEnvironment());
    } else if ((jiraFieldsToUpdate.containsKey("environment")) && (StringUtils.isBlank(caseBean.getEnvironment()))) {
      issueObject.setEnvironment(null);
    }
    if ((jiraFieldsToUpdate.containsKey("duedate")) && (StringUtils.isNotBlank(caseBean.getDueDate()))) {
      setDueDate(issueObject, caseBean.getDueDate(), dateFormat);
    } else if ((jiraFieldsToUpdate.containsKey("duedate")) && (StringUtils.isBlank(caseBean.getDueDate())))
      issueObject.setDueDate(null);
    if ((jiraFieldsToUpdate.containsKey("timeoriginalestimate")) && (StringUtils.isNotBlank(caseBean.getOriginalEstimate()))) {
      setEstimate(issueObject, caseBean.getOriginalEstimate());
    } else if ((jiraFieldsToUpdate.containsKey("timeoriginalestimate")) && (StringUtils.isBlank(caseBean.getOriginalEstimate()))) {
      issueObject.setOriginalEstimate(null);
    }
    return issueObject;
  }
  
  public MutableIssue setLabels(MutableIssue issueObject, String label)
  {
    log.debug("Jira Label field to set : " + label);
    ApplicationUser user = jiraAuthenticationContext.getLoggedInUser();
    if (StringUtils.isNotBlank(label)) {
      LabelManager labelManager = (LabelManager)ComponentAccessor.getComponent(LabelManager.class);
      Set<String> labelstoSet = Sets.newHashSet();
      StringTokenizer st = new StringTokenizer(label, "|");
      
      while (st.hasMoreTokens()) {
        labelstoSet.add(st.nextToken().trim());
      }
      
      labelManager.setLabels(user, issueObject.getId(), labelstoSet, false, false);
    }
    log.debug("Jira Label field has set in Issue object Successfully!");
    return issueObject;
  }
  
  public MutableIssue setEnvironment(MutableIssue issueObject, String environment) {
    log.debug("Jira Environment field to set : " + environment);
    if (StringUtils.isNotBlank(environment)) {
      issueObject.setEnvironment(environment);
    }
    return issueObject;
  }
  
  public MutableIssue setDueDate(MutableIssue issueObject, String dueDate, String dateFormat)
  {
    log.debug("Jira Due date field to set : " + dueDate);
    log.debug("Date format to be used : " + dateFormat);
    if (StringUtils.isNotBlank(dueDate)) {
      DateFormat formatter = new SimpleDateFormat(dateFormat);
      try
      {
        Date date = formatter.parse(dueDate);
        Timestamp timeStampDate = new Timestamp(date.getTime());
        
        issueObject.setDueDate(timeStampDate);
      }
      catch (ParseException e) {
        log.debug("Date parse exeception" + e.getMessage());
      }
    }
    
    log.debug("Jira Due date field has set in Issue object Successfully!");
    return issueObject;
  }
  
  public MutableIssue setFixedVersion(MutableIssue issueObject, String fixVersion, String projectId)
  {
    log.debug("Jira Fix Version field to set : " + fixVersion);
    if (StringUtils.isNotBlank(fixVersion))
    {
      Collection<Version> fixVersionCollectionIssue = new ArrayList();
      StringTokenizer st = new StringTokenizer(fixVersion, ",");
      String token;
      while (st.hasMoreTokens()) {
        token = st.nextToken().trim();
        
        Collection<Version> versionCollection = ComponentAccessor.getVersionManager().getVersionsByName(token);
        
        for (Version fixedVersion : versionCollection) {
          if ((fixedVersion.getName().equals(token)) && (fixedVersion.getProjectId().equals(Long.valueOf(projectId)))) {
            fixVersionCollectionIssue.add(fixedVersion);
          }
        }
      }
      issueObject.setFixVersions(fixVersionCollectionIssue);
    }
    log.debug("Jira Fix version field has set in Issue object Successfully!");
    return issueObject;
  }
  
  public MutableIssue setAffectedVersion(MutableIssue issueObject, String affectVersion, String projectId)
  {
    log.debug("Jira AffectedVersion field to set : " + affectVersion);
    if (StringUtils.isNotBlank(affectVersion))
    {
      Collection<Version> affectedVersionCollectionIssue = new ArrayList();
      StringTokenizer st = new StringTokenizer(affectVersion, ",");
      String token;
      while (st.hasMoreTokens()) {
        token = st.nextToken().trim();
        
        Collection<Version> versionCollection = ComponentAccessor.getVersionManager().getVersionsByName(token);
        
        for (Version affectedVersion : versionCollection) {
          if ((affectedVersion.getName().equals(token)) && (affectedVersion.getProjectId().equals(Long.valueOf(projectId)))) {
            affectedVersionCollectionIssue.add(affectedVersion);
          }
        }
      }
      
      issueObject.setAffectedVersions(affectedVersionCollectionIssue);
    }
    log.debug("Jira Affected version field has set in Issue object Successfully!");
    return issueObject;
  }
  
  public MutableIssue setStatus(MutableIssue issueObject, String status)
  {
    log.debug("Jira Status field to set : " + status);
    if (StringUtils.isNotBlank(status)) {
      Collection<Status> statuses = ComponentAccessor.getConstantsManager().getStatuses();
      
      for (Status statusObj : statuses) {
        if (statusObj.getName().equals(status))
        {


          issueObject.setStatusId(statusObj.getId());
        }
      }
    }
    
    log.debug("Jira Status field has set in Issue object Successfully!");
    return issueObject;
  }
  
  public MutableIssue setResolution(MutableIssue issueObject, String resolution)
  {
    log.debug("Jira Resolution field to set : " + resolution);
    if (StringUtils.isNotBlank(resolution)) {
      Collection<Resolution> resolutions = ComponentAccessor.getConstantsManager().getResolutions();
      
      for (Resolution resolutionObj : resolutions) {
        if (resolutionObj.getName().equals(resolution))
        {

          issueObject.setResolutionId(resolutionObj.getId());
        }
      }
    }
    
    log.debug("Jira Resolution field has set in Issue object Successfully!");
    return issueObject;
  }
  
  public void setAssignee(MutableIssue issueObject, String assignee)
  {
    log.debug("Jira Assignee field to set : " + assignee);
    
    if (StringUtils.isNotBlank(assignee)) {
      UserManager userManager = (UserManager)ComponentAccessor.getComponent(UserManager.class);
      ApplicationUser assigneeAppUserList = userManager.getUserByName(assignee);
      
      if (assigneeAppUserList != null) {
        issueObject.setAssignee(assigneeAppUserList);
      }
    }
    
    log.debug("Jira Assignee field has set in Issue object Successfully!");
  }
  
  public void setReporter(MutableIssue issueObject, ApplicationUser user)
  {
    if (user != null) {
      issueObject.setReporter(user);
    }
  }
  
  public void setReporter(MutableIssue issueObject, String reporter) {
    log.debug("Jira Reporter field to set : " + reporter);
    
    if (StringUtils.isNotBlank(reporter)) {
      UserManager userManager = (UserManager)ComponentAccessor.getComponent(UserManager.class);
      ApplicationUser user = userManager.getUserByName(reporter);
      log.debug("reporterAppUserList : " + user);
      if (user != null) {
        setReporter(issueObject, user);
      }
    } else {
      issueObject.setReporter(jiraAuthenticationContext.getLoggedInUser());
    }
    log.debug("Jira Reporter field has set in Issue object Successfully!");
  }
  
  public MutableIssue setPriority(MutableIssue issueObject, String priority)
  {
    log.debug("Jira Priority field to set : " + priority);
    if (StringUtils.isNotBlank(priority)) {
      Collection<Priority> priorities = ComponentAccessor.getConstantsManager().getPriorities();
      for (Priority priorityObj : priorities) {
        log.debug("priorityObj.getName() : " + priorityObj.getName());
        if (priorityObj.getName().equals(priority))
        {
          issueObject.setPriorityId(priorityObj.getId());
          
          log.debug("I am set");
        }
      }
    }
    
    log.debug("Jira Priority field has set in Issue object Successfully!");
    return issueObject;
  }
  
  public MutableIssue setComponent(MutableIssue issueObject, String component, String projectId)
  {
    log.debug("Jira Component field to set : " + component);
    if (StringUtils.isNotBlank(component)) {
      Collection<ProjectComponent> projectComponentCollection = issueObject.getProjectObject().getComponents();
      Collection<ProjectComponent> issueComponentCollection = new ArrayList();
      StringTokenizer st = new StringTokenizer(component, ",");
      String token;
      while (st.hasMoreTokens()) {
        token = st.nextToken().trim();
        for (ProjectComponent comp : projectComponentCollection) {
          if ((comp.getName().equals(token)) && (comp.getProjectId().equals(Long.valueOf(projectId)))) {
            issueComponentCollection.add(comp);
          }
        }
      }
      
      issueObject.setComponent(issueComponentCollection);
    }
    
    log.debug("Jira Component field has set in Issue object Successfully!");
    return issueObject;
  }
  
  public MutableIssue setDescription(MutableIssue issueObject, String description)
  {
    log.debug("Jira Description field to set : " + description);
    if (StringUtils.isNotBlank(description)) {
      issueObject.setDescription(description);
    }
    return issueObject;
  }
  
  public MutableIssue setEstimate(MutableIssue issueObject, String originalestimate) {
    log.debug("Jira Environment field to set : " + originalestimate);
    if (originalestimate != null) {
      issueObject.setOriginalEstimate(Long.valueOf(originalestimate));
    }
    return issueObject;
  }
}
