package com.go2group.synapse.rest;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.option.LazyLoadedOption;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldException;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.label.Label;
import com.atlassian.jira.issue.resolution.Resolution;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.permission.GlobalPermissionKey;
import com.atlassian.jira.permission.ProjectPermissions;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.json.JSONArray;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.go2group.synapse.bean.ChartJSDataSet;
import com.go2group.synapse.bean.ChartJSPieDataSet;
import com.go2group.synapse.bean.CycleRunStatusStatBean;
import com.go2group.synapse.bean.ExecutionBurndownData;
import com.go2group.synapse.bean.FieldExecutionStatisticsBean;
import com.go2group.synapse.bean.HtmlResponseWrapper;
import com.go2group.synapse.bean.IssueWrapperBean;
import com.go2group.synapse.bean.RESTErrorMessage;
import com.go2group.synapse.bean.RunStatusOutputBean;
import com.go2group.synapse.bean.StatItemBean;
import com.go2group.synapse.bean.TestCycleOutputBean;
import com.go2group.synapse.bean.TestPlanGadgetBean;
import com.go2group.synapse.bean.TestRunHistoryOutputBean;
import com.go2group.synapse.bean.TestRunOutputBean;
import com.go2group.synapse.bean.TesterExecutionStatisticsBean;
import com.go2group.synapse.bean.runattribute.RunAttributeValueOutputBean;
import com.go2group.synapse.bean.status.category.StatusCategoryOutputBean;
import com.go2group.synapse.constant.SynapseCustomField;
import com.go2group.synapse.constant.SynapsePermission;
import com.go2group.synapse.core.enums.CustomJQLFieldEnum;
import com.go2group.synapse.core.enums.UrgencyFieldEnum;
import com.go2group.synapse.core.exception.InvalidDataException;
import com.go2group.synapse.core.util.PermissionUtil;
import com.go2group.synapse.enums.StatusCategoryEnum;
import com.go2group.synapse.enums.TestRunStatusEnum;
import com.go2group.synapse.helper.DefectStatisticsVsTestCycleGadgetHelper;
import com.go2group.synapse.helper.TestCaseExecutionDuringPeriodHelper;
import com.go2group.synapse.helper.TestCaseExecutionToCycleStatisticsHelper;
import com.go2group.synapse.helper.TestCaseExecutionToFieldStatisticsHelper;
import com.go2group.synapse.helper.TestCaseExecutionToTesterStatisticsHelper;
import com.go2group.synapse.helper.TestRunAssignedToMeGadgetHelper;
import com.go2group.synapse.service.GadgetService;
import com.go2group.synapse.service.RunStatusService;
import com.go2group.synapse.service.TestCycleService;
import com.go2group.synapse.service.TestRunService;
import com.go2group.synapse.service.TestSuiteService;
import com.go2group.synapse.service.runattribute.RunAttributeService;
import com.go2group.synapse.util.PluginUtil;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

@Path("synapseGadget")
@javax.ws.rs.Consumes({"application/json"})
@Produces({"application/json"})
public class GadgetREST
{
  private final Logger log = Logger.getLogger(GadgetREST.class);
  
  private final GadgetService gadgetService;
  
  private final TestCycleService testCycleService;
  
  private final IssueManager issueManager;
  private final I18nHelper i18n;
  private final TestRunService testRunService;
  private final TestSuiteService testSuiteService;
  private final JiraAuthenticationContext jiraAuthenticationContext;
  private final PermissionManager permissionManager;
  private final PermissionUtil permissionUtil;
  private final GlobalPermissionManager globalPermissionManager;
  private final RunAttributeService runAttributeService;
  private final RunStatusService runStatusService;
  private final UserManager userManager;
  
  @Autowired
  public GadgetREST(@ComponentImport IssueManager issueManager, @ComponentImport I18nHelper i18n, @ComponentImport JiraAuthenticationContext jiraAuthenticationContext, @ComponentImport PermissionManager permissionManager, @ComponentImport GlobalPermissionManager globalPermissionManager, @ComponentImport UserManager userManager, PermissionUtil permissionUtil, GadgetService gadgetService, TestCycleService testCycleService, TestRunService testRunService, TestSuiteService testSuiteService, RunAttributeService runAttributeService, RunStatusService runStatusService)
  {
    this.permissionUtil = permissionUtil;
    this.gadgetService = gadgetService;
    this.testCycleService = testCycleService;
    this.issueManager = issueManager;
    this.i18n = i18n;
    this.testRunService = testRunService;
    this.testSuiteService = testSuiteService;
    this.jiraAuthenticationContext = jiraAuthenticationContext;
    this.permissionManager = permissionManager;
    this.globalPermissionManager = globalPermissionManager;
    this.runAttributeService = runAttributeService;
    this.runStatusService = runStatusService;
    this.userManager = userManager;
    log.debug("synapseRT gadgets initialized");
  }
  
  @GET
  @Path("getTestPlans")
  public Response getTestPlans(@QueryParam("projectKey") String projectKey) {
    log.debug("Fetch Test Plans for the Gadget.");
    List<TestPlanGadgetBean> allTestPlans = gadgetService.getTestPlans(projectKey);
    return Response.ok(allTestPlans).build();
  }
  
  @GET
  @Path("getTestCycles")
  public Response getTestCycles(@QueryParam("testPlanKey") String testPlan) {
    log.debug("Fetch Test Cycles for the Gadget.");
    
    Issue issue = null;
    if (StringUtils.isNumeric(testPlan)) {
      issue = issueManager.getIssueObject(Long.valueOf(testPlan));
    } else {
      issue = issueManager.getIssueByCurrentKey(testPlan);
    }
    try {
      if (issue == null) {
        throw new InvalidDataException(i18n.getText("errormessage.validation.invalid.issuekey", testPlan));
      }
      List<TestCycleOutputBean> allTestCycles = testCycleService.getCycles(issue.getId());
      

      List<TestCycleOutputBean> filteredCycles = new ArrayList();
      for (TestCycleOutputBean cycle : allTestCycles) {
        if ((!"Draft".equals(cycle.getStatus())) && (!"Aborted".equals(cycle.getStatus()))) {
          filteredCycles.add(cycle);
        }
      }
      
      return Response.ok(filteredCycles).build();
    }
    catch (InvalidDataException e) {
      log.debug(e.getMessage(), e);
      log.error(e.getMessage());
      return Response.serverError().entity(e.getMessage()).build();
    }
  }
  

  @GET
  @Path("getTestCycleDetails")
  public Response getTestCycleDetails(@QueryParam("testCycleId") String testCycleId)
  {
    CycleRunStatusStatBean runStat = null;
    try {
      TestCycleOutputBean cycleOutputBean = testCycleService.getCycle(Integer.valueOf(testCycleId));
      Issue testPlan = issueManager.getIssueObject(cycleOutputBean.getTpId());
      Project project = testPlan.getProjectObject();
      boolean hasPermission = (hasViewPermission(project)) && (hasSynapseBrowsePermission(project));
      if (hasPermission) {
        runStat = testCycleService.getRunStat(Integer.valueOf(testCycleId));
      } else {
        return Response.status(Response.Status.FORBIDDEN).entity(new RESTErrorMessage(i18n.getText("synapse.gadget.error.browse.permission.project"))).build();
      }
    } catch (InvalidDataException e) {
      log.debug(e.getMessage(), e);
      log.error(e.getMessage());
      return Response.serverError().entity(e.getMessage()).build();
    } catch (NumberFormatException e) {
      log.debug(e.getMessage(), e);
      log.error(e.getMessage());
      return Response.serverError().entity(e.getMessage()).build();
    }
    return Response.ok(runStat).build();
  }
  
  @GET
  @Path("getTestCaseBurndownData")
  public Response getTestCaseBurndownData(@QueryParam("testCycleId") String testCycleId) {
    log.debug("Retrieving chart data for testcase burndown gadget for cycle :" + testCycleId);
    
    if ((testCycleId != null) && (testCycleId.length() > 0)) {
      try
      {
        TestCycleOutputBean cycleOutputBean = null;
        try {
          cycleOutputBean = testCycleService.getCycle(Integer.valueOf(testCycleId));
        } catch (InvalidDataException err) {
          log.debug(err.getMessage(), err);
          log.error(err.getMessage());
          return Response.status(Response.Status.GONE).entity(i18n.getText("synapse.gadget.error.issue.deleted")).build();
        }
        Issue testPlan = issueManager.getIssueObject(cycleOutputBean.getTpId());
        testPlan = PluginUtil.getValidatedIssue(testPlan, ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser());
        if (testPlan == null) {
          return Response.status(Response.Status.FORBIDDEN).entity(new RESTErrorMessage(i18n.getText("synapse.gadget.error.issue.permission"))).build();
        }
        Project project = testPlan.getProjectObject();
        boolean hasPermission = (hasViewPermission(project)) && (hasSynapseBrowsePermission(project));
        if (hasPermission)
        {
          ExecutionBurndownData chartData = gadgetService.getTestCaseBurndownData(Integer.valueOf(Integer.parseInt(testCycleId)), null);
          return Response.ok(chartData).build();
        }
        
        return Response.status(Response.Status.FORBIDDEN).entity(new RESTErrorMessage(i18n.getText("synapse.gadget.error.browse.permission.project"))).build();
      }
      catch (NumberFormatException e) {
        log.debug(e.getMessage());
        return Response.serverError().entity(e.getMessage()).build();
      }
      catch (InvalidDataException e) {
        log.debug(e.getMessage());
        return Response.serverError().entity(e.getMessage()).build();
      }
    }
    
    log.debug("Invalid testCycleId received");
    return Response.serverError().entity(i18n.getText("errormessage.testcycle.validation.no.cycleid")).build();
  }
  
  @GET
  @Path("getTestCycleGadgetData")
  public Response getTestCycleGadgetData(@QueryParam("cycleId") String cycleId)
  {
    log.debug("Retrieve execution chart data for Cycle with id " + cycleId);
    
    try
    {
      TestCycleOutputBean cycleOutputBean = testCycleService.getCycle(Integer.valueOf(cycleId));
      Issue testPlan = issueManager.getIssueObject(cycleOutputBean.getTpId());
      testPlan = PluginUtil.getValidatedIssue(testPlan, ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser());
      if (testPlan == null) {
        return Response.status(Response.Status.FORBIDDEN).entity(new RESTErrorMessage(i18n.getText("synapse.gadget.error.issue.permission"))).build();
      }
      Project project = testPlan.getProjectObject();
      boolean hasPermission = (hasViewPermission(project)) && (hasSynapseBrowsePermission(project));
      if (hasPermission)
      {
        CycleRunStatusStatBean runStat = testCycleService.getRunStat(Integer.valueOf(cycleId));
        
        ChartJSPieDataSet chartData = new ChartJSPieDataSet();
        for (TestRunStatusEnum status : TestRunStatusEnum.getActiveStatuses()) {
          StatItemBean statItemBean = (StatItemBean)runStat.getStatusStatistics().get(status.getLocalizedName());
          if (statItemBean != null) {
            chartData.addDataSet(status.getLocalizedName(), Integer.valueOf(statItemBean.getCount()), status.getColor());
          }
        }
        log.debug("Returning chart data :" + chartData);
        
        return Response.ok(chartData).build();
      }
      
      return Response.status(Response.Status.FORBIDDEN).entity(new RESTErrorMessage(i18n.getText("synapse.gadget.error.browse.permission.project"))).build();
    }
    catch (InvalidDataException e) {
      log.debug(e.getMessage(), e);
      log.error(e.getMessage());
      return Response.serverError().entity(e.getMessage()).build();
    }
  }
  
  @GET
  @Path("getTestCycleGadgetConfigData")
  public Response getTestCycleGadgetConfigData(@QueryParam("cycleId") String cycleId, @QueryParam("planId") String planId, @QueryParam("noOfTestCases") Integer noOfTestCases)
  {
    log.debug("Retrieve execution chart data for Cycle with id " + cycleId);
    
    Issue testPlan = null;
    if (StringUtils.isNumeric(planId)) {
      testPlan = issueManager.getIssueObject(Long.valueOf(planId));
    } else {
      testPlan = issueManager.getIssueByCurrentKey(planId);
    }
    try {
      if (testPlan == null) {
        return Response.status(Response.Status.GONE).entity(i18n.getText("synapse.gadget.error.issue.deleted")).build();
      }
      testPlan = PluginUtil.getValidatedIssue(testPlan, ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser());
      if (testPlan == null) {
        return Response.status(Response.Status.FORBIDDEN).entity(new RESTErrorMessage(i18n.getText("synapse.gadget.error.issue.permission"))).build();
      }
      
      Project project = testPlan.getProjectObject();
      boolean hasPermission = (hasViewPermission(project)) && (hasSynapseBrowsePermission(project));
      if (hasPermission)
      {
        JSONObject responseJson = new JSONObject();
        if (StringUtils.isNotBlank(cycleId)) {
          TestCycleOutputBean testPlanBean = testCycleService.getCycle(Integer.valueOf(cycleId));
          responseJson.put("testCycle", testPlanBean.getName());
        } else {
          responseJson.put("testCycle", i18n.getText("synapse.common.label.option.select.all"));
        }
        
        responseJson.put("project", testPlan.getProjectObject().getName());
        responseJson.put("noOfTestCases", noOfTestCases);
        responseJson.put("testPlan", testPlan.getKey() + " - " + StringEscapeUtils.escapeHtml4(testPlan.getSummary()));
        
        JSONObject arrayJson = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        for (TestCycleOutputBean cycleOutputBean : testCycleService.getCycles(testPlan)) {
          JSONObject cycleJson = new JSONObject();
          if ((!cycleOutputBean.isAborted()) && (!cycleOutputBean.isDraft())) {
            cycleJson.put("id", cycleOutputBean.getID());
            cycleJson.put("name", cycleOutputBean.getName());
            jsonArray.put(cycleJson);
          }
        }
        arrayJson.put("cyclebean", jsonArray);
        responseJson.put("allTestCycles", arrayJson);
        return Response.ok(responseJson.toString()).build();
      }
      
      return Response.status(Response.Status.FORBIDDEN).entity(new RESTErrorMessage(i18n.getText("synapse.gadget.error.browse.permission.project"))).build();
    }
    catch (InvalidDataException e)
    {
      log.debug(e.getMessage(), e);
      log.error(e.getMessage());
      return Response.serverError().entity(e.getMessage()).build();
    } catch (JSONException e) {
      log.debug(e.getMessage(), e);
      log.error(e.getMessage());
      return Response.serverError().entity(e.getMessage()).build();
    }
  }
  
  @GET
  @Path("getDefectStatisticsVsTestCycleGadgetConfigData")
  public Response getDefectStatisticsVsTestCycleGadgetConfigData(@QueryParam("cycleId") String cycleId) {
    log.debug("Retrieve defect statistics for Cycle with id " + cycleId);
    try {
      TestCycleOutputBean testCycleBean = null;
      try {
        testCycleBean = testCycleService.getCycle(Integer.valueOf(cycleId));
      } catch (InvalidDataException err) {
        log.debug(err.getMessage(), err);
        log.error(err.getMessage());
        return Response.status(Response.Status.GONE).entity(i18n.getText("synapse.gadget.error.issue.deleted")).build();
      }
      Issue testPlan = issueManager.getIssueObject(testCycleBean.getTpId());
      Project project = testPlan.getProjectObject();
      boolean hasPermission = (hasViewPermission(project)) && (hasSynapseBrowsePermission(project));
      testPlan = PluginUtil.getValidatedIssue(testPlan, ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser());
      if (testPlan == null) {
        return Response.status(Response.Status.FORBIDDEN).entity(new RESTErrorMessage(i18n.getText("synapse.gadget.error.issue.permission"))).build();
      }
      if (hasPermission) {
        JSONObject responseJson = new JSONObject();
        responseJson.put("project", testPlan.getProjectObject().getName());
        responseJson.put("testPlan", testPlan.getKey() + " - " + StringEscapeUtils.escapeHtml4(testPlan.getSummary()));
        responseJson.put("testCycle", testCycleBean.getName());
        return Response.ok(responseJson.toString()).build();
      }
      
      return Response.status(Response.Status.FORBIDDEN).entity(new RESTErrorMessage(i18n.getText("synapse.gadget.error.browse.permission.project"))).build();
    }
    catch (JSONException e) {
      log.debug(e.getMessage(), e);
      log.error(e.getMessage());
      return Response.serverError().entity(e.getMessage()).build();
    }
  }
  
  @GET
  @Path("getDefectStatisticsVsTestCycleGadgetData")
  public Response getDefectStatisticsVsTestCycleGadgetData(@QueryParam("cycleId") String cycleId, @QueryParam("columnNames") String columnNames, @QueryParam("num") Integer number)
  {
    log.debug("Retrieve defect statistics data for Cycle with id " + cycleId);
    try {
      TestCycleOutputBean testCycleBean = null;
      try {
        testCycleBean = testCycleService.getCycle(Integer.valueOf(cycleId));
      } catch (InvalidDataException err) {
        log.debug(err.getMessage(), err);
        log.error(err.getMessage());
        return Response.status(Response.Status.GONE).entity(i18n.getText("synapse.gadget.error.issue.deleted")).build();
      }
      Issue testPlan = issueManager.getIssueObject(testCycleBean.getTpId());
      Project project = testPlan.getProjectObject();
      boolean hasPermission = (hasViewPermission(project)) && (hasSynapseBrowsePermission(project));
      testPlan = PluginUtil.getValidatedIssue(testPlan, ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser());
      if (testPlan == null) {
        return Response.status(Response.Status.FORBIDDEN).entity(new RESTErrorMessage(i18n.getText("synapse.gadget.error.issue.permission"))).build();
      }
      if (hasPermission) {
        List<TestRunOutputBean> testRunBeans = testRunService.getTestRuns(Integer.valueOf(cycleId), ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser());
        log.debug("testRunBeans " + testRunBeans);
        
        List<String> testCaseIds = null;
        Map<Long, List<String>> bug2TestCases = new HashMap();
        Map<Long, Issue> bug2Issue = new HashMap();
        for (TestRunOutputBean runBean : testRunBeans) {
          log.debug("runBean - " + runBean.getID());
          log.debug("runBean getBugs- " + runBean.getBugs());
          
          testCase = issueManager.getIssueObject(runBean.getTestCaseId());
          
          List<IssueWrapperBean> wrapperBeans = runBean.getBugs();
          if (wrapperBeans != null) {
            for (IssueWrapperBean wrapperBean : wrapperBeans) {
              testCaseIds = (List)bug2TestCases.get(wrapperBean.getId());
              if (testCaseIds == null) {
                testCaseIds = new ArrayList();
              }
              if (testCase != null) {
                testCaseIds.add(testCase.getKey());
              }
              bug2TestCases.put(wrapperBean.getId(), testCaseIds);
              if (bug2Issue.get(wrapperBean.getId()) == null) {
                bug2Issue.put(wrapperBean.getId(), issueManager.getIssueByCurrentKey(wrapperBean.getKey()));
              }
            }
          }
        }
        
        Issue testCase;
        DefectStatisticsVsTestCycleGadgetHelper cycleGadgetHelper = new DefectStatisticsVsTestCycleGadgetHelper(bug2TestCases, bug2Issue, columnNames, number);
        
        return Response.ok(new HtmlResponseWrapper(cycleGadgetHelper.getHtml())).build();
      }
      
      return Response.status(Response.Status.FORBIDDEN).entity(new RESTErrorMessage(i18n.getText("synapse.gadget.error.browse.permission.project"))).build();

    }
    catch (InvalidDataException e)
    {

      log.debug(e.getMessage(), e);
      log.error(e.getMessage());
      return Response.serverError().entity(e.getMessage()).build();
    }
  }
  
  @GET
  @Path("getTCExecutionByTesterGadgetConfigData")
  public Response getTCExecutionByTesterGadgetConfigData(@QueryParam("cycleId") String cycleId, @QueryParam("testPlanId") String testPlanId, @QueryParam("latestResult") String latestResult) {
    log.debug("Retrieve execution chart data for Cycle with id " + cycleId);
    try {
      String testCycleName = null;
      if (StringUtils.isBlank(cycleId)) {
        testCycleName = i18n.getText("synapse.common.label.option.select.all");
      } else {
        try {
          TestCycleOutputBean testCycleBean = testCycleService.getCycle(Integer.valueOf(cycleId));
          testCycleName = testCycleBean.getName();
        } catch (InvalidDataException err) {
          log.debug(err.getMessage(), err);
          log.error(err.getMessage());
          return Response.status(Response.Status.GONE).entity(i18n.getText("synapse.gadget.error.issue.deleted")).build();
        }
      }
      Issue testPlan = null;
      if (StringUtils.isNumeric(testPlanId)) {
        testPlan = issueManager.getIssueObject(Long.valueOf(testPlanId));
      } else {
        testPlan = issueManager.getIssueByCurrentKey(testPlanId);
      }
      if (testPlan == null) {
        throw new InvalidDataException(i18n.getText("synapse.gadget.error.issue.deleted"));
      }
      testPlan = PluginUtil.getValidatedIssue(testPlan, ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser());
      if (testPlan == null) {
        return Response.status(Response.Status.FORBIDDEN).entity(new RESTErrorMessage(i18n.getText("synapse.gadget.error.issue.permission"))).build();
      }
      Project project = testPlan.getProjectObject();
      boolean hasPermission = (hasViewPermission(project)) && (hasSynapseBrowsePermission(project));
      if (hasPermission) {
        JSONObject responseJson = new JSONObject();
        responseJson.put("project", testPlan.getProjectObject().getName());
        responseJson.put("testPlan", testPlan.getKey() + " - " + StringEscapeUtils.escapeHtml4(testPlan.getSummary()));
        responseJson.put("testCycle", testCycleName);
        if ((latestResult.equalsIgnoreCase("")) || (latestResult.equalsIgnoreCase("false"))) {
          responseJson.put("latestResult", i18n.getText("synapse.common.label.no"));
        } else {
          responseJson.put("latestResult", i18n.getText("synapse.common.label.ok"));
        }
        return Response.ok(responseJson.toString()).build();
      }
      
      return Response.status(Response.Status.FORBIDDEN).entity(new RESTErrorMessage(i18n.getText("synapse.gadget.error.browse.permission.project"))).build();
    }
    catch (InvalidDataException e)
    {
      log.debug(e.getMessage(), e);
      log.error(e.getMessage());
      return Response.status(Response.Status.GONE).entity(e.getMessage()).build();
    } catch (JSONException e) {
      log.debug(e.getMessage(), e);
      log.error(e.getMessage());
      return Response.serverError().entity(e.getMessage()).build();
    }
  }
  
  @GET
  @Path("getTCExecutionByFieldGadgetConfigData")
  public Response getTCExecutionByFieldGadgetConfigData(@QueryParam("cycleId") String cycleId, @QueryParam("testPlanId") String testPlanId, @QueryParam("field") String field, @QueryParam("latestResult") String latestResult) {
    log.debug("Retrieve execution chart data for Cycle with id " + cycleId);
    try {
      String testCycleName = null;
      if (StringUtils.isBlank(cycleId)) {
        testCycleName = i18n.getText("synapse.common.label.option.select.all");
      } else {
        try {
          TestCycleOutputBean testCycleBean = testCycleService.getCycle(Integer.valueOf(cycleId));
          testCycleName = testCycleBean.getName();
        } catch (InvalidDataException err) {
          log.debug(err.getMessage(), err);
          log.error(err.getMessage());
          return Response.status(Response.Status.GONE).entity(i18n.getText("synapse.gadget.error.issue.deleted")).build();
        }
      }
      Issue testPlan = null;
      if (StringUtils.isNumeric(testPlanId)) {
        testPlan = issueManager.getIssueObject(Long.valueOf(testPlanId));
      } else {
        testPlan = issueManager.getIssueByCurrentKey(testPlanId);
      }
      if (testPlan == null) {
        throw new InvalidDataException(i18n.getText("synapse.gadget.error.issue.deleted"));
      }
      testPlan = PluginUtil.getValidatedIssue(testPlan, ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser());
      if (testPlan == null) {
        return Response.status(Response.Status.FORBIDDEN).entity(new RESTErrorMessage(i18n.getText("synapse.gadget.error.issue.permission"))).build();
      }
      String fieldName = getFieldNameFromKey(field);
      Project project = testPlan.getProjectObject();
      boolean hasPermission = (hasViewPermission(project)) && (hasSynapseBrowsePermission(project));
      if (hasPermission) {
        JSONObject responseJson = new JSONObject();
        responseJson.put("project", testPlan.getProjectObject().getName());
        responseJson.put("testPlan", testPlan.getKey() + " - " + StringEscapeUtils.escapeHtml4(testPlan.getSummary()));
        responseJson.put("testCycle", testCycleName);
        responseJson.put("field", fieldName);
        if ((latestResult.equalsIgnoreCase("")) || (latestResult.equalsIgnoreCase("false"))) {
          responseJson.put("latestResult", i18n.getText("synapse.common.label.no"));
        } else {
          responseJson.put("latestResult", i18n.getText("synapse.common.label.ok"));
        }
        return Response.ok(responseJson.toString()).build();
      }
      
      return Response.status(Response.Status.FORBIDDEN).entity(new RESTErrorMessage(i18n.getText("synapse.gadget.error.browse.permission.project"))).build();
    }
    catch (InvalidDataException e)
    {
      log.debug(e.getMessage(), e);
      log.error(e.getMessage());
      return Response.status(Response.Status.GONE).entity(e.getMessage()).build();
    } catch (JSONException e) {
      log.debug(e.getMessage(), e);
      log.error(e.getMessage());
      return Response.serverError().entity(e.getMessage()).build();
    }
  }
  
  @GET
  @Path("getTCExecutionByTesterGadgetData")
  public Response getTCExecutionByTesterGadgetData(@QueryParam("cycleId") String cycleId, @QueryParam("testPlanId") String testPlanId, @QueryParam("num") String number, @QueryParam("latestResult") String latestResult)
  {
    log.debug("Retrieve TC execution data for Cycle with id " + cycleId);
    try {
      Issue testPlan = null;
      if (StringUtils.isNumeric(testPlanId)) {
        testPlan = issueManager.getIssueObject(Long.valueOf(testPlanId));
      } else {
        testPlan = issueManager.getIssueByCurrentKey(testPlanId);
      }
      if (testPlan == null) {
        return Response.status(Response.Status.GONE).entity(i18n.getText("synapse.gadget.error.issue.deleted")).build();
      }
      testPlan = PluginUtil.getValidatedIssue(testPlan, ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser());
      if (testPlan == null) {
        return Response.status(Response.Status.FORBIDDEN).entity(new RESTErrorMessage(i18n.getText("synapse.gadget.error.issue.permission"))).build();
      }
      Project project = testPlan.getProjectObject();
      boolean hasPermission = (hasViewPermission(project)) && (hasSynapseBrowsePermission(project));
      if (hasPermission)
      {
        List<String> testCycleIds = new ArrayList();
        List<Integer> cycles = new ArrayList();
        List<TestCycleOutputBean> testCycleBeans = null;
        Iterator localIterator1; TestCycleOutputBean testCycleBean; if (StringUtils.isBlank(cycleId)) {
          testCycleBeans = testCycleService.getCycles(Long.valueOf(testPlan.getId().longValue()));
          testCycleIds.add("-1");
          for (localIterator1 = testCycleBeans.iterator(); localIterator1.hasNext();) { testCycleBean = (TestCycleOutputBean)localIterator1.next();
            if ((!testCycleBean.isAborted()) && (!testCycleBean.isDraft())) {
              cycles.add(testCycleBean.getID());
            }
          }
        } else {
          TestCycleOutputBean testCycleBean = testCycleService.getCycle(Integer.valueOf(cycleId));
          testCycleBeans = new ArrayList();
          testCycleBeans.add(testCycleBean);
          testCycleIds.add(String.valueOf(testCycleBean.getID()));
        }
        
        Object testRunBeans = new ArrayList();
        TestCycleOutputBean testCycleBean; if ((latestResult.equalsIgnoreCase("")) || (latestResult.equalsIgnoreCase("false"))) {
          for (testCycleBean = testCycleBeans.iterator(); testCycleBean.hasNext();) { testCycleBean = (TestCycleOutputBean)testCycleBean.next();
            if ((!testCycleBean.isAborted()) && (!testCycleBean.isDraft())) {
              List<TestRunOutputBean> testRuns = testRunService.getTestRuns(Integer.valueOf(testCycleBean.getID().intValue()), ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser());
              if ((testRuns != null) && (testRuns.size() > 0)) {
                ((List)testRunBeans).addAll(testRuns);
              }
            }
          }
        } else {
          List<TestRunOutputBean> testRuns = testRunService.getLatestTestRunsForReport(testPlan.getId(), testCycleIds, ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser());
          if ((testRuns != null) && (testRuns.size() > 0)) {
            ((List)testRunBeans).addAll(testRuns);
          }
        }
        Map<String, TesterExecutionStatisticsBean> statisticsMap = new TreeMap();
        
        for (TestRunOutputBean runBean : (List)testRunBeans) {
          String testerName = null;
          String userName = null;
          ApplicationUser user = runBean.getTester();
          if (user == null) {
            testerName = i18n.getText("synapse.common.label.unassigned");
            userName = testerName;
          } else {
            testerName = user.getDisplayName();
            userName = user.getUsername();
            log.debug("testerName : " + testerName);
          }
          TesterExecutionStatisticsBean statisticsBean = (TesterExecutionStatisticsBean)statisticsMap.get(testerName);
          if (statisticsBean == null) {
            statisticsBean = new TesterExecutionStatisticsBean();
          }
          statisticsBean.setTesterUserName(userName);
          
          StatItemBean statItemBean = (StatItemBean)statisticsBean.getStatusStatistics().get(runBean.getRunStatus().getName());
          if (statItemBean == null) {
            statItemBean = new StatItemBean(runBean.getStatusEnum());
            statisticsBean.getStatusStatistics().put(runBean.getRunStatus().getName(), statItemBean);
          }
          statItemBean.incrementCount();
          
          if (runBean.getBugs() != null) {
            for (IssueWrapperBean wrapperBean : runBean.getBugs()) {
              statisticsBean.getBugs().add(wrapperBean.getKey());
            }
          }
          statisticsMap.put(testerName, statisticsBean);
        }
        
        TestCaseExecutionToTesterStatisticsHelper statisticsHelper = new TestCaseExecutionToTesterStatisticsHelper(statisticsMap, testPlan, cycleId, number, cycles, latestResult);
        return Response.ok(new HtmlResponseWrapper(statisticsHelper.getHtml())).build();
      }
      
      return Response.status(Response.Status.FORBIDDEN).entity(new RESTErrorMessage(i18n.getText("synapse.gadget.error.browse.permission.project"))).build();
    }
    catch (InvalidDataException e) {
      log.debug(e.getMessage(), e);
      log.error(e.getMessage());
      return Response.serverError().entity(e.getMessage()).build();
    }
  }
  
  @GET
  @Path("getTCExecutionByFieldGadgetData")
  public Response getTCExecutionByFieldGadgetData(@QueryParam("cycleId") String cycleId, @QueryParam("testPlanId") String testPlanId, @QueryParam("num") String number, @QueryParam("field") String field, @QueryParam("latestResult") String latestResult)
    throws FieldException
  {
    log.debug("Retrieve TC execution data for Cycle with id " + cycleId);
    try {
      Issue testPlan = null;
      if (StringUtils.isNumeric(testPlanId)) {
        testPlan = issueManager.getIssueObject(Long.valueOf(testPlanId));
      } else {
        testPlan = issueManager.getIssueByCurrentKey(testPlanId);
      }
      if (testPlan == null) {
        return Response.status(Response.Status.GONE).entity(i18n.getText("synapse.gadget.error.issue.deleted")).build();
      }
      testPlan = PluginUtil.getValidatedIssue(testPlan, 
        ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser());
      if (testPlan == null) {
        return 
          Response.status(Response.Status.FORBIDDEN).entity(new RESTErrorMessage(i18n.getText("synapse.gadget.error.issue.permission"))).build();
      }
      Project project = testPlan.getProjectObject();
      boolean hasPermission = (hasViewPermission(project)) && (hasSynapseBrowsePermission(project));
      if (hasPermission) {
        List<String> testCycleIds = new ArrayList();
        List<Integer> cycles = new ArrayList();
        List<TestCycleOutputBean> testCycleBeans = null;
        if (StringUtils.isBlank(cycleId)) {
          testCycleBeans = testCycleService.getCycles(Long.valueOf(testPlan.getId().longValue()));
          testCycleIds.add("-1");
          for (TestCycleOutputBean testCycleBean : testCycleBeans) {
            if ((!testCycleBean.isAborted()) && (!testCycleBean.isDraft())) {
              cycles.add(testCycleBean.getID());
            }
          }
        } else {
          TestCycleOutputBean testCycleBean = testCycleService.getCycle(Integer.valueOf(cycleId));
          testCycleBeans = new ArrayList();
          testCycleBeans.add(testCycleBean);
          testCycleIds.add(String.valueOf(testCycleBean.getID()));
        }
        
        Object testRunBeans = new ArrayList();
        Set<Long> bugs = new HashSet();
        Map<String, Integer> statusMap = new HashMap();
        if ((latestResult.equalsIgnoreCase("")) || (latestResult.equalsIgnoreCase("false"))) {
          for (TestCycleOutputBean testCycleBean : testCycleBeans) {
            if ((!testCycleBean.isAborted()) && (!testCycleBean.isDraft())) {
              List<TestRunOutputBean> testRuns = testRunService.getTestRuns(Integer.valueOf(testCycleBean.getID().intValue()), ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser());
              if ((testRuns != null) && (testRuns.size() > 0)) {
                ((List)testRunBeans).addAll(testRuns);
              }
            }
          }
        } else {
          Object testRuns = testRunService.getLatestTestRunsForReport(testPlan.getId(), testCycleIds, ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser());
          if ((testRuns != null) && (((List)testRuns).size() > 0)) {
            ((List)testRunBeans).addAll((Collection)testRuns);
          }
        }
        Object statisticsMap = new TreeMap();
        
        CustomFieldManager customFieldManager = ComponentAccessor.getCustomFieldManager();
        CustomField cField = customFieldManager.getCustomFieldObject(field);
        boolean isCascadeField = false;
        boolean isRequirement = false;
        Map<String, String> jqlNameMap = new TreeMap();
        if (ComponentAccessor.getFieldManager().isCustomField(cField)) {
          customFieldTypeKey = cField.getCustomFieldType().getKey();
          log.debug("customFieldTypeKey " + customFieldTypeKey);
          if (customFieldTypeKey.contains("cascadingselect")) {
            isCascadeField = true;
          }
          if (customFieldTypeKey.contains("testSuiteField")) {
            statisticsMap = new LinkedHashMap();
          }
          if (customFieldTypeKey.contains("requirementField")) {
            isRequirement = true;
          }
        }
        
        for (String customFieldTypeKey = ((List)testRunBeans).iterator(); customFieldTypeKey.hasNext();) { runBean = (TestRunOutputBean)customFieldTypeKey.next();
          List<Long> bugIds = runBean.getBugIds();
          String runStatus = runBean.getStatusEnum().getLocalizedName();
          if (!statusMap.containsKey(runStatus)) {
            statusMap.put(runStatus, Integer.valueOf(0));
          }
          statusCount = (Integer)statusMap.get(runBean.getRunStatus().getName());
          statusMap.put(runStatus, Integer.valueOf(statusCount.intValue() + 1));
          if ((bugIds != null) && (bugIds.size() > 0)) {
            bugs.addAll(runBean.getBugIds());
          }
          List<String> fieldValues = getFieldValues(runBean, field);
          if (isCascadeField) {
            jqlNameMap = getCascadeValues(runBean, field);
          }
          
          for (String value : fieldValues) {
            FieldExecutionStatisticsBean statisticsBean = (FieldExecutionStatisticsBean)((Map)statisticsMap).get(value);
            if (statisticsBean == null) {
              statisticsBean = new FieldExecutionStatisticsBean();
            }
            statisticsBean.setFieldName(value);
            if (isCascadeField) {
              if (jqlNameMap.get(value) != null) {
                statisticsBean.setJqlSearchName((String)jqlNameMap.get(value));
              } else {
                statisticsBean.setJqlSearchName(value);
              }
            } else {
              statisticsBean.setJqlSearchName(value);
            }
            if (isRequirement) {
              Issue reqIssue = issueManager.getIssueObject(value);
              if (reqIssue != null) {
                statisticsBean.setIssueSummary(reqIssue.getSummary());
              }
            }
            StatItemBean statItemBean = (StatItemBean)statisticsBean.getStatusStatistics().get(runBean.getRunStatus().getName());
            if (statItemBean == null) {
              statItemBean = new StatItemBean(runBean.getStatusEnum());
              statisticsBean.getStatusStatistics().put(runBean.getRunStatus().getName(), statItemBean);
            }
            statItemBean.incrementCount();
            
            if (runBean.getBugs() != null) {
              for (IssueWrapperBean wrapperBean : runBean.getBugs()) {
                statisticsBean.getBugs().add(wrapperBean.getKey());
              }
            }
            ((Map)statisticsMap).put(value, statisticsBean); } }
        TestRunOutputBean runBean;
        Integer statusCount;
        String fieldName = getFieldNameFromKey(field);
        String jqlFieldName = field;
        
        if (ComponentAccessor.getFieldManager().isCustomField(cField)) {
          if (cField.getCustomFieldType().getKey().contains("testSuiteField")) {
            Set<String> testSuitePaths = testSuiteService.getAllTestSuitesPath(testPlan.getProjectId(), ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser());
            statisticsMap = makeSameOrder(testSuitePaths, (Map)statisticsMap);
          } else if (cField.getName().equalsIgnoreCase(CustomJQLFieldEnum.URGENCYOFRUN.getName())) {
            List<String> urgencyInOrder = UrgencyFieldEnum.valuesAsString(i18n);
            Map<String, FieldExecutionStatisticsBean> orderedStatisticsMap = new LinkedHashMap();
            if (((Map)statisticsMap).containsKey("-")) {
              orderedStatisticsMap.put("-", ((Map)statisticsMap).get("-"));
            }
            for (String urgency : urgencyInOrder) {
              if (((Map)statisticsMap).containsKey(urgency)) {
                orderedStatisticsMap.put(urgency, ((Map)statisticsMap).get(urgency));
              }
            }
            statisticsMap = orderedStatisticsMap;
          }
        }
        int totalRunBeans = ((List)testRunBeans).size();
        int totalBugs = bugs.size();
        TestCaseExecutionToFieldStatisticsHelper statisticsHelper = new TestCaseExecutionToFieldStatisticsHelper((Map)statisticsMap, testPlan, cycleId, number, fieldName, isCascadeField, totalRunBeans, totalBugs, statusMap, cycles, latestResult, jqlFieldName);
        
        return Response.ok(new HtmlResponseWrapper(statisticsHelper.getHtml())).build();
      }
      
      return Response.status(Response.Status.FORBIDDEN).entity(new RESTErrorMessage(i18n.getText("synapse.gadget.error.browse.permission.project"))).build();
    }
    catch (InvalidDataException e) {
      log.debug(e.getMessage(), e);
      log.error(e.getMessage());
      return Response.serverError().entity(e.getMessage()).build();
    }
  }
  
  private Map<String, String> getCascadeValues(TestRunOutputBean runBean, String field) {
    Map<String, String> cascadeValues = new TreeMap();
    List<String> fieldValues = new ArrayList();
    Issue testCase = runBean.getTestCase();
    CustomFieldManager customFieldManager = ComponentAccessor.getCustomFieldManager();
    CustomField cField = customFieldManager.getCustomFieldObject(field);
    Map<String, Option> map = (Map)testCase.getCustomFieldValue(cField);
    if ((map != null) && (map.size() > 0)) {
      Collection<Option> values = map.values();
      for (Option value : values) {
        if (fieldValues.size() > 0) {
          String previousVal = (String)fieldValues.get(0);
          fieldValues.set(0, previousVal + "-" + value);
          cascadeValues.clear();
          cascadeValues.put(previousVal + "-" + value, previousVal + "','" + value);
        } else {
          cascadeValues.put(value.toString(), value.toString());
          fieldValues.add(value.toString());
        }
      }
    }
    
    return cascadeValues;
  }
  
  private List<String> getFieldValues(TestRunOutputBean runBean, String field) {
    List<String> fieldValues = new ArrayList();
    Issue testCase = runBean.getTestCase();
    CustomFieldManager customFieldManager = ComponentAccessor.getCustomFieldManager();
    CustomField cField = customFieldManager.getCustomFieldObject(field);
    if (ComponentAccessor.getFieldManager().isCustomField(cField)) {
      ??? = cField.getCustomFieldType().getKey();
      log.debug("customFieldTypeKey " + ???);
      Collection<Option> values; if (???.contains("cascadingselect")) {
        Map<String, Option> map = (Map)testCase.getCustomFieldValue(cField);
        if ((map != null) && (map.size() > 0)) {
          values = map.values();
          for (Option value : values) {
            if (fieldValues.size() > 0) {
              String previousVal = (String)fieldValues.get(0);
              fieldValues.set(0, previousVal + "-" + value);
            } else {
              fieldValues.add(value.toString());
            }
          }
        }
      } else if (cField.getName().equalsIgnoreCase(CustomJQLFieldEnum.URGENCYOFRUN.getName())) {
        String value = runBean.getUrgency();
        if (StringUtils.isNotBlank(value)) {
          fieldValues.add(value.toString());
        }
      }
      else if (cField.getName().equalsIgnoreCase(SynapseCustomField.RUNATTRIBUTES.getName())) {
        List<Integer> runAttributevalues = runBean.getRunAttributeIds();
        if ((!runAttributevalues.isEmpty()) && (runAttributevalues != null)) {
          for (Integer value : runAttributevalues) {
            RunAttributeValueOutputBean runAttribute = runAttributeService.getAttributeValue(value);
            fieldValues.add(runAttribute.getValue());
          }
        }
      } else if ((!???.contains("multiselect")) && (
        (???.contains("select")) || (???.contains("radiobuttons")))) {
        LazyLoadedOption value = (LazyLoadedOption)testCase.getCustomFieldValue(cField);
        if (value != null) {
          fieldValues.add(value.toString());
        }
      } else if (???.contains("multiuserpicker")) {
        List<ApplicationUser> values = (List)testCase.getCustomFieldValue(cField);
        if ((values != null) && (values.size() > 0)) {
          Iterator iterator = values.iterator();
          while (iterator.hasNext()) {
            ApplicationUser value = (ApplicationUser)iterator.next();
            if (value != null) {
              fieldValues.add(value.getDisplayName());
            }
          }
        }
      } else if (???.contains("userpicker")) {
        ApplicationUser value = (ApplicationUser)testCase.getCustomFieldValue(cField);
        if (value != null) {
          fieldValues.add(value.getDisplayName());
        }
      } else if (???.contains("multigrouppicker")) {
        List<Group> values = (List)testCase.getCustomFieldValue(cField);
        if ((values != null) && (values.size() > 0)) {
          Iterator iterator = values.iterator();
          while (iterator.hasNext()) {
            Group value = (Group)iterator.next();
            if (value != null) {
              fieldValues.add(value.getName());
            }
          }
        }
      } else if (???.contains("grouppicker")) {
        List<Group> values = (List)testCase.getCustomFieldValue(cField);
        if ((values != null) && (values.size() > 0)) {
          fieldValues.add(((Group)values.get(0)).getName());
        }
      } else if (???.contains("multiversion")) {
        List<Version> values = (List)testCase.getCustomFieldValue(cField);
        if ((values != null) && (values.size() > 0)) {
          Iterator iterator = values.iterator();
          while (iterator.hasNext()) {
            Version value = (Version)iterator.next();
            if (value != null) {
              fieldValues.add(value.getName());
            }
          }
        }
      } else if (???.contains("version")) {
        List<Version> values = (List)testCase.getCustomFieldValue(cField);
        if ((values != null) && (values.size() > 0)) {
          fieldValues.add(((Version)values.get(0)).getName());
        }
      } else if (???.contains("project")) {
        Project value = (Project)testCase.getCustomFieldValue(cField);
        if (value != null) {
          fieldValues.add(value.getName());
        }
      } else if ((???.contains("testSuiteField")) || (???.contains("requirementField"))) {
        String strValue = (String)testCase.getCustomFieldValue(cField);
        strValue = StringEscapeUtils.unescapeHtml4(strValue);
        Set<String> values = new HashSet(Arrays.asList(strValue.split(",")));
        if ((values != null) && (values.size() > 0)) {
          Iterator iterator = values.iterator();
          while (iterator.hasNext()) {
            Object value = iterator.next();
            if ((value != null) && (StringUtils.isNotBlank(value.toString()))) {
              fieldValues.add(value.toString().trim());
            }
          }
        }
      }
      else {
        ??? = (Collection)testCase.getCustomFieldValue(cField);
        if ((??? != null) && (???.size() > 0)) {
          Iterator iterator = ???.iterator();
          while (iterator.hasNext()) {
            Object value = iterator.next();
            if (value != null) {
              fieldValues.add(value.toString());
            }
          }
        }
      }
    }
    field = field.toLowerCase().replaceAll(" ", "").replaceAll("/", "");
    if (field != null) {
      switch (field) {
      case "creator": 
        fieldValues.add(testCase.getCreator() != null ? testCase.getCreator().getDisplayName() : "-");
        break;
      case "issuetype": 
        fieldValues.add(testCase.getIssueType() != null ? testCase.getIssueType().getName() : "-");
        break;
      case "project": 
        fieldValues.add(testCase.getProjectObject() != null ? testCase.getProjectObject().getName() : "-");
        break;
      case "assignee": 
        fieldValues.add(testCase.getAssignee() != null ? testCase.getAssignee().getDisplayName() : "-");
        break;
      case "priority": 
        fieldValues.add(testCase.getPriority() != null ? testCase.getPriority().getName() : "-");
        break;
      case "status": 
        fieldValues.add(testCase.getStatus() != null ? testCase.getStatus().getName() : "-");
        break;
      case "resolution": 
        fieldValues.add(testCase.getResolution() != null ? testCase.getResolution().getName() : "-");
        break;
      case "reporter": 
        fieldValues.add(testCase.getReporter() != null ? testCase.getReporter().getDisplayName() : "-");
        break;
      case "environment": 
        fieldValues.add(testCase.getEnvironment() != null ? testCase.getEnvironment() : "-");
      }
      
    }
    
    if (field.equalsIgnoreCase("labels")) {
      Set<Label> labels = testCase.getLabels();
      if ((labels != null) && (labels.size() > 0)) {
        for (Label label : labels) {
          fieldValues.add(label.getLabel());
        }
      }
    }
    
    if (field.equalsIgnoreCase("components")) {
      Collection<ProjectComponent> components = testCase.getComponents();
      if ((components != null) && (components.size() > 0)) {
        for (ProjectComponent component : components) {
          fieldValues.add(component.getName());
        }
      }
    }
    
    if (field.equalsIgnoreCase("fixVersions")) {
      Collection<Version> fixVersion = testCase.getFixVersions();
      if ((fixVersion != null) && (fixVersion.size() > 0)) {
        for (Version version : fixVersion) {
          fieldValues.add(version.getName());
        }
      }
    }
    
    if ((fieldValues != null) && (fieldValues.size() > 0)) {
      return fieldValues;
    }
    fieldValues.add("-");
    return fieldValues;
  }
  
  @GET
  @Path("getTCExecutionByCycleGadgetData")
  public Response getTCExecutionByCycleGadgetData(@QueryParam("testPlanId") String testPlanId, @QueryParam("num") String number)
  {
    log.debug("Retrieve TC execution data for plan with id " + testPlanId);
    try
    {
      Issue testPlan = issueManager.getIssueObject(testPlanId);
      if (testPlan == null) {
        return Response.status(Response.Status.GONE).entity(i18n.getText("synapse.gadget.error.issue.deleted")).build();
      }
      testPlan = PluginUtil.getValidatedIssue(testPlan, ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser());
      if (testPlan == null) {
        return Response.status(Response.Status.FORBIDDEN).entity(new RESTErrorMessage(i18n.getText("synapse.gadget.error.issue.permission"))).build();
      }
      Project project = testPlan.getProjectObject();
      boolean hasPermission = (hasViewPermission(project)) && (hasSynapseBrowsePermission(project));
      if (hasPermission) {
        List<TestCycleOutputBean> testCycleBeans = null;
        testCycleBeans = testCycleService.getCycles(Long.valueOf(testPlan.getId().longValue()));
        Map<String, TesterExecutionStatisticsBean> statisticsMap = new LinkedHashMap();
        
        for (Iterator localIterator1 = testCycleBeans.iterator(); localIterator1.hasNext();) { testCycleBean = (TestCycleOutputBean)localIterator1.next();
          if ((!testCycleBean.isAborted()) && (!testCycleBean.isDraft())) {
            statisticsBean = (TesterExecutionStatisticsBean)statisticsMap.get(testCycleBean.getID());
            if (statisticsBean == null) {
              statisticsBean = new TesterExecutionStatisticsBean();
            }
            
            for (TestRunOutputBean runBean : testRunService.getTestRuns(Integer.valueOf(testCycleBean.getID().intValue()), ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser())) {
              StatItemBean statItemBean = (StatItemBean)statisticsBean.getStatusStatistics().get(runBean.getRunStatus().getName());
              if (statItemBean == null) {
                statItemBean = new StatItemBean(runBean.getStatusEnum());
                statisticsBean.getStatusStatistics().put(runBean.getRunStatus().getName(), statItemBean);
              }
              statItemBean.incrementCount();
              
              if (runBean.getBugs() != null) {
                for (IssueWrapperBean wrapperBean : runBean.getBugs()) {
                  statisticsBean.getBugs().add(wrapperBean.getKey());
                }
              }
              statisticsBean.setCycleId(testCycleBean.getID());
              statisticsMap.put(testCycleBean.getName(), statisticsBean);
            } } }
        TestCycleOutputBean testCycleBean;
        TesterExecutionStatisticsBean statisticsBean;
        TestCaseExecutionToCycleStatisticsHelper statisticsHelper = new TestCaseExecutionToCycleStatisticsHelper(statisticsMap, testPlan, number);
        return Response.ok(new HtmlResponseWrapper(statisticsHelper.getHtml())).build();
      }
      
      return Response.status(Response.Status.FORBIDDEN).entity(new RESTErrorMessage(i18n.getText("synapse.gadget.error.browse.permission.project"))).build();
    }
    catch (InvalidDataException e)
    {
      log.debug(e.getMessage(), e);
      log.error(e.getMessage());
      return Response.serverError().entity(e.getMessage()).build();
    }
  }
  
  @GET
  @Path("assignedToMeRuns")
  public Response getAssignedToMeRuns(@QueryParam("num") Integer number) {
    log.debug("Retrieving test runs assigned to me");
    
    List<TestRunOutputBean> tRuns = gadgetService.getMyOpenRuns();
    
    TestRunAssignedToMeGadgetHelper tratmgHelper = new TestRunAssignedToMeGadgetHelper(tRuns, number);
    
    return Response.ok(new HtmlResponseWrapper(tratmgHelper.getHtml())).build();
  }
  
  @GET
  @Path("getTestCaseExecutionsPeriodTableData")
  public Response getTestCaseExecutionsPeriodTableData(@QueryParam("project") String projectKey, @QueryParam("groupBy") String groupBy, @QueryParam("statistictype") String statisticType, @QueryParam("periodstartdate") String startDate, @QueryParam("periodenddate") String endDate, @QueryParam("testExecutors") String testExecutors, @QueryParam("adhocTestRuns") String adhocTestRuns)
    throws ParseException
  {
    log.debug("Retrieving table data for testcase executions during period gadget for project :" + projectKey);
    
    String html = "";
    if (groupBy == null) {
      groupBy = i18n.getText("synapse.gadget.testcase.executions.during.period.day");
    }
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    Date fromDate = sdf.parse(startDate);
    Date toDate = sdf.parse(endDate);
    String[] testExecutorArray = testExecutors.split("\\|");
    List<String> executors = new ArrayList();
    for (int index = 0; index < testExecutorArray.length; index++) {
      if (testExecutorArray[index].equalsIgnoreCase("-1")) {
        executors.add(i18n.getText("synapse.common.label.option.select.all"));
      } else {
        Optional<ApplicationUser> user = userManager.getUserById(Long.valueOf(testExecutorArray[index]));
        if (user != null) {
          executors.add(((ApplicationUser)user.get()).getUsername());
        }
      }
    }
    Set<TestRunOutputBean> testRunBeans = new HashSet();
    List<Integer> cycles = new ArrayList();
    Issue testPlan = null;
    
    if ((projectKey != null) && (projectKey.length() > 0)) {
      Project project = ComponentAccessor.getProjectManager().getProjectObjByKeyIgnoreCase(projectKey);
      List<TestPlanGadgetBean> allTestPlans = gadgetService.getTestPlans(projectKey);
      for (TestPlanGadgetBean testPlanKey : allTestPlans) {
        try {
          if (StringUtils.isNumeric(testPlanKey.getPlanKey())) {
            testPlan = issueManager.getIssueObject(testPlanKey.getPlanKey());
          } else {
            testPlan = issueManager.getIssueByCurrentKey(testPlanKey.getPlanKey());
          }
          if (testPlan == null) {
            return 
              Response.status(Response.Status.GONE).entity(i18n.getText("synapse.gadget.error.issue.deleted")).build();
          }
          testPlan = PluginUtil.getValidatedIssue(testPlan, 
            ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser());
          if (testPlan == null) {
            return 
            
              Response.status(Response.Status.FORBIDDEN).entity(new RESTErrorMessage(i18n.getText("synapse.gadget.error.issue.permission"))).build();
          }
          Project projectObjectKey = testPlan.getProjectObject();
          boolean hasPermission = (hasViewPermission(projectObjectKey)) && (hasSynapseBrowsePermission(projectObjectKey));
          TestCycleOutputBean testCycleBean; if (hasPermission)
          {
            List<TestCycleOutputBean> testCycleBeans = null;
            testCycleBeans = testCycleService.getCycles(Long.valueOf(testPlan.getId().longValue()));
            for (TestCycleOutputBean testCycleBean : testCycleBeans) {
              if ((!testCycleBean.isAborted()) && (!testCycleBean.isDraft())) {
                cycles.add(testCycleBean.getID());
              }
            }
            
            for (??? = testCycleBeans.iterator(); ???.hasNext();) { testCycleBean = (TestCycleOutputBean)???.next();
              if ((!testCycleBean.isAborted()) && (!testCycleBean.isDraft())) {
                List<TestRunOutputBean> testRuns = testRunService.getTestRuns(Integer.valueOf(testCycleBean.getID().intValue()), ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser());
                if ((testRuns != null) && (testRuns.size() > 0)) {
                  testRunBeans.addAll(testRuns);
                }
              }
            }
            
            if ((!adhocTestRuns.equalsIgnoreCase("")) && (!adhocTestRuns.equalsIgnoreCase("false"))) {
              Object adhocTestRunList = testRunService.getAllAdhocTestRuns(project.getId(), ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser());
              if ((adhocTestRunList != null) && (((List)adhocTestRunList).size() > 0)) {
                for (TestRunOutputBean adhocTestRun : (List)adhocTestRunList) {
                  testRunBeans.add(adhocTestRun);
                  if ((adhocTestRun.getTestRunHistory() != null) && (adhocTestRun.getTestRunHistory().size() > 0)) {
                    for (TestRunHistoryOutputBean runHistory : adhocTestRun.getTestRunHistory()) {
                      Timestamp historyExecutionTime = runHistory.getExecutionTime();
                      if ((historyExecutionTime != null) && (historyExecutionTime.after(fromDate)) && (historyExecutionTime.before(toDate))) {
                        testRunBeans.add(runHistory.getTestRun());
                      }
                    }
                  }
                }
              }
            }
          } else {
            return 
            
              Response.status(Response.Status.FORBIDDEN).entity(new RESTErrorMessage(i18n.getText("synapse.gadget.error.browse.permission.project"))).build();
          }
        } catch (InvalidDataException e) {
          log.debug(e.getMessage(), e);
          log.error(e.getMessage());
          return Response.serverError().entity(e.getMessage()).build();
        }
      }
      

      Object statisticsMap = new TreeMap();
      
      statisticsMap = gadgetService.getTestCaseExecutionTableData(testRunBeans, (Map)statisticsMap, fromDate, toDate, statisticType, groupBy, executors);
      

      TestCaseExecutionDuringPeriodHelper statisticsHelper = new TestCaseExecutionDuringPeriodHelper((Map)statisticsMap, testPlan, cycles, statisticType, groupBy);
      

      html = html + statisticsHelper.getHtml();
    }
    

    return Response.ok(new HtmlResponseWrapper(html)).build();
  }
  
  @GET
  @Path("getTestCaseExecutionsPeriodChartData")
  public Response getTestCaseExecutionsPeriodChartData(@QueryParam("project") String projectKey, @QueryParam("groupBy") String groupBy, @QueryParam("statistictype") String statisticType, @QueryParam("periodstartdate") String startDate, @QueryParam("periodenddate") String endDate, @QueryParam("testExecutors") String testExecutors, @QueryParam("adhocTestRuns") String adhocTestRuns)
    throws ParseException, InvalidDataException
  {
    log.debug("Retrieving chart data for testcase executions during period gadget for project :" + projectKey);
    
    if (groupBy == null) {
      groupBy = i18n.getText("synapse.gadget.testcase.executions.during.period.day");
    }
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    Date fromDate = sdf.parse(startDate);
    Date toDate = sdf.parse(endDate);
    String[] testExecutorArray = testExecutors.split("\\|");
    List<String> executors = new ArrayList();
    for (int index = 0; index < testExecutorArray.length; index++) {
      if (testExecutorArray[index].equalsIgnoreCase("-1")) {
        executors.add(i18n.getText("synapse.common.label.option.select.all"));
      } else {
        Optional<ApplicationUser> user = userManager.getUserById(Long.valueOf(testExecutorArray[index]));
        if (user != null) {
          executors.add(((ApplicationUser)user.get()).getUsername());
        }
      }
    }
    
    if ((projectKey != null) && (projectKey.length() > 0)) {
      Project project = ComponentAccessor.getProjectManager().getProjectObjByKeyIgnoreCase(projectKey);
      List<TestPlanGadgetBean> allTestPlans = gadgetService.getTestPlans(projectKey);
      Set<TestRunOutputBean> testRunBeans = new HashSet();
      for (Iterator localIterator1 = allTestPlans.iterator(); localIterator1.hasNext();) { testPlanKey = (TestPlanGadgetBean)localIterator1.next();
        try {
          Issue testPlan = null;
          if (StringUtils.isNumeric(testPlanKey.getPlanKey())) {
            testPlan = issueManager.getIssueObject(testPlanKey.getPlanKey());
          } else {
            testPlan = issueManager.getIssueByCurrentKey(testPlanKey.getPlanKey());
          }
          if (testPlan == null) {
            return 
              Response.status(Response.Status.GONE).entity(i18n.getText("synapse.gadget.error.issue.deleted")).build();
          }
          testPlan = PluginUtil.getValidatedIssue(testPlan, 
            ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser());
          if (testPlan == null) {
            return 
            
              Response.status(Response.Status.FORBIDDEN).entity(new RESTErrorMessage(i18n.getText("synapse.gadget.error.issue.permission"))).build();
          }
          Project projectObjectKey = testPlan.getProjectObject();
          hasPermission = (hasViewPermission(projectObjectKey)) && (hasSynapseBrowsePermission(projectObjectKey));
          TestCycleOutputBean testCycleBean; if (hasPermission)
          {
            List<Integer> cycles = new ArrayList();
            List<TestCycleOutputBean> testCycleBeans = null;
            testCycleBeans = testCycleService.getCycles(Long.valueOf(testPlan.getId().longValue()));
            for (TestCycleOutputBean testCycleBean : testCycleBeans) {
              if ((!testCycleBean.isAborted()) && (!testCycleBean.isDraft())) {
                cycles.add(testCycleBean.getID());
              }
            }
            
            for (??? = testCycleBeans.iterator(); ???.hasNext();) { testCycleBean = (TestCycleOutputBean)???.next();
              if ((!testCycleBean.isAborted()) && (!testCycleBean.isDraft())) {
                List<TestRunOutputBean> testRuns = testRunService.getTestRuns(Integer.valueOf(testCycleBean.getID().intValue()), ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser());
                if ((testRuns != null) && (testRuns.size() > 0)) {
                  testRunBeans.addAll(testRuns);
                }
              }
            }
            
            if ((!adhocTestRuns.equalsIgnoreCase("")) && (!adhocTestRuns.equalsIgnoreCase("false"))) {
              Object adhocTestRunList = testRunService.getAllAdhocTestRuns(project.getId(), ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser());
              if ((adhocTestRunList != null) && (((List)adhocTestRunList).size() > 0)) {
                for (TestRunOutputBean adhocTestRun : (List)adhocTestRunList) {
                  testRunBeans.add(adhocTestRun);
                }
              }
            }
          } else {
            return 
            
              Response.status(Response.Status.FORBIDDEN).entity(new RESTErrorMessage(i18n.getText("synapse.gadget.error.browse.permission.project"))).build();
          }
        } catch (InvalidDataException e) {
          log.debug(e.getMessage(), e);
          log.error(e.getMessage());
          return Response.serverError().entity(e.getMessage()).build();
        } }
      TestPlanGadgetBean testPlanKey;
      boolean hasPermission;
      Object truns = new HashSet();
      for (TestRunOutputBean runBean : testRunBeans) {
        Timestamp executionTime = runBean.getExecutionOn();
        
        if ((executionTime != null) && (executionTime.after(fromDate)) && (executionTime.before(toDate))) {
          ((Set)truns).add(runBean);
          
          if ((runBean.getTestRunHistory() != null) && (runBean.getTestRunHistory().size() > 0)) {
            for (TestRunHistoryOutputBean runHistory : runBean.getTestRunHistory()) {
              Timestamp historyExecutionTime = runHistory.getExecutionTime();
              if ((historyExecutionTime != null) && (historyExecutionTime.after(fromDate)) && (historyExecutionTime.before(toDate))) {
                ((Set)truns).add(runHistory.getTestRun());
              }
            }
          }
        }
      }
      
      Map<String, Map<RunStatusOutputBean, Integer>> datewiseMap = new HashMap();
      
      datewiseMap = gadgetService.getTestCaseExecutionChartData((Set)truns, datewiseMap, statisticType, groupBy, fromDate, toDate, executors);
      ExecutionTrendData executionTrendBean = new ExecutionTrendData();
      
      List<RunStatusOutputBean> exCategoryStatuses = getExecutedStatuses();
      Calendar c = Calendar.getInstance();
      c.setTime(fromDate);
      executionTrendBean.addMinDate(c.getTime());
      
      Calendar endCal = Calendar.getInstance();
      endCal.setTime(toDate);
      endCal.set(11, 23);
      endCal.set(12, 59);
      endCal.set(13, 59);
      endCal.set(14, 999);
      SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
      
      while (c.getTimeInMillis() < endCal.getTimeInMillis()) {
        date = formatter.format(c.getTime());
        Map<RunStatusOutputBean, Integer> statusMap; Integer total; if (datewiseMap.containsKey(date)) {
          statusMap = (Map)datewiseMap.get(date);
          total = Integer.valueOf(0);
          for (RunStatusOutputBean status : exCategoryStatuses) {
            if (statusMap.containsKey(status)) {
              Integer statusCount = Integer.valueOf(((Integer)statusMap.get(status)).intValue());
              executionTrendBean.addToStatusTrend(status.getName(), new ChartJSDataSet((String)date, statusCount
                .toString()));
              total = Integer.valueOf(total.intValue() + statusCount.intValue());
            } else {
              executionTrendBean.addToStatusTrend(status.getName(), new ChartJSDataSet((String)date, "0"));
            }
          }
        }
        c.add(5, 1);
      }
      
      executionTrendBean.addMaxDate(endCal.getTime());
      
      for (Object date = exCategoryStatuses.iterator(); ((Iterator)date).hasNext();) { RunStatusOutputBean status = (RunStatusOutputBean)((Iterator)date).next();
        executionTrendBean.addToStatusColors(status.getName(), status.getColor());
      }
      
      ExecutionsData userExecutionData = new ExecutionsData();
      userExecutionData.setExecutionTrendData(executionTrendBean);
      
      OverviewData overviewData = new OverviewData();
      overviewData.setExecutionsData(userExecutionData);
      
      return Response.ok(overviewData).build();
    }
    return null;
  }
  
  @GET
  @Path("getTestExecutors")
  public Response getTestExecutors(@QueryParam("projectKey") String projectKey, @QueryParam("periodstartdate") String startDate, @QueryParam("periodenddate") String endDate)
    throws InvalidDataException, ParseException
  {
    log.debug("Fetch Test Executors for the Gadget.");
    Project project = ComponentAccessor.getProjectManager().getProjectObjByKey(projectKey);
    Map<Long, String> testExecutorsMap = new HashMap();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    
    Date fromDate = null;
    Date toDate = null;
    if ((!startDate.equals("null")) && (!startDate.isEmpty()) && (startDate.length() > 0)) {
      fromDate = sdf.parse(startDate);
    }
    if ((!endDate.equals("null")) && (!endDate.isEmpty()) && (endDate.length() > 0)) {
      toDate = sdf.parse(endDate);
    }
    
    if (project != null) {
      testExecutorsMap = gadgetService.getTestExecutors(project.getId(), fromDate, toDate);
    }
    return Response.ok(testExecutorsMap).build();
  }
  
  @GET
  @Path("getTestCaseExecutionGadgetData")
  public Response getTestCaseExecutionGadgetData(@QueryParam("statistictype") String statistictype, @QueryParam("periodstartdate") String startDate, @QueryParam("periodenddate") String endDate) throws InvalidDataException, JSONException
  {
    log.debug("Retrieve execution chart data for Cycle with id ");
    
    JSONObject responseJson = new JSONObject();
    
    if (statistictype.equals("1")) {
      responseJson.put("statistictype", i18n.getText("synapse.gadget.testcase.executions.during.period.executor"));
    } else {
      responseJson.put("statistictype", i18n.getText("synapse.gadget.testcase.executions.during.period.execution"));
    }
    String fromDate = startDate.replaceAll("-", "");
    String toDate = endDate.replaceAll("-", "");
    responseJson.put("startDate", fromDate);
    responseJson.put("endDate", toDate);
    
    return Response.ok(responseJson.toString()).build();
  }
  
  private List<RunStatusOutputBean> getExecutedStatuses() throws InvalidDataException {
    List<RunStatusOutputBean> exCategoryStatuses = new ArrayList();
    Collection<RunStatusOutputBean> runStatuses = runStatusService.getRunStatuses(new Integer[0]);
    for (RunStatusOutputBean runStatus : runStatuses) {
      if ((runStatus.getStatusCategory().getName().equalsIgnoreCase(StatusCategoryEnum.EXECUTED.getName())) && 
        (!runStatus.getName().equalsIgnoreCase(TestRunStatusEnum.NOT_APPLICABLE.getName()))) {
        exCategoryStatuses.add(runStatus);
      }
    }
    
    return exCategoryStatuses;
  }
  
  public boolean hasViewPermission(Project project)
  {
    if (project != null) {
      ApplicationUser user = jiraAuthenticationContext.getLoggedInUser();
      log.debug("Evaluating browse permission for project for the user '" + user + "'");
      
      boolean hasPermission = (globalPermissionManager.hasPermission(GlobalPermissionKey.ADMINISTER, user)) || (permissionManager.hasPermission(ProjectPermissions.BROWSE_PROJECTS, project, user));
      
      log.debug("HasViewPermission? " + hasPermission);
      return hasPermission;
    }
    return false;
  }
  

  public boolean hasSynapseBrowsePermission(Project project)
  {
    if (project != null) {
      ApplicationUser user = jiraAuthenticationContext.getLoggedInUser();
      log.debug("Evaluating browse permission for project for the user '" + user + "'");
      boolean hasPermission = permissionUtil.hasSynapsePermission(project, SynapsePermission.BROWSE_SYNAPSE_PANELS);
      
      log.debug("Has synapse permission? " + hasPermission);
      return hasPermission;
    }
    return false;
  }
  
  private String getFieldNameFromKey(String key)
  {
    String fieldName = new String(key);
    Field fieldObj = ComponentAccessor.getFieldManager().getField(key);
    if (fieldObj != null) {
      fieldName = fieldObj.getName();
    } else {
      CustomField customFieldObj = ComponentAccessor.getCustomFieldManager().getCustomFieldObject(key);
      if (customFieldObj != null) {
        fieldName = customFieldObj.getName();
      }
    }
    return fieldName;
  }
  
  private Map<String, FieldExecutionStatisticsBean> makeSameOrder(Set<String> source, Map<String, FieldExecutionStatisticsBean> target) {
    Map<String, FieldExecutionStatisticsBean> orderedMap = new LinkedHashMap();
    
    Iterator<String> iterator = source.iterator();
    while (iterator.hasNext()) {
      String key = (String)iterator.next();
      if (target.keySet().contains(key)) {
        orderedMap.put(key, target.get(key));
      }
    }
    
    if ((orderedMap.size() != target.size()) && (target.containsKey("-"))) {
      orderedMap.put("-", target.get("-"));
    }
    return orderedMap;
  }
  
  @XmlRootElement
  protected class ExecutionTrendData {
    @XmlElement
    private Map<String, List<ChartJSDataSet>> statusTrend = new LinkedHashMap();
    
    @XmlElement
    private String dateFormat = "YYYY-MM-DD";
    
    @XmlElement
    private Map<String, String> statusColors = new LinkedHashMap();
    @XmlElement
    private Date maxDate;
    @XmlElement
    private Date minDate;
    
    protected ExecutionTrendData() {}
    
    public Map<String, List<ChartJSDataSet>> getStatusTrend()
    {
      return statusTrend;
    }
    
    public void addToStatusTrend(String status, ChartJSDataSet dataSet) {
      List<ChartJSDataSet> statusCharts = new ArrayList();
      if (statusTrend.containsKey(status)) {
        statusCharts = (List)statusTrend.get(status);
      }
      statusCharts.add(dataSet);
      statusTrend.put(status, statusCharts);
    }
    
    public void addAllToStatusTrend(Map<String, List<ChartJSDataSet>> dataSets) {
      for (String status : dataSets.keySet()) {
        List<ChartJSDataSet> dataSetVals = (List)dataSets.get(status);
        if (statusTrend.containsKey(status)) {
          List<ChartJSDataSet> existingVals = (List)statusTrend.get(status);
          existingVals.addAll(dataSetVals);
          statusTrend.put(status, existingVals);
        }
      }
    }
    
    public Map<String, String> getStatusColors() {
      return statusColors;
    }
    
    public void addToStatusColors(String status, String color) {
      statusColors.put(status, color);
    }
    
    public void addMaxDate(Date maxDate) {
      this.maxDate = maxDate;
    }
    
    public void addMinDate(Date minDate) {
      this.minDate = minDate;
    }
    
    public Date getMaxDate() {
      return maxDate;
    }
    
    public Date getMinDate() {
      return minDate;
    }
  }
  
  @XmlRootElement
  protected class ExecutionsData {
    @XmlElement
    private GadgetREST.ExecutionTrendData executionTrendData;
    
    protected ExecutionsData() {}
    
    public GadgetREST.ExecutionTrendData getExecutionTrendData() {
      return executionTrendData;
    }
    
    public void setExecutionTrendData(GadgetREST.ExecutionTrendData executionTrendData) {
      this.executionTrendData = executionTrendData;
    }
  }
  
  @XmlRootElement
  protected class OverviewData {
    @XmlElement
    private GadgetREST.ExecutionsData executionsData;
    
    protected OverviewData() {}
    
    public GadgetREST.ExecutionsData getExecutionsData() {
      return executionsData;
    }
    
    public void setExecutionsData(GadgetREST.ExecutionsData executionsData) {
      this.executionsData = executionsData;
    }
  }
}
