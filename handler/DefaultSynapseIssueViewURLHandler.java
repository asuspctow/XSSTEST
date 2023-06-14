package com.go2group.synapse.handler;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.plugin.issueview.IssueViewFieldParams;
import com.atlassian.jira.plugin.issueview.IssueViewModuleDescriptor;
import com.atlassian.jira.plugin.issueview.IssueViewRequestParams;
import com.atlassian.jira.plugin.issueview.IssueViewRequestParamsHelper;
import com.atlassian.jira.plugin.issueview.IssueViewRequestParamsImpl;
import com.atlassian.jira.plugin.searchrequestview.HttpRequestHeaders;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.go2group.synapse.bean.CustomDisplayPrefBean;
import com.go2group.synapse.config.SynapseConfig;
import com.go2group.synapse.core.exception.InvalidDataException;
import com.go2group.synapse.core.tree.bean.TreeFilterItem;
import com.go2group.synapse.core.tree.bean.TreeNode;
import com.go2group.synapse.core.util.JqLResultBean;
import com.go2group.synapse.enums.PreferencesModuleEnum;
import com.go2group.synapse.enums.RequirementsFieldNameEnum;
import com.go2group.synapse.issue.views.SynapseRequirementIssueWordView;
import com.go2group.synapserm.bean.ReqRelationOutputBean;
import com.go2group.synapserm.bean.ReqSuiteMemberOutputBean;
import com.go2group.synapserm.bean.ReqSuiteOutputBean;
import com.go2group.synapserm.bean.tree.Tree;
import com.go2group.synapserm.bean.tree.impl.DefaultRequirementTreePool;
import com.go2group.synapserm.service.RequirementServiceRM;
import com.go2group.synapserm.service.RequirementSuiteService;
import com.go2group.synapserm.util.PluginUtil;
import com.openhtmltopdf.DOMBuilder;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DefaultSynapseIssueViewURLHandler implements SynapseIssueViewURLHandler
{
  private final PluginAccessor pluginAccessor;
  private final IssueManager issueManager;
  private final IssueViewRequestParamsHelper issueViewRequestParamsHelper;
  private final UserManager userManager;
  private final RequirementServiceRM requirementServiceRM;
  private RequirementSuiteService requirementSuiteService;
  protected final DefaultRequirementTreePool requirementTreePool;
  private SynapseConfig synapseConfig;
  private Logger log = Logger.getLogger(DefaultSynapseIssueViewURLHandler.class);
  


  @Autowired
  public DefaultSynapseIssueViewURLHandler(@ComponentImport PluginAccessor pluginAccessor, @ComponentImport IssueManager issueManager, @ComponentImport IssueViewRequestParamsHelper issueViewRequestParamsHelper, @ComponentImport UserManager userManager, RequirementServiceRM requirementServiceRM, RequirementSuiteService requirementSuiteService, DefaultRequirementTreePool requirementTreePool, SynapseConfig synapseConfig)
  {
    this.pluginAccessor = pluginAccessor;
    this.issueManager = issueManager;
    this.issueViewRequestParamsHelper = issueViewRequestParamsHelper;
    this.userManager = userManager;
    this.requirementServiceRM = requirementServiceRM;
    this.requirementSuiteService = requirementSuiteService;
    this.requirementTreePool = requirementTreePool;
    this.synapseConfig = synapseConfig;
  }
  
  public String getURLWithoutContextPath(IssueViewModuleDescriptor moduleDescriptor, String issueKey) {
    return 
      "/exportRequirements/" + moduleDescriptor.getCompleteKey() + "/" + issueKey + "/" + issueKey + "." + moduleDescriptor.getFileExtension();
  }
  


  private static String getSampleURL()
  {
    return "/exportRequirements/jira.issueviews:xml/JRA-10/JRA-10.xml";
  }
  
  public void handleGetRequest(HttpServletRequest request, HttpServletResponse response) throws IOException
  {
    log.debug("i entered");
    String pathInfo = request.getPathInfo();
    
    if (StringUtils.isBlank(pathInfo)) {
      response.sendError(400, "Invalid path format. Path should be of format " + 
        getSampleURL());
      return;
    }
    

    if (pathInfo.startsWith("/")) {
      pathInfo = pathInfo.substring(1);
    }
    
    int firstSlashLocation = pathInfo.indexOf("/");
    if (firstSlashLocation == -1) {
      response.sendError(400, "Invalid path format. Path should be of format " + 
        getSampleURL());
      return;
    }
    String pluginKey = pathInfo.substring(0, firstSlashLocation);
    
    int secondSlashLocation = pathInfo.indexOf("/", firstSlashLocation + 1);
    if (secondSlashLocation == -1) {
      response.sendError(400, "Invalid path format. Path should be of format " + 
        getSampleURL());
      return;
    }
    String projectKey = pathInfo.substring(firstSlashLocation + 1, secondSlashLocation);
    
    ApplicationUser user = null;
    if (request.getRemoteUser() != null) {
      user = userManager.getUserByName(request.getRemoteUser());
      if (user == null) {
        response.sendError(400, "Could not find a user with the username " + 
          StringEscapeUtils.escapeHtml4(request.getRemoteUser()));
        return;
      }
    }
    
    IssueViewModuleDescriptor moduleDescriptor = getPluginModule(pluginKey);
    if (moduleDescriptor == null) {
      response.sendError(400, "Could not find any enabled plugin with key " + 
        StringEscapeUtils.escapeHtml4(pluginKey));
      return;
    }
    

    SynapseRequirementIssueWordView view = null;
    StringBuffer content = new StringBuffer();
    view = (SynapseRequirementIssueWordView)moduleDescriptor.getIssueView();
    

    IssueViewFieldParams issueViewFieldParams = issueViewRequestParamsHelper.getIssueViewFieldParams(request.getParameterMap());
    if ((issueViewFieldParams.isCustomViewRequested()) && (!issueViewFieldParams.isAnyFieldDefined())) {
      response.sendError(400, "No valid field defined for issue custom view");
      return;
    }
    
    IssueViewRequestParams issueViewRequestParams = new IssueViewRequestParamsImpl(issueViewFieldParams);
    if (!"index".equalsIgnoreCase(request.getParameter("jira.issue.searchlocation")))
    {




      Project project = ComponentAccessor.getProjectManager().getProjectObjByKey(projectKey);
      List<Issue> allReqIssues = new ArrayList();
      ReqSuiteMemberOutputBean req;
      try {
        Collection<ReqRelationOutputBean> rootReqs = requirementServiceRM.getRootRequirements(project.getId(), null, true, null);
        if ((rootReqs != null) && (rootReqs.size() > 0)) {
          for (ReqRelationOutputBean reqRelationOutputBean : rootReqs) {
            allReqIssues.add(reqRelationOutputBean.getChildIssue());
          }
        }
        
        Object reqs = (List)requirementSuiteService.getIssueTypeMembers(project.getId());
        List<Issue> allParentReqInSuite = null;
        if ((reqs != null) && (((List)reqs).size() > 0)) {
          allParentReqInSuite = new ArrayList();
          for (Iterator localIterator2 = ((List)reqs).iterator(); localIterator2.hasNext();) { req = (ReqSuiteMemberOutputBean)localIterator2.next();
            long reqMember = req.getMember().longValue();
            Issue reqIssue = issueManager.getIssueObject(Long.valueOf(reqMember));
            if ((reqIssue != null) && 
              (PluginUtil.isFilterPassed(reqIssue, null))) {
              allParentReqInSuite.add(reqIssue);
            }
          }
          
          allReqIssues.addAll(allParentReqInSuite);
        }
      } catch (InvalidDataException e) {
        log.debug(e.getMessage(), e);
      }
      if ((allReqIssues != null) && (!allReqIssues.isEmpty())) {
        boolean isNumberingEnabled = false;
        String userKey = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser().getKey();
        Map<String, List<CustomDisplayPrefBean>> moduleToFieldPrefMap = synapseConfig.getPreference(userKey);
        if ((moduleToFieldPrefMap != null) && (!moduleToFieldPrefMap.containsKey(PreferencesModuleEnum.REQUIREMENTS_PAGE.getName()))) {
          moduleToFieldPrefMap = synapseConfig.getPreference("Application");
        }
        if (moduleToFieldPrefMap != null) {
          Object fields = (List)moduleToFieldPrefMap.get(PreferencesModuleEnum.REQUIREMENTS_PAGE.getName());
          if (fields != null) {
            if (!((List)fields).isEmpty()) {
              for (CustomDisplayPrefBean prefBean : (List)fields) {
                if (RequirementsFieldNameEnum.NUMBERING.getName().equalsIgnoreCase(prefBean.getField())) {
                  isNumberingEnabled = true;
                }
              }
            }
          } else {
            isNumberingEnabled = true;
          }
        } else {
          isNumberingEnabled = true;
        }
        content.append(view.getContent(allReqIssues, issueViewRequestParams, "ProjectLevel", null, null, isNumberingEnabled));
      }
    }
    if (!"true".equalsIgnoreCase(request.getParameter(""))) {
      response.setContentType(moduleDescriptor.getContentType() + ";charset=" + 
        ComponentAccessor.getApplicationProperties().getEncoding());
      

      view.writeHeaders(projectKey, new HttpRequestHeaders(response), issueViewRequestParams);
    }
    log.debug("full content : " + content.toString());
    response.getWriter().write(content.toString());
  }
  
  public void handlePostRequest(HttpServletRequest request, HttpServletResponse response) throws IOException
  {
    log.debug("i entered");
    String pathInfo = request.getPathInfo();
    String fileFormat = request.getParameter("fileFormat");
    
    if (StringUtils.isBlank(pathInfo)) {
      response.sendError(400, "Invalid path format. Path should be of format " + 
        getSampleURL());
      return;
    }
    

    if (pathInfo.startsWith("/")) {
      pathInfo = pathInfo.substring(1);
    }
    
    int firstSlashLocation = pathInfo.indexOf("/");
    if (firstSlashLocation == -1) {
      response.sendError(400, "Invalid path format. Path should be of format " + 
        getSampleURL());
      return;
    }
    String pluginKey = pathInfo.substring(0, firstSlashLocation);
    
    int secondSlashLocation = pathInfo.indexOf("/", firstSlashLocation + 1);
    if (secondSlashLocation == -1) {
      response.sendError(400, "Invalid path format. Path should be of format " + 
        getSampleURL());
      return;
    }
    int thirdSlashLocation = pathInfo.indexOf("/", secondSlashLocation + 1);
    String projectKey = pathInfo.substring(firstSlashLocation + 1, secondSlashLocation);
    String exportAll = "";
    if (secondSlashLocation != -1) {
      exportAll = pathInfo.substring(secondSlashLocation + 1, thirdSlashLocation);
    }
    

    int fourthSlashLocation = pathInfo.indexOf("/", thirdSlashLocation + 1);
    String reqSuiteId = "";
    if (fourthSlashLocation != -1) {
      reqSuiteId = pathInfo.substring(thirdSlashLocation + 1, fourthSlashLocation);
    }
    ApplicationUser user = null;
    if (request.getRemoteUser() != null) {
      user = userManager.getUserByName(request.getRemoteUser());
      if (user == null) {
        response.sendError(400, "Could not find a user with the username " + 
          StringEscapeUtils.escapeHtml4(request.getRemoteUser()));
        return;
      }
    }
    
    IssueViewModuleDescriptor moduleDescriptor = getPluginModule(pluginKey);
    if (moduleDescriptor == null) {
      response.sendError(400, "Could not find any enabled plugin with key " + 
        StringEscapeUtils.escapeHtml4(pluginKey));
      return;
    }
    

    SynapseRequirementIssueWordView view = null;
    StringBuffer content = new StringBuffer();
    view = (SynapseRequirementIssueWordView)moduleDescriptor.getIssueView();
    

    IssueViewFieldParams issueViewFieldParams = issueViewRequestParamsHelper.getIssueViewFieldParams(request.getParameterMap());
    if ((issueViewFieldParams.isCustomViewRequested()) && (!issueViewFieldParams.isAnyFieldDefined())) {
      response.sendError(400, "No valid field defined for issue custom view");
      return;
    }
    
    IssueViewRequestParams issueViewRequestParams = new IssueViewRequestParamsImpl(issueViewFieldParams);
    if (!"index".equalsIgnoreCase(request.getParameter("jira.issue.searchlocation")))
    {




      Project project = ComponentAccessor.getProjectManager().getProjectObjByKey(projectKey);
      List<TreeFilterItem> filters = null;
      List<Long> jqlIssues = null;
      String json = request.getParameter("filter-param");
      if (StringUtils.isNotBlank(json)) {
        ObjectMapper mapper = new ObjectMapper();
        
        TreeNode treeNode = (TreeNode)mapper.readValue(json, TreeNode.class);
        filters = PluginUtil.getFilters(treeNode);
        JqLResultBean jqLResultBean = PluginUtil.getJqlResult(treeNode.getInjectJql(), treeNode.getJqlQuery());
        jqlIssues = jqLResultBean.getIssues();
      }
      boolean isNumberingEnabled = false;
      String userKey = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser().getKey();
      Map<String, List<CustomDisplayPrefBean>> moduleToFieldPrefMap = synapseConfig.getPreference(userKey);
      if ((moduleToFieldPrefMap != null) && (!moduleToFieldPrefMap.containsKey(PreferencesModuleEnum.REQUIREMENTS_PAGE.getName()))) {
        moduleToFieldPrefMap = synapseConfig.getPreference("Application");
      }
      if (moduleToFieldPrefMap != null) {
        List<CustomDisplayPrefBean> fields = (List)moduleToFieldPrefMap.get(PreferencesModuleEnum.REQUIREMENTS_PAGE.getName());
        if (fields != null) {
          if (!fields.isEmpty()) {
            for (CustomDisplayPrefBean prefBean : fields) {
              if (RequirementsFieldNameEnum.NUMBERING.getName().equalsIgnoreCase(prefBean.getField())) {
                isNumberingEnabled = true;
              }
            }
          }
        } else {
          isNumberingEnabled = true;
        }
      } else {
        isNumberingEnabled = true;
      }
      if (isNumberingEnabled) {
        Tree requirementModelTree = requirementTreePool.getRequirementTree(project.getId());
        if (requirementModelTree != null) {
          requirementModelTree.loadCompleteTree();
        }
      }
      
      if ((filters == null) && (exportAll.equalsIgnoreCase("exportAll"))) {
        filters = new ArrayList();
      }
      ContentHolder contentHolder = generateContent(project.getId(), reqSuiteId, view, issueViewRequestParams, filters, jqlIssues, isNumberingEnabled);
      
      Issue firstIssue = contentHolder.getFirstIssue();
      if (firstIssue != null) {
        content.append(contentHolder.getContent().toString());
        content.append(view.getFooter(firstIssue));
      }
    }
    log.debug("Content generated successfully ");
    if ((StringUtils.isNotBlank(fileFormat)) && (fileFormat.equalsIgnoreCase("pdf"))) {
      String html = content.toString();
      try {
        File pdfFile = File.createTempFile(projectKey, ".pdf");
        java.io.OutputStream outputStream = new FileOutputStream(pdfFile);
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.withW3cDocument(DOMBuilder.jsoup2DOM(Jsoup.parse(html)), "");
        builder.toStream(outputStream);
        builder.testMode(true);
        builder.run();
        html = new String(Files.readAllBytes(pdfFile.toPath()));
        response.setContentType("application/pdf;charset=" + 
          ComponentAccessor.getApplicationProperties().getEncoding());
        response.setHeader("Content-Disposition", "attachment; filename=\"" + projectKey + ".pdf\"");
        response.getWriter().write(html);
      } catch (Exception e) {
        log.debug(e.getMessage(), e);
        log.error(e.getMessage());
      }
    }
    else if (!"true".equalsIgnoreCase(request.getParameter(""))) {
      response.setContentType(moduleDescriptor.getContentType() + ";charset=" + 
        ComponentAccessor.getApplicationProperties().getEncoding());
      view.writeHeaders(projectKey, new HttpRequestHeaders(response), issueViewRequestParams);
    }
    


    response.getWriter().write(content.toString());
  }
  
  private IssueViewModuleDescriptor getPluginModule(String pluginKey) {
    try {
      return (IssueViewModuleDescriptor)pluginAccessor.getEnabledPluginModule(pluginKey);
    } catch (ClassCastException e) {
      return null;
    } catch (IllegalArgumentException e) {}
    return null;
  }
  
  private ContentHolder generateContent(Long projectId, String reqSuiteId, SynapseRequirementIssueWordView view, IssueViewRequestParams issueViewRequestParams, List<TreeFilterItem> filters, List<Long> jqlIssues, boolean isNumberingEnabled)
  {
    ContentHolder contentHolder = new ContentHolder();
    try {
      if ((reqSuiteId != null) && (reqSuiteId != "")) {
        ReqSuiteOutputBean reqSuite = requirementSuiteService.getSuite(Integer.valueOf(Integer.parseInt(reqSuiteId)));
        populateContent(reqSuite, view, issueViewRequestParams, contentHolder, filters, jqlIssues, isNumberingEnabled);
      }
      else
      {
        ReqSuiteOutputBean defaultSuite = requirementSuiteService.getDefaultSuite(projectId);
        if (defaultSuite != null) {
          Collection<ReqSuiteMemberOutputBean> rootSuitesAsMembers = defaultSuite.getSuiteMembers();
          if ((rootSuitesAsMembers != null) && (rootSuitesAsMembers.size() > 0)) {
            for (ReqSuiteMemberOutputBean rootSuiteAsMember : rootSuitesAsMembers) {
              ReqSuiteOutputBean rootReqSuite = requirementSuiteService.getSuite(Integer.valueOf(rootSuiteAsMember.getMember().intValue()));
              if (rootReqSuite != null) {
                populateContent(rootReqSuite, view, issueViewRequestParams, contentHolder, filters, jqlIssues, isNumberingEnabled);
              }
            }
          }
        }
        

        Collection<ReqRelationOutputBean> rootReqs = requirementServiceRM.getRootRequirements(projectId, null, true, null);
        if ((rootReqs != null) && (rootReqs.size() > 0)) {
          for (int index = 0; index < rootReqs.size(); index++) {
            if (contentHolder.getFirstIssue() == null) {
              Issue firstRequirement = issueManager.getIssueObject(((ReqRelationOutputBean)((List)rootReqs).get(index)).getChild());
              if (firstRequirement != null) {
                if (jqlIssues == null) {
                  if (PluginUtil.isFilterPassed(firstRequirement, filters)) {
                    contentHolder.getContent().append(view.getHeader(firstRequirement));
                    contentHolder.setFirstIssue(firstRequirement);
                    contentHolder.getContent().append(view.getBody(firstRequirement, issueViewRequestParams, "", filters, null, isNumberingEnabled));
                  }
                  else if (requirementServiceRM.areChildrenPassedFilters(firstRequirement.getId(), filters)) {
                    contentHolder.getContent().append(view.getHeader(firstRequirement));
                    contentHolder.setFirstIssue(firstRequirement);
                    contentHolder.getContent().append(view.getBody(firstRequirement, issueViewRequestParams, "", filters, null, isNumberingEnabled));
                  }
                  
                }
                else if (jqlIssues.contains(firstRequirement.getId())) {
                  contentHolder.getContent().append(view.getHeader(firstRequirement));
                  contentHolder.setFirstIssue(firstRequirement);
                  contentHolder.getContent().append(view.getBody(firstRequirement, issueViewRequestParams, "", filters, jqlIssues, isNumberingEnabled));
                }
                else if (requirementServiceRM.areChildrenPassedJql(firstRequirement.getId(), jqlIssues)) {
                  contentHolder.getContent().append(view.getHeader(firstRequirement));
                  contentHolder.setFirstIssue(firstRequirement);
                  contentHolder.getContent().append(view.getBody(firstRequirement, issueViewRequestParams, "", filters, jqlIssues, isNumberingEnabled));
                }
              }
            }
            else
            {
              ReqRelationOutputBean reqRelationOutputBean = (ReqRelationOutputBean)((List)rootReqs).get(index);
              Issue requirement = issueManager.getIssueObject(reqRelationOutputBean.getChild());
              if (requirement != null) {
                if (jqlIssues == null) {
                  if (PluginUtil.isFilterPassed(requirement, filters)) {
                    contentHolder.getContent().append(view.getBody(requirement, issueViewRequestParams, "", filters, null, isNumberingEnabled));
                  }
                  else if (requirementServiceRM.areChildrenPassedFilters(requirement.getId(), filters)) {
                    contentHolder.getContent().append(view.getBody(requirement, issueViewRequestParams, "", filters, null, isNumberingEnabled));
                  }
                  
                }
                else if (jqlIssues.contains(requirement.getId())) {
                  contentHolder.getContent().append(view.getBody(requirement, issueViewRequestParams, "", filters, jqlIssues, isNumberingEnabled));
                }
                else if (requirementServiceRM.areChildrenPassedJql(requirement.getId(), jqlIssues)) {
                  contentHolder.getContent().append(view.getBody(requirement, issueViewRequestParams, "", filters, jqlIssues, isNumberingEnabled));
                }
                
              }
            }
          }
        }
      }
    }
    catch (InvalidDataException e)
    {
      e.printStackTrace();
    }
    return contentHolder;
  }
  
  private void populateContent(ReqSuiteOutputBean reqSuite, SynapseRequirementIssueWordView view, IssueViewRequestParams issueViewRequestParams, ContentHolder contentHolder, List<TreeFilterItem> filters, List<Long> jqlIssues, boolean isNumberingEnabled) throws InvalidDataException {
    Collection<ReqSuiteMemberOutputBean> issueMembers = reqSuite.getIssueMembers();
    ReqSuiteMemberOutputBean issueMember; if ((issueMembers != null) && (issueMembers.size() > 0)) {
      for (int index = 0; index < issueMembers.size(); index++) {
        if (contentHolder.getFirstIssue() == null) {
          Issue firstRequirement = issueManager.getIssueObject(((ReqSuiteMemberOutputBean)((List)issueMembers).get(index)).getMember());
          if (firstRequirement != null) {
            if (jqlIssues == null) {
              if (PluginUtil.isFilterPassed(firstRequirement, filters)) {
                contentHolder.getContent().append(view.getHeader(firstRequirement));
                contentHolder.setFirstIssue(firstRequirement);
                contentHolder.getContent().append(view.getBody(firstRequirement, issueViewRequestParams, "", filters, null, isNumberingEnabled));
              }
              else if (requirementServiceRM.areChildrenPassedFilters(firstRequirement.getId(), filters)) {
                contentHolder.getContent().append(view.getHeader(firstRequirement));
                contentHolder.setFirstIssue(firstRequirement);
                contentHolder.getContent().append(view.getBody(firstRequirement, issueViewRequestParams, "", filters, null, isNumberingEnabled));
              }
              
            }
            else if (jqlIssues.contains(firstRequirement.getId())) {
              contentHolder.getContent().append(view.getHeader(firstRequirement));
              contentHolder.setFirstIssue(firstRequirement);
              contentHolder.getContent().append(view.getBody(firstRequirement, issueViewRequestParams, "", filters, jqlIssues, isNumberingEnabled));
            }
            else if (requirementServiceRM.areChildrenPassedJql(firstRequirement.getId(), jqlIssues)) {
              contentHolder.getContent().append(view.getHeader(firstRequirement));
              contentHolder.setFirstIssue(firstRequirement);
              contentHolder.getContent().append(view.getBody(firstRequirement, issueViewRequestParams, "", filters, jqlIssues, isNumberingEnabled));
            }
          }
        }
        else
        {
          issueMember = (ReqSuiteMemberOutputBean)((List)issueMembers).get(index);
          Issue requirement = issueManager.getIssueObject(issueMember.getMember());
          if (requirement != null) {
            if (jqlIssues == null) {
              if (PluginUtil.isFilterPassed(requirement, filters)) {
                contentHolder.getContent().append(view.getBody(requirement, issueViewRequestParams, "", filters, null, isNumberingEnabled));
              }
              else if (requirementServiceRM.areChildrenPassedFilters(requirement.getId(), filters)) {
                contentHolder.getContent().append(view.getBody(requirement, issueViewRequestParams, "", filters, null, isNumberingEnabled));
              }
              
            }
            else if (jqlIssues.contains(requirement.getId())) {
              contentHolder.getContent().append(view.getBody(requirement, issueViewRequestParams, "", filters, jqlIssues, isNumberingEnabled));
            }
            else if (requirementServiceRM.areChildrenPassedJql(requirement.getId(), jqlIssues)) {
              contentHolder.getContent().append(view.getBody(requirement, issueViewRequestParams, "", filters, jqlIssues, isNumberingEnabled));
            }
          }
        }
      }
    }
    


    Collection<ReqSuiteMemberOutputBean> suiteMembers = reqSuite.getSuiteMembers();
    if ((suiteMembers != null) && (suiteMembers.size() > 0)) {
      for (ReqSuiteMemberOutputBean suiteAsMember : suiteMembers) {
        ReqSuiteOutputBean subSuite = requirementSuiteService.getSuite(Integer.valueOf(suiteAsMember.getMember().intValue()));
        if (subSuite != null) {
          populateContent(subSuite, view, issueViewRequestParams, contentHolder, filters, jqlIssues, isNumberingEnabled);
        }
      }
    }
  }
}
