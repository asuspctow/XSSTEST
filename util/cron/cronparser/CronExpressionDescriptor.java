package com.go2group.synapse.util.cron.cronparser;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.I18nHelper.BeanFactory;
import com.go2group.synapse.util.cron.cronparser.builder.DayOfMonthDescriptionBuilder;
import com.go2group.synapse.util.cron.cronparser.builder.DayOfWeekDescriptionBuilder;
import com.go2group.synapse.util.cron.cronparser.builder.HoursDescriptionBuilder;
import com.go2group.synapse.util.cron.cronparser.builder.MinutesDescriptionBuilder;
import com.go2group.synapse.util.cron.cronparser.builder.MonthDescriptionBuilder;
import com.go2group.synapse.util.cron.cronparser.builder.SecondsDescriptionBuilder;
import com.go2group.synapse.util.cron.cronparser.builder.YearDescriptionBuilder;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CronExpressionDescriptor
{
  private static final I18nHelper i18nHelper = ComponentAccessor.getI18nHelperFactory().getInstance(ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser());
  




  private static final Logger LOG = LoggerFactory.getLogger(CronExpressionDescriptor.class);
  private static final char[] specialCharacters = { '/', '-', ',', '*' };
  
  private CronExpressionDescriptor() {}
  
  public static String getDescription(String expression) throws ParseException
  {
    return getDescription(DescriptionTypeEnum.FULL, expression, new Options(), Locale.US);
  }
  
  public static String getDescription(String expression, Options options) throws ParseException {
    return getDescription(DescriptionTypeEnum.FULL, expression, options, Locale.US);
  }
  
  public static String getDescription(String expression, Locale locale) throws ParseException {
    return getDescription(DescriptionTypeEnum.FULL, expression, new Options(), locale);
  }
  
  public static String getDescription(String expression, Options options, Locale locale) throws ParseException {
    return getDescription(DescriptionTypeEnum.FULL, expression, options, locale);
  }
  
  public static String getDescription(DescriptionTypeEnum type, String expression) throws ParseException {
    return getDescription(type, expression, new Options(), Locale.US);
  }
  
  public static String getDescription(DescriptionTypeEnum type, String expression, Locale locale) throws ParseException {
    return getDescription(type, expression, new Options(), locale);
  }
  
  public static String getDescription(DescriptionTypeEnum type, String expression, Options options) throws ParseException {
    return getDescription(type, expression, options, Locale.US);
  }
  
  public static String getDescription(DescriptionTypeEnum type, String expression, Options options, Locale locale) throws ParseException
  {
    String description = "";
    try {
      String[] expressionParts = ExpressionParser.parse(expression, options, i18nHelper);
      switch (1.$SwitchMap$com$go2group$synapse$util$cron$cronparser$DescriptionTypeEnum[type.ordinal()]) {
      case 1: 
        description = getFullDescription(expressionParts, options);
        break;
      case 2: 
        description = getTimeOfDayDescription(expressionParts, options);
        break;
      case 3: 
        description = getHoursDescription(expressionParts, options);
        break;
      case 4: 
        description = getMinutesDescription(expressionParts);
        break;
      case 5: 
        description = getSecondsDescription(expressionParts);
        break;
      case 6: 
        description = getDayOfMonthDescription(expressionParts);
        break;
      case 7: 
        description = getMonthDescription(expressionParts);
        break;
      case 8: 
        description = getDayOfWeekDescription(expressionParts, options);
        break;
      case 9: 
        description = getYearDescription(expressionParts);
        break;
      default: 
        description = getSecondsDescription(expressionParts);
      }
    }
    catch (ParseException e) {
      if (!options.isThrowExceptionOnParseError()) {
        description = e.getMessage();
        LOG.debug("Exception parsing expression.", e);
      } else {
        LOG.error("Exception parsing expression.", e);
        throw e;
      }
    }
    return description;
  }
  



  private static String getYearDescription(String[] expressionParts)
  {
    return new YearDescriptionBuilder(i18nHelper).getSegmentDescription(expressionParts[6], ", " + i18nHelper.getText("synapse.cron.every_year"));
  }
  



  private static String getDayOfWeekDescription(String[] expressionParts, Options options)
  {
    return new DayOfWeekDescriptionBuilder(options, i18nHelper).getSegmentDescription(expressionParts[5], ", " + i18nHelper.getText("synapse.cron.every_day"));
  }
  



  private static String getMonthDescription(String[] expressionParts)
  {
    return new MonthDescriptionBuilder(i18nHelper).getSegmentDescription(expressionParts[4], "");
  }
  



  private static String getDayOfMonthDescription(String[] expressionParts)
  {
    String description = null;
    String exp = expressionParts[3].replace("?", "*");
    if ("L".equals(exp)) {
      description = ", " + i18nHelper.getText("synapse.cron.on_the_last_day_of_the_month");
    } else if (("WL".equals(exp)) || ("LW".equals(exp))) {
      description = ", " + i18nHelper.getText("synapse.cron.on_the_last_weekday_of_the_month");
    } else {
      Pattern pattern = Pattern.compile("(\\dW)|(W\\d)");
      Matcher matcher = pattern.matcher(exp);
      if (matcher.matches()) {
        int dayNumber = Integer.parseInt(matcher.group().replace("W", ""));
        String dayString = dayNumber == 1 ? i18nHelper.getText("synapse.cron.first_weekday") : MessageFormat.format(i18nHelper.getText("synapse.cron.weekday_nearest_day"), new Object[] { Integer.valueOf(dayNumber) });
        description = MessageFormat.format(", " + i18nHelper.getText("synapse.cron.on_the_of_the_month"), new Object[] { dayString });
      } else {
        description = new DayOfMonthDescriptionBuilder(i18nHelper).getSegmentDescription(exp, ", " + i18nHelper.getText("synapse.cron.every_day"));
      }
    }
    return description;
  }
  



  private static String getSecondsDescription(String[] expressionParts)
  {
    return new SecondsDescriptionBuilder(i18nHelper).getSegmentDescription(expressionParts[0], i18nHelper.getText("synapse.cron.every_second"));
  }
  



  private static String getMinutesDescription(String[] expressionParts)
  {
    return new MinutesDescriptionBuilder(i18nHelper).getSegmentDescription(expressionParts[1], i18nHelper.getText("synapse.cron.every_minute"));
  }
  



  private static String getHoursDescription(String[] expressionParts, Options opts)
  {
    return new HoursDescriptionBuilder(opts, i18nHelper).getSegmentDescription(expressionParts[2], i18nHelper.getText("synapse.cron.every_hour"));
  }
  



  private static String getTimeOfDayDescription(String[] expressionParts, Options opts)
  {
    String secondsExpression = expressionParts[0];
    String minutesExpression = expressionParts[1];
    String hoursExpression = expressionParts[2];
    StringBuilder description = new StringBuilder();
    
    if ((!StringUtils.containsAny(minutesExpression, specialCharacters)) && (!StringUtils.containsAny(hoursExpression, specialCharacters)) && (!StringUtils.containsAny(secondsExpression, specialCharacters))) {
      description.append(i18nHelper.getText("synapse.cron.at")).append(" ").append(DateAndTimeUtils.formatTime(hoursExpression, minutesExpression, secondsExpression, opts, i18nHelper));
    } else if ((minutesExpression.contains("-")) && (!minutesExpression.contains("/")) && (!StringUtils.containsAny(hoursExpression, specialCharacters)))
    {
      String[] minuteParts = minutesExpression.split("-");
      description.append(MessageFormat.format(i18nHelper.getText("synapse.cron.every_minute_between"), new Object[] { DateAndTimeUtils.formatTime(hoursExpression, minuteParts[0], opts, i18nHelper), 
        DateAndTimeUtils.formatTime(hoursExpression, minuteParts[1], opts, i18nHelper) }));
    } else if ((hoursExpression.contains(",")) && (!StringUtils.containsAny(minutesExpression, specialCharacters)))
    {
      String[] hourParts = hoursExpression.split(",");
      description.append(i18nHelper.getText("synapse.cron.at"));
      for (int i = 0; i < hourParts.length; i++) {
        description.append(" ").append(DateAndTimeUtils.formatTime(hourParts[i], minutesExpression, opts, i18nHelper));
        if (i < hourParts.length - 2) {
          description.append(",");
        }
        if (i == hourParts.length - 2) {
          description.append(" ");
          description.append(i18nHelper.getText("synapse.cron.and"));
        }
      }
    } else {
      String secondsDescription = getSecondsDescription(expressionParts);
      String minutesDescription = getMinutesDescription(expressionParts);
      String hoursDescription = getHoursDescription(expressionParts, opts);
      description.append(secondsDescription);
      if ((description.length() > 0) && (StringUtils.isNotEmpty(minutesDescription))) {
        description.append(", ");
      }
      description.append(minutesDescription);
      if ((description.length() > 0) && (StringUtils.isNotEmpty(hoursDescription))) {
        description.append(", ");
      }
      description.append(hoursDescription);
    }
    return description.toString();
  }
  




  private static String getFullDescription(String[] expressionParts, Options options)
  {
    String description = "";
    String timeSegment = getTimeOfDayDescription(expressionParts, options);
    String dayOfMonthDesc = getDayOfMonthDescription(expressionParts);
    String monthDesc = getMonthDescription(expressionParts);
    String dayOfWeekDesc = getDayOfWeekDescription(expressionParts, options);
    String yearDesc = getYearDescription(expressionParts);
    description = MessageFormat.format("{0}{1}{2}{3}", new Object[] { timeSegment, "*".equals(expressionParts[3]) ? dayOfWeekDesc : dayOfMonthDesc, monthDesc, yearDesc });
    description = transformVerbosity(description, options);
    description = transformCase(description, options);
    return description;
  }
  



  private static String transformCase(String description, Options options)
  {
    String descTemp = description;
    switch (options.getCasingType()) {
    case Sentence: 
      descTemp = StringUtils.upperCase(new StringBuilder().append("").append(descTemp.charAt(0)).toString()) + descTemp.substring(1);
      break;
    case Title: 
      descTemp = StringUtils.capitalize(descTemp);
      break;
    default: 
      descTemp = descTemp.toLowerCase();
    }
    
    return descTemp;
  }
  




  private static String transformVerbosity(String description, Options options)
  {
    String descTemp = description;
    if (!options.isVerbose()) {
      descTemp = descTemp.replace(i18nHelper.getText("synapse.cron.every_1_minute"), i18nHelper.getText("synapse.cron.every_minute"));
      descTemp = descTemp.replace(i18nHelper.getText("synapse.cron.every_1_hour"), i18nHelper.getText("synapse.cron.every_hour"));
      descTemp = descTemp.replace(i18nHelper.getText("synapse.cron.every_1_day"), i18nHelper.getText("synapse.cron.every_day"));
      descTemp = descTemp.replace(", " + i18nHelper.getText("synapse.cron.every_minute"), "");
      descTemp = descTemp.replace(", " + i18nHelper.getText("synapse.cron.every_hour"), "");
      descTemp = descTemp.replace(", " + i18nHelper.getText("synapse.cron.every_day"), "");
    }
    return descTemp;
  }
}
