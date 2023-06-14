package com.go2group.synapse.rest.pub;

import com.go2group.synapse.bean.IssueWrapperBean;
import com.go2group.synapse.bean.TestRunAttachmentOutputBean;
import com.go2group.synapse.bean.TestRunIterationOutputBean;
import com.go2group.synapse.bean.TestRunStepOutputBean;
import java.util.Collection;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;



@XmlRootElement
class TestRunStepRestBean
{
  private TestRunStepOutputBean outputBean;
  
  public TestRunStepRestBean(TestRunStepOutputBean outputBean)
  {
    this.outputBean = outputBean;
  }
  
  @XmlElement
  public Integer getID() {
    return outputBean.getID();
  }
  
  @XmlElement
  public String getStep()
  {
    return outputBean.getStepRaw();
  }
  
  @XmlElement
  public String getExpectedResult() {
    return outputBean.getExpectedResultRaw();
  }
  
  @XmlElement
  public String getActualResult() {
    return outputBean.getActualResultRaw();
  }
  
  @XmlElement
  public String getStepData() {
    return outputBean.getStepDataRaw();
  }
  
  @XmlElement
  public String getStatus() {
    return outputBean.getStatus();
  }
  
  @XmlElement
  public Collection<IssueWrapperBean> getTestRunStepBugsWrapper() {
    return outputBean.getTestRunStepBugsWrapper();
  }
  
  @XmlElement
  public Collection<TestRunAttachmentOutputBean> getTestRunStepAttachments() {
    return outputBean.getTestRunStepAttachments();
  }
  
  public TestRunIterationOutputBean getTestRunIteration() {
    return outputBean.getTestRunIteration();
  }
  
  @XmlElement
  public Integer getTestRunIterationId() {
    if (outputBean.getTestRunIteration() != null) {
      TestRunIterationOutputBean iterationOutputBean = outputBean.getTestRunIteration();
      if (!TestRunIterationOutputBean.ITERATION_TYPE_DEFAULT.equals(iterationOutputBean.getType())) {
        return outputBean.getTestRunIteration().getId();
      }
    }
    return null;
  }
}
