package com.go2group.synapse.util.comparator;

import java.util.Comparator;
import net.java.ao.Entity;

public class EntityComparator implements Comparator<Entity>
{
  public EntityComparator() {}
  
  public int compare(Entity o1, Entity o2)
  {
    if (o1.getID() > o2.getID())
      return 1;
    if (o1.getID() < o2.getID()) {
      return -1;
    }
    return 0;
  }
}
