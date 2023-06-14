package com.go2group.synapse.rest.pub;

import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;




@XmlRootElement
public class TestCycleRestBean
{
  @XmlElement
  private Integer testCycleId;
  @XmlElement
  private String testCycleName;
  @XmlElement
  private Long testPlanId;
  @XmlElement
  private List<String> addTestCaseKeys;
  @XmlElement
  private List<String> removeTestCaseKeys;
  @XmlElement
  private Map<String, String> errorMap = null;
  @XmlElement
  private List<Integer> runIds = null;
  @XmlElement
  private String status;
  @XmlElement
  private String testerName;
  @XmlElement
  private boolean notifyTester;
  
  public TestCycleRestBean() {}
  
  public Integer getTestCycleId() {
    return testCycleId;
  }
  
  public void setTestCycleId(Integer testCycleId) {
    this.testCycleId = testCycleId;
  }
  
  public Long getTestPlanId() {
    return testPlanId;
  }
  
  public void setTestPlanId(Long testPlanId) {
    this.testPlanId = testPlanId;
  }
  
  public List<String> getAddTestCaseKeys() {
    return addTestCaseKeys;
  }
  
  public void setAddTestCaseKeys(List<String> addTestCaseKeys) {
    this.addTestCaseKeys = addTestCaseKeys;
  }
  
  public List<String> getRemoveTestCaseKeys() {
    return removeTestCaseKeys;
  }
  
  public void setRemoveTestCaseKeys(List<String> removeTestCaseKeys) {
    this.removeTestCaseKeys = removeTestCaseKeys;
  }
  
  public Map<String, String> getErrorMap() {
    return errorMap;
  }
  
  public void setErrorMap(Map<String, String> errorMap) {
    this.errorMap = errorMap;
  }
  
  public String getTestCycleName() {
    return testCycleName;
  }
  
  public void setTestCycleName(String testCycleName) {
    this.testCycleName = testCycleName;
  }
  
  public List<Integer> getRunIds() {
    return runIds;
  }
  
  public void setRunIds(List<Integer> runIds) {
    this.runIds = runIds;
  }
  
  public String getStatus() {
    return status;
  }
  
  public void setStatus(String status) {
    this.status = status;
  }
  
  public String getTesterName() {
    return testerName;
  }
  
  public void setTesterName(String testerName) {
    this.testerName = testerName;
  }
  
  public boolean isNotifyTester() {
    return notifyTester;
  }
  
  public void setNotifyTester(boolean notifyTester) {
    this.notifyTester = notifyTester;
  }
}
