package com.go2group.synapse.util.cron.cronparser.builder;

import com.atlassian.jira.util.I18nHelper;
import com.go2group.synapse.util.cron.cronparser.DateAndTimeUtils;
import java.text.MessageFormat;

public class MinutesDescriptionBuilder extends AbstractDescriptionBuilder
{
  public MinutesDescriptionBuilder(I18nHelper i18nHelper)
  {
    super(i18nHelper);
  }
  
  protected String getSingleItemDescription(String expression)
  {
    return DateAndTimeUtils.formatMinutes(expression);
  }
  
  protected String getIntervalDescriptionFormat(String expression)
  {
    return MessageFormat.format(getI18nHelper().getText("synapse.cron.every_x") + " " + minPlural(expression), new Object[] { expression });
  }
  
  protected String getBetweenDescriptionFormat(String expression)
  {
    return getI18nHelper().getText("synapse.cron.minutes_through_past_the_hour");
  }
  
  protected String getDescriptionFormat(String expression)
  {
    return 
      getI18nHelper().getText("synapse.cron.at_x") + " " + minPlural(expression) + " " + getI18nHelper().getText("synapse.cron.past_the_hour");
  }
  
  private String minPlural(String expression) {
    return plural(expression, getI18nHelper().getText("synapse.cron.minute"), getI18nHelper().getText("synapse.cron.minutes"));
  }
}
