package com.go2group.synapse.constant;

import com.go2group.synapse.enums.TestRunStatusEnum;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;










































public final class PluginConstant
{
  public static final String DATA_SEPARATOR = "~~VV~~";
  public static final String TEST_SUITE_SEPARATOR = "/";
  private static List<String> cycleStatuses;
  private static Map<String, String> cycleActionStatusMap;
  private static Map<String, String> supportedAppTypesMap;
  private static List<String> cycleActions;
  public static final String REQUIREMENT_PANEL = "requirement-panel";
  public static final String TEST_PLAN_MEMBER_PANEL = "test-plan-member-panel";
  public static final String REQUIREMENT_MEMBER_SERVICE = "requirementService";
  public static final String TEST_PLAN_MEMBER_SERVICE = "testPlanMemberService";
  public static final String TC_TO_REQUIREMENT_LINK_SERVICE = "testCaseToRequirementLinkService";
  public static final String TPMEM_REQ_FILTER_SERVICE = "testPlanMemberRequirementFilterService";
  public static final String CYCLE_STATUS_DRAFT = "Draft";
  public static final String CYCLE_STATUS_ACTIVE = "Active";
  public static final String CYCLE_STATUS_COMPLETED = "Completed";
  public static final String CYCLE_STATUS_ABORTED = "Aborted";
  public static final String CYCLE_ACTION_START = "Start";
  public static final String CYCLE_ACTION_ABORT = "Abort";
  public static final String CYCLE_ACTION_COMPLETE = "Complete";
  public static final String CYCLE_ACTION_RESUME = "Resume";
  public static final String LOZ_SUCCESS = "aui-lozenge-success";
  public static final String LOZ_ERROR = "aui-lozenge-error";
  public static final String LOZ_CURRENT = "aui-lozenge-current";
  public static final String LOZ_COMPLETE = "aui-lozenge-complete";
  public static final String LOZ_MOVED = "aui-lozenge-moved";
  public static final String RUN_HISTORY_ACTIVITY_TYPE_STATUS = "Status";
  public static final String RUN_HISTORY_ACTIVITY_TYPE_ISSUE = "Issue";
  public static final String RUN_HISTORY_ACTIVITY_ISSUE_ATTACHED = "Attached";
  public static final String RUN_HISTORY_ACTIVITY_ISSUE_DETACHED = "Detached";
  public static final String RUN_HISTORY_ACTIVITY_NEW_TEST_RUN = "Test Run";
  public static final String SYN_AGENT = "SYN_AGENT";
  public static final String SYN_AGENT_009 = "SYN_AGENT_009";
  public static final String SYN_AGENT_006 = "SYN_AGENT_006";
  public static final String SYANPSE_AGENT_RUN_ID = "synRunId";
  public static final String ISSUE_KEY = "issueKey";
  public static final String SYANPSE_AGENT_STEP_ID = "synStepId";
  public static final String SYANPSE_AGENT_REQ_KEY = "synReqKey";
  public static final String SYANPSE_AGENT_TEST_SUITE_ID = "parentTsId";
  public static final String SYANPSE_AGENT_2_VALUE = "create_link";
  public static final String CATEGORY_CYCLE = "cycle";
  public static final String CATEGORY_TEST_RUN = "testRun";
  public static final String REQUIREMENT_PARENT = "parent";
  public static final String ISSUE_ID = "IssueId";
  public static final String REQUIREMENT_CHILD = "child";
  public static final String IMPORT_ISSUE_TYPE_REQUIREMENT = "Requirement";
  public static final String IMPORT_ISSUE_TYPE_TESTCASE = "Testcase";
  public static final String UNDEFINED = "undefined";
  public static final String DEFAULT_SUITE_NAME = "default";
  public static final String SUCCESS = "success";
  public static final String PLUGIN_WORKABLE = "true";
  public static final String PLUGIN_READONLY = "false";
  public static final String REQUIREMENT_ICON_URL = "/download/resources/com.go2group.jira.plugin.synapse/synapse/images/icon-requirement.png";
  public static final String TEST_CASE_ICON_URL = "/download/resources/com.go2group.jira.plugin.synapse/synapse/images/icon-testcase.png";
  public static final String TEST_PLAN_ICON_URL = "/download/resources/com.go2group.jira.plugin.synapse/synapse/images/icon-testplan.png";
  public static final String BUG_ICON_URL = "/download/resources/com.go2group.jira.plugin.synapse/synapse/images/icon-bug.png";
  public static final String IMPORT_TC_STATUS = "com.go2group.synapse.importtc.status:";
  public static final String MOVE_OR_COPY_KEY = "moveorcopy:";
  public static final String IMPORT_TC_MESSAGE = "com.go2group.synapse.importtc.message";
  public static final String ISSUE_KEY_HEADER = "ISSUE KEY";
  public static final String SUMMARY_HEADER = "SUMMARY";
  public static final String DESCRIPTION_HEADER = "DESCRIPTION";
  public static final String STEP_HEADER = "STEP#";
  public static final String EXPECTED_RESULT_HEADER = "EXPECTED RESULT#";
  public static final String STEP = "STEP";
  public static final String EXPECTED_RESULT = "EXPECTED RESULT";
  public static final String STEP_ID = "STEP ID";
  public static final String TEST_STEP = "Test Step";
  public static final String TEST_PLAN = "Test Plan";
  public static final String TEST_SUITE = "Test Suite";
  public static final String TEST_CASE_FIELD = "Test Case";
  public static final String ESTIMATE = "Estimate(mins)";
  public static final String FORECAST = "Forecast(mins)";
  public static final String CHILD_REQUIREMENTS = "Child Requirements";
  public static final String PARENT_REQUIREMENT = "Parent Requirement";
  public static final String REQUIREMENT_SUITE_FIELD = "Requirement Suite";
  public static final String IMPORT_AFFECT_VERSION = "Affects Version/s";
  public static final String IMPORT_ASSIGNEE = "Assignee";
  public static final String IMPORT_COMPONENT = "Component/s";
  public static final String IMPORT_DESCRIPTION = "Description";
  public static final String IMPORT_DUE_DATE = "Due Date";
  public static final String IMPORT_ENVIRONMENT = "Environment";
  public static final String IMPORT_FIX_VERSION = "Fix Version/s";
  public static final String IMPORT_KEY = "Key";
  public static final String IMPORT_LABELS = "Labels";
  public static final String IMPORT_PRIORITY = "Priority";
  public static final String IMPORT_REPORTER = "Reporter";
  public static final String IMPORT_SUMMARY = "Summary";
  public static final String IMPORT_SUMMARY_CAPITAL = "SUMMARY";
  public static final String IMPORT_KEY_CAPITAL = "KEY";
  public static final String IMPORT_ORIGINAL_ESTIMATE = "Original Estimate (in seconds)";
  public static final String IMPORT_STEP_ID = "StepID";
  public static final String IMPORT_STEP = "Step";
  public static final String IMPORT_STEP_ID_CAPITAL = "STEP ID";
  public static final String IMPORT_STEP_CAPITAL = "STEP";
  public static final String IMPORT_EXPECTED_RESULT = "ExpectedResult";
  public static final String IMPORT_ISSUE_TYPE = "Synapse Issue Type";
  public static final String IMPORT_ISSUE_LINK = "SynapseIssueLink";
  public static final String IMPORT_TEST_DATA = "TestData";
  public static final String IMPORT_REQUIREMENT_ID = "Requirement";
  public static final String BASELINE_ID = "Baseline";
  public static final String IMPORT_TEST_SUITE_ID = "TestSuite";
  public static final String IMPORT_ESTIMATION = "Estimate";
  public static final String PARENT_KEY = "ParentKey";
  public static final String CHILD_KEY = "ChildKey";
  public static final String TEST_CASE = "TestCase";
  public static final String REQUIREMENT_SUITE = "RequirementSuite";
  public static final String IMPORT_SYNAPSE_ISSUE_TYPE = "Synapse Issue Type";
  public static final String IMPORT_TEST_STEP = "TestStep";
  public static final String IMPORT_TEST_REFERENCE = "TestReference";
  public static final String TEST_PLAN_TREE = "test-plan";
  public static final String TEST_CYCLE_TREE = "test-cycle";
  public static final String TEST_SUITE_TREE = "test-suite";
  public static final String REQUIREMENT_TREE = "requirement";
  public static final String ADVANCED_TEST_CYCLE_TREE = "advanced-test-cycle";
  public static final String ADVANCED_TEST_CYCLE_ALL_TEST_RUNS = "synapse.web.cycle.allruns.name";
  public static final String TREE_TYPE_GROUPED_LEAVES = "grouped-leaves";
  public static final String TREE_TYPE_BRANCH_N_LEAVES = "branch-n-leaves";
  public static final String ALL_PROJECTS = "-1";
  public static final String DEFAULT_SELECT_OPTION = "-1";
  public static final String REST_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
  public static final String PRETTY_DATETIME_FORMAT = "dd-MMM-yyyy HH:mm:ss";
  public static final String REST_DATE_FORMAT = "yyyy-MM-dd";
  public static final String CHART_DATE_FORMAT = "yyyy-MM-dd";
  public static final String CHART_DATE_FORMAT_JS = "YYYY-MM-DD";
  public static final Long SYNAPSE_ATTACHMENT_FILE_LIMIT = Long.valueOf(10485760L);
  
  public static final String ITEM_TEST_SUITE = "testSuite";
  
  public static final String ITEM_TEST_CASES = "testCases";
  
  public static final String ACTION_MOVE = "move";
  
  public static final String ACTION_COPY = "copy";
  
  public static final String ACTION_CLONE = "clone";
  
  public static final String TEST_CASE_CLONE_KEY = "TEST_CASE_CLONE_KEY";
  
  public static final int PAGE_ALL = 0;
  
  public static final int PAGE_SIZE_100 = 100;
  
  public static final int PAGE_SIZE_500 = 500;
  
  public static final int PAGE_SIZE_1000 = 1000;
  
  public static final String INTEGRATION_APP_TYPE_BAMBOO = "Bamboo";
  
  public static final String INTEGRATION_APP_TYPE_JENKINS = "Jenkins";
  
  public static final String INTEGRATION_APP_TYPE_JENKINS_LOWER_VERSION = "Jenkins2";
  
  public static final String TEST_SUITE_MAIN_PAGE = "TEST_SUITE_MAIN_PAGE";
  
  public static final String TEST_SUITE_TREE_PAGE = "TEST_SUITE_TREE_PAGE";
  
  public static final String TEST_SUITE_TREE_PAGE_MENU_MOVE_COPY = "TEST_SUITE_TREE_PAGE_MENU_MOVE_COPY";
  
  public static final String TEST_SUITE_TREE_PAGE_MENU_CLONE = "TEST_SUITE_TREE_PAGE_MENU_CLONE";
  
  public static final String TEST_SUITE_TEST_CASE_PAGE = "TEST_SUITE_TEST_CASE_PAGE";
  
  public static final String CLONE_REQUIREMENT = "REQUIREMET";
  
  public static final String CLONE_TEST_CASE = "TEST_CASE";
  
  public static final String CLONE_TEST_PLAN = "TEST_PLAN";
  
  public static final String CLONE_REQ_OPTION_CHILD = "childRequirement";
  
  public static final String CLONE_REQ_OPTION_TEST_CASES = "testCases";
  
  public static final String CLONE_TEST_CASE_OPTION_REQ = "requirements";
  
  public static final String CLONE_TEST_CASE_OPTION_SUITE = "testSuites";
  
  public static final String CLONE_TEST_PLAN_OPTION_CYCLES = "cycles";
  
  public static final String CLONE_TEST_PLAN_OPTION_TESTERS = "testers";
  
  public static final String REQUIREMENT_TREE_CONTEXT = "requirement-context";
  
  public static final String MODULE_REQUIREMENT = "requirement-module";
  
  public static final String MODULE_TRACEABILITY = "traceability-module";
  
  public static final String MODULE_TEST_PLAN_SUMMARY_UNRESOLVED = "test-plan-summary-unresolved-module";
  public static final String MODULE_TEST_PLAN_REPORT = "test-plan-report-module";
  public static final String MODULE_TEST_CYCLE_REPORT = "test-cycle-report-module";
  public static final String MODULE_REQ_COVERAGE_REPORT = "req-coverage-report-module";
  public static final String MODULE_ADHOC_RUN_REPORT = "adhoc-run-report-module";
  public static final String MODULE_REQ_BASED_REPORT = "req-based-report-module";
  public static final String MODULE_TEST_EXECUTION_BASED_REPORT = "test-execution-based-report-module";
  public static final String MODULE_TEST_EXEC_DATE_WISE_REPORT = "test-exec-date-wise-report-module";
  public static final String MODULE_RUN_ATTRIBUTE_BASED_REPORT = "run-attribute-based-report-module";
  public static final String CATEGORY_REPORT = "report";
  public static final String CATEGORY_FILTER = "filter";
  public static final String RUN_SOURCE_TEST_CYCLE_TABLE = "test-cycle-table";
  public static final String RUN_SOURCE_TEST_CYCLE_TREE = "test-cycle-tree";
  public static final String TREE_TEST_SUITE_PREFIX = "synapse-tree-ts-";
  public static final String TREE_TEST_PLAN_PREFIX = "synapse-tree-tp-";
  public static final String TREE_TEST_CYCLE_PREFIX = "synapse-tree-tcy-";
  public static final String TREE_TEST_REQUIREMENT_PREFIX = "synapse-tree-req-";
  public static final String CRON_SCHEDULE_DAILY = "daily";
  public static final String CRON_SCHEDULE_WEEKLY = "weekly";
  public static final String CRON_SCHEDULE_MONTHLY = "monthly";
  public static final String CRON_SCHEDULE_CRON = "cron";
  public static final String JOB_STATUS_IN_PROGRESS = "in-progress";
  public static final String TEST_CASE_MODIFIED = "test-case-modified";
  public static final String SYN_TOO_MANY_RECS = "SYN_TOO_MANY_RECS";
  public static final String COLUMN_CONFIG_CATEGORY_WEB_PAGE = "web-page";
  public static final String COLUMN_CONFIG_MODULE_TEST_CYCLE = "test-cycle";
  public static final String COLUMN_CONFIG_MODULE_TEST_CYCLE_ALL_RUNS = "test-cycle-all-runs";
  public static final String COLUMN_CONFIG_MODULE_TEST_SUITE = "test-suite";
  public static final String TEST_PARAM_CONTAINER_START = "<<";
  public static final String TEST_PARAM_CONTAINER_END = ">>";
  public static final String TEST_PARAM_COLOR_START = "{color:blue}";
  public static final String TEST_PARAM_COLOR_END = "{color}";
  public static final int TEST_STEP_ELIPSIS_LENGTH = 50;
  public static final String TEST_SUITE_HIERARCHY_LOOP_FOUND = "TEST_SUITE_HIERARCHY_LOOP_FOUND";
  public static final String REQUIREMENT_LINK_SOURCE_CLONE = "clone";
  public static final String REQUIREMENT_LINK_SOURCE_IMPORT = "import";
  public static final String REQUIREMENT_LINK_SOURCE_TEST_CASE_LINK = "test-case-link";
  public static final String REQUIREMENT_LINK_SOURCE_ADD_TEST_CASE = "add-test-case";
  public static final String TEST_CYCLE_REPORT_TESTER = "Tester";
  public static final String TEST_CYCLE_REPORT_TESTED_BY = "TestedBy";
  public static final String TEST_CYCLE_REPORT_EXECUTED_ON = "ExecutedOn";
  public static final String TEST_CYCLE_REPORT_EFFORT = "Effort";
  public static final String TEST_CYCLE_REPORT_DEFECTS = "Defects";
  public static final String TEST_CYCLE_REPORT_ATTACHMENTS = "Attachments";
  public static final String TEST_CYCLE_REPORT_COMMENTS = "Comments";
  public static final String TEST_CYCLE_REPORT_TEST_TYPE = "TestType";
  public static final String RUN_ATTRIBUTE_REPORT_TEST_SUITE = "TestSuite";
  public static final String EXPORT_TESTSUITE_KEY = "exporttestsuite:";
  public static final String IMPORT_SYNAPSE_FIELD_STEP_ID = "synapse.import.label.synapsefield.StepID";
  public static final String IMPORT_SYNAPSE_FIELD_STEP = "synapse.import.label.synapsefield.Step";
  public static final String IMPORT_SYNAPSE_FIELD_STEP_ID_CAPITAL = "STEP ID";
  public static final String IMPORT_SYNAPSE_FIELD_STEP_CAPITAL = "STEP";
  public static final String IMPORT_SYNAPSE_FIELD_EXPECTED_RESULT = "synapse.import.label.synapsefield.ExpectedResult";
  public static final String IMPORT_SYNAPSE_FIELD_SYNAPSE_ISSUE_TYPE = "Synapse Issue Type";
  public static final String IMPORT_SYNAPSE_FIELD_TEST_DATA = "synapse.import.label.synapsefield.TestData";
  public static final String IMPORT_SYNAPSE_FIELD_REQUIREMENT_ID = "synapse.import.label.synapsefield.Requirement";
  public static final String IMPORT_SYNAPSE_FIELD_TEST_SUITE_ID = "synapse.import.label.synapsefield.TestSuite";
  public static final String IMPORT_SYNAPSE_FIELD_ESTIMATION = "synapse.import.label.synapsefield.Estimate";
  public static final String IMPORT_SYNAPSE_FIELD_TEST_STEP = "synapse.import.label.synapsefield.TestStep";
  public static final String IMPORT_SYNAPSE_FIELD_TEST_REFERENCE = "synapse.import.label.synapsefield.TestReference";
  public static final String IMPORT_SYNAPSE_FIELD_ISSUE_LINK = "synapse.import.label.synapsefield.SynapseIssueLink";
  public static final String SYNAPSE_FIELD_ISSUE_ID = "synapse.import.label.synapsefield.IssueId";
  public static final String SYNAPSE_FIELD_PARENT_KEY = "synapse.import.label.synapsefield.ParentKey";
  public static final String SYNAPSE_FIELD_CHILD_KEY = "synapse.import.label.synapsefield.ChildKey";
  public static final String SYNAPSE_FIELD_TEST_CASE = "synapse.import.label.synapsefield.TestCase";
  public static final String SYNAPSE_FIELD_REQUIREMENT_SUITE = "synapse.import.label.synapsefield.RequirementSuite";
  public static final long DELETED_TESTCASE = -1L;
  
  private PluginConstant() {}
  
  private static void loadCycleStatuses()
  {
    cycleStatuses = new ArrayList();
    
    cycleStatuses.add("Aborted");
    cycleStatuses.add("Active");
    cycleStatuses.add("Completed");
    cycleStatuses.add("Draft");
  }
  
  private static void loadCycleActionStatusMap() {
    cycleActionStatusMap = new HashMap();
    
    cycleActionStatusMap.put("Start", "Active");
    cycleActionStatusMap.put("Abort", "Aborted");
    cycleActionStatusMap.put("Complete", "Completed");
    cycleActionStatusMap.put("Resume", "Active");
  }
  
  private static void loadCycleActions() {
    cycleActions = new ArrayList();
    
    cycleActions.add("Start");
    cycleActions.add("Abort");
    cycleActions.add("Complete");
    cycleActions.add("Resume");
  }
  
  private static void loadSupportedAppTypes() {
    supportedAppTypesMap = new LinkedHashMap();
    
    supportedAppTypesMap.put("Bamboo", "synapse.config.integration.app.bamboo");
    supportedAppTypesMap.put("Jenkins", "synapse.config.integration.app.jenkins");
    supportedAppTypesMap.put("Jenkins2", "synapse.config.integration.app.jenkinslowerversion");
  }
  
  public static final Map<String, String> getSupportedAppTypes() {
    if (supportedAppTypesMap == null) {
      loadSupportedAppTypes();
    }
    
    return supportedAppTypesMap;
  }
  
  public static final List<String> getCycleStatuses() {
    if (cycleStatuses == null) {
      loadCycleStatuses();
    }
    return cycleStatuses;
  }
  
  public static final Map<String, String> getCycleActionStatusMap() {
    if (cycleActionStatusMap == null) {
      loadCycleActionStatusMap();
    }
    return cycleActionStatusMap;
  }
  
  public static final List<Integer> getRunStatuses() {
    List<TestRunStatusEnum> statuses = TestRunStatusEnum.valuesList();
    if ((statuses != null) && (statuses.size() > 0)) {
      List<Integer> runStatuses = new ArrayList();
      for (TestRunStatusEnum statusEnum : statuses) {
        runStatuses.add(statusEnum.getId());
      }
      return runStatuses;
    }
    return null;
  }
  
  public static final List<String> getCycleActions() {
    if (cycleActions == null) {
      loadCycleActions();
    }
    
    return cycleActions;
  }
}
