package com.go2group.synapse.rest.pub;

import javax.xml.bind.annotation.XmlElement;



public class AutomationRestBean
{
  @XmlElement
  private String testPlanKey;
  @XmlElement
  private String testCycleName;
  @XmlElement
  private String appName;
  @XmlElement
  private String jobKey;
  @XmlElement
  private String testType;
  @XmlElement
  private String params;
  
  public AutomationRestBean() {}
  
  public String getTestType()
  {
    return testType;
  }
  
  public void setTestType(String testType) {
    this.testType = testType;
  }
  
  public String getAppName() {
    return appName;
  }
  
  public void setAppName(String appName) {
    this.appName = appName;
  }
  
  public String getTestPlanKey() {
    return testPlanKey;
  }
  
  public void setTestPlanKey(String testPlanKey) {
    this.testPlanKey = testPlanKey;
  }
  
  public String getJobKey() {
    return jobKey;
  }
  
  public void setJobKey(String jobKey) {
    this.jobKey = jobKey;
  }
  
  public String getTestCycleName() {
    return testCycleName;
  }
  
  public void setTestCycleName(String testCycleName) {
    this.testCycleName = testCycleName;
  }
  
  public String getParams() {
    return params;
  }
  
  public void setParams(String params) {
    this.params = params;
  }
}
