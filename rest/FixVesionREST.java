package com.go2group.synapse.rest;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.util.json.JSONArray;
import java.util.Collection;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.apache.commons.lang3.StringUtils;

@Path("fixVersions")
@javax.ws.rs.Consumes({"application/json"})
@Produces({"application/json"})
public class FixVesionREST
{
  public FixVesionREST() {}
  
  @GET
  @Path("fetchFixVersions")
  public Response doActivity(@QueryParam("projectKey") String projectKey)
  {
    JSONArray array = new JSONArray();
    Collection<Version> versions = null;
    Project project; if (StringUtils.isNotBlank(projectKey)) {
      project = ComponentAccessor.getProjectManager().getProjectByCurrentKey(projectKey);
      if (project != null) {
        versions = ComponentAccessor.getVersionManager().getVersions(project);
      }
    } else {
      versions = ComponentAccessor.getVersionManager().getAllVersions();
    }
    for (Version version : versions) {
      array.put(version.getName());
    }
    return Response.ok(array.toString()).build();
  }
}
