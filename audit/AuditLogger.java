package com.go2group.synapse.audit;

import com.atlassian.jira.user.ApplicationUser;
import com.go2group.synapse.bean.TestStepOutputBean;

public abstract interface AuditLogger
{
  public abstract void executeAuditLog(ApplicationUser paramApplicationUser, String paramString, TestStepOutputBean paramTestStepOutputBean1, TestStepOutputBean paramTestStepOutputBean2);
}
