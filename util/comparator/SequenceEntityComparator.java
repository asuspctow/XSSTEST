package com.go2group.synapse.util.comparator;

import com.go2group.synapse.ao.SequenceEntity;
import java.util.Comparator;

public class SequenceEntityComparator implements Comparator<SequenceEntity>
{
  public SequenceEntityComparator() {}
  
  public int compare(SequenceEntity o1, SequenceEntity o2)
  {
    if (o1.getSequence().intValue() > o2.getSequence().intValue())
      return 1;
    if (o1.getSequence().intValue() < o2.getSequence().intValue()) {
      return -1;
    }
    return 0;
  }
}
