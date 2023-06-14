package com.go2group.synapse.rest;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.json.JSONArray;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.go2group.synapse.bean.HtmlResponseWrapper;
import com.go2group.synapse.bean.RequirementTree;
import com.go2group.synapse.bean.RequirementTree.ChildTree;
import com.go2group.synapse.bean.RequirementTree.ParentTree;
import com.go2group.synapse.config.SynapseConfig;
import com.go2group.synapse.core.exception.InvalidDataException;
import com.go2group.synapse.core.tree.bean.TreeFilterItem;
import com.go2group.synapse.core.tree.bean.TreeItem;
import com.go2group.synapse.core.util.JqLResultBean;
import com.go2group.synapse.core.util.PluginUtil;
import com.go2group.synapse.service.RequirementService;
import com.go2group.synapse.web.panel.RequirementMemberHelper;
import com.go2group.synapserm.bean.ReqSuiteMemberOutputBean;
import com.go2group.synapserm.bean.ReqSuiteOutputBean;
import com.go2group.synapserm.service.RequirementSuiteService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;



@Path("requirementMember")
@Consumes({"application/json"})
@Produces({"application/json"})
public class RequirementMemberREST
{
  private final RequirementService requirementService;
  private final RequirementSuiteService requirementSuiteService;
  private final IssueManager issueManager;
  private final ApplicationProperties applicationProperties;
  private final I18nHelper i18n;
  private final SynapseConfig synapseConfig;
  private static final Logger log = Logger.getLogger(RequirementMemberREST.class);
  

  @Autowired
  public RequirementMemberREST(@ComponentImport IssueManager issueManager, @ComponentImport ApplicationProperties applicationProperties, @ComponentImport I18nHelper i18n, RequirementService requirementService, RequirementSuiteService requirementSuiteService, SynapseConfig synapseConfig)
  {
    this.requirementService = requirementService;
    this.requirementSuiteService = requirementSuiteService;
    this.issueManager = issueManager;
    this.applicationProperties = applicationProperties;
    this.i18n = i18n;
    this.synapseConfig = synapseConfig;
  }
  
  @POST
  @Path("addMember")
  public Response addMember(String reqMemberDetails) {
    String html = "";
    try {
      JSONObject jsonObject = new JSONObject(reqMemberDetails);
      String currentReqkey = jsonObject.getString("currentReqkey");
      JSONArray memberReqKeys = jsonObject.getJSONArray("memberReqKey");
      String reqRelation = jsonObject.getString("reqRelation");
      
      if (memberReqKeys != null)
      {


        if ("parent".equalsIgnoreCase(reqRelation))
        {
          if (memberReqKeys.length() > 1)
          {
            throw new InvalidDataException(i18n.getText("synapse.web.panel.requirement.validation.multiple.parent.notallowed"));
          }
        }
        
        for (int index = 0; index < memberReqKeys.length(); index++) {
          String memberReqKey = memberReqKeys.getString(index);
          
          Issue member = issueManager.getIssueByCurrentKey(memberReqKey);
          Issue currentIssue = issueManager.getIssueByCurrentKey(currentReqkey);
          
          if ("parent".equalsIgnoreCase(reqRelation)) {
            ReqSuiteMemberOutputBean parentSuite = requirementSuiteService.getParent(currentIssue.getId(), currentIssue.getProjectId());
            if (parentSuite != null) {
              requirementSuiteService.deleteRelation(parentSuite.getReqSuiteOutputBean().getId(), currentIssue.getId());
            }
            requirementService.addParent(currentIssue.getId(), member.getId(), ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser(), true);
            break; }
          if ("child".equalsIgnoreCase(reqRelation)) {
            ReqSuiteMemberOutputBean parentSuite = requirementSuiteService.getParent(member.getId(), member.getProjectId());
            if (parentSuite != null) {
              requirementSuiteService.deleteRelation(parentSuite.getReqSuiteOutputBean().getId(), member.getId());
            }
            requirementService.addChild(currentIssue.getId(), member.getId());
          }
        }
      }
    } catch (JSONException jsonExcep) {
      log.debug(jsonExcep.getMessage(), jsonExcep);
      log.error(jsonExcep.getMessage());
      return Response.serverError().entity(jsonExcep.getMessage())
        .build();
    } catch (NumberFormatException e) {
      log.debug(e.getMessage(), e);
      log.error(e.getMessage());
      return Response.serverError().entity(e.getMessage()).build();
    } catch (InvalidDataException e) {
      log.debug(e.getMessage(), e);
      log.error(e.getMessage());
      return Response.serverError().entity(e.getMessage()).build();
    }
    
    return Response.ok(new HtmlResponseWrapper(html)).build();
  }
  
  @GET
  @Path("getParent")
  public Response getParentRequirement(@QueryParam("currentReqKey") String currentReqKey)
  {
    String html = "";
    try {
      Issue issue = issueManager.getIssueByCurrentKey(currentReqKey);
      Issue parentIssue = requirementService.getParent(issue.getId());
      if (parentIssue != null) {
        List<Issue> members = new ArrayList();
        members.add(parentIssue);
        html = RequirementMemberHelper.getMemberHtml(members, "parent", applicationProperties, i18n);
      }
      else {
        html = "";
      }
    }
    catch (Exception excep) {
      log.debug(excep.getMessage(), excep);
      log.error(excep.getMessage());
      return Response.serverError().entity(excep.getMessage()).build();
    }
    return Response.ok(new HtmlResponseWrapper(html)).build();
  }
  
  @GET
  @Path("getChildren")
  public Response getChildrenRequirements(@QueryParam("currentReqKey") String currentReqKey)
  {
    String html = "";
    try {
      Issue issue = issueManager.getIssueByCurrentKey(currentReqKey);
      
      List<Issue> children = requirementService.getChildren(issue.getId());
      if ((children != null) && (children.size() > 0)) {
        html = RequirementMemberHelper.getMemberHtml(children, "child", applicationProperties, i18n);
      }
      else {
        html = "";
      }
    }
    catch (Exception excep) {
      log.debug(excep.getMessage(), excep);
      log.error(excep.getMessage());
      return Response.serverError().entity(excep.getMessage()).build();
    }
    return Response.ok(new HtmlResponseWrapper(html)).build();
  }
  
  @POST
  @Path("deleteParent")
  public Response deleteParent(String data) {
    String html = "success";
    try {
      JSONObject jsonObject = new JSONObject(data);
      String currentReqkey = jsonObject.getString("currentReqkey");
      

      Issue currentIssue = issueManager.getIssueByCurrentKey(currentReqkey);
      if (currentIssue != null) {
        requirementService.removeParent(currentIssue.getId());
      }
    } catch (JSONException jsonExcep) {
      log.debug(jsonExcep.getMessage(), jsonExcep);
      log.error(jsonExcep.getMessage());
      return Response.serverError().entity(jsonExcep.getMessage())
        .build();
    } catch (NumberFormatException e) {
      e.printStackTrace();
    } catch (InvalidDataException e) {
      e.printStackTrace();
    }
    
    return Response.ok(new HtmlResponseWrapper(html)).build();
  }
  
  @POST
  @Path("deleteChild")
  public Response deleteChild(String data) {
    String html = "success";
    try {
      JSONObject jsonObject = new JSONObject(data);
      String currentReqkey = jsonObject.getString("currentReqkey");
      String childReqkey = jsonObject.getString("childReqkey");
      

      Issue currentIssue = issueManager.getIssueByCurrentKey(currentReqkey);
      
      Issue childIssue = issueManager.getIssueByCurrentKey(childReqkey);
      
      if (currentIssue != null) {
        requirementService.removeChild(currentIssue.getId(), childIssue.getId());
        requirementService.addToDefaultParent(childIssue.getId(), childIssue.getProjectId());
      }
    } catch (JSONException jsonExcep) {
      log.debug(jsonExcep.getMessage(), jsonExcep);
      log.error(jsonExcep.getMessage());
      return Response.serverError().entity(jsonExcep.getMessage()).build();
    } catch (Exception e) {
      log.debug(e.getMessage(), e);
      log.error(e.getMessage());
    }
    
    return Response.ok(new HtmlResponseWrapper(html)).build();
  }
  
  @GET
  @Path("getRequirementTree")
  public Response getRequirementTree(@QueryParam("currentReqKey") String currentReqKey)
  {
    String html = "";
    try {
      Issue issue = issueManager.getIssueByCurrentKey(currentReqKey);
      if (issue != null)
      {
        RequirementTree requirementTree = requirementService.getRequirementTree(issue.getId());
        if (requirementTree != null) {
          List<Issue> parents = getParents(requirementTree);
          List<RequirementTree.ChildTree> children = requirementTree.getChildren();
          
          int margin = 0;
          if ((parents != null) && (parents.size() > 0)) {
            Collections.reverse(parents);
            if ((children == null) || (children.size() == 0)) {
              parents.add(issue);
            }
            html = RequirementMemberHelper.getParentHtml(parents, applicationProperties);
            
            margin = parents.size() * 20 - 10;
          }
          
          if ((children != null) && (children.size() > 0))
          {
            html = html + RequirementMemberHelper.getNodeHtml(issue, applicationProperties, margin);
            
            html = html + getAllChildren(children);
          }
          if (((parents == null) || (parents.size() == 0)) && ((children == null) || 
            (children.size() == 0)))
          {
            html = "<tr><td><label>" + i18n.getText("synapse.web.panel.label.req.no-recs") + "</label></td></tr>";
          }
        }
        else
        {
          html = "<tr><td><label>" + i18n.getText("synapse.web.panel.label.req.no-recs") + "</label></td></tr>";
        }
      }
    }
    catch (Exception excep) {
      log.debug(excep.getMessage(), excep);
      log.error(excep.getMessage());
      return Response.serverError().entity(excep.getMessage()).build();
    }
    return Response.ok(new HtmlResponseWrapper(html)).build();
  }
  
  private List<Issue> getParents(RequirementTree requirementTree) {
    List<Issue> parentIssues = new ArrayList();
    RequirementTree.ParentTree parent = requirementTree.getParent();
    if (parent != null) {
      getParents(parent, parentIssues);
    }
    
    return parentIssues;
  }
  
  private void getParents(RequirementTree.ParentTree parentTree, List<Issue> parentBranch) {
    if (parentTree.getParent() == null) {
      parentBranch.add(parentTree.getIssue());
    } else {
      parentBranch.add(parentTree.getIssue());
      getParents(parentTree.getParent(), parentBranch);
    }
  }
  
  private String getAllChildren(List<RequirementTree.ChildTree> childTree) {
    String html = "<tr>\r\n<td colspan=\"3\">\r\n<table class=\"aui subtable syn-tree-padding\" border=\"0\">";
    
    if (childTree != null) {
      for (RequirementTree.ChildTree child : childTree) {
        html = html + RequirementMemberHelper.getChildrenHtml(Arrays.asList(new Issue[] { child.getIssue() }), applicationProperties);
        if ((child.getChildren() != null) && (child.getChildren().size() > 0)) {
          html = html + getAllChildren(child.getChildren());
        }
      }
    }
    return html + "</table>\r\n</td>\r\n</tr>";
  }
  
  @POST
  @Path("addDefaultParent")
  public Response addDefaultParent(String data) {
    try {
      JSONObject jsonObject = new JSONObject(data);
      String projectKey = jsonObject.getString("projectKey");
      

      List<String> reqTypes = synapseConfig.getIssueTypeIds("Requirement");
      

      ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
      
      Project project = ComponentAccessor.getProjectManager().getProjectByCurrentKey(projectKey);
      if (project != null)
      {
        List<Issue> allRequirementsInJIRA = requirementService.getRequirementsInJIRA(user, project);
        for (Issue requirement : allRequirementsInJIRA) {
          if (reqTypes.contains(requirement.getIssueType().getId())) {
            requirementService.addToDefaultParent(requirement
              .getId(), project.getId());
          }
        }
      } else {
        return Response.serverError().entity("Invalid project").build();
      }
    } catch (NumberFormatException e) {
      log.debug(e.getMessage(), e);
      log.error(e.getMessage());
      return Response.serverError().entity(e.getMessage()).build();
    } catch (InvalidDataException e) {
      log.debug(e.getMessage(), e);
      log.error(e.getMessage());
      return Response.serverError().entity(e.getMessage()).build();
    } catch (JSONException e) {
      log.debug(e.getMessage(), e);
      log.error(e.getMessage());
      return Response.serverError().entity(e.getMessage()).build();
    }
    return Response.ok(new HtmlResponseWrapper("success")).build();
  }
  
  @POST
  @Path("getMembersCount")
  public Response getMembersCount(TreeItem treeItem, @Context HttpServletRequest request) {
    JSONObject output = new JSONObject();
    RequirementTree reqTree = null;
    try {
      List<TreeFilterItem> filters = PluginUtil.getFilters(treeItem);
      JqLResultBean jqLResultBean = PluginUtil.getJqlResult(treeItem.getInjectJql(), treeItem.getJqlQuery());
      List<Long> jqlIssues = jqLResultBean.getIssues();
      String reqId = treeItem.getId();
      log.debug("Fetch Members count for requirement " + reqId);
      
      int count = 0;
      Issue reqIssue = issueManager.getIssueObject(Long.valueOf(reqId));
      if (reqIssue != null) {
        reqTree = requirementService.getRequirementTree(Long.valueOf(reqId));
        if (reqTree != null) {
          count = reqTree.getChildrenCount(filters, jqlIssues);
        }
      } else {
        count = requirementSuiteService.getChildrenRequirementsCount(Integer.valueOf(reqId), filters, jqlIssues).intValue();
      }
      output.put("responseText", String.valueOf(count));
    } catch (NumberFormatException e) {
      log.debug(e.getMessage(), e);
      log.error(e.getMessage());
    } catch (InvalidDataException e) {
      log.debug(e.getMessage(), e);
      log.error(e.getMessage());
    } catch (JSONException e) {
      log.debug(e.getMessage(), e);
      log.error(e.getMessage());
    }
    
    return Response.ok(output.toString()).build();
  }
  
  @POST
  @Path("reorderRequirementMembers")
  public Response reorderRequirementMembers(String reorderData, @Context HttpServletRequest request) {
    log.debug("reorderRequirementMembers - reorderData" + reorderData);
    JSONObject output = new JSONObject();
    try {
      JSONObject reorderJson = new JSONObject(reorderData);
      long parentReqId = reorderJson.getLong("parentReqId");
      long currentReqId = reorderJson.getLong("currentReqId");
      long referenceReqId = reorderJson.getLong("referenceReqId");
      
      requirementService.changeOrder(Long.valueOf(parentReqId), Long.valueOf(currentReqId), Long.valueOf(referenceReqId));
      
      output.put("html", "done");
    } catch (NumberFormatException e) {
      log.debug(e.getMessage(), e);
      log.error(e.getMessage());
    } catch (InvalidDataException e) {
      log.debug(e.getMessage(), e);
      log.error(e.getMessage());
    } catch (JSONException e) {
      log.debug(e.getMessage(), e);
      log.error(e.getMessage());
    }
    return Response.ok(output.toString()).build();
  }
}
