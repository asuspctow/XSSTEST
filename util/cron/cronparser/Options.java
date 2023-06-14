package com.go2group.synapse.util.cron.cronparser;

public class Options
{
  private boolean throwExceptionOnParseError;
  private CasingTypeEnum casingType;
  private boolean verbose;
  private boolean zeroBasedDayOfWeek;
  private boolean twentyFourHourTime;
  
  public Options() {
    throwExceptionOnParseError = true;
    casingType = CasingTypeEnum.Sentence;
    verbose = false;
    
    zeroBasedDayOfWeek = true;
    twentyFourHourTime = false;
  }
  
  public static Options twentyFourHour() {
    Options opts = new Options();
    opts.setTwentyFourHourTime(true);
    return opts;
  }
  


  public boolean isThrowExceptionOnParseError()
  {
    return throwExceptionOnParseError;
  }
  


  public void setThrowExceptionOnParseError(boolean throwExceptionOnParseError)
  {
    this.throwExceptionOnParseError = throwExceptionOnParseError;
  }
  


  public CasingTypeEnum getCasingType()
  {
    return casingType;
  }
  


  public void setCasingType(CasingTypeEnum casingType)
  {
    this.casingType = casingType;
  }
  


  public boolean isVerbose()
  {
    return verbose;
  }
  


  public void setVerbose(boolean verbose)
  {
    this.verbose = verbose;
  }
  










  public void setZeroBasedDayOfWeek(boolean zeroBasedDayOfWeek)
  {
    this.zeroBasedDayOfWeek = zeroBasedDayOfWeek;
  }
  










  public boolean isZeroBasedDayOfWeek()
  {
    return zeroBasedDayOfWeek;
  }
  


  public boolean isTwentyFourHourTime()
  {
    return twentyFourHourTime;
  }
  


  public void setTwentyFourHourTime(boolean twentyFourHourTime)
  {
    this.twentyFourHourTime = twentyFourHourTime;
  }
}
