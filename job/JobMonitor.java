package com.go2group.synapse.job;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.cluster.ClusterManager;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.event.events.PluginEnabledEvent;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.scheduler.SchedulerService;
import com.atlassian.scheduler.SchedulerServiceException;
import com.atlassian.scheduler.config.JobConfig;
import com.atlassian.scheduler.config.JobId;
import com.atlassian.scheduler.config.RunMode;
import com.atlassian.scheduler.config.Schedule;
import java.util.Date;
import java.util.Random;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;




@Component
public class JobMonitor
  implements InitializingBean, DisposableBean
{
  private static final Logger log = Logger.getLogger(JobMonitor.class);
  
  private final EventPublisher eventPublisher;
  
  private final JenkinsJob jenkinsJob;
  private final JenkinsLowerVersionJob jenkinsLowerVersionJob;
  private final BambooJob bambooJob;
  private final CreateCustomFieldJob createCustomFieldJob;
  private final CreateIssueTypeJob createIssueTypeJob;
  private final SubscriptionJob subscriptionJob;
  private final PlanOverviewDataCreationJob planOverviewDataCreationJob;
  private final SchedulerService schedulerService;
  private final ClusterManager clusterManager;
  private static final int MIN_DELAY = 60000;
  private static final int MAX_JITTER = 10000;
  private static final Random RANDOM = new Random();
  
  private static final String PLUGIN_KEY = "com.go2group.jira.plugin.synapse";
  

  public JobMonitor(@ComponentImport EventPublisher eventPublisher, @ComponentImport SchedulerService schedulerService, @ComponentImport ClusterManager clusterManager, JenkinsJob jenkinsJob, JenkinsLowerVersionJob jenkinsLowerVersionJob, BambooJob bambooJob, CreateCustomFieldJob createCustomFieldJob, SubscriptionJob subscriptionJob, CreateIssueTypeJob createIssueTypeJob, PlanOverviewDataCreationJob planOverviewDataCreationJob)
  {
    this.eventPublisher = eventPublisher;
    this.jenkinsJob = jenkinsJob;
    this.jenkinsLowerVersionJob = jenkinsLowerVersionJob;
    this.bambooJob = bambooJob;
    this.schedulerService = schedulerService;
    this.clusterManager = clusterManager;
    this.createCustomFieldJob = createCustomFieldJob;
    this.subscriptionJob = subscriptionJob;
    this.createIssueTypeJob = createIssueTypeJob;
    this.planOverviewDataCreationJob = planOverviewDataCreationJob;
  }
  
  public void destroy() throws Exception
  {
    log.debug("destroy{");
    unregisterListener();
    unregisterJobRunner();
  }
  
  @EventListener
  public void onPluginEnabled(PluginEnabledEvent event)
  {
    if ("com.go2group.jira.plugin.synapse".equals(event.getPlugin().getKey()))
    {
      log.debug("onPluginEnabled{");
      launch();
    }
  }
  
  public void afterPropertiesSet() throws Exception
  {
    log.debug("afterPropertiesSet{");
    registerListener();
  }
  
  private void registerListener()
  {
    log.debug("registerListeners");
    eventPublisher.register(this);
  }
  
  private void unregisterListener()
  {
    log.debug("unregisterListeners");
    eventPublisher.unregister(this);
  }
  
  private void launch()
  {
    registerJobRunner();
    schedule();
  }
  
  private void schedule()
  {
    int jitter = RANDOM.nextInt(10000);
    
    Date firstRun = new Date(System.currentTimeMillis() + 60000L + jitter);
    


    JobConfig jenkinsJobConfig = JobConfig.forJobRunnerKey(JenkinsJob.JOB_KEY).withSchedule(Schedule.forInterval(60000L, firstRun)).withRunMode(getRunMode());
    

    JobConfig jenkinsLowerVersionJobConfig = JobConfig.forJobRunnerKey(JenkinsLowerVersionJob.JOB_KEY).withSchedule(Schedule.forInterval(60000L, firstRun)).withRunMode(getRunMode());
    

    JobConfig bambooJobConfig = JobConfig.forJobRunnerKey(BambooJob.JOB_KEY).withSchedule(Schedule.forInterval(60000L, firstRun)).withRunMode(getRunMode());
    

    JobConfig createCustomFieldJobConfig = JobConfig.forJobRunnerKey(CreateCustomFieldJob.JOB_KEY).withSchedule(Schedule.runOnce(firstRun)).withRunMode(getRunMode());
    

    JobConfig createIssueTypeJobConfig = JobConfig.forJobRunnerKey(CreateIssueTypeJob.JOB_KEY).withSchedule(Schedule.runOnce(firstRun)).withRunMode(getRunMode());
    

    JobConfig subscriptionJobConfig = JobConfig.forJobRunnerKey(SubscriptionJob.JOB_KEY).withSchedule(Schedule.forInterval(subscriptionJob.getDetaultInterval(), firstRun)).withRunMode(getRunMode());
    
    JobConfig planOverviewDatacreationJonConfig = JobConfig.forJobRunnerKey(PlanOverviewDataCreationJob.JOB_KEY).withSchedule(Schedule.forCronExpression("0 5 12 ? * *")).withRunMode(getRunMode());
    try {
      schedulerService.scheduleJob(JobId.of(JenkinsJob.JOB_ID), jenkinsJobConfig);
      log.info("Scheduled Jenkins Job!");
      schedulerService.scheduleJob(JobId.of(JenkinsLowerVersionJob.JOB_ID), jenkinsLowerVersionJobConfig);
      log.info("Scheduled Jenkins Job!");
      schedulerService.scheduleJob(JobId.of(BambooJob.JOB_ID), bambooJobConfig);
      log.info("Scheduled Bamboo Job!");
      schedulerService.scheduleJob(JobId.of(CreateCustomFieldJob.JOB_ID), createCustomFieldJobConfig);
      log.info("Scheduled CreateCustomFieldJob Job!");
      schedulerService.scheduleJob(JobId.of(CreateIssueTypeJob.JOB_ID), createIssueTypeJobConfig);
      log.info("Scheduled PlanOverviewDataCreationJob Job!");
      schedulerService.scheduleJob(JobId.of(PlanOverviewDataCreationJob.JOB_ID), planOverviewDatacreationJonConfig);
      schedulerService.scheduleJob(subscriptionJob.getJobId(), subscriptionJobConfig);
      log.info("Scheduled Synapse Subscription Job!");
    }
    catch (SchedulerServiceException e) {
      log.error(e.getMessage());
      log.debug(e.getMessage(), e);
    }
    
    log.debug("Successfully scheduled Jobs!");
  }
  
  private RunMode getRunMode() {
    if (clusterManager.isClustered()) {
      return RunMode.RUN_ONCE_PER_CLUSTER;
    }
    return RunMode.RUN_LOCALLY;
  }
  

  private void registerJobRunner()
  {
    log.debug("registerJobRunner");
    schedulerService.registerJobRunner(JenkinsJob.JOB_KEY, jenkinsJob);
    schedulerService.registerJobRunner(JenkinsLowerVersionJob.JOB_KEY, jenkinsLowerVersionJob);
    schedulerService.registerJobRunner(BambooJob.JOB_KEY, bambooJob);
    schedulerService.registerJobRunner(CreateCustomFieldJob.JOB_KEY, createCustomFieldJob);
    schedulerService.registerJobRunner(SubscriptionJob.JOB_KEY, subscriptionJob);
    schedulerService.registerJobRunner(PlanOverviewDataCreationJob.JOB_KEY, planOverviewDataCreationJob);
  }
  
  private void unregisterJobRunner()
  {
    log.debug("unregisterJobRunner");
    schedulerService.unregisterJobRunner(JenkinsJob.JOB_KEY);
    schedulerService.unregisterJobRunner(JenkinsLowerVersionJob.JOB_KEY);
    schedulerService.unregisterJobRunner(BambooJob.JOB_KEY);
    schedulerService.unregisterJobRunner(CreateCustomFieldJob.JOB_KEY);
    schedulerService.unregisterJobRunner(SubscriptionJob.JOB_KEY);
    schedulerService.unregisterJobRunner(PlanOverviewDataCreationJob.JOB_KEY);
  }
}
