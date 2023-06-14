package com.go2group.synapse.rest.pub;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.project.Project;
import com.go2group.synapse.bean.RESTErrorMessage;
import com.go2group.synapse.bean.RESTSuccessMessage;
import com.go2group.synapse.constant.SynapsePermission;
import com.go2group.synapse.enums.SynapseResponseStatusEnum;
import com.go2group.synapse.manager.PermissionUtilAbstract;
import com.go2group.synapse.util.PluginUtil;
import java.util.List;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

public abstract class AbstractPublicREST
{
  protected final PermissionUtilAbstract permissionUtil;
  
  protected AbstractPublicREST(PermissionUtilAbstract permissionUtil)
  {
    this.permissionUtil = permissionUtil;
  }
  
  protected boolean hasValidLicense() {
    return PluginUtil.hasValidLicense();
  }
  
  protected boolean hasViewPermission(Issue issue) {
    return permissionUtil.hasViewPermission(issue);
  }
  
  protected boolean hasEditPermission(Issue issue) {
    return permissionUtil.hasEditPermission(issue);
  }
  
  protected boolean hasSynapsePermission(Project project, SynapsePermission permission) {
    return permissionUtil.hasSynapsePermission(project, permission);
  }
  
  protected Response forbidden() {
    return Response.status(Response.Status.FORBIDDEN).build();
  }
  
  protected Response forbidden(String message) {
    return Response.status(Response.Status.FORBIDDEN).entity(new RESTErrorMessage(message)).build();
  }
  
  protected Response notFound() {
    return Response.status(Response.Status.NOT_FOUND).build();
  }
  
  protected Response notFound(String message) {
    return Response.status(Response.Status.NOT_FOUND).entity(new RESTErrorMessage(message)).build();
  }
  
  protected Response success() {
    return Response.ok(new RESTSuccessMessage("Success")).build();
  }
  
  protected Response error(Exception e) {
    return Response.serverError().entity(new RESTErrorMessage(e.getMessage())).build();
  }
  
  protected Response error(String message) { return Response.serverError().entity(new RESTErrorMessage(message)).build(); }
  
  protected Response error(List<String> message)
  {
    return Response.serverError().entity(new RESTErrorMessage(message)).build();
  }
  
  protected Response rateLimitExceeded(String message) {
    return Response.status(SynapseResponseStatusEnum.RATE_LIMIT_EXCEEDED).entity(new RESTErrorMessage(message)).build();
  }
}
