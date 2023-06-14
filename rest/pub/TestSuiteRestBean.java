package com.go2group.synapse.rest.pub;

import com.go2group.synapse.bean.IssueWrapperBean;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TestSuiteRestBean
{
  @XmlElement
  private String testSuitePath;
  @XmlElement
  private String projectKey;
  @XmlElement
  private List<String> testCaseKeys;
  @XmlElement
  private String testCaseKey;
  @XmlElement
  private Integer testSuiteId;
  @XmlElement
  private Integer createdTestSuiteId;
  @XmlElement
  private IssueWrapperBean createdTestCase;
  
  public TestSuiteRestBean() {}
  
  public String getTestSuitePath()
  {
    return testSuitePath;
  }
  
  public void setTestSuitePath(String testSuitePath) {
    this.testSuitePath = testSuitePath;
  }
  
  public String getProjectKey() {
    return projectKey;
  }
  
  public void setProjectKey(String projectKey) {
    this.projectKey = projectKey;
  }
  
  public List<String> getTestCaseKeys() {
    return testCaseKeys;
  }
  
  @XmlElement
  public void setTestCaseKeys(List<String> testCaseKeys) { this.testCaseKeys = testCaseKeys; }
  
  public Integer getTestSuiteId()
  {
    return testSuiteId;
  }
  
  public void setTestSuiteId(Integer testSuiteId) {
    this.testSuiteId = testSuiteId;
  }
  
  public Integer getCreatedTestSuiteId() {
    return createdTestSuiteId;
  }
  
  public void setCreatedTestSuiteId(Integer createdTestSuiteId) {
    this.createdTestSuiteId = createdTestSuiteId;
  }
  
  public IssueWrapperBean getCreatedTestCase() {
    return createdTestCase;
  }
  
  public void setCreatedTestCase(IssueWrapperBean createdTestCase) {
    this.createdTestCase = createdTestCase;
  }
  
  public String getTestCaseKey() {
    return testCaseKey;
  }
  
  public void setTestCaseKey(String testCaseKey) {
    this.testCaseKey = testCaseKey;
  }
}
