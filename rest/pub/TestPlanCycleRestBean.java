package com.go2group.synapse.rest.pub;

import com.go2group.synapse.bean.TestCycleOutputBean;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TestPlanCycleRestBean
{
  private TestCycleOutputBean testCycleOutputBean;
  
  public TestPlanCycleRestBean(TestCycleOutputBean testCycleOutputBean)
  {
    this.testCycleOutputBean = testCycleOutputBean;
  }
  
  @XmlElement
  public Integer getID() {
    return testCycleOutputBean.getID();
  }
  
  @XmlElement
  public String getName() {
    return testCycleOutputBean.getNameHtml();
  }
  
  @XmlElement
  public String getCycleName() {
    return testCycleOutputBean.getNameHtml().replaceAll("'", "\\\\'");
  }
  
  @XmlElement
  public String getNameHtml()
  {
    return testCycleOutputBean.getNameHtml();
  }
  
  @XmlElement
  public String getEnvironment() {
    return testCycleOutputBean.getEnvironment();
  }
  
  @XmlElement
  public String getBuild() {
    return testCycleOutputBean.getBuild();
  }
  
  @XmlElement
  public Long getSprint() {
    return testCycleOutputBean.getSprint();
  }
  
  @XmlElement
  public String getStatus() {
    return testCycleOutputBean.getStatus();
  }
  
  @XmlElement
  public String getCycleStartedDate() {
    return testCycleOutputBean.getCycleStartedDate();
  }
  
  @XmlElement
  public String getCycleCompletedDate() {
    return testCycleOutputBean.getCycleCompletedDate();
  }
  
  @XmlElement
  public String getPlannedStartDate() {
    return testCycleOutputBean.getPlannedStartDate();
  }
  
  @XmlElement
  public String getPlannedEndDate() {
    return testCycleOutputBean.getPlannedEndDate();
  }
  
  public boolean isDeleteable() {
    return testCycleOutputBean.isDeleteable();
  }
  
  public boolean isDraft() {
    return testCycleOutputBean.isDraft();
  }
  
  public boolean isAborted() {
    return testCycleOutputBean.isAborted();
  }
  
  public boolean isActive() {
    return testCycleOutputBean.isActive();
  }
  
  public boolean isCompleted() {
    return testCycleOutputBean.isCompleted();
  }
  
  public boolean isAdhocTestCycle() {
    return testCycleOutputBean.isAdhocTestCycle();
  }
}
