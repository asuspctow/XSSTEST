package com.go2group.synapse.rest;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.go2group.synapse.bean.ActiveJobOutputBean;
import com.go2group.synapse.core.exception.InvalidDataException;
import com.go2group.synapse.service.ActiveJobService;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.apache.log4j.Logger;





@Path("importtestcase")
@Consumes({"application/json"})
@Produces({"application/json"})
public class ImportTestCaseRest
{
  private static final Logger log = Logger.getLogger(ImportTestCaseRest.class);
  private ActiveJobService statusService;
  
  public ImportTestCaseRest(ActiveJobService statusService) {
    this.statusService = statusService;
  }
  
  @GET
  @Path("status")
  public Response getImportStatus()
  {
    JiraAuthenticationContext context = ComponentAccessor.getJiraAuthenticationContext();
    log.debug("context : " + context);
    ApplicationUser user = context.getLoggedInUser();
    log.debug("user : " + user);
    String statusKey = "com.go2group.synapse.importtc.status:" + user.getKey();
    log.debug("statusKey : " + statusKey);
    ActiveJobOutputBean actionBean = null;
    try {
      actionBean = statusService.getJobDetails(statusKey);
    } catch (InvalidDataException e) {
      log.debug("Import test case - update status excption : " + e.getMessage(), e);
    }
    JSONObject json = new JSONObject();
    if (actionBean != null)
    {
      log.debug("url : " + actionBean.getUrl());
      json = new JSONObject();
      try {
        json.put("value", Float.valueOf(actionBean.getValue()));
        json.put("backUrl", actionBean.getUrl());
        json.put("filePath", actionBean.getFilePath());
      } catch (JSONException e) {
        log.debug(e.getMessage());
      }
    }
    return Response.ok(json.toString()).build();
  }
  
  private void removeSessionAttributes(HttpSession session) {
    log.debug("Removing session attributes");
    
    session.removeAttribute("customFieldToUpdate");
    session.removeAttribute("issueFieldToUpdate");
    session.removeAttribute("synapseFieldToUpdate");
    session.removeAttribute("customKeyAndDataType");
    session.removeAttribute("csvHeaders");
    session.removeAttribute("file");
    session.removeAttribute("jiraFields");
    session.removeAttribute("fieldMappingValue");
  }
  
  @GET
  @Path("delete")
  public Response deleteImportStatus() { log.debug("delete : starts...");
    JiraAuthenticationContext context = ComponentAccessor.getJiraAuthenticationContext();
    log.debug("context : " + context);
    ApplicationUser user = context.getLoggedInUser();
    log.debug("user : " + user);
    String statusKey = "com.go2group.synapse.importtc.status:" + user.getKey();
    log.debug("statusKey : " + statusKey);
    try {
      statusService.deleteKey(statusKey);
    } catch (InvalidDataException e) {
      log.debug("Import test case - update status excption : " + e.getMessage(), e);
    }
    

    JSONObject json = new JSONObject();
    try {
      json.put("ok", "ok");
    }
    catch (JSONException e) {
      log.debug(e.getMessage());
    }
    return Response.ok(json.toString()).build();
  }
}
