package com.go2group.synapse.util.comparator;

import com.atlassian.jira.issue.Issue;
import java.util.Comparator;

public class IssueComparator implements Comparator<Issue>
{
  public IssueComparator() {}
  
  public int compare(Issue o1, Issue o2)
  {
    if (o1.getId().longValue() > o2.getId().longValue())
      return 1;
    if (o1.getId().longValue() < o2.getId().longValue()) {
      return -1;
    }
    return 0;
  }
}
