package com.go2group.synapse.rest.pub;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class RequirementRestBean
{
  private String projectKey;
  private Long projectId;
  private String suiteName;
  private Integer parentSuiteId;
  private Integer id;
  
  public RequirementRestBean() {}
  
  @XmlElement
  public Integer getId()
  {
    return id;
  }
  
  public void setId(Integer id) {
    this.id = id;
  }
  
  @XmlElement
  public String getProjectKey() {
    return projectKey;
  }
  
  @XmlElement
  public Long getProjectId() {
    return projectId;
  }
  
  public void setProjectId(Long projectId) {
    this.projectId = projectId;
  }
  
  public void setProjectKey(String projectKey) {
    this.projectKey = projectKey;
  }
  
  @XmlElement
  public String getSuiteName() {
    return suiteName;
  }
  
  public void setSuiteName(String suiteName) {
    this.suiteName = suiteName;
  }
  
  @XmlElement
  public Integer getParentSuiteId() {
    return parentSuiteId;
  }
  
  public void setParentSuiteId(Integer parentSuiteId) {
    this.parentSuiteId = parentSuiteId;
  }
}
