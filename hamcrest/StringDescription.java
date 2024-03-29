package org.hamcrest;

import java.io.IOException;

public class StringDescription
  extends BaseDescription
{
  private final Appendable out;
  
  public StringDescription()
  {
    this(new StringBuilder());
  }
  
  public StringDescription(Appendable out) {
    this.out = out;
  }
  







  public static String toString(SelfDescribing value)
  {
    return new StringDescription().appendDescriptionOf(value).toString();
  }
  


  public static String asString(SelfDescribing selfDescribing)
  {
    return toString(selfDescribing);
  }
  
  protected void append(String str) {
    try {
      out.append(str);
    } catch (IOException e) {
      throw new RuntimeException("Could not write description", e);
    }
  }
  
  protected void append(char c) {
    try {
      out.append(c);
    } catch (IOException e) {
      throw new RuntimeException("Could not write description", e);
    }
  }
  


  public String toString()
  {
    return out.toString();
  }
}
