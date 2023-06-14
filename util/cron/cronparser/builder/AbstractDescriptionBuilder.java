package com.go2group.synapse.util.cron.cronparser.builder;

import com.atlassian.jira.util.I18nHelper;
import java.text.MessageFormat;
import org.apache.commons.lang3.StringUtils;

public abstract class AbstractDescriptionBuilder
{
  private final I18nHelper i18nHelper;
  
  public AbstractDescriptionBuilder(I18nHelper i18nHelper)
  {
    this.i18nHelper = i18nHelper;
  }
  
  protected final char[] SpecialCharsMinusStar = { '/', '-', ',' };
  
  public String getSegmentDescription(String expression, String allDescription) {
    String description = "";
    if ((StringUtils.isEmpty(expression)) || ("0".equals(expression))) {
      description = "";
    } else if ("*".equals(expression)) {
      description = allDescription;
    } else if (!StringUtils.containsAny(expression, SpecialCharsMinusStar)) {
      description = MessageFormat.format(getDescriptionFormat(expression), new Object[] { getSingleItemDescription(expression) });
    } else if (expression.contains("/")) {
      String[] segments = expression.split("/");
      description = MessageFormat.format(getIntervalDescriptionFormat(segments[1]), new Object[] { getSingleItemDescription(segments[1]) });
      
      if (segments[0].contains("-")) {
        String betweenSegmentOfInterval = segments[0];
        String[] betweenSegments = betweenSegmentOfInterval.split("-");
        description = description + ", " + MessageFormat.format(getBetweenDescriptionFormat(betweenSegmentOfInterval), new Object[] { getSingleItemDescription(betweenSegments[0]), getSingleItemDescription(betweenSegments[1]) });
      }
    } else if (expression.contains("-")) {
      String[] segments = expression.split("-");
      description = MessageFormat.format(getBetweenDescriptionFormat(expression), new Object[] { getSingleItemDescription(segments[0]), getSingleItemDescription(segments[1]) });
    } else if (expression.contains(",")) {
      String[] segments = expression.split(",");
      StringBuilder descriptionContent = new StringBuilder();
      for (int i = 0; i < segments.length; i++) {
        if ((i > 0) && (segments.length > 2) && 
          (i < segments.length - 1)) {
          descriptionContent.append(", ");
        }
        
        if ((i > 0) && (segments.length > 1) && ((i == segments.length - 1) || (segments.length == 2))) {
          descriptionContent.append(" ");
          descriptionContent.append(i18nHelper.getText("synapse.cron.and"));
          descriptionContent.append(" ");
        }
        descriptionContent.append(getSingleItemDescription(segments[i]));
      }
      description = MessageFormat.format(getDescriptionFormat(expression), new Object[] { descriptionContent.toString() });
    }
    return description;
  }
  





  protected abstract String getBetweenDescriptionFormat(String paramString);
  





  protected abstract String getIntervalDescriptionFormat(String paramString);
  




  protected abstract String getSingleItemDescription(String paramString);
  




  protected abstract String getDescriptionFormat(String paramString);
  




  @Deprecated
  protected String plural(int num, String singular, String plural)
  {
    return plural(String.valueOf(num), singular, plural);
  }
  






  protected String plural(String expression, String singular, String plural)
  {
    if ((org.apache.commons.lang3.math.NumberUtils.isNumber(expression)) && (Integer.parseInt(expression) > 1))
      return plural;
    if (StringUtils.contains(expression, ",")) {
      return plural;
    }
    return singular;
  }
  
  public I18nHelper getI18nHelper() {
    return i18nHelper;
  }
}
