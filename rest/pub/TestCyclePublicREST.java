package com.go2group.synapse.rest.pub;

import com.atlassian.annotations.security.XsrfProtectionExcluded;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.go2group.synapse.bean.AutomationAppOutputBean;
import com.go2group.synapse.bean.AutomationBuildDetailOutputBean;
import com.go2group.synapse.bean.TestCycleInputBean;
import com.go2group.synapse.bean.TestCycleOutputBean;
import com.go2group.synapse.constant.SynapsePermission;
import com.go2group.synapse.core.audit.log.ActionEnum;
import com.go2group.synapse.core.audit.log.AuditLogInputBean;
import com.go2group.synapse.core.audit.log.ModuleEnum;
import com.go2group.synapse.core.audit.log.SourceEnum;
import com.go2group.synapse.core.audit.log.service.AuditLogService;
import com.go2group.synapse.core.exception.InvalidDataException;
import com.go2group.synapse.enums.AutomationRunStepEnum;
import com.go2group.synapse.enums.AutomationTestTypeEnum;
import com.go2group.synapse.exception.ApplicationAccessException;
import com.go2group.synapse.manager.PermissionUtilAbstract;
import com.go2group.synapse.service.ConfigService;
import com.go2group.synapse.service.IntegrationService;
import com.go2group.synapse.service.IntegrationServiceFactory;
import com.go2group.synapse.service.TestCycleService;
import com.go2group.synapse.service.impl.ratelimit.DefaultRestRateLimiter;
import com.go2group.synapse.util.PluginUtil;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

@Path("public/testCycle")
@Consumes({"application/json"})
@Produces({"application/json"})
public class TestCyclePublicREST
  extends AbstractPublicREST
{
  private static final Logger log = Logger.getLogger(TestCyclePublicREST.class);
  
  private final TestCycleService testCycleService;
  
  private final IssueManager issueManager;
  
  private ConfigService configService;
  
  private final I18nHelper i18n;
  
  private final AuditLogService auditLogService;
  
  private final IntegrationServiceFactory integrationServiceFactory;
  

  public TestCyclePublicREST(@ComponentImport IssueManager issueManager, @ComponentImport I18nHelper i18n, PermissionUtilAbstract permissionUtil, TestCycleService testCycleService, ConfigService configService, AuditLogService auditLogService, IntegrationServiceFactory integrationServiceFactory)
  {
    super(permissionUtil);
    this.issueManager = issueManager;
    this.testCycleService = testCycleService;
    this.i18n = i18n;
    this.configService = configService;
    this.auditLogService = auditLogService;
    this.integrationServiceFactory = integrationServiceFactory;
  }
  
  @POST
  @Path("addAutomationJobToTestCycle")
  @XsrfProtectionExcluded
  public Response addAutomationCycle(AutomationRestBean automationRestBean) {
    HttpServletRequest request = ExecutingHttpRequest.get();
    boolean canProceed = DefaultRestRateLimiter.canRequestProceed(request);
    if (!canProceed) {
      return rateLimitExceeded("Too many Requests");
    }
    if (automationRestBean == null) {
      log.debug("NULL object received where automation data is expected");
      return notFound(i18n.getText("synapse.validation.no.input"));
    }
    

    if (StringUtils.isBlank(automationRestBean.getTestPlanKey())) {
      return notFound(i18n.getText("errormessage.automationstep.validation.noplankey"));
    }
    if (StringUtils.isBlank(automationRestBean.getTestCycleName())) {
      return notFound(i18n.getText("errormessage.automationstep.validation.nocyclename"));
    }
    if (StringUtils.isBlank(automationRestBean.getJobKey())) {
      return notFound(i18n.getText("synapse.web.panel.error.add.automationstep"));
    }
    if (StringUtils.isBlank(automationRestBean.getAppName())) {
      return notFound(i18n.getText("errormessage.automationstep.validation.noappname"));
    }
    if (StringUtils.isBlank(automationRestBean.getTestType())) {
      return notFound(i18n.getText("errormessage.automationstep.validation.notesttype"));
    }
    
    if (log.isDebugEnabled()) {
      log.debug("Progressing cycle in testplan :" + automationRestBean.getTestPlanKey());
      log.debug("Cycle Name :" + automationRestBean.getTestCycleName());
    }
    
    Issue tpIssue = issueManager.getIssueByKeyIgnoreCase(automationRestBean.getTestPlanKey());
    
    if (tpIssue == null) {
      log.debug("Test Plan issue not found for key:" + automationRestBean.getTestPlanKey());
      return notFound(i18n.getText("servererror.rest.testplan.notfound", automationRestBean.getTestPlanKey()));
    }
    

    if (!hasValidLicense()) {
      log.debug("Invalid license");
      return forbidden(i18n.getText("servererror.rest.invalid.license"));
    }
    

    if (!hasEditPermission(tpIssue)) {
      log.debug("Does not have enough edit permission on the issue");
      return forbidden(i18n.getText("servererror.rest.no.edit.permission"));
    }
    

    if (!hasSynapsePermission(tpIssue.getProjectObject(), SynapsePermission.MANAGE_TESTPLANS)) {
      log.debug("User does not have permission to Manage Test Plans");
      return forbidden(i18n.getText("servererror.rest.no.manage.testplan.permssion"));
    }
    try
    {
      TestCycleOutputBean tCycleBean = testCycleService.getCycleByName(tpIssue.getId(), automationRestBean.getTestCycleName());
      appOutputBeans = configService.getAutomationApps();
      log.debug("appOutputBeans : " + appOutputBeans);
      
      Integer appId = null;
      if (appOutputBeans == null) {
        return notFound(i18n.getText("errormessage.automationstep.validation.appintegration"));
      }
      
      for (Object localObject1 = appOutputBeans.iterator(); ((Iterator)localObject1).hasNext();) { AutomationAppOutputBean appOutputBean = (AutomationAppOutputBean)((Iterator)localObject1).next();
        if (appOutputBean.getAppName().equalsIgnoreCase(automationRestBean.getAppName())) {
          appId = appOutputBean.getID();
        }
      }
      if (appId == null) {
        return notFound(i18n.getText("errormessage.automationstep.validation.appname"));
      }
      if ((!automationRestBean.getTestType().equals(AutomationTestTypeEnum.JUNIT.getKey())) && 
        (!automationRestBean.getTestType().equals(AutomationTestTypeEnum.SELENIUM.getKey())) && 
        (!automationRestBean.getTestType().equals(AutomationTestTypeEnum.TESTNG.getKey()))) {
        return notFound(i18n.getText("errormessage.automationstep.validation.testtype"));
      }
      TestCycleInputBean cycleInputBean = new TestCycleInputBean();
      
      cycleInputBean.setId(tCycleBean.getID());
      cycleInputBean.setApplicationId(appId);
      cycleInputBean.setTriggerKey(automationRestBean.getJobKey());
      cycleInputBean.setTestType(automationRestBean.getTestType());
      cycleInputBean.setExecutionStatus(AutomationRunStepEnum.READY.getValue());
      
      String params = automationRestBean.getParams();
      if (params != null) {
        if (StringUtils.isNotBlank(params)) {
          if (params.contains("=")) {
            String srtCycle = ";SRTCYCLE=" + tCycleBean.getID();
            params = params + srtCycle;
          } else {
            String srtCycle = "SRTCYCLE=" + tCycleBean.getID();
            params = srtCycle;
          }
        } else {
          String srtCycle = "SRTCYCLE=" + tCycleBean.getID();
          params = srtCycle;
        }
      } else {
        params = "";
      }
      params = params.replaceAll("\n", "");
      params = params.replaceAll(" ", "");
      
      cycleInputBean.setBuildParams(params);
      
      AutomationBuildDetailOutputBean automationBuildDetail = testCycleService.createCycleAutomationDetail(cycleInputBean);
      

      AuditLogInputBean auditLogInputBean = PluginUtil.getAuditLogInputBean(ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser(), tpIssue.getProjectObject());
      auditLogInputBean.setAction(ActionEnum.CREATED);
      auditLogInputBean.setModule(ModuleEnum.TEST_CYCLE);
      auditLogInputBean.setSource(SourceEnum.REST.getName());
      auditLogInputBean.setLogTime(new Date());
      auditLogInputBean.setLog("Created Automation Test '" + automationBuildDetail.getAutomationAppBean().getAppName() + "|" + automationBuildDetail
        .getTriggerKey() + "' in the Test Cycle '" + automationBuildDetail.getTestCycle().getName() + "|" + automationBuildDetail
        .getTestType() + "' of the Test Plan '" + tpIssue.getKey() + "' through REST");
      auditLogService.createAuditLog(auditLogInputBean);
      

      return success();
    } catch (InvalidDataException e) {
      log.error(e.getMessage());
      log.debug(e.getMessage(), e);
      return error(e);
    } catch (Exception ex) { List<AutomationAppOutputBean> appOutputBeans;
      log.error(ex.getMessage());
      log.debug(ex.getMessage(), ex);
      return error(ex);
    } finally {
      DefaultRestRateLimiter.endCall(request);
    }
  }
  
  @POST
  @Path("automationJobs")
  @XsrfProtectionExcluded
  public Response getAutomationJobsOfCycle(String data) {
    HttpServletRequest request = ExecutingHttpRequest.get();
    boolean canProceed = DefaultRestRateLimiter.canRequestProceed(request);
    if (!canProceed) {
      return rateLimitExceeded("Too many Requests");
    }
    
    if (StringUtils.isBlank(data)) {
      log.debug("NULL object received where test plan key and cycleName are expected");
      return notFound(i18n.getText("synapse.validation.no.input"));
    }
    

    String cycleName = null;
    String tpKey = null;
    try {
      JSONObject jsonObject = new JSONObject(data);
      tpKey = jsonObject.has("testPlanKey") ? jsonObject.getString("testPlanKey") : "";
      cycleName = jsonObject.has("testCycleName") ? jsonObject.getString("testCycleName") : "";
    } catch (JSONException e) {
      log.error(e.getMessage());
      log.debug(e.getMessage(), e);
      return error(e);
    }
    
    JSONObject jsonObject;
    if (StringUtils.isBlank(tpKey)) {
      return notFound(i18n.getText("errormessage.automationstep.validation.noplankey"));
    }
    if (StringUtils.isBlank(cycleName)) {
      return notFound(i18n.getText("errormessage.automationstep.validation.nocyclename"));
    }
    
    Issue tpIssue = issueManager.getIssueByKeyIgnoreCase(tpKey);
    
    if (tpIssue == null) {
      log.debug("Test Plan issue not found for key:" + tpKey);
      return notFound(i18n.getText("servererror.rest.testplan.notfound", tpKey));
    }
    

    if (!hasValidLicense()) {
      log.debug("Invalid license");
      return forbidden(i18n.getText("servererror.rest.invalid.license"));
    }
    

    if (!hasEditPermission(tpIssue)) {
      log.debug("Does not have enough edit permission on the issue");
      return forbidden(i18n.getText("servererror.rest.no.edit.permission"));
    }
    

    if (!hasSynapsePermission(tpIssue.getProjectObject(), SynapsePermission.MANAGE_TESTPLANS)) {
      log.debug("User does not have permission to Manage Test Plans");
      return forbidden(i18n.getText("servererror.rest.no.manage.testplan.permssion"));
    }
    try
    {
      TestCycleOutputBean tCycleBean = testCycleService.getCycleByName(tpIssue.getId(), cycleName);
      automationJobs = tCycleBean.getBuildDetailBean();
      List<AutomationRestOutputBean> autoJobs = new ArrayList();
      Object localObject1; if ((automationJobs != null) && (!automationJobs.isEmpty())) {
        for (localObject1 = automationJobs.iterator(); ((Iterator)localObject1).hasNext();) { AutomationBuildDetailOutputBean automationJob = (AutomationBuildDetailOutputBean)((Iterator)localObject1).next();
          autoJobs.add(new AutomationRestOutputBean(automationJob));
        }
      }
      return Response.ok(autoJobs).build();
    } catch (InvalidDataException e) {
      log.error(e.getMessage());
      log.debug(e.getMessage(), e);
      return error(e);
    } catch (Exception ex) { List<AutomationBuildDetailOutputBean> automationJobs;
      log.error(ex.getMessage());
      log.debug(ex.getMessage(), ex);
      return error(ex);
    } finally {
      DefaultRestRateLimiter.endCall(request);
    }
  }
  
  @POST
  @Path("triggerAutomationJob")
  @XsrfProtectionExcluded
  public Response triggerAutomationJob(String data) {
    HttpServletRequest request = ExecutingHttpRequest.get();
    boolean canProceed = DefaultRestRateLimiter.canRequestProceed(request);
    if (!canProceed) {
      return rateLimitExceeded("Too many Requests");
    }
    
    if (StringUtils.isBlank(data)) {
      log.debug("NULL object received where test plan key and cycleName are expected");
      return notFound(i18n.getText("synapse.validation.no.input"));
    }
    

    String cycleName = null;
    String tpKey = null;
    Integer automationBuildId = null;
    try {
      JSONObject jsonObject = new JSONObject(data);
      tpKey = jsonObject.has("testPlanKey") ? jsonObject.getString("testPlanKey") : "";
      cycleName = jsonObject.has("testCycleName") ? jsonObject.getString("testCycleName") : "";
      automationBuildId = Integer.valueOf(jsonObject.has("automationBuildId") ? jsonObject.getInt("automationBuildId") : 0);
    } catch (JSONException e) {
      log.error(e.getMessage());
      log.debug(e.getMessage(), e);
      return error(e);
    }
    
    JSONObject jsonObject;
    if (StringUtils.isBlank(tpKey)) {
      return notFound(i18n.getText("errormessage.automationstep.validation.noplankey"));
    }
    if (StringUtils.isBlank(cycleName)) {
      return notFound(i18n.getText("errormessage.automationstep.validation.nocyclename"));
    }
    if ((automationBuildId == null) || (automationBuildId.equals(Integer.valueOf(0)))) {
      return notFound(i18n.getText("errormessage.automationstep.validation.noautomationbuildid"));
    }
    
    Issue tpIssue = issueManager.getIssueByKeyIgnoreCase(tpKey);
    
    if (tpIssue == null) {
      log.debug("Test Plan issue not found for key:" + tpKey);
      return notFound(i18n.getText("servererror.rest.testplan.notfound", tpKey));
    }
    

    if (!hasValidLicense()) {
      log.debug("Invalid license");
      return forbidden(i18n.getText("servererror.rest.invalid.license"));
    }
    

    if (!hasEditPermission(tpIssue)) {
      log.debug("Does not have enough edit permission on the issue");
      return forbidden(i18n.getText("servererror.rest.no.edit.permission"));
    }
    

    if (!hasSynapsePermission(tpIssue.getProjectObject(), SynapsePermission.MANAGE_TESTPLANS)) {
      log.debug("User does not have permission to Manage Test Plans");
      return forbidden(i18n.getText("servererror.rest.no.manage.testplan.permssion"));
    }
    
    try
    {
      TestCycleOutputBean tCycleBean = testCycleService.getCycleByName(tpIssue.getId(), cycleName);
      buildBeans = tCycleBean.getBuildDetailBean();
      AutomationAppOutputBean appOutputBean = null;
      AutomationBuildDetailOutputBean buildDetailOutputBean = null;
      for (AutomationBuildDetailOutputBean buildBean : buildBeans) {
        if (buildBean.getID().equals(automationBuildId)) {
          buildDetailOutputBean = buildBean;
          appOutputBean = buildBean.getAutomationAppBean();
        }
      }
      
      IntegrationService integrationService = integrationServiceFactory.getService(appOutputBean
        .getAppType());
      


      String callBackUrl = buildDetailOutputBean.getCallBackUrl();
      try {
        callBackUrl = integrationService.triggerCycleBuild(buildDetailOutputBean);
      } catch (ApplicationAccessException e) {
        log.error(e.getMessage());
        log.debug(e.getMessage(), e);
        throw new InvalidDataException(i18n.getText("synapse.web.panel.error.notreachable", e.getMessage()));
      }
      

      TestCycleInputBean cycleInputBean = new TestCycleInputBean();
      
      cycleInputBean.setId(tCycleBean.getID());
      cycleInputBean.setCallBackUrl(callBackUrl);
      cycleInputBean.setExecutionStatus(AutomationRunStepEnum.RUNNING.getValue());
      cycleInputBean.setAutoBuildDetailId(automationBuildId);
      cycleInputBean.setExecutorId(ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser().getId());
      AutomationBuildDetailOutputBean automationBuildDetail = testCycleService.editCycleAutomationDetail(cycleInputBean);
      

      AuditLogInputBean auditLogInputBean = PluginUtil.getAuditLogInputBean(ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser());
      auditLogInputBean.setAction(ActionEnum.UPDATED);
      auditLogInputBean.setModule(ModuleEnum.TEST_CYCLE);
      auditLogInputBean.setSource(SourceEnum.WEB_PAGE.getName());
      auditLogInputBean.setLogTime(new Date());
      auditLogInputBean.setLog("Triggered Automation Test '" + automationBuildDetail.getAutomationAppBean().getAppName() + "|" + automationBuildDetail
        .getTriggerKey() + "' in the Test Cycle '" + automationBuildDetail.getTestCycle().getName() + "|" + automationBuildDetail
        .getTestType() + "' of the Test Plan '" + automationBuildDetail.getTestCycle().getTestPlan().getKey() + "'");
      auditLogService.createAuditLog(auditLogInputBean);
      
      return success();
    } catch (InvalidDataException e) {
      log.error(e.getMessage());
      log.debug(e.getMessage(), e);
      return error(e);
    } catch (Exception ex) { List<AutomationBuildDetailOutputBean> buildBeans;
      log.error(ex.getMessage());
      log.debug(ex.getMessage(), ex);
      return error(ex);
    } finally {
      DefaultRestRateLimiter.endCall(request);
    }
  }
}
