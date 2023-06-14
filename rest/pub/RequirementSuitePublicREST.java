package com.go2group.synapse.rest.pub;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.go2group.synapse.bean.HtmlResponseWrapper;
import com.go2group.synapse.core.exception.InvalidDataException;
import com.go2group.synapse.manager.PermissionUtilAbstract;
import com.go2group.synapse.service.impl.ratelimit.DefaultRestRateLimiter;
import com.go2group.synapserm.bean.ReqSuiteMemberOutputBean;
import com.go2group.synapserm.bean.ReqSuiteMemberRestBean;
import com.go2group.synapserm.bean.ReqSuiteOutputBean;
import com.go2group.synapserm.service.RequirementSuiteService;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

@Path("public/requirementSuite")
public class RequirementSuitePublicREST extends AbstractPublicREST
{
  private static final Logger log = Logger.getLogger(RequirementSuitePublicREST.class);
  
  private RequirementSuiteService requirementSuiteService;
  
  private ProjectManager projectManager;
  
  private I18nHelper i18nHelper;
  
  public RequirementSuitePublicREST(@ComponentImport ProjectManager projectManager, @ComponentImport I18nHelper i18nHelper, PermissionUtilAbstract permissionUtil, RequirementSuiteService requirementSuiteService)
  {
    super(permissionUtil);
    this.requirementSuiteService = requirementSuiteService;
    this.projectManager = projectManager;
    this.i18nHelper = i18nHelper;
  }
  
  @Path("/create")
  @POST
  public Response createRequirementSuite(RequirementRestBean requirementRestBean) {
    try {
      HttpServletRequest request = ExecutingHttpRequest.get();
      canProceed = DefaultRestRateLimiter.canRequestProceed(request);
      HttpServletRequest request; if (!canProceed) {
        return rateLimitExceeded("Too many Requests");
      }
      
      Project project = projectManager.getProjectByCurrentKey(requirementRestBean.getProjectKey());
      if (project == null) { HttpServletRequest request;
        return Response.serverError().entity(i18nHelper.getText("synapse.run.attribute.project.panel.invalid.proj")).build();
      }
      
      ReqSuiteOutputBean reqSuite = requirementSuiteService.addSuite(requirementRestBean.getParentSuiteId(), requirementRestBean.getSuiteName(), project.getId(), true, ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser().getId());
      RequirementRestBean resultBean; if (reqSuite != null) {
        resultBean = new RequirementRestBean();
        resultBean.setId(reqSuite.getId());
        resultBean.setProjectId(reqSuite.getProjectId());
        resultBean.setSuiteName(reqSuite.getName());
        resultBean.setParentSuiteId(requirementRestBean.getParentSuiteId());
        HttpServletRequest request; return Response.ok(resultBean).build(); }
      HttpServletRequest request;
      return Response.serverError().entity("Failed").build();
    } catch (InvalidDataException e) {
      boolean canProceed;
      log.debug(e.getMessage(), e);
      log.error(e.getMessage());
      HttpServletRequest request; return Response.serverError().entity(e.getMessage()).build();
    } finally {
      HttpServletRequest request = ExecutingHttpRequest.get();
      DefaultRestRateLimiter.endCall(request);
    }
  }
  
  @Path("/delete")
  @DELETE
  public Response deleteRequirementSuite(@QueryParam("projectKeyOrId") String projectKeyOrId, @QueryParam("requirementSuiteId") Integer requirementSuiteId) {
    try {
      HttpServletRequest request = ExecutingHttpRequest.get();
      canProceed = DefaultRestRateLimiter.canRequestProceed(request);
      HttpServletRequest request; if (!canProceed) {
        return rateLimitExceeded("Too many Requests");
      }
      
      Project project = projectManager.getProjectByCurrentKey(projectKeyOrId);
      if ((project == null) && (StringUtils.isNumeric(projectKeyOrId))) {
        project = projectManager.getProjectObj(Long.valueOf(projectKeyOrId));
      }
      if (project == null) { HttpServletRequest request;
        return Response.serverError().entity(i18nHelper.getText("synapse.run.attribute.project.panel.invalid.proj")).build();
      }
      HttpServletRequest request;
      if ((requirementSuiteId == null) || (requirementSuiteId.intValue() <= 0)) {
        return Response.serverError().entity(i18nHelper.getText("synapserm.req.suite.error.invalid.id")).build();
      }
      
      boolean deleted = requirementSuiteService.deleteSuite(requirementSuiteId, project.getId());
      if (deleted) { HttpServletRequest request;
        return Response.ok(new HtmlResponseWrapper("success")).build(); }
      HttpServletRequest request;
      return Response.ok(new HtmlResponseWrapper("failed")).build();
    } catch (InvalidDataException e) {
      boolean canProceed;
      log.debug(e.getMessage(), e);
      log.error(e.getMessage());
      HttpServletRequest request; return Response.serverError().entity(e.getMessage()).build();
    } finally {
      HttpServletRequest request = ExecutingHttpRequest.get();
      DefaultRestRateLimiter.endCall(request);
    }
  }
  
  @Path("/addMember")
  @POST
  public Response addMember(@QueryParam("requirementSuiteId") Integer requirementSuiteId, @QueryParam("memberId") Long memberId, @QueryParam("memberProjectKeyOrId") String projectKeyOrId) {
    try {
      HttpServletRequest request = ExecutingHttpRequest.get();
      canProceed = DefaultRestRateLimiter.canRequestProceed(request);
      HttpServletRequest request; if (!canProceed) {
        return rateLimitExceeded("Too many Requests");
      }
      
      Project project = projectManager.getProjectByCurrentKey(projectKeyOrId);
      if ((project == null) && (StringUtils.isNumeric(projectKeyOrId))) {
        project = projectManager.getProjectObj(Long.valueOf(projectKeyOrId));
      }
      if (project == null) { HttpServletRequest request;
        return Response.serverError().entity(i18nHelper.getText("synapse.run.attribute.project.panel.invalid.proj")).build();
      }
      
      ReqSuiteMemberOutputBean reqSuiteMemberOutputBean = requirementSuiteService.addRequirementMember(requirementSuiteId, memberId, project.getId());
      ReqSuiteMemberRestBean reqSuiteMember = new ReqSuiteMemberRestBean();
      reqSuiteMember.setReqSuite(reqSuiteMemberOutputBean.getReqSuiteOutputBean().getId());
      reqSuiteMember.setMember(reqSuiteMemberOutputBean.getMember());
      reqSuiteMember.setMemberType(reqSuiteMemberOutputBean.getMemberType());
      reqSuiteMember.setMemberProject(reqSuiteMemberOutputBean.getMemberProject());
      HttpServletRequest request;
      return Response.ok(reqSuiteMember).build();
    } catch (InvalidDataException e) { boolean canProceed;
      log.debug(e.getMessage(), e);
      log.error(e.getMessage());
      HttpServletRequest request; return Response.serverError().entity(e.getMessage()).build();
    } finally {
      HttpServletRequest request = ExecutingHttpRequest.get();
      DefaultRestRateLimiter.endCall(request);
    }
  }
  
  @Path("/deleteMember")
  @DELETE
  public Response deleteMember(@QueryParam("requirementSuiteId") Integer parentId, @QueryParam("memberId") Long memberId) {
    try {
      HttpServletRequest request = ExecutingHttpRequest.get();
      canProceed = DefaultRestRateLimiter.canRequestProceed(request);
      HttpServletRequest request; if (!canProceed) {
        return rateLimitExceeded("Too many Requests");
      }
      
      boolean deleted = requirementSuiteService.deleteRelation(parentId, memberId);
      if (deleted) { HttpServletRequest request;
        return Response.ok(new HtmlResponseWrapper("success")).build(); }
      HttpServletRequest request;
      return Response.ok(new HtmlResponseWrapper("failed")).build();
    } catch (InvalidDataException e) {
      boolean canProceed;
      log.debug(e.getMessage(), e);
      log.error(e.getMessage());
      HttpServletRequest request; return Response.serverError().entity(e.getMessage()).build();
    } finally {
      HttpServletRequest request = ExecutingHttpRequest.get();
      DefaultRestRateLimiter.endCall(request);
    }
  }
}
