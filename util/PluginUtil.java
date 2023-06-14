package com.go2group.synapse.util;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.issue.status.category.StatusCategory;
import com.atlassian.jira.jql.parser.JqlQueryParser;
import com.atlassian.jira.ofbiz.DefaultOfBizConnectionFactory;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.UserProjectHistoryManager;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.JiraDurationUtils;
import com.atlassian.jira.util.thread.JiraThreadLocalUtils;
import com.atlassian.query.Query;
import com.go2group.synapse.bean.IssueWrapperBean;
import com.go2group.synapse.bean.TestParamOutputBean;
import com.go2group.synapse.bean.TestRunOutputBean;
import com.go2group.synapse.config.SynapseConfig;
import com.go2group.synapse.core.enums.CustomJQLFieldEnum;
import com.go2group.synapse.enums.TestRunStatusEnum;
import com.go2group.synapse.jira.api.container.jira80.SynapseSearchResults;
import com.go2group.synapse.manager.PermissionUtilAbstract;
import com.go2group.synapse.manager.impl.DefaultPermissionUtil;
import com.go2group.synapse.service.LicenseUtilService;
import com.go2group.synapse.service.TestParamService;
import java.io.PrintStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.config.DatasourceInfo;
import org.ofbiz.core.entity.jdbc.dbtype.DatabaseType;

public class PluginUtil extends com.go2group.synapse.core.util.PluginUtil
{
  private static IssueManager issueManager = ;
  private static final Logger log = Logger.getLogger(PluginUtil.class);
  private static PermissionUtilAbstract permissionUtil = (PermissionUtilAbstract)ComponentAccessor.getOSGiComponentInstanceOfType(PermissionUtilAbstract.class);
  private static JqlQueryParser jqlQueryParser = (JqlQueryParser)ComponentAccessor.getOSGiComponentInstanceOfType(JqlQueryParser.class);
  private static SearchService searchService = (SearchService)ComponentAccessor.getOSGiComponentInstanceOfType(SearchService.class);
  private static UserProjectHistoryManager userProjectHistoryManager = (UserProjectHistoryManager)ComponentAccessor.getOSGiComponentInstanceOfType(UserProjectHistoryManager.class);
  private static PermissionManager permissionManager = (PermissionManager)ComponentAccessor.getOSGiComponentInstanceOfType(PermissionManager.class);
  
  static {
    jqlQueryParser = (JqlQueryParser)ComponentAccessor.getOSGiComponentInstanceOfType(JqlQueryParser.class);
    searchService = (SearchService)ComponentAccessor.getOSGiComponentInstanceOfType(SearchService.class);
    if (permissionUtil == null) {
      permissionUtil = (DefaultPermissionUtil)com.go2group.synapse.core.spring.ApplicationContextProvider.getApplicationContext().getBean("permissionUtil");
    }
  }
  
  public static boolean hasValidLicense() {
    LicenseUtilService licenseUtil = (LicenseUtilService)ComponentAccessor.getOSGiComponentInstanceOfType(LicenseUtilService.class);
    if (licenseUtil != null) {
      return licenseUtil.hasValidLicense();
    }
    return false;
  }
  
  public static List<IssueWrapperBean> getIssueWrapper(Collection<Issue> issues)
  {
    log.debug("Creating IssueWrapperBean for the Issues : " + issues);
    
    if ((issues != null) && (issues.size() > 0)) {
      List<IssueWrapperBean> issueWrapperList = new ArrayList();
      
      for (Issue issue : issues) {
        IssueWrapperBean issueWrapperBean = new IssueWrapperBean();
        issueWrapperBean.setId(issue.getId());
        issueWrapperBean.setKey(issue.getKey());
        issueWrapperBean.setSummary(issue.getSummary());
        
        issueWrapperBean.setStatusId(issue.getStatus().getId());
        issueWrapperBean.setStatusLozenge(issue.getStatus().getStatusCategory().getColorName());
        if (issue.getResolution() == null) {
          issueWrapperBean.setResolution(false);
        } else {
          issueWrapperBean.setResolution(true);
        }
        issueWrapperList.add(issueWrapperBean);
      }
      
      log.debug("Returning issue wrapper :" + issueWrapperList);
      
      return issueWrapperList;
    }
    
    log.debug("Returning empty list. No issues received in argument");
    return Collections.emptyList();
  }
  
  public static List<IssueWrapperBean> getIssueWrapperWithViewPermission(Collection<String> issuesIds, ApplicationUser user)
  {
    log.debug("Creating IssueWrapperBean for the Issues : " + issuesIds);
    
    if ((issuesIds != null) && (issuesIds.size() > 0)) {
      List<IssueWrapperBean> issueWrapperList = new ArrayList();
      
      for (String issueId : issuesIds) {
        Issue issue = issueManager.getIssueObject(Long.valueOf(issueId));
        if (permissionUtil.hasViewPermission(issue, user)) {
          IssueWrapperBean issueWrapperBean = new IssueWrapperBean();
          issueWrapperBean.setId(issue.getId());
          issueWrapperBean.setKey(issue.getKey());
          issueWrapperBean.setSummary(issue.getSummary());
          
          issueWrapperBean.setStatusId(issue.getStatus().getId());
          issueWrapperBean.setStatusLozenge(issue.getStatus().getStatusCategory().getColorName());
          if (issue.getResolution() == null) {
            issueWrapperBean.setResolution(false);
          } else {
            issueWrapperBean.setResolution(true);
          }
          issueWrapperList.add(issueWrapperBean);
        }
      }
      
      log.debug("Returning issue wrapper :" + issueWrapperList);
      
      return issueWrapperList;
    }
    
    log.debug("Returning empty list. No issues received in argument");
    return Collections.emptyList();
  }
  








  public static String getLocalizedIssueTypeNames(SynapseConfig synapseConfig, String synapseName)
  {
    List<String> names = synapseConfig.getJIRAIssueTypeNames(synapseName);
    if ((names != null) && (names.size() > 0)) {
      String csvIssueTypeNames = "''";
      switch (synapseName) {
      case "Test Case": 
        csvIssueTypeNames = (String)names.get(0);
        if ((StringUtils.isNotBlank(csvIssueTypeNames)) && (csvIssueTypeNames.length() > 0)) {
          csvIssueTypeNames = "'" + csvIssueTypeNames + "'";
        } else {
          csvIssueTypeNames = "''";
        }
        break;
      case "Test Plan": 
        csvIssueTypeNames = (String)names.get(0);
        if ((StringUtils.isNotBlank(csvIssueTypeNames)) && (csvIssueTypeNames.length() > 0)) {
          csvIssueTypeNames = "'" + csvIssueTypeNames + "'";
        } else {
          csvIssueTypeNames = "''";
        }
        break;
      case "Requirement": 
      case "Bug": 
        csvIssueTypeNames = "";
        for (String id : names) {
          csvIssueTypeNames = csvIssueTypeNames + ",'" + id + "'";
        }
        csvIssueTypeNames = csvIssueTypeNames.substring(1);
      }
      
      
      return csvIssueTypeNames;
    }
    return "''";
  }
  







  public static String[] getLocalizedIssueTypeNamesAsArray(SynapseConfig synapseConfig, String synapseName)
  {
    List<String> names = synapseConfig.getJIRAIssueTypeNames(synapseName);
    if ((names != null) && (names.size() > 0)) {
      return (String[])names.toArray(new String[0]);
    }
    return null;
  }
  
  public static Long convertPrettifiedTextToMinute(String prettifiedText) throws com.atlassian.core.util.InvalidDurationException
  {
    if (StringUtils.isNotBlank(prettifiedText)) {
      JiraDurationUtils jiraUtilsraUtils = ComponentAccessor.getJiraDurationUtils();
      return jiraUtilsraUtils.parseDuration(prettifiedText, ComponentAccessor.getJiraAuthenticationContext().getLocale());
    }
    return null;
  }
  
  public static String convertMinuteToPrettifiedText(Long minute) {
    if (minute != null) {
      if (minute.longValue() == 0L) {
        return "";
      }
      JiraDurationUtils jiraUtilsraUtils = ComponentAccessor.getJiraDurationUtils();
      return jiraUtilsraUtils.getShortFormattedDuration(minute, ComponentAccessor.getJiraAuthenticationContext().getLocale());
    }
    return null;
  }
  
  public static String getI18nText(String key) {
    return ComponentAccessor.getI18nHelperFactory().getInstance(ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser()).getText(key);
  }
  
  public static int getJIRAVersion()
  {
    BuildUtilsInfo buildUtils = (BuildUtilsInfo)ComponentAccessor.getComponentOfType(BuildUtilsInfo.class);
    int[] vArr = buildUtils.getVersionNumbers();
    int versionNumber = 0;
    switch (vArr.length) {
    case 3: 
      versionNumber = Integer.valueOf(String.valueOf(vArr[0]) + String.valueOf(vArr[1]) + String.valueOf(vArr[2])).intValue();
      return versionNumber;
    case 2: 
      versionNumber = Integer.valueOf(String.valueOf(vArr[0]) + String.valueOf(vArr[1])).intValue();
      return versionNumber;
    }
    return vArr[0];
  }
  
  public static boolean isCloneSupported()
  {
    int jiraVersion = getJIRAVersion();
    if (jiraVersion >= 711) {
      return true;
    }
    return false;
  }
  
  public static List<Issue> convertToIssueObjectsWithViewPermission(Collection<Long> issueIds, ApplicationUser user) {
    if (issueIds == null) {
      return null;
    }
    if (issueIds.size() == 0) {
      return new ArrayList();
    }
    List<Issue> issues = new ArrayList();
    for (Long issueId : issueIds) {
      Issue issue = issueManager.getIssueObject(issueId);
      if ((issue != null) && (permissionUtil.hasViewPermission(issue, user))) {
        issues.add(issue);
      }
    }
    return issues;
  }
  
  public static List<Issue> convertToIssueObjectsWithViewPermission(Collection<Long> issueIds) {
    if (issueIds == null) {
      return null;
    }
    if (issueIds.size() == 0) {
      return new ArrayList();
    }
    List<Issue> issues = new ArrayList();
    for (Long issueId : issueIds) {
      Issue issue = issueManager.getIssueObject(issueId);
      if ((issue != null) && (permissionUtil.hasViewPermission(issue))) {
        issues.add(issue);
      }
    }
    return issues;
  }
  
  public static Issue getValidatedIssue(Long issueId) {
    Issue issue = issueManager.getIssueObject(issueId);
    if ((issue != null) && (permissionUtil.hasViewPermission(issue))) {
      return issue;
    }
    return null;
  }
  
  public static Issue getValidatedIssue(Long issueId, ApplicationUser user) {
    return com.go2group.synapse.core.util.PluginUtil.getValidatedIssue(issueManager, permissionUtil, issueId, user);
  }
  
  public static Issue getValidatedIssue(Issue issue, ApplicationUser user) {
    if ((issue != null) && (permissionUtil.hasViewPermission(issue, user))) {
      return issue;
    }
    return null;
  }
  
  public static boolean isPostgreSql() {
    log.debug("Checking if database is Postgres?");
    
    DatasourceInfo dataSourceInfo = DefaultOfBizConnectionFactory.getInstance().getDatasourceInfo();
    
    log.debug("DatabaseType :" + dataSourceInfo.getDatabaseTypeFromJDBCConnection().getFieldTypeName());
    
    if (dataSourceInfo.getDatabaseTypeFromJDBCConnection().getFieldTypeName().startsWith("postgres")) {
      return true;
    }
    return false;
  }
  
  public static boolean isOracle()
  {
    log.debug("Checking if database is ORACLE?");
    
    DatasourceInfo dataSourceInfo = DefaultOfBizConnectionFactory.getInstance().getDatasourceInfo();
    
    log.debug("DatabaseType :" + dataSourceInfo.getDatabaseTypeFromJDBCConnection().getFieldTypeName());
    
    if (dataSourceInfo.getDatabaseTypeFromJDBCConnection().getFieldTypeName().toLowerCase().startsWith("oracle")) {
      return true;
    }
    return false;
  }
  
  public static String getSchemaPrefix()
  {
    String lSchema = getSchema();
    
    if ((lSchema != null) && (lSchema.length() > 0)) {
      return lSchema + ".";
    }
    return "";
  }
  

  private static String getSchema()
  {
    String schema = "";
    
    DatasourceInfo dataSourceInfo = DefaultOfBizConnectionFactory.getInstance().getDatasourceInfo();
    
    log.debug("DatabaseType :" + dataSourceInfo.getDatabaseTypeFromJDBCConnection().getFieldTypeName());
    
    if (dataSourceInfo.getDatabaseTypeFromJDBCConnection().getFieldTypeName().startsWith("mssql")) {
      schema = dataSourceInfo.getSchemaName();
    }
    
    log.debug("Returning database Schema:" + schema);
    
    return schema;
  }
  
  public static boolean isMultipleFieldType(String fieldTypeKey) {
    int index = fieldTypeKey.lastIndexOf(':') + 1;
    String fieldTypeKeyName = fieldTypeKey.substring(index);
    
    switch (fieldTypeKeyName) {
    case "multicheckboxes": 
    case "radiobuttons": 
    case "select": 
    case "multiselect": 
    case "cascadingselect": 
    case "multiuserpicker": 
    case "multiversion": 
    case "multigrouppicker": 
      return true;
    }
    return false;
  }
  
  public static String formatTime(DateTimeFormatter dateTimeFormatter, Timestamp timeStamp) {
    if (timeStamp != null) {
      return dateTimeFormatter.forLoggedInUser().withStyle(com.atlassian.jira.datetime.DateTimeStyle.COMPLETE).format(toDate(timeStamp));
    }
    return "";
  }
  
  public static Date toDate(Timestamp timestamp)
  {
    long milliseconds = timestamp.getTime() + timestamp.getNanos() / 1000000;
    return new Date(milliseconds);
  }
  
  public static Object getFieldValue(Issue issue, Field field, DateTimeFormatter dateTimeFormatter) {
    FieldManager fieldManager = ComponentAccessor.getFieldManager();
    if (!fieldManager.isCustomField(field)) {
      switch (field.getId()) {
      case "versions": 
        return getStringRepresentation(issue.getAffectedVersions());
      







      case "description": 
        return issue.getDescription();
      
      case "assignee": 
        if (issue.getAssignee() != null) {
          return issue.getAssignee().getDisplayName();
        }
        return "Unassigned";
      


      case "Bug": 
        return issue.getIssueType().getName();
      case "components": 
        List<String> components = new ArrayList();
        if ((issue.getComponents() != null) && (issue.getComponents().size() > 0)) {
          for (ProjectComponent component : issue.getComponents()) {
            components.add(component.getName());
          }
        }
        return getStringRepresentation(components);
      case "created": 
        return formatTime(dateTimeFormatter, issue.getCreated());
      case "creator": 
        return issue.getCreator().getDisplayName();
      case "duedate": 
        return formatTime(dateTimeFormatter, issue.getDueDate());
      

      case "fixVersions": 
        return getStringRepresentation(issue.getFixVersions());
      case "issuetype": 
        return issue.getIssueType().getName();
      case "issuekey": 
        return issue.getKey();
      case "labels": 
        return getStringRepresentation(issue.getLabels());
      case "priority": 
        if (issue.getPriority() != null) {
          return issue.getPriority().getName();
        }
        return "";
      
      case "project": 
        return issue.getProjectObject().getName();
      case "reporter": 
        if (issue.getReporter() != null) {
          return issue.getReporter().getDisplayName();
        }
        return "Anonymous";
      
      case "resolution": 
        if (issue.getResolution() != null) {
          return issue.getResolution().getName();
        }
        return "Unresolved";
      
      case "resolutiondate": 
        return formatTime(dateTimeFormatter, issue.getResolutionDate());
      

      case "status": 
        if (issue.getStatus() != null) {
          return issue.getStatus().getName();
        }
        return "";
      


      case "summary": 
        return issue.getSummary();
      case "timeestimate": 
        return getString(issue.getEstimate());
      case "timeoriginalestimate": 
        return getString(issue.getOriginalEstimate());
      case "timespent": 
        return getString(issue.getTimeSpent());
      

      case "updated": 
        return formatTime(dateTimeFormatter, issue.getUpdated());
      case "votes": 
        return getString(issue.getVotes());
      

      case "watches": 
        return getString(issue.getWatches());
      }
      
    }
    






    return null;
  }
  
  public static String getStringRepresentation(Collection collection)
  {
    StringBuffer value = new StringBuffer("");
    for (Object object : collection) {
      value.append(",");
      value.append(object.toString());
    }
    
    return value.toString().trim().length() > 1 ? value.toString().substring(1) : "";
  }
  
  public static String getString(Object object) {
    if (object == null) {
      return "";
    }
    return object.toString();
  }
  
  public static boolean isTestCaseEditable(Issue issue, IssueManager issueManager, ApplicationUser user)
  {
    boolean isEditable = issueManager.isEditable(issue, user);
    return isEditable;
  }
  
  public static Locale getCurrentLocale() {
    return Locale.US;
  }
  
  public static boolean isTestParamAvailable(String text) {
    String unescapedText = StringEscapeUtils.unescapeHtml(text);
    if (StringUtils.isNotBlank(unescapedText)) {
      int startIndex = 0;
      while (startIndex != -1) {
        startIndex = unescapedText.indexOf("<<", startIndex);
        if (startIndex != -1) {
          int endIndex = unescapedText.indexOf(">>", startIndex);
          if (endIndex != -1) {
            return true;
          }
          startIndex += "<<".length();
          if (startIndex >= text.length()) {
            break;
          }
        }
      }
      return false;
    }
    return false;
  }
  
  public static List<String> extractTestParams(String text) {
    String unescapedText = StringEscapeUtils.unescapeHtml(text);
    if (StringUtils.isNotBlank(unescapedText)) {
      List<String> testParams = new ArrayList();
      int startIndex = 0;
      while (startIndex != -1) {
        startIndex = unescapedText.indexOf("<<", startIndex);
        if (startIndex != -1) {
          int endIndex = unescapedText.indexOf(">>", startIndex);
          if (endIndex != -1) {
            String paramName = unescapedText.substring(unescapedText.indexOf("<<", startIndex) + "<<".length(), endIndex);
            testParams.add(paramName);
          }
          startIndex += "<<".length();
          if (startIndex >= text.length()) {
            break;
          }
        }
      }
      return testParams;
    }
    return null;
  }
  
  public static String colorTestParams(String text, TestParamService testParamService, Integer scope, Long scopeRefId) throws com.go2group.synapse.core.exception.InvalidDataException {
    StringBuilder unescapedText = new StringBuilder(StringEscapeUtils.unescapeHtml(text));
    if (StringUtils.isNotBlank(unescapedText)) {
      int startIndex = 0;
      while (startIndex != -1) {
        startIndex = unescapedText.indexOf("<<", startIndex);
        if (startIndex != -1) {
          int endIndex = unescapedText.indexOf(">>", startIndex);
          if (endIndex != -1) {
            String paramName = unescapedText.substring(unescapedText.indexOf("<<", startIndex) + "<<".length(), endIndex);
            
            TestParamOutputBean testParam = testParamService.getTestParamByName(paramName, scope, scopeRefId);
            if (testParam != null) {
              int colorStart = unescapedText.indexOf("{color:blue}<<", startIndex - ("{color:blue}".length() + "<<".length()));
              if (colorStart == -1) {
                unescapedText.insert(endIndex + ">>".length(), "{color}");
                unescapedText.insert(startIndex, "{color:blue}");
              }
            }
          }
          startIndex += "{color:blue}".length() + "<<".length();
          if (startIndex >= text.length()) {
            break;
          }
        }
      }
      return unescapedText.toString();
    }
    return null;
  }
  
  public static boolean validateTestParamString(String text) {
    if (StringUtils.isBlank(text)) {
      return true;
    }
    Pattern paramValuePattern = Pattern.compile("[a-zA-Z0-9[^><]]+");
    if (paramValuePattern.matcher(text).matches()) {
      return true;
    }
    return false;
  }
  
  public static List<Issue> getTestCasesInJIRA(Query query, ApplicationUser user, int offset, int limit) {
    SearchResults searchResults = null;
    try
    {
      JiraThreadLocalUtils.preCall();
      searchResults = searchService.search(user, query, com.atlassian.jira.web.bean.PagerFilter.newPageAlignedFilter(offset, limit));
    } catch (SearchException e) {
      log.debug(e.getMessage(), e);
      log.error(e.getMessage());
    } finally {
      JiraThreadLocalUtils.postCall(log, null);
    }
    if (searchResults != null) {
      return SynapseSearchResults.getResults(searchResults);
    }
    return Collections.emptyList();
  }
  
  public static List<NameValuePair> parseToNameValuePair(String params)
  {
    List<NameValuePair> parametersList = null;
    if (StringUtils.isNotBlank(params)) {
      String[] paramValuePairs = params.split(";");
      if ((paramValuePairs != null) && (paramValuePairs.length > 0)) {
        parametersList = new ArrayList();
        for (String paramValuePair : paramValuePairs) {
          String[] paramAndValue = paramValuePair.split("=");
          if ((paramAndValue != null) && (paramAndValue.length == 2)) {
            NameValuePair nameValuePair = new org.apache.http.message.BasicNameValuePair(paramAndValue[0], paramAndValue[1]);
            parametersList.add(nameValuePair);
          }
        }
      }
    }
    return parametersList;
  }
  
  public static boolean isEligible(TestRunOutputBean testRun, String jql) {
    boolean isEligibleToRemove = false;
    List<String> testersOfCycleArguments = getParamValues(jql, "testersOfCycle");
    List<String> resultOfCycleArguments = getParamValues(jql, "resultsOfCycle");
    List<String> urgencyOfCycleArguments = getParamValues(jql, "urgencyOfRun");
    List<String> runAttributeOfCycleArguments = getParamValues(jql, "runAttributeOfCycle");
    
    if ((jql.contains("resultsOfCycle")) || (jql.contains(CustomJQLFieldEnum.RESULTSOFCYCLE.getName()))) {
      if (!resultOfCycleArguments.contains(testRun.getStatusEnum().getName())) {
        isEligibleToRemove = true;
      } else {
        isEligibleToRemove = false;
      }
    }
    
    if ((jql.contains("testersOfCycle")) || (jql.contains(CustomJQLFieldEnum.TESTERSOFCYCLE.getName()))) {
      if (testRun.getTester() != null) {
        if (!testersOfCycleArguments.contains(testRun.getTester().getName())) {
          isEligibleToRemove = true;
        } else {
          isEligibleToRemove = false;
        }
      } else {
        isEligibleToRemove = true;
      }
    }
    
    if (((jql.contains("urgencyOfRun")) || (jql.contains(CustomJQLFieldEnum.URGENCYOFRUN.getName()))) && 
      (testRun.getUrgency() != null)) {
      if (!urgencyOfCycleArguments.contains(testRun.getUrgency())) {
        isEligibleToRemove = true;
      } else {
        isEligibleToRemove = false;
      }
    }
    

    if ((jql.contains("runAttributeOfCycle")) || (jql.contains(CustomJQLFieldEnum.RUNATTRIBUTEOFCYCLE.getName()))) {
      Collection<List<String>> attributeCollection = testRun.getRunAttributes().values();
      List<String> testRunAttributes = new ArrayList();
      if ((attributeCollection != null) && (!org.springframework.util.CollectionUtils.isEmpty(attributeCollection))) {
        for (List<String> attributeList : attributeCollection) {
          for (String attribute : attributeList) {
            testRunAttributes.add(attribute);
          }
        }
        if (!testRunAttributes.contains(runAttributeOfCycleArguments)) {
          isEligibleToRemove = true;
        } else {
          isEligibleToRemove = false;
        }
      }
    }
    
    return isEligibleToRemove;
  }
  
  public static void main(String[] args) {
    String step = "Step with param 1 <<Param 1>>, Step with param 1 <<Param 3>>";
    
    List<String> res = extractTestParams(step);
    System.out.println("Data:" + step);
    System.out.println(res);
  }
  
  public PluginUtil() {}
}
