package com.go2group.synapse.util.cron.cronparser;

import com.atlassian.jira.util.I18nHelper;
import java.text.ParseException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;



class ExpressionParser
{
  private ExpressionParser() {}
  
  @Deprecated
  public static String[] parse(String expression, I18nHelper i18nHelper)
    throws ParseException
  {
    return parse(expression, null, i18nHelper);
  }
  
  public static String[] parse(String expression, Options options, I18nHelper i18nHelper) throws ParseException {
    String[] parsed = { "", "", "", "", "", "", "" };
    if (StringUtils.isEmpty(expression)) {
      throw new IllegalArgumentException(i18nHelper.getText("synapse.cron.expression_empty_exception"));
    }
    
    String[] expressionParts = expression.split(" ");
    if (expressionParts.length < 5)
      throw new ParseException(expression, 0);
    if (expressionParts.length == 5) {
      parsed[0] = "";
      System.arraycopy(expressionParts, 0, parsed, 1, 5);
    } else if (expressionParts.length == 6)
    {
      Pattern yearRegex = Pattern.compile("(.*)\\d{4}$");
      if (yearRegex.matcher(expressionParts[5]).matches()) {
        System.arraycopy(expressionParts, 0, parsed, 1, 6);
      } else {
        System.arraycopy(expressionParts, 0, parsed, 0, 6);
      }
    } else if (expressionParts.length == 7) {
      parsed = expressionParts;
    } else {
      throw new ParseException(expression, 7);
    }
    
    normaliseExpression(parsed, options);
    
    return parsed;
  }
  



  private static void normaliseExpression(String[] expressionParts, Options options)
  {
    expressionParts[3] = expressionParts[3].replace('?', '*');
    expressionParts[5] = expressionParts[5].replace('?', '*');
    

    expressionParts[0] = (expressionParts[0].startsWith("0/") ? expressionParts[0].replace("0/", "*/") : expressionParts[0]);
    expressionParts[1] = (expressionParts[1].startsWith("0/") ? expressionParts[1].replace("0/", "*/") : expressionParts[1]);
    expressionParts[2] = (expressionParts[2].startsWith("0/") ? expressionParts[2].replace("0/", "*/") : expressionParts[2]);
    expressionParts[3] = (expressionParts[3].startsWith("1/") ? expressionParts[3].replace("1/", "*/") : expressionParts[3]);
    expressionParts[4] = (expressionParts[4].startsWith("1/") ? expressionParts[4].replace("1/", "*/") : expressionParts[4]);
    expressionParts[5] = (expressionParts[5].startsWith("1/") ? expressionParts[5].replace("1/", "*/") : expressionParts[5]);
    

    for (int i = 0; i < expressionParts.length; i++) {
      if ("*/1".equals(expressionParts[i])) {
        expressionParts[i] = "*";
      }
    }
    

    if (!StringUtils.isNumeric(expressionParts[5])) {
      for (int i = 0; i <= 6; i++) {
        expressionParts[5] = expressionParts[5].replace(DateAndTimeUtils.getDayOfWeekName(i + 1), String.valueOf(i));
      }
    }
    

    if (!StringUtils.isNumeric(expressionParts[4])) {
      for (int i = 1; i <= 12; i++) {
        DateTime currentMonth = new DateTime().withDayOfMonth(1).withMonthOfYear(i);
        String currentMonthDescription = currentMonth.toString("MMM", Locale.ENGLISH).toUpperCase();
        expressionParts[4] = expressionParts[4].replace(currentMonthDescription, String.valueOf(i));
      }
    }
    

    if ("0".equals(expressionParts[0])) {
      expressionParts[0] = "";
    }
    

    if (((options == null) || (options.isZeroBasedDayOfWeek())) && ("0".equals(expressionParts[5]))) {
      expressionParts[5] = "7";
    }
  }
}
