package com.go2group.synapse.job;

import com.atlassian.scheduler.config.JobRunnerKey;

public abstract interface BambooJobRunner extends com.atlassian.scheduler.JobRunner
{
  public static final JobRunnerKey JOB_KEY = JobRunnerKey.of(BambooJobRunner.class.getName());
  
  public static final String JOB_ID = BambooJobRunner.class.getName() + ":ID";
  public static final long DEFAULT_INTERVAL = 60000L;
}
