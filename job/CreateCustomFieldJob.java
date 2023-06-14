package com.go2group.synapse.job;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.context.IssueContextImpl;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.I18nHelper.BeanFactory;
import com.atlassian.jira.web.action.admin.translation.TranslationManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.scheduler.JobRunnerRequest;
import com.atlassian.scheduler.JobRunnerResponse;
import com.go2group.synapse.config.SynapseConfig;
import com.go2group.synapse.constant.SynapseCustomField;
import com.go2group.synapse.core.enums.UrgencyFieldEnum;
import com.go2group.synapse.enums.TestRunStatusEnum;
import com.go2group.synapse.helper.CustomFieldHelper;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class CreateCustomFieldJob implements CreateCustomFieldJobRunner
{
  private static final Logger log = Logger.getLogger(CreateCustomFieldJob.class);
  private final I18nHelper i18n;
  private final SynapseConfig synapseConfig;
  
  public CreateCustomFieldJob(@ComponentImport I18nHelper i18n, SynapseConfig synapseConfig) {
    this.i18n = i18n;
    this.synapseConfig = synapseConfig;
  }
  
  public JobRunnerResponse runJob(JobRunnerRequest request)
  {
    log.debug("custom field creation - starts..");
    try {
      setupCustomField();
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
    log.debug("custom field creation - ends..");
    return JobRunnerResponse.success();
  }
  
  public void setupCustomField() { log.debug("setupCustomField - starts");
    
    List<String> testCaseTypeIds = synapseConfig.getIssueTypeIds("Test Case");
    List<String> reqTypeIds = synapseConfig.getIssueTypeIds("Requirement");
    
    Locale localeEnglish = Locale.ENGLISH;
    I18nHelper i18nEnglish = ComponentAccessor.getI18nHelperFactory().getInstance(localeEnglish);
    
    Locale localeGerman = Locale.GERMAN;
    I18nHelper i18nGerman = ComponentAccessor.getI18nHelperFactory().getInstance(localeGerman);
    
    Locale localeFrench = Locale.FRENCH;
    I18nHelper i18nFrench = ComponentAccessor.getI18nHelperFactory().getInstance(localeFrench);
    
    Locale localeJapanese = Locale.JAPANESE;
    I18nHelper i18nJapanese = ComponentAccessor.getI18nHelperFactory().getInstance(localeJapanese);
    
    Locale localeKorean = Locale.KOREAN;
    I18nHelper i18nKorean = ComponentAccessor.getI18nHelperFactory().getInstance(localeKorean);
    



    Locale localeSimplifiedChinese = Locale.SIMPLIFIED_CHINESE;
    I18nHelper i18nSimplifiedChinese = ComponentAccessor.getI18nHelperFactory().getInstance(localeSimplifiedChinese);
    
    Locale localeTraditionalChinese = Locale.TRADITIONAL_CHINESE;
    I18nHelper i18nTraditionalChinese = ComponentAccessor.getI18nHelperFactory().getInstance(localeTraditionalChinese);
    
    String fieldName = SynapseCustomField.TESTSUITE.getName();
    if (fieldName.equals(SynapseCustomField.TESTSUITE.getKey())) {
      fieldName = "Test Suite";
    }
    log.debug("setupCustomField - fieldName : " + fieldName);
    String description = SynapseCustomField.TESTSUITE.getDescription();
    CustomField synapseCustomField = CustomFieldHelper.createCustomField(SynapseCustomField.TESTSUITE.getKey(), fieldName, description, SynapseCustomField.TESTSUITE.getFieldTypeKey(), testCaseTypeIds, true);
    
    String fieldKey = SynapseCustomField.TESTSUITE.getKey();
    String descKey = SynapseCustomField.TESTSUITE.getDescriptionKey();
    TranslationManager translationManager = ComponentAccessor.getTranslationManager();
    if (synapseCustomField != null) {
      translationManager.setCustomFieldTranslation(synapseCustomField, localeEnglish, i18nEnglish.getText(fieldKey), i18nEnglish.getText(descKey));
      translationManager.setCustomFieldTranslation(synapseCustomField, localeGerman, i18nGerman.getText(fieldKey), i18nGerman.getText(descKey));
      translationManager.setCustomFieldTranslation(synapseCustomField, localeFrench, i18nFrench.getText(fieldKey), i18nFrench.getText(descKey));
      translationManager.setCustomFieldTranslation(synapseCustomField, localeJapanese, i18nJapanese.getText(fieldKey), i18nJapanese.getText(descKey));
      translationManager.setCustomFieldTranslation(synapseCustomField, localeKorean, i18nKorean.getText(fieldKey), i18nKorean.getText(descKey));
      translationManager.setCustomFieldTranslation(synapseCustomField, localeSimplifiedChinese, i18nSimplifiedChinese.getText(fieldKey), i18nSimplifiedChinese.getText(descKey));
      translationManager.setCustomFieldTranslation(synapseCustomField, localeTraditionalChinese, i18nTraditionalChinese.getText(fieldKey), i18nTraditionalChinese.getText(descKey));
    }
    
    fieldName = SynapseCustomField.REQUIREMENTSUITE.getName();
    if (fieldName.equals(SynapseCustomField.REQUIREMENTSUITE.getKey())) {
      fieldName = "Requirement Suite";
    }
    log.debug("setupCustomField - fieldName : " + fieldName);
    description = SynapseCustomField.REQUIREMENTSUITE.getDescription();
    synapseCustomField = CustomFieldHelper.createCustomField(SynapseCustomField.REQUIREMENTSUITE.getKey(), fieldName, description, SynapseCustomField.REQUIREMENTSUITE.getFieldTypeKey(), testCaseTypeIds, true);
    
    fieldKey = SynapseCustomField.REQUIREMENTSUITE.getKey();
    descKey = SynapseCustomField.REQUIREMENTSUITE.getDescriptionKey();
    if (synapseCustomField != null) {
      translationManager.setCustomFieldTranslation(synapseCustomField, localeEnglish, i18nEnglish.getText(fieldKey), i18nEnglish.getText(descKey));
      translationManager.setCustomFieldTranslation(synapseCustomField, localeGerman, i18nGerman.getText(fieldKey), i18nGerman.getText(descKey));
      translationManager.setCustomFieldTranslation(synapseCustomField, localeFrench, i18nFrench.getText(fieldKey), i18nFrench.getText(descKey));
      translationManager.setCustomFieldTranslation(synapseCustomField, localeJapanese, i18nJapanese.getText(fieldKey), i18nJapanese.getText(descKey));
      translationManager.setCustomFieldTranslation(synapseCustomField, localeKorean, i18nKorean.getText(fieldKey), i18nKorean.getText(descKey));
      translationManager.setCustomFieldTranslation(synapseCustomField, localeSimplifiedChinese, i18nSimplifiedChinese.getText(fieldKey), i18nSimplifiedChinese.getText(descKey));
      translationManager.setCustomFieldTranslation(synapseCustomField, localeTraditionalChinese, i18nTraditionalChinese.getText(fieldKey), i18nTraditionalChinese.getText(descKey));
    }
    
    fieldName = SynapseCustomField.REQUIREMENT.getName();
    if (fieldName.equals(SynapseCustomField.REQUIREMENT.getKey())) {
      fieldName = "Requirement";
    }
    log.debug("setupCustomField - fieldName : " + fieldName);
    description = SynapseCustomField.REQUIREMENT.getDescription();
    synapseCustomField = CustomFieldHelper.createCustomField(SynapseCustomField.REQUIREMENT.getKey(), fieldName, description, SynapseCustomField.REQUIREMENT.getFieldTypeKey(), testCaseTypeIds, true);
    
    fieldKey = SynapseCustomField.REQUIREMENT.getKey();
    descKey = SynapseCustomField.REQUIREMENT.getDescriptionKey();
    if (synapseCustomField != null) {
      translationManager.setCustomFieldTranslation(synapseCustomField, localeEnglish, i18nEnglish.getText(fieldKey), i18nEnglish.getText(descKey));
      translationManager.setCustomFieldTranslation(synapseCustomField, localeGerman, i18nGerman.getText(fieldKey), i18nGerman.getText(descKey));
      translationManager.setCustomFieldTranslation(synapseCustomField, localeFrench, i18nFrench.getText(fieldKey), i18nFrench.getText(descKey));
      translationManager.setCustomFieldTranslation(synapseCustomField, localeJapanese, i18nJapanese.getText(fieldKey), i18nJapanese.getText(descKey));
      translationManager.setCustomFieldTranslation(synapseCustomField, localeKorean, i18nKorean.getText(fieldKey), i18nKorean.getText(descKey));
      translationManager.setCustomFieldTranslation(synapseCustomField, localeSimplifiedChinese, i18nSimplifiedChinese.getText(fieldKey), i18nSimplifiedChinese.getText(descKey));
      translationManager.setCustomFieldTranslation(synapseCustomField, localeTraditionalChinese, i18nTraditionalChinese.getText(fieldKey), i18nTraditionalChinese.getText(descKey));
    }
    
    fieldName = SynapseCustomField.PARENT_REQUIREMENT.getName();
    if (fieldName.equals(SynapseCustomField.PARENT_REQUIREMENT.getKey())) {
      fieldName = "Parent Requirement";
    }
    log.debug("setupCustomField - fieldName : " + fieldName);
    description = SynapseCustomField.PARENT_REQUIREMENT.getDescription();
    synapseCustomField = CustomFieldHelper.createCustomField(SynapseCustomField.PARENT_REQUIREMENT.getKey(), fieldName, description, SynapseCustomField.REQUIREMENT.getFieldTypeKey(), reqTypeIds, true);
    
    fieldKey = SynapseCustomField.PARENT_REQUIREMENT.getKey();
    descKey = SynapseCustomField.PARENT_REQUIREMENT.getDescriptionKey();
    if (synapseCustomField != null) {
      translationManager.setCustomFieldTranslation(synapseCustomField, localeEnglish, i18nEnglish.getText(fieldKey), i18nEnglish.getText(descKey));
      translationManager.setCustomFieldTranslation(synapseCustomField, localeGerman, i18nGerman.getText(fieldKey), i18nGerman.getText(descKey));
      translationManager.setCustomFieldTranslation(synapseCustomField, localeFrench, i18nFrench.getText(fieldKey), i18nFrench.getText(descKey));
      translationManager.setCustomFieldTranslation(synapseCustomField, localeJapanese, i18nJapanese.getText(fieldKey), i18nJapanese.getText(descKey));
      translationManager.setCustomFieldTranslation(synapseCustomField, localeKorean, i18nKorean.getText(fieldKey), i18nKorean.getText(descKey));
      translationManager.setCustomFieldTranslation(synapseCustomField, localeSimplifiedChinese, i18nSimplifiedChinese.getText(fieldKey), i18nSimplifiedChinese.getText(descKey));
      translationManager.setCustomFieldTranslation(synapseCustomField, localeTraditionalChinese, i18nTraditionalChinese.getText(fieldKey), i18nTraditionalChinese.getText(descKey));
    }
    
    fieldName = SynapseCustomField.CHILD_REQUIREMENT.getName();
    if (fieldName.equals(SynapseCustomField.CHILD_REQUIREMENT.getKey())) {
      fieldName = "Child Requirement";
    }
    log.debug("setupCustomField - fieldName : " + fieldName);
    description = SynapseCustomField.CHILD_REQUIREMENT.getDescription();
    synapseCustomField = CustomFieldHelper.createCustomField(SynapseCustomField.CHILD_REQUIREMENT.getKey(), fieldName, description, SynapseCustomField.REQUIREMENT.getFieldTypeKey(), reqTypeIds, true);
    
    fieldKey = SynapseCustomField.CHILD_REQUIREMENT.getKey();
    descKey = SynapseCustomField.CHILD_REQUIREMENT.getDescriptionKey();
    if (synapseCustomField != null) {
      translationManager.setCustomFieldTranslation(synapseCustomField, localeEnglish, i18nEnglish.getText(fieldKey), i18nEnglish.getText(descKey));
      translationManager.setCustomFieldTranslation(synapseCustomField, localeGerman, i18nGerman.getText(fieldKey), i18nGerman.getText(descKey));
      translationManager.setCustomFieldTranslation(synapseCustomField, localeFrench, i18nFrench.getText(fieldKey), i18nFrench.getText(descKey));
      translationManager.setCustomFieldTranslation(synapseCustomField, localeJapanese, i18nJapanese.getText(fieldKey), i18nJapanese.getText(descKey));
      translationManager.setCustomFieldTranslation(synapseCustomField, localeKorean, i18nKorean.getText(fieldKey), i18nKorean.getText(descKey));
      translationManager.setCustomFieldTranslation(synapseCustomField, localeSimplifiedChinese, i18nSimplifiedChinese.getText(fieldKey), i18nSimplifiedChinese.getText(descKey));
      translationManager.setCustomFieldTranslation(synapseCustomField, localeTraditionalChinese, i18nTraditionalChinese.getText(fieldKey), i18nTraditionalChinese.getText(descKey));
    }
    
    fieldName = SynapseCustomField.TESTCASE.getName();
    if (fieldName.equals(SynapseCustomField.TESTCASE.getKey())) {
      fieldName = "Test Case";
    }
    log.debug("setupCustomField - fieldName : " + fieldName);
    description = SynapseCustomField.TESTCASE.getDescription();
    synapseCustomField = CustomFieldHelper.createCustomField(SynapseCustomField.TESTCASE.getKey(), fieldName, description, SynapseCustomField.TESTCASE.getFieldTypeKey(), testCaseTypeIds, true);
    
    fieldKey = SynapseCustomField.TESTCASE.getKey();
    descKey = SynapseCustomField.TESTCASE.getDescriptionKey();
    if (synapseCustomField != null) {
      translationManager.setCustomFieldTranslation(synapseCustomField, localeEnglish, i18nEnglish.getText(fieldKey), i18nEnglish.getText(descKey));
      translationManager.setCustomFieldTranslation(synapseCustomField, localeGerman, i18nGerman.getText(fieldKey), i18nGerman.getText(descKey));
      translationManager.setCustomFieldTranslation(synapseCustomField, localeFrench, i18nFrench.getText(fieldKey), i18nFrench.getText(descKey));
      translationManager.setCustomFieldTranslation(synapseCustomField, localeJapanese, i18nJapanese.getText(fieldKey), i18nJapanese.getText(descKey));
      translationManager.setCustomFieldTranslation(synapseCustomField, localeKorean, i18nKorean.getText(fieldKey), i18nKorean.getText(descKey));
      translationManager.setCustomFieldTranslation(synapseCustomField, localeSimplifiedChinese, i18nSimplifiedChinese.getText(fieldKey), i18nSimplifiedChinese.getText(descKey));
      translationManager.setCustomFieldTranslation(synapseCustomField, localeTraditionalChinese, i18nTraditionalChinese.getText(fieldKey), i18nTraditionalChinese.getText(descKey));
    }
    

    fieldName = SynapseCustomField.RUNSTATUS.getName();
    if (fieldName.equals(SynapseCustomField.RUNSTATUS.getKey())) {
      fieldName = "Run Status";
    }
    description = SynapseCustomField.RUNSTATUS.getDescription();
    synapseCustomField = CustomFieldHelper.createCustomField(SynapseCustomField.RUNSTATUS.getKey(), fieldName, description, SynapseCustomField.RUNSTATUS.getFieldTypeKey(), testCaseTypeIds, true);
    if (synapseCustomField != null) {
      createOptions("select", synapseCustomField);
      

      fieldKey = SynapseCustomField.RUNSTATUS.getKey();
      descKey = SynapseCustomField.RUNSTATUS.getDescriptionKey();
      translationManager.setCustomFieldTranslation(synapseCustomField, localeEnglish, i18nEnglish.getText(fieldKey), i18nEnglish.getText(descKey));
      translationManager.setCustomFieldTranslation(synapseCustomField, localeGerman, i18nGerman.getText(fieldKey), i18nGerman.getText(descKey));
      translationManager.setCustomFieldTranslation(synapseCustomField, localeFrench, i18nFrench.getText(fieldKey), i18nFrench.getText(descKey));
      translationManager.setCustomFieldTranslation(synapseCustomField, localeJapanese, i18nJapanese.getText(fieldKey), i18nJapanese.getText(descKey));
      translationManager.setCustomFieldTranslation(synapseCustomField, localeKorean, i18nKorean.getText(fieldKey), i18nKorean.getText(descKey));
      translationManager.setCustomFieldTranslation(synapseCustomField, localeSimplifiedChinese, i18nSimplifiedChinese.getText(fieldKey), i18nSimplifiedChinese.getText(descKey));
      translationManager.setCustomFieldTranslation(synapseCustomField, localeTraditionalChinese, i18nTraditionalChinese.getText(fieldKey), i18nTraditionalChinese.getText(descKey));
    }
    

    fieldName = SynapseCustomField.REQUIREMENT_LINK.getName();
    if (fieldName.equals(SynapseCustomField.REQUIREMENT_LINK.getKey())) {
      fieldName = "Linked";
    }
    log.debug("setupCustomField - fieldName : " + fieldName);
    description = SynapseCustomField.REQUIREMENT_LINK.getDescription();
    CustomFieldHelper.createCustomField(SynapseCustomField.REQUIREMENT_LINK.getKey(), fieldName, description, SynapseCustomField.REQUIREMENT_LINK.getFieldTypeKey(), reqTypeIds, false);
    
    fieldName = SynapseCustomField.TESTERSOFCYCLE.getName();
    if (fieldName.equals(SynapseCustomField.TESTERSOFCYCLE.getKey())) {
      fieldName = "testersOfCycle";
    }
    log.debug("setupCustomField - fieldName : " + fieldName);
    description = SynapseCustomField.TESTERSOFCYCLE.getDescription();
    CustomFieldHelper.createCustomField(SynapseCustomField.TESTERSOFCYCLE.getKey(), fieldName, description, SynapseCustomField.TESTERSOFCYCLE.getFieldTypeKey(), testCaseTypeIds, true);
    
    fieldName = SynapseCustomField.RESULTSOFCYCLE.getName();
    if (fieldName.equals(SynapseCustomField.RESULTSOFCYCLE.getKey())) {
      fieldName = "resultsOfCycle";
    }
    log.debug("setupCustomField - fieldName : " + fieldName);
    description = SynapseCustomField.RESULTSOFCYCLE.getDescription();
    CustomField createdCustomField = CustomFieldHelper.createCustomField(SynapseCustomField.RESULTSOFCYCLE.getKey(), fieldName, description, SynapseCustomField.RESULTSOFCYCLE.getFieldTypeKey(), testCaseTypeIds, true);
    if (createdCustomField != null) {
      createOptions("select", createdCustomField);
    }
    
    fieldName = SynapseCustomField.URGENCYOFRUN.getName();
    if (fieldName.equals(SynapseCustomField.URGENCYOFRUN.getKey())) {
      fieldName = "urgencyOfRun";
    }
    log.debug("setupCustomField - fieldName : " + fieldName);
    description = SynapseCustomField.URGENCYOFRUN.getDescription();
    synapseCustomField = CustomFieldHelper.createCustomField(SynapseCustomField.URGENCYOFRUN.getKey(), fieldName, description, SynapseCustomField.URGENCYOFRUN.getFieldTypeKey(), testCaseTypeIds, true);
    if (synapseCustomField != null) {
      createOptions("select", synapseCustomField);
    }
    
    fieldName = SynapseCustomField.RUNATTRIBUTES.getName();
    if (fieldName.equals(SynapseCustomField.RUNATTRIBUTES.getKey())) {
      fieldName = "runAttributes";
    }
    log.debug("setupCustomField - fieldName : " + fieldName);
    description = SynapseCustomField.RUNATTRIBUTES.getDescription();
    synapseCustomField = CustomFieldHelper.createCustomField(SynapseCustomField.RUNATTRIBUTES.getKey(), fieldName, description, SynapseCustomField.RUNATTRIBUTES.getFieldTypeKey(), testCaseTypeIds, true);
    if (synapseCustomField != null) {
      createOptions("select", synapseCustomField);
    }
    
    fieldName = SynapseCustomField.TEST_INFO.getName();
    description = SynapseCustomField.TEST_INFO.getDescription();
    List<String> requirementTypeIds = synapseConfig.getIssueTypeIds("Requirement");
    if ((requirementTypeIds != null) && (requirementTypeIds.size() > 0)) {
      synapseCustomField = CustomFieldHelper.createCustomField(SynapseCustomField.TEST_INFO.getKey(), fieldName, description, SynapseCustomField.TEST_INFO.getFieldTypeKey(), requirementTypeIds, false);
      
      if (synapseCustomField != null) {
        createOptions("select", synapseCustomField);
        
        fieldKey = SynapseCustomField.TEST_INFO.getKey();
        descKey = SynapseCustomField.TEST_INFO.getDescriptionKey();
        translationManager.setCustomFieldTranslation(synapseCustomField, localeEnglish, i18nEnglish.getText(fieldKey), i18nEnglish.getText(descKey));
        translationManager.setCustomFieldTranslation(synapseCustomField, localeGerman, i18nGerman.getText(fieldKey), i18nGerman.getText(descKey));
        translationManager.setCustomFieldTranslation(synapseCustomField, localeFrench, i18nFrench.getText(fieldKey), i18nFrench.getText(descKey));
        translationManager.setCustomFieldTranslation(synapseCustomField, localeJapanese, i18nJapanese.getText(fieldKey), i18nJapanese.getText(descKey));
        translationManager.setCustomFieldTranslation(synapseCustomField, localeKorean, i18nKorean.getText(fieldKey), i18nKorean.getText(descKey));
        translationManager.setCustomFieldTranslation(synapseCustomField, localeSimplifiedChinese, i18nSimplifiedChinese.getText(fieldKey), i18nSimplifiedChinese.getText(descKey));
        translationManager.setCustomFieldTranslation(synapseCustomField, localeTraditionalChinese, i18nTraditionalChinese.getText(fieldKey), i18nTraditionalChinese.getText(descKey));
      }
    }
    
    fieldName = SynapseCustomField.EXECUTION_COUNT.getName();
    description = SynapseCustomField.EXECUTION_COUNT.getDescription();
    synapseCustomField = CustomFieldHelper.createCustomField(SynapseCustomField.EXECUTION_COUNT.getKey(), fieldName, description, SynapseCustomField.EXECUTION_COUNT.getFieldTypeKey(), testCaseTypeIds, true);
    if (synapseCustomField != null) {
      fieldKey = SynapseCustomField.EXECUTION_COUNT.getKey();
      descKey = SynapseCustomField.EXECUTION_COUNT.getDescriptionKey();
      translationManager.setCustomFieldTranslation(synapseCustomField, localeEnglish, i18nEnglish.getText(fieldKey), i18nEnglish.getText(descKey));
      translationManager.setCustomFieldTranslation(synapseCustomField, localeGerman, i18nGerman.getText(fieldKey), i18nGerman.getText(descKey));
      translationManager.setCustomFieldTranslation(synapseCustomField, localeFrench, i18nFrench.getText(fieldKey), i18nFrench.getText(descKey));
      translationManager.setCustomFieldTranslation(synapseCustomField, localeJapanese, i18nJapanese.getText(fieldKey), i18nJapanese.getText(descKey));
      translationManager.setCustomFieldTranslation(synapseCustomField, localeKorean, i18nKorean.getText(fieldKey), i18nKorean.getText(descKey));
      translationManager.setCustomFieldTranslation(synapseCustomField, localeSimplifiedChinese, i18nSimplifiedChinese.getText(fieldKey), i18nSimplifiedChinese.getText(descKey));
      translationManager.setCustomFieldTranslation(synapseCustomField, localeTraditionalChinese, i18nTraditionalChinese.getText(fieldKey), i18nTraditionalChinese.getText(descKey));
    }
  }
  
  private void createOptions(String string, CustomField cusfield)
  {
    log.debug("createOptions for the " + cusfield.getFieldName() + " - starts..");
    IssueFactory issueFactory = ComponentAccessor.getIssueFactory();
    MutableIssue issue = issueFactory.getIssue();
    IssueContextImpl issueContext = new IssueContextImpl(issue.getProjectId(), issue.getIssueTypeId());
    FieldConfig fieldConfig = cusfield.getRelevantConfig(issueContext);
    OptionsManager optionManager = (OptionsManager)ComponentAccessor.getComponent(OptionsManager.class);
    List<Option> options = optionManager.getOptions(fieldConfig);
    Set<String> optionValueList = new HashSet();
    if ((null != options) && (!options.isEmpty())) {
      for (Option option : options) {
        optionValueList.add(option.getValue());
      }
    }
    if ((cusfield.getFieldName().equalsIgnoreCase(i18n.getText("synapse.field.customfield.result.name"))) || 
    
      (cusfield.getFieldName().equalsIgnoreCase(i18n.getText("synapse.jql.customfield.result.translated.name"))) || 
      
      (cusfield.getFieldName().equalsIgnoreCase(i18n.getText("synapse.field.customfield.run.status.name")))) {
      if (!optionValueList.contains(TestRunStatusEnum.PASSED.getLocalizedName())) {
        optionManager.createOption(fieldConfig, null, Long.valueOf(1L), TestRunStatusEnum.PASSED
          .getLocalizedName());
      }
      if (!optionValueList.contains(TestRunStatusEnum.FAILED.getLocalizedName())) {
        optionManager.createOption(fieldConfig, null, Long.valueOf(2L), TestRunStatusEnum.FAILED
          .getLocalizedName());
      }
      if (!optionValueList.contains(TestRunStatusEnum.BLOCKED.getLocalizedName())) {
        optionManager.createOption(fieldConfig, null, Long.valueOf(3L), TestRunStatusEnum.BLOCKED
          .getLocalizedName());
      }
      if (!optionValueList.contains(TestRunStatusEnum.NOT_TESTED.getLocalizedName())) {
        Option defaultOption = optionManager.createOption(fieldConfig, null, Long.valueOf(4L), TestRunStatusEnum.NOT_TESTED
          .getLocalizedName());
        

        if (!cusfield.getFieldName().equalsIgnoreCase(i18n.getText("synapse.field.customfield.result.name"))) {
          cusfield.getCustomFieldType().setDefaultValue(fieldConfig, defaultOption);
        }
      }
      if ((!optionValueList.contains(TestRunStatusEnum.NOT_APPLICABLE.getLocalizedName())) && (
        (cusfield.getFieldName().equalsIgnoreCase(i18n.getText("synapse.field.customfield.result.name"))) || 
        
        (cusfield.getFieldName().equalsIgnoreCase(i18n.getText("synapse.jql.customfield.result.translated.name"))) || 
        
        (cusfield.getFieldName().equalsIgnoreCase(i18n.getText("synapse.field.customfield.run.status.name"))))) {
        optionManager.createOption(fieldConfig, null, Long.valueOf(5L), TestRunStatusEnum.NOT_APPLICABLE
          .getLocalizedName());
      }
    }
    
    if (cusfield.getFieldName().equalsIgnoreCase(i18n.getText("synapse.field.customfield.urgency.name"))) {
      if (!optionValueList.contains(UrgencyFieldEnum.CRITICAL.getI18nName(i18n))) {
        optionManager.createOption(fieldConfig, null, Long.valueOf(1L), UrgencyFieldEnum.CRITICAL
          .getI18nName(i18n));
      }
      if (!optionValueList.contains(UrgencyFieldEnum.HIGH.getI18nName(i18n))) {
        optionManager.createOption(fieldConfig, null, Long.valueOf(2L), UrgencyFieldEnum.HIGH.getI18nName(i18n));
      }
      if (!optionValueList.contains(UrgencyFieldEnum.MEDIUM.getI18nName(i18n))) {
        optionManager.createOption(fieldConfig, null, Long.valueOf(3L), UrgencyFieldEnum.MEDIUM.getI18nName(i18n));
      }
      if (!optionValueList.contains(UrgencyFieldEnum.LOW.getI18nName(i18n))) {
        optionManager.createOption(fieldConfig, null, Long.valueOf(4L), UrgencyFieldEnum.LOW.getI18nName(i18n));
      }
      log.debug("createOptions for the " + cusfield.getFieldName() + " - ends..");
    }
  }
}
