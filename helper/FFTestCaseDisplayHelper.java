package com.go2group.synapse.helper;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.I18nHelper.BeanFactory;
import com.atlassian.jira.util.VelocityParamFactory;
import com.atlassian.velocity.VelocityManager;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

public class FFTestCaseDisplayHelper
{
  private static final String FF_TC_DISPLAY_TEMPLATE = "/templates/web/view/freeform-testcase-view.vm";
  private static final Logger log = Logger.getLogger(FFTestCaseDisplayHelper.class);
  
  public FFTestCaseDisplayHelper() {}
  
  public String getDisplayHtml(List<Issue> issues, String jql, String projectKey) { log.debug("Building HTML for freeform test case display");
    
    Map<String, Object> velocityParams = ComponentAccessor.getVelocityParamFactory().getDefaultVelocityParams();
    
    velocityParams.put("issues", issues);
    velocityParams.put("i18n", getI18n());
    velocityParams.put("jql", jql);
    velocityParams.put("projectKey", projectKey);
    velocityParams.put("baseurl", ComponentAccessor.getApplicationProperties().getString("jira.baseurl"));
    
    log.debug("VelocityParams :" + velocityParams);
    
    VelocityManager vm = ComponentAccessor.getVelocityManager();
    String html = vm.getEncodedBody("", "/templates/web/view/freeform-testcase-view.vm", null, velocityParams);
    
    log.debug("Returning html : " + html);
    
    return html;
  }
  
  private I18nHelper getI18n() {
    return ComponentAccessor.getI18nHelperFactory().getInstance(ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser());
  }
}
