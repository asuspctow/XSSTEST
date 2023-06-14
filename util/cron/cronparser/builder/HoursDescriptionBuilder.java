package com.go2group.synapse.util.cron.cronparser.builder;

import com.atlassian.jira.util.I18nHelper;
import com.go2group.synapse.util.cron.cronparser.DateAndTimeUtils;
import com.go2group.synapse.util.cron.cronparser.Options;
import java.text.MessageFormat;

public class HoursDescriptionBuilder extends AbstractDescriptionBuilder
{
  private final Options options;
  
  public HoursDescriptionBuilder(Options options, I18nHelper i18nHelper)
  {
    super(i18nHelper);
    this.options = options;
  }
  
  protected String getSingleItemDescription(String expression)
  {
    return DateAndTimeUtils.formatTime(expression, "0", options, getI18nHelper());
  }
  
  protected String getIntervalDescriptionFormat(String expression)
  {
    return MessageFormat.format(getI18nHelper().getText("synapse.cron.every_x") + " " + 
      plural(expression, getI18nHelper().getText("synapse.cron.hour"), getI18nHelper().getText("synapse.cron.hours")), new Object[] { expression });
  }
  
  protected String getBetweenDescriptionFormat(String expression)
  {
    return getI18nHelper().getText("synapse.cron.between_x_and_y");
  }
  
  protected String getDescriptionFormat(String expression)
  {
    return getI18nHelper().getText("synapse.cron.at_x");
  }
}
