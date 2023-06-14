package com.go2group.synapse.job;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.scheduler.JobRunnerRequest;
import com.atlassian.scheduler.JobRunnerResponse;
import com.go2group.synapse.bean.RunStatusOutputBean;
import com.go2group.synapse.bean.TestCycleOutputBean;
import com.go2group.synapse.bean.TestPlanOverviewInputBean;
import com.go2group.synapse.bean.TestRunHistoryOutputBean;
import com.go2group.synapse.bean.TestRunOutputBean;
import com.go2group.synapse.core.exception.InvalidDataException;
import com.go2group.synapse.service.RunStatusService;
import com.go2group.synapse.service.TestRunService;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class PlanOverviewDataCreationJob implements PlanOverviewDataCreationJobRunner
{
  private static final Logger log = Logger.getLogger(PlanOverviewDataCreationJob.class);
  
  private TestRunService testRunService;
  private RunStatusService runStatusService;
  private IssueManager issueManager;
  
  public PlanOverviewDataCreationJob(@ComponentImport IssueManager issueManager, TestRunService testRunService, RunStatusService runStatusService)
  {
    this.testRunService = testRunService;
    this.issueManager = issueManager;
    this.runStatusService = runStatusService;
  }
  
  public JobRunnerResponse runJob(JobRunnerRequest request)
  {
    try {
      log.debug("Job execution starts - PlanOverviewDataCreationJob");
      
      Calendar cal = Calendar.getInstance();
      cal.add(5, -1);
      cal.set(11, 0);
      cal.set(12, 0);
      cal.set(13, 0);
      cal.set(14, 0);
      Date yestDate = cal.getTime();
      Timestamp yestTS = new Timestamp(cal.getTimeInMillis());
      log.debug("PlanOverviewDataCreationJob for date: " + yestDate);
      
      Timestamp lastUpdatedTPODate = testRunService.getLastUpdatedTestPlanOverviewDate();
      Integer days = Integer.valueOf(1);
      if (lastUpdatedTPODate == null) {
        days = Integer.valueOf(30);
      } else {
        long difference = yestTS.getTime() - lastUpdatedTPODate.getTime();
        float daysBetween = (float)TimeUnit.DAYS.convert(difference, TimeUnit.MILLISECONDS);
        days = Integer.valueOf((int)daysBetween + 1);
      }
      
      if (days.intValue() > 30) {
        days = Integer.valueOf(30);
      }
      
      for (int i = days.intValue(); i >= 2; i--) {
        Calendar nDaysBefore = Calendar.getInstance();
        nDaysBefore.add(5, -1 * (i - 1));
        nDaysBefore.set(11, 0);
        nDaysBefore.set(12, 0);
        nDaysBefore.set(13, 0);
        nDaysBefore.set(14, 0);
        Date nDaysBeforeDate = nDaysBefore.getTime();
        Timestamp nDaysBeforTS = new Timestamp(nDaysBefore.getTimeInMillis());
        
        List<TestRunHistoryOutputBean> executionHistoryBeans = getExecutionHistoryBeans(nDaysBeforeDate);
        List<TestRunHistoryOutputBean> defectHistoryBeans = getDefectHistoryBeans(nDaysBeforeDate);
        
        Map<String, TestPlanOverviewInputBean> testPlanOvrvwBeans = new HashMap();
        for (TestRunHistoryOutputBean trhao : executionHistoryBeans) {
          Issue testPlan = issueManager.getIssueObject(trhao.getTestRun().getCycle().getTpId());
          RunStatusOutputBean rs = runStatusService.getRunStatus(trhao.getActivity());
          if ((testPlan != null) && (rs != null)) {
            String key = testPlan.getProjectId() + "-" + trhao.getExecutorId() + "-" + rs.getId();
            if (testPlanOvrvwBeans.isEmpty()) {
              TestPlanOverviewInputBean tpob = new TestPlanOverviewInputBean(nDaysBeforTS, testPlan.getProjectId(), trhao.getExecutorId(), Long.valueOf(rs.getId().intValue()), Integer.valueOf(1));
              testPlanOvrvwBeans.put(key, tpob);
            }
            else if (testPlanOvrvwBeans.containsKey(key)) {
              TestPlanOverviewInputBean tpob = (TestPlanOverviewInputBean)testPlanOvrvwBeans.get(key);
              tpob.incrementCount();
              testPlanOvrvwBeans.put(key, tpob);
            } else {
              TestPlanOverviewInputBean tpob = new TestPlanOverviewInputBean(nDaysBeforTS, testPlan.getProjectId(), trhao.getExecutorId(), Long.valueOf(rs.getId().intValue()), Integer.valueOf(1));
              testPlanOvrvwBeans.put(key, tpob);
            }
          }
        }
        
        for (TestRunHistoryOutputBean trhao : defectHistoryBeans) {
          Issue testPlan = issueManager.getIssueObject(trhao.getTestRun().getCycle().getTpId());
          String defectActivity = trhao.getActivity();
          if (testPlan != null) {
            String key = testPlan.getProjectId() + "-" + trhao.getExecutorId() + "-0";
            TestPlanOverviewInputBean tpob = null;
            if (testPlanOvrvwBeans.containsKey(key)) {
              tpob = (TestPlanOverviewInputBean)testPlanOvrvwBeans.get(key);
            } else {
              tpob = new TestPlanOverviewInputBean(nDaysBeforTS, testPlan.getProjectId(), trhao.getExecutorId(), Long.valueOf(0L), Integer.valueOf(0));
            }
            if (defectActivity.equals("Attached")) {
              tpob.incrementCount();
            } else if (defectActivity.startsWith("+")) {
              tpob.incrementCount();
            } else if (defectActivity.startsWith("-")) {
              tpob.decrementCount();
            }
            testPlanOvrvwBeans.put(key, tpob);
          }
        }
        for (TestPlanOverviewInputBean testPlanOverviewBean : testPlanOvrvwBeans.values()) {
          log.debug("Updating TestPlanOverview");
          testRunService.updateTestPlanOverview(testPlanOverviewBean);
        }
      }
    } catch (InvalidDataException e) {
      log.debug(e.getMessage(), e);
      log.error(e.getMessage());
    }
    return JobRunnerResponse.success();
  }
  
  private List<TestRunHistoryOutputBean> getExecutionHistoryBeans(Date date) {
    try {
      return testRunService.getExecutionHistoryForDate(date);
    }
    catch (InvalidDataException e) {
      log.debug(e.getMessage(), e);
      log.error(e.getMessage());
    }
    return null;
  }
  
  private List<TestRunHistoryOutputBean> getDefectHistoryBeans(Date date) {
    try {
      return testRunService.getDefectHistoryForDate(date);
    }
    catch (InvalidDataException e) {
      log.debug(e.getMessage(), e);
      log.error(e.getMessage());
    }
    return null;
  }
}
