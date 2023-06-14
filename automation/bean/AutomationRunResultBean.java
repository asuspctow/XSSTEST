package com.go2group.synapse.automation.bean;

public class AutomationRunResultBean
{
  private String result;
  private String errorType;
  private String errorMessage;
  private String testCaseLinkName;
  
  public AutomationRunResultBean() {}
  
  public String getResult()
  {
    return result;
  }
  
  public void setResult(String result) {
    this.result = result;
  }
  
  public String getErrorType() {
    return errorType;
  }
  
  public void setErrorType(String errorType) {
    this.errorType = errorType;
  }
  
  public String getTestCaseLinkName() {
    return testCaseLinkName;
  }
  
  public void setTestCaseLinkName(String testCaseLinkName) {
    this.testCaseLinkName = testCaseLinkName;
  }
  
  public String getErrorMessage() {
    return errorMessage;
  }
  
  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }
  
  public void initialise() {
    result = "";
    
    errorType = "";
    
    errorMessage = "";
    
    testCaseLinkName = "";
  }
  
  public String toString()
  {
    return "[testCaseLinkName=" + testCaseLinkName + "][result=" + result + "][errorType=" + errorType + "][errorMessage=" + errorMessage + "]";
  }
}
