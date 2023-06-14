package com.go2group.synapse.rest;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.go2group.synapse.config.SynapseConfig;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.apache.log4j.Logger;




@Path("synapseIssueIdentifier")
@Consumes({"application/json"})
@Produces({"application/json"})
public class SynapseIssueIdentifierREST
{
  private static final Logger log = Logger.getLogger(SynapseIssueIdentifierREST.class);
  private IssueManager issueManager;
  private final SynapseConfig synapseConfig;
  
  public SynapseIssueIdentifierREST(@ComponentImport IssueManager issueManager, SynapseConfig synapseConfig) {
    this.synapseConfig = synapseConfig;
    this.issueManager = issueManager;
  }
  
  @GET
  @Path("getSynapseIssueType")
  public Response getSynapseIssueType(@QueryParam("issuekey") String issuekey) {
    boolean isTestCase = false;
    boolean isRequirement = false;
    
    JSONObject json = new JSONObject();
    try {
      log.debug("REST getIsTestCase invoked with the issuekey " + issuekey);
      Issue issue = issueManager.getIssueObject(issuekey);
      log.debug("Issue in Context " + issue);
      
      if (issue != null) {
        isTestCase = synapseConfig.getIssueTypeIds("Test Case").contains(issue.getIssueType().getId());
        
        log.debug("Issue in context is Test case? " + isTestCase);
        
        json.put("isTestCase", isTestCase);
        isRequirement = synapseConfig.getIssueTypeIds("Requirement").contains(issue.getIssueType().getId());
        log.debug("Issue in context is Requirement? " + isRequirement);
        json.put("isRequirement", isRequirement);
      }
      else {
        log.debug("Returning the boolean true as the issue object is null, so that the drop down list is not removed. ");
        json.put("isTestCase", true);
        json.put("isRequirement", true);
        return Response.ok(json.toString()).build();
      }
    }
    catch (Exception e) {
      log.debug(e.getMessage(), e);
      log.error(e.getMessage());
    }
    


    return Response.ok(json.toString()).build();
  }
}
