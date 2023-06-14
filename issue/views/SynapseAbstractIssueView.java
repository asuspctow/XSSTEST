package com.go2group.synapse.issue.views;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.LookAndFeelBean;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.plugin.issueview.AbstractIssueView;
import com.atlassian.jira.plugin.issueview.IssueViewModuleDescriptor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.VelocityParamFactory;
import java.util.HashMap;
import java.util.Map;

public abstract class SynapseAbstractIssueView extends AbstractIssueView
{
  protected final JiraAuthenticationContext jiraAuthenticationContext;
  protected final CommentManager commentManager;
  protected final ApplicationProperties applicationProperties;
  private final VelocityParamFactory velocityParamFactory;
  
  protected SynapseAbstractIssueView(JiraAuthenticationContext jiraAuthenticationContext, CommentManager commentManager, ApplicationProperties applicationProperties, VelocityParamFactory velocityParamFactory)
  {
    this.jiraAuthenticationContext = jiraAuthenticationContext;
    this.commentManager = commentManager;
    this.applicationProperties = applicationProperties;
    this.velocityParamFactory = velocityParamFactory;
  }
  


  protected abstract String getLinkToPrevious(Issue paramIssue);
  


  protected abstract boolean printCssLinks();
  

  public String getHeader(String title, String linkToPrevious)
  {
    Map<String, Object> bodyParams = velocityParamFactory.getDefaultVelocityParams(jiraAuthenticationContext);
    
    bodyParams.put("title", title);
    bodyParams.put("contentType", descriptor.getContentType() + "; charset=" + applicationProperties.getEncoding());
    
    LookAndFeelBean lookAndFeelBean = LookAndFeelBean.getInstance(applicationProperties);
    bodyParams.put("linkColour", lookAndFeelBean.getTextLinkColour());
    bodyParams.put("linkAColour", lookAndFeelBean.getTextActiveLinkColour());
    bodyParams.put("showCssLinks", printCssLinks() ? Boolean.TRUE : Boolean.FALSE);
    bodyParams.put("linkToPrevious", linkToPrevious);
    
    bodyParams.put("style", getStyleSheetHtml());
    
    return descriptor.getHtml("header", bodyParams);
  }
  
  public String getStyleSheetHtml() {
    return descriptor.getHtml("style", new HashMap());
  }
}
