package com.go2group.synapse.rest;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.bc.user.search.UserSearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.VelocityParamFactory;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.velocity.VelocityManager;
import com.go2group.synapse.bean.BasicFilterOutputBean;
import com.go2group.synapse.bean.HtmlResponseWrapper;
import com.go2group.synapse.bean.TestSuiteOutputBean;
import com.go2group.synapse.core.basic.filter.service.BasicFilterSearchService;
import com.go2group.synapse.core.exception.InvalidDataException;
import com.go2group.synapse.core.helper.SynapseCommonJqlHelper;
import com.go2group.synapse.core.rest.SynapseJQLBean;
import com.go2group.synapse.core.util.JqLResultBean;
import com.go2group.synapse.service.TestSuiteService;
import com.go2group.synapse.util.PluginUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

@Path("basicFilter")
@Consumes({"application/json"})
@javax.ws.rs.Produces({"application/json"})
public class BasicFilterREST
{
  private static final Logger log = Logger.getLogger(BasicFilterREST.class);
  
  private final SynapseCommonJqlHelper synapseCommonJqlHelper;
  
  private final BasicFilterSearchService basicFilterSearchService;
  private final TestSuiteService testSuiteService;
  private final I18nHelper i18nHelper;
  private final IssueManager issueManager;
  private static final String BASIC_FILTER_SEARCH_TEMPLATE = "/core/templates/web/basic/filter/basic-filter-view.vm";
  
  @Autowired
  public BasicFilterREST(@ComponentImport I18nHelper i18nHelper, @ComponentImport IssueManager issueManager, BasicFilterSearchService basicFilterSearchService, TestSuiteService testSuiteService)
  {
    this.basicFilterSearchService = basicFilterSearchService;
    this.i18nHelper = i18nHelper;
    this.issueManager = issueManager;
    this.testSuiteService = testSuiteService;
    synapseCommonJqlHelper = new SynapseCommonJqlHelper();
  }
  
  @GET
  @Path("getBasicFilterSearch")
  public Response getBasicFilterSearch(@QueryParam("issueKey") String issueKey, @QueryParam("project") String selectedProject) {
    log.debug("Getting basic filter search criterias : ");
    Issue issue = issueManager.getIssueByKeyIgnoreCase(issueKey);
    if (issue != null) {
      String html = getBasicFilterHtml(issueKey, selectedProject);
      return Response.ok(new HtmlResponseWrapper(html)).build();
    }
    return Response.serverError().entity(i18nHelper.getText("errormessage.validation.invalid.issuekey", issueKey)).build();
  }
  
  @POST
  @Path("basicFilterRelatedIssues")
  public <T, N> Response getBasicFilterRelatedIssues(BasicFilterOutputBean basicFilterOutputBean, @Context HttpServletRequest request)
  {
    String injectJql = "";
    if ((basicFilterOutputBean != null) && (basicFilterOutputBean.getSynapseJQLBean() != null)) {
      basicFilterOutputBean.getSynapseJQLBean().setContextPath(request.getContextPath());
      injectJql = basicFilterOutputBean.getSynapseJQLBean().getInjectJql();
    }
    String jql = formJQLQuery(basicFilterOutputBean);
    JqLResultBean jqLResultBean = PluginUtil.getJqlResult(injectJql, jql);
    



    return Response.ok(jqLResultBean).build();
  }
  
  private String getBasicFilterHtml(String issueKey, String selectedProject) {
    VelocityManager vm = ComponentAccessor.getVelocityManager();
    Map<String, Object> velocityParams = ComponentAccessor.getVelocityParamFactory().getDefaultVelocityParams();
    velocityParams.put("i18n", i18nHelper);
    
    velocityParams.put("rootSuites", getRootSuites(selectedProject));
    velocityParams.put("allComponents", getAllComponents(issueKey));
    velocityParams.put("allPriorities", getAllPriorities());
    velocityParams.put("allStatus", getAllStatus());
    velocityParams.put("allAssignees", getAllAssignees());
    
    String html = vm.getEncodedBody("", "/core/templates/web/basic/filter/basic-filter-view.vm", null, velocityParams);
    
    return html;
  }
  
  public List<TestSuiteOutputBean> getRootSuites(String selectedProject) {
    List<TestSuiteOutputBean> rootSuites = new ArrayList();
    try {
      Long projectId = Long.valueOf(selectedProject);
      rootSuites = testSuiteService.getAllRootTestSuites(projectId);
    } catch (InvalidDataException e) {
      e.printStackTrace();
    }
    return rootSuites;
  }
  
  public Collection<ProjectComponent> getAllComponents(String issueKey) {
    Issue issue = issueManager.getIssueObject(issueKey);
    Collection<ProjectComponent> components = new ArrayList();
    if (issue != null) {
      Project project = issue.getProjectObject();
      components = project.getComponents();
    }
    return components;
  }
  
  public Collection<Priority> getAllPriorities() {
    return ComponentAccessor.getConstantsManager().getPriorities();
  }
  
  public Collection<Status> getAllStatus() {
    return ComponentAccessor.getConstantsManager().getStatuses();
  }
  
  public Collection<ApplicationUser> getAllAssignees() {
    UserSearchService userService = (UserSearchService)ComponentAccessor.getComponent(UserSearchService.class);
    JiraServiceContext context = new JiraServiceContextImpl(ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser());
    return userService.findUsers(context, "");
  }
  
  private String formJQLQuery(BasicFilterOutputBean basicFilterOutputBean) {
    StringBuilder jql = new StringBuilder();
    if (basicFilterOutputBean != null) {
      List<String> rootSuites = basicFilterOutputBean.getRootSuites();
      if ((rootSuites != null) && (rootSuites.size() > 0)) {
        String rootSuiteJql = "issue in testSuite(";
        for (int i = 0; i < rootSuites.size(); i++) {
          rootSuiteJql = rootSuiteJql + "'" + (String)rootSuites.get(i) + "'";
          if (i != rootSuites.size() - 1) {
            rootSuiteJql = rootSuiteJql + ",";
          }
        }
        rootSuiteJql = rootSuiteJql + ")";
        if (jql.length() > 0) {
          jql.append(" and ").append(rootSuiteJql);
        } else {
          jql.append(rootSuiteJql);
        }
      }
      List<String> components = basicFilterOutputBean.getComponents();
      if ((components != null) && (components.size() > 0)) {
        String componentJql = "";
        if (components.contains("No")) {
          componentJql = "component is EMPTY";
        } else {
          componentJql = "component in (";
          for (int i = 0; i < components.size(); i++) {
            componentJql = componentJql + "'" + (String)components.get(i) + "'";
            if (i != components.size() - 1) {
              componentJql = componentJql + ",";
            }
          }
          componentJql = componentJql + ")";
        }
        if (jql.length() > 0) {
          jql.append(" and ").append(componentJql);
        } else {
          jql.append(componentJql);
        }
      }
      List<String> priorities = basicFilterOutputBean.getPriorities();
      if ((priorities != null) && (priorities.size() > 0)) {
        String priorityJql = "priority in (";
        for (int i = 0; i < priorities.size(); i++) {
          priorityJql = priorityJql + "'" + (String)priorities.get(i) + "'";
          if (i != priorities.size() - 1) {
            priorityJql = priorityJql + ",";
          }
        }
        priorityJql = priorityJql + ")";
        if (jql.length() > 0) {
          jql.append(" and ").append(priorityJql);
        } else {
          jql.append(priorityJql);
        }
      }
      List<String> assignees = basicFilterOutputBean.getAssignees();
      if ((assignees != null) && (assignees.size() > 0)) {
        String assigneeJql = "";
        if (assignees.contains("Unassigned")) {
          assigneeJql = "assignee is EMPTY";
        } else {
          assigneeJql = "assignee in (";
          for (int i = 0; i < assignees.size(); i++) {
            assigneeJql = assigneeJql + "'" + (String)assignees.get(i) + "'";
            if (i != assignees.size() - 1) {
              assigneeJql = assigneeJql + ",";
            }
          }
          assigneeJql = assigneeJql + ")";
        }
        if (jql.length() > 0) {
          jql.append(" and ").append(assigneeJql);
        } else {
          jql.append(assigneeJql);
        }
      }
      List<String> statuses = basicFilterOutputBean.getStatuses();
      if ((statuses != null) && (statuses.size() > 0)) {
        String statusJql = "status in (";
        for (int i = 0; i < statuses.size(); i++) {
          statusJql = statusJql + "'" + (String)statuses.get(i) + "'";
          if (i != statuses.size() - 1) {
            statusJql = statusJql + ",";
          }
        }
        statusJql = statusJql + ")";
        if (jql.length() > 0) {
          jql.append(" and ").append(statusJql);
        } else {
          jql.append(statusJql);
        }
      }
      List<String> labels = basicFilterOutputBean.getLabels();
      if ((labels != null) && (labels.size() > 0)) {
        labels.removeAll(Arrays.asList(new String[] { "", null }));
        String labelJql = "labels in (";
        for (int i = 0; i < labels.size(); i++) {
          labelJql = labelJql + "'" + (String)labels.get(i) + "'";
          if (i != labels.size() - 1) {
            labelJql = labelJql + ",";
          }
        }
        labelJql = labelJql + ")";
        if (jql.length() > 0) {
          jql.append(" and ").append(labelJql);
        } else {
          jql.append(labelJql);
        }
      }
      String text = basicFilterOutputBean.getBasicText();
      if ((text != null) && (!text.isEmpty())) {
        String textJql = "text ~ '" + text + "'";
        if (jql.length() > 0) {
          jql.append(" and ").append(textJql);
        } else {
          jql.append(textJql);
        }
      }
    }
    if (jql.length() > 0) {
      return jql.toString();
    }
    return "";
  }
}
