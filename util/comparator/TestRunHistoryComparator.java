package com.go2group.synapse.util.comparator;

import com.go2group.synapse.bean.TestRunHistoryOutputBean;
import java.util.Comparator;

public class TestRunHistoryComparator implements Comparator<TestRunHistoryOutputBean>
{
  public TestRunHistoryComparator() {}
  
  public int compare(TestRunHistoryOutputBean arg0, TestRunHistoryOutputBean arg1)
  {
    if ((arg0 != null) && (arg0.getExecutionTime() != null)) {
      if ((arg1 != null) && (arg1.getExecutionTime() != null)) {
        return arg0.getExecutionTime().compareTo(arg1.getExecutionTime());
      }
      return 1;
    }
    
    return -1;
  }
}
