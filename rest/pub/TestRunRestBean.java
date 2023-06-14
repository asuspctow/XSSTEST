package com.go2group.synapse.rest.pub;

import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.go2group.synapse.bean.TestCycleOutputBean;
import com.go2group.synapse.bean.TestRunDisplayBean;
import com.go2group.synapse.bean.TestRunStepOutputBean;
import com.go2group.synapse.util.PluginUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.apache.commons.lang3.StringUtils;



@XmlRootElement
class TestRunRestBean
{
  private final TestRunDisplayBean testRunBean;
  private final DateTimeFormatter dateTimeFormatter;
  private final UserManager userManager;
  private final IssueManager issueManager;
  private final I18nHelper i18nHelper;
  private Issue testPlan;
  @XmlElement
  private Collection<TestRunStepRestBean> testRunSteps;
  
  public TestRunRestBean(TestRunDisplayBean testRunBean, DateTimeFormatter dateTimeFormatter, @ComponentImport UserManager userManager, IssueManager issueManager, I18nHelper i18nHelper)
  {
    this.testRunBean = testRunBean;
    this.dateTimeFormatter = dateTimeFormatter;
    this.userManager = userManager;
    this.issueManager = issueManager;
    this.i18nHelper = i18nHelper;
  }
  
  @XmlElement
  public Integer getID() {
    return testRunBean.getID();
  }
  
  @XmlElement
  public String getSummary() {
    return testRunBean.getSummary();
  }
  
  @XmlElement
  public String getStatus() {
    return testRunBean.getStatus();
  }
  
  @XmlElement
  public String getComment() {
    return testRunBean.getCommentWiki();
  }
  
  public String getLozenge() {
    return testRunBean.getLozenge();
  }
  
  @XmlElement
  public String getTestCaseKey()
  {
    return testRunBean.getTestCaseKey();
  }
  
  @XmlElement
  public Long getTestCaseId() {
    return testRunBean.getTestCaseId();
  }
  
  @XmlElement
  public Integer getType() {
    return testRunBean.getType();
  }
  
  @XmlElement
  public String getTesterName() {
    Long testerId = testRunBean.getTesterId();
    if (testerId != null) {
      if (testerId.longValue() == -1L) {
        return "";
      }
      Optional<ApplicationUser> userOptional = userManager.getUserById(testerId);
      if (userOptional.isPresent()) {
        return ((ApplicationUser)userOptional.get()).getUsername();
      }
      return String.valueOf(testerId);
    }
    return "";
  }
  
  @XmlElement
  public String getExecutedBy() {
    String executor = testRunBean.getExecutedBy();
    if (StringUtils.isBlank(executor)) {
      return "";
    }
    return executor;
  }
  
  @XmlElement
  public String getExecutionOn() {
    return PluginUtil.formatTime(dateTimeFormatter, testRunBean.getExecutionOn());
  }
  
  @XmlElement
  public TestRunDetailRestBean getTestRunDetails() {
    TestRunDetailRestBean detailRestBean = new TestRunDetailRestBean();
    detailRestBean.setTmpTestRunSteps(testRunBean.getTestRunSteps());
    
    Collection<TestRunStepOutputBean> runSteps = testRunBean.getTestRunSteps();
    if ((runSteps != null) && (runSteps.size() > 0)) {
      Collection<TestRunStepRestBean> restBeans = new ArrayList();
      for (TestRunStepOutputBean stepOutputBean : runSteps) {
        TestRunStepRestBean restBean = new TestRunStepRestBean(stepOutputBean);
        restBeans.add(restBean);
      }
      detailRestBean.setTestRunSteps(restBeans);
    }
    detailRestBean.setAutomationRunSteps(testRunBean.getAutomationRunSteps());
    detailRestBean.setTestRunAttachments(testRunBean.getTestRunAttachments());
    detailRestBean.setTestRunBugs(testRunBean.getTestRunBugs());
    detailRestBean.setTestRunHistory(testRunBean.getTestRunHistory());
    detailRestBean.setPrevRunId(testRunBean.getPrevRunId());
    detailRestBean.setNextRunId(testRunBean.getNextRunId());
    return detailRestBean;
  }
  
  @XmlElement
  public String getEstimate() { return testRunBean.getEstimateAsString(); }
  

  @XmlElement
  public String getEfforts() { return testRunBean.getEffortAsString(); }
  
  @XmlElement
  public String getTestPlanKey() {
    if (testPlan == null) {
      if ((testRunBean.getCycle() != null) && 
        (!testRunBean.getCycle().isAdhocTestCycle())) {
        testPlan = issueManager.getIssueObject(testRunBean.getCycle().getTpId());
        if (testPlan != null) {
          return testPlan.getKey();
        }
      }
    }
    else {
      return testPlan.getKey();
    }
    return "";
  }
  
  @XmlElement
  public String getTestPlanSummary() { if (testPlan == null) {
      if ((testRunBean.getCycle() != null) && 
        (!testRunBean.getCycle().isAdhocTestCycle())) {
        testPlan = issueManager.getIssueObject(testRunBean.getCycle().getTpId());
        if (testPlan != null) {
          return testPlan.getSummary();
        }
      }
    }
    else {
      return testPlan.getSummary();
    }
    return "";
  }
  
  @XmlElement
  public Integer getTestCycleId() { if (testRunBean.getCycle() != null) {
      return testRunBean.getCycle().getID();
    }
    return null;
  }
  
  @XmlElement
  public String getTestCycleSummary() { if (testRunBean.getCycle() != null) {
      if (testRunBean.getCycle().isAdhocTestCycle()) {
        return i18nHelper.getText("synapse.common.label.adhoc");
      }
      return testRunBean.getCycle().getName();
    }
    
    return "";
  }
  
  @XmlElement
  public Map<String, List<String>> getRunAttributes() {
    return testRunBean.getRunAttributes();
  }
}
