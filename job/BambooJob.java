package com.go2group.synapse.job;

import com.atlassian.scheduler.JobRunnerRequest;
import com.atlassian.scheduler.JobRunnerResponse;
import com.go2group.synapse.bean.AutomationAppInputBean;
import com.go2group.synapse.bean.AutomationAppOutputBean;
import com.go2group.synapse.bean.AutomationBuildDetailOutputBean;
import com.go2group.synapse.bean.AutomationRunStepInputBean;
import com.go2group.synapse.bean.AutomationRunStepOutputBean;
import com.go2group.synapse.bean.RunStatusOutputBean;
import com.go2group.synapse.core.exception.InvalidDataException;
import com.go2group.synapse.enums.AutomationRunStepEnum;
import com.go2group.synapse.exception.ApplicationAccessException;
import com.go2group.synapse.exception.BuildIncompleteException;
import com.go2group.synapse.service.BambooService;
import com.go2group.synapse.service.TestCycleService;
import com.go2group.synapse.service.TestRunService;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class BambooJob
  implements BambooJobRunner
{
  private static final Logger log = Logger.getLogger(BambooJob.class);
  
  private final TestRunService testRunService;
  
  private final BambooService bambooService;
  private final TestCycleService cycleService;
  
  public BambooJob(TestRunService testRunService, BambooService bambooService, TestCycleService cycleService)
  {
    this.testRunService = testRunService;
    this.bambooService = bambooService;
    this.cycleService = cycleService;
  }
  
  public JobRunnerResponse runJob(JobRunnerRequest jrRequest)
  {
    log.debug("runJob ==================" + jrRequest);
    

    List<AutomationRunStepOutputBean> aRunSteps = testRunService.getExecutingAutomationRunSteps("Bamboo");
    
    log.debug("Automation Steps :" + aRunSteps);
    Iterator localIterator;
    if (aRunSteps != null)
    {
      for (localIterator = aRunSteps.iterator(); localIterator.hasNext();) { aRunStep = (AutomationRunStepOutputBean)localIterator.next();
        AutomationRunStepInputBean inputBean = new AutomationRunStepInputBean();
        inputBean.setID(aRunStep.getID());
        inputBean.setCallbackUrl(aRunStep.getCallbackUrl());
        inputBean.setExecutionState(AutomationRunStepEnum.getAutomationRunStepEnum(aRunStep.getExecutionState()));
        inputBean.setTriggerKey(aRunStep.getTriggerKey());
        inputBean.setStatus(aRunStep.getStatus());
        inputBean.setStatusId(aRunStep.getRunStatus().getId());
        inputBean.setLastExecutedBy(aRunStep.getLastExecutedBy());
        inputBean.setLastExecutorId(aRunStep.getLastExecutorId());
        
        AutomationAppOutputBean appOutputBean = aRunStep.getAutomationAppBean();
        AutomationAppInputBean appInputBean = new AutomationAppInputBean();
        appInputBean.setID(appOutputBean.getID());
        appInputBean.setAppType(appOutputBean.getAppType());
        appInputBean.setBaseUrl(appOutputBean.getBaseUrl());
        appInputBean.setUserName(appOutputBean.getUserName());
        appInputBean.setPassword(appOutputBean.getPassword());
        
        inputBean.setAutomationAppBean(appInputBean);
        
        try
        {
          bambooService.processCallback(inputBean);
        } catch (ApplicationAccessException e) {
          log.error(e.getMessage());
          log.debug(e.getMessage(), e);
        }
        catch (BuildIncompleteException localBuildIncompleteException1) {}
      }
    }
    

    try
    {
      Object automationBuildDetailOutputBeans = cycleService.getRunningCycleAutomationDetail("Bamboo");
      if ((automationBuildDetailOutputBeans != null) && (((List)automationBuildDetailOutputBeans).size() > 0)) {
        log.debug("Going to trigger call back process of automation from Jenkins job ......");
        for (AutomationBuildDetailOutputBean buildDetailOutputBean : (List)automationBuildDetailOutputBeans) {
          log.debug("Build key ......" + buildDetailOutputBean.getTriggerKey());
          try
          {
            bambooService.processCycleCallback(buildDetailOutputBean);
          } catch (ApplicationAccessException e) {
            log.debug(e.getMessage(), e);
          } catch (BuildIncompleteException e) {
            log.debug(e.getMessage(), e);
          }
        }
      }
    } catch (InvalidDataException e) {
      AutomationRunStepOutputBean aRunStep;
      log.debug(e.getMessage(), e);
    }
    
    return JobRunnerResponse.success();
  }
}
