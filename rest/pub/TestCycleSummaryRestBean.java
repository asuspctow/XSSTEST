package com.go2group.synapse.rest.pub;

import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.go2group.synapse.bean.IssueWrapperBean;
import com.go2group.synapse.bean.TestRunAttachmentOutputBean;
import com.go2group.synapse.bean.TestRunOutputBean;
import com.go2group.synapse.util.PluginUtil;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import javax.xml.bind.annotation.XmlElement;
import org.apache.commons.lang3.StringUtils;





public class TestCycleSummaryRestBean
{
  private TestRunOutputBean outputBean;
  private DateTimeFormatter dateTimeFormatter;
  private final UserManager userManager;
  
  public TestCycleSummaryRestBean(TestRunOutputBean outputBean, DateTimeFormatter dateTimeFormatter, @ComponentImport UserManager userManager)
  {
    this.outputBean = outputBean;
    this.dateTimeFormatter = dateTimeFormatter;
    this.userManager = userManager;
  }
  
  @XmlElement
  public Integer getID() { return outputBean.getID(); }
  
  @XmlElement
  public String getSummary() {
    return outputBean.getSummary();
  }
  
  @XmlElement
  public String getStatus() { return outputBean.getStatus(); }
  

  @XmlElement
  public String getTestCaseKey() { return outputBean.getTestCaseKey(); }
  
  @XmlElement
  public String getExecutedBy() {
    String executor = outputBean.getExecutedBy();
    if (StringUtils.isBlank(executor)) {
      return "";
    }
    return executor;
  }
  
  @XmlElement
  public Collection<TestRunAttachmentOutputBean> getAttachments() {
    return outputBean.getAttachments();
  }
  
  @XmlElement
  public Collection<IssueWrapperBean> getBugs() { return outputBean.getBugs(); }
  
  @XmlElement
  public String getExecutionTimeStamp() {
    return PluginUtil.formatTime(dateTimeFormatter, outputBean.getExecutionTime());
  }
  
  @XmlElement
  public String getTesterName() {
    Long testerId = outputBean.getTesterId();
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
  public String getComment() {
    return outputBean.getComment();
  }
  
  private String formatTime(Timestamp timeStamp)
  {
    if (timeStamp != null) {
      return dateTimeFormatter.format(toDate(timeStamp));
    }
    return "";
  }
  

  public static Date toDate(Timestamp timestamp)
  {
    long milliseconds = timestamp.getTime() + timestamp.getNanos() / 1000000;
    return new Date(milliseconds);
  }
}
