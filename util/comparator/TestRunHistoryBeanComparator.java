package com.go2group.synapse.util.comparator;

import com.go2group.synapse.bean.TestRunHistoryOutputBean;

public class TestRunHistoryBeanComparator implements java.util.Comparator<TestRunHistoryOutputBean>
{
  public TestRunHistoryBeanComparator() {}
  
  public int compare(TestRunHistoryOutputBean leftBean, TestRunHistoryOutputBean rightBean)
  {
    if ((leftBean == null) && (rightBean == null)) {
      return 0;
    }
    if ((leftBean != null) && (rightBean != null)) {
      return rightBean.getExecutionTime().compareTo(leftBean.getExecutionTime());
    }
    if (leftBean == null) {
      return 1;
    }
    if (rightBean == null) {
      return -1;
    }
    return 0;
  }
}
