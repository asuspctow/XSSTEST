package com.go2group.synapse.job;

import com.atlassian.scheduler.JobRunner;
import com.atlassian.scheduler.config.JobRunnerKey;

public abstract interface JenkinsJobRunner extends JobRunner
{
  public static final JobRunnerKey JOB_KEY = JobRunnerKey.of(JenkinsJobRunner.class.getName());
  
  public static final String JOB_ID = JenkinsJobRunner.class.getName() + ":ID";
  public static final long DEFAULT_INTERVAL = 60000L;
}
