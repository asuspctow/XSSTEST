package com.go2group.synapse.rest;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.datetime.DateTimeStyle;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.VelocityParamFactory;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.velocity.VelocityManager;
import com.go2group.synapse.bean.MultipleDataResponseWrapper;
import com.go2group.synapse.bean.SprintTestCaseDisplayBean;
import com.go2group.synapse.bean.TestRunOutputBean;
import com.go2group.synapse.config.SynapseConfig;
import com.go2group.synapse.core.common.bean.HtmlResponseWrapper;
import com.go2group.synapse.core.util.PermissionUtil;
import com.go2group.synapse.enums.TestRunStatusEnum;
import com.go2group.synapse.service.ConfigService;
import com.go2group.synapse.service.RequirementService;
import com.go2group.synapse.service.TestCaseToRequirementLinkService;
import com.go2group.synapse.service.TestCycleService;
import com.go2group.synapse.service.TestRunRequirementService;
import com.go2group.synapse.service.TestRunService;
import com.go2group.synapse.service.runattribute.RunAttributeService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


@Path("/synapseAgile")
@Produces({"application/json"})
@Consumes({"application/json"})
public class SynapseAgileREST
{
  private static final Logger log = Logger.getLogger(SynapseAgileREST.class);
  
  private static final String AGILE_TEST_RESULTS_DIALOG = "/templates/web/action/agile/agile-test-results-dialog.vm";
  
  private static final String AGILE_TEST_RESULTS_DIALOG_TABLE_TBODY = "/templates/web/action/agile/agile-test-results-dialog-table-tbody.vm";
  
  private static final String AGILE_TEST_RUNS_VIEW = "/templates/web/action/agile/agile-test-runs-view.vm";
  
  private static final String AGILE_TEST_RUNS_VIEW_TABLE_TBODY = "/templates/web/action/agile/agile-test-runs-view-tbody.vm";
  
  private final RequirementService requirementService;
  private final TestRunService testRunService;
  private final IssueManager issueManager;
  private final DateTimeFormatter dateTimeFormatter;
  private final SynapseAgileHelper synapseAgileHelper;
  
  public SynapseAgileREST(@ComponentImport IssueManager issueManager, @ComponentImport DateTimeFormatter dateTimeFormatter, TestCycleService testCycleService, SynapseConfig synapseConfig, ConfigService configService, TestCaseToRequirementLinkService testCaseLinkService, TestRunRequirementService testRunRequirementService, @ComponentImport FieldLayoutManager fieldLayoutManager, @ComponentImport RendererManager rendererManager, @ComponentImport IssueLinkManager issueLinkManager, @ComponentImport I18nHelper i18nHelper, PermissionUtil permissionUtil, RunAttributeService runAttributeService, TestRunService testRunService, RequirementService requirementService)
  {
    this.requirementService = requirementService;
    this.testRunService = testRunService;
    this.issueManager = issueManager;
    this.dateTimeFormatter = dateTimeFormatter.forLoggedInUser().withStyle(DateTimeStyle.COMPLETE);
    synapseAgileHelper = new SynapseAgileHelper(issueManager, dateTimeFormatter, fieldLayoutManager, rendererManager, issueLinkManager, i18nHelper, permissionUtil, testRunService, synapseConfig, configService, testCaseLinkService, testRunRequirementService, runAttributeService);
  }
  
  @GET
  @Path("/getTestCases")
  public Response getTestCases(@QueryParam("requirementKey") String requirementKey, @QueryParam("sprint") String sprint, @QueryParam("pageNumber") String pageNumber, @Context HttpServletRequest request)
  {
    try
    {
      Issue requirment = issueManager.getIssueByCurrentKey(requirementKey);
      if (requirment != null)
      {
        int pageNum = Integer.parseInt(pageNumber);
        int recordsPerPage = Integer.valueOf("100").intValue();
        
        List<SprintTestCaseDisplayBean> sprintTestCases = requirementService.getSprintTestCases(requirment.getId(), pageNum, recordsPerPage);
        
        Map<String, Object> velocityParams = ComponentAccessor.getVelocityParamFactory().getDefaultVelocityParams();
        velocityParams.put("sprint", sprint);
        
        List<Long> sprintIds = new ArrayList();
        if ((StringUtils.isNotBlank(sprint)) && (!sprint.equalsIgnoreCase("undefined"))) {
          String[] sprints = sprint.split(",");
          for (String s : sprints) {
            sprintIds.add(Long.valueOf(s));
          }
        }
        
        int recordCount = 0;
        if ((sprintTestCases != null) && (sprintTestCases.size() > 0)) {
          for (??? = sprintTestCases.iterator(); ((Iterator)???).hasNext();) { sprintCase = (SprintTestCaseDisplayBean)((Iterator)???).next();
            recordCount = ((SprintTestCaseDisplayBean)sprintCase).getTotalRecords();
            TestRunOutputBean testRun = null;
            if (sprintIds.size() > 0) {
              testRun = testRunService.getLatestTestRunInSprint(((SprintTestCaseDisplayBean)sprintCase).getTestCaseId(), sprintIds);
            } else {
              testRun = testRunService.getLatestTestRunForTestCase(((SprintTestCaseDisplayBean)sprintCase).getTestCaseId(), null, ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser());
            }
            if (testRun != null) {
              ((SprintTestCaseDisplayBean)sprintCase).setSprintLatestStatus(testRun.getLocalizedStatus());
              if (testRun.getExecutionTime() != null) {
                ((SprintTestCaseDisplayBean)sprintCase).setExecutedOn(dateTimeFormatter.format(testRun.getExecutionTime()));
              }
            }
          }
          Object statusLozengeMap = new HashMap();
          for (Object sprintCase = TestRunStatusEnum.valuesList().iterator(); ((Iterator)sprintCase).hasNext();) { TestRunStatusEnum statusLoz = (TestRunStatusEnum)((Iterator)sprintCase).next();
            ((Map)statusLozengeMap).put(statusLoz.getLocalizedName(), statusLoz.getColor());
          }
          velocityParams.put("stausMap", statusLozengeMap);
          velocityParams.put("sprintTestCases", sprintTestCases);
        }
        velocityParams.put("contextPath", request.getContextPath());
        velocityParams.put("i18n", ComponentAccessor.getJiraAuthenticationContext().getI18nHelper());
        velocityParams.put("sprint", sprint);
        velocityParams.put("issueKey", requirementKey);
        VelocityManager vm = ComponentAccessor.getVelocityManager();
        String html = "";
        if (pageNum == 1) {
          html = vm.getEncodedBody("", "/templates/web/action/agile/agile-test-results-dialog.vm", null, velocityParams);
        } else {
          html = vm.getEncodedBody("", "/templates/web/action/agile/agile-test-results-dialog-table-tbody.vm", null, velocityParams);
        }
        
        if (pageNum == 0) {
          pageNum = recordCount / recordsPerPage + 1;
        }
        int remRecords = recordCount - pageNum * recordsPerPage;
        
        MultipleDataResponseWrapper responseWrapper = new MultipleDataResponseWrapper(html);
        responseWrapper.setTotalRecordsMessage("" + recordCount);
        responseWrapper.setTotalRecords(recordCount);
        responseWrapper.setPageNumber(pageNum + 1);
        
        switch (pageNum) {
        case 1: 
          if (remRecords <= 0) {
            responseWrapper.setRemainingRecords(0);
          } else {
            responseWrapper.setRemainingRecords(remRecords);
          }
          break;
        default: 
          if (remRecords <= 0) {
            responseWrapper.setRemainingRecords(0);
          } else
            responseWrapper.setRemainingRecords(remRecords);
          break;
        }
        if (remRecords <= 0) {
          responseWrapper.setShowAll(false);
          if (pageNum == 1) {
            responseWrapper.setShowHide(false);
          } else {
            responseWrapper.setShowHide(true);
          }
        }
        else if (remRecords <= recordsPerPage) {
          responseWrapper.setShowAll(false);
          responseWrapper.setShowHide(false);
        } else {
          responseWrapper.setShowAll(true);
          responseWrapper.setShowHide(false);
        }
        
        return Response.ok(responseWrapper).build();
      }
    } catch (Exception e) {
      log.debug(e.getMessage(), e);
      log.error(e.getMessage());
    }
    return Response.ok("").build();
  }
  
  @GET
  @Path("/getTestRuns")
  public Response getTestRuns(@QueryParam("testCaseKey") String testCaseKey, @QueryParam("sprint") String sprint, @QueryParam("pageNumber") String pageNumber, @Context HttpServletRequest request)
  {
    try {
      Issue testCase = issueManager.getIssueByCurrentKey(testCaseKey);
      ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
      if (testCase != null) {
        List<Long> sprintIds = new ArrayList();
        if ((StringUtils.isNotBlank(sprint)) && (!sprint.equalsIgnoreCase("undefined"))) {
          String[] sprints = sprint.split(",");
          for (String s : sprints) {
            sprintIds.add(Long.valueOf(s));
          }
        }
        
        int pageNum = Integer.parseInt(pageNumber);
        int recordsPerPage = Integer.valueOf("5").intValue();
        
        int recordCount = 0;
        Object testRuns = null;
        if (sprintIds.size() > 0) {
          testRuns = testRunService.getSprintRuns(sprintIds, testCase.getId(), pageNum, recordsPerPage, user);
          recordCount = testRunService.getSprintRunsCount(sprintIds, testCase.getId()).intValue();
        } else {
          testRuns = testRunService.getTestRunsOrdered(testCase.getId(), "id", true, pageNum, recordsPerPage, ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser());
          recordCount = testRunService.getTestRunCountFromTestCase(testCase.getId(), false).intValue();
        }
        Map<String, Object> velocityParams = ComponentAccessor.getVelocityParamFactory().getDefaultVelocityParams();
        velocityParams.put("testRuns", testRuns);
        velocityParams.put("testCase", testCase);
        velocityParams.put("sprint", sprint);
        velocityParams.put("contextPath", request.getContextPath());
        velocityParams.put("i18n", ComponentAccessor.getJiraAuthenticationContext().getI18nHelper());
        velocityParams.put("dateTimeFormatter", dateTimeFormatter);
        velocityParams.put("baseUrl", ComponentAccessor.getApplicationProperties().getString("jira.baseurl"));
        
        VelocityManager vm = ComponentAccessor.getVelocityManager();
        String html = "";
        if (pageNum == 1) {
          html = vm.getEncodedBody("", "/templates/web/action/agile/agile-test-runs-view.vm", null, velocityParams);
        } else {
          html = vm.getEncodedBody("", "/templates/web/action/agile/agile-test-runs-view-tbody.vm", null, velocityParams);
        }
        if (pageNum == 0) {
          pageNum = recordCount / recordsPerPage + 1;
        }
        int remRecords = recordCount - pageNum * recordsPerPage;
        
        MultipleDataResponseWrapper responseWrapper = new MultipleDataResponseWrapper(html);
        responseWrapper.setTotalRecords(recordCount);
        responseWrapper.setPageNumber(pageNum + 1);
        
        switch (pageNum) {
        case 1: 
          if (remRecords <= 0) {
            responseWrapper.setRemainingRecords(0);
          } else {
            responseWrapper.setRemainingRecords(remRecords);
          }
          break;
        default: 
          if (remRecords <= 0) {
            responseWrapper.setRemainingRecords(0);
          } else
            responseWrapper.setRemainingRecords(remRecords);
          break;
        }
        if (remRecords <= 0) {
          responseWrapper.setShowAll(false);
          if (pageNum == 1) {
            responseWrapper.setShowHide(false);
          } else {
            responseWrapper.setShowHide(true);
          }
        }
        else if (remRecords <= recordsPerPage) {
          responseWrapper.setShowAll(false);
          responseWrapper.setShowHide(false);
        } else {
          responseWrapper.setShowAll(true);
          responseWrapper.setShowHide(false);
        }
        
        return Response.ok(responseWrapper).build();
      }
    } catch (Exception e) {
      log.debug(e.getMessage(), e);
      log.error(e.getMessage());
    }
    return Response.ok("").build();
  }
  
  @GET
  @Path("/getTestRunViewHtml")
  public Response getTestRunViewHtml(@QueryParam("runId") Integer runId, @QueryParam("referrer") String referrer, @Context HttpServletRequest request) {
    String html = synapseAgileHelper.getTestRunViewHtml(runId, request.getContextPath(), referrer);
    return Response.ok(new HtmlResponseWrapper(html)).build();
  }
  
  @GET
  @Path("/getTestRunRowHtml")
  public Response getTestRunRowHtml(@QueryParam("runId") Integer runId, @QueryParam("referrer") String referrer, @Context HttpServletRequest request) {
    String html = synapseAgileHelper.getTestRunRowViewHtml(runId, request.getContextPath());
    return Response.ok(new HtmlResponseWrapper(html)).build();
  }
}
