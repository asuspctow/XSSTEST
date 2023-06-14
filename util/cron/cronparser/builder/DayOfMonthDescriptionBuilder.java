package com.go2group.synapse.util.cron.cronparser.builder;

import com.atlassian.jira.util.I18nHelper;

public class DayOfMonthDescriptionBuilder extends AbstractDescriptionBuilder
{
  public DayOfMonthDescriptionBuilder(I18nHelper i18nHelper) {
    super(i18nHelper);
  }
  
  protected String getSingleItemDescription(String expression)
  {
    return expression;
  }
  
  protected String getIntervalDescriptionFormat(String expression)
  {
    return ", " + getI18nHelper().getText("synapse.cron.every_x") + " " + plural(expression, getI18nHelper().getText("synapse.cron.day"), getI18nHelper().getText("synapse.cron.days"));
  }
  
  protected String getBetweenDescriptionFormat(String expression)
  {
    return ", " + getI18nHelper().getText("synapse.cron.between_days_of_the_month");
  }
  
  protected String getDescriptionFormat(String expression)
  {
    return ", " + getI18nHelper().getText("synapse.cron.on_day_of_the_month");
  }
}
