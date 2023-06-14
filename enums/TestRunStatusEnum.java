package com.go2group.synapse.enums;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.I18nHelper.BeanFactory;
import com.go2group.synapse.bean.RunStatusOutputBean;
import com.go2group.synapse.bean.status.RunStatusInfoOutputBean;
import com.go2group.synapse.core.common.bean.StatusLozenge;
import com.go2group.synapse.core.exception.InvalidDataException;
import com.go2group.synapse.service.RunStatusService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;




public class TestRunStatusEnum
{
  private static final Logger log = Logger.getLogger(TestRunStatusEnum.class);
  
  public static final String PASSED_STATUS = "Passed";
  
  public static final String FAILED_STATUS = "Failed";
  
  public static final String BLOCKED_STATUS = "Blocked";
  
  public static final String NOT_TESTED_STATUS = "Not Tested";
  public static final String NOT_APPLICABLE_STATUS = "NA";
  public static TestRunStatusEnum PASSED;
  public static TestRunStatusEnum FAILED;
  public static TestRunStatusEnum BLOCKED;
  public static TestRunStatusEnum NOT_TESTED;
  public static TestRunStatusEnum NOT_APPLICABLE;
  private final Integer id;
  private final String name;
  private String localizedName;
  private final StatusLozenge lozenge;
  private String color;
  private boolean enabled;
  private String aliasName;
  private String standardStatusKey;
  private static List<TestRunStatusEnum> statuses = new ArrayList();
  private static boolean valuesChanged = false;
  private static Locale currentLocale = null;
  
  static {
    currentLocale = ComponentAccessor.getApplicationProperties().getDefaultLocale();
    loadLocalizedStatuses();
  }
  
  private TestRunStatusEnum(String name, Integer id) {
    this.id = id;
    this.name = name;
    lozenge = StatusLozenge.getStatusLozenge(name);
  }
  
  public Integer getId() {
    return id;
  }
  
  public String getName() {
    return name;
  }
  
  public String getColor() {
    return color;
  }
  
  public void setColor(String color) {
    this.color = color;
  }
  
  public String getAliasName() {
    return aliasName;
  }
  
  public void setAliasName(String aliasName) {
    this.aliasName = aliasName;
  }
  
  public static TestRunStatusEnum getEnum(String name) {
    ApplicationUser contextUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
    return getEnum(name, contextUser);
  }
  
  public static TestRunStatusEnum getStandardStausEnumByKey(String key) {
    if (StringUtils.isBlank(key)) {
      return null;
    }
    if (PASSED.getStandardStatusKey().equals(key))
      return PASSED;
    if (FAILED.getStandardStatusKey().equals(key))
      return FAILED;
    if (BLOCKED.getStandardStatusKey().equals(key))
      return BLOCKED;
    if (NOT_TESTED.getStandardStatusKey().equals(key))
      return NOT_TESTED;
    if (NOT_APPLICABLE.getStandardStatusKey().equals(key)) {
      return NOT_APPLICABLE;
    }
    return null;
  }
  
  public static TestRunStatusEnum getEnum(String name, ApplicationUser contextUser) {
    for (TestRunStatusEnum status : values(contextUser)) {
      if ((name.equalsIgnoreCase(status.getName())) || (name.equalsIgnoreCase(status.getLocalizedName()))) {
        return status;
      }
    }
    return null;
  }
  
  public static TestRunStatusEnum getEnumById(Integer id) {
    ApplicationUser contextUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
    return getEnumById(id, contextUser);
  }
  
  public static TestRunStatusEnum getEnumById(Integer id, ApplicationUser contextUser) {
    for (TestRunStatusEnum status : values(contextUser)) {
      if (id.intValue() == status.getId().intValue()) {
        return status;
      }
    }
    return null;
  }
  
  private static synchronized List<TestRunStatusEnum> values(ApplicationUser contextUser) {
    if (contextUser == null) {
      contextUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
    }
    Locale changedDefaultLocale = ComponentAccessor.getApplicationProperties().getDefaultLocale();
    I18nHelper i18n = ComponentAccessor.getI18nHelperFactory().getInstance(contextUser);
    Locale userLocale = i18n.getLocale();
    if (currentLocale != null) {
      if (!currentLocale.equals(changedDefaultLocale)) {
        valuesChanged = true;
        currentLocale = changedDefaultLocale;
      }
      if (!currentLocale.equals(userLocale)) {
        valuesChanged = true;
        currentLocale = userLocale;
      }
    }
    if ((valuesChanged) || (statuses == null) || (statuses.size() == 0)) {
      loadLocalizedStatuses();
      valuesChanged = false;
    }
    List<TestRunStatusEnum> copyStatues = new ArrayList();
    if (statuses != null) {
      copyStatues.addAll(statuses);
    }
    return copyStatues;
  }
  
  public static List<TestRunStatusEnum> valuesList() {
    ApplicationUser contextUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
    return valuesList(contextUser);
  }
  
  public static List<TestRunStatusEnum> valuesList(ApplicationUser contextUser) {
    return values(contextUser);
  }
  
  public static List<TestRunStatusEnum> getActiveStatuses() {
    ApplicationUser contextUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
    return getActiveStatuses(contextUser);
  }
  
  public static List<TestRunStatusEnum> getActiveStatuses(ApplicationUser contextUser) {
    if (contextUser == null) {
      contextUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
    }
    List<TestRunStatusEnum> activeStatuses = new ArrayList();
    for (TestRunStatusEnum status : values(contextUser)) {
      if (status.isEnabled()) {
        activeStatuses.add(status);
      }
    }
    return activeStatuses;
  }
  
  public static List<String> getActiveStatusNames() {
    ApplicationUser contextUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
    return getActiveStatusNames(contextUser);
  }
  
  public static List<String> getActiveStatusNames(ApplicationUser contextUser) {
    List<String> activeStatuses = new ArrayList();
    for (TestRunStatusEnum status : values(contextUser)) {
      if (status.isEnabled()) {
        activeStatuses.add(status.getName());
      }
    }
    return activeStatuses;
  }
  
  public StatusLozenge getLozenge() {
    return lozenge;
  }
  
  public boolean isEnabled() {
    return enabled;
  }
  
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }
  
  public String getLocalizedName() {
    return localizedName;
  }
  
  public void setLocalizedName(String localizedName) {
    this.localizedName = localizedName;
  }
  
  public static void setValuesChanged(boolean valuesChanged) {
    valuesChanged = valuesChanged;
  }
  
  public String getStandardStatusKey() {
    return standardStatusKey;
  }
  
  public void setStandardStatusKey(String standardStatusKey) {
    this.standardStatusKey = standardStatusKey;
  }
  
  private static void loadLocalizedStatuses() {
    RunStatusService runStatusService = (RunStatusService)ComponentAccessor.getOSGiComponentInstanceOfType(RunStatusService.class);
    statuses = new ArrayList();
    try {
      i18n = ComponentAccessor.getI18nHelperFactory().getInstance(ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser());
      Collection<RunStatusOutputBean> allRunStatuses = runStatusService.getRunStatuses(new Integer[0]);
      if ((allRunStatuses != null) && (allRunStatuses.size() > 0))
        for (RunStatusOutputBean runStatus : allRunStatuses) {
          RunStatusInfoOutputBean runStatusInfo = runStatus.getRunStatusInfo();
          String searchName = runStatus.getName();
          String aliasName = "";
          if (runStatus.isDefault()) {
            if ((runStatusInfo != null) && (StringUtils.isNotBlank(runStatusInfo.getAliasName()))) {
              searchName = runStatusInfo.getAliasName();
            }
            for (StandardStatusEnum standardStatus : StandardStatusEnum.values()) {
              String i18nName = i18n.getText(standardStatus.getKey());
              if ((searchName != null) && (searchName.equals(i18nName))) {
                searchName = standardStatus.getName();
              }
            }
            aliasName = runStatusInfo != null ? runStatusInfo.getAliasName() : "";
          }
          StandardStatusEnum standardStatus = StandardStatusEnum.getEnum(searchName);
          if (standardStatus != null) {
            switch (1.$SwitchMap$com$go2group$synapse$enums$StandardStatusEnum[standardStatus.ordinal()]) {
            case 1: 
              PASSED = new TestRunStatusEnum(runStatus.getName(), runStatus.getId());
              PASSED.setLocalizedName(runStatus.getName());
              PASSED.setColor(runStatus.getColor());
              PASSED.setEnabled(runStatus.isEnabled());
              PASSED.setAliasName(aliasName);
              PASSED.setStandardStatusKey(standardStatus.getKey());
              statuses.add(PASSED);
              break;
            case 2: 
              FAILED = new TestRunStatusEnum(runStatus.getName(), runStatus.getId());
              FAILED.setLocalizedName(runStatus.getName());
              FAILED.setColor(runStatus.getColor());
              FAILED.setEnabled(runStatus.isEnabled());
              FAILED.setAliasName(aliasName);
              FAILED.setStandardStatusKey(standardStatus.getKey());
              statuses.add(FAILED);
              break;
            case 3: 
              BLOCKED = new TestRunStatusEnum(runStatus.getName(), runStatus.getId());
              BLOCKED.setLocalizedName(runStatus.getName());
              BLOCKED.setColor(runStatus.getColor());
              BLOCKED.setEnabled(runStatus.isEnabled());
              BLOCKED.setAliasName(aliasName);
              BLOCKED.setStandardStatusKey(standardStatus.getKey());
              statuses.add(BLOCKED);
              break;
            case 4: 
              NOT_TESTED = new TestRunStatusEnum(runStatus.getName(), runStatus.getId());
              NOT_TESTED.setLocalizedName(runStatus.getName());
              NOT_TESTED.setColor(runStatus.getColor());
              NOT_TESTED.setEnabled(runStatus.isEnabled());
              NOT_TESTED.setAliasName(aliasName);
              NOT_TESTED.setStandardStatusKey(standardStatus.getKey());
              statuses.add(NOT_TESTED);
              break;
            case 5: 
              NOT_APPLICABLE = new TestRunStatusEnum(runStatus.getName(), runStatus.getId());
              NOT_APPLICABLE.setLocalizedName(runStatus.getName());
              NOT_APPLICABLE.setColor(runStatus.getColor());
              NOT_APPLICABLE.setEnabled(runStatus.isEnabled());
              NOT_APPLICABLE.setAliasName(aliasName);
              NOT_APPLICABLE.setStandardStatusKey(standardStatus.getKey());
              statuses.add(NOT_APPLICABLE);
            }
          }
          else {
            TestRunStatusEnum status = new TestRunStatusEnum(runStatus.getName(), runStatus.getId());
            status.setLocalizedName(runStatus.getName());
            status.setColor(runStatus.getColor());
            status.setEnabled(runStatus.isEnabled());
            statuses.add(status);
          }
        }
    } catch (InvalidDataException e) {
      I18nHelper i18n;
      log.debug(e.getMessage(), e);
      log.error(e.getMessage());
    }
  }
}
