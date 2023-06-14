package com.go2group.synapse.listener;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.go2group.synapse.bean.TestSuiteOutputBean;
import com.go2group.synapse.config.SynapseConfig;
import com.go2group.synapse.core.exception.InvalidDataException;
import com.go2group.synapse.manager.CleanupManager;
import com.go2group.synapse.service.RunStatusService;
import com.go2group.synapse.service.TestRunService;
import com.go2group.synapse.service.TestSuiteService;
import com.opensymphony.workflow.InvalidActionException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericDelegator;
import org.ofbiz.core.entity.GenericValue;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;



@Component
public class SynapseIssueListener
  implements InitializingBean, DisposableBean
{
  private static final Logger log = Logger.getLogger(SynapseIssueListener.class);
  
  private final EventPublisher eventPublisher;
  
  private final SynapseConfig synapseConfig;
  
  private final TestRunService testRunService;
  
  private final TestSuiteService testSuiteService;
  
  private final CleanupManager cleanupManager;
  
  private final RunStatusService runStatusService;
  

  public SynapseIssueListener(@ComponentImport EventPublisher eventPublisher, SynapseConfig synapseConfig, TestRunService testRunService, TestSuiteService testSuiteService, CleanupManager cleanupManager, RunStatusService runStatusService)
  {
    this.eventPublisher = eventPublisher;
    this.synapseConfig = synapseConfig;
    this.testRunService = testRunService;
    this.testSuiteService = testSuiteService;
    this.cleanupManager = cleanupManager;
    this.runStatusService = runStatusService;
  }
  
  public void destroy()
    throws Exception
  {
    eventPublisher.unregister(this);
  }
  
  public void afterPropertiesSet() throws Exception
  {
    eventPublisher.register(this);
  }
  
  @EventListener
  public void handleIssueEvent(IssueEvent issueEvent) {
    Issue issue = issueEvent.getIssue();
    IssueType issueType = issue.getIssueType();
    
    if ((EventType.ISSUE_CREATED_ID.equals(issueEvent.getEventTypeId())) && 
      (synapseConfig.getIssueTypeIds("Test Case").contains(issueType.getId()))) {
      updateDefaultRunStatus(issue);
    }
    

    if ((EventType.ISSUE_UPDATED_ID.equals(issueEvent.getEventTypeId())) && 
      (synapseConfig.getIssueTypeIds("Test Case").contains(issueType.getId()))) {
      handleTestRunUpdate(issueEvent);
    }
    

    if ((EventType.ISSUE_MOVED_ID.equals(issueEvent.getEventTypeId())) || (EventType.ISSUE_UPDATED_ID.equals(issueEvent.getEventTypeId()))) {
      handleTestCaseMove(issueEvent);
      handleRequirementMove(issueEvent);
    }
  }
  
  private void updateDefaultRunStatus(Issue issue) {
    runStatusService.updateDefaultRunStatus(issue);
  }
  
  private void handleTestRunUpdate(IssueEvent issueEvent)
  {
    List<GenericValue> changeItems = null;
    boolean synapseItemsChanged = false;
    try {
      GenericValue changeLog = issueEvent.getChangeLog();
      Map<String, Object> fields = new HashMap();
      fields.put("group", changeLog.get("id"));
      
      changeItems = internalDelegator.findByAnd("ChangeItem", fields);
      for (GenericValue changedField : changeItems) {
        String field = changedField.getString("field");
        if ((field.equals("summary")) || (field.equals("description"))) {
          synapseItemsChanged = true;
          break;
        }
      }
      if (synapseItemsChanged) {
        testRunService.updateTestCaseChangeToRun(issueEvent.getIssue().getId(), "test-case-modified");
      }
    } catch (Exception e) {
      log.error(e.getMessage());
      log.debug(e.getMessage(), e);
    }
  }
  
  private void handleTestCaseMove(IssueEvent issueEvent) {
    Issue movedIssue = issueEvent.getIssue();
    IssueType issueType = movedIssue.getIssueType();
    



    if (!synapseConfig.getIssueTypeIds("Test Case").contains(issueType.getId())) {
      try {
        List<TestSuiteOutputBean> testSuiteOutputBeans = testSuiteService.getTestSuitesInTestCase(movedIssue.getId());
        if ((testSuiteOutputBeans != null) && (testSuiteOutputBeans.size() > 0)) {
          log.info("Looks like a Test Case (" + movedIssue.getKey() + ") is moved to another type:" + issueType.getName());
          for (TestSuiteOutputBean testSuiteOutputBean : testSuiteOutputBeans) {
            testSuiteService.removeMember(testSuiteOutputBean.getID(), movedIssue.getId());
            log.info("Removed erstwhile Test Case (" + movedIssue.getKey() + ") from Test Suite:" + testSuiteOutputBean.getName());
          }
        }
        
        handleTestCaseDelete(movedIssue.getId());
      } catch (InvalidDataException e) {
        log.error(e.getMessage());
        log.debug(e.getMessage(), e);
      }
    }
  }
  
  private void handleTestCaseDelete(Long testCaseId) {
    try {
      cleanupManager.cleanTestCaseReqLinkByTestCase(testCaseId);
      cleanupManager.cleanTestStepParams(testCaseId);
      cleanupManager.cleanTestStep(testCaseId);
      cleanupManager.cleanTestPlanMembersByTestCase(testCaseId);
      cleanupManager.cleanTestRunReference(testCaseId);
      cleanupManager.cleanTestSuiteMember(testCaseId);
      cleanupManager.cleanPlanMemberSuite(testCaseId);
    } catch (Exception e) {
      log.error(e.getMessage());
      log.debug(e.getMessage(), e);
    }
  }
  
  private void handleRequirementMove(IssueEvent issueEvent) {
    Issue movedIssue = issueEvent.getIssue();
    IssueType issueType = movedIssue.getIssueType();
    log.info("Requirement '" + movedIssue.getKey() + "' is moved to another type:" + issueType.getName());
    



    if (!synapseConfig.getIssueTypeIds("Requirement").contains(issueType.getId())) {
      try {
        cleanupManager.cleanRequirementTestRunLink(movedIssue.getId());
        cleanupManager.cleanTestCaseReqLinkByRequirement(movedIssue.getId());
      } catch (InvalidActionException e) {
        log.error(e.getMessage());
        log.debug(e.getMessage(), e);
      }
    }
  }
}
