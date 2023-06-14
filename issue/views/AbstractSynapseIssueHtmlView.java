package com.go2group.synapse.issue.views;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.LookAndFeelBean;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.renderer.JiraRendererPlugin;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderer;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.fields.util.FieldPredicates;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.operation.IssueOperations;
import com.atlassian.jira.issue.util.AggregateTimeTrackingBean;
import com.atlassian.jira.issue.views.util.IssueViewUtil;
import com.atlassian.jira.issue.views.util.SearchRequestViewUtils;
import com.atlassian.jira.plugin.issueview.AbstractIssueView;
import com.atlassian.jira.plugin.issueview.IssueViewModuleDescriptor;
import com.atlassian.jira.plugin.issueview.IssueViewRequestParams;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.JiraUrlCodec;
import com.atlassian.jira.util.JiraVelocityUtils;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.go2group.synapse.bean.AutomationStepOuputBean;
import com.go2group.synapse.bean.ConfigMapOutputBean;
import com.go2group.synapse.bean.CustomDisplayPrefBean;
import com.go2group.synapse.bean.RequirementChapterBean;
import com.go2group.synapse.bean.RequirementCoverageDisplayBean;
import com.go2group.synapse.bean.TestCaseDetailsOutputBean;
import com.go2group.synapse.bean.TestCycleSummaryDisplayBean;
import com.go2group.synapse.bean.TestPlanDisplayBean;
import com.go2group.synapse.bean.TestPlanMemberOutputBean;
import com.go2group.synapse.bean.TestStepDisplayBean;
import com.go2group.synapse.bean.TestStepOutputBean;
import com.go2group.synapse.bean.TestSuiteOutputBean;
import com.go2group.synapse.bean.TimeTrackBean;
import com.go2group.synapse.config.SynapseConfig;
import com.go2group.synapse.core.common.bean.StatusLozenge;
import com.go2group.synapse.core.exception.InvalidDataException;
import com.go2group.synapse.core.tree.bean.TreeFilterItem;
import com.go2group.synapse.enums.PreferencesModuleEnum;
import com.go2group.synapse.enums.RequirementsFieldNameEnum;
import com.go2group.synapse.enums.TestRunStatusEnum;
import com.go2group.synapse.enums.TestRunTypeEnum;
import com.go2group.synapse.helper.TestPlanDisplayHelper;
import com.go2group.synapse.service.ConfigService;
import com.go2group.synapse.service.RequirementService;
import com.go2group.synapse.service.TestCaseToRequirementLinkService;
import com.go2group.synapse.service.TestCycleService;
import com.go2group.synapse.service.TestPlanMemberService;
import com.go2group.synapse.service.TestPlanService;
import com.go2group.synapse.service.TestRunRequirementService;
import com.go2group.synapse.service.TestRunService;
import com.go2group.synapse.service.TestStepService;
import com.go2group.synapse.service.TestSuiteService;
import com.go2group.synapse.service.runattribute.RunAttributeService;
import com.go2group.synapserm.bean.ReqSuiteMemberOutputBean;
import com.go2group.synapserm.bean.ReqSuiteOutputBean;
import com.go2group.synapserm.bean.tree.impl.DefaultRequirementSuiteTree;
import com.go2group.synapserm.bean.tree.impl.RequirementTreePool;
import com.go2group.synapserm.service.RequirementSuiteService;
import com.go2group.synapserm.util.PluginUtil;
import com.openhtmltopdf.DOMBuilder;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.opensymphony.util.TextUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import webwork.action.Action;





public abstract class AbstractSynapseIssueHtmlView
  extends AbstractIssueView
{
  private static final Logger log = Logger.getLogger(AbstractSynapseIssueHtmlView.class);
  
  protected final JiraAuthenticationContext authenticationContext;
  
  protected final ApplicationProperties applicationProperties;
  
  protected final CommentManager commentManager;
  
  protected final FieldScreenRendererFactory fieldScreenRendererFactory;
  
  protected final IssueViewUtil issueViewUtil;
  
  private final FieldVisibilityManager fieldVisibilityManager;
  
  private final TestStepService testStepService;
  
  private final TestCaseToRequirementLinkService tcrLinkService;
  
  private final TestSuiteService testSuiteService;
  private final TestPlanMemberService tpMemberService;
  private final UserManager userManager;
  private final TestRunService tRunService;
  private final ConfigService configService;
  private final RequirementService requirementService;
  private final RequirementSuiteService requirementSuiteService;
  private final SynapseConfig synapseConfig;
  private final TestPlanService testPlanService;
  private final TestCycleService testCycleService;
  private final TestPlanMemberService testPlanMemberService;
  private final TestRunRequirementService testRunRequirementService;
  private RequirementTreePool treePool;
  private final RunAttributeService runAttributeService;
  
  public AbstractSynapseIssueHtmlView(JiraAuthenticationContext authenticationContext, @ComponentImport ApplicationProperties applicationProperties, CommentManager commentManager, FieldScreenRendererFactory fieldScreenRendererFactory, IssueViewUtil issueViewUtil, FieldVisibilityManager fieldVisibilityManager, TestStepService testStepService, TestCaseToRequirementLinkService tcrLinkService, TestSuiteService testSuiteService, TestRunService tRunService, TestPlanMemberService tpMemberService, @ComponentImport UserManager userManager, ConfigService configService, RequirementService requirementService, SynapseConfig synapseConfig, TestPlanService testPlanService, TestCycleService testCycleService, TestPlanMemberService testPlanMemberService, RequirementTreePool requirementTreePool, RequirementSuiteService requirementSuiteService, TestRunRequirementService testRunRequirementService, RunAttributeService runAttributeService)
  {
    this.authenticationContext = authenticationContext;
    this.applicationProperties = applicationProperties;
    this.commentManager = commentManager;
    this.fieldScreenRendererFactory = fieldScreenRendererFactory;
    this.issueViewUtil = issueViewUtil;
    this.fieldVisibilityManager = fieldVisibilityManager;
    this.testStepService = testStepService;
    this.tcrLinkService = tcrLinkService;
    this.testSuiteService = testSuiteService;
    this.tRunService = tRunService;
    this.userManager = userManager;
    this.tpMemberService = tpMemberService;
    this.configService = configService;
    this.requirementService = requirementService;
    this.synapseConfig = synapseConfig;
    this.testPlanService = testPlanService;
    this.testCycleService = testCycleService;
    this.testPlanMemberService = testPlanMemberService;
    treePool = requirementTreePool;
    this.requirementSuiteService = requirementSuiteService;
    this.testRunRequirementService = testRunRequirementService;
    this.runAttributeService = runAttributeService;
  }
  
  public String getContent(Issue issue, IssueViewRequestParams issueViewRequestParams)
  {
    return getHeader(issue) + getBody(issue, issueViewRequestParams) + getFooter(issue);
  }
  
  public String getBody(Issue issue, IssueViewRequestParams issueViewFieldParams)
  {
    Map<String, Object> bodyParams = null;
    if (synapseConfig.getIssueTypeIds("Test Case").contains(issue.getIssueType().getId())) {
      bodyParams = getTestCaseBody(issue, issueViewFieldParams, null, false);
    } else if (synapseConfig.getIssueTypeIds("Test Plan").contains(issue.getIssueType().getId())) {
      bodyParams = getTestPlanBody(issue, issueViewFieldParams);
    } else {
      boolean isNumberingEnabled = false;
      String userKey = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser().getKey();
      Map<String, List<CustomDisplayPrefBean>> moduleToFieldPrefMap = synapseConfig.getPreference(userKey);
      if ((moduleToFieldPrefMap != null) && (!moduleToFieldPrefMap.containsKey(PreferencesModuleEnum.REQUIREMENTS_PAGE.getName()))) {
        moduleToFieldPrefMap = synapseConfig.getPreference("Application");
      }
      if (moduleToFieldPrefMap != null) {
        List<CustomDisplayPrefBean> fields = (List)moduleToFieldPrefMap.get(PreferencesModuleEnum.REQUIREMENTS_PAGE.getName());
        if (fields != null) {
          if (!fields.isEmpty()) {
            for (CustomDisplayPrefBean prefBean : fields) {
              if (RequirementsFieldNameEnum.NUMBERING.getName().equalsIgnoreCase(prefBean.getField())) {
                isNumberingEnabled = true;
              }
            }
          }
        } else {
          isNumberingEnabled = true;
        }
      } else {
        isNumberingEnabled = true;
      }
      bodyParams = getRequirementBody(issue, issueViewFieldParams, "1", null, null, isNumberingEnabled);
    }
    String html = descriptor.getHtml("view", bodyParams);
    if (descriptor.getFileExtension().equalsIgnoreCase("pdf")) {
      try {
        File pdfFile = File.createTempFile(issue.getKey(), ".pdf");
        OutputStream outputStream = new FileOutputStream(pdfFile);
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.withW3cDocument(DOMBuilder.jsoup2DOM(Jsoup.parse(html)), "");
        builder.toStream(outputStream);
        builder.testMode(true);
        builder.run();
        html = new String(Files.readAllBytes(pdfFile.toPath()));
      } catch (Exception e) {
        log.debug(e.getMessage(), e);
        log.error(e.getMessage());
      }
    }
    
    return html;
  }
  
  private Map<String, Object> getTestPlanBody(Issue issue, IssueViewRequestParams issueViewFieldParams) {
    Map<String, Object> bodyParams = JiraVelocityUtils.getDefaultVelocityParams(authenticationContext);
    bodyParams.put("issue", issue);
    bodyParams.put("wordView", this);
    ApplicationUser user = authenticationContext.getLoggedInUser();
    boolean timeTrackingEnabled = applicationProperties.getOption("jira.option.timetracking");
    boolean subTasksEnabled = applicationProperties.getOption("jira.option.allowsubtasks");
    

    try
    {
      List<RequirementCoverageDisplayBean> requirementCoverageBeans = testPlanService.getRequirementCoverage(issue.getId());
      bodyParams.put("requirements", requirementCoverageBeans);
      
      List<TestCycleSummaryDisplayBean> cycleSummaryBeans = testCycleService.getTestCycleSummaryList(issue.getId(), true);
      bodyParams.put("cycleSummaryList", cycleSummaryBeans);
      
      List<TestPlanMemberOutputBean> testPlanMembers = testPlanMemberService.getTestPlanMembersComplete(issue, authenticationContext.getLoggedInUser());
      bodyParams.put("members", testPlanMembers);
      
      bodyParams.put("ttEnabled", Boolean.valueOf(synapseConfig.isTimeTrackingEnabled()));
      
      if (synapseConfig.isTimeTrackingEnabled()) {
        TimeTrackBean timeTrackBean = testPlanService.getTimeTrackInfo(issue.getId());
        if (timeTrackBean.getEstimate() != null) {
          bodyParams.put("estimation", timeTrackBean.getEstimateDisplayLabel());
        }
        
        if (timeTrackBean.getForecast() != null) {
          bodyParams.put("forecast", timeTrackBean.getForecastDisplayLabel());
        }
        if (timeTrackBean.getForecast() != null) {
          bodyParams.put("effort", timeTrackBean.getEffortDisplayLabel());
        }
      }
    } catch (InvalidDataException e) {
      log.debug(e.getMessage());
    }
    
    bodyParams.put("issue", issue);
    
    bodyParams.put("i18n", authenticationContext.getI18nHelper());
    bodyParams.put("outlookdate", authenticationContext.getOutlookDate());
    bodyParams.put("fieldVisibility", fieldVisibilityManager);
    bodyParams.put("timeTrackingEnabled", Boolean.valueOf(timeTrackingEnabled));
    bodyParams.put("linkingEnabled", Boolean.valueOf(applicationProperties.getOption("jira.option.issuelinking")));
    bodyParams.put("subtasksEnabled", Boolean.valueOf(subTasksEnabled));
    bodyParams.put("linkCollection", issueViewUtil.getLinkCollection(issue, user));
    bodyParams.put("votingEnabled", Boolean.valueOf(applicationProperties.getOption("jira.option.voting")));
    bodyParams.put("wordView", this);
    bodyParams.put("remoteUser", user);
    bodyParams.put("stringUtils", new StringUtils());
    bodyParams.put("encoder", new JiraUrlCodec());
    
    if ((timeTrackingEnabled) && (subTasksEnabled) && (!issue.isSubTask())) {
      AggregateTimeTrackingBean bean = issueViewUtil.createAggregateBean(issue);
      if (bean.getSubTaskCount() > 0) {
        bodyParams.put("aggregateTimeTrackingBean", issueViewUtil
          .createTimeTrackingBean(bean, authenticationContext.getI18nHelper()));
      }
    }
    
    List<Comment> comments = commentManager.getCommentsForUser(issue, user);
    if (applicationProperties.getDefaultBackedString("jira.issue.actions.order").equals("desc")) {
      Collections.reverse(comments);
    }
    bodyParams.put("comments", comments);
    return bodyParams;
  }
  
  public Map<String, Object> getRequirementBody(Issue issue, IssueViewRequestParams issueViewFieldParams, String level, List<TreeFilterItem> filters, List<Long> jqlIssues, boolean isNumberingEnabled) {
    ApplicationUser user = authenticationContext.getLoggedInUser();
    boolean timeTrackingEnabled = applicationProperties.getOption("jira.option.timetracking");
    boolean subTasksEnabled = applicationProperties.getOption("jira.option.allowsubtasks");
    Map<String, Object> bodyParams = JiraVelocityUtils.getDefaultVelocityParams(authenticationContext);
    ConfigMapOutputBean configMapBean = configService.getConfigMapping("synapse.config.testdata");
    boolean showStepData = (configMapBean != null) && (configMapBean.getValue().equals("true"));
    RequirementChapterBean chapterBean = getChildRequirement(null, issue, 0, level, filters, jqlIssues, isNumberingEnabled);
    
    try
    {
      Issue parentRequirement = requirementService.getParent(issue.getId());
      if (parentRequirement != null) {
        bodyParams.put("parentRequirement", parentRequirement.getKey());
      }
      String reqSuite = getRequirementSuite(issue);
      bodyParams.put("requirementSuite", reqSuite);
    } catch (InvalidDataException e) {
      log.debug(e.getMessage());
    }
    
    RendererManager rendererManager = ComponentAccessor.getRendererManager();
    JiraRendererPlugin renderer = rendererManager.getRendererForType("atlassian-wiki-renderer");
    bodyParams.put("textutils", new TextUtils());
    bodyParams.put("renderer", renderer);
    bodyParams.put("issue", issue);
    bodyParams.put("chapterBean", chapterBean);
    bodyParams.put("wordView", this);
    bodyParams.put("showStepData", Boolean.valueOf(showStepData));
    String userPreference = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser().getKey();
    Map<String, List<CustomDisplayPrefBean>> moduleToFieldPrefMap = synapseConfig.getPreference(userPreference);
    if (moduleToFieldPrefMap != null) {
      List<CustomDisplayPrefBean> prefFields = (List)moduleToFieldPrefMap.get("RequirementWord");
      if ((prefFields != null) && (prefFields.size() > 0)) {
        List<String> fields = new ArrayList();
        for (CustomDisplayPrefBean field : prefFields) {
          fields.add(field.getField());
        }
        if (filters != null) {
          fields.add("Child Requirements".replace(" ", ""));
        }
        if (fields.size() > 0) {
          bodyParams.put("preferenceFields", fields);
        }
      }
    }
    bodyParams.put("i18n", authenticationContext.getI18nHelper());
    bodyParams.put("outlookdate", authenticationContext.getOutlookDate());
    bodyParams.put("fieldVisibility", fieldVisibilityManager);
    bodyParams.put("timeTrackingEnabled", Boolean.valueOf(timeTrackingEnabled));
    bodyParams.put("linkingEnabled", Boolean.valueOf(applicationProperties.getOption("jira.option.issuelinking")));
    bodyParams.put("subtasksEnabled", Boolean.valueOf(subTasksEnabled));
    bodyParams.put("linkCollection", issueViewUtil.getLinkCollection(issue, user));
    bodyParams.put("votingEnabled", Boolean.valueOf(applicationProperties.getOption("jira.option.voting")));
    bodyParams.put("wordView", this);
    bodyParams.put("remoteUser", user);
    bodyParams.put("stringUtils", new StringUtils());
    bodyParams.put("encoder", new JiraUrlCodec());
    bodyParams.put("showStepData", Boolean.valueOf(showStepData));
    bodyParams.put("ttEnabled", Boolean.valueOf(synapseConfig.isTimeTrackingEnabled()));
    
    if ((timeTrackingEnabled) && (subTasksEnabled) && (!issue.isSubTask())) {
      AggregateTimeTrackingBean bean = issueViewUtil.createAggregateBean(issue);
      if (bean.getSubTaskCount() > 0) {
        bodyParams.put("aggregateTimeTrackingBean", issueViewUtil
          .createTimeTrackingBean(bean, authenticationContext.getI18nHelper()));
      }
    }
    
    List<Comment> comments = commentManager.getCommentsForUser(issue, user);
    if (applicationProperties.getDefaultBackedString("jira.issue.actions.order").equals("desc")) {
      Collections.reverse(comments);
    }
    bodyParams.put("comments", comments);
    return bodyParams;
  }
  
  public FieldScreenRenderer getFieldScreenRenderer(Issue issue) {
    FieldScreenRenderer fieldScreenRenderer = fieldScreenRendererFactory.getFieldScreenRenderer(issue, IssueOperations.VIEW_ISSUE_OPERATION, FieldPredicates.isCustomField());
    return fieldScreenRenderer;
  }
  
  private RequirementChapterBean getChildRequirement(RequirementChapterBean parentBean, Issue issue, int counter, String level, List<TreeFilterItem> filters, List<Long> jqlIssues, boolean isNumberingEnabled) {
    ApplicationUser user = authenticationContext.getLoggedInUser();
    RequirementChapterBean chapterBean = new RequirementChapterBean();
    chapterBean.setIssue(issue);
    
    List<Issue> childRequirements = null;
    List<RequirementChapterBean> childIssues = new ArrayList();
    Map<Long, List<TestStepDisplayBean>> tcAndTsMap = new HashMap();
    try {
      childRequirements = requirementService.getChildren(issue.getId());
      log.debug("Child requirement : " + childRequirements);
      if (childRequirements != null) {
        int tempCounter = 1;
        for (Issue childRequirement : childRequirements) {
          if (jqlIssues == null) {
            if (PluginUtil.isFilterPassed(childRequirement, filters)) {
              RequirementChapterBean childChapterBean = getChildRequirement(chapterBean, childRequirement, tempCounter, level, filters, null, isNumberingEnabled);
              childIssues.add(childChapterBean);
              tempCounter++;
            } else {
              List<Issue> children = requirementService.getChildren(childRequirement.getId(), filters, null);
              if ((children != null) && (children.size() > 0)) {
                RequirementChapterBean childChapterBean = getChildRequirement(chapterBean, childRequirement, tempCounter, level, filters, null, isNumberingEnabled);
                childIssues.add(childChapterBean);
                tempCounter++;
              }
            }
          }
          else if (jqlIssues.contains(childRequirement.getId())) {
            RequirementChapterBean childChapterBean = getChildRequirement(chapterBean, childRequirement, tempCounter, level, filters, jqlIssues, isNumberingEnabled);
            childIssues.add(childChapterBean);
            tempCounter++;
          } else {
            children = requirementService.getChildren(childRequirement.getId(), filters, jqlIssues);
            if ((children != null) && (children.size() > 0)) {
              RequirementChapterBean childChapterBean = getChildRequirement(chapterBean, childRequirement, tempCounter, level, filters, jqlIssues, isNumberingEnabled);
              childIssues.add(childChapterBean);
              tempCounter++;
            }
          }
        }
        
        if (isNumberingEnabled) {
          DefaultRequirementSuiteTree modelTree = null;
          if (treePool == null) {
            treePool = ((RequirementTreePool)ComponentAccessor.getOSGiComponentInstanceOfType(RequirementTreePool.class));
          }
          if (treePool != null) {
            modelTree = treePool.getRequirementTree(issue.getProjectId());
          }
          log.info("Requirement tree for the project id " + issue.getProjectId() + " is " + modelTree);
          if (modelTree != null) {
            String itemNumber = PluginUtil.deriveRequirementNumber(modelTree, "node", String.valueOf(issue.getId()));
            if (log.isDebugEnabled()) {
              log.debug("Item number for Issue " + issue.getKey() + " is " + itemNumber);
            }
            if (StringUtils.isNotBlank(itemNumber)) {
              chapterBean.setLevel(itemNumber);
            }
          }
        }
      }
      else if (isNumberingEnabled) {
        DefaultRequirementSuiteTree modelTree = null;
        if (treePool == null) {
          treePool = ((RequirementTreePool)ComponentAccessor.getOSGiComponentInstanceOfType(RequirementTreePool.class));
        }
        if (treePool != null) {
          modelTree = treePool.getRequirementTree(issue.getProjectId());
        }
        log.info("Requirement tree for the project id " + issue.getProjectId() + " is " + modelTree);
        if (modelTree != null) {
          String itemNumber = PluginUtil.deriveRequirementNumber(modelTree, "leaf", String.valueOf(issue.getId()));
          if (log.isDebugEnabled()) {
            log.debug("Item number for Issue " + issue.getKey() + " is " + itemNumber);
          }
          if (StringUtils.isNotBlank(itemNumber)) {
            chapterBean.setLevel(itemNumber);
          }
        }
      }
      
      chapterBean.setChildIssues(childIssues);
      List<String> reqKeys = new ArrayList();
      reqKeys.add(issue.getKey());
      Object tcasesId = tcrLinkService.getTestCasesInRequirement(reqKeys);
      List<Issue> tcases = new ArrayList();
      
      for (List<Issue> children = ((Set)tcasesId).iterator(); children.hasNext();) { long tcId = ((Long)children.next()).longValue();
        Issue tcIdIssue = ComponentAccessor.getIssueManager().getIssueObject(Long.valueOf(tcId));
        tcases.add(tcIdIssue);
        RendererManager rendererManager = ComponentAccessor.getRendererManager();
        JiraRendererPlugin renderer = rendererManager.getRendererForType("atlassian-wiki-renderer");
        
        TestCaseDetailsOutputBean testCaseDetailsBean = testStepService.getTestCaseDetails(tcIdIssue);
        if ((testCaseDetailsBean != null) && (testCaseDetailsBean.getTestSteps() != null)) {
          List<TestStepOutputBean> beans = testCaseDetailsBean.getTestSteps();
          List<TestStepDisplayBean> displayBeans = new ArrayList();
          for (TestStepOutputBean stepBean : beans) {
            TestStepDisplayBean displayBean = new TestStepDisplayBean(stepBean);
            displayBeans.add(displayBean);
          }
          tcAndTsMap.put(Long.valueOf(tcId), displayBeans);
        }
      }
      chapterBean.setTcIssues(tcases);
      chapterBean.setTcAndTsMap(tcAndTsMap);
    } catch (InvalidDataException e) {
      log.debug(e.getMessage());
    }
    List<Comment> comments = commentManager.getCommentsForUser(issue, user);
    if (applicationProperties.getDefaultBackedString("jira.issue.actions.order").equals("desc")) {
      Collections.reverse(comments);
    }
    chapterBean.setComments(comments);
    
    return chapterBean;
  }
  
  private Map<String, Object> getTestCaseBody(Issue issue, IssueViewRequestParams issueViewFieldParams, Integer referenceId, boolean isExportAllTestSuites) {
    ApplicationUser user = authenticationContext.getLoggedInUser();
    boolean timeTrackingEnabled = applicationProperties.getOption("jira.option.timetracking");
    boolean subTasksEnabled = applicationProperties.getOption("jira.option.allowsubtasks");
    Map<String, Object> bodyParams = JiraVelocityUtils.getDefaultVelocityParams(authenticationContext);
    List<TestStepOutputBean> testSteps = null;
    List<AutomationStepOuputBean> automationSteps = null;
    List<Issue> reqIssues = null;
    List<TestSuiteOutputBean> suiteIssues = null;
    List<TestSuiteOutputBean> selectedSuiteIssues = new ArrayList();
    Map<Integer, TestSuiteOutputBean> memberIdRootMap = null;
    List<TestPlanDisplayBean> tppDisplayBeans = null;
    ConfigMapOutputBean configMapBean = configService.getConfigMapping("synapse.config.testdata");
    boolean showStepData = (configMapBean != null) && (configMapBean.getValue().equals("true"));
    List<TestStepDisplayBean> displayBeans = new ArrayList();
    try
    {
      TestCaseDetailsOutputBean tcDetails = testStepService.getTestCaseDetails(issue);
      RendererManager rendererManager;
      TestStepDisplayBean displayBean; if (tcDetails != null) {
        if (TestRunTypeEnum.MANUAL.equals(tcDetails.getType())) {
          testSteps = tcDetails.getTestSteps();
          rendererManager = ComponentAccessor.getRendererManager();
          JiraRendererPlugin renderer = rendererManager.getRendererForType("atlassian-wiki-renderer");
          bodyParams.put("renderer", renderer);
          if (testSteps != null)
          {
            for (TestStepOutputBean stepBean : testSteps) {
              displayBean = new TestStepDisplayBean(stepBean);
              displayBeans.add(displayBean);
            }
          }
        }
        else {
          automationSteps = tcDetails.getAutomationSteps();
        }
      }
      reqIssues = tcrLinkService.getRequirements(issue, user);
      suiteIssues = testSuiteService.getTestSuitesInTestCase(issue.getId());
      

      for (TestSuiteOutputBean tcSuite : suiteIssues)
      {
        Integer rootSuiteId = testSuiteService.getRootTestSuiteForMemberSuite(tcSuite.getID()).getID();
        if (isExportAllTestSuites) {
          tcSuite.setTestSuiteHierarchy(getTestSuiteHierarchy(tcSuite));
          selectedSuiteIssues.add(tcSuite);
        }
        else if (tcSuite.getID().equals(referenceId)) {
          tcSuite.setTestSuiteHierarchy(getTestSuiteHierarchy(tcSuite));
          selectedSuiteIssues.add(tcSuite);
        }
        

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
      
      String userPreference = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser().getKey();
      Map<String, List<CustomDisplayPrefBean>> moduleToFieldPrefMap = synapseConfig.getPreference(userPreference);
      if (moduleToFieldPrefMap != null) {
        Object prefFields = (List)moduleToFieldPrefMap.get("TestCaseWord");
        if ((prefFields != null) && (((List)prefFields).size() > 0)) {
          List<String> fields = new ArrayList();
          for (CustomDisplayPrefBean field : (List)prefFields) {
            fields.add(field.getField());
          }
          if (fields.size() > 0) {
            bodyParams.put("preferenceFields", fields);
          }
        }
      }
      TestPlanDisplayHelper tpDisplayHelper = new TestPlanDisplayHelper(tRunService, tpMemberService, testRunRequirementService, userManager, runAttributeService);
      
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
    catch (InvalidDataException e) {
      log.debug(e.getMessage(), e);
    }
    
    bodyParams.put("issue", issue);
    
    bodyParams.put("i18n", authenticationContext.getI18nHelper());
    bodyParams.put("outlookdate", authenticationContext.getOutlookDate());
    bodyParams.put("fieldVisibility", fieldVisibilityManager);
    bodyParams.put("timeTrackingEnabled", Boolean.valueOf(timeTrackingEnabled));
    bodyParams.put("linkingEnabled", Boolean.valueOf(applicationProperties.getOption("jira.option.issuelinking")));
    bodyParams.put("subtasksEnabled", Boolean.valueOf(subTasksEnabled));
    bodyParams.put("linkCollection", issueViewUtil.getLinkCollection(issue, user));
    bodyParams.put("votingEnabled", Boolean.valueOf(applicationProperties.getOption("jira.option.voting")));
    bodyParams.put("wordView", this);
    bodyParams.put("remoteUser", user);
    bodyParams.put("stringUtils", new StringUtils());
    bodyParams.put("encoder", new JiraUrlCodec());
    bodyParams.put("testSteps", displayBeans);
    bodyParams.put("automationSteps", automationSteps);
    bodyParams.put("reqIssues", reqIssues);
    bodyParams.put("suiteIssues", selectedSuiteIssues);
    bodyParams.put("rootMap", memberIdRootMap);
    bodyParams.put("tppDisplayBeans", tppDisplayBeans);
    bodyParams.put("showStepData", Boolean.valueOf(showStepData));
    bodyParams.put("ttEnabled", Boolean.valueOf(synapseConfig.isTimeTrackingEnabled()));
    
    if ((timeTrackingEnabled) && (subTasksEnabled) && (!issue.isSubTask())) {
      AggregateTimeTrackingBean bean = issueViewUtil.createAggregateBean(issue);
      if (bean.getSubTaskCount() > 0) {
        bodyParams.put("aggregateTimeTrackingBean", issueViewUtil
          .createTimeTrackingBean(bean, authenticationContext.getI18nHelper()));
      }
    }
    
    List<Comment> comments = commentManager.getCommentsForUser(issue, user);
    if (applicationProperties.getDefaultBackedString("jira.issue.actions.order").equals("desc")) {
      Collections.reverse(comments);
    }
    bodyParams.put("comments", comments);
    return bodyParams;
  }
  
  private String getTestSuiteHierarchy(TestSuiteOutputBean tsuiteBean) throws InvalidDataException
  {
    String testSuiteHierarchy = testSuiteService.getRootPathName(tsuiteBean);
    
    log.debug("testSuiteHierarchy : " + testSuiteHierarchy);
    return testSuiteHierarchy;
  }
  
  public String getHeader(Issue issue) {
    return getHeader("[#" + issue.getKey() + "] " + issue.getSummary(), getLinkToPrevious(issue));
  }
  
  protected abstract String getLinkToPrevious(Issue paramIssue);
  
  public String getRunStatusColor(String status) {
    return TestRunStatusEnum.getEnum(status).getLozenge().getColor();
  }
  
  public String getDefectKeys(String rawKeys) {
    if ((rawKeys != null) && (rawKeys.trim().length() > 0)) {
      return rawKeys.substring(1, rawKeys.length() - 1).replaceAll(" ", "");
    }
    return "";
  }
  











  public String getHeader(String title, String linkToPrevious)
  {
    Map<String, Object> bodyParams = JiraVelocityUtils.getDefaultVelocityParams(authenticationContext);
    bodyParams.put("title", title);
    bodyParams.put("contentType", descriptor.getContentType() + "; charset=" + applicationProperties.getEncoding());
    
    LookAndFeelBean lookAndFeelBean = LookAndFeelBean.getInstance(applicationProperties);
    bodyParams.put("linkColour", lookAndFeelBean.getTextLinkColour());
    bodyParams.put("linkAColour", lookAndFeelBean.getTextActiveLinkColour());
    bodyParams.put("showCssLinks", printCssLinks() ? Boolean.TRUE : Boolean.FALSE);
    bodyParams.put("linkToPrevious", linkToPrevious);
    
    bodyParams.put("style", getStyleSheetHtml());
    
    return descriptor.getHtml("header", bodyParams);
  }
  



  protected abstract boolean printCssLinks();
  


  public String getStyleSheetHtml()
  {
    return descriptor.getHtml("style", new HashMap());
  }
  
  public String getFooter(Issue issue) {
    Map<String, Object> footerParams = JiraVelocityUtils.getDefaultVelocityParams(authenticationContext);
    footerParams.put("generatedInfo", SearchRequestViewUtils.getGeneratedInfo(authenticationContext.getLoggedInUser()));
    return descriptor.getHtml("footer", footerParams);
  }
  
  public String getPrettyDuration(Long v) {
    return issueViewUtil.getPrettyDuration(v);
  }
  
  public String getRenderedContent(String fieldName, String value, Issue issue) {
    return issueViewUtil.getRenderedContent(fieldName, value, issue);
  }
  
  public boolean isTestCaseIssue(Issue issue) {
    return synapseConfig.getIssueTypeIds("Test Case").contains(issue.getIssueType().getId());
  }
  
  public boolean isTestPlanIssue(Issue issue) {
    return synapseConfig.getIssueTypeIds("Test Plan").contains(issue.getIssueType().getId());
  }
  




  public String getCustomFieldHtml(FieldLayoutItem fieldLayoutItem, CustomField field, Issue issue)
  {
    Action action = null;
    
    Map<String, Object> displayParams = MapBuilder.newBuilder("textOnly", Boolean.TRUE).toMutableMap();
    
    return field.getViewHtml(fieldLayoutItem, action, issue, displayParams);
  }
  
  private String getRequirementSuite(Issue issue)
  {
    String suite = "";
    try {
      ReqSuiteMemberOutputBean member = requirementSuiteService.getMemberRepresentation(issue.getId());
      if ((member != null) && (member.getReqSuiteOutputBean() != null)) {
        suite = member.getReqSuiteOutputBean() != null ? member.getReqSuiteOutputBean().getName() : "";
      } else {
        Issue parentRequirement = requirementService.getParent(issue.getId());
        if (parentRequirement != null) {
          suite = getRequirementSuite(parentRequirement);
        }
      }
    } catch (InvalidDataException e) {
      log.debug(e.getMessage());
    }
    return suite;
  }
  
  public String getTestCaseBodyContent(Issue issue, IssueViewRequestParams issueViewFieldParams, Integer referenceId, boolean isExportAllTestSuites) {
    Map<String, Object> bodyParams = null;
    if (synapseConfig.getIssueTypeIds("Test Case").contains(issue.getIssueType().getId())) {
      bodyParams = getTestCaseBody(issue, issueViewFieldParams, referenceId, isExportAllTestSuites);
    }
    String html = descriptor.getHtml("view", bodyParams);
    if (descriptor.getFileExtension().equalsIgnoreCase("pdf")) {
      try {
        File pdfFile = File.createTempFile(issue.getKey(), ".pdf");
        OutputStream outputStream = new FileOutputStream(pdfFile);
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.withW3cDocument(DOMBuilder.jsoup2DOM(Jsoup.parse(html)), "");
        builder.toStream(outputStream);
        builder.testMode(true);
        builder.run();
        html = new String(Files.readAllBytes(pdfFile.toPath()));
      } catch (Exception e) {
        log.debug(e.getMessage(), e);
        log.error(e.getMessage());
      }
    }
    
    return html;
  }
}
