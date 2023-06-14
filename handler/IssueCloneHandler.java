package com.go2group.synapse.handler;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.go2group.synapse.ao.TestCycle;
import com.go2group.synapse.bean.TestCycleOutputBean;
import com.go2group.synapse.bean.TestPlanMemberOutputBean;
import com.go2group.synapse.config.SynapseConfig;
import com.go2group.synapse.manager.TestCaseManager;
import com.go2group.synapse.manager.TestPlanManager;
import com.go2group.synapse.util.comparator.ChangeItemBeanComparator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.log4j.Logger;






public class IssueCloneHandler
{
  private final TestCaseManager testcaseManager;
  private final SynapseConfig synapseConfig;
  private final TestPlanManager testplanManager;
  private static Logger log = Logger.getLogger(IssueCloneHandler.class);
  
  public IssueCloneHandler(TestCaseManager testcaseManager, SynapseConfig synapseConfig, TestPlanManager testplanManager) {
    this.testcaseManager = testcaseManager;
    this.synapseConfig = synapseConfig;
    this.testplanManager = testplanManager;
  }
  
  public void handle(Issue issue, Issue clonedIssue) {
    log.debug("Handling issueclone action");
    
    if (issue != null)
    {
      log.debug("Clone performed on the issue : " + issue);
      if (synapseConfig.getIssueTypeIds("Test Case").contains(issue.getIssueType().getId())) {
        if (clonedIssue == null) {
          clonedIssue = getClonedIssue(issue);
        }
        cloneTestSteps(issue, clonedIssue);
      } else if (synapseConfig.getIssueTypeIds("Test Plan").contains(issue.getIssueType().getId())) {
        if (clonedIssue == null) {
          clonedIssue = getClonedIssue(issue);
        }
        cloneTestPlanMembers(issue, clonedIssue);
        cloneCycles(issue, clonedIssue);
        cloneTesters(issue, clonedIssue);
      }
    }
    else {
      log.warn("Original Issue in Clone is received as NULL");
    }
  }
  
  private void cloneTestSteps(Issue issue, Issue clonedIssue) {
    log.info("Cloning Test Steps from original issue : " + issue + " to issue:" + clonedIssue);
    
    if (clonedIssue != null) {
      testcaseManager.cloneSteps(issue, clonedIssue);
      
      log.info("Test Steps Successfully Cloned to Issue : " + clonedIssue);
    }
  }
  
  private void cloneTestPlanMembers(Issue testPlan, Issue clonedIssue) {
    log.info("Cloning Test Plan Members from original issue : " + testPlan + " to issue:" + clonedIssue);
    if (clonedIssue != null) {
      List<TestPlanMemberOutputBean> testPlanMemberBeans = testplanManager.getTestPlanMembers(testPlan);
      if ((testPlanMemberBeans != null) && (testPlanMemberBeans.size() > 0)) {
        List<Issue> testCases = new ArrayList();
        for (TestPlanMemberOutputBean memberBean : testPlanMemberBeans) {
          Issue testCase = memberBean.getTestCase();
          if (testCase != null) {
            testCases.add(testCase);
          }
        }
        for (Issue testcase : testCases) {
          testplanManager.cloneTestPlanMembers(clonedIssue, testcase);
        }
      }
    }
  }
  
  private void cloneCycles(Issue issue, Issue clonedIssue) {
    log.info("Cloning Cycles from original issue :" + issue + " to issue:" + clonedIssue);
    
    if (clonedIssue != null) {
      TestCycle[] cycles = testplanManager.getCycleEntities(issue.getId());
      if ((cycles != null) && (cycles.length > 0)) {
        TestCycleOutputBean clonedCycle = null;
        for (TestCycle cycle : cycles) {
          Issue testPlan = ComponentAccessor.getIssueManager().getIssueObject(cycle.getTpId());
          Project project = testPlan.getProjectObject();
          
          testplanManager.cloneCycles(issue, clonedIssue, cycle, clonedCycle, project.getId());
        }
      }
    }
  }
  
  private void cloneTesters(Issue issue, Issue clonedIssue) {
    log.info("Cloning Testers from original issue :" + issue + " to issue:" + clonedIssue);
    
    if (clonedIssue != null) {
      List<TestPlanMemberOutputBean> orgTPMembers = testplanManager.getTestPlanMembers(issue);
      if ((orgTPMembers != null) && (!orgTPMembers.isEmpty())) {
        for (TestPlanMemberOutputBean orgTPMember : orgTPMembers) {
          testplanManager.cloneTesters(issue, clonedIssue, orgTPMember);
        }
      }
    }
  }
  
  private Issue getClonedIssue(Issue issue) {
    log.debug("Retrieving the newly cloned issue");
    
    Issue clonedIssue = null;
    
    List<ChangeItemBean> changeItemBeans = ComponentAccessor.getChangeHistoryManager().getChangeItemsForField(issue, "Link");
    Collections.sort(changeItemBeans, new ChangeItemBeanComparator());
    
    if ((changeItemBeans != null) && (changeItemBeans.size() > 0)) {
      clonedIssue = ComponentAccessor.getIssueManager().getIssueByCurrentKey(((ChangeItemBean)changeItemBeans.get(changeItemBeans.size() - 1)).getTo());
    }
    

    log.debug("Returning cloned issue :" + clonedIssue);
    
    return clonedIssue;
  }
}
