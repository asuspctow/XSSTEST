package com.go2group.synapse.rest.pub;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class AdvancedTestCycleRestBean
{
  @XmlElement
  private String attributeOneId;
  @XmlElement
  private String attributeOneValue;
  @XmlElement
  private String attributeTwoId;
  @XmlElement
  private String attributeTwoValue;
  @XmlElement
  private Integer testCycleAttribId;
  
  public AdvancedTestCycleRestBean() {}
  
  public String getAttributeOneId()
  {
    return attributeOneId;
  }
  
  public void setAttributeOneId(String attributeOneId) {
    this.attributeOneId = attributeOneId;
  }
  
  public String getAttributeOneValue() {
    return attributeOneValue;
  }
  
  public void setAttributeOneValue(String attributeOneValue) {
    this.attributeOneValue = attributeOneValue;
  }
  
  public String getAttributeTwoId() {
    return attributeTwoId;
  }
  
  public void setAttributeTwoId(String attributeTwoId) {
    this.attributeTwoId = attributeTwoId;
  }
  
  public String getAttributeTwoValue() {
    return attributeTwoValue;
  }
  
  public void setAttributeTwoValue(String attributeTwoValue) {
    this.attributeTwoValue = attributeTwoValue;
  }
  
  public Integer getTestCycleAttribId() {
    return testCycleAttribId;
  }
  
  public void setTestCycleAttribId(Integer testCycleAttribId) {
    this.testCycleAttribId = testCycleAttribId;
  }
}
