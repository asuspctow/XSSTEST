package com.go2group.synapse.job;

import com.go2group.synapse.email.EmailBodyBuilderLookupFactory;
import com.go2group.synapse.manager.SynapseMailer;


public abstract class SynapseAsyncTask<T extends SynapseAsyncTaskConfig>
  implements Runnable
{
  protected SynapseAsyncTaskConfig synapseAsyncTaskConfig;
  protected SynapseMailer synapseMailer;
  protected EmailBodyBuilderLookupFactory emailBodyBuilderLookupFactory;
  
  public SynapseAsyncTask(SynapseAsyncTaskConfig synapseAsyncTaskConfig, SynapseMailer synapseMailer, EmailBodyBuilderLookupFactory emailBodyBuilderLookupFactory)
  {
    this.synapseAsyncTaskConfig = synapseAsyncTaskConfig;
    this.synapseMailer = synapseMailer;
    this.emailBodyBuilderLookupFactory = emailBodyBuilderLookupFactory;
  }
}
