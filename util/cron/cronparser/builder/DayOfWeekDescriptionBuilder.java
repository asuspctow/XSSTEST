package com.go2group.synapse.util.cron.cronparser.builder;

import com.atlassian.jira.util.I18nHelper;
import com.go2group.synapse.util.PluginUtil;
import com.go2group.synapse.util.cron.cronparser.DateAndTimeUtils;
import com.go2group.synapse.util.cron.cronparser.Options;
import java.text.MessageFormat;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTime.Property;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class DayOfWeekDescriptionBuilder extends AbstractDescriptionBuilder
{
  private final Options options;
  
  public DayOfWeekDescriptionBuilder(I18nHelper i18nHelper)
  {
    super(i18nHelper);
    options = null;
  }
  
  public DayOfWeekDescriptionBuilder(Options options, I18nHelper i18nHelper) {
    super(i18nHelper);
    this.options = options;
  }
  
  protected String getSingleItemDescription(String expression)
  {
    String exp = expression;
    if (expression.contains("#")) {
      exp = expression.substring(0, expression.indexOf("#"));
    } else if (expression.contains("L")) {
      exp = exp.replace("L", "");
    }
    if (StringUtils.isNumeric(exp)) {
      int dayOfWeekNum = Integer.parseInt(exp);
      boolean isZeroBasedDayOfWeek = (options == null) || (options.isZeroBasedDayOfWeek());
      boolean isInvalidDayOfWeekForSetting = (options != null) && (!options.isZeroBasedDayOfWeek()) && (dayOfWeekNum <= 1);
      if ((isInvalidDayOfWeekForSetting) || ((isZeroBasedDayOfWeek) && (dayOfWeekNum == 0)))
        return DateAndTimeUtils.getDayOfWeekName(7);
      if ((options != null) && (!options.isZeroBasedDayOfWeek())) {
        dayOfWeekNum--;
      }
      return DateAndTimeUtils.getDayOfWeekName(dayOfWeekNum);
    }
    DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("EEE").withLocale(java.util.Locale.ENGLISH);
    return dateTimeFormatter.parseDateTime(WordUtils.capitalizeFully(exp)).dayOfWeek().getAsText(PluginUtil.getCurrentLocale());
  }
  

  protected String getIntervalDescriptionFormat(String expression)
  {
    return MessageFormat.format(", " + getI18nHelper().getText("synapse.cron.interval_description_format"), new Object[] { expression });
  }
  
  protected String getBetweenDescriptionFormat(String expression)
  {
    return ", " + getI18nHelper().getText("synapse.cron.between_weekday_description_format");
  }
  
  protected String getDescriptionFormat(String expression)
  {
    String format = null;
    if (expression.contains("#")) {
      String dayOfWeekOfMonthNumber = expression.substring(expression.indexOf("#") + 1);
      String dayOfWeekOfMonthDescription = "";
      if ("1".equals(dayOfWeekOfMonthNumber)) {
        dayOfWeekOfMonthDescription = getI18nHelper().getText("synapse.cron.first");
      } else if ("2".equals(dayOfWeekOfMonthNumber)) {
        dayOfWeekOfMonthDescription = getI18nHelper().getText("synapse.cron.second");
      } else if ("3".equals(dayOfWeekOfMonthNumber)) {
        dayOfWeekOfMonthDescription = getI18nHelper().getText("synapse.cron.third");
      } else if ("4".equals(dayOfWeekOfMonthNumber)) {
        dayOfWeekOfMonthDescription = getI18nHelper().getText("synapse.cron.fourth");
      } else if ("5".equals(dayOfWeekOfMonthNumber)) {
        dayOfWeekOfMonthDescription = getI18nHelper().getText("synapse.cron.fifth");
      }
      format = ", " + String.format(getI18nHelper().getText("synapse.cron.on_the_day_of_the_month"), new Object[] { dayOfWeekOfMonthDescription });
    } else if (expression.contains("L")) {
      format = ", " + getI18nHelper().getText("synapse.cron.on_the_last_of_the_month");
    } else {
      format = ", " + getI18nHelper().getText("synapse.cron.only_on");
    }
    return format;
  }
}
