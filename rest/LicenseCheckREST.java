package com.go2group.synapse.rest;

import com.atlassian.jira.util.json.JSONObject;
import com.go2group.synapse.util.PluginUtil;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.apache.log4j.Logger;



@Path("licenseCheck")
@Consumes({"application/json"})
@Produces({"application/json"})
public class LicenseCheckREST
{
  private static final Logger log = Logger.getLogger(LicenseCheckREST.class);
  
  public LicenseCheckREST() {}
  
  @GET
  @Path("validate")
  public Response validate() {
    try {
      JSONObject json = new JSONObject();
      json.put("validLicense", PluginUtil.hasValidLicense());
      
      return Response.ok(json.toString()).build();
    } catch (Exception e) {
      log.error(e.getMessage());
      log.debug(e.getMessage(), e);
    }
    return Response.serverError().build();
  }
}
