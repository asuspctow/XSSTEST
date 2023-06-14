package com.go2group.synapse.job;

import com.atlassian.scheduler.config.JobRunnerKey;

public abstract interface PlanOverviewDataCreationJobRunner extends com.atlassian.scheduler.JobRunner
{
  public static final JobRunnerKey JOB_KEY = JobRunnerKey.of(PlanOverviewDataCreationJobRunner.class.getName());
  
  public static final String JOB_ID = PlanOverviewDataCreationJobRunner.class.getName() + ":ID";
  public static final long DEFAULT_INTERVAL = 60000L;
}
