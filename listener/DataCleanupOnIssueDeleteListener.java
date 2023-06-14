package com.go2group.synapse.listener;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.go2group.synapse.config.SynapseConfig;
import com.go2group.synapse.manager.CleanupManager;
import java.util.List;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Component
public class DataCleanupOnIssueDeleteListener
  implements InitializingBean, DisposableBean
{
  private static final Logger log = Logger.getLogger(DataCleanupOnIssueDeleteListener.class);
  
  private final EventPublisher eventPublisher;
  
  private final SynapseConfig synapseConfig;
  
  private final CleanupManager cleanupManager;
  
  public DataCleanupOnIssueDeleteListener(@ComponentImport EventPublisher eventPublisher, SynapseConfig synapseConfig, CleanupManager cleanupManager)
  {
    this.eventPublisher = eventPublisher;
    this.synapseConfig = synapseConfig;
    this.cleanupManager = cleanupManager;
  }
  
  public void destroy() throws Exception
  {
    eventPublisher.unregister(this);
  }
  
  public void afterPropertiesSet() throws Exception
  {
    eventPublisher.register(this);
  }
  
  @EventListener
  public void handleIssueEvent(IssueEvent issueEvent)
  {
    log.debug("Received issue event for handling data cleanup on Issue Delete with for issue:" + issueEvent.getIssue());
    

    if (EventType.ISSUE_DELETED_ID.equals(issueEvent.getEventTypeId()))
    {
      log.debug("Received issue event :" + issueEvent.getEventTypeId());
      
      Issue issue = issueEvent.getIssue();
      IssueType issueType = issue.getIssueType();
      
      if (synapseConfig.getIssueTypeIds("Bug").contains(issueType.getId())) {
        handleBugDelete(issue);
      } else if (synapseConfig.getIssueTypeIds("Requirement").contains(issueType.getId())) {
        handleRequirementDelete(issue);
      } else if (synapseConfig.getIssueTypeIds("Test Case").contains(issueType.getId())) {
        handleTestCaseDelete(issue);
      } else if (synapseConfig.getIssueTypeIds("Test Plan").contains(issueType.getId())) {
        handleTestPlanDelete(issue);
      }
    }
  }
  
  private void handleBugDelete(Issue bug)
  {
    log.info("Handling data cleanup on bug delete for bug :" + bug);
    
    cleanupManager.cleanTestRunBug(bug.getId());
  }
  
  private void handleRequirementDelete(Issue requirement)
  {
    log.info("Handling data cleanup on requirement delete for requirement :" + requirement);
    
    cleanupManager.cleanTestCaseReqLinkByRequirement(requirement.getId());
    cleanupManager.cleanRequirementLink(requirement.getId());
    cleanupManager.cleanRequirementSuite(requirement.getId());
    cleanupManager.cleanRequirementTestRunLink(requirement.getId());
  }
  
  private void handleTestCaseDelete(Issue testCase)
  {
    log.info("Handling data cleanup on testcase delete for testcase :" + testCase);
    
    cleanupManager.cleanTestCaseReqLinkByTestCase(testCase.getId());
    cleanupManager.cleanTestStepParams(testCase.getId());
    cleanupManager.cleanTestStep(testCase.getId());
    cleanupManager.cleanTestPlanMembersByTestCase(testCase.getId());
    cleanupManager.cleanTestRunReference(testCase.getId());
    cleanupManager.cleanTestSuiteMember(testCase.getId());
    cleanupManager.cleanPlanMemberSuite(testCase.getId());
  }
  
  private void handleTestPlanDelete(Issue testPlan) {
    log.info("Handling data cleanup on testplan delete for testplan:" + testPlan);
    
    cleanupManager.cleanTestPlanMembersByTestPlan(testPlan.getId());
    cleanupManager.cleanTestCycle(testPlan.getId());
  }
}
