package com.go2group.synapse.tree;

import com.go2group.synapse.core.tree.EventReceiver;
import com.go2group.synapse.core.tree.TreeEventSubscriberLog;
import com.go2group.synapse.web.panel.AdvancedTestCycleTreeEventReceiver;
import com.go2group.synapse.web.panel.RequirementTreeEventReceiver;
import com.go2group.synapse.web.panel.TestCycleTreeEventReceiver;
import com.go2group.synapse.web.panel.TestPlanMemberTreeEventReceiver;
import com.go2group.synapse.web.panel.TestSuiteTreeEventReceiver;
import com.go2group.synapserm.event.receiver.RequirementSuiteTreeEventReceiver;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;


@Component
public class SynapseTreeEventSubscriberLog
  implements TreeEventSubscriberLog
{
  private List<EventReceiver> receivers = new ArrayList();
  
  private RequirementTreeEventReceiver requirementTreeEventReceiver;
  
  private TestSuiteTreeEventReceiver testSuiteTreeEventReceiver;
  private TestCycleTreeEventReceiver testCycleTreeEventReceiver;
  private AdvancedTestCycleTreeEventReceiver advancedTestCycleTreeEventReceiver;
  private RequirementSuiteTreeEventReceiver requirementSuiteTreeEventReceiver;
  private TestPlanMemberTreeEventReceiver testPlanMemberTreeEventReceiver;
  
  public SynapseTreeEventSubscriberLog(RequirementSuiteTreeEventReceiver requirementSuiteTreeEventReceiver, TestSuiteTreeEventReceiver testSuiteTreeEventReceiver, TestCycleTreeEventReceiver testCycleTreeEventReceiver, AdvancedTestCycleTreeEventReceiver advancedTestCycleTreeEventReceiver, TestPlanMemberTreeEventReceiver testPlanMemberTreeEventReceiver)
  {
    this.requirementSuiteTreeEventReceiver = requirementSuiteTreeEventReceiver;
    this.testSuiteTreeEventReceiver = testSuiteTreeEventReceiver;
    this.testCycleTreeEventReceiver = testCycleTreeEventReceiver;
    this.advancedTestCycleTreeEventReceiver = advancedTestCycleTreeEventReceiver;
    this.testPlanMemberTreeEventReceiver = testPlanMemberTreeEventReceiver;
    receivers.add(requirementSuiteTreeEventReceiver);
    receivers.add(testSuiteTreeEventReceiver);
    receivers.add(testCycleTreeEventReceiver);
    receivers.add(advancedTestCycleTreeEventReceiver);
    receivers.add(testPlanMemberTreeEventReceiver);
  }
  
  public List<EventReceiver> getReceivers()
  {
    return receivers;
  }
  
  public void addReceiver(EventReceiver eventReceiver)
  {
    receivers.add(eventReceiver);
  }
  
  public RequirementTreeEventReceiver getRequirementTreeEventReceiver() {
    return requirementTreeEventReceiver;
  }
  
  public void setRequirementTreeEventReceiver(RequirementTreeEventReceiver requirementTreeEventReceiver) {
    this.requirementTreeEventReceiver = requirementTreeEventReceiver;
  }
  
  public TestSuiteTreeEventReceiver getTestSuiteTreeEventReceiver() {
    return testSuiteTreeEventReceiver;
  }
  
  public void setTestSuiteTreeEventReceiver(TestSuiteTreeEventReceiver testSuiteTreeEventReceiver) {
    this.testSuiteTreeEventReceiver = testSuiteTreeEventReceiver;
  }
  
  public TestCycleTreeEventReceiver getTestCycleTreeEventReceiver() {
    return testCycleTreeEventReceiver;
  }
  
  public void setTestCycleTreeEventReceiver(TestCycleTreeEventReceiver testCycleTreeEventReceiver) {
    this.testCycleTreeEventReceiver = testCycleTreeEventReceiver;
  }
  
  public RequirementSuiteTreeEventReceiver getRequirementSuiteTreeEventReceiver() {
    return requirementSuiteTreeEventReceiver;
  }
  
  public void setRequirementSuiteTreeEventReceiver(RequirementSuiteTreeEventReceiver requirementSuiteTreeEventReceiver) {
    this.requirementSuiteTreeEventReceiver = requirementSuiteTreeEventReceiver;
  }
  
  public TestPlanMemberTreeEventReceiver getTestPlanMemberTreeEventReceiver() {
    return testPlanMemberTreeEventReceiver;
  }
  
  public void setTestPlanMemberTreeEventReceiver(TestPlanMemberTreeEventReceiver testPlanMemberTreeEventReceiver) {
    this.testPlanMemberTreeEventReceiver = testPlanMemberTreeEventReceiver;
  }
  
  public AdvancedTestCycleTreeEventReceiver getAdvancedTestCycleTreeEventReceiver() {
    return advancedTestCycleTreeEventReceiver;
  }
  
  public void setAdvancedTestCycleTreeEventReceiver(AdvancedTestCycleTreeEventReceiver advancedTestCycleTreeEventReceiver) {
    this.advancedTestCycleTreeEventReceiver = advancedTestCycleTreeEventReceiver;
  }
}
