package com.go2group.synapse.util.cron.cronparser.builder;

import com.atlassian.jira.util.I18nHelper;
import com.go2group.synapse.util.PluginUtil;
import java.text.MessageFormat;
import org.joda.time.DateTime;

public class YearDescriptionBuilder
  extends AbstractDescriptionBuilder
{
  public YearDescriptionBuilder(I18nHelper i18nHelper)
  {
    super(i18nHelper);
  }
  
  protected String getSingleItemDescription(String expression)
  {
    return new DateTime().withYear(Integer.parseInt(expression)).toString("yyyy", PluginUtil.getCurrentLocale());
  }
  
  protected String getIntervalDescriptionFormat(String expression)
  {
    return MessageFormat.format(", " + getI18nHelper().getText("synapse.cron.every_x") + " " + 
      plural(expression, getI18nHelper().getText("synapse.cron.year"), getI18nHelper().getText("synapse.cron.years")), new Object[] { expression });
  }
  
  protected String getBetweenDescriptionFormat(String expression)
  {
    return ", " + getI18nHelper().getText("synapse.cron.between_description_format");
  }
  
  protected String getDescriptionFormat(String expression)
  {
    return ", " + getI18nHelper().getText("synapse.cron.only_in_year");
  }
}
