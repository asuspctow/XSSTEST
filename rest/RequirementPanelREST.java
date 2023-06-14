package com.go2group.synapse.rest;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.go2group.synapse.bean.HtmlResponseWrapper;
import com.go2group.synapse.bean.TCReqLinkBean;
import com.go2group.synapse.bean.TestRunOutputBean;
import com.go2group.synapse.bean.TestRunRequirementOutputBean;
import com.go2group.synapse.core.audit.log.ActionEnum;
import com.go2group.synapse.core.audit.log.AuditLogInputBean;
import com.go2group.synapse.core.audit.log.ModuleEnum;
import com.go2group.synapse.core.audit.log.SourceEnum;
import com.go2group.synapse.core.audit.log.service.AuditLogService;
import com.go2group.synapse.core.exception.InvalidDataException;
import com.go2group.synapse.core.util.PermissionUtil;
import com.go2group.synapse.service.TestCaseToRequirementLinkService;
import com.go2group.synapse.service.TestRunRequirementService;
import com.go2group.synapse.service.TestRunService;
import com.go2group.synapse.util.PluginUtil;
import com.go2group.synapse.web.panel.RequirementPanelIssueViewHelper;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;











@Path("requirementpanel")
@Consumes({"application/json"})
@Produces({"application/json"})
public class RequirementPanelREST
{
  private final TestCaseToRequirementLinkService tcrLinkService;
  private final TestRunService testRunService;
  private final TestRunRequirementService testRunRequirementService;
  private final I18nHelper i18n;
  private final PermissionUtil permissionUtil;
  private final IssueManager issueManager;
  private static final Logger log = Logger.getLogger(RequirementPanelREST.class);
  
  private final AuditLogService auditLogService;
  

  @Autowired
  public RequirementPanelREST(@ComponentImport I18nHelper i18n, PermissionUtil permissionUtil, @ComponentImport IssueManager issueManager, TestCaseToRequirementLinkService tcrLinkService, TestRunService testRunService, TestRunRequirementService testRunRequirementService, AuditLogService auditLogService)
  {
    this.tcrLinkService = tcrLinkService;
    this.i18n = i18n;
    this.permissionUtil = permissionUtil;
    this.issueManager = issueManager;
    this.testRunService = testRunService;
    this.testRunRequirementService = testRunRequirementService;
    this.auditLogService = auditLogService;
  }
  
  @POST
  @Path("linkRequirements")
  public Response linkRequirements(TCReqLinkBean tcReqLinkBean)
  {
    log.debug("Linking Requirements, received arguments : " + tcReqLinkBean);
    
    String requirementKeyString = tcReqLinkBean.getRequirementKeys();
    
    if ((requirementKeyString != null) && (requirementKeyString.trim().length() > 0)) {
      String[] requirementKeyArr = requirementKeyString.split(",");
      String history = tcReqLinkBean.getHistory();
      try
      {
        List<Issue> linkedRequirements = tcrLinkService.linkRequirement(tcReqLinkBean.getTestcaseKey(), Arrays.asList(requirementKeyArr), ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser(), "test-case-link", true);
        
        AuditLogInputBean auditLogInputBean = PluginUtil.getAuditLogInputBean(ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser());
        auditLogInputBean.setAction(ActionEnum.LINKED);
        auditLogInputBean.setModule(ModuleEnum.TEST_CASE);
        auditLogInputBean.setSource(SourceEnum.WEB_PAGE.getName());
        auditLogInputBean.setLogTime(new Date());
        auditLogInputBean.setLog("Linked Requirement(s) " + Arrays.asList(requirementKeyArr) + " to Test Case '" + tcReqLinkBean.getTestcaseKey() + "'");
        auditLogService.createAuditLog(auditLogInputBean);
        
        Iterator localIterator1;
        if ((StringUtils.isNotBlank(history)) && ("yes".equalsIgnoreCase(history)) && 
          (linkedRequirements != null) && (linkedRequirements.size() > 0)) {
          Issue testCase = issueManager.getIssueByCurrentKey(tcReqLinkBean.getTestcaseKey());
          if (testCase != null) {
            List<TestRunOutputBean> testRuns = testRunService.getTestRuns(testCase.getId(), ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser());
            if ((testRuns != null) && (testRuns.size() > 0)) {
              for (localIterator1 = testRuns.iterator(); localIterator1.hasNext();) { testRun = (TestRunOutputBean)localIterator1.next();
                for (Issue requirement : linkedRequirements) {
                  TestRunRequirementOutputBean runRequirementOutputBean = testRunRequirementService.createTestRunRequirement(testRun.getID(), requirement.getId());
                  
                  auditLogInputBean.clear();
                  auditLogInputBean.setAction(ActionEnum.LINKED);
                  auditLogInputBean.setModule(ModuleEnum.TEST_CASE);
                  auditLogInputBean.setSource(SourceEnum.WEB_PAGE.getName());
                  auditLogInputBean.setLogTime(new Date());
                  auditLogInputBean.setLog("Linked Test Run 'TR" + runRequirementOutputBean.getTestRun().getID() + "' of Test Case '" + tcReqLinkBean.getTestcaseKey() + "' to Requirement '" + requirement.getKey() + "', while linking");
                  auditLogService.createAuditLog(auditLogInputBean);
                }
              }
            }
          }
        }
        

        TestRunOutputBean testRun;
        
        log.debug("Linked requirements : " + linkedRequirements);
        

        Issue issueInContext = issueManager.getIssueObject(tcReqLinkBean.getTestcaseKey());
        
        RequirementPanelIssueViewHelper helper = new RequirementPanelIssueViewHelper(permissionUtil);
        
        return Response.ok(new HtmlResponseWrapper(helper.getHtml(linkedRequirements, issueInContext))).build();
      } catch (InvalidDataException e) {
        log.debug(e.getMessage(), e);
        log.warn(e.getMessage());
        return Response.serverError().entity(e.getMessage()).build();
      } catch (Exception e) {
        log.debug(e.getMessage(), e);
        log.warn(e.getMessage());
        return 
          Response.serverError()
          .entity(i18n.getText("errormessage.unexpected.server.error") + i18n.getText("servererror.rest.link.requirements.failed"))
          .build();
      }
    }
    log.debug("No requirement keys found for linking");
    
    return Response.ok(Collections.emptyList()).build();
  }
  

  @DELETE
  @Path("delinkRequirement")
  public Response delinkRequirement(TCReqLinkBean tcReqLinkBean)
  {
    log.debug("Delinking Requirement, received arguments : " + tcReqLinkBean);
    
    try
    {
      ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
      tcrLinkService.delinkRequirement(tcReqLinkBean.getTestcaseKey(), tcReqLinkBean.getRequirementKeys(), true, user);
      
      AuditLogInputBean auditLogInputBean = PluginUtil.getAuditLogInputBean(user);
      auditLogInputBean.setAction(ActionEnum.DELINKED);
      auditLogInputBean.setModule(ModuleEnum.TEST_CASE);
      auditLogInputBean.setSource(SourceEnum.WEB_PAGE.getName());
      auditLogInputBean.setLogTime(new Date());
      auditLogInputBean.setLog("Delinked Requirement(s) '" + tcReqLinkBean.getRequirementKeys() + "' from Test Case '" + tcReqLinkBean.getTestcaseKey() + "'");
      auditLogService.createAuditLog(auditLogInputBean);
      

      Issue testCase = issueManager.getIssueByCurrentKey(tcReqLinkBean.getTestcaseKey());
      Issue requirement; if (testCase != null) {
        requirement = issueManager.getIssueByCurrentKey(tcReqLinkBean.getRequirementKeys());
        if (requirement != null) {
          List<TestRunOutputBean> testRuns = testRunService.getTestRuns(testCase.getId(), user);
          if ((testRuns != null) && (testRuns.size() > 0)) {
            for (TestRunOutputBean testRun : testRuns) {
              testRunRequirementService.deleteTestRunRequirement(testRun.getID(), requirement.getId());
              
              auditLogInputBean.clear();
              auditLogInputBean.setAction(ActionEnum.DELINKED);
              auditLogInputBean.setModule(ModuleEnum.TEST_CASE);
              auditLogInputBean.setSource(SourceEnum.WEB_PAGE.getName());
              auditLogInputBean.setLogTime(new Date());
              auditLogInputBean.setLog("Delinked Test Run 'TR" + testRun.getID() + "' of Test Case '" + tcReqLinkBean.getTestcaseKey() + "' from Requirement '" + requirement.getKey() + "', while delinking");
              auditLogService.createAuditLog(auditLogInputBean);
            }
          }
        }
      }
      


      return Response.ok().build();
    } catch (InvalidDataException e) {
      log.debug(e.getMessage(), e);
      log.warn(e.getMessage());
      return Response.serverError().entity(e.getMessage()).build();
    } catch (Exception e) {
      log.debug(e.getMessage(), e);
      log.warn(e.getMessage()); }
    return 
    

      Response.serverError().entity(i18n.getText("errormessage.unexpected.server.error") + i18n.getText("servererror.rest.delink.requirements.failed")).build();
  }
}
