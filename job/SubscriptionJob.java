package com.go2group.synapse.job;

import com.atlassian.scheduler.JobRunnerRequest;
import com.atlassian.scheduler.JobRunnerResponse;
import com.atlassian.scheduler.config.JobId;
import com.atlassian.scheduler.config.JobRunnerKey;
import com.go2group.synapse.bean.JobRunInfoOutputBean;
import com.go2group.synapse.bean.SubscriptionOutputBean;
import com.go2group.synapse.core.exception.InvalidDataException;
import com.go2group.synapse.core.filter.service.SynapseFilterService;
import com.go2group.synapse.email.EmailBodyBuilderLookupFactory;
import com.go2group.synapse.manager.SynapseMailer;
import com.go2group.synapse.service.JobRunInfoService;
import com.go2group.synapse.service.SynapseSubscriptionService;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;



@Component
public class SubscriptionJob
  implements SynapseJobRunner
{
  private static final Logger log = Logger.getLogger(SubscriptionJob.class);
  
  static final ExecutorService threadPool = Executors.newFixedThreadPool(25);
  
  private SynapseSubscriptionService synapseSubscriptionService;
  
  private JobRunInfoService jobRunInfoService;
  private SynapseMailer synapseMailer;
  private EmailBodyBuilderLookupFactory emailBodyBuilderLookupFactory;
  private SynapseFilterService synapseFilterService;
  public static final JobRunnerKey JOB_KEY = JobRunnerKey.of(SubscriptionJob.class.getName());
  
  public SubscriptionJob(SynapseSubscriptionService synapseSubscriptionService, JobRunInfoService jobRunInfoService, SynapseMailer synapseMailer, EmailBodyBuilderLookupFactory emailBodyBuilderLookupFactory, SynapseFilterService synapseFilterService)
  {
    this.synapseSubscriptionService = synapseSubscriptionService;
    this.jobRunInfoService = jobRunInfoService;
    this.synapseMailer = synapseMailer;
    this.emailBodyBuilderLookupFactory = emailBodyBuilderLookupFactory;
    this.synapseFilterService = synapseFilterService;
  }
  
  public JobId getJobId()
  {
    return JobId.of(SubscriptionJob.class.getName() + " :ID");
  }
  
  public long getDetaultInterval()
  {
    return 60000L;
  }
  
  public JobRunnerResponse runJob(JobRunnerRequest request)
  {
    log.info("Subscription Job started");
    try {
      List<SubscriptionOutputBean> subscriptions = synapseSubscriptionService.getAllTodaySubscritions();
      Date now; if ((subscriptions != null) && (subscriptions.size() > 0)) {
        log.info("Number of subscriptions to be executed today is " + subscriptions.size());
        now = new Date();
        for (SubscriptionOutputBean subscription : subscriptions) {
          JobRunInfoOutputBean jobRunInfo = jobRunInfoService.getLastRunInfo(subscription.getID());
          
          if (jobRunInfo != null) {
            if (jobRunInfo.getExecutionTime().before(now)) {
              SendReportTaskConfig sendReportTaskConfig = new SendReportTaskConfig();
              sendReportTaskConfig.setSubscription(subscription);
              sendReportTaskConfig.setSynapseFilterService(synapseFilterService);
              
              log.info("Execution subscription :" + subscription.getName());
              SynapseAsyncTask<SendReportTaskConfig> task = new SendReportTask(sendReportTaskConfig, synapseMailer, emailBodyBuilderLookupFactory);
              threadPool.execute(task);
              
              jobRunInfoService.addJobRunInfo(subscription.getID(), new Timestamp(now.getTime()), "in-progress");
            }
          } else {
            SendReportTaskConfig sendReportTaskConfig = new SendReportTaskConfig();
            sendReportTaskConfig.setSubscription(subscription);
            sendReportTaskConfig.setSynapseFilterService(synapseFilterService);
            
            SynapseAsyncTask<SendReportTaskConfig> task = new SendReportTask(sendReportTaskConfig, synapseMailer, emailBodyBuilderLookupFactory);
            threadPool.execute(task);
            
            jobRunInfoService.addJobRunInfo(subscription.getID(), new Timestamp(now.getTime()), "in-progress");
          }
        }
      } else {
        log.info("Number of subscriptions to be executed today is 0");
      }
    } catch (InvalidDataException e) {
      log.debug(e.getMessage(), e);
      log.error(e.getMessage());
    }
    
    log.info("Subscription Job initiated successfully");
    return null;
  }
  


  public void forceStop()
  {
    threadPool.shutdown();
    try
    {
      if (!threadPool.awaitTermination(120L, TimeUnit.SECONDS)) {
        threadPool.shutdownNow();
        
        if (!threadPool.awaitTermination(60L, TimeUnit.SECONDS)) {
          log.error("Pool did not terminate");
        }
      }
    } catch (InterruptedException e) {
      log.debug(e.getMessage(), e);
      log.error(e.getMessage());
      
      threadPool.shutdownNow();
      
      Thread.currentThread().interrupt();
    }
  }
  
  public SynapseSubscriptionService getSynapseSubscriptionService() {
    return synapseSubscriptionService;
  }
  
  public void setSynapseSubscriptionService(SynapseSubscriptionService synapseSubscriptionService)
  {
    this.synapseSubscriptionService = synapseSubscriptionService;
  }
  
  public JobRunInfoService getJobRunInfoService() {
    return jobRunInfoService;
  }
  
  public void setJobRunInfoService(JobRunInfoService jobRunInfoService) {
    this.jobRunInfoService = jobRunInfoService;
  }
  
  public SynapseMailer getSynapseMailer() {
    return synapseMailer;
  }
  
  public void setSynapseMailer(SynapseMailer synapseMailer) {
    this.synapseMailer = synapseMailer;
  }
  
  public EmailBodyBuilderLookupFactory getEmailBodyBuilderLookupFactory() {
    return emailBodyBuilderLookupFactory;
  }
  
  public void setEmailBodyBuilderLookupFactory(EmailBodyBuilderLookupFactory emailBodyBuilderLookupFactory)
  {
    this.emailBodyBuilderLookupFactory = emailBodyBuilderLookupFactory;
  }
}
