package com.go2group.synapse.rest;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.json.JSONArray;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.go2group.synapse.bean.HtmlResponseWrapper;
import com.go2group.synapse.bean.MultipleDataResponseWrapper;
import com.go2group.synapse.bean.TestRunOutputBean;
import com.go2group.synapse.config.SynapseConfig;
import com.go2group.synapse.constant.SynapsePermission;
import com.go2group.synapse.core.exception.InvalidDataException;
import com.go2group.synapse.manager.PermissionUtilAbstract;
import com.go2group.synapse.service.RequirementService;
import com.go2group.synapse.service.TestCaseToRequirementLinkService;
import com.go2group.synapse.service.TestPlanMemberService;
import com.go2group.synapse.service.TestRunRequirementService;
import com.go2group.synapse.service.TestRunService;
import com.go2group.synapse.service.runattribute.RunAttributeService;
import com.go2group.synapse.util.PluginUtil;
import com.go2group.synapse.web.panel.RequirementMemberHelper;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;






@Path("requirementTestCase")
@Consumes({"application/json"})
@Produces({"application/json"})
public class RequirementTestCaseREST
{
  private RequirementService requirementService;
  private final TestCaseToRequirementLinkService tcrLinkService;
  private I18nHelper i18n;
  private IssueManager issueManager;
  private ApplicationProperties applicationProperties;
  private static final Logger log = Logger.getLogger(RequirementTestCaseREST.class);
  
  private TestRunService tRunService;
  
  private TestPlanMemberService tpMemberService;
  
  private final TestRunRequirementService testRunRequirementService;
  private final PermissionUtilAbstract permissionUtil;
  private final SynapseConfig synapseConfig;
  private final RunAttributeService runAttributeService;
  
  @Autowired
  public RequirementTestCaseREST(@ComponentImport IssueManager issueManager, @ComponentImport I18nHelper i18n, @ComponentImport ApplicationProperties applicationProperties, PermissionUtilAbstract permissionUtil, RequirementService requirementService, TestCaseToRequirementLinkService tcrLinkService, TestRunService tRunService, TestPlanMemberService tpMemberService, SynapseConfig synapseConfig, TestRunRequirementService testRunRequirementService, RunAttributeService runAttributeService)
  {
    this.permissionUtil = permissionUtil;
    this.requirementService = requirementService;
    this.tcrLinkService = tcrLinkService;
    this.tRunService = tRunService;
    this.tpMemberService = tpMemberService;
    this.issueManager = issueManager;
    this.i18n = i18n;
    this.applicationProperties = applicationProperties;
    this.synapseConfig = synapseConfig;
    this.testRunRequirementService = testRunRequirementService;
    this.runAttributeService = runAttributeService;
  }
  

  @GET
  @Path("getRelatedTestCases")
  public Response getRelatedTestCases(@QueryParam("currentReqKey") String currentReqKey)
  {
    String html = "";
    int recordCount = 0;
    
    try
    {
      Issue requirement = issueManager.getIssueByCurrentKey(currentReqKey);
      List<Issue> testCases = requirementService.getTestCases(requirement.getId());
      int defaultDisplayLength = Integer.valueOf(synapseConfig.getPropertyValue("25")).intValue();
      
      Map<Issue, List<Issue>> tcAndReqMap = new LinkedHashMap();
      tcAndReqMap = requirementService.getAllTransitiveTestCasesWithRequirement(requirement, tcAndReqMap);
      if (testCases != null) {
        recordCount = testCases.size();
        List<Issue> transitiveTestCases = new ArrayList(tcAndReqMap.keySet());
        if ((transitiveTestCases != null) && (transitiveTestCases.size() > 0)) {
          transitiveTestCases.removeAll(testCases);
          recordCount += transitiveTestCases.size();
        }
        boolean readOnly = isReadOnly(requirement);
        
        boolean hasPermisssion = permissionUtil.hasSynapsePermission(requirement.getProjectObject(), SynapsePermission.MANAGE_REQUIREMENTS);
        
        html = RequirementMemberHelper.getRequirementTestCasesHtml(requirement, testCases, tcAndReqMap, applicationProperties, i18n, readOnly, hasPermisssion, defaultDisplayLength);
      } else {
        html = "";
      }
    }
    catch (Exception excep) {
      log.debug(excep.getMessage(), excep);
      log.error(excep.getMessage());
      return Response.serverError().entity(excep.getMessage()).build();
    }
    MultipleDataResponseWrapper responseWrapper = new MultipleDataResponseWrapper(html);
    responseWrapper.setTotalRecordsMessage(i18n.getText("synapse.web.panel.label.reqtc.total") + " " + recordCount);
    responseWrapper.setTotalRecords(recordCount);
    return Response.ok(responseWrapper).build();
  }
  
  @GET
  @Path("getRelatedDefects")
  public Response getRelatedDefects(@QueryParam("currentReqKey") String currentReqKey, @Context HttpServletRequest request) {
    String html = "";
    try {
      Issue currentRequirement = issueManager.getIssueByCurrentKey(currentReqKey);
      if (currentRequirement != null) {
        List<Issue> defects = requirementService.getDefects(currentRequirement.getId());
        int defaultDisplayLength = Integer.valueOf(synapseConfig.getPropertyValue("10")).intValue();
        String contextPath = request.getContextPath();
        html = RequirementMemberHelper.getRequirementDefectsHtml(defects, currentRequirement, i18n, defaultDisplayLength, contextPath);
      }
    } catch (Exception excep) {
      log.debug(excep.getMessage(), excep);
      log.error(excep.getMessage());
      return Response.serverError().entity(excep.getMessage()).build();
    }
    return Response.ok(new HtmlResponseWrapper(html)).build();
  }
  
  private boolean isReadOnly(Issue issue) {
    return (!PluginUtil.hasValidLicense()) || (!permissionUtil.hasEditPermission(issue));
  }
  
  @POST
  @Path("addTestCase")
  public Response addTestCase(String data) {
    String html = "success";
    try {
      JSONObject jsonObject = new JSONObject(data);
      String currentReqkey = jsonObject.getString("currentReqkey");
      JSONArray testCaseKeys = jsonObject.getJSONArray("testCaseKeys");
      String history = jsonObject.getString("history");
      
      if ((testCaseKeys != null) && (currentReqkey != null)) {
        Issue requirement = issueManager.getIssueByCurrentKey(currentReqkey);
        if (requirement != null) {
          List<String> requirementKeys = new ArrayList();
          requirementKeys.add(currentReqkey);
          
          for (int index = 0; index < testCaseKeys.length(); index++) {
            String testCaseKey = testCaseKeys.getString(index);
            tcrLinkService.linkRequirement(testCaseKey, requirementKeys, ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser(), "add-test-case", true);
            
            if ((StringUtils.isNotBlank(history)) && ("yes".equalsIgnoreCase(history))) {
              Issue testCase = issueManager.getIssueByCurrentKey(testCaseKey);
              if (testCase != null) {
                List<TestRunOutputBean> testRuns = tRunService.getTestRuns(testCase.getId(), ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser());
                if ((testRuns != null) && (testRuns.size() > 0)) {
                  for (TestRunOutputBean testRun : testRuns) {
                    testRunRequirementService.createTestRunRequirement(testRun.getID(), requirement.getId());
                  }
                }
              }
            }
          }
        }
      }
    } catch (JSONException jsonExcep) {
      log.debug(jsonExcep.getMessage(), jsonExcep);
      log.error(jsonExcep.getMessage());
      return Response.serverError().entity(jsonExcep.getMessage()).build();
    } catch (NumberFormatException e) {
      html = "failed";
      log.debug(e.getMessage(), e);
      log.error(e.getMessage());
      return Response.serverError().entity(e.getMessage()).build();
    } catch (InvalidDataException e) {
      html = "failed";
      log.debug(e.getMessage(), e);
      log.error(e.getMessage());
      return Response.serverError().entity(e.getMessage()).build();
    }
    
    return Response.ok(new HtmlResponseWrapper(html)).build();
  }
  
  @POST
  @Path("deleteTestCase")
  public Response deleteTestCase(String data) {
    String html = "success";
    try {
      JSONObject jsonObject = new JSONObject(data);
      String currentReqkey = jsonObject.getString("currentReqkey");
      String testCaseKey = jsonObject.getString("testCaseKey");
      
      if ((testCaseKey != null) && (currentReqkey != null)) {
        ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
        tcrLinkService.delinkRequirement(testCaseKey, currentReqkey, true, user);
        
        Issue testCase = issueManager.getIssueByCurrentKey(testCaseKey);
        if (testCase != null) {
          requirement = issueManager.getIssueByCurrentKey(currentReqkey);
          if (requirement != null) {
            List<TestRunOutputBean> testRuns = tRunService.getTestRuns(testCase.getId(), ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser());
            if ((testRuns != null) && (testRuns.size() > 0)) {
              for (TestRunOutputBean testRun : testRuns)
                testRunRequirementService.deleteTestRunRequirement(testRun.getID(), requirement.getId());
            }
          }
        }
      }
    } catch (JSONException jsonExcep) {
      Issue requirement;
      log.debug(jsonExcep.getMessage(), jsonExcep);
      log.error(jsonExcep.getMessage());
      return Response.serverError().entity(jsonExcep.getMessage()).build();
    } catch (NumberFormatException e) {
      html = "failed";
      log.debug(e.getMessage(), e);
      log.error(e.getMessage());
      return Response.serverError().entity(e.getMessage()).build();
    } catch (InvalidDataException e) {
      html = "failed";
      log.debug(e.getMessage(), e);
      log.error(e.getMessage());
      return Response.serverError().entity(e.getMessage()).build();
    }
    
    return Response.ok(new HtmlResponseWrapper(html)).build();
  }
  
  @GET
  @Path("getTestCaseCycleDetails")
  public Response getTestCaseCycleDetails(@QueryParam("requirementId") String requirementId, @QueryParam("testCaseId") String testCaseId, @Context HttpServletRequest request) {
    String html = "";
    try {
      Issue requirement = issueManager.getIssueObject(Long.valueOf(requirementId));
      Issue testCase = issueManager.getIssueObject(Long.valueOf(testCaseId));
      if (testCase != null) {
        html = new RequirementMemberHelper().getTestCycleSummaryHtml(requirement, testCase, tRunService, testRunRequirementService, tpMemberService, runAttributeService, i18n, request);
      } else {
        html = "";
      }
    }
    catch (Exception excep) {
      log.debug(excep.getMessage(), excep);
      log.error(excep.getMessage());
      return Response.serverError().entity(excep.getMessage()).build();
    }
    return Response.ok(new HtmlResponseWrapper(html)).build();
  }
}
