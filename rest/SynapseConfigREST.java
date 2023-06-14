package com.go2group.synapse.rest;

import com.go2group.synapse.bean.PermissionEntityInputBean;
import com.go2group.synapse.core.exception.InvalidDataException;
import com.go2group.synapse.service.ConfigService;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.apache.log4j.Logger;



@Path("config")
@Consumes({"application/json"})
@Produces({"application/json"})
public class SynapseConfigREST
{
  private static final Logger log = Logger.getLogger(SynapseConfigREST.class);
  private final ConfigService configService;
  
  public SynapseConfigREST(ConfigService configService)
  {
    this.configService = configService;
  }
  
  @POST
  @Path("updatePermission")
  public Response updatePermission(PermissionEntityInputBean permissionBean)
  {
    log.debug("Updating Permission configuration");
    try
    {
      configService.updatePermission(permissionBean);
      return Response.ok().build();
    } catch (InvalidDataException e) {
      log.debug(e.getMessage(), e);
      log.error(e.getMessage());
      return Response.serverError().entity(e.getMessage()).build();
    }
  }
}
