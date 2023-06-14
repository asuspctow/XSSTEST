package com.go2group.synapse.helper;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.history.ChangeLogUtils;
import com.atlassian.jira.issue.util.IssueChangeHolder;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import org.ofbiz.core.entity.GenericValue;

public class ChangeLogUtil
{
  public ChangeLogUtil() {}
  
  public static void create(Issue issue, String fieldType, String field, String from, String fromString, String to, String toString)
  {
    ApplicationUser user = com.atlassian.jira.component.ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
    create(issue, fieldType, field, from, fromString, to, toString, user);
  }
  
  public static void create(Issue issue, String fieldType, String field, String from, String fromString, String to, String toString, ApplicationUser user) {
    GenericValue before = issue.getGenericValue();
    GenericValue after = issue.getGenericValue();
    IssueChangeHolder changeHolder = new com.atlassian.jira.issue.util.DefaultIssueChangeHolder();
    changeHolder.addChangeItem(new com.atlassian.jira.issue.history.ChangeItemBean(fieldType, field, from, fromString, to, toString));
    
    ChangeLogUtils.createChangeGroup(user, before, after, changeHolder.getChangeItems(), false);
  }
}
