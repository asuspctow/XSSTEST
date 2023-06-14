package com.go2group.synapse.job;

import com.atlassian.scheduler.config.JobRunnerKey;

public abstract interface CreateCustomFieldJobRunner extends com.atlassian.scheduler.JobRunner
{
  public static final JobRunnerKey JOB_KEY = JobRunnerKey.of(CreateCustomFieldJobRunner.class.getName());
  
  public static final String JOB_ID = CreateCustomFieldJobRunner.class.getName() + ":ID";
  public static final long DEFAULT_INTERVAL = 60000L;
}
