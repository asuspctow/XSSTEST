package com.go2group.synapse.issue.views;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.views.util.IssueViewUtil;
import com.atlassian.jira.issue.views.util.WordViewUtils;
import com.atlassian.jira.plugin.issueview.IssueViewRequestParams;
import com.atlassian.jira.plugin.searchrequestview.RequestHeaders;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.go2group.synapse.config.SynapseConfig;
import com.go2group.synapse.service.ConfigService;
import com.go2group.synapse.service.RequirementService;
import com.go2group.synapse.service.TestCaseToRequirementLinkService;
import com.go2group.synapse.service.TestCycleService;
import com.go2group.synapse.service.TestPlanMemberService;
import com.go2group.synapse.service.TestPlanService;
import com.go2group.synapse.service.TestRunRequirementService;
import com.go2group.synapse.service.TestRunService;
import com.go2group.synapse.service.TestStepService;
import com.go2group.synapse.service.TestSuiteService;
import com.go2group.synapse.service.runattribute.RunAttributeService;
import com.go2group.synapserm.bean.tree.impl.RequirementTreePool;
import com.go2group.synapserm.service.RequirementSuiteService;





public class SynapseIssuePdfView
  extends AbstractSynapseIssueHtmlView
{
  public SynapseIssuePdfView(JiraAuthenticationContext authenticationContext, @ComponentImport ApplicationProperties applicationProperties, CommentManager commentManager, FieldScreenRendererFactory fieldScreenRendererFactory, IssueViewUtil issueViewUtil, FieldVisibilityManager fieldVisibilityManager, TestStepService testStepService, TestCaseToRequirementLinkService tcrLinkService, TestSuiteService testSuiteService, TestRunService tRunService, TestPlanMemberService tpMemberService, @ComponentImport UserManager userManager, ConfigService configService, RequirementService requirementService, SynapseConfig synapseConfig, TestPlanService testPlanService, TestCycleService testCycleService, TestPlanMemberService testPlanMemberService, RequirementTreePool treePool, RequirementSuiteService requirementSuiteService, TestRunRequirementService testRunRequirementService, RunAttributeService runAttributeService)
  {
    super(authenticationContext, applicationProperties, commentManager, fieldScreenRendererFactory, issueViewUtil, fieldVisibilityManager, testStepService, tcrLinkService, testSuiteService, tRunService, tpMemberService, userManager, configService, requirementService, synapseConfig, testPlanService, testCycleService, testPlanMemberService, treePool, requirementSuiteService, testRunRequirementService, runAttributeService);
  }
  

  protected String getLinkToPrevious(Issue issue)
  {
    return null;
  }
  
  protected boolean printCssLinks() {
    return false;
  }
  
  public void writeHeaders(Issue issue, RequestHeaders requestHeaders, IssueViewRequestParams issueViewRequestParams)
  {
    WordViewUtils.writeGenericNoCacheHeaders(requestHeaders);
    requestHeaders.addHeader("content-disposition", "attachment;filename=\"" + issue.getKey() + ".pdf\";");
  }
  
  public void writeHeaders(String fileName, RequestHeaders requestHeaders, IssueViewRequestParams issueViewRequestParams)
  {
    WordViewUtils.writeGenericNoCacheHeaders(requestHeaders);
    requestHeaders.addHeader("content-disposition", "attachment;filename=\"" + fileName + ".pdf\";");
  }
}
