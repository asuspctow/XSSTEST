package com.go2group.synapse.rest.pub;

import com.go2group.synapse.bean.AutomationAppOutputBean;
import com.go2group.synapse.bean.AutomationBuildDetailOutputBean;
import javax.xml.bind.annotation.XmlElement;




public class AutomationRestOutputBean
{
  @XmlElement
  private Integer automationBuildId;
  @XmlElement
  private String appName;
  @XmlElement
  private String jobKey;
  @XmlElement
  private String testType;
  @XmlElement
  private String params;
  
  public AutomationRestOutputBean(AutomationBuildDetailOutputBean automationBuildDetailOutputBean)
  {
    automationBuildId = automationBuildDetailOutputBean.getID();
    appName = automationBuildDetailOutputBean.getAutomationAppBean().getAppName();
    jobKey = automationBuildDetailOutputBean.getTriggerKey();
    testType = automationBuildDetailOutputBean.getTestType();
    params = automationBuildDetailOutputBean.getParams();
  }
  
  public String getTestType() {
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
  
  public String getJobKey() {
    return jobKey;
  }
  
  public void setJobKey(String jobKey) {
    this.jobKey = jobKey;
  }
  
  public String getParams() {
    return params;
  }
  
  public void setParams(String params) {
    this.params = params;
  }
  
  public Integer getAutomationBuildId() {
    return automationBuildId;
  }
  
  public void setAutomationJobId(Integer automationBuildId) {
    this.automationBuildId = automationBuildId;
  }
}
