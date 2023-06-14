package com.go2group.synapse.helper;

import com.atlassian.greenhopper.service.sprint.Sprint;
import com.atlassian.greenhopper.service.sprint.Sprint.SprintBuilder;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.label.LabelManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.go2group.synapse.bean.ImportBean;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class CustomFieldHelper
  extends com.go2group.synapse.core.helper.CustomFieldHelper
{
  private JiraAuthenticationContext jiraAuthenticationContext;
  private static final Logger log = Logger.getLogger(CustomFieldHelper.class);
  private String dateFormat;
  
  public CustomFieldHelper(JiraAuthenticationContext jiraAuthenticationContext, String dateFormat) { this.jiraAuthenticationContext = jiraAuthenticationContext;
    this.dateFormat = dateFormat;
  }
  
  public CustomFieldHelper(JiraAuthenticationContext jiraAuthenticationContext) { this.jiraAuthenticationContext = jiraAuthenticationContext; }
  
  public CustomFieldHelper()
  {
    if (jiraAuthenticationContext == null) {
      jiraAuthenticationContext = ComponentAccessor.getJiraAuthenticationContext();
    }
  }
  
  public CustomFieldHelper(JiraAuthenticationContext jiraAuthenticationContext, String dateFormat, String importProject) {
    this.jiraAuthenticationContext = jiraAuthenticationContext;
    this.dateFormat = dateFormat;
  }
  
  public MutableIssue populateCustomFields(MutableIssue issueObject, ImportBean caseBean) {
    log.debug("Started populateCustomFields");
    if ((caseBean.getCustomField() == null) || (caseBean.getCustomField().size() == 0)) {
      return issueObject;
    }
    
    if (jiraAuthenticationContext == null) {
      jiraAuthenticationContext = ComponentAccessor.getJiraAuthenticationContext();
    }
    
    Map<String, CustomField> customKeyAndDataType = caseBean.getCustomFieldNamMap();
    
    for (String custFieldName : caseBean.getCustomField().keySet()) {
      try {
        CustomField cusfield = (CustomField)customKeyAndDataType.get(custFieldName);
        String fieldName = cusfield.getName();
        log.debug("Field Name of Custom field: " + fieldName);
        log.debug("cusfield: name " + cusfield.getCustomFieldType().getName());
        
        log.debug("cusfield: key " + cusfield.getCustomFieldType().getKey());
        
        if (caseBean.getCustomField().containsKey(fieldName)) {
          String customFieldTypeKey = cusfield.getCustomFieldType().getKey();
          if (cusfield.getCustomFieldType().getKey().lastIndexOf(':') != -1) {
            ??? = cusfield.getCustomFieldType().getKey().lastIndexOf(':') + 1;
            customFieldTypeKey = customFieldTypeKey.substring(???);
          }
          log.debug("customFieldTypeKey " + customFieldTypeKey);
          switch (customFieldTypeKey) {
          case "datepicker": 
            if (StringUtils.isNotBlank((CharSequence)caseBean.getCustomField().get(fieldName))) {
              issueObject = setDatePicker((String)caseBean.getCustomField().get(fieldName), cusfield, issueObject, dateFormat);
            } else {
              issueObject.setCustomFieldValue(cusfield, null);
            }
            break;
          case "datetime": 
            if (StringUtils.isNotBlank((CharSequence)caseBean.getCustomField().get(fieldName))) {
              issueObject = setDateTimePicker((String)caseBean.getCustomField().get(fieldName), cusfield, issueObject, dateFormat);
            } else {
              issueObject.setCustomFieldValue(cusfield, null);
            }
            break;
          case "float": 
            if (StringUtils.isNotBlank((CharSequence)caseBean.getCustomField().get(fieldName))) {
              issueObject = setNumberField((String)caseBean.getCustomField().get(fieldName), cusfield, issueObject);
            } else {
              issueObject.setCustomFieldValue(cusfield, null);
            }
            break;
          case "textfield": 
            if (StringUtils.isNotBlank((CharSequence)caseBean.getCustomField().get(fieldName))) {
              issueObject = setTextFieldSingleLine((String)caseBean.getCustomField().get(fieldName), cusfield, issueObject);
            } else {
              issueObject.setCustomFieldValue(cusfield, null);
            }
            break;
          case "textarea": 
            if (StringUtils.isNotBlank((CharSequence)caseBean.getCustomField().get(fieldName))) {
              issueObject = setTextFieldMultiLine((String)caseBean.getCustomField().get(fieldName), cusfield, issueObject);
            } else {
              issueObject.setCustomFieldValue(cusfield, null);
            }
            break;
          case "multicheckboxes": 
            if (StringUtils.isNotBlank((CharSequence)caseBean.getCustomField().get(fieldName))) {
              issueObject = setCheckboxes((String)caseBean.getCustomField().get(fieldName), cusfield, issueObject);
            } else {
              issueObject.setCustomFieldValue(cusfield, null);
            }
            break;
          case "radiobuttons": 
            if (StringUtils.isNotBlank((CharSequence)caseBean.getCustomField().get(fieldName))) {
              issueObject = setRadioButtons((String)caseBean.getCustomField().get(fieldName), cusfield, issueObject);
            } else {
              issueObject.setCustomFieldValue(cusfield, null);
            }
            break;
          case "select": 
            if (StringUtils.isNotBlank((CharSequence)caseBean.getCustomField().get(fieldName))) {
              issueObject = setSelectListSingleChoice((String)caseBean.getCustomField().get(fieldName), cusfield, issueObject);
            } else {
              issueObject.setCustomFieldValue(cusfield, null);
            }
            break;
          case "multiselect": 
            if (StringUtils.isNotBlank((CharSequence)caseBean.getCustomField().get(fieldName))) {
              issueObject = setSelectListMultipleChoice((String)caseBean.getCustomField().get(fieldName), cusfield, issueObject);
            } else {
              issueObject.setCustomFieldValue(cusfield, null);
            }
            break;
          case "cascadingselect": 
            if (StringUtils.isNotBlank((CharSequence)caseBean.getCustomField().get(fieldName))) {
              issueObject = setSelectListCascading((String)caseBean.getCustomField().get(fieldName), cusfield, issueObject);
            } else {
              issueObject.setCustomFieldValue(cusfield, null);
            }
            break;
          case "url": 
            if (StringUtils.isNotBlank((CharSequence)caseBean.getCustomField().get(fieldName))) {
              issueObject = setURL((String)caseBean.getCustomField().get(fieldName), cusfield, issueObject);
            } else {
              issueObject.setCustomFieldValue(cusfield, null);
            }
            break;
          case "userpicker": 
            if (StringUtils.isNotBlank((CharSequence)caseBean.getCustomField().get(fieldName))) {
              issueObject = setUserPickerSingleUser((String)caseBean.getCustomField().get(fieldName), cusfield, issueObject);
            } else {
              issueObject.setCustomFieldValue(cusfield, null);
            }
            break;
          case "multiuserpicker": 
            if (StringUtils.isNotBlank((CharSequence)caseBean.getCustomField().get(fieldName))) {
              issueObject = setUserPickerMultipleUser((String)caseBean.getCustomField().get(fieldName), cusfield, issueObject);
            } else {
              issueObject.setCustomFieldValue(cusfield, null);
            }
            break;
          case "version": 
            if (StringUtils.isNotBlank((CharSequence)caseBean.getCustomField().get(fieldName))) {
              issueObject = setVersionPickerSingleVersion((String)caseBean.getCustomField().get(fieldName), cusfield, issueObject);
            } else {
              issueObject.setCustomFieldValue(cusfield, null);
            }
            break;
          case "multiversion": 
            if (StringUtils.isNotBlank((CharSequence)caseBean.getCustomField().get(fieldName))) {
              issueObject = setVersionPickerMultipleVersion((String)caseBean.getCustomField().get(fieldName), cusfield, issueObject);
            } else {
              issueObject.setCustomFieldValue(cusfield, null);
            }
            break;
          case "project": 
            if (StringUtils.isNotBlank((CharSequence)caseBean.getCustomField().get(fieldName))) {
              issueObject = setProjectPickerSingleProject((String)caseBean.getCustomField().get(fieldName), cusfield, issueObject);
            } else {
              issueObject.setCustomFieldValue(cusfield, null);
            }
            break;
          case "readonlyfield": 
            if (StringUtils.isNotBlank((CharSequence)caseBean.getCustomField().get(fieldName))) {
              issueObject = setTextFielReadOnly((String)caseBean.getCustomField().get(fieldName), cusfield, issueObject);
            } else {
              issueObject.setCustomFieldValue(cusfield, null);
            }
            break;
          case "grouppicker": 
            if (StringUtils.isNotBlank((CharSequence)caseBean.getCustomField().get(fieldName))) {
              issueObject = setGroupPickerSingleGroup((String)caseBean.getCustomField().get(fieldName), cusfield, issueObject);
            } else {
              issueObject.setCustomFieldValue(cusfield, null);
            }
            break;
          case "multigrouppicker": 
            if (StringUtils.isNotBlank((CharSequence)caseBean.getCustomField().get(fieldName))) {
              issueObject = setGroupPickerMultipleGroup((String)caseBean.getCustomField().get(fieldName), cusfield, issueObject);
            } else {
              issueObject.setCustomFieldValue(cusfield, null);
            }
            break;
          case "sprint": 
            if (StringUtils.isNotBlank((CharSequence)caseBean.getCustomField().get(fieldName))) {
              Sprint sprint = Sprint.builder().id(Long.valueOf((String)caseBean.getCustomField().get(fieldName))).build();
              if (sprint == null) {
                sprint = Sprint.builder().name((String)caseBean.getCustomField().get(fieldName)).build();
              }
              if (sprint != null) {
                List<Sprint> sprints = new ArrayList();
                sprints.add(sprint);
                issueObject.setCustomFieldValue(cusfield, sprints);
              }
            } else {
              issueObject.setCustomFieldValue(cusfield, null);
            }
            








            break;
          }
          
        }
      }
      catch (Exception e)
      {
        log.debug("Error when setting values to Issue Object" + e.getMessage());
      }
    }
    log.debug("Have set the all the values in the issue object successfully!");
    return issueObject;
  }
  
  public MutableIssue setLabels(ImportBean caseBean, MutableIssue issueObject) {
    if ((caseBean.getCustomField() == null) || (caseBean.getCustomField().size() == 0)) {
      return issueObject;
    }
    
    log.debug("About to set the Label value in issue Object ");
    if (jiraAuthenticationContext == null) {
      jiraAuthenticationContext = ComponentAccessor.getJiraAuthenticationContext();
    }
    ApplicationUser user = jiraAuthenticationContext.getLoggedInUser();
    LabelManager labelManager = (LabelManager)ComponentAccessor.getComponent(LabelManager.class);
    try {
      Set<String> importBeanCFNames = caseBean.getCustomField().keySet();
      for (String fieldName : importBeanCFNames) {
        CustomField cusfield = (CustomField)caseBean.getCustomFieldNamMap().get(fieldName);
        boolean cfContains = cusfield != null;
        
        log.debug("field : " + fieldName + "; importBeanCFNames.contains(fieldName): " + cfContains);
        if ((cfContains) && 
          (cusfield.getCustomFieldType().getName().equalsIgnoreCase("Labels"))) {
          String label = (String)caseBean.getCustomField().get(fieldName);
          if (StringUtils.isNotBlank(label)) {
            label = label.startsWith("[") ? label.substring(1, label.length() - 1) : label;
          }
          Set<String> stringSet = Sets.newHashSet();
          StringTokenizer st = new StringTokenizer(label, ",");
          while (st.hasMoreTokens()) {
            stringSet.add(st.nextToken().trim());
          }
          labelManager.setLabels(user, issueObject.getId(), cusfield.getIdAsLong(), stringSet, false, false);
        }
      }
    }
    catch (Exception e) {
      log.debug("Error when setting values to Issue Object" + e.getMessage());
    }
    log.debug("Have set the Label value in the issue object successfully!");
    return issueObject;
  }
  
  public String getDateFormat() { return dateFormat; }
  
  public void setDateFormat(String dateFormat) {
    this.dateFormat = dateFormat;
  }
}
