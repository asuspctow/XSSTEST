package com.go2group.synapse.job;

public class SynapseCronExpression
{
  private static String STAR = "*";
  
  private static String QUESTION = "?";
  
  private static String HYPHEN = "-";
  
  private static String HASH = "#";
  
  private static String SLASH = "/";
  
  private static String COMMA = ",";
  
  private boolean daily;
  
  private boolean weekly;
  private boolean monthly;
  private String cronExpression;
  
  public SynapseCronExpression(String cronExpression)
    throws Exception
  {
    String[] elements = cronExpression.split(" ");
    if ((elements != null) && (elements.length == 6)) {
      this.cronExpression = cronExpression;
    } else {
      throw new Exception("Invalid cron");
    }
  }
  
  public boolean isDaily() {
    String[] elements = cronExpression.split(" ");
    if ((elements != null) && (elements.length == 6)) {}
    




    return daily;
  }
  
  public void setDaily(boolean daily) {
    this.daily = daily;
  }
  
  public boolean isWeekly() {
    return weekly;
  }
  
  public void setWeekly(boolean weekly) {
    this.weekly = weekly;
  }
  
  public boolean isMonthly() {
    return monthly;
  }
  
  public void setMonthly(boolean monthly) {
    this.monthly = monthly;
  }
  
  public String getCronExpression() {
    return cronExpression;
  }
}
