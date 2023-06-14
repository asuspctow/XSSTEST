package com.go2group.synapse.util.cron.cronparser.builder;

import com.atlassian.jira.util.I18nHelper;
import java.text.MessageFormat;

public class SecondsDescriptionBuilder extends AbstractDescriptionBuilder
{
  public SecondsDescriptionBuilder(I18nHelper i18nHelper)
  {
    super(i18nHelper);
  }
  
  protected String getSingleItemDescription(String expression)
  {
    return expression;
  }
  
  protected String getIntervalDescriptionFormat(String expression)
  {
    return MessageFormat.format(getI18nHelper().getText("synapse.cron.every_x_seconds"), new Object[] { expression });
  }
  
  protected String getBetweenDescriptionFormat(String expression)
  {
    return getI18nHelper().getText("synapse.cron.seconds_through_past_the_minute");
  }
  
  protected String getDescriptionFormat(String expression)
  {
    return getI18nHelper().getText("synapse.cron.at_x_seconds_past_the_minute");
  }
}
