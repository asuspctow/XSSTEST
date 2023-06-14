package com.go2group.synapse.job;

import com.atlassian.jira.mail.Email;
import com.go2group.synapse.bean.SubscriptionOutputBean;
import com.go2group.synapse.email.EmailBodyBuilderLookupFactory;
import com.go2group.synapse.email.SubscriptionEmailBuilder;
import com.go2group.synapse.email.SubscriptionEmailParam;
import com.go2group.synapse.manager.SynapseMailer;
import java.util.List;
import org.apache.log4j.Logger;

public class SendReportTask
  extends SynapseAsyncTask<SendReportTaskConfig>
{
  private static final Logger log = Logger.getLogger(SendReportTask.class);
  
  public SendReportTask(SendReportTaskConfig sendReportTaskConfig, SynapseMailer synapseMailer, EmailBodyBuilderLookupFactory emailBodyBuilderLookupFactory)
  {
    super(sendReportTaskConfig, synapseMailer, emailBodyBuilderLookupFactory);
  }
  
  public void run()
  {
    log.info("Task execution started :" + synapseAsyncTaskConfig);
    SendReportTaskConfig sendReportTaskConfig = (SendReportTaskConfig)synapseAsyncTaskConfig;
    SubscriptionOutputBean subscription; if (sendReportTaskConfig.getSubscription().getFilterId() != null) {
      SubscriptionEmailBuilder builder = new SubscriptionEmailBuilder(emailBodyBuilderLookupFactory);
      SubscriptionEmailParam emailParam = new SubscriptionEmailParam();
      
      subscription = sendReportTaskConfig.getSubscription();
      log.debug("subscription : " + subscription);
      emailParam.setSubscription(subscription);
      
      List<Email> emails = builder.buildEmails(emailParam);
      log.debug("emails : " + emails);
      if ((emails != null) && (emails.size() > 0)) {
        for (Email email : emails) {
          synapseMailer.sendEmail(email);
          log.info("Sent email to " + email.getTo() + " for the subscription '" + subscription.getName() + "'");
        }
      }
    }
    log.info("Task execution completed");
  }
  
  public static void main(String[] args) {}
}
