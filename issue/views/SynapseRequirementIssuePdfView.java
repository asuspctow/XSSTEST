package com.go2group.synapse.issue.views;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.views.util.IssueViewUtil;
import com.atlassian.jira.issue.views.util.WordViewUtils;
import com.atlassian.jira.plugin.issueview.IssueViewModuleDescriptor;
import com.atlassian.jira.plugin.issueview.IssueViewRequestParams;
import com.atlassian.jira.plugin.searchrequestview.RequestHeaders;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.go2group.synapse.config.SynapseConfig;
import com.go2group.synapse.core.tree.bean.TreeFilterItem;
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
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class SynapseRequirementIssuePdfView
  extends AbstractSynapseIssueHtmlView
{
  private static final Logger log = Logger.getLogger(SynapseRequirementIssuePdfView.class);
  
  private final RequirementService requirementService;
  
  private final TestStepService testStepService;
  
  private final SynapseConfig synapseConfig;
  
  private final TestCaseToRequirementLinkService tcrLinkService;
  

  public SynapseRequirementIssuePdfView(JiraAuthenticationContext authenticationContext, @ComponentImport ApplicationProperties applicationProperties, CommentManager commentManager, FieldScreenRendererFactory fieldScreenRendererFactory, IssueViewUtil issueViewUtil, FieldVisibilityManager fieldVisibilityManager, TestStepService testStepService, TestCaseToRequirementLinkService tcrLinkService, TestSuiteService testSuiteService, TestRunService tRunService, TestPlanMemberService tpMemberService, @ComponentImport UserManager userManager, ConfigService configService, RequirementService requirementService, SynapseConfig synapseConfig, TestPlanService testPlanService, TestCycleService testCycleService, TestPlanMemberService testPlanMemberService, RequirementTreePool treePool, RequirementSuiteService requirementSuiteService, TestRunRequirementService testRunRequirementService, RunAttributeService runAttributeService)
  {
    super(authenticationContext, applicationProperties, commentManager, fieldScreenRendererFactory, issueViewUtil, fieldVisibilityManager, testStepService, tcrLinkService, testSuiteService, tRunService, tpMemberService, userManager, configService, requirementService, synapseConfig, testPlanService, testCycleService, testPlanMemberService, treePool, requirementSuiteService, testRunRequirementService, runAttributeService);
    

    this.requirementService = requirementService;
    this.testStepService = testStepService;
    this.tcrLinkService = tcrLinkService;
    this.synapseConfig = synapseConfig;
  }
  
  protected String getLinkToPrevious(Issue issue) {
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
  
  public void writeHeaders(String projectKey, RequestHeaders requestHeaders, IssueViewRequestParams issueViewRequestParams) {
    WordViewUtils.writeGenericNoCacheHeaders(requestHeaders);
    requestHeaders.addHeader("content-disposition", "attachment;filename=\"" + projectKey + ".pdf\";");
  }
  

  public String getContent(Issue issue, IssueViewRequestParams issueViewRequestParams, String source, List<TreeFilterItem> filters, List<Long> jqlIssues, boolean isNumberingEnabled)
  {
    return super.getHeader(issue) + getBody(issue, issueViewRequestParams, source, filters, jqlIssues, isNumberingEnabled) + super.getFooter(issue);
  }
  
  public String getContent(List<Issue> issues, IssueViewRequestParams issueViewRequestParams, String source, List<TreeFilterItem> filters, List<Long> jqlIssues, boolean isNumberingEnabled) {
    log.debug("getcontent starts...." + issues.size());
    StringBuffer content = new StringBuffer();
    String header = super.getHeader((Issue)issues.get(0));
    content.append(header);
    String footer = super.getFooter((Issue)issues.get(0));
    
    String body = "";
    int counter = 1;
    for (Issue issue : issues) {
      try {
        content.append(getBody(issue, issueViewRequestParams, counter + "", filters, jqlIssues, isNumberingEnabled));
        counter++;
      } catch (Exception e) {
        log.debug(e.getMessage(), e);
        log.error(e.getMessage());
      }
    }
    
    content.append(footer);
    
    return content.toString();
  }
  
  public String getBody(Issue issue, IssueViewRequestParams issueViewFieldParams, String counter, List<TreeFilterItem> filters, List<Long> jqlIssues, boolean isNumberingEnabled)
  {
    Map<String, Object> bodyParams = null;
    StringBuffer content = new StringBuffer();
    Project project = issue.getProjectObject();
    if (StringUtils.isBlank(counter)) {
      counter = "1";
    }
    
    bodyParams = super.getRequirementBody(issue, issueViewFieldParams, counter, filters, jqlIssues, isNumberingEnabled);
    String tempStr = descriptor.getHtml("view", bodyParams);
    content.append(tempStr);
    
    return content.toString();
  }
}
