package com.go2group.synapse.rest.pub;

import com.atlassian.annotations.security.XsrfProtectionExcluded;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.json.JSONArray;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.go2group.synapse.bean.IssueWrapperBean;
import com.go2group.synapse.bean.TestCycleOutputBean;
import com.go2group.synapse.bean.TestPlanMemberOutputBean;
import com.go2group.synapse.bean.TestRunBugOutputBean;
import com.go2group.synapse.bean.TestRunDisplayBean;
import com.go2group.synapse.bean.TestRunIterationOutputBean;
import com.go2group.synapse.bean.TestRunOutputBean;
import com.go2group.synapse.bean.TestRunStepOutputBean;
import com.go2group.synapse.bean.TestRunStepUpdateInputBean;
import com.go2group.synapse.bean.TestRunUpdateInputBean;
import com.go2group.synapse.bean.runattribute.RunAttributeValueOutputBean;
import com.go2group.synapse.constant.SynapsePermission;
import com.go2group.synapse.core.audit.log.ActionEnum;
import com.go2group.synapse.core.audit.log.AuditLogInputBean;
import com.go2group.synapse.core.audit.log.ModuleEnum;
import com.go2group.synapse.core.audit.log.SourceEnum;
import com.go2group.synapse.core.audit.log.service.AuditLogService;
import com.go2group.synapse.core.exception.InvalidDataException;
import com.go2group.synapse.enums.StandardStatusEnum;
import com.go2group.synapse.enums.TestRunStatusEnum;
import com.go2group.synapse.manager.PermissionUtilAbstract;
import com.go2group.synapse.service.TestCycleService;
import com.go2group.synapse.service.TestPlanMemberService;
import com.go2group.synapse.service.TestRunService;
import com.go2group.synapse.service.impl.ratelimit.DefaultRestRateLimiter;
import com.go2group.synapse.service.runattribute.RunAttributeService;
import com.go2group.synapse.util.PluginUtil;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;
import org.json.simple.parser.JSONParser;

@Path("public/testRun")
@Consumes({"application/json"})
@Produces({"application/json"})
public class TestRunPublicREST
  extends AbstractPublicREST
{
  private static final Logger log = Logger.getLogger(TestRunPublicREST.class);
  

  private final TestRunService testRunService;
  
  private final I18nHelper i18n;
  
  private final IssueManager issueManager;
  
  private final DateTimeFormatter dateTimeFormatter;
  
  private final UserManager userManager;
  
  private final AuditLogService auditLogService;
  
  private final TestPlanMemberService testPlanMemberService;
  
  private final RunAttributeService runAttributeService;
  
  private final TestCycleService testCycleService;
  

  public TestRunPublicREST(@ComponentImport I18nHelper i18n, @ComponentImport IssueManager issueManager, DateTimeFormatter dateTimeFormatter, @ComponentImport UserManager userManager, PermissionUtilAbstract permissionUtil, TestRunService testRunService, AuditLogService auditLogService, TestPlanMemberService testPlanMemberService, RunAttributeService runAttributeService, TestCycleService testCycleService)
  {
    super(permissionUtil);
    this.testRunService = testRunService;
    this.i18n = i18n;
    this.issueManager = issueManager;
    this.dateTimeFormatter = dateTimeFormatter;
    this.userManager = userManager;
    this.auditLogService = auditLogService;
    this.testPlanMemberService = testPlanMemberService;
    this.runAttributeService = runAttributeService;
    this.testCycleService = testCycleService;
  }
  
  @GET
  @Path("{runId}")
  @XsrfProtectionExcluded
  public Response getTestRunDetails(@PathParam("runId") int runId) {
    log.debug("Retreiving test run details for runId :" + runId);
    try {
      HttpServletRequest request = ExecutingHttpRequest.get();
      canProceed = DefaultRestRateLimiter.canRequestProceed(request);
      Response localResponse1; if (!canProceed) { HttpServletRequest request;
        return rateLimitExceeded("Too many Requests");
      }
      

      if (!hasValidLicense()) {
        log.debug("Invalid license");
        HttpServletRequest request; return forbidden(i18n.getText("servererror.rest.invalid.license"));
      }
      

      Issue operatingIssue = null;
      HttpServletRequest request;
      try { operatingIssue = testRunService.getTestPlanByRun(Integer.valueOf(runId));
      } catch (InvalidDataException e) {
        log.debug(e.getMessage());
        return notFound(i18n.getText("servererror.rest.testrun.notfound", Integer.valueOf(runId)));
      }
      
      if (operatingIssue == null) {
        try
        {
          operatingIssue = testRunService.getTestCaseByRun(Integer.valueOf(runId));
        } catch (InvalidDataException e) {
          log.debug(e.getMessage());
        }
      }
      
      TestRunDisplayBean trWrapperBean;
      if (operatingIssue != null)
      {

        if (!hasViewPermission(operatingIssue)) {
          log.debug("Does not have view permission on the issue");
          HttpServletRequest request; return forbidden(i18n.getText("servererror.rest.no.view.permission"));
        }
        

        trWrapperBean = (TestRunDisplayBean)testRunService.getTestRun(Integer.valueOf(runId));
        TestRunRestBean bean = new TestRunRestBean(trWrapperBean, dateTimeFormatter, userManager, issueManager, i18n);
        HttpServletRequest request; return Response.ok(bean).build();
      }
      
      HttpServletRequest request;
      return forbidden();
    } catch (Exception e) {
      boolean canProceed;
      log.error(e.getMessage());
      log.debug(e.getMessage(), e);
      HttpServletRequest request; return error(e);
    } finally {
      HttpServletRequest request = ExecutingHttpRequest.get();
      DefaultRestRateLimiter.endCall(request);
    }
  }
  
  @GET
  @Path("{testCaseKey}/testRuns")
  @XsrfProtectionExcluded
  public Response getTestRuns(@PathParam("testCaseKey") String testCaseKey) {
    log.debug("Retreiving test case execution history details for :" + testCaseKey);
    try {
      HttpServletRequest request = ExecutingHttpRequest.get();
      canProceed = DefaultRestRateLimiter.canRequestProceed(request);
      Response localResponse; if (!canProceed) { HttpServletRequest request;
        return rateLimitExceeded("Too many Requests");
      }
      
      HttpServletRequest request;
      if (!hasValidLicense()) {
        log.debug("Invalid license");
        return forbidden(i18n.getText("servererror.rest.invalid.license"));
      }
      
      Object tcIssue = issueManager.getIssueByKeyIgnoreCase(testCaseKey);
      List<TestRunOutputBean> tRuns;
      if (tcIssue != null)
      {

        if (!hasViewPermission((Issue)tcIssue)) {
          log.debug("Does not have view permission on the issue");
          HttpServletRequest request; return forbidden(i18n.getText("servererror.rest.no.view.permission"));
        }
        
        tRuns = testRunService.getTestRuns(((Issue)tcIssue).getId(), ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser());
        List<TestRunRestBean> beans = new ArrayList();
        Object localObject1; if ((tRuns != null) && (tRuns.size() > 0)) {
          for (localObject1 = tRuns.iterator(); ((Iterator)localObject1).hasNext();) { TestRunOutputBean tRun = (TestRunOutputBean)((Iterator)localObject1).next();
            TestRunRestBean bean = new TestRunRestBean((TestRunDisplayBean)tRun, dateTimeFormatter, userManager, issueManager, i18n);
            beans.add(bean);
          }
        }
        HttpServletRequest request;
        return Response.ok(beans).build();
      }
      
      HttpServletRequest request;
      return forbidden();
    } catch (Exception e) {
      boolean canProceed;
      log.error(e.getMessage());
      log.debug(e.getMessage(), e);
      HttpServletRequest request; return error(e);
    } finally {
      HttpServletRequest request = ExecutingHttpRequest.get();
      DefaultRestRateLimiter.endCall(request);
    }
  }
  
  @Path("testRunsBetween")
  @POST
  @XsrfProtectionExcluded
  public Response getTestRunsBetween(String data)
  {
    if (!hasValidLicense()) {
      log.debug("Invalid license");
      return forbidden(i18n.getText("servererror.rest.invalid.license"));
    }
    try {
      HttpServletRequest request = ExecutingHttpRequest.get();
      canProceed = DefaultRestRateLimiter.canRequestProceed(request);
      if (!canProceed) { HttpServletRequest request;
        return rateLimitExceeded("Too many Requests");
      }
      Object jsonObject;
      if (StringUtils.isNotBlank(data)) {
        jsonObject = new com.atlassian.jira.util.json.JSONObject(data);
        String fromDate = ((com.atlassian.jira.util.json.JSONObject)jsonObject).getString("fromDate");
        String toDate = null;
        
        if (((com.atlassian.jira.util.json.JSONObject)jsonObject).has("toDate")) {
          toDate = ((com.atlassian.jira.util.json.JSONObject)jsonObject).getString("toDate");
        }
        List<TestRunOutputBean> tRuns;
        if ((fromDate != null) && (fromDate.length() > 0)) {
          log.debug("Retrieving test cases executed from:" + fromDate + " to:" + toDate);
          tRuns = testRunService.getTestRuns(fromDate, toDate, ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser());
          List<TestRunRestBean> beans = new ArrayList();
          Object localObject1; if ((tRuns != null) && (tRuns.size() > 0))
            for (localObject1 = tRuns.iterator(); ((Iterator)localObject1).hasNext();) { TestRunOutputBean tRun = (TestRunOutputBean)((Iterator)localObject1).next();
              TestRunRestBean bean = new TestRunRestBean(new TestRunDisplayBean(tRun), dateTimeFormatter, userManager, issueManager, i18n);
              beans.add(bean);
            }
          HttpServletRequest request;
          return Response.ok(beans).build(); }
        HttpServletRequest request;
        return notFound(i18n.getText("errormessage.validation.invalid.input"));
      }
      HttpServletRequest request;
      return notFound(i18n.getText("errormessage.validation.invalid.input"));
    } catch (Exception e) {
      boolean canProceed;
      log.error(e.getMessage());
      log.debug(e.getMessage(), e);
      HttpServletRequest request; return error(e);
    } finally {
      HttpServletRequest request = ExecutingHttpRequest.get();
      DefaultRestRateLimiter.endCall(request);
    }
  }
  
  @POST
  @Path("update")
  @XsrfProtectionExcluded
  public Response updateTestRun(TestRunUpdateInputBean testRunInput) {
    log.debug("Updating test run with details :" + testRunInput);
    try {
      HttpServletRequest request = ExecutingHttpRequest.get();
      canProceed = DefaultRestRateLimiter.canRequestProceed(request);
      Response localResponse1; if (!canProceed) { HttpServletRequest request;
        return rateLimitExceeded("Too many Requests");
      }
      

      if (!hasValidLicense()) {
        log.debug("Invalid license");
        HttpServletRequest request; return forbidden(i18n.getText("servererror.rest.invalid.license"));
      }
      

      Issue operatingIssue = null;
      HttpServletRequest request;
      try { operatingIssue = testRunService.getTestPlanByRun(testRunInput.getRunId());
      } catch (InvalidDataException e) {
        log.debug(e.getMessage());
        return notFound(i18n.getText("servererror.rest.testrun.notfound", testRunInput.getRunId()));
      }
      
      boolean adhocRun = false;
      
      if (operatingIssue == null) {
        try
        {
          operatingIssue = testRunService.getTestCaseByRun(testRunInput.getRunId());
          adhocRun = true;
        } catch (InvalidDataException e) {
          log.debug(e.getMessage());
        }
      }
      

      if (operatingIssue != null) {
        Object tCycleBean;
        if (!adhocRun)
        {


          tCycleBean = testRunService.getTestCycleByRun(testRunInput.getRunId());
          
          if ((isTestPlanResolved(operatingIssue)) || (isCycleReadOnly((TestCycleOutputBean)tCycleBean))) {
            log.debug("Does not have enough edit permission on the issue");
            HttpServletRequest request; return forbidden(i18n.getText("servererror.rest.no.edit.permission"));
          }
        }
        


        if (!hasEditPermission(operatingIssue)) {
          log.debug("Does not have enough edit permission on the issue");
          HttpServletRequest request; return forbidden(i18n.getText("servererror.rest.no.edit.permission"));
        }
        

        if (!hasSynapsePermission(operatingIssue.getProjectObject(), SynapsePermission.EXECUTE_TESTRUNS)) {
          log.debug("User does not have permission to Execute Test Runs");
          HttpServletRequest request; return forbidden(i18n.getText("servererror.rest.no.execute.testrun.permission"));
        }
        

        Object statusEnum;
        
        if (testRunInput.getResult() != null) {
          statusEnum = TestRunStatusEnum.getEnum(testRunInput.getResult());
          if (statusEnum == null) {
            StandardStatusEnum standardStatusEnum = StandardStatusEnum.getEnum(testRunInput.getResult());
            if (standardStatusEnum != null) {
              statusEnum = TestRunStatusEnum.getStandardStausEnumByKey(standardStatusEnum.getKey());
            }
          }
          TestRunOutputBean testRun = testRunService.updateTestRunStatus(testRunInput.getRunId(), (TestRunStatusEnum)statusEnum);
          
          Project project = adhocRun ? testRun.getTestCase().getProjectObject() : testRun.getCycle().getTestPlan().getProjectObject();
          AuditLogInputBean auditLogInputBean = PluginUtil.getAuditLogInputBean(ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser(), project);
          auditLogInputBean.setAction(ActionEnum.UPDATED);
          auditLogInputBean.setModule(ModuleEnum.TEST_RUN);
          auditLogInputBean.setSource(SourceEnum.WEB_PAGE.getName());
          auditLogInputBean.setLogTime(new Date());
          if (testRun.getCycle().isAdhocTestCycle()) {
            auditLogInputBean.setLog("Updated status '" + testRunInput.getResult() + "' to Adhoc Test Run 'TR" + testRun.getID() + "' of the Test Case '" + testRun.getTestCaseKey() + "' through REST");
          } else {
            auditLogInputBean.setLog("Updated status '" + testRunInput.getResult() + "' to Test Run '" + testRun.getTestCaseKey() + "' of Test Cycle '" + testRun.getCycle().getName() + "' of the Test Plan '" + testRun.getCycle().getTestPlan().getKey() + "' through REST");
          }
          auditLogService.createAuditLog(auditLogInputBean);
        }
        


        if (testRunInput.getComment() != null) {
          testRunService.updateTestRunComment(testRunInput.getRunId(), testRunInput.getComment());
        }
        
        if ((testRunInput.getBugs() != null) && (testRunInput.getBugs().size() > 0)) {
          for (statusEnum = testRunInput.getBugs().iterator(); ((Iterator)statusEnum).hasNext();) { String bugKey = (String)((Iterator)statusEnum).next();
            
            Issue bugIssue = issueManager.getIssueByKeyIgnoreCase(bugKey);
            if (bugIssue != null) {
              testRunService.addTestRunBug(testRunInput.getRunId(), bugIssue.getId(), null);
            } else {
              log.debug("Invalid bug issue key received");
              throw new InvalidDataException(i18n.getText("errormessage.validation.invalid.issuekey", bugKey));
            }
          }
        }
        if (testRunInput.getRunAttributes() != null) {
          testRunService.updateRunAttributes(operatingIssue.getProjectObject().getId(), testRunInput.getRunId(), testRunInput.getRunAttributes());
        }
        HttpServletRequest request;
        return success();
      }
      
      throw new InvalidDataException(i18n.getText("synapse.common.message.system.error"));
    } catch (Exception e) {
      boolean canProceed;
      log.error(e.getMessage());
      log.debug(e.getMessage(), e);
      HttpServletRequest request; return error(e);
    } finally {
      HttpServletRequest request = ExecutingHttpRequest.get();
      DefaultRestRateLimiter.endCall(request);
    }
  }
  
  @POST
  @Path("updateStep")
  @XsrfProtectionExcluded
  public Response updateTestRunStep(TestRunStepUpdateInputBean trStepUpdateInput) {
    log.debug("Updating test run with details :" + trStepUpdateInput);
    try {
      HttpServletRequest request = ExecutingHttpRequest.get();
      canProceed = DefaultRestRateLimiter.canRequestProceed(request);
      Response localResponse1; if (!canProceed) { HttpServletRequest request;
        return rateLimitExceeded("Too many Requests");
      }
      

      if (!hasValidLicense()) {
        log.debug("Invalid license");
        HttpServletRequest request; return forbidden(i18n.getText("servererror.rest.invalid.license"));
      }
      

      Issue operatingIssue = null;
      HttpServletRequest request;
      try { operatingIssue = testRunService.getTestPlanByRunStep(trStepUpdateInput.getRunStepId());
      } catch (InvalidDataException e) {
        log.debug(e.getMessage());
        return notFound(i18n.getText("servererror.rest.testrunstep.notfound", trStepUpdateInput.getRunStepId()));
      }
      
      boolean adhocRun = false;
      
      if (operatingIssue == null) {
        try
        {
          operatingIssue = testRunService.getTestCaseByRunStep(trStepUpdateInput.getRunStepId());
          adhocRun = true;
        } catch (InvalidDataException e) {
          log.debug(e.getMessage());
        }
      }
      

      if (operatingIssue != null) { Object tCycleBean;
        if (!adhocRun)
        {

          tCycleBean = testRunService.getTestCycleByRunStep(trStepUpdateInput.getRunStepId());
          
          if ((isTestPlanResolved(operatingIssue)) || (isCycleReadOnly((TestCycleOutputBean)tCycleBean))) {
            log.debug("Does not have enough edit permission on the issue");
            HttpServletRequest request; return forbidden(i18n.getText("servererror.rest.no.edit.permission"));
          }
        }
        


        if (!hasEditPermission(operatingIssue)) {
          log.debug("Does not have enough edit permission on the issue");
          HttpServletRequest request; return forbidden(i18n.getText("servererror.rest.no.edit.permission"));
        }
        

        if (!hasSynapsePermission(operatingIssue.getProjectObject(), SynapsePermission.EXECUTE_TESTRUNS)) {
          log.debug("User does not have permission to Execute Test Runs");
          HttpServletRequest request; return forbidden(i18n.getText("servererror.rest.no.execute.testrun.permission"));
        }
        
        Object statusEnum;
        if ((trStepUpdateInput.getResult() != null) && (trStepUpdateInput.getResult().trim().length() > 0)) {
          statusEnum = TestRunStatusEnum.getEnum(trStepUpdateInput.getResult());
          if (statusEnum == null) {
            StandardStatusEnum standardStatusEnum = StandardStatusEnum.getEnum(trStepUpdateInput.getResult());
            if (standardStatusEnum != null) {
              statusEnum = TestRunStatusEnum.getStandardStausEnumByKey(standardStatusEnum.getKey());
            }
          }
          TestRunStepOutputBean testRunStep = testRunService.updateTestRunStepStatus(trStepUpdateInput.getRunStepId(), ((TestRunStatusEnum)statusEnum).getId());
          

          TestRunOutputBean testRun = testRunStep.getTestRun();
          Project project = adhocRun ? testRun.getTestCase().getProjectObject() : testRun.getCycle().getTestPlan().getProjectObject();
          AuditLogInputBean auditLogInputBean = PluginUtil.getAuditLogInputBean(ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser(), project);
          auditLogInputBean.setAction(ActionEnum.UPDATED);
          auditLogInputBean.setModule(ModuleEnum.TEST_RUN);
          auditLogInputBean.setSource(SourceEnum.REST.getName());
          auditLogInputBean.setLogTime(new Date());
          String auditLog = "";
          if (testRun.getCycle().isAdhocTestCycle())
          {

            auditLog = "Updated Test Run step status to '" + testRunStep.getStatus() + "' for the Step '" + PluginUtil.getEllipsisString(testRunStep.getStepRaw(), 50) + "'; Adhoc Test Run:TR" + testRun.getID() + "; Test Iteration:" + testRunStep.getTestRunIteration().getName() + "; Test Case:" + testRun.getTestCaseKey() + " through REST";

          }
          else
          {
            auditLog = "Updated Test Run step status to '" + testRunStep.getStatus() + "' for the Step '" + PluginUtil.getEllipsisString(testRunStep.getStepRaw(), 50) + "'; Test Run:" + testRun.getTestCaseKey() + "; Test Iteration:" + testRunStep.getTestRunIteration().getName() + "; Test Cycle:" + testRun.getCycle().getName() + "; Test Plan:" + testRun.getCycle().getTestPlan().getKey() + " through REST";
          }
          auditLogInputBean.setLog(auditLog);
          auditLogService.createAuditLog(auditLogInputBean);
        }
        

        if ((trStepUpdateInput.getActualResult() != null) && (trStepUpdateInput.getActualResult().trim().length() > 0)) {
          testRunService.updateTestRunStepResult(trStepUpdateInput.getRunStepId(), trStepUpdateInput.getActualResult().trim());
        }
        
        if ((trStepUpdateInput.getBugs() != null) && (trStepUpdateInput.getBugs().size() > 0)) {
          for (statusEnum = trStepUpdateInput.getBugs().iterator(); ((Iterator)statusEnum).hasNext();) { String bugKey = (String)((Iterator)statusEnum).next();
            
            Issue bugIssue = issueManager.getIssueByKeyIgnoreCase(bugKey);
            if (bugIssue != null) {
              TestRunStepOutputBean testRunStep = testRunService.getTestRunStep(trStepUpdateInput.getRunStepId());
              testRunService.addTestRunStepBug(trStepUpdateInput.getRunStepId(), bugIssue.getId(), testRunStep.getTestRunIteration().getName());
            } else {
              log.debug("Invalid bug issue key received");
              throw new InvalidDataException(i18n.getText("errormessage.validation.invalid.issuekey", bugKey));
            }
          }
        }
        HttpServletRequest request;
        return success();
      }
      
      throw new InvalidDataException(i18n.getText("synapse.common.message.system.error"));
    } catch (Exception e) {
      boolean canProceed;
      log.error(e.getMessage());
      log.debug(e.getMessage(), e);
      HttpServletRequest request; return error(e);
    } finally {
      HttpServletRequest request = ExecutingHttpRequest.get();
      DefaultRestRateLimiter.endCall(request);
    }
  }
  

  @GET
  @Path("defects/{runId}")
  @XsrfProtectionExcluded
  public Response getDefectsOfTestRun(@PathParam("runId") int runId)
  {
    log.debug("Retreiving test run defects for runId :" + runId);
    try {
      HttpServletRequest request = ExecutingHttpRequest.get();
      canProceed = DefaultRestRateLimiter.canRequestProceed(request);
      Response localResponse1; if (!canProceed) { HttpServletRequest request;
        return rateLimitExceeded("Too many Requests");
      }
      

      if (!hasValidLicense()) {
        log.debug("Invalid license");
        HttpServletRequest request; return forbidden(i18n.getText("servererror.rest.invalid.license"));
      }
      

      Issue operatingIssue = null;
      try {
        operatingIssue = testRunService.getTestPlanByRun(Integer.valueOf(runId));
      } catch (InvalidDataException e) {
        log.debug(e.getMessage());
        HttpServletRequest request; return notFound(i18n.getText("servererror.rest.testrun.notfound", Integer.valueOf(runId)));
      }
      
      if (operatingIssue == null) {
        try
        {
          operatingIssue = testRunService.getTestCaseByRun(Integer.valueOf(runId));
        } catch (InvalidDataException e) {
          log.debug(e.getMessage());
        }
      }
      
      TestRunDisplayBean trWrapperBean;
      if (operatingIssue != null)
      {
        Object request;
        if (!hasViewPermission(operatingIssue)) {
          log.debug("Does not have view permission on the issue");
          e = forbidden(i18n.getText("servererror.rest.no.view.permission"));
          
















          request = ExecutingHttpRequest.get();
          DefaultRestRateLimiter.endCall((HttpServletRequest)request);return e;
        }
        

        trWrapperBean = (TestRunDisplayBean)testRunService.getTestRun(Integer.valueOf(runId));
        
        HttpServletRequest request;
        return Response.ok(trWrapperBean.getBugs()).build();
      }
      
      HttpServletRequest request;
      return forbidden();
    } catch (Exception e) {
      boolean canProceed;
      log.error(e.getMessage());
      log.debug(e.getMessage(), e);
      HttpServletRequest request; return error(e);
    } finally {
      HttpServletRequest request = ExecutingHttpRequest.get();
      DefaultRestRateLimiter.endCall(request);
    }
  }
  
  @Path("linkTestRunBugs")
  @PUT
  public Response linkTestRunBugs(String data) { log.debug("Adding bugs to test run");
    
    if (!hasValidLicense()) {
      log.debug("Invalid license");
      return forbidden(i18n.getText("servererror.rest.invalid.license"));
    }
    try {
      HttpServletRequest request = ExecutingHttpRequest.get();
      canProceed = DefaultRestRateLimiter.canRequestProceed(request);
      if (!canProceed) { HttpServletRequest request;
        return rateLimitExceeded("Too many Requests");
      }
      Object jsonObject;
      if (StringUtils.isNotBlank(data)) {
        jsonObject = new com.atlassian.jira.util.json.JSONObject(data);
        
        String runId = ((com.atlassian.jira.util.json.JSONObject)jsonObject).getString("runId");
        JSONArray issueKeys = ((com.atlassian.jira.util.json.JSONObject)jsonObject).getJSONArray("issueKeys");
        
        log.debug("Received runId:" + runId);
        log.debug("Received issueKeys:" + issueKeys);
        
        TestRunOutputBean outputBean = testRunService.getTestRun(Integer.valueOf(runId));
        Issue tcIssue = issueManager.getIssueByCurrentKey(outputBean.getTestCaseKey());
        if (permissionUtil.hasSynapsePermission(tcIssue.getProjectObject(), SynapsePermission.EXECUTE_TESTRUNS)) { HttpServletRequest request;
          return 
            Response.serverError().entity(i18n.getText("errormessage.unexpected.server.error") + i18n.getText("servererror.rest.testrunbug.add.failed")).build();
        }
        if ((StringUtils.isNotBlank(runId)) && (issueKeys != null)) {
          for (int counter = 0; counter < issueKeys.length(); counter++) {
            Issue issue = issueManager.getIssueByCurrentKey(issueKeys.getString(counter));
            if (issue != null)
              try {
                TestRunBugOutputBean testRunBug = testRunService.addTestRunBug(Integer.valueOf(runId), issue.getId(), null);
                

                TestRunOutputBean testRun = testRunBug.getTestRun();
                Project project = testRun.getCycle().isAdhocTestCycle() ? testRun.getTestCase().getProjectObject() : testRun.getCycle().getTestPlan().getProjectObject();
                AuditLogInputBean auditLogInputBean = PluginUtil.getAuditLogInputBean(ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser(), project);
                auditLogInputBean.setAction(ActionEnum.ADDED);
                auditLogInputBean.setModule(ModuleEnum.TEST_RUN);
                auditLogInputBean.setSource(SourceEnum.REST.getName());
                auditLogInputBean.setLogTime(new Date());
                String auditLog = "";
                
                if (testRun.getCycle().isAdhocTestCycle()) {
                  auditLog = "Added Defect '" + issue.getKey() + "' to Adhoc Test Run 'TR" + testRun.getID() + "' of the Test Case '" + testRun.getTestCaseKey() + "' through REST";
                }
                else {
                  auditLog = "Added Defect '" + issue.getKey() + "' to Test Run '" + testRun.getTestCaseKey() + "'; Test Cycle:" + testRun.getCycle().getName() + "; Test Plan:" + testRun.getCycle().getTestPlan().getKey() + " through REST";
                }
                auditLogInputBean.setLog(auditLog);
                auditLogService.createAuditLog(auditLogInputBean);
              }
              catch (InvalidDataException idx) {
                log.error(idx.getMessage());
                log.debug(idx.getMessage(), idx);
              }
          }
        }
      }
      HttpServletRequest request;
      return success();
    } catch (Exception e) { boolean canProceed;
      log.error(e.getMessage());
      log.debug(e.getMessage(), e);
      HttpServletRequest request; return 
        Response.serverError().entity(i18n.getText("errormessage.unexpected.server.error") + i18n.getText("servererror.rest.testrunbug.add.failed")).build();
    } finally {
      HttpServletRequest request = ExecutingHttpRequest.get();
      DefaultRestRateLimiter.endCall(request);
    }
  }
  
  @Path("linkTestRunStepBugs")
  @PUT
  public Response linkTestRunStepBugs(String data) {
    log.debug("Adding bugs to test run");
    
    if (!hasValidLicense()) {
      log.debug("Invalid license");
      return forbidden(i18n.getText("servererror.rest.invalid.license"));
    }
    try {
      HttpServletRequest request = ExecutingHttpRequest.get();
      canProceed = DefaultRestRateLimiter.canRequestProceed(request);
      if (!canProceed) { HttpServletRequest request;
        return rateLimitExceeded("Too many Requests");
      }
      Object jsonObject;
      if (StringUtils.isNotBlank(data)) {
        jsonObject = new com.atlassian.jira.util.json.JSONObject(data);
        
        String runId = ((com.atlassian.jira.util.json.JSONObject)jsonObject).getString("runId");
        String runRunStepId = ((com.atlassian.jira.util.json.JSONObject)jsonObject).getString("runRunStepId");
        JSONArray issueKeys = ((com.atlassian.jira.util.json.JSONObject)jsonObject).getJSONArray("issueKeys");
        
        log.debug("Received runId:" + runId);
        log.debug("Received issueKeys:" + issueKeys);
        
        TestRunOutputBean outputBean = testRunService.getTestRun(Integer.valueOf(runId));
        Issue tcIssue = issueManager.getIssueByCurrentKey(outputBean.getTestCaseKey());
        if (permissionUtil.hasSynapsePermission(tcIssue.getProjectObject(), SynapsePermission.EXECUTE_TESTRUNS)) { HttpServletRequest request;
          return 
            Response.serverError().entity(i18n.getText("errormessage.unexpected.server.error") + i18n.getText("servererror.rest.testrunbug.add.failed")).build();
        }
        if ((StringUtils.isNotBlank(runId)) && (issueKeys != null)) {
          TestRunStepOutputBean testRunStep = testRunService.getTestRunStep(Integer.valueOf(runRunStepId));
          List<String> defectKeys = new ArrayList();
          for (int counter = 0; counter < issueKeys.length(); counter++) {
            Issue issue = issueManager.getIssueByCurrentKey(issueKeys.getString(counter));
            if (issue != null) {
              try {
                TestRunBugOutputBean testRunBug = testRunService.addTestRunStepBug(Integer.valueOf(runRunStepId), issue.getId(), testRunStep.getTestRunIteration().getName());
                defectKeys.add(testRunBug.getTestRun().getTestCaseKey());
              } catch (InvalidDataException idx) {
                log.error(idx.getMessage());
                log.debug(idx.getMessage(), idx);
              }
            }
          }
          
          TestRunOutputBean testRun = testRunStep.getTestRun();
          Project project = testRun.getCycle().isAdhocTestCycle() ? testRun.getTestCase().getProjectObject() : testRun.getCycle().getTestPlan().getProjectObject();
          AuditLogInputBean auditLogInputBean = PluginUtil.getAuditLogInputBean(ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser(), project);
          auditLogInputBean.setAction(ActionEnum.ADDED);
          auditLogInputBean.setModule(ModuleEnum.TEST_RUN);
          auditLogInputBean.setSource(SourceEnum.REST.getName());
          auditLogInputBean.setLogTime(new Date());
          String auditLog = "";
          if (testRun.getCycle().isAdhocTestCycle())
          {
            auditLog = "Added Defect(s) '" + defectKeys + "' to Test Run Step '" + PluginUtil.getEllipsisString(testRunStep.getStepRaw(), 50) + "'; Adhoc Test Run:TR" + testRun.getID() + "; Test Case:" + testRunStep.getTestRun().getTestCaseKey();
          }
          else
          {
            auditLog = "Added Defect(s) '" + issueKeys + "' to Test Run Step '" + PluginUtil.getEllipsisString(testRunStep.getStepRaw(), 50) + "'; Test Run '" + testRun.getTestCaseKey() + "'; Test Iteration:" + testRunStep.getTestRunIteration().getName() + "; Test Cycle:" + testRun.getCycle().getName() + "; Test Plan:" + testRun.getCycle().getTestPlan().getKey();
          }
          auditLogInputBean.setLog(auditLog);
          auditLogService.createAuditLog(auditLogInputBean);
        }
      }
      HttpServletRequest request;
      return success();
    } catch (Exception e) { boolean canProceed;
      log.error(e.getMessage());
      log.debug(e.getMessage(), e);
      HttpServletRequest request; return 
        Response.serverError().entity(i18n.getText("errormessage.unexpected.server.error") + i18n.getText("servererror.rest.testrunbug.add.failed")).build();
    } finally {
      HttpServletRequest request = ExecutingHttpRequest.get();
      DefaultRestRateLimiter.endCall(request);
    }
  }
  
  @Path("reorderTestRun")
  @PUT
  public Response reorderTestRun(String data) { log.debug("Reordering Test runs for data :" + data);
    
    if (!hasValidLicense()) {
      log.debug("Invalid license");
      return forbidden(i18n.getText("servererror.rest.invalid.license"));
    }
    try {
      HttpServletRequest request = ExecutingHttpRequest.get();
      canProceed = DefaultRestRateLimiter.canRequestProceed(request);
      if (!canProceed) { HttpServletRequest request;
        return rateLimitExceeded("Too many Requests");
      }
      
      com.atlassian.jira.util.json.JSONObject jsonObject = new com.atlassian.jira.util.json.JSONObject(data);
      Integer testRunId = Integer.valueOf(jsonObject.getInt("testRunId"));
      Integer refTestRunId = Integer.valueOf(jsonObject.getInt("refTestRunId"));
      refTestRunId = (refTestRunId == null) || (refTestRunId.intValue() == -1) ? null : refTestRunId;
      
      TestRunOutputBean runOutputBean = testRunService.getTestRun(testRunId);
      TestCycleOutputBean cycleOutputBean = runOutputBean.getCycle();
      
      Issue tpIssue = issueManager.getIssueObject(runOutputBean.getCycle().getTpId());
      Response localResponse2;
      if ((hasManagePermission(tpIssue)) && (!isReadOnly(tpIssue)) && (!isCycleReadOnlyForReorder(cycleOutputBean))) {
        testRunService.reorderTestRun(testRunId, refTestRunId);
      } else {
        log.debug("Invalid license");
        HttpServletRequest request; return forbidden(i18n.getText("servererror.rest.no.manage.reorder.permission"));
      }
      HttpServletRequest request;
      return success();
    } catch (JSONException|InvalidDataException e) { boolean canProceed;
      log.debug(e.getMessage(), e);
      log.error(e.getMessage());
      HttpServletRequest request; return Response.serverError().entity(e.getMessage()).build();
    } finally {
      HttpServletRequest request = ExecutingHttpRequest.get();
      DefaultRestRateLimiter.endCall(request);
    }
  }
  
  /* Error */
  @Path("adhoc/create/{testCaseKey}")
  @PUT
  public Response createAdhocTestRun(@PathParam("testCaseKey") String testCaseKey)
  {
    // Byte code:
    //   0: getstatic 11	com/go2group/synapse/rest/pub/TestRunPublicREST:log	Lorg/apache/log4j/Logger;
    //   3: new 12	java/lang/StringBuilder
    //   6: dup
    //   7: invokespecial 13	java/lang/StringBuilder:<init>	()V
    //   10: ldc -43
    //   12: invokevirtual 15	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   15: aload_1
    //   16: invokevirtual 15	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   19: invokevirtual 17	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   22: invokevirtual 18	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   25: invokestatic 19	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   28: astore_2
    //   29: aload_2
    //   30: invokestatic 20	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:canRequestProceed	(Ljavax/servlet/http/HttpServletRequest;)Z
    //   33: istore_3
    //   34: iload_3
    //   35: ifne +24 -> 59
    //   38: aload_0
    //   39: ldc 21
    //   41: invokevirtual 22	com/go2group/synapse/rest/pub/TestRunPublicREST:rateLimitExceeded	(Ljava/lang/String;)Ljavax/ws/rs/core/Response;
    //   44: astore 4
    //   46: invokestatic 19	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   49: astore 5
    //   51: aload 5
    //   53: invokestatic 23	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   56: aload 4
    //   58: areturn
    //   59: aload_0
    //   60: invokevirtual 24	com/go2group/synapse/rest/pub/TestRunPublicREST:hasValidLicense	()Z
    //   63: ifne +41 -> 104
    //   66: getstatic 11	com/go2group/synapse/rest/pub/TestRunPublicREST:log	Lorg/apache/log4j/Logger;
    //   69: ldc 25
    //   71: invokevirtual 18	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   74: aload_0
    //   75: aload_0
    //   76: getfield 3	com/go2group/synapse/rest/pub/TestRunPublicREST:i18n	Lcom/atlassian/jira/util/I18nHelper;
    //   79: ldc 26
    //   81: invokeinterface 27 2 0
    //   86: invokevirtual 28	com/go2group/synapse/rest/pub/TestRunPublicREST:forbidden	(Ljava/lang/String;)Ljavax/ws/rs/core/Response;
    //   89: astore 4
    //   91: invokestatic 19	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   94: astore 5
    //   96: aload 5
    //   98: invokestatic 23	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   101: aload 4
    //   103: areturn
    //   104: aload_0
    //   105: getfield 4	com/go2group/synapse/rest/pub/TestRunPublicREST:issueManager	Lcom/atlassian/jira/issue/IssueManager;
    //   108: aload_1
    //   109: invokeinterface 214 2 0
    //   114: astore 4
    //   116: aload_0
    //   117: aload 4
    //   119: invokevirtual 37	com/go2group/synapse/rest/pub/TestRunPublicREST:hasViewPermission	(Lcom/atlassian/jira/issue/Issue;)Z
    //   122: ifne +41 -> 163
    //   125: getstatic 11	com/go2group/synapse/rest/pub/TestRunPublicREST:log	Lorg/apache/log4j/Logger;
    //   128: ldc 38
    //   130: invokevirtual 18	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   133: aload_0
    //   134: aload_0
    //   135: getfield 3	com/go2group/synapse/rest/pub/TestRunPublicREST:i18n	Lcom/atlassian/jira/util/I18nHelper;
    //   138: ldc 39
    //   140: invokeinterface 27 2 0
    //   145: invokevirtual 28	com/go2group/synapse/rest/pub/TestRunPublicREST:forbidden	(Ljava/lang/String;)Ljavax/ws/rs/core/Response;
    //   148: astore 5
    //   150: invokestatic 19	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   153: astore 6
    //   155: aload 6
    //   157: invokestatic 23	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   160: aload 5
    //   162: areturn
    //   163: aload_0
    //   164: aload 4
    //   166: invokeinterface 88 1 0
    //   171: getstatic 89	com/go2group/synapse/constant/SynapsePermission:EXECUTE_TESTRUNS	Lcom/go2group/synapse/constant/SynapsePermission;
    //   174: invokevirtual 90	com/go2group/synapse/rest/pub/TestRunPublicREST:hasSynapsePermission	(Lcom/atlassian/jira/project/Project;Lcom/go2group/synapse/constant/SynapsePermission;)Z
    //   177: ifne +41 -> 218
    //   180: getstatic 11	com/go2group/synapse/rest/pub/TestRunPublicREST:log	Lorg/apache/log4j/Logger;
    //   183: ldc -41
    //   185: invokevirtual 18	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   188: aload_0
    //   189: aload_0
    //   190: getfield 3	com/go2group/synapse/rest/pub/TestRunPublicREST:i18n	Lcom/atlassian/jira/util/I18nHelper;
    //   193: ldc 92
    //   195: invokeinterface 27 2 0
    //   200: invokevirtual 28	com/go2group/synapse/rest/pub/TestRunPublicREST:forbidden	(Ljava/lang/String;)Ljavax/ws/rs/core/Response;
    //   203: astore 5
    //   205: invokestatic 19	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   208: astore 6
    //   210: aload 6
    //   212: invokestatic 23	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   215: aload 5
    //   217: areturn
    //   218: invokestatic 55	com/atlassian/jira/component/ComponentAccessor:getJiraAuthenticationContext	()Lcom/atlassian/jira/security/JiraAuthenticationContext;
    //   221: invokeinterface 56 1 0
    //   226: astore 5
    //   228: aload_0
    //   229: getfield 2	com/go2group/synapse/rest/pub/TestRunPublicREST:testRunService	Lcom/go2group/synapse/service/TestRunService;
    //   232: aload 4
    //   234: invokeinterface 54 1 0
    //   239: aconst_null
    //   240: aload 5
    //   242: invokeinterface 216 1 0
    //   247: invokeinterface 217 4 0
    //   252: astore 6
    //   254: aload 6
    //   256: ifnull +210 -> 466
    //   259: aload 6
    //   261: invokevirtual 100	com/go2group/synapse/bean/TestRunOutputBean:getCycle	()Lcom/go2group/synapse/bean/TestCycleOutputBean;
    //   264: invokevirtual 113	com/go2group/synapse/bean/TestCycleOutputBean:isAdhocTestCycle	()Z
    //   267: ifeq +16 -> 283
    //   270: aload 6
    //   272: invokevirtual 99	com/go2group/synapse/bean/TestRunOutputBean:getTestCase	()Lcom/atlassian/jira/issue/Issue;
    //   275: invokeinterface 88 1 0
    //   280: goto +16 -> 296
    //   283: aload 6
    //   285: invokevirtual 100	com/go2group/synapse/bean/TestRunOutputBean:getCycle	()Lcom/go2group/synapse/bean/TestCycleOutputBean;
    //   288: invokevirtual 101	com/go2group/synapse/bean/TestCycleOutputBean:getTestPlan	()Lcom/atlassian/jira/issue/Issue;
    //   291: invokeinterface 88 1 0
    //   296: astore 7
    //   298: invokestatic 55	com/atlassian/jira/component/ComponentAccessor:getJiraAuthenticationContext	()Lcom/atlassian/jira/security/JiraAuthenticationContext;
    //   301: invokeinterface 56 1 0
    //   306: aload 7
    //   308: invokestatic 102	com/go2group/synapse/util/PluginUtil:getAuditLogInputBean	(Lcom/atlassian/jira/user/ApplicationUser;Lcom/atlassian/jira/project/Project;)Lcom/go2group/synapse/core/audit/log/AuditLogInputBean;
    //   311: astore 8
    //   313: aload 8
    //   315: getstatic 218	com/go2group/synapse/core/audit/log/ActionEnum:CREATED	Lcom/go2group/synapse/core/audit/log/ActionEnum;
    //   318: invokevirtual 104	com/go2group/synapse/core/audit/log/AuditLogInputBean:setAction	(Lcom/go2group/synapse/core/audit/log/ActionEnum;)V
    //   321: aload 8
    //   323: getstatic 219	com/go2group/synapse/core/audit/log/ModuleEnum:TEST_CASE	Lcom/go2group/synapse/core/audit/log/ModuleEnum;
    //   326: invokevirtual 106	com/go2group/synapse/core/audit/log/AuditLogInputBean:setModule	(Lcom/go2group/synapse/core/audit/log/ModuleEnum;)V
    //   329: aload 8
    //   331: getstatic 151	com/go2group/synapse/core/audit/log/SourceEnum:REST	Lcom/go2group/synapse/core/audit/log/SourceEnum;
    //   334: invokevirtual 108	com/go2group/synapse/core/audit/log/SourceEnum:getName	()Ljava/lang/String;
    //   337: invokevirtual 109	com/go2group/synapse/core/audit/log/AuditLogInputBean:setSource	(Ljava/lang/String;)V
    //   340: aload 8
    //   342: new 110	java/util/Date
    //   345: dup
    //   346: invokespecial 111	java/util/Date:<init>	()V
    //   349: invokevirtual 112	com/go2group/synapse/core/audit/log/AuditLogInputBean:setLogTime	(Ljava/util/Date;)V
    //   352: aload 8
    //   354: new 12	java/lang/StringBuilder
    //   357: dup
    //   358: invokespecial 13	java/lang/StringBuilder:<init>	()V
    //   361: ldc -36
    //   363: invokevirtual 15	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   366: aload 6
    //   368: invokevirtual 116	com/go2group/synapse/bean/TestRunOutputBean:getID	()Ljava/lang/Integer;
    //   371: invokevirtual 80	java/lang/StringBuilder:append	(Ljava/lang/Object;)Ljava/lang/StringBuilder;
    //   374: ldc -35
    //   376: invokevirtual 15	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   379: aload 6
    //   381: invokevirtual 118	com/go2group/synapse/bean/TestRunOutputBean:getTestCaseKey	()Ljava/lang/String;
    //   384: invokevirtual 15	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   387: ldc -34
    //   389: invokevirtual 15	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   392: invokevirtual 17	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   395: invokevirtual 120	com/go2group/synapse/core/audit/log/AuditLogInputBean:setLog	(Ljava/lang/String;)V
    //   398: aload_0
    //   399: getfield 7	com/go2group/synapse/rest/pub/TestRunPublicREST:auditLogService	Lcom/go2group/synapse/core/audit/log/service/AuditLogService;
    //   402: aload 8
    //   404: invokeinterface 126 2 0
    //   409: new 42	com/go2group/synapse/rest/pub/TestRunRestBean
    //   412: dup
    //   413: new 41	com/go2group/synapse/bean/TestRunDisplayBean
    //   416: dup
    //   417: aload 6
    //   419: invokespecial 77	com/go2group/synapse/bean/TestRunDisplayBean:<init>	(Lcom/go2group/synapse/bean/TestRunOutputBean;)V
    //   422: aload_0
    //   423: getfield 5	com/go2group/synapse/rest/pub/TestRunPublicREST:dateTimeFormatter	Lcom/atlassian/jira/datetime/DateTimeFormatter;
    //   426: aload_0
    //   427: getfield 6	com/go2group/synapse/rest/pub/TestRunPublicREST:userManager	Lcom/atlassian/jira/user/util/UserManager;
    //   430: aload_0
    //   431: getfield 4	com/go2group/synapse/rest/pub/TestRunPublicREST:issueManager	Lcom/atlassian/jira/issue/IssueManager;
    //   434: aload_0
    //   435: getfield 3	com/go2group/synapse/rest/pub/TestRunPublicREST:i18n	Lcom/atlassian/jira/util/I18nHelper;
    //   438: invokespecial 43	com/go2group/synapse/rest/pub/TestRunRestBean:<init>	(Lcom/go2group/synapse/bean/TestRunDisplayBean;Lcom/atlassian/jira/datetime/DateTimeFormatter;Lcom/atlassian/jira/user/util/UserManager;Lcom/atlassian/jira/issue/IssueManager;Lcom/atlassian/jira/util/I18nHelper;)V
    //   441: astore 9
    //   443: aload 9
    //   445: invokestatic 44	javax/ws/rs/core/Response:ok	(Ljava/lang/Object;)Ljavax/ws/rs/core/Response$ResponseBuilder;
    //   448: invokevirtual 45	javax/ws/rs/core/Response$ResponseBuilder:build	()Ljavax/ws/rs/core/Response;
    //   451: astore 10
    //   453: invokestatic 19	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   456: astore 11
    //   458: aload 11
    //   460: invokestatic 23	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   463: aload 10
    //   465: areturn
    //   466: getstatic 11	com/go2group/synapse/rest/pub/TestRunPublicREST:log	Lorg/apache/log4j/Logger;
    //   469: ldc -33
    //   471: invokevirtual 18	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   474: new 224	com/go2group/synapse/bean/HtmlResponseWrapper
    //   477: dup
    //   478: ldc -104
    //   480: invokespecial 225	com/go2group/synapse/bean/HtmlResponseWrapper:<init>	(Ljava/lang/String;)V
    //   483: invokestatic 44	javax/ws/rs/core/Response:ok	(Ljava/lang/Object;)Ljavax/ws/rs/core/Response$ResponseBuilder;
    //   486: invokevirtual 45	javax/ws/rs/core/Response$ResponseBuilder:build	()Ljavax/ws/rs/core/Response;
    //   489: astore 7
    //   491: invokestatic 19	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   494: astore 8
    //   496: aload 8
    //   498: invokestatic 23	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   501: aload 7
    //   503: areturn
    //   504: astore 6
    //   506: getstatic 11	com/go2group/synapse/rest/pub/TestRunPublicREST:log	Lorg/apache/log4j/Logger;
    //   509: aload 6
    //   511: invokevirtual 32	com/go2group/synapse/core/exception/InvalidDataException:getMessage	()Ljava/lang/String;
    //   514: aload 6
    //   516: invokevirtual 50	org/apache/log4j/Logger:debug	(Ljava/lang/Object;Ljava/lang/Throwable;)V
    //   519: getstatic 11	com/go2group/synapse/rest/pub/TestRunPublicREST:log	Lorg/apache/log4j/Logger;
    //   522: aload 6
    //   524: invokevirtual 32	com/go2group/synapse/core/exception/InvalidDataException:getMessage	()Ljava/lang/String;
    //   527: invokevirtual 49	org/apache/log4j/Logger:error	(Ljava/lang/Object;)V
    //   530: invokestatic 185	javax/ws/rs/core/Response:serverError	()Ljavax/ws/rs/core/Response$ResponseBuilder;
    //   533: aload 6
    //   535: invokevirtual 32	com/go2group/synapse/core/exception/InvalidDataException:getMessage	()Ljava/lang/String;
    //   538: invokevirtual 188	javax/ws/rs/core/Response$ResponseBuilder:entity	(Ljava/lang/Object;)Ljavax/ws/rs/core/Response$ResponseBuilder;
    //   541: invokevirtual 45	javax/ws/rs/core/Response$ResponseBuilder:build	()Ljavax/ws/rs/core/Response;
    //   544: astore 7
    //   546: invokestatic 19	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   549: astore 8
    //   551: aload 8
    //   553: invokestatic 23	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   556: aload 7
    //   558: areturn
    //   559: astore 6
    //   561: getstatic 11	com/go2group/synapse/rest/pub/TestRunPublicREST:log	Lorg/apache/log4j/Logger;
    //   564: aload 6
    //   566: invokevirtual 48	java/lang/Exception:getMessage	()Ljava/lang/String;
    //   569: aload 6
    //   571: invokevirtual 50	org/apache/log4j/Logger:debug	(Ljava/lang/Object;Ljava/lang/Throwable;)V
    //   574: getstatic 11	com/go2group/synapse/rest/pub/TestRunPublicREST:log	Lorg/apache/log4j/Logger;
    //   577: aload 6
    //   579: invokevirtual 48	java/lang/Exception:getMessage	()Ljava/lang/String;
    //   582: invokevirtual 49	org/apache/log4j/Logger:error	(Ljava/lang/Object;)V
    //   585: invokestatic 185	javax/ws/rs/core/Response:serverError	()Ljavax/ws/rs/core/Response$ResponseBuilder;
    //   588: new 12	java/lang/StringBuilder
    //   591: dup
    //   592: invokespecial 13	java/lang/StringBuilder:<init>	()V
    //   595: aload_0
    //   596: getfield 3	com/go2group/synapse/rest/pub/TestRunPublicREST:i18n	Lcom/atlassian/jira/util/I18nHelper;
    //   599: ldc -70
    //   601: invokeinterface 27 2 0
    //   606: invokevirtual 15	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   609: aload_0
    //   610: getfield 3	com/go2group/synapse/rest/pub/TestRunPublicREST:i18n	Lcom/atlassian/jira/util/I18nHelper;
    //   613: ldc -30
    //   615: invokeinterface 27 2 0
    //   620: invokevirtual 15	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   623: invokevirtual 17	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   626: invokevirtual 188	javax/ws/rs/core/Response$ResponseBuilder:entity	(Ljava/lang/Object;)Ljavax/ws/rs/core/Response$ResponseBuilder;
    //   629: invokevirtual 45	javax/ws/rs/core/Response$ResponseBuilder:build	()Ljavax/ws/rs/core/Response;
    //   632: astore 7
    //   634: invokestatic 19	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   637: astore 8
    //   639: aload 8
    //   641: invokestatic 23	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   644: aload 7
    //   646: areturn
    //   647: astore 12
    //   649: invokestatic 19	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   652: astore 13
    //   654: aload 13
    //   656: invokestatic 23	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   659: aload 12
    //   661: athrow
    // Line number table:
    //   Java source line #831	-> byte code offset #0
    //   Java source line #833	-> byte code offset #25
    //   Java source line #834	-> byte code offset #29
    //   Java source line #835	-> byte code offset #34
    //   Java source line #836	-> byte code offset #38
    //   Java source line #893	-> byte code offset #46
    //   Java source line #894	-> byte code offset #51
    //   Java source line #836	-> byte code offset #56
    //   Java source line #840	-> byte code offset #59
    //   Java source line #841	-> byte code offset #66
    //   Java source line #842	-> byte code offset #74
    //   Java source line #893	-> byte code offset #91
    //   Java source line #894	-> byte code offset #96
    //   Java source line #842	-> byte code offset #101
    //   Java source line #845	-> byte code offset #104
    //   Java source line #847	-> byte code offset #116
    //   Java source line #848	-> byte code offset #125
    //   Java source line #849	-> byte code offset #133
    //   Java source line #893	-> byte code offset #150
    //   Java source line #894	-> byte code offset #155
    //   Java source line #849	-> byte code offset #160
    //   Java source line #854	-> byte code offset #163
    //   Java source line #855	-> byte code offset #180
    //   Java source line #856	-> byte code offset #188
    //   Java source line #893	-> byte code offset #205
    //   Java source line #894	-> byte code offset #210
    //   Java source line #856	-> byte code offset #215
    //   Java source line #859	-> byte code offset #218
    //   Java source line #861	-> byte code offset #228
    //   Java source line #862	-> byte code offset #254
    //   Java source line #864	-> byte code offset #259
    //   Java source line #865	-> byte code offset #298
    //   Java source line #866	-> byte code offset #313
    //   Java source line #867	-> byte code offset #321
    //   Java source line #868	-> byte code offset #329
    //   Java source line #869	-> byte code offset #340
    //   Java source line #870	-> byte code offset #352
    //   Java source line #871	-> byte code offset #398
    //   Java source line #874	-> byte code offset #409
    //   Java source line #875	-> byte code offset #443
    //   Java source line #893	-> byte code offset #453
    //   Java source line #894	-> byte code offset #458
    //   Java source line #875	-> byte code offset #463
    //   Java source line #877	-> byte code offset #466
    //   Java source line #878	-> byte code offset #474
    //   Java source line #893	-> byte code offset #491
    //   Java source line #894	-> byte code offset #496
    //   Java source line #878	-> byte code offset #501
    //   Java source line #880	-> byte code offset #504
    //   Java source line #881	-> byte code offset #506
    //   Java source line #882	-> byte code offset #519
    //   Java source line #883	-> byte code offset #530
    //   Java source line #893	-> byte code offset #546
    //   Java source line #894	-> byte code offset #551
    //   Java source line #883	-> byte code offset #556
    //   Java source line #884	-> byte code offset #559
    //   Java source line #885	-> byte code offset #561
    //   Java source line #886	-> byte code offset #574
    //   Java source line #888	-> byte code offset #585
    //   Java source line #889	-> byte code offset #601
    //   Java source line #890	-> byte code offset #629
    //   Java source line #893	-> byte code offset #634
    //   Java source line #894	-> byte code offset #639
    //   Java source line #887	-> byte code offset #644
    //   Java source line #893	-> byte code offset #647
    //   Java source line #894	-> byte code offset #654
    //   Java source line #895	-> byte code offset #659
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	662	0	this	TestRunPublicREST
    //   0	662	1	testCaseKey	String
    //   28	2	2	request	HttpServletRequest
    //   33	2	3	canProceed	boolean
    //   44	58	4	localResponse1	Response
    //   114	119	4	testcaseIssue	Object
    //   49	3	5	request	HttpServletRequest
    //   94	122	5	request	HttpServletRequest
    //   226	15	5	user	ApplicationUser
    //   153	3	6	request	HttpServletRequest
    //   208	3	6	request	HttpServletRequest
    //   252	166	6	tRun	TestRunOutputBean
    //   504	30	6	e	InvalidDataException
    //   559	19	6	e	Exception
    //   296	349	7	project	Project
    //   311	92	8	auditLogInputBean	AuditLogInputBean
    //   494	3	8	request	HttpServletRequest
    //   549	3	8	request	HttpServletRequest
    //   637	3	8	request	HttpServletRequest
    //   441	3	9	runRestBean	TestRunRestBean
    //   451	13	10	localResponse2	Response
    //   456	3	11	request	HttpServletRequest
    //   647	13	12	localObject1	Object
    //   652	3	13	request	HttpServletRequest
    // Exception table:
    //   from	to	target	type
    //   228	453	504	com/go2group/synapse/core/exception/InvalidDataException
    //   466	491	504	com/go2group/synapse/core/exception/InvalidDataException
    //   228	453	559	java/lang/Exception
    //   466	491	559	java/lang/Exception
    //   25	46	647	finally
    //   59	91	647	finally
    //   104	150	647	finally
    //   163	205	647	finally
    //   218	453	647	finally
    //   466	491	647	finally
    //   504	546	647	finally
    //   559	634	647	finally
    //   647	649	647	finally
  }
  
  /* Error */
  @Path("adhoc/getTestRuns/{testCaseKey}")
  @GET
  public Response getAdhocTestRun(@PathParam("testCaseKey") String testCaseKey)
  {
    // Byte code:
    //   0: getstatic 11	com/go2group/synapse/rest/pub/TestRunPublicREST:log	Lorg/apache/log4j/Logger;
    //   3: new 12	java/lang/StringBuilder
    //   6: dup
    //   7: invokespecial 13	java/lang/StringBuilder:<init>	()V
    //   10: ldc -29
    //   12: invokevirtual 15	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   15: aload_1
    //   16: invokevirtual 15	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   19: invokevirtual 17	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   22: invokevirtual 18	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   25: aload_0
    //   26: invokevirtual 24	com/go2group/synapse/rest/pub/TestRunPublicREST:hasValidLicense	()Z
    //   29: ifne +37 -> 66
    //   32: getstatic 11	com/go2group/synapse/rest/pub/TestRunPublicREST:log	Lorg/apache/log4j/Logger;
    //   35: ldc 25
    //   37: invokevirtual 18	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   40: aload_0
    //   41: aload_0
    //   42: getfield 3	com/go2group/synapse/rest/pub/TestRunPublicREST:i18n	Lcom/atlassian/jira/util/I18nHelper;
    //   45: ldc 26
    //   47: invokeinterface 27 2 0
    //   52: invokevirtual 28	com/go2group/synapse/rest/pub/TestRunPublicREST:forbidden	(Ljava/lang/String;)Ljavax/ws/rs/core/Response;
    //   55: astore_2
    //   56: invokestatic 19	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   59: astore_3
    //   60: aload_3
    //   61: invokestatic 23	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   64: aload_2
    //   65: areturn
    //   66: aload_0
    //   67: getfield 4	com/go2group/synapse/rest/pub/TestRunPublicREST:issueManager	Lcom/atlassian/jira/issue/IssueManager;
    //   70: aload_1
    //   71: invokeinterface 214 2 0
    //   76: astore_2
    //   77: aload_0
    //   78: aload_2
    //   79: invokevirtual 37	com/go2group/synapse/rest/pub/TestRunPublicREST:hasViewPermission	(Lcom/atlassian/jira/issue/Issue;)Z
    //   82: ifne +39 -> 121
    //   85: getstatic 11	com/go2group/synapse/rest/pub/TestRunPublicREST:log	Lorg/apache/log4j/Logger;
    //   88: ldc 38
    //   90: invokevirtual 18	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   93: aload_0
    //   94: aload_0
    //   95: getfield 3	com/go2group/synapse/rest/pub/TestRunPublicREST:i18n	Lcom/atlassian/jira/util/I18nHelper;
    //   98: ldc 39
    //   100: invokeinterface 27 2 0
    //   105: invokevirtual 28	com/go2group/synapse/rest/pub/TestRunPublicREST:forbidden	(Ljava/lang/String;)Ljavax/ws/rs/core/Response;
    //   108: astore_3
    //   109: invokestatic 19	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   112: astore 4
    //   114: aload 4
    //   116: invokestatic 23	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   119: aload_3
    //   120: areturn
    //   121: aload_0
    //   122: getfield 2	com/go2group/synapse/rest/pub/TestRunPublicREST:testRunService	Lcom/go2group/synapse/service/TestRunService;
    //   125: aload_2
    //   126: invokeinterface 54 1 0
    //   131: invokestatic 55	com/atlassian/jira/component/ComponentAccessor:getJiraAuthenticationContext	()Lcom/atlassian/jira/security/JiraAuthenticationContext;
    //   134: invokeinterface 56 1 0
    //   139: invokeinterface 228 3 0
    //   144: astore_3
    //   145: new 58	java/util/ArrayList
    //   148: dup
    //   149: invokespecial 59	java/util/ArrayList:<init>	()V
    //   152: astore 4
    //   154: aload_3
    //   155: ifnull +89 -> 244
    //   158: aload_3
    //   159: invokeinterface 60 1 0
    //   164: ifle +80 -> 244
    //   167: aload_3
    //   168: invokeinterface 61 1 0
    //   173: astore 5
    //   175: aload 5
    //   177: invokeinterface 62 1 0
    //   182: ifeq +62 -> 244
    //   185: aload 5
    //   187: invokeinterface 63 1 0
    //   192: checkcast 64	com/go2group/synapse/bean/TestRunOutputBean
    //   195: astore 6
    //   197: new 42	com/go2group/synapse/rest/pub/TestRunRestBean
    //   200: dup
    //   201: new 41	com/go2group/synapse/bean/TestRunDisplayBean
    //   204: dup
    //   205: aload 6
    //   207: invokespecial 77	com/go2group/synapse/bean/TestRunDisplayBean:<init>	(Lcom/go2group/synapse/bean/TestRunOutputBean;)V
    //   210: aload_0
    //   211: getfield 5	com/go2group/synapse/rest/pub/TestRunPublicREST:dateTimeFormatter	Lcom/atlassian/jira/datetime/DateTimeFormatter;
    //   214: aload_0
    //   215: getfield 6	com/go2group/synapse/rest/pub/TestRunPublicREST:userManager	Lcom/atlassian/jira/user/util/UserManager;
    //   218: aload_0
    //   219: getfield 4	com/go2group/synapse/rest/pub/TestRunPublicREST:issueManager	Lcom/atlassian/jira/issue/IssueManager;
    //   222: aload_0
    //   223: getfield 3	com/go2group/synapse/rest/pub/TestRunPublicREST:i18n	Lcom/atlassian/jira/util/I18nHelper;
    //   226: invokespecial 43	com/go2group/synapse/rest/pub/TestRunRestBean:<init>	(Lcom/go2group/synapse/bean/TestRunDisplayBean;Lcom/atlassian/jira/datetime/DateTimeFormatter;Lcom/atlassian/jira/user/util/UserManager;Lcom/atlassian/jira/issue/IssueManager;Lcom/atlassian/jira/util/I18nHelper;)V
    //   229: astore 7
    //   231: aload 4
    //   233: aload 7
    //   235: invokeinterface 65 2 0
    //   240: pop
    //   241: goto -66 -> 175
    //   244: aload 4
    //   246: invokestatic 44	javax/ws/rs/core/Response:ok	(Ljava/lang/Object;)Ljavax/ws/rs/core/Response$ResponseBuilder;
    //   249: invokevirtual 45	javax/ws/rs/core/Response$ResponseBuilder:build	()Ljavax/ws/rs/core/Response;
    //   252: astore 5
    //   254: invokestatic 19	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   257: astore 6
    //   259: aload 6
    //   261: invokestatic 23	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   264: aload 5
    //   266: areturn
    //   267: astore_3
    //   268: getstatic 11	com/go2group/synapse/rest/pub/TestRunPublicREST:log	Lorg/apache/log4j/Logger;
    //   271: aload_3
    //   272: invokevirtual 32	com/go2group/synapse/core/exception/InvalidDataException:getMessage	()Ljava/lang/String;
    //   275: aload_3
    //   276: invokevirtual 50	org/apache/log4j/Logger:debug	(Ljava/lang/Object;Ljava/lang/Throwable;)V
    //   279: getstatic 11	com/go2group/synapse/rest/pub/TestRunPublicREST:log	Lorg/apache/log4j/Logger;
    //   282: aload_3
    //   283: invokevirtual 32	com/go2group/synapse/core/exception/InvalidDataException:getMessage	()Ljava/lang/String;
    //   286: invokevirtual 49	org/apache/log4j/Logger:error	(Ljava/lang/Object;)V
    //   289: invokestatic 185	javax/ws/rs/core/Response:serverError	()Ljavax/ws/rs/core/Response$ResponseBuilder;
    //   292: aload_3
    //   293: invokevirtual 32	com/go2group/synapse/core/exception/InvalidDataException:getMessage	()Ljava/lang/String;
    //   296: invokevirtual 188	javax/ws/rs/core/Response$ResponseBuilder:entity	(Ljava/lang/Object;)Ljavax/ws/rs/core/Response$ResponseBuilder;
    //   299: invokevirtual 45	javax/ws/rs/core/Response$ResponseBuilder:build	()Ljavax/ws/rs/core/Response;
    //   302: astore 4
    //   304: invokestatic 19	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   307: astore 5
    //   309: aload 5
    //   311: invokestatic 23	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   314: aload 4
    //   316: areturn
    //   317: astore_3
    //   318: getstatic 11	com/go2group/synapse/rest/pub/TestRunPublicREST:log	Lorg/apache/log4j/Logger;
    //   321: aload_3
    //   322: invokevirtual 48	java/lang/Exception:getMessage	()Ljava/lang/String;
    //   325: aload_3
    //   326: invokevirtual 50	org/apache/log4j/Logger:debug	(Ljava/lang/Object;Ljava/lang/Throwable;)V
    //   329: getstatic 11	com/go2group/synapse/rest/pub/TestRunPublicREST:log	Lorg/apache/log4j/Logger;
    //   332: aload_3
    //   333: invokevirtual 48	java/lang/Exception:getMessage	()Ljava/lang/String;
    //   336: invokevirtual 49	org/apache/log4j/Logger:error	(Ljava/lang/Object;)V
    //   339: invokestatic 185	javax/ws/rs/core/Response:serverError	()Ljavax/ws/rs/core/Response$ResponseBuilder;
    //   342: new 12	java/lang/StringBuilder
    //   345: dup
    //   346: invokespecial 13	java/lang/StringBuilder:<init>	()V
    //   349: aload_0
    //   350: getfield 3	com/go2group/synapse/rest/pub/TestRunPublicREST:i18n	Lcom/atlassian/jira/util/I18nHelper;
    //   353: ldc -70
    //   355: invokeinterface 27 2 0
    //   360: invokevirtual 15	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   363: aload_0
    //   364: getfield 3	com/go2group/synapse/rest/pub/TestRunPublicREST:i18n	Lcom/atlassian/jira/util/I18nHelper;
    //   367: ldc -30
    //   369: invokeinterface 27 2 0
    //   374: invokevirtual 15	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   377: invokevirtual 17	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   380: invokevirtual 188	javax/ws/rs/core/Response$ResponseBuilder:entity	(Ljava/lang/Object;)Ljavax/ws/rs/core/Response$ResponseBuilder;
    //   383: invokevirtual 45	javax/ws/rs/core/Response$ResponseBuilder:build	()Ljavax/ws/rs/core/Response;
    //   386: astore 4
    //   388: invokestatic 19	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   391: astore 5
    //   393: aload 5
    //   395: invokestatic 23	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   398: aload 4
    //   400: areturn
    //   401: astore 8
    //   403: invokestatic 19	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   406: astore 9
    //   408: aload 9
    //   410: invokestatic 23	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   413: aload 8
    //   415: athrow
    // Line number table:
    //   Java source line #901	-> byte code offset #0
    //   Java source line #904	-> byte code offset #25
    //   Java source line #905	-> byte code offset #32
    //   Java source line #906	-> byte code offset #40
    //   Java source line #943	-> byte code offset #56
    //   Java source line #944	-> byte code offset #60
    //   Java source line #906	-> byte code offset #64
    //   Java source line #909	-> byte code offset #66
    //   Java source line #911	-> byte code offset #77
    //   Java source line #912	-> byte code offset #85
    //   Java source line #913	-> byte code offset #93
    //   Java source line #943	-> byte code offset #109
    //   Java source line #944	-> byte code offset #114
    //   Java source line #913	-> byte code offset #119
    //   Java source line #921	-> byte code offset #121
    //   Java source line #922	-> byte code offset #145
    //   Java source line #923	-> byte code offset #154
    //   Java source line #924	-> byte code offset #167
    //   Java source line #925	-> byte code offset #197
    //   Java source line #926	-> byte code offset #231
    //   Java source line #927	-> byte code offset #241
    //   Java source line #929	-> byte code offset #244
    //   Java source line #943	-> byte code offset #254
    //   Java source line #944	-> byte code offset #259
    //   Java source line #929	-> byte code offset #264
    //   Java source line #930	-> byte code offset #267
    //   Java source line #931	-> byte code offset #268
    //   Java source line #932	-> byte code offset #279
    //   Java source line #933	-> byte code offset #289
    //   Java source line #943	-> byte code offset #304
    //   Java source line #944	-> byte code offset #309
    //   Java source line #933	-> byte code offset #314
    //   Java source line #934	-> byte code offset #317
    //   Java source line #935	-> byte code offset #318
    //   Java source line #936	-> byte code offset #329
    //   Java source line #938	-> byte code offset #339
    //   Java source line #939	-> byte code offset #355
    //   Java source line #940	-> byte code offset #383
    //   Java source line #943	-> byte code offset #388
    //   Java source line #944	-> byte code offset #393
    //   Java source line #937	-> byte code offset #398
    //   Java source line #943	-> byte code offset #401
    //   Java source line #944	-> byte code offset #408
    //   Java source line #945	-> byte code offset #413
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	416	0	this	TestRunPublicREST
    //   0	416	1	testCaseKey	String
    //   55	10	2	localResponse	Response
    //   76	50	2	testcaseIssue	Object
    //   59	61	3	request	HttpServletRequest
    //   144	24	3	tRuns	List<TestRunOutputBean>
    //   267	26	3	e	InvalidDataException
    //   317	16	3	e	Exception
    //   112	3	4	request	HttpServletRequest
    //   152	247	4	respRuns	List<TestRunRestBean>
    //   173	92	5	localObject1	Object
    //   307	3	5	request	HttpServletRequest
    //   391	3	5	request	HttpServletRequest
    //   195	11	6	tRun	TestRunOutputBean
    //   257	3	6	request	HttpServletRequest
    //   229	5	7	runRestBean	TestRunRestBean
    //   401	13	8	localObject2	Object
    //   406	3	9	request	HttpServletRequest
    // Exception table:
    //   from	to	target	type
    //   121	254	267	com/go2group/synapse/core/exception/InvalidDataException
    //   121	254	317	java/lang/Exception
    //   25	56	401	finally
    //   66	109	401	finally
    //   121	254	401	finally
    //   267	304	401	finally
    //   317	388	401	finally
    //   401	403	401	finally
  }
  
  @Path("createRunBug")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @POST
  @XsrfProtectionExcluded
  public Response createRunBug(String jsonIssueString, @Context HttpHeaders headers)
  {
    if (!hasValidLicense()) {
      log.debug("Invalid license");
      return forbidden(i18n.getText("servererror.rest.invalid.license"));
    }
    
    JSONParser parser = new JSONParser();
    Integer runId = null;
    String key = null;
    Object obj = null;
    
    try
    {
      HttpServletRequest req = ExecutingHttpRequest.get();
      boolean canProceed = DefaultRestRateLimiter.canRequestProceed(req);
      HttpServletRequest request; if (!canProceed) {
        return rateLimitExceeded("Too many Requests");
      }
      HttpServletRequest request;
      try {
        obj = parser.parse(jsonIssueString);
      } catch (Exception e) {
        log.warn("Invalid TestRun Id received :" + runId);
        return error(e);
      }
      
      log.debug("jsonIssueString : " + jsonIssueString);
      org.json.simple.JSONObject jsonObject = (org.json.simple.JSONObject)obj;
      try
      {
        Long runIdJSON = (Long)jsonObject.get("runId");
        runId = Integer.valueOf(runIdJSON.toString());
      } catch (NumberFormatException e) {
        log.warn("Invalid TestRun Id received :" + jsonObject.get("runId"));
        HttpServletRequest request; return error(i18n.getText(i18n.getText("errormessage.testrun.validation.invalid.runid", jsonObject.get("runId"))));
      } catch (ClassCastException e) {
        log.warn("Invalid TestRun Id received :" + jsonObject.get("runId"));
        HttpServletRequest request; return error(i18n.getText(i18n.getText("errormessage.testrun.validation.invalid.runid", jsonObject.get("runId"))));
      }
      jsonObject.remove("runId");
      HttpServletRequest request1 = ExecutingHttpRequest.get();
      String strippedJson = jsonObject.toJSONString();
      org.json.simple.JSONObject toJira = (org.json.simple.JSONObject)parser.parse(strippedJson);
      HttpClient client = HttpClientBuilder.create().build();
      
      String baseUrl = ComponentAccessor.getApplicationProperties().getString("jira.baseurl");
      if ((baseUrl != null) && (baseUrl.endsWith("/"))) {
        baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
      }
      String url = baseUrl + "/rest/api/2/issue/";
      log.debug("url : " + url);
      HttpPost request = new HttpPost(url);
      request.setConfig(getRequestConfig());
      
      request.addHeader("Content-Type", "application/json;charset=UTF-8");
      
      request.addHeader("Authorization", request1.getHeader("Authorization"));
      
      request.setEntity(new StringEntity(toJira.toJSONString(), "UTF-8"));
      HttpResponse response = client.execute(request);
      log.debug("response : " + response.getStatusLine().getStatusCode());
      
      org.json.simple.JSONObject responseObject = (org.json.simple.JSONObject)parser.parse(getJSONFromResponse(response));
      if ((response.getStatusLine().getStatusCode() != 200) && (response.getStatusLine().getStatusCode() != 201)) { HttpServletRequest request;
        return Response.ok(responseObject).build();
      }
      
      key = (String)responseObject.get("key");
      log.debug("key : " + key);
    } catch (IOException e) {
      HttpServletRequest request;
      log.debug(e.getMessage(), e);
    } catch (Exception e) { HttpServletRequest request;
      log.debug(e.getMessage(), e);
    } finally { HttpServletRequest request;
      HttpServletRequest request = ExecutingHttpRequest.get();
      DefaultRestRateLimiter.endCall(request);
    }
    
    Issue bugIssue = issueManager.getIssueByCurrentKey(key);
    
    if (!hasSynapsePermission(bugIssue.getProjectObject(), SynapsePermission.EXECUTE_TESTRUNS)) {
      log.debug("User does not have permission to execute Test runs");
      return forbidden(i18n.getText("servererror.rest.no.execute.testrun.permission"));
    }
    
    try
    {
      testRunService.addTestRunBug(runId, bugIssue.getId(), null);
      IssueWrapperBean issueWrapperBean = new IssueWrapperBean();
      issueWrapperBean.setId(bugIssue.getId());
      issueWrapperBean.setKey(bugIssue.getKey());
      
      return Response.ok(issueWrapperBean).build();
    } catch (InvalidDataException e) {
      log.error(e.getMessage());
      log.debug(e.getMessage(), e);
      return error(e);
    }
  }
  


  @Path("createRunStepBug")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @POST
  @XsrfProtectionExcluded
  public Response createRunStepBug(String jsonIssueString, @Context HttpHeaders headers)
  {
    if (!hasValidLicense()) {
      log.debug("Invalid license");
      return forbidden(i18n.getText("servererror.rest.invalid.license"));
    }
    
    JSONParser parser = new JSONParser();
    Integer runStepId = null;
    String key = null;
    try
    {
      HttpServletRequest req = ExecutingHttpRequest.get();
      boolean canProceed = DefaultRestRateLimiter.canRequestProceed(req);
      if (!canProceed) { HttpServletRequest request;
        return rateLimitExceeded("Too many Requests");
      }
      
      Object obj = null;
      HttpServletRequest request;
      try {
        obj = parser.parse(jsonIssueString);
      } catch (Exception e) {
        log.warn("Invalid TestRun Id received :" + runStepId);
        return error(e);
      }
      org.json.simple.JSONObject jsonObject = (org.json.simple.JSONObject)obj;
      try {
        Long runStepIdJSON = (Long)jsonObject.get("runStepId");
        runStepId = Integer.valueOf(runStepIdJSON.toString());
      } catch (NumberFormatException e) {
        log.warn("Invalid TestRun Id received :" + runStepId);
        HttpServletRequest request; return error(i18n.getText(i18n.getText("errormessage.testrun.validation.invalid.runstepid", jsonObject.get("runStepId"))));
      } catch (ClassCastException e) {
        log.warn("Invalid TestRun Id received :" + runStepId);
        HttpServletRequest request; return error(i18n.getText(i18n.getText("errormessage.testrun.validation.invalid.runstepid", jsonObject.get("runStepId"))));
      }
      jsonObject.remove("runStepId");
      HttpServletRequest request1 = ExecutingHttpRequest.get();
      String strippedJson = jsonObject.toJSONString();
      
      org.json.simple.JSONObject toJira = (org.json.simple.JSONObject)parser.parse(strippedJson);
      HttpClient client = HttpClientBuilder.create().build();
      
      String baseUrl = ComponentAccessor.getApplicationProperties().getString("jira.baseurl");
      if ((baseUrl != null) && (baseUrl.endsWith("/"))) {
        baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
      }
      String url = baseUrl + "/rest/api/2/issue/";
      log.debug("url : " + url);
      HttpPost request = new HttpPost(url);
      request.setConfig(getRequestConfig());
      
      request.addHeader("Content-Type", "application/json;charset=UTF-8");
      
      request.addHeader("Authorization", request1.getHeader("Authorization"));
      
      request.setEntity(new StringEntity(toJira.toJSONString(), "UTF-8"));
      HttpResponse response = client.execute(request);
      log.debug("response : " + response.getStatusLine().getStatusCode());
      
      org.json.simple.JSONObject responseObject = (org.json.simple.JSONObject)parser.parse(getJSONFromResponse(response));
      if ((response.getStatusLine().getStatusCode() != 200) && (response.getStatusLine().getStatusCode() != 201)) { HttpServletRequest request;
        return Response.ok(responseObject).build();
      }
      
      key = (String)responseObject.get("key");
      log.debug("key : " + key);
    } catch (IOException e) {
      HttpServletRequest request;
      log.debug(e.getMessage(), e);
    } catch (Exception e) { HttpServletRequest request;
      log.debug(e.getMessage(), e);
    } finally { HttpServletRequest request;
      HttpServletRequest request = ExecutingHttpRequest.get();
      DefaultRestRateLimiter.endCall(request);
    }
    
    Issue bugIssue = issueManager.getIssueByCurrentKey(key);
    
    if (!hasSynapsePermission(bugIssue.getProjectObject(), SynapsePermission.EXECUTE_TESTRUNS)) {
      log.debug("User does not have permission to execute Test runs");
      return forbidden(i18n.getText("servererror.rest.no.execute.testrun.permission"));
    }
    
    try
    {
      TestRunStepOutputBean testRunStep = testRunService.getTestRunStep(runStepId);
      testRunService.addTestRunStepBug(runStepId, bugIssue.getId(), testRunStep.getTestRunIteration().getName());
      IssueWrapperBean issueWrapperBean = new IssueWrapperBean();
      issueWrapperBean.setId(bugIssue.getId());
      issueWrapperBean.setKey(bugIssue.getKey());
      
      return Response.ok(issueWrapperBean).build();
    } catch (InvalidDataException e) {
      log.error(e.getMessage());
      log.debug(e.getMessage(), e);
      return error(e);
    }
  }
  
  @Path("getTestRunsForDefect/{defectKey}")
  @GET
  public Response getTestRunsForDefect(@PathParam("defectKey") String defectKey)
  {
    log.debug("Get test runs for defects :" + defectKey);
    

    if (!hasValidLicense()) {
      log.debug("Invalid license");
      return forbidden(i18n.getText("servererror.rest.invalid.license"));
    }
    
    Issue bugIssue = issueManager.getIssueObject(defectKey);
    
    if (!hasViewPermission(bugIssue)) {
      log.debug("Does not have view permission on the issue");
      return forbidden(i18n.getText("servererror.rest.no.view.permission"));
    }
    



    try
    {
      HttpServletRequest request = ExecutingHttpRequest.get();
      canProceed = DefaultRestRateLimiter.canRequestProceed(request);
      if (!canProceed) { HttpServletRequest request;
        return rateLimitExceeded("Too many Requests");
      }
      
      Object tRuns = testRunService.getTestRunsByBug(bugIssue.getId());
      List<TestRunRestBean> respRuns = new ArrayList();
      Object localObject1; if ((tRuns != null) && (((Collection)tRuns).size() > 0))
        for (localObject1 = ((Collection)tRuns).iterator(); ((Iterator)localObject1).hasNext();) { TestRunOutputBean tRun = (TestRunOutputBean)((Iterator)localObject1).next();
          TestRunRestBean runRestBean = new TestRunRestBean(new TestRunDisplayBean(tRun), dateTimeFormatter, userManager, issueManager, i18n);
          respRuns.add(runRestBean);
        }
      HttpServletRequest request;
      return Response.ok(respRuns).build();
    } catch (InvalidDataException e) {
      log.debug(e.getMessage(), e);
      log.error(e.getMessage());
      HttpServletRequest request; return Response.serverError().entity(e.getMessage()).build();
    } catch (Exception e) { boolean canProceed;
      log.debug(e.getMessage(), e);
      log.error(e.getMessage());
      HttpServletRequest request; return 
      

        Response.serverError().entity(i18n.getText("errormessage.unexpected.server.error") + i18n.getText("servererror.rest.testrun.status.retrieve.failed")).build();
    } finally {
      HttpServletRequest request = ExecutingHttpRequest.get();
      DefaultRestRateLimiter.endCall(request);
    }
  }
  
  public boolean hasManagePermission(Issue tpIssue) {
    return permissionUtil.hasSynapsePermission(tpIssue.getProjectObject(), SynapsePermission.MANAGE_TESTPLANS);
  }
  
  public boolean isReadOnly(Issue tpIssue) {
    return (!PluginUtil.hasValidLicense()) || 
      (isTestPlanResolved(tpIssue)) || 
      (!permissionUtil.hasEditPermission(tpIssue));
  }
  
  private RequestConfig getRequestConfig() {
    int CONNECTION_TIMEOUT_MS = 60000;
    return RequestConfig.custom()
      .setConnectionRequestTimeout(CONNECTION_TIMEOUT_MS)
      .setConnectTimeout(CONNECTION_TIMEOUT_MS)
      .setSocketTimeout(CONNECTION_TIMEOUT_MS)
      .build();
  }
  
  protected String getJSONFromResponse(HttpResponse response) throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
    StringBuffer buff = new StringBuffer();
    for (;;) {
      String line = reader.readLine();
      if (line == null) {
        break;
      }
      buff.append(line);
    }
    
    return buff.toString();
  }
  
  private boolean isTestPlanResolved(Issue tpIssue)
  {
    log.debug("Checking if test plan is Resolved for issue :" + tpIssue);
    return tpIssue.getResolution() != null;
  }
  
  private boolean isCycleReadOnly(TestCycleOutputBean tCycleBean) {
    log.debug("Checking if the test cycle is readonly:" + tCycleBean);
    return ("Completed".equals(tCycleBean.getStatus())) || 
      ("Aborted".equals(tCycleBean.getStatus())) || 
      ("Draft".equals(tCycleBean.getStatus()));
  }
  
  private boolean isCycleReadOnlyForReorder(TestCycleOutputBean tCycleBean) {
    log.debug("Checking if the test cycle is readonly:" + tCycleBean);
    return ("Completed".equals(tCycleBean.getStatus())) || 
      ("Aborted".equals(tCycleBean.getStatus()));
  }
  
  @POST
  @Path("createTestRuns")
  @XsrfProtectionExcluded
  public Response createTestRuns(String data)
  {
    try {
      HttpServletRequest request = ExecutingHttpRequest.get();
      canProceed = DefaultRestRateLimiter.canRequestProceed(request);
      Response localResponse; if (!canProceed) { HttpServletRequest request;
        return rateLimitExceeded("Too many Requests");
      }
      
      if (!hasValidLicense()) {
        log.debug("Invalid license");
        HttpServletRequest request; return forbidden(i18n.getText("servererror.rest.invalid.license"));
      }
      Object jsonObject;
      if (StringUtils.isNotBlank(data)) {
        jsonObject = new com.atlassian.jira.util.json.JSONObject(data);
        String testCaseKeys = ((com.atlassian.jira.util.json.JSONObject)jsonObject).has("testCaseKeys") ? ((com.atlassian.jira.util.json.JSONObject)jsonObject).getString("testCaseKeys") : null;
        String cycleId = ((com.atlassian.jira.util.json.JSONObject)jsonObject).has("cycleId") ? ((com.atlassian.jira.util.json.JSONObject)jsonObject).getString("cycleId") : null;
        JSONArray attributeArray = ((com.atlassian.jira.util.json.JSONObject)jsonObject).has("attributeValues") ? ((com.atlassian.jira.util.json.JSONObject)jsonObject).getJSONArray("attributeValues") : null;
        String urgencyValue = ((com.atlassian.jira.util.json.JSONObject)jsonObject).has("urgencyValue") ? ((com.atlassian.jira.util.json.JSONObject)jsonObject).getString("urgencyValue") : null;
        String assignValue = ((com.atlassian.jira.util.json.JSONObject)jsonObject).has("assignValue") ? ((com.atlassian.jira.util.json.JSONObject)jsonObject).getString("assignValue") : null;
        String progressKey = ((com.atlassian.jira.util.json.JSONObject)jsonObject).has("progressKey") ? ((com.atlassian.jira.util.json.JSONObject)jsonObject).getString("progressKey") : null;
        
        String[] urgencyValues = null;
        String[] testCases = null;
        String[] assignValues = null;
        if ((testCaseKeys == null) || (testCaseKeys.isEmpty())) {
          throw new InvalidDataException("TestCaseId is Mandatory");
        }
        testCases = testCaseKeys.split(",");
        
        if ((cycleId == null) || (cycleId.isEmpty())) {
          throw new InvalidDataException("Cycle Id is Mandatory");
        }
        if ((attributeArray == null) || (attributeArray.length() == 0)) {
          throw new InvalidDataException("Please specify attribute values");
        }
        
        if (assignValue != null) {
          assignValues = assignValue.split(",");
        }
        if (urgencyValue != null) {
          urgencyValues = urgencyValue.split(",");
        }
        
        Map<String, List<Integer>> runAttributeCombination = new HashMap();
        Map<String, String> assignValueList = new HashMap();
        Map<String, String> urgencyValueList = new HashMap();
        

        for (int index = 0; index < attributeArray.length(); index++) {
          com.atlassian.jira.util.json.JSONObject attributes = attributeArray.getJSONObject(index);
          String runAttributeOne = null;
          String runAttributeTwo = null;
          Iterator<String> keys = attributes.keys();
          int count = 1;
          while (keys.hasNext()) {
            String key = (String)keys.next();
            if (count == 1) {
              runAttributeOne = attributes.getString(key);
            } else {
              runAttributeTwo = attributes.getString(key);
            }
            count++;
          }
          
          List<Integer> attributeValues = new ArrayList();
          Integer attributeOneValueId = null;
          Integer attributetwoValueId = null;
          if (!runAttributeOne.equals("-")) {
            RunAttributeValueOutputBean runAttributeValue = runAttributeService.getAttributeValue(runAttributeOne);
            attributeValues.add(Integer.valueOf(runAttributeValue.getId().intValue()));
            attributeOneValueId = Integer.valueOf(runAttributeValue.getId().intValue());
          }
          if (!runAttributeTwo.equals("-")) {
            RunAttributeValueOutputBean runAttributeValue = runAttributeService.getAttributeValue(runAttributeTwo);
            attributeValues.add(Integer.valueOf(runAttributeValue.getId().intValue()));
            attributetwoValueId = Integer.valueOf(runAttributeValue.getId().intValue());
          }
          

          runAttributeCombination.put(String.valueOf(index + 1), attributeValues);
          if (assignValues != null) {
            if (!assignValues[0].isEmpty()) {
              if (!assignValues[index].equals("null")) {
                assignValueList.put(String.valueOf(index + 1), assignValues[index].trim());
              } else {
                assignValueList.put(String.valueOf(index + 1), i18n.getText("synapse.common.label.unassigned"));
              }
            } else {
              assignValueList.put(String.valueOf(index + 1), i18n.getText("synapse.common.label.unassigned"));
            }
          } else {
            assignValueList.put(String.valueOf(index + 1), i18n.getText("synapse.common.label.unassigned"));
          }
          if (urgencyValues != null) {
            if (!urgencyValues[0].isEmpty()) {
              urgencyValueList.put(String.valueOf(index + 1), urgencyValues[index].trim());
            } else {
              urgencyValueList.put(String.valueOf(index + 1), "");
            }
          } else {
            urgencyValueList.put(String.valueOf(index + 1), "");
          }
          
          runAttributeService.addRunAttributeCombination(Integer.valueOf(cycleId), attributeOneValueId, attributetwoValueId);
        }
        

        TestCycleOutputBean testCycleOutputBean = testCycleService.getCycle(Integer.valueOf(cycleId));
        Issue testPlan = issueManager.getIssueObject(testCycleOutputBean.getTpId());
        List<TestPlanMemberOutputBean> tpMembers = new ArrayList();
        ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
        int index;
        if (testCases != null) {
          Set<Long> testCaseIdList = new HashSet();
          if ((testCases != null) && (testCases.length > 0)) {
            for (index = 0; index < testCases.length; index++) {
              Issue testCaseIssue = issueManager.getIssueByCurrentKey(testCases[index].trim());
              if (testCaseIssue != null) {
                testCaseIdList.add(testCaseIssue.getId());
              } else {
                testCaseIdList.add(Long.valueOf(testCases[index].trim()));
              }
            }
          }
          for (Long tcId : testCaseIdList) {
            TestPlanMemberOutputBean testPlanMember = testPlanMemberService.getTestPlanMember(testPlan.getId(), tcId);
            tpMembers.add(testPlanMember);
          }
        }
        
        testCycleService.createStandardBulkTestRun(progressKey, tpMembers, Integer.valueOf(cycleId), user, runAttributeCombination, urgencyValueList, assignValueList); }
      HttpServletRequest request;
      return success();
    } catch (Exception e) {
      boolean canProceed;
      log.error(e.getMessage());
      log.debug(e.getMessage(), e);
      HttpServletRequest request; return error(e);
    } finally {
      HttpServletRequest request = ExecutingHttpRequest.get();
      DefaultRestRateLimiter.endCall(request);
    }
  }
  
  @GET
  @Path("getTestRunsCreated")
  @XsrfProtectionExcluded
  public Response getTestRunsCreated(@QueryParam("testPlanIdOrKey") String testPlanIdOrKey, @QueryParam("cutOffDateAndTime") String cutOffDateAndTime) { log.debug("getTestRunsCreated - testPlanIdOrKey:" + testPlanIdOrKey + "; cutOffDateAndTime:" + cutOffDateAndTime);
    List<TestRunOutputBean> testRuns = null;
    try {
      SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
      if (StringUtils.isNotBlank(cutOffDateAndTime)) {
        Timestamp dateAndTime = null;
        if (cutOffDateAndTime.length() == 10) {
          dateAndTime = new Timestamp(sdf2.parse(cutOffDateAndTime).getTime());
          
          Calendar cal = Calendar.getInstance();
          cal.setTimeInMillis(dateAndTime.getTime());
          cal.set(11, 0);
          cal.set(12, 0);
          cal.set(13, 0);
          dateAndTime = new Timestamp(cal.getTime().getTime());
        } else if (cutOffDateAndTime.length() == 19) {
          dateAndTime = new Timestamp(sdf1.parse(cutOffDateAndTime).getTime());
        }
        if (dateAndTime != null) {
          log.debug("getTestRunsCreated - Date and time:" + sdf1.format(Long.valueOf(dateAndTime.getTime())));
          testRuns = testRunService.getTestRunsCreatedAfter(dateAndTime, testPlanIdOrKey);
          if (testRuns != null) {
            log.debug("getTestRunsCreated - testRuns count:" + testRuns.size());
          }
          return Response.ok().entity(testRuns).build();
        }
      }
    } catch (Exception e) {
      log.debug(e.getMessage(), e);
      log.error(e.getMessage());
      return Response.serverError().entity(e.getMessage()).build();
    }
    
    return Response.serverError().entity("Failed").build();
  }
}
