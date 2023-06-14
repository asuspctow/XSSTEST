package com.go2group.synapse.rest.pub;

import com.go2group.synapse.bean.AutomationRunStepOutputBean;
import com.go2group.synapse.bean.IssueWrapperBean;
import com.go2group.synapse.bean.TestRunAttachmentOutputBean;
import com.go2group.synapse.bean.TestRunHistoryOutputBean;
import com.go2group.synapse.bean.TestRunStepOutputBean;
import java.util.Collection;
import javax.xml.bind.annotation.XmlElement;









class TestRunDetailRestBean
{
  @XmlElement
  private Collection<TestRunStepRestBean> testRunSteps;
  private Collection<TestRunStepOutputBean> tmpTestRunSteps;
  @XmlElement
  private Collection<AutomationRunStepOutputBean> automationRunSteps;
  @XmlElement
  private Collection<TestRunHistoryOutputBean> testRunHistory;
  @XmlElement
  private Collection<IssueWrapperBean> testRunBugs;
  @XmlElement
  private Collection<TestRunAttachmentOutputBean> testRunAttachments;
  @XmlElement
  private Integer prevRunId;
  @XmlElement
  private Integer nextRunId;
  
  TestRunDetailRestBean() {}
  
  public Collection<TestRunStepRestBean> getTestRunSteps()
  {
    return testRunSteps;
  }
  
  public void setTestRunSteps(Collection<TestRunStepRestBean> testRunSteps) {
    this.testRunSteps = testRunSteps;
  }
  

  public Collection<TestRunStepOutputBean> getTmpTestRunSteps()
  {
    return tmpTestRunSteps;
  }
  
  public void setTmpTestRunSteps(Collection<TestRunStepOutputBean> tmpTestRunSteps) {
    this.tmpTestRunSteps = tmpTestRunSteps;
  }
  
  public Collection<AutomationRunStepOutputBean> getAutomationRunSteps() {
    return automationRunSteps;
  }
  
  public void setAutomationRunSteps(Collection<AutomationRunStepOutputBean> automationRunSteps) {
    this.automationRunSteps = automationRunSteps;
  }
  
  public Collection<TestRunHistoryOutputBean> getTestRunHistory() {
    return testRunHistory;
  }
  
  public void setTestRunHistory(Collection<TestRunHistoryOutputBean> testRunHistory) {
    this.testRunHistory = testRunHistory;
  }
  
  public Collection<IssueWrapperBean> getTestRunBugs() {
    return testRunBugs;
  }
  
  public void setTestRunBugs(Collection<IssueWrapperBean> testRunBugs) {
    this.testRunBugs = testRunBugs;
  }
  
  public Collection<TestRunAttachmentOutputBean> getTestRunAttachments() {
    return testRunAttachments;
  }
  
  public void setTestRunAttachments(Collection<TestRunAttachmentOutputBean> testRunAttachments) {
    this.testRunAttachments = testRunAttachments;
  }
  
  public Integer getPrevRunId() {
    return prevRunId;
  }
  
  public void setPrevRunId(Integer prevRunId) {
    this.prevRunId = prevRunId;
  }
  
  public Integer getNextRunId() {
    return nextRunId;
  }
  
  public void setNextRunId(Integer nextRunId) {
    this.nextRunId = nextRunId;
  }
}
