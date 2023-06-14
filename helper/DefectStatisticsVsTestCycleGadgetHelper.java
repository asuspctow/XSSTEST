package com.go2group.synapse.helper;

import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper.BeanFactory;
import com.atlassian.jira.util.VelocityParamFactory;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.velocity.VelocityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import org.apache.log4j.Logger;
import webwork.action.Action;

public class DefectStatisticsVsTestCycleGadgetHelper
{
  private final com.atlassian.jira.util.I18nHelper i18n;
  private final IssueManager issueManager;
  private final FieldManager fieldManager;
  private Map<Long, List<String>> bug2TestCases = null;
  private Map<Long, Issue> bug2Issue = null;
  private String columnNames = null;
  private Integer number = null;
  
  private final FieldLayoutManager fieldLayoutManager;
  private static final String DEFECT_TESTCYCLE_STATISTICS_GADGET_VIEW = "/templates/web/view/defect-testcycle-statistics-gadget-view.vm";
  private static final Logger log = Logger.getLogger(DefectStatisticsVsTestCycleGadgetHelper.class);
  
  public DefectStatisticsVsTestCycleGadgetHelper(Map<Long, List<String>> bug2TestCases, Map<Long, Issue> bug2Issue, String columnNames, Integer number)
  {
    this.bug2TestCases = bug2TestCases;
    this.bug2Issue = bug2Issue;
    this.columnNames = columnNames;
    this.number = number;
    fieldManager = ComponentAccessor.getFieldManager();
    issueManager = ComponentAccessor.getIssueManager();
    fieldLayoutManager = ComponentAccessor.getFieldLayoutManager();
    i18n = ComponentAccessor.getI18nHelperFactory().getInstance(ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser());
  }
  
  public String getHtml()
  {
    log.debug("Constructing Html");
    
    Map<String, Object> velocityParams = ComponentAccessor.getVelocityParamFactory().getDefaultVelocityParams();
    
    List<String> columnsField = new ArrayList();
    if (columnNames != null) {
      StringTokenizer stringTokenizer = new StringTokenizer(columnNames, "|");
      while (stringTokenizer.hasMoreTokens()) {
        columnsField.add(stringTokenizer.nextToken());
      }
    }
    velocityParams.put("bug2TestCases", bug2TestCases);
    velocityParams.put("bug2Issue", bug2Issue);
    velocityParams.put("columnsField", columnsField);
    velocityParams.put("this", this);
    velocityParams.put("i18n", i18n);
    velocityParams.put("fieldManager", fieldManager);
    velocityParams.put("baseUrl", ComponentAccessor.getApplicationProperties().getString("jira.baseurl"));
    velocityParams.put("jiraBaseUrl", ComponentAccessor.getApplicationProperties().getString("jira.baseurl"));
    velocityParams.put("number", number);
    log.debug("VelocityParams :" + velocityParams);
    
    VelocityManager vm = ComponentAccessor.getVelocityManager();
    String html = vm.getEncodedBody("", "/templates/web/view/defect-testcycle-statistics-gadget-view.vm", null, velocityParams);
    
    log.debug("Returning html : " + html);
    
    return html;
  }
  
  public Issue getIssue(Long issueId) {
    return issueManager.getIssueObject(issueId);
  }
  
  public String getDefectKeys(String rawKeys)
  {
    if ((rawKeys != null) && (rawKeys.trim().length() > 0)) {
      return rawKeys.substring(1, rawKeys.length() - 1).replaceAll(" ", "");
    }
    return "";
  }
  
  public String getComponents(Issue issue)
  {
    String componentString = "";
    for (ProjectComponent proj : issue.getComponents()) {
      if (componentString.equals("")) {
        componentString = componentString + proj.getName();
      } else {
        componentString = componentString + "," + proj.getName();
      }
    }
    return componentString;
  }
  





  public String getCustomFieldHtml(CustomField field, Issue issue)
  {
    com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem fieldLayoutItem = fieldLayoutManager.getFieldLayout(issue).getFieldLayoutItem(field);
    Action action = null;
    
    Map<String, Object> displayParams = MapBuilder.newBuilder("textOnly", Boolean.TRUE).toMutableMap();
    
    return field.getViewHtml(fieldLayoutItem, action, issue, displayParams);
  }
}
