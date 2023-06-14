package com.go2group.synapse.rest.pub;

import com.go2group.synapse.bean.TestStepInputBean;
import java.util.Collection;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;







public class TestCaseIssueBean
{
  @XmlElement
  private String description;
  @XmlElement
  private String summary;
  @XmlElement
  private List<TestStepInputBean> steps;
  @XmlElement
  private String projectKey;
  @XmlElement
  private List<String> testSuites;
  @XmlElement
  private Collection<TestPlanRestBean> testPlans;
  @XmlElement
  private String automationReference;
  @XmlElement
  private String estimate;
  @XmlElement
  private String forecast;
  @XmlElement
  private String issueKey;
  @XmlElement
  private String issueId;
  
  public TestCaseIssueBean() {}
  
  public String getIssueId()
  {
    return issueId;
  }
  


  public void setIssueId(String issueId)
  {
    this.issueId = issueId;
  }
  


  public String getIssueKey()
  {
    return issueKey;
  }
  


  public void setIssueKey(String issueKey)
  {
    this.issueKey = issueKey;
  }
  


  public String getEstimate()
  {
    return estimate;
  }
  


  public void setEstimate(String estimate)
  {
    this.estimate = estimate;
  }
  

  public String getForecast()
  {
    return forecast;
  }
  


  public void setForecast(String forecast)
  {
    this.forecast = forecast;
  }
  
  public String getDescription() {
    return description;
  }
  
  public void setDescription(String description) {
    this.description = description;
  }
  
  public String getSummary() {
    return summary;
  }
  
  public void setSummary(String summary) {
    this.summary = summary;
  }
  
  public List<TestStepInputBean> getSteps() {
    return steps;
  }
  
  public void setSteps(List<TestStepInputBean> steps) {
    this.steps = steps;
  }
  
  public String getProjectKey() {
    return projectKey;
  }
  
  public void setProjectKey(String projectKey) {
    this.projectKey = projectKey;
  }
  
  public List<String> getTestSuites() {
    return testSuites;
  }
  
  public void setTestSuites(List<String> testSuites) {
    this.testSuites = testSuites;
  }
  
  public Collection<TestPlanRestBean> getTestPlans() {
    return testPlans;
  }
  
  public void setTestPlans(Collection<TestPlanRestBean> testPlans) {
    this.testPlans = testPlans;
  }
  
  public String getAutomationReference() {
    return automationReference;
  }
  
  public void setAutomationReference(String automationReference) {
    this.automationReference = automationReference;
  }
}
