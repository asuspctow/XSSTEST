package com.go2group.synapse.issue.views;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.datetime.DateTimeStyle;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.util.AggregateTimeTrackingBean;
import com.atlassian.jira.issue.util.AggregateTimeTrackingCalculator;
import com.atlassian.jira.issue.util.AggregateTimeTrackingCalculatorFactory;
import com.atlassian.jira.issue.views.util.IssueViewUtil;
import com.atlassian.jira.issue.views.util.RssViewUtils;
import com.atlassian.jira.plugin.customfield.CustomFieldTypeModuleDescriptor;
import com.atlassian.jira.plugin.issueview.AbstractIssueView;
import com.atlassian.jira.plugin.issueview.IssueViewFieldParams;
import com.atlassian.jira.plugin.issueview.IssueViewModuleDescriptor;
import com.atlassian.jira.plugin.issueview.IssueViewRequestParams;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.JiraVelocityUtils;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.util.velocity.DefaultVelocityRequestContextFactory;
import com.atlassian.jira.util.velocity.RequestContextParameterHolder;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.web.bean.CustomIssueXMLViewFieldsBean;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.go2group.synapse.bean.AutomationStepOuputBean;
import com.go2group.synapse.bean.ConfigMapOutputBean;
import com.go2group.synapse.bean.TestCaseDetailsOutputBean;
import com.go2group.synapse.bean.TestPlanDisplayBean;
import com.go2group.synapse.bean.TestStepOutputBean;
import com.go2group.synapse.bean.TestSuiteOutputBean;
import com.go2group.synapse.config.SynapseConfig;
import com.go2group.synapse.core.exception.InvalidDataException;
import com.go2group.synapse.helper.TestPlanDisplayHelper;
import com.go2group.synapse.service.ConfigService;
import com.go2group.synapse.service.TestCaseToRequirementLinkService;
import com.go2group.synapse.service.TestPlanMemberService;
import com.go2group.synapse.service.TestRunService;
import com.go2group.synapse.service.TestStepService;
import com.go2group.synapse.service.TestSuiteService;
import com.go2group.synapse.service.runattribute.RunAttributeService;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;



public class SynapseIssueXMLView
  extends AbstractIssueView
{
  private static final Logger log = Logger.getLogger(SynapseIssueXMLView.class);
  
  private static final String RSS_MODE_RENDERED = "rendered";
  
  private static final String RSS_MODE_RAW = "raw";
  
  private final JiraAuthenticationContext authenticationContext;
  
  private final ApplicationProperties applicationProperties;
  
  private final FieldLayoutManager fieldLayoutManager;
  
  private final CommentManager commentManager;
  
  private final IssueViewUtil issueViewUtil;
  
  private final AggregateTimeTrackingCalculatorFactory aggregateTimeTrackingCalculatorFactory;
  
  private final DateTimeFormatterFactory dateTimeFormatterFactory;
  private final BuildUtilsInfo buildUtilsInfo;
  private final FieldVisibilityManager fieldVisibilityManager;
  private final TestStepService testStepService;
  private final TestCaseToRequirementLinkService tcrLinkService;
  private final TestSuiteService testSuiteService;
  private final TestPlanMemberService tpMemberService;
  private final UserManager userManager;
  private final TestRunService tRunService;
  private final ConfigService configService;
  private final SynapseConfig synapseConfig;
  private final RunAttributeService runAttributeService;
  
  public SynapseIssueXMLView(JiraAuthenticationContext authenticationContext, @ComponentImport ApplicationProperties applicationProperties, FieldLayoutManager fieldLayoutManager, CommentManager commentManager, IssueViewUtil issueViewUtil, AggregateTimeTrackingCalculatorFactory aggregateTimeTrackingCalculatorFactory, BuildUtilsInfo buildUtilsInfo, DateTimeFormatterFactory dateTimeFormatterFactory, FieldVisibilityManager fieldVisibilityManager, TestStepService testStepService, TestCaseToRequirementLinkService tcrLinkService, TestSuiteService testSuiteService, TestRunService tRunService, TestPlanMemberService tpMemberService, @ComponentImport UserManager userManager, ConfigService configService, SynapseConfig synapseConfig, RunAttributeService runAttributeService)
  {
    this.authenticationContext = authenticationContext;
    this.applicationProperties = applicationProperties;
    this.fieldLayoutManager = fieldLayoutManager;
    this.commentManager = commentManager;
    this.issueViewUtil = issueViewUtil;
    this.aggregateTimeTrackingCalculatorFactory = aggregateTimeTrackingCalculatorFactory;
    this.dateTimeFormatterFactory = dateTimeFormatterFactory;
    this.fieldVisibilityManager = fieldVisibilityManager;
    this.buildUtilsInfo = ((BuildUtilsInfo)Assertions.notNull("buildUtilsInfo", buildUtilsInfo));
    this.testStepService = testStepService;
    this.tcrLinkService = tcrLinkService;
    this.testSuiteService = testSuiteService;
    this.tRunService = tRunService;
    this.userManager = userManager;
    this.tpMemberService = tpMemberService;
    this.configService = configService;
    this.synapseConfig = synapseConfig;
    this.runAttributeService = runAttributeService;
  }
  
  public String getContent(Issue issue, IssueViewRequestParams issueViewRequestParams) {
    String header = getHeader(issueViewRequestParams);
    String body = getBody(issue, issueViewRequestParams);
    String footer = getFooter();
    return header + body + footer;
  }
  
  private String getFooter()
  {
    return descriptor.getHtml("footer", Collections.emptyMap());
  }
  
  private String getHeader(IssueViewRequestParams issueViewRequestParams)
  {
    Map<String, Object> headerParams = JiraVelocityUtils.getDefaultVelocityParams(authenticationContext);
    headerParams.put("title", applicationProperties.getString("jira.title"));
    headerParams.put("buildInfo", buildUtilsInfo.getBuildInformation());
    headerParams.put("currentDate", new Date());
    headerParams.put("rssLocale", RssViewUtils.getRssLocale(authenticationContext.getLocale()));
    headerParams.put("version", buildUtilsInfo.getVersion());
    headerParams.put("buildNumber", buildUtilsInfo.getCurrentBuildNumber());
    headerParams.put("buildDate", new SimpleDateFormat("dd-MM-yyyy").format(buildUtilsInfo.getCurrentBuildDate()));
    headerParams.put("customViewRequested", 
      Boolean.valueOf(issueViewRequestParams.getIssueViewFieldParams().isCustomViewRequested()));
    

    VelocityRequestContext velocityRequestContext = new DefaultVelocityRequestContextFactory(applicationProperties).getJiraVelocityRequestContext();
    RequestContextParameterHolder requestParameters = velocityRequestContext.getRequestParameters();
    if (requestParameters != null) {
      String requestURL = requestParameters.getRequestURL();
      if (requestURL != null) {
        String queryString = StringEscapeUtils.escapeXml(requestParameters.getQueryString());
        

        if (queryString != null) {
          headerParams.put("exampleURLPrefix", requestURL + "?" + queryString + "&amp;");
        } else {
          headerParams.put("exampleURLPrefix", requestURL + "?");
        }
      }
    }
    
    return descriptor.getHtml("header", headerParams);
  }
  
  public String getBody(Issue issue, IssueViewRequestParams issueViewRequestParams) {
    ConfigMapOutputBean configMapBean = configService.getConfigMapping("synapse.config.testdata");
    boolean showStepData = (configMapBean != null) && (configMapBean.getValue().equals("true"));
    List<TestStepOutputBean> testSteps = null;
    List<AutomationStepOuputBean> automationSteps = null;
    List<Issue> reqIssues = null;
    List<TestSuiteOutputBean> suiteIssues = null;
    Map<Integer, TestSuiteOutputBean> memberIdRootMap = null;
    List<TestPlanDisplayBean> tppDisplayBeans = null;
    Map<String, Object> bodyParams = JiraVelocityUtils.getDefaultVelocityParams(authenticationContext);
    try {
      TestCaseDetailsOutputBean tcDetails = testStepService.getTestCaseDetails(issue);
      if (tcDetails != null) {
        testSteps = tcDetails.getTestSteps();
        automationSteps = tcDetails.getAutomationSteps();
      }
      reqIssues = tcrLinkService.getRequirements(issue, ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser());
      suiteIssues = testSuiteService.getTestSuitesInTestCase(issue.getId());
      
      for (TestSuiteOutputBean tcSuite : suiteIssues)
      {
        Integer rootSuiteId = testSuiteService.getRootTestSuiteForMemberSuite(tcSuite.getID()).getID();
        tcSuite.setTestSuiteHierarchy(getTestSuiteHierarchy(tcSuite));
        
        if ((rootSuiteId != null) && (!rootSuiteId.equals(tcSuite.getID()))) {
          memberIdRootMap = new HashMap();
          TestSuiteOutputBean rootSuite = testSuiteService.getTestSuite(rootSuiteId);
          memberIdRootMap.put(tcSuite.getID(), rootSuite);
        }
      }
      log.debug("beans " + testSteps);
      if (testSteps != null) {
        log.debug("beans " + testSteps.size());
      }
      
      TestPlanDisplayHelper tpDisplayHelper = new TestPlanDisplayHelper(userManager, tRunService, tpMemberService, ComponentAccessor.getIssueManager(), runAttributeService);
      
      tppDisplayBeans = tpDisplayHelper.getTestPlanPanelDisplayBeans(issue, authenticationContext.getLoggedInUser());
      
      if (tcDetails.getEstimate() != null) {
        bodyParams.put("estimation", Long.valueOf(tcDetails.getEstimate().longValue() / 60L));
      } else {
        bodyParams.put("estimation", tcDetails.getEstimate());
      }
      
      if (tcDetails.getForecast() != null) {
        bodyParams.put("forecast", Long.valueOf(tcDetails.getForecast().longValue() / 60L));
      } else {
        bodyParams.put("forecast", tcDetails.getForecast());
      }
    }
    catch (InvalidDataException e)
    {
      e.printStackTrace();
    }
    
    bodyParams.put("issue", issue);
    bodyParams.put("i18n", authenticationContext.getI18nHelper());
    bodyParams.put("dateTimeFormatter", dateTimeFormatterFactory
      .formatter().forLoggedInUser().withStyle(DateTimeStyle.RSS_RFC822_DATE_TIME));
    bodyParams.put("dateFormatter", dateTimeFormatterFactory
      .formatter().withSystemZone().withStyle(DateTimeStyle.RSS_RFC822_DATE_TIME));
    bodyParams.put("testSteps", testSteps);
    bodyParams.put("automationSteps", automationSteps);
    bodyParams.put("reqIssues", reqIssues);
    bodyParams.put("suiteIssues", suiteIssues);
    bodyParams.put("rootMap", memberIdRootMap);
    bodyParams.put("tppDisplayBeans", tppDisplayBeans);
    bodyParams.put("showStepData", Boolean.valueOf(showStepData));
    bodyParams.put("ttEnabled", Boolean.valueOf(synapseConfig.isTimeTrackingEnabled()));
    

    CustomIssueXMLViewFieldsBean customIssueXmlViewFieldsBean = new CustomIssueXMLViewFieldsBean(fieldVisibilityManager, issueViewRequestParams.getIssueViewFieldParams(), issue.getProjectId(), issue.getIssueTypeId());
    
    bodyParams.put("issueXmlViewFields", customIssueXmlViewFieldsBean);
    


    Object velocityRequestContextFactory = new DefaultVelocityRequestContextFactory(applicationProperties);
    
    VelocityRequestContext velocityRequestContext = ((VelocityRequestContextFactory)velocityRequestContextFactory).getJiraVelocityRequestContext();
    String rssMode = velocityRequestContext.getRequestParameter("rssMode");
    
    if ((StringUtils.isNotEmpty(rssMode)) && ("raw".equals(rssMode))) {
      bodyParams.put("rssMode", "raw");
    } else {
      if (StringUtils.isNotEmpty(rssMode)) {
        log.warn("Invalid rssMode parameter specified '" + rssMode + "'.  Currently only supports '" + "raw" + "'");
      }
      
      bodyParams.put("rssMode", "rendered");
    }
    bodyParams.put("votingEnabled", Boolean.valueOf(applicationProperties.getOption("jira.option.voting")));
    bodyParams.put("watchingEnabled", Boolean.valueOf(applicationProperties.getOption("jira.option.watching")));
    bodyParams.put("xmlView", this);
    ApplicationUser user = authenticationContext.getUser();
    bodyParams.put("remoteUser", user);
    
    bodyParams.put("linkingEnabled", Boolean.valueOf(applicationProperties.getOption("jira.option.issuelinking")));
    if (customIssueXmlViewFieldsBean.isFieldRequestedAndVisible("issuelinks")) {
      bodyParams.put("linkCollection", issueViewUtil.getLinkCollection(issue, user));
    }
    
    if (customIssueXmlViewFieldsBean.isFieldRequestedAndVisible("comment")) {
      List comments = commentManager.getCommentsForUser(issue, user);
      
      if (applicationProperties.getDefaultBackedString("jira.issue.actions.order").equals("desc")) {
        Collections.reverse(comments);
      }
      bodyParams.put("comments", comments);
    }
    
    boolean timeTrackingEnabled = applicationProperties.getOption("jira.option.timetracking");
    boolean subTasksEnabled = applicationProperties.getOption("jira.option.allowsubtasks");
    bodyParams.put("timeTrackingEnabled", Boolean.valueOf(timeTrackingEnabled));
    if ((timeTrackingEnabled) && (subTasksEnabled) && (!issue.isSubTask()))
    {
      AggregateTimeTrackingBean bean = aggregateTimeTrackingCalculatorFactory.getCalculator(issue).getAggregates(issue);
      if (bean.getSubTaskCount() > 0) {
        bodyParams.put("aggregateTimeTrackingBean", bean);
      }
    }
    
    List customFields = getVisibleCustomFields(issue, user, issueViewRequestParams.getIssueViewFieldParams());
    bodyParams.put("visibleCustomFields", customFields);
    
    return descriptor.getHtml("view", bodyParams);
  }
  
  private String getTestSuiteHierarchy(TestSuiteOutputBean tsuiteBean) throws InvalidDataException
  {
    String testSuiteHierarchy = testSuiteService.getRootPathName(tsuiteBean);
    
    log.debug("testSuiteHierarchy : " + testSuiteHierarchy);
    return testSuiteHierarchy;
  }
  
  public String getRenderedContent(String fieldName, String value, Issue issue) {
    return issueViewUtil.getRenderedContent(fieldName, value, issue);
  }
  
  public String getPrettyDuration(Long v) {
    return issueViewUtil.getPrettyDuration(v);
  }
  
  public List<FieldLayoutItem> getVisibleCustomFields(Issue issue, ApplicationUser user, IssueViewFieldParams issueViewFieldParams)
  {
    String issueTypeId = issue.getIssueTypeId();
    FieldLayout fieldLayout = fieldLayoutManager.getFieldLayout(issue);
    List<FieldLayoutItem> customFields = fieldLayout.getVisibleCustomFieldLayoutItems(issue.getProjectObject(), 
      EasyList.build(issueTypeId));
    
    List<FieldLayoutItem> result = customFields;
    if ((issueViewFieldParams != null) && 
      (!issueViewFieldParams.isAllCustomFields()) && 
      (issueViewFieldParams.isCustomViewRequested()))
    {
      List<FieldLayoutItem> requestedCustomFields = new ArrayList(customFields.size());
      
      for (FieldLayoutItem customField : customFields)
      {
        if (issueViewFieldParams.getCustomFieldIds().contains(customField.getOrderableField().getId())) {
          requestedCustomFields.add(customField);
        }
      }
      result = requestedCustomFields;
    }
    


    result = new ArrayList(result);
    Collections.sort(result);
    return result;
  }
  
  public String getCustomFieldXML(CustomField field, Issue issue) {
    FieldLayoutItem fieldLayoutItem = fieldLayoutManager.getFieldLayout(issue).getFieldLayoutItem(field);
    


    CustomFieldTypeModuleDescriptor moduleDescriptor = field.getCustomFieldType().getDescriptor();
    if (moduleDescriptor.isXMLTemplateExists()) {
      String xmlValue = moduleDescriptor.getViewXML(field, issue, fieldLayoutItem, false);
      

      if (xmlValue != null) {
        return xmlValue;
      }
      log.info("No XML data has been defined for the customfield [" + field.getId() + "]");
    }
    
    return "";
  }
}
