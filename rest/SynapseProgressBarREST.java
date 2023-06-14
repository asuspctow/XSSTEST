package com.go2group.synapse.rest;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.go2group.synapse.bean.ActiveJobOutputBean;
import com.go2group.synapse.core.exception.InvalidDataException;
import com.go2group.synapse.service.ActiveJobService;
import java.text.DecimalFormat;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.apache.log4j.Logger;





@Path("progressbar")
@Consumes({"application/json"})
@Produces({"application/json"})
public class SynapseProgressBarREST
{
  private static final Logger log = Logger.getLogger(SynapseProgressBarREST.class);
  private final ActiveJobService statusService;
  
  public SynapseProgressBarREST(ActiveJobService statusService) {
    this.statusService = statusService;
  }
  
  @GET
  @Path("getProgress")
  public Response getProgress(@QueryParam("progressEntityKey") String progressEntityKey)
  {
    JiraAuthenticationContext jiraAuthenticationContext = ComponentAccessor.getJiraAuthenticationContext();
    ApplicationUser user = jiraAuthenticationContext.getLoggedInUser();
    log.debug("user : " + user);
    log.debug("progressEntityKey : " + progressEntityKey);
    ActiveJobOutputBean actionBean = null;
    JSONObject json = new JSONObject();
    try {
      actionBean = statusService.getJobDetails(progressEntityKey);
      if (actionBean != null) {
        json.put("value", Float.valueOf(actionBean.getValue()));
        json.put("backUrl", actionBean.getUrl());
        json.put("filePath", actionBean.getFilePath());
        json.put("param1", actionBean.getParam1());
        json.put("param2", actionBean.getParam2());
        DecimalFormat df = new DecimalFormat("#");
        json.put("percent", df.format(Float.valueOf(actionBean.getValue()).floatValue() * 100.0F));
      }
    } catch (InvalidDataException|NumberFormatException|JSONException e) {
      log.debug(e.getMessage(), e);
      log.error(e.getMessage());
    }
    return Response.ok(json.toString()).build();
  }
  
  @DELETE
  @Path("deleteProgressEntityKey")
  public Response deleteProgressEntityKey(@QueryParam("progressEntityKey") String progressEntityKey) {
    JiraAuthenticationContext jiraAuthenticationContext = ComponentAccessor.getJiraAuthenticationContext();
    ApplicationUser user = jiraAuthenticationContext.getLoggedInUser();
    log.debug("user : " + user);
    log.debug("statusKey : " + progressEntityKey);
    JSONObject json = new JSONObject();
    try {
      statusService.deleteKey(progressEntityKey);
      json.put("ok", "ok");
    }
    catch (InvalidDataException|JSONException e) {
      log.debug(e.getMessage(), e);
      log.error(e.getMessage());
    }
    return Response.ok(json.toString()).build();
  }
}
