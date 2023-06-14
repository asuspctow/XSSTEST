package com.go2group.synapse.rest;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.go2group.synapse.bean.RequirementCloneParamBean;
import com.go2group.synapse.bean.TestCaseCloneParamBean;
import com.go2group.synapse.bean.TestPlanCloneParamBean;
import com.go2group.synapse.core.exception.InvalidDataException;
import com.go2group.synapse.service.ActiveJobService;
import com.go2group.synapse.service.RequirementService;
import com.go2group.synapse.service.SynapseIssueCloneService;
import com.go2group.synapse.service.TestCaseToRequirementLinkService;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;



@Path("/synapseClone")
@Produces({"application/json"})
@Consumes({"application/json"})
public class CloneREST
{
  private static final Logger log = Logger.getLogger(CloneREST.class);
  
  private final SynapseIssueCloneService synapseIssueCloneService;
  private final RequirementService requirementService;
  private final TestCaseToRequirementLinkService testCaseService;
  private final IssueManager issueManager;
  private final ActiveJobService activeJobService;
  
  @Autowired
  public CloneREST(@ComponentImport IssueManager issueManager, SynapseIssueCloneService synapseIssueCloneService, RequirementService requirementService, TestCaseToRequirementLinkService testCaseService, ActiveJobService activeJobService)
  {
    this.synapseIssueCloneService = synapseIssueCloneService;
    this.requirementService = requirementService;
    this.testCaseService = testCaseService;
    this.issueManager = issueManager;
    this.activeJobService = activeJobService;
  }
  
  @POST
  @Path("/clone")
  public Response clone(String data)
  {
    JSONObject output = new JSONObject();
    if (data != null) {
      try {
        JSONObject jsonObject = new JSONObject(data);
        String cloneEntity = jsonObject.getString("cloneEntity");
        String newSummary = jsonObject.getString("summary");
        String progressKey = jsonObject.getString("progressKey");
        
        boolean attachments = jsonObject.getBoolean("attachments");
        boolean subtasks = jsonObject.getBoolean("subtasks");
        boolean links = jsonObject.getBoolean("links");
        boolean sprints = jsonObject.getBoolean("sprints");
        boolean customFields = jsonObject.getBoolean("customFields");
        
        boolean isPrefix = Boolean.valueOf(jsonObject.getString("isPrefix")).booleanValue();
        
        String srcIssueKey = jsonObject.getString("srcIssueKey");
        
        if (StringUtils.isNotBlank(srcIssueKey)) {
          Issue issue = issueManager.getIssueObject(srcIssueKey);
          if (issue != null) {
            try {
              activeJobService.createKey(progressKey, null);
            } catch (InvalidDataException ???) {
              log.debug("move test suite -  excption : " + ???.getMessage(), ???);
            }
            switch (cloneEntity)
            {




            case "REQUIREMET": 
              boolean childRequirements = jsonObject.getBoolean("childRequirement");
              boolean testCases = jsonObject.getBoolean("testCases");
              RequirementCloneParamBean params = new RequirementCloneParamBean();
              params.setCloneEntity(cloneEntity);
              params.setIssue(issue);
              params.setUser(ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser());
              params.setNewSummary(newSummary);
              params.setPrefix(isPrefix);
              
              params.setAttachments(attachments);
              params.setSubtasks(subtasks);
              params.setLinks(links);
              params.setSprints(sprints);
              params.setCustomFields(customFields);
              
              params.setChildRequirements(childRequirements);
              params.setTestCases(testCases);
              
              params.setRequirementService(requirementService);
              params.setTestCaseService(testCaseService);
              

              String newIssueKey = synapseIssueCloneService.clone(params, progressKey);
              output.put("newIssueKey", newIssueKey);
              
              break;
            




            case "TEST_CASE": 
              boolean requirementsEnabled = jsonObject.getBoolean("requirements");
              boolean testSuitesEnabled = jsonObject.getBoolean("testSuites");
              
              TestCaseCloneParamBean testCaseParams = new TestCaseCloneParamBean();
              testCaseParams.setCloneEntity(cloneEntity);
              testCaseParams.setIssue(issue);
              testCaseParams.setUser(ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser());
              testCaseParams.setNewSummary(newSummary);
              testCaseParams.setPrefix(isPrefix);
              
              testCaseParams.setAttachments(attachments);
              testCaseParams.setSubtasks(subtasks);
              testCaseParams.setLinks(links);
              testCaseParams.setSprints(sprints);
              testCaseParams.setCustomFields(customFields);
              
              testCaseParams.setRequirements(requirementsEnabled);
              testCaseParams.setTestSuites(testSuitesEnabled);
              
              String newTestCaseIssueKey = synapseIssueCloneService.clone(testCaseParams, progressKey);
              output.put("newIssueKey", newTestCaseIssueKey);
              
              break;
            




            case "TEST_PLAN": 
              boolean cyclesEnabled = jsonObject.getBoolean("cycles");
              boolean testersEnabled = jsonObject.getBoolean("testers");
              
              TestPlanCloneParamBean testPlanParams = new TestPlanCloneParamBean();
              testPlanParams.setCloneEntity(cloneEntity);
              testPlanParams.setIssue(issue);
              testPlanParams.setUser(ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser());
              testPlanParams.setNewSummary(newSummary);
              testPlanParams.setPrefix(isPrefix);
              
              testPlanParams.setAttachments(attachments);
              testPlanParams.setSubtasks(subtasks);
              testPlanParams.setLinks(links);
              testPlanParams.setSprints(sprints);
              testPlanParams.setCustomFields(customFields);
              
              testPlanParams.setCycles(cyclesEnabled);
              testPlanParams.setTesters(testersEnabled);
              
              String newTestPlanIssueKey = synapseIssueCloneService.clone(testPlanParams, progressKey);
              output.put("newIssueKey", newTestPlanIssueKey);
            }
            
          }
        }
      }
      catch (Exception e) {
        log.error(e.getMessage());
        log.debug(e.getMessage(), e);
      }
    }
    return Response.ok(output.toString()).build();
  }
}
