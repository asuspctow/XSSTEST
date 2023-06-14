package com.go2group.synapse.rest;

import com.go2group.synapse.config.SynapseConfig;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.apache.log4j.Logger;




@Path("/common")
@Consumes({"application/json"})
@Produces({"application/json"})
public class SynapseCommonREST
{
  private static final Logger log = Logger.getLogger(SynapseCommonREST.class);
  private final SynapseConfig synapseConfig;
  
  public SynapseCommonREST(SynapseConfig synapseConfig)
  {
    this.synapseConfig = synapseConfig;
  }
  
  @GET
  @Path("synapseProjects")
  public Response getSynapseProjects(@Context HttpServletRequest request) {
    try {
      List<String> configuredProjects = synapseConfig.getPropertyValues("synapse.config.project.mapping");
      if (configuredProjects.contains("-1")) {
        configuredProjects.add("all");
      }
      return Response.ok(configuredProjects).build();
    } catch (Exception e) {
      log.debug(e.getMessage(), e);
      log.error(e.getMessage());
      return Response.serverError().entity(e.getMessage()).build();
    }
  }
}
