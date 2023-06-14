package com.go2group.synapse.job;

import com.go2group.synapse.bean.SubscriptionOutputBean;

public class SendReportTaskConfig extends SynapseAsyncTaskConfig {
  private SubscriptionOutputBean subscription;
  private com.go2group.synapse.core.filter.service.SynapseFilterService synapseFilterService;
  
  public SendReportTaskConfig() {}
  
  public SubscriptionOutputBean getSubscription() {
    return subscription;
  }
  
  public void setSubscription(SubscriptionOutputBean subscription) {
    this.subscription = subscription;
  }
  
  public String toString()
  {
    return subscription.getName();
  }
  
  public com.go2group.synapse.core.filter.service.SynapseFilterService getSynapseFilterService() {
    return synapseFilterService;
  }
  
  public void setSynapseFilterService(com.go2group.synapse.core.filter.service.SynapseFilterService synapseFilterService) {
    this.synapseFilterService = synapseFilterService;
  }
}
