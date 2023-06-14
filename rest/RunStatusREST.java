package com.go2group.synapse.rest;

import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.go2group.synapse.core.common.bean.HtmlResponseWrapper;
import com.go2group.synapse.core.exception.InvalidDataException;
import com.go2group.synapse.service.RunStatusService;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.apache.log4j.Logger;


@Path("/runStatus")
@Consumes({"application/json"})
@Produces({"application/json"})
public class RunStatusREST
{
  private static Logger log = Logger.getLogger(RunStatusREST.class);
  private RunStatusService runStatusService;
  
  public RunStatusREST(RunStatusService runStatusService)
  {
    this.runStatusService = runStatusService;
  }
  
  @Path("changeRunStatus")
  @PUT
  public Response changeRunStatus(String data) {
    try {
      JSONObject jsonObject = new JSONObject(data);
      Integer statusId = Integer.valueOf(jsonObject.getInt("statusId"));
      boolean isEnabled = jsonObject.getBoolean("isEnabled");
      
      runStatusService.toggleRunStatus(statusId, isEnabled);
      return Response.ok(new HtmlResponseWrapper("success")).build();
    } catch (InvalidDataException e) {
      log.debug(e.getMessage(), e);
      log.error(e.getMessage());
      return Response.serverError().entity(e.getMessage()).build();
    } catch (JSONException e) {
      log.debug(e.getMessage(), e);
      log.error(e.getMessage());
      return Response.serverError().entity(e.getMessage()).build();
    }
  }
  
  @Path("reorderRunStatus")
  @POST
  public Response reorderRunStatus(@QueryParam("runStatusId") Integer runStatusId, @QueryParam("refRunStatusId") Integer refRunStatusId)
  {
    log.debug("reorder RunStatus - starts " + runStatusId + " refRunStatusId " + refRunStatusId);
    refRunStatusId = (refRunStatusId == null) || (refRunStatusId.intValue() == -1) ? null : refRunStatusId;
    try {
      runStatusService.reorderRunStatus(runStatusId, refRunStatusId);
    } catch (Exception e) {
      log.debug(e.getMessage(), e);
      log.error(e.getMessage());
    }
    return Response.ok().build();
  }
}
