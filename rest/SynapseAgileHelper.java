package com.go2group.synapse.rest;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.datetime.DateTimeStyle;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.MultipleCustomFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldException;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.NavigableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.plugin.ProjectPermissionKey;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.VelocityParamFactory;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.velocity.VelocityManager;
import com.go2group.synapse.bean.ConfigMapOutputBean;
import com.go2group.synapse.bean.CustomDisplayPrefBean;
import com.go2group.synapse.bean.TestCycleOutputBean;
import com.go2group.synapse.bean.TestRunActionDisplayBean;
import com.go2group.synapse.bean.TestRunDisplayBean;
import com.go2group.synapse.bean.TestRunHistoryOutputBean;
import com.go2group.synapse.bean.TestRunOutputBean;
import com.go2group.synapse.bean.TestRunRequirementOutputBean;
import com.go2group.synapse.bean.runattribute.RunAttributeOutputBean;
import com.go2group.synapse.config.SynapseConfig;
import com.go2group.synapse.constant.SynapseIssueType;
import com.go2group.synapse.constant.SynapsePermission;
import com.go2group.synapse.core.exception.InvalidDataException;
import com.go2group.synapse.core.util.PermissionUtil;
import com.go2group.synapse.enums.TestRunStatusEnum;
import com.go2group.synapse.enums.TestRunTypeEnum;
import com.go2group.synapse.service.ConfigService;
import com.go2group.synapse.service.TestCaseToRequirementLinkService;
import com.go2group.synapse.service.TestRunRequirementService;
import com.go2group.synapse.service.TestRunService;
import com.go2group.synapse.service.runattribute.RunAttributeService;
import com.go2group.synapse.util.PluginUtil;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

public class SynapseAgileHelper
{
  private static final Logger log = Logger.getLogger(SynapseAgileHelper.class);
  
  private final TestRunService testRunService;
  
  private final IssueManager issueManager;
  
  private final DateTimeFormatter dateTimeFormatter;
  
  private final SynapseConfig synapseConfig;
  private final ConfigService configService;
  private final TestCaseToRequirementLinkService testCaseLinkService;
  private final TestRunRequirementService testRunRequirementService;
  private final FieldLayoutManager fieldLayoutManager;
  private final RendererManager rendererManager;
  private final IssueLinkManager issueLinkManager;
  private final I18nHelper i18nHelper;
  private final PermissionUtil permissionUtil;
  private final RunAttributeService runAttributeService;
  
  @Autowired
  public SynapseAgileHelper(@ComponentImport IssueManager issueManager, @ComponentImport DateTimeFormatter dateTimeFormatter, @ComponentImport FieldLayoutManager fieldLayoutManager, @ComponentImport RendererManager rendererManager, @ComponentImport IssueLinkManager issueLinkManager, @ComponentImport I18nHelper i18nHelper, PermissionUtil permissionUtil, TestRunService testRunService, SynapseConfig synapseConfig, ConfigService configService, TestCaseToRequirementLinkService testCaseLinkService, TestRunRequirementService testRunRequirementService, RunAttributeService runAttributeService)
  {
    this.permissionUtil = permissionUtil;
    this.testRunService = testRunService;
    this.issueManager = issueManager;
    this.dateTimeFormatter = dateTimeFormatter.forLoggedInUser().withStyle(DateTimeStyle.COMPLETE);
    this.synapseConfig = synapseConfig;
    this.configService = configService;
    this.testCaseLinkService = testCaseLinkService;
    this.testRunRequirementService = testRunRequirementService;
    this.fieldLayoutManager = fieldLayoutManager;
    this.rendererManager = rendererManager;
    this.issueLinkManager = issueLinkManager;
    this.i18nHelper = i18nHelper;
    this.runAttributeService = runAttributeService;
  }
  
  public String getTestRunViewHtml(Integer runId, String contextPath, String referrer) {
    Map<String, Object> velocityParams = ComponentAccessor.getVelocityParamFactory().getDefaultVelocityParams();
    VelocityManager vm = ComponentAccessor.getVelocityManager();
    try {
      TestRunActionDisplayBean actionDisplayBean = new TestRunActionDisplayBean();
      TestRunDisplayBean testRunBean = (TestRunDisplayBean)testRunService.getTestRun(runId);
      if (testRunBean == null) {
        return "";
      }
      actionDisplayBean.setTestRunBean(testRunBean);
      
      actionDisplayBean.setRunStatuses(TestRunStatusEnum.getActiveStatuses());
      TestCycleOutputBean testCycleBean = testRunBean.getCycle();
      actionDisplayBean.setTestCycleBean(testCycleBean);
      
      long currentProjectId = -1L;
      if (testCycleBean != null) {
        if (testCycleBean.getTpId().longValue() == -1L) {
          if (testRunBean != null) {
            Issue testCase = testRunBean.getTestCase();
            if (testCase != null) {
              velocityParams.put("currentProjectId", testCase.getProjectId());
              currentProjectId = testCase.getProjectId().longValue();
              
              velocityParams.put("hasProjectPermission", Boolean.valueOf(hasPermissionForCurrentProject(testCase.getProjectId().longValue())));
              actionDisplayBean.setPermission(hasPermission(testCycleBean, testRunBean));
            }
          }
        } else {
          Issue testPlan = issueManager.getIssueObject(testCycleBean.getTpId());
          if (testPlan != null) {
            velocityParams.put("currentProjectId", testPlan.getProjectId());
            currentProjectId = testPlan.getProjectId().longValue();
            
            velocityParams.put("hasProjectPermission", Boolean.valueOf(hasPermissionForCurrentProject(testPlan.getProjectId().longValue())));
          }
        }
      }
      
      String bugTypeId = synapseConfig.getPropertyIds(SynapseIssueType.BUG.getKey()).toString();
      String bugTypeName = synapseConfig.getPropertyValues(SynapseIssueType.BUG.getKey()).toString();
      velocityParams.put("bugTypeId", bugTypeId);
      velocityParams.put("bugTypeName", bugTypeName);
      

      Collection<TestRunHistoryOutputBean> history = testRunBean.getTestRunHistory();
      Timestamp executedOn = null;
      long dateToCompare = 0L;
      long dateLong = 0L;
      String executedBy = null;
      if (history != null) {
        Iterator<TestRunHistoryOutputBean> iter = history.iterator();
        while (iter.hasNext()) {
          TestRunHistoryOutputBean historyBean = (TestRunHistoryOutputBean)iter.next();
          
          if (historyBean.getActivityType().equals("Status")) {
            if (dateToCompare == 0L) {
              dateToCompare = historyBean.getExecutionTime().getTime();
              executedBy = historyBean.getExecutorFullName();
              executedOn = historyBean.getExecutionTime();
              
              actionDisplayBean.setTestedBy(executedBy);
              actionDisplayBean.setTestedOn(String.valueOf(executedOn));
            } else {
              dateLong = historyBean.getExecutionTime().getTime();
              if (dateToCompare < dateLong) {
                dateToCompare = dateLong;
                executedBy = historyBean.getExecutorFullName();
                executedOn = historyBean.getExecutionTime();
                
                actionDisplayBean.setTestedBy(executedBy);
                actionDisplayBean.setTestedOn(String.valueOf(executedOn));
              }
            }
          }
        }
      }
      

      log.debug("Executed on : " + executedOn);
      log.debug("executedBy : " + executedBy);
      





      testRunBean.setRenderedDescription(getDescriptionHtml(testRunBean, testCycleBean));
      



      ConfigMapOutputBean configMapBean = configService.getConfigMapping("synapse.config.testdata");
      boolean showStepData = (configMapBean != null) && (configMapBean.getValue().equals("true"));
      velocityParams.put("showStepData", Boolean.valueOf(showStepData));
      log.debug("before fields display ........");
      



      velocityParams.put("fieldsToDisplay", getFieldsMap(testRunBean.getTestCaseKey(), contextPath, ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser(), dateTimeFormatter));
      
      log.debug("after fields display ........");
      





      velocityParams.put("previousRunId", null);
      velocityParams.put("nextRunId", null);
      
      Long testCaseId = testRunBean.getTestCaseId();
      Issue testCase = issueManager.getIssueObject(testCaseId);
      if (testCase != null) {
        velocityParams.put("testCaseId", testCaseId);
        Collection<Long> reqIds = testCaseLinkService.getRequirementIds(testCase);
        Long id; if ((reqIds != null) && (reqIds.size() > 0)) {
          List<Issue> requirements = new ArrayList();
          for (Iterator localIterator = reqIds.iterator(); localIterator.hasNext();) { id = (Long)localIterator.next();
            Issue requirement = issueManager.getIssueObject(id);
            if (requirement != null) {
              requirements.add(requirement);
            }
          }
          velocityParams.put("requirements", requirements);
        }
        
        Collection<TestRunRequirementOutputBean> requirements = testRunRequirementService.getTestRunRequirements(runId);
        if ((requirements != null) && (requirements.size() > 0)) {
          Object mappedRequirements = new ArrayList();
          for (TestRunRequirementOutputBean testRunRequirement : requirements) {
            ((List)mappedRequirements).add(testRunRequirement.getRequirement());
          }
          velocityParams.put("mappedRequirements", mappedRequirements);
        }
      }
      actionDisplayBean.setExecutable(isExecutable(testCycleBean, testRunBean, referrer));
      velocityParams.put("action", actionDisplayBean);
      velocityParams.put("testRunBean", testRunBean);
      velocityParams.put("testRunDetailsBean", testRunBean);
      velocityParams.put("cycleReadOnly", Boolean.valueOf(isCycleReadOnly(testCycleBean, testRunBean, referrer)));
      velocityParams.put("referredView", Boolean.valueOf(isReferredView(referrer)));
      velocityParams.put("adhocRun", Boolean.valueOf(isAdhocRun(testCycleBean)));
      velocityParams.put("manualRunType", TestRunTypeEnum.MANUAL.getValue());
      velocityParams.put("i18n", i18nHelper);
      velocityParams.put("runId", runId);
      velocityParams.put("referrer", referrer);
      
      String bugTypes = PluginUtil.getLocalizedIssueTypeNames(synapseConfig, "Bug");
      bugTypes = URLEncoder.encode(bugTypes, "UTF-8");
      velocityParams.put("linkIssueType", bugTypes);
      velocityParams.put("linkIssueName", i18nHelper.getText("synapse.web.panel.testrun.bug.name"));
      
      List<RunAttributeOutputBean> runAttributes = runAttributeService.getRunAttributes(String.valueOf(currentProjectId));
      velocityParams.put("runAttributes", runAttributes);
    }
    catch (InvalidDataException e) {
      log.error(e.getMessage(), e);
      log.debug(e.getMessage(), e);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      log.debug(e.getMessage(), e);
    }
    
    String html = vm.getEncodedBody("", "/templates/web/action/testrun/scrum/scrum-run-view.vm", null, velocityParams);
    
    return html;
  }
  
  public String getTestRunRowViewHtml(Integer runId, String contextPath) {
    Map<String, Object> velocityParams = ComponentAccessor.getVelocityParamFactory().getDefaultVelocityParams();
    VelocityManager vm = ComponentAccessor.getVelocityManager();
    try {
      TestRunDisplayBean testRunBean = (TestRunDisplayBean)testRunService.getTestRun(runId);
      if (testRunBean == null) {
        return "";
      }
      velocityParams.put("testRun", testRunBean);
      velocityParams.put("i18n", i18nHelper);
      velocityParams.put("contextPath", contextPath);
      velocityParams.put("dateTimeFormatter", dateTimeFormatter);
    } catch (InvalidDataException e) {
      log.error(e.getMessage(), e);
      log.debug(e.getMessage(), e);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      log.debug(e.getMessage(), e);
    }
    String html = vm.getEncodedBody("", "/templates/web/action/agile/agile-test-run-row-view.vm", null, velocityParams);
    return html;
  }
  
  private boolean hasPermissionForCurrentProject(long projectId) {
    PermissionManager permissionManager = ComponentAccessor.getPermissionManager();
    ProjectPermissionKey projectPermissionKey = new ProjectPermissionKey("CREATE_ISSUES");
    Project project = ComponentAccessor.getProjectManager().getProjectObj(Long.valueOf(projectId));
    ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
    boolean createPermission = permissionManager.hasPermission(projectPermissionKey, project, user);
    return createPermission;
  }
  
  private String getDescriptionHtml(TestRunDisplayBean testRunDisplayBean, TestCycleOutputBean testCycleOutputBean) {
    Issue issue = issueManager.getIssueObject(testRunDisplayBean.getTestCaseId());
    
    if (issue == null)
    {
      issue = issueManager.getIssueObject(testCycleOutputBean.getTpId());
    }
    
    FieldLayout fieldLayout = fieldLayoutManager.getFieldLayout(issue);
    FieldLayoutItem fieldLayoutItem = fieldLayout.getFieldLayoutItem("description");
    String rendererType = fieldLayoutItem != null ? fieldLayoutItem.getRendererType() : null;
    return rendererManager.getRenderedContent(rendererType, testRunDisplayBean.getDescription(), issue.getIssueRenderContext());
  }
  
  private Map<String, String> getFieldsMap(String testcaseKey, String contextPath, ApplicationUser user, DateTimeFormatter dateTimeFormatter) {
    Map<String, List<CustomDisplayPrefBean>> moduleToFieldPrefMap = null;
    Map<String, String> fieldsMap = new HashMap();
    Issue testcase = issueManager.getIssueByCurrentKey(testcaseKey);
    



    FieldManager fieldManager = ComponentAccessor.getFieldManager();
    Set<NavigableField> allJiraFields = null;
    List<String> fieldIds = new ArrayList();
    NavigableField field;
    try { allJiraFields = fieldManager.getAvailableNavigableFields(user);
      for (localIterator = allJiraFields.iterator(); localIterator.hasNext();) { field = (NavigableField)localIterator.next();
        fieldIds.add(field.getId());
      }
    } catch (FieldException e1) { Iterator localIterator;
      log.debug(e1.getMessage(), e1);
    }
    

    moduleToFieldPrefMap = synapseConfig.getPreference(ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser().getKey());
    if ((moduleToFieldPrefMap == null) || (moduleToFieldPrefMap.keySet().size() == 0)) {
      moduleToFieldPrefMap = synapseConfig.getPreference("Application");
    }
    Object configuredFields = (List)moduleToFieldPrefMap.get("RunDialog");
    

    if (configuredFields != null) {
      for (CustomDisplayPrefBean configuredField : (List)configuredFields)
      {
        if (fieldIds.contains(configuredField.getField())) {
          Field field = fieldManager.getField(configuredField.getField());
          CustomField cfield = ComponentAccessor.getCustomFieldManager().getCustomFieldObject(field.getId());
          
          if (cfield != null) {
            if (cfield.getValue(testcase) != null)
            {
              CustomFieldType fieldType = cfield.getCustomFieldType();
              if ((fieldType instanceof MultipleCustomFieldType))
              {
                if ((cfield.getValue(testcase) instanceof Collection)) {
                  fieldsMap.put(cfield.getName(), getStringRepresentation((Collection)cfield.getValue(testcase)));
                }
                else {
                  fieldsMap.put(cfield.getName(), getString(cfield.getValue(testcase)));
                }
              }
              else {
                fieldsMap.put(cfield.getName(), getString(cfield.getValue(testcase)));
              }
            } else {
              fieldsMap.put(cfield.getName(), "");
            }
          }
          else {
            fieldsMap.put(field.getName(), (String)PluginUtil.getFieldValue(testcase, configuredField.getField(), contextPath, issueLinkManager, dateTimeFormatter));
          }
        }
      }
    }
    return fieldsMap;
  }
  
  private String getStringRepresentation(Collection collection) {
    StringBuffer value = new StringBuffer("");
    for (Object object : collection) {
      value.append(",");
      value.append(object.toString());
    }
    
    return value.toString().trim().length() > 1 ? value.toString().substring(1) : "";
  }
  
  private String getString(Object object) {
    if (object == null) {
      return "";
    }
    return object.toString();
  }
  
  private boolean isCycleReadOnly(TestCycleOutputBean testCycleBean, TestRunOutputBean testRunOutputBean, String referrer)
  {
    return (!PluginUtil.hasValidLicense()) || 
      ("Completed".equals(testCycleBean.getStatus())) || 
      ("Aborted".equals(testCycleBean.getStatus())) || 
      (isReferredView(referrer)) || (
      (!isAdhocRun(testCycleBean)) && (isOperatingIssueResolved(testCycleBean, testRunOutputBean)));
  }
  
  private boolean isReferredView(String referrer)
  {
    if ((StringUtils.isNotBlank(referrer)) && (
      (referrer.equals("other")) || (referrer.equals("viewOnly")))) {
      return true;
    }
    

    return false;
  }
  
  private boolean isAdhocRun(TestCycleOutputBean testCycleBean) {
    if (testCycleBean != null) {
      return testCycleBean.getTpId().longValue() == -1L;
    }
    return true;
  }
  
  private boolean isOperatingIssueResolved(TestCycleOutputBean testCycleOutputBean, TestRunOutputBean testRunOutputBean) {
    return getOperatingIssue(testCycleOutputBean, testRunOutputBean).getResolution() != null;
  }
  
  private Issue getOperatingIssue(TestCycleOutputBean testCycleOutputBean, TestRunOutputBean testRunOutputBean) {
    log.debug("Retrieving operating issue");
    if (isAdhocRun(testCycleOutputBean)) {
      log.debug("Is an Adhoc run");
      return issueManager.getIssueObject(testRunOutputBean.getTestCaseId());
    }
    log.debug("Is a Cycle run");
    return issueManager.getIssueObject(testCycleOutputBean.getTpId());
  }
  
  private boolean isExecutable(TestCycleOutputBean testCycleOutputBean, TestRunOutputBean testRunOutputBean, String referrer)
  {
    boolean hasPermission = hasPermission(testCycleOutputBean, testRunOutputBean);
    boolean isCycleReadOnly = isCycleReadOnly(testCycleOutputBean, testRunOutputBean, referrer);
    return (!isCycleReadOnly) && (hasPermission);
  }
  
  private boolean hasPermission(TestCycleOutputBean testCycleOutputBean, TestRunOutputBean testRunOutputBean) {
    return permissionUtil.hasSynapsePermission(getOperatingIssue(testCycleOutputBean, testRunOutputBean).getProjectObject(), SynapsePermission.EXECUTE_TESTRUNS);
  }
}
