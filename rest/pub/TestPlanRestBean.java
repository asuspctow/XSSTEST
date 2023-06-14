package com.go2group.synapse.rest.pub;

import com.go2group.synapse.bean.TestRunOutputBean;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlElement;

public class TestPlanRestBean
{
  @XmlElement
  private String testPlanKey;
  @XmlElement
  private String testPlanSummary;
  @XmlElement
  private List<TestCycleRestBean> testCycles;
  @XmlElement
  private List<TestRunRestBean> testRuns;
  @XmlElement
  private Map<String, List<TestRunOutputBean>> cycleWithRuns;
  
  public TestPlanRestBean() {}
  
  public String getTestPlanKey()
  {
    return testPlanKey;
  }
  
  public void setTestPlanKey(String testPlanKey) {
    this.testPlanKey = testPlanKey;
  }
  
  public String getTestPlanSummary() {
    return testPlanSummary;
  }
  
  public void setTestPlanSummary(String testPlanSummary) {
    this.testPlanSummary = testPlanSummary;
  }
  
  public List<TestCycleRestBean> getTestCycles() {
    if (testCycles == null) {
      testCycles = new java.util.ArrayList();
    }
    return testCycles;
  }
  
  public void setTestCycles(List<TestCycleRestBean> testCycles) {
    this.testCycles = testCycles;
  }
  
  public List<TestRunRestBean> getTestRuns() {
    return testRuns;
  }
  
  public void setTestRuns(List<TestRunRestBean> testRuns) {
    this.testRuns = testRuns;
  }
  
  public Map<String, List<TestRunOutputBean>> getCycleWithRuns() {
    return cycleWithRuns;
  }
  
  public void setCycleWithRuns(Map<String, List<TestRunOutputBean>> cycleWithRuns) {
    this.cycleWithRuns = cycleWithRuns;
  }
}
