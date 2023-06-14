package com.go2group.synapse.helper;

import java.util.regex.Pattern;

public class NamingHelper
{
  public NamingHelper() {}
  
  public static boolean isInValidLabelName(String name) throws com.go2group.synapse.core.exception.InvalidDataException
  {
    Pattern pattern = Pattern.compile("[<>]");
    return pattern.matcher(name).find();
  }
  
  public static boolean isInValidName(String name) throws com.go2group.synapse.core.exception.InvalidDataException
  {
    Pattern pattern = Pattern.compile("[<>]");
    return pattern.matcher(name).find();
  }
}
