package com.go2group.synapse.automation.bean;

import java.util.Map;

public class AutomationResultBean {
  private String cycleName;
  
  public AutomationResultBean() {}
  
  private Map<String, AutomationRunResultBean> automationRunResultMap = new java.util.HashMap();
  
  public String getCycleName() {
    return cycleName;
  }
  
  public void setCycleName(String cycleName) {
    this.cycleName = cycleName;
  }
  
  public Map<String, AutomationRunResultBean> getAutomationRunResultMap() {
    return automationRunResultMap;
  }
  
  public void setAutomationRunResultMap(Map<String, AutomationRunResultBean> automationRunResultMap) {
    this.automationRunResultMap = automationRunResultMap;
  }
}
