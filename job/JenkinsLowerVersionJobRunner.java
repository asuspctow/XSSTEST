package com.go2group.synapse.job;

import com.atlassian.scheduler.JobRunner;
import com.atlassian.scheduler.config.JobRunnerKey;

public abstract interface JenkinsLowerVersionJobRunner extends JobRunner
{
  public static final JobRunnerKey JOB_KEY = JobRunnerKey.of(JenkinsLowerVersionJobRunner.class.getName());
  
  public static final String JOB_ID = JenkinsLowerVersionJobRunner.class.getName() + ":ID";
  public static final long DEFAULT_INTERVAL = 60000L;
}
