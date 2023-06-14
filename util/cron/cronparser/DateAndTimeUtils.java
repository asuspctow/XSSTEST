package com.go2group.synapse.util.cron.cronparser;

import com.atlassian.jira.util.I18nHelper;
import com.go2group.synapse.util.PluginUtil;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTime.Property;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;





public final class DateAndTimeUtils
{
  private DateAndTimeUtils() {}
  
  public static String formatTime(String hoursExpression, String minutesExpression, Options opts, I18nHelper i18nHelper)
  {
    return formatTime(hoursExpression, minutesExpression, "", opts, i18nHelper);
  }
  





  public static String formatTime(String hoursExpression, String minutesExpression, String secondsExpression, Options opts, I18nHelper i18nHelper)
  {
    int hour = Integer.parseInt(hoursExpression);
    int minutes = Integer.parseInt(minutesExpression);
    
    DateTimeFormatter timeFormat;
    LocalTime localTime;
    DateTimeFormatter timeFormat;
    if (opts.isTwentyFourHourTime()) { DateTimeFormatter timeFormat;
      if (!StringUtils.isEmpty(secondsExpression)) {
        int seconds = Integer.parseInt(secondsExpression);
        LocalTime localTime = new LocalTime(hour, minutes, seconds);
        timeFormat = DateTimeFormat.mediumTime();
      } else {
        LocalTime localTime = new LocalTime(hour, minutes);
        timeFormat = DateTimeFormat.shortTime();
      }
    } else { DateTimeFormatter timeFormat;
      if (!StringUtils.isEmpty(secondsExpression)) {
        int seconds = Integer.parseInt(secondsExpression);
        LocalTime localTime = new LocalTime(hour, minutes, seconds);
        timeFormat = DateTimeFormat.forPattern("h:mm:ss a");
      } else {
        localTime = new LocalTime(hour, minutes);
        timeFormat = DateTimeFormat.forPattern("h:mm a");
      }
    }
    return localTime.toString(timeFormat.withLocale(PluginUtil.getCurrentLocale()));
  }
  
  public static String getDayOfWeekName(int dayOfWeek) {
    return new DateTime().withDayOfWeek(dayOfWeek).dayOfWeek().getAsText(PluginUtil.getCurrentLocale());
  }
  




  public static String formatMinutes(String minutesExpression)
  {
    if (StringUtils.contains(minutesExpression, ",")) {
      StringBuilder formattedExpression = new StringBuilder();
      for (String minute : StringUtils.split(minutesExpression, ',')) {
        formattedExpression.append(StringUtils.leftPad(minute, 2, '0'));
        formattedExpression.append(",");
      }
      return formattedExpression.toString();
    }
    return StringUtils.leftPad(minutesExpression, 2, '0');
  }
}
