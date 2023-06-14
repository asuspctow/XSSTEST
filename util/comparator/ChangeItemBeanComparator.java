package com.go2group.synapse.util.comparator;

import com.atlassian.jira.issue.history.ChangeItemBean;
import java.sql.Timestamp;
import java.util.Comparator;

public class ChangeItemBeanComparator
  implements Comparator<ChangeItemBean>
{
  public ChangeItemBeanComparator() {}
  
  public int compare(ChangeItemBean arg0, ChangeItemBean arg1)
  {
    if ((arg0 != null) && (arg0.getCreated() != null)) {
      if ((arg1 != null) && (arg0.getCreated() != null)) {
        return arg0.getCreated().compareTo(arg1.getCreated());
      }
      return 1;
    }
    
    return -1;
  }
}
