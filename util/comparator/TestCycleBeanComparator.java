package com.go2group.synapse.util.comparator;

import com.go2group.synapse.bean.TestCycleOutputBean;

public class TestCycleBeanComparator implements java.util.Comparator<TestCycleOutputBean>
{
  public TestCycleBeanComparator() {}
  
  public int compare(TestCycleOutputBean arg0, TestCycleOutputBean arg1)
  {
    if ((arg0 != null) && (arg0.getStartTime() != null)) {
      if ((arg1 != null) && (arg1.getStartTime() != null)) {
        return arg0.getStartTime().compareTo(arg1.getStartTime());
      }
      return 1;
    }
    
    return -1;
  }
}
