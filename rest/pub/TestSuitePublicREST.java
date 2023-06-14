package com.go2group.synapse.rest.pub;

import com.atlassian.annotations.security.XsrfProtectionExcluded;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.go2group.synapse.bean.TestSuiteInputBean;
import com.go2group.synapse.bean.TestSuiteMemberInputBean;
import com.go2group.synapse.bean.TestSuiteMemberOutputBean;
import com.go2group.synapse.bean.TestSuiteMemberRestBean;
import com.go2group.synapse.bean.TestSuiteOutputBean;
import com.go2group.synapse.bean.TestSuiteRootRestBean;
import com.go2group.synapse.constant.SynapsePermission;
import com.go2group.synapse.core.audit.log.ActionEnum;
import com.go2group.synapse.core.audit.log.AuditLogInputBean;
import com.go2group.synapse.core.audit.log.ModuleEnum;
import com.go2group.synapse.core.audit.log.SourceEnum;
import com.go2group.synapse.core.audit.log.service.AuditLogService;
import com.go2group.synapse.core.exception.InvalidDataException;
import com.go2group.synapse.enums.TestSuiteMemberTypeEnum;
import com.go2group.synapse.manager.PermissionUtilAbstract;
import com.go2group.synapse.service.TestStepService;
import com.go2group.synapse.service.TestSuiteService;
import com.go2group.synapse.service.impl.ratelimit.DefaultRestRateLimiter;
import com.go2group.synapse.util.PluginUtil;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;












@Path("public/testSuite")
@Consumes({"application/json"})
@Produces({"application/json"})
public class TestSuitePublicREST
  extends AbstractPublicREST
{
  private static final Logger log = Logger.getLogger(TestSuitePublicREST.class);
  
  private final I18nHelper i18n;
  
  private TestSuiteService testSuiteService;
  
  private IssueManager issueManager;
  
  private TestStepService testStepService;
  
  private final AuditLogService auditLogService;
  

  protected TestSuitePublicREST(@ComponentImport I18nHelper i18n, @ComponentImport IssueManager issueManager, PermissionUtilAbstract permissionUtil, TestSuiteService testSuiteService, TestStepService testStepService, AuditLogService auditLogService)
  {
    super(permissionUtil);
    this.testSuiteService = testSuiteService;
    this.i18n = i18n;
    this.issueManager = issueManager;
    this.testStepService = testStepService;
    this.auditLogService = auditLogService;
  }
  
  @Path("linkTestCase")
  @POST
  @XsrfProtectionExcluded
  public Response linkTestCase(TestSuiteRestBean testSuiteRestBean)
  {
    log.debug("linkTestCase starts..");
    try {
      HttpServletRequest request = ExecutingHttpRequest.get();
      boolean canProceed = DefaultRestRateLimiter.canRequestProceed(request);
      Response localResponse; if (!canProceed) { HttpServletRequest request;
        return rateLimitExceeded("Too many Requests");
      }
      HttpServletRequest request;
      if (!hasValidLicense()) {
        log.debug("Invalid license");
        return forbidden(i18n.getText("servererror.rest.invalid.license"));
      }
      
      Project project = ComponentAccessor.getProjectManager().getProjectByCurrentKey(testSuiteRestBean.getProjectKey());
      
      if (project == null) {
        log.debug("Invalid Project Key");
        HttpServletRequest request; return error(i18n.getText("servererror.rest.invalid.project"));
      }
      

      if (!hasSynapsePermission(project, SynapsePermission.MANAGE_TESTSUITES)) {
        log.debug("User does not have permission to Manage Test Suites");
        HttpServletRequest request; return forbidden(i18n.getText("servererror.rest.no.manage.testsuite.permission"));
      }
      
      List<String> errorMessage = new ArrayList();
      List<String> testCaseKeys = new ArrayList();
      for (String tcKey : testSuiteRestBean.getTestCaseKeys()) {
        Issue testCaseIssue = issueManager.getIssueByCurrentKey(tcKey);
        
        if (testCaseIssue == null) {
          log.debug("Invalid Test Case Key");
          errorMessage.add(i18n.getText("servererror.rest.testcase.notfound", tcKey));
        }
        else {
          try {
            linkTestCaseWithTestSuite(testSuiteRestBean.getTestSuitePath(), testCaseIssue, 
              testSuiteRestBean.getTestSuiteId() + "", project.getId(), true);
            testCaseKeys.add(tcKey);
          } catch (InvalidDataException e) {
            log.debug(e.getMessage(), e);
            log.error(e.getMessage());
            errorMessage.add(e.getMessage());
          }
        }
      }
      
      try
      {
        TestSuiteOutputBean testSuite = testSuiteService.getTestSuite(testSuiteRestBean.getTestSuiteId());
        
        AuditLogInputBean auditLogInputBean = PluginUtil.getAuditLogInputBean(ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser(), project);
        auditLogInputBean.setAction(ActionEnum.ADDED);
        auditLogInputBean.setModule(ModuleEnum.TEST_SUITE);
        auditLogInputBean.setSource(SourceEnum.REST.getName());
        auditLogInputBean.setLogTime(new Date());
        auditLogInputBean.setLog("Added Test Case(s) '" + testCaseKeys + "' to Test Suite '" + testSuite.getName() + "' through REST");
        auditLogService.createAuditLog(auditLogInputBean);
      }
      catch (InvalidDataException e) {
        log.debug(e.getMessage(), e);
        log.error(e.getMessage());
      }
      if (errorMessage.size() > 0) { HttpServletRequest request;
        return error(errorMessage);
      }
      log.debug("linkTestCase ends..");
      HttpServletRequest request; return success();
    } finally {
      HttpServletRequest request = ExecutingHttpRequest.get();
      DefaultRestRateLimiter.endCall(request);
    }
  }
  
  @Path("createTestSuite")
  @POST
  @XsrfProtectionExcluded
  public Response createTestSuite(TestSuiteRestBean testSuiteRestBean) {
    try {
      HttpServletRequest request = ExecutingHttpRequest.get();
      boolean canProceed = DefaultRestRateLimiter.canRequestProceed(request);
      Response localResponse; if (!canProceed) { HttpServletRequest request;
        return rateLimitExceeded("Too many Requests");
      }
      HttpServletRequest request;
      if (!hasValidLicense()) {
        log.debug("Invalid license");
        return forbidden(i18n.getText("servererror.rest.invalid.license"));
      }
      
      Project project = ComponentAccessor.getProjectManager().getProjectByCurrentKey(testSuiteRestBean.getProjectKey());
      
      if (project == null) {
        log.debug("Invalid Project Key");
        HttpServletRequest request; return error(i18n.getText("servererror.rest.invalid.project"));
      }
      

      if (!hasSynapsePermission(project, SynapsePermission.MANAGE_TESTSUITES)) {
        log.debug("User does not have permission to Manage Test Suites");
        HttpServletRequest request; return forbidden(i18n.getText("servererror.rest.no.manage.testsuite.permission"));
      }
      Integer testSuiteId = null;
      try {
        log.debug("Create Test Suite starts...");
        testSuiteId = linkTestCaseWithTestSuite(testSuiteRestBean.getTestSuitePath(), null, null, project.getId(), false);
        log.debug("Create Test Suite ends...");
        

        if (testSuiteId != null) {
          TestSuiteOutputBean testSuite = testSuiteService.getTestSuite(testSuiteId);
          auditLogInputBean = PluginUtil.getAuditLogInputBean(ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser(), project);
          auditLogInputBean.setAction(ActionEnum.CREATED);
          auditLogInputBean.setModule(ModuleEnum.TEST_SUITE);
          auditLogInputBean.setSource(SourceEnum.REST.getName());
          auditLogInputBean.setLogTime(new Date());
          auditLogInputBean.setLog("Created Test Suite '" + testSuite.getName() + "' in Project '" + project.getKey() + "' through REST");
          auditLogService.createAuditLog(auditLogInputBean);
        }
      } catch (InvalidDataException e) {
        AuditLogInputBean auditLogInputBean;
        log.debug(e.getMessage(), e);
        HttpServletRequest request; return error(e);
      }
      
      testSuiteRestBean.setCreatedTestSuiteId(testSuiteId);
      HttpServletRequest request; return Response.ok(testSuiteRestBean).build();
    } finally {
      HttpServletRequest request = ExecutingHttpRequest.get();
      DefaultRestRateLimiter.endCall(request);
    }
  }
  
  /* Error */
  @Path("createTestCase")
  @POST
  @XsrfProtectionExcluded
  public Response createTestCase(String jsonIssueString, @javax.ws.rs.core.Context javax.ws.rs.core.HttpHeaders headers)
  {
    // Byte code:
    //   0: invokestatic 10	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   3: astore_3
    //   4: aload_3
    //   5: invokestatic 11	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:canRequestProceed	(Ljavax/servlet/http/HttpServletRequest;)Z
    //   8: istore 4
    //   10: iload 4
    //   12: ifne +24 -> 36
    //   15: aload_0
    //   16: ldc 12
    //   18: invokevirtual 13	com/go2group/synapse/rest/pub/TestSuitePublicREST:rateLimitExceeded	(Ljava/lang/String;)Ljavax/ws/rs/core/Response;
    //   21: astore 5
    //   23: invokestatic 10	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   26: astore 6
    //   28: aload 6
    //   30: invokestatic 14	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   33: aload 5
    //   35: areturn
    //   36: aload_0
    //   37: invokevirtual 15	com/go2group/synapse/rest/pub/TestSuitePublicREST:hasValidLicense	()Z
    //   40: ifne +41 -> 81
    //   43: getstatic 7	com/go2group/synapse/rest/pub/TestSuitePublicREST:log	Lorg/apache/log4j/Logger;
    //   46: ldc 16
    //   48: invokevirtual 9	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   51: aload_0
    //   52: aload_0
    //   53: getfield 3	com/go2group/synapse/rest/pub/TestSuitePublicREST:i18n	Lcom/atlassian/jira/util/I18nHelper;
    //   56: ldc 17
    //   58: invokeinterface 18 2 0
    //   63: invokevirtual 19	com/go2group/synapse/rest/pub/TestSuitePublicREST:forbidden	(Ljava/lang/String;)Ljavax/ws/rs/core/Response;
    //   66: astore 5
    //   68: invokestatic 10	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   71: astore 6
    //   73: aload 6
    //   75: invokestatic 14	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   78: aload 5
    //   80: areturn
    //   81: new 90	org/json/simple/parser/JSONParser
    //   84: dup
    //   85: invokespecial 91	org/json/simple/parser/JSONParser:<init>	()V
    //   88: astore 5
    //   90: new 92	com/go2group/synapse/rest/pub/TestSuiteRestBean
    //   93: dup
    //   94: invokespecial 93	com/go2group/synapse/rest/pub/TestSuiteRestBean:<init>	()V
    //   97: astore 6
    //   99: aconst_null
    //   100: astore 7
    //   102: aconst_null
    //   103: astore 8
    //   105: aconst_null
    //   106: astore 9
    //   108: aload 5
    //   110: aload_1
    //   111: invokevirtual 94	org/json/simple/parser/JSONParser:parse	(Ljava/lang/String;)Ljava/lang/Object;
    //   114: astore 10
    //   116: aload 10
    //   118: checkcast 95	org/json/simple/JSONObject
    //   121: astore 11
    //   123: aload 11
    //   125: ldc 96
    //   127: invokevirtual 97	org/json/simple/JSONObject:containsKey	(Ljava/lang/Object;)Z
    //   130: ifeq +132 -> 262
    //   133: aload 11
    //   135: ldc 96
    //   137: invokevirtual 98	org/json/simple/JSONObject:get	(Ljava/lang/Object;)Ljava/lang/Object;
    //   140: checkcast 99	org/json/simple/JSONArray
    //   143: astore 12
    //   145: aload 12
    //   147: invokevirtual 100	org/json/simple/JSONArray:iterator	()Ljava/util/Iterator;
    //   150: astore 13
    //   152: new 30	java/util/ArrayList
    //   155: dup
    //   156: invokespecial 31	java/util/ArrayList:<init>	()V
    //   159: astore 8
    //   161: aload 13
    //   163: invokeinterface 34 1 0
    //   168: ifeq +86 -> 254
    //   171: new 101	com/go2group/synapse/bean/TestStepInputBean
    //   174: dup
    //   175: invokespecial 102	com/go2group/synapse/bean/TestStepInputBean:<init>	()V
    //   178: astore 14
    //   180: aload 13
    //   182: invokeinterface 35 1 0
    //   187: astore 15
    //   189: aload 15
    //   191: checkcast 95	org/json/simple/JSONObject
    //   194: astore 16
    //   196: aload 14
    //   198: aload 16
    //   200: ldc 103
    //   202: invokevirtual 98	org/json/simple/JSONObject:get	(Ljava/lang/Object;)Ljava/lang/Object;
    //   205: checkcast 36	java/lang/String
    //   208: invokevirtual 104	com/go2group/synapse/bean/TestStepInputBean:setStep	(Ljava/lang/String;)V
    //   211: aload 14
    //   213: aload 16
    //   215: ldc 105
    //   217: invokevirtual 98	org/json/simple/JSONObject:get	(Ljava/lang/Object;)Ljava/lang/Object;
    //   220: checkcast 36	java/lang/String
    //   223: invokevirtual 106	com/go2group/synapse/bean/TestStepInputBean:setExpectedResult	(Ljava/lang/String;)V
    //   226: aload 14
    //   228: aload 16
    //   230: ldc 107
    //   232: invokevirtual 98	org/json/simple/JSONObject:get	(Ljava/lang/Object;)Ljava/lang/Object;
    //   235: checkcast 36	java/lang/String
    //   238: invokevirtual 108	com/go2group/synapse/bean/TestStepInputBean:setStepData	(Ljava/lang/String;)V
    //   241: aload 8
    //   243: aload 14
    //   245: invokeinterface 41 2 0
    //   250: pop
    //   251: goto -90 -> 161
    //   254: aload 11
    //   256: ldc 96
    //   258: invokevirtual 109	org/json/simple/JSONObject:remove	(Ljava/lang/Object;)Ljava/lang/Object;
    //   261: pop
    //   262: aload 11
    //   264: ldc 110
    //   266: invokevirtual 97	org/json/simple/JSONObject:containsKey	(Ljava/lang/Object;)Z
    //   269: ifeq +68 -> 337
    //   272: aload 11
    //   274: ldc 110
    //   276: invokevirtual 98	org/json/simple/JSONObject:get	(Ljava/lang/Object;)Ljava/lang/Object;
    //   279: checkcast 95	org/json/simple/JSONObject
    //   282: astore 12
    //   284: aload 6
    //   286: aload 12
    //   288: ldc 111
    //   290: invokevirtual 98	org/json/simple/JSONObject:get	(Ljava/lang/Object;)Ljava/lang/Object;
    //   293: checkcast 36	java/lang/String
    //   296: invokevirtual 112	com/go2group/synapse/rest/pub/TestSuiteRestBean:setProjectKey	(Ljava/lang/String;)V
    //   299: aload 6
    //   301: aload 12
    //   303: ldc 113
    //   305: invokevirtual 98	org/json/simple/JSONObject:get	(Ljava/lang/Object;)Ljava/lang/Object;
    //   308: checkcast 36	java/lang/String
    //   311: invokevirtual 114	com/go2group/synapse/rest/pub/TestSuiteRestBean:setTestSuitePath	(Ljava/lang/String;)V
    //   314: aload 6
    //   316: aload 12
    //   318: ldc 115
    //   320: invokevirtual 98	org/json/simple/JSONObject:get	(Ljava/lang/Object;)Ljava/lang/Object;
    //   323: checkcast 116	java/lang/Integer
    //   326: invokevirtual 117	com/go2group/synapse/rest/pub/TestSuiteRestBean:setTestSuiteId	(Ljava/lang/Integer;)V
    //   329: aload 11
    //   331: ldc 110
    //   333: invokevirtual 109	org/json/simple/JSONObject:remove	(Ljava/lang/Object;)Ljava/lang/Object;
    //   336: pop
    //   337: invokestatic 10	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   340: astore 12
    //   342: aload 11
    //   344: invokevirtual 118	org/json/simple/JSONObject:toJSONString	()Ljava/lang/String;
    //   347: astore 13
    //   349: aload 5
    //   351: aload 13
    //   353: invokevirtual 94	org/json/simple/parser/JSONParser:parse	(Ljava/lang/String;)Ljava/lang/Object;
    //   356: checkcast 95	org/json/simple/JSONObject
    //   359: astore 14
    //   361: invokestatic 119	org/apache/http/impl/client/HttpClientBuilder:create	()Lorg/apache/http/impl/client/HttpClientBuilder;
    //   364: invokevirtual 120	org/apache/http/impl/client/HttpClientBuilder:build	()Lorg/apache/http/impl/client/CloseableHttpClient;
    //   367: astore 15
    //   369: invokestatic 121	com/atlassian/jira/component/ComponentAccessor:getApplicationProperties	()Lcom/atlassian/jira/config/properties/ApplicationProperties;
    //   372: ldc 123
    //   374: invokeinterface 124 2 0
    //   379: astore 16
    //   381: aload 16
    //   383: ifnull +28 -> 411
    //   386: aload 16
    //   388: ldc 125
    //   390: invokevirtual 126	java/lang/String:endsWith	(Ljava/lang/String;)Z
    //   393: ifeq +18 -> 411
    //   396: aload 16
    //   398: iconst_0
    //   399: aload 16
    //   401: invokevirtual 127	java/lang/String:length	()I
    //   404: iconst_1
    //   405: isub
    //   406: invokevirtual 128	java/lang/String:substring	(II)Ljava/lang/String;
    //   409: astore 16
    //   411: new 44	java/lang/StringBuilder
    //   414: dup
    //   415: invokespecial 45	java/lang/StringBuilder:<init>	()V
    //   418: aload 16
    //   420: invokevirtual 48	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   423: ldc -127
    //   425: invokevirtual 48	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   428: invokevirtual 49	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   431: astore 17
    //   433: getstatic 7	com/go2group/synapse/rest/pub/TestSuitePublicREST:log	Lorg/apache/log4j/Logger;
    //   436: new 44	java/lang/StringBuilder
    //   439: dup
    //   440: invokespecial 45	java/lang/StringBuilder:<init>	()V
    //   443: ldc -126
    //   445: invokevirtual 48	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   448: aload 17
    //   450: invokevirtual 48	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   453: invokevirtual 49	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   456: invokevirtual 9	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   459: new 131	org/apache/http/client/methods/HttpPost
    //   462: dup
    //   463: aload 17
    //   465: invokespecial 132	org/apache/http/client/methods/HttpPost:<init>	(Ljava/lang/String;)V
    //   468: astore 18
    //   470: aload 18
    //   472: aload_0
    //   473: invokespecial 133	com/go2group/synapse/rest/pub/TestSuitePublicREST:getRequestConfig	()Lorg/apache/http/client/config/RequestConfig;
    //   476: invokevirtual 134	org/apache/http/client/methods/HttpPost:setConfig	(Lorg/apache/http/client/config/RequestConfig;)V
    //   479: aload 18
    //   481: ldc -121
    //   483: ldc -120
    //   485: invokevirtual 137	org/apache/http/client/methods/HttpPost:addHeader	(Ljava/lang/String;Ljava/lang/String;)V
    //   488: aload 18
    //   490: ldc -118
    //   492: aload 12
    //   494: ldc -118
    //   496: invokeinterface 140 2 0
    //   501: invokevirtual 137	org/apache/http/client/methods/HttpPost:addHeader	(Ljava/lang/String;Ljava/lang/String;)V
    //   504: aload 18
    //   506: new 141	org/apache/http/entity/StringEntity
    //   509: dup
    //   510: aload 14
    //   512: invokevirtual 118	org/json/simple/JSONObject:toJSONString	()Ljava/lang/String;
    //   515: ldc -114
    //   517: invokespecial 143	org/apache/http/entity/StringEntity:<init>	(Ljava/lang/String;Ljava/lang/String;)V
    //   520: invokevirtual 144	org/apache/http/client/methods/HttpPost:setEntity	(Lorg/apache/http/HttpEntity;)V
    //   523: aload 15
    //   525: aload 18
    //   527: invokeinterface 145 2 0
    //   532: astore 19
    //   534: getstatic 7	com/go2group/synapse/rest/pub/TestSuitePublicREST:log	Lorg/apache/log4j/Logger;
    //   537: new 44	java/lang/StringBuilder
    //   540: dup
    //   541: invokespecial 45	java/lang/StringBuilder:<init>	()V
    //   544: ldc -110
    //   546: invokevirtual 48	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   549: aload 19
    //   551: invokeinterface 147 1 0
    //   556: invokeinterface 148 1 0
    //   561: invokevirtual 149	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
    //   564: invokevirtual 49	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   567: invokevirtual 9	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   570: aload 5
    //   572: aload_0
    //   573: aload 19
    //   575: invokevirtual 150	com/go2group/synapse/rest/pub/TestSuitePublicREST:getJSONFromResponse	(Lorg/apache/http/HttpResponse;)Ljava/lang/String;
    //   578: invokevirtual 94	org/json/simple/parser/JSONParser:parse	(Ljava/lang/String;)Ljava/lang/Object;
    //   581: checkcast 95	org/json/simple/JSONObject
    //   584: astore 20
    //   586: aload 19
    //   588: invokeinterface 147 1 0
    //   593: invokeinterface 148 1 0
    //   598: sipush 200
    //   601: if_icmpeq +44 -> 645
    //   604: aload 19
    //   606: invokeinterface 147 1 0
    //   611: invokeinterface 148 1 0
    //   616: sipush 201
    //   619: if_icmpeq +26 -> 645
    //   622: aload 20
    //   624: invokestatic 88	javax/ws/rs/core/Response:ok	(Ljava/lang/Object;)Ljavax/ws/rs/core/Response$ResponseBuilder;
    //   627: invokevirtual 89	javax/ws/rs/core/Response$ResponseBuilder:build	()Ljavax/ws/rs/core/Response;
    //   630: astore 21
    //   632: invokestatic 10	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   635: astore 22
    //   637: aload 22
    //   639: invokestatic 14	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   642: aload 21
    //   644: areturn
    //   645: aload 20
    //   647: ldc -105
    //   649: invokevirtual 98	org/json/simple/JSONObject:get	(Ljava/lang/Object;)Ljava/lang/Object;
    //   652: checkcast 36	java/lang/String
    //   655: astore 7
    //   657: getstatic 7	com/go2group/synapse/rest/pub/TestSuitePublicREST:log	Lorg/apache/log4j/Logger;
    //   660: new 44	java/lang/StringBuilder
    //   663: dup
    //   664: invokespecial 45	java/lang/StringBuilder:<init>	()V
    //   667: ldc -104
    //   669: invokevirtual 48	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   672: aload 7
    //   674: invokevirtual 48	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   677: invokevirtual 49	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   680: invokevirtual 9	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   683: aload_0
    //   684: getfield 4	com/go2group/synapse/rest/pub/TestSuitePublicREST:issueManager	Lcom/atlassian/jira/issue/IssueManager;
    //   687: aload 7
    //   689: invokeinterface 37 2 0
    //   694: astore 9
    //   696: aload 9
    //   698: invokeinterface 153 1 0
    //   703: astore 21
    //   705: invokestatic 57	com/atlassian/jira/component/ComponentAccessor:getJiraAuthenticationContext	()Lcom/atlassian/jira/security/JiraAuthenticationContext;
    //   708: invokeinterface 58 1 0
    //   713: aload 21
    //   715: invokestatic 59	com/go2group/synapse/util/PluginUtil:getAuditLogInputBean	(Lcom/atlassian/jira/user/ApplicationUser;Lcom/atlassian/jira/project/Project;)Lcom/go2group/synapse/core/audit/log/AuditLogInputBean;
    //   718: astore 22
    //   720: aload 22
    //   722: getstatic 60	com/go2group/synapse/core/audit/log/ActionEnum:ADDED	Lcom/go2group/synapse/core/audit/log/ActionEnum;
    //   725: invokevirtual 61	com/go2group/synapse/core/audit/log/AuditLogInputBean:setAction	(Lcom/go2group/synapse/core/audit/log/ActionEnum;)V
    //   728: aload 22
    //   730: getstatic 62	com/go2group/synapse/core/audit/log/ModuleEnum:TEST_SUITE	Lcom/go2group/synapse/core/audit/log/ModuleEnum;
    //   733: invokevirtual 63	com/go2group/synapse/core/audit/log/AuditLogInputBean:setModule	(Lcom/go2group/synapse/core/audit/log/ModuleEnum;)V
    //   736: aload 22
    //   738: getstatic 64	com/go2group/synapse/core/audit/log/SourceEnum:REST	Lcom/go2group/synapse/core/audit/log/SourceEnum;
    //   741: invokevirtual 65	com/go2group/synapse/core/audit/log/SourceEnum:getName	()Ljava/lang/String;
    //   744: invokevirtual 66	com/go2group/synapse/core/audit/log/AuditLogInputBean:setSource	(Ljava/lang/String;)V
    //   747: aload 22
    //   749: new 67	java/util/Date
    //   752: dup
    //   753: invokespecial 68	java/util/Date:<init>	()V
    //   756: invokevirtual 69	com/go2group/synapse/core/audit/log/AuditLogInputBean:setLogTime	(Ljava/util/Date;)V
    //   759: aload 22
    //   761: new 44	java/lang/StringBuilder
    //   764: dup
    //   765: invokespecial 45	java/lang/StringBuilder:<init>	()V
    //   768: ldc -102
    //   770: invokevirtual 48	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   773: aload 9
    //   775: invokeinterface 155 1 0
    //   780: invokevirtual 48	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   783: ldc 84
    //   785: invokevirtual 48	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   788: aload 21
    //   790: invokeinterface 85 1 0
    //   795: invokevirtual 48	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   798: ldc 73
    //   800: invokevirtual 48	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   803: invokevirtual 49	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   806: invokevirtual 74	com/go2group/synapse/core/audit/log/AuditLogInputBean:setLog	(Ljava/lang/String;)V
    //   809: aload_0
    //   810: getfield 6	com/go2group/synapse/rest/pub/TestSuitePublicREST:auditLogService	Lcom/go2group/synapse/core/audit/log/service/AuditLogService;
    //   813: aload 22
    //   815: invokeinterface 75 2 0
    //   820: goto +36 -> 856
    //   823: astore 10
    //   825: getstatic 7	com/go2group/synapse/rest/pub/TestSuitePublicREST:log	Lorg/apache/log4j/Logger;
    //   828: aload 10
    //   830: invokevirtual 157	java/io/IOException:getMessage	()Ljava/lang/String;
    //   833: aload 10
    //   835: invokevirtual 54	org/apache/log4j/Logger:debug	(Ljava/lang/Object;Ljava/lang/Throwable;)V
    //   838: goto +18 -> 856
    //   841: astore 10
    //   843: getstatic 7	com/go2group/synapse/rest/pub/TestSuitePublicREST:log	Lorg/apache/log4j/Logger;
    //   846: aload 10
    //   848: invokevirtual 159	java/lang/Exception:getMessage	()Ljava/lang/String;
    //   851: aload 10
    //   853: invokevirtual 54	org/apache/log4j/Logger:debug	(Ljava/lang/Object;Ljava/lang/Throwable;)V
    //   856: aload_0
    //   857: aload 9
    //   859: invokeinterface 153 1 0
    //   864: getstatic 160	com/go2group/synapse/constant/SynapsePermission:MANAGE_TESTCASES	Lcom/go2group/synapse/constant/SynapsePermission;
    //   867: invokevirtual 27	com/go2group/synapse/rest/pub/TestSuitePublicREST:hasSynapsePermission	(Lcom/atlassian/jira/project/Project;Lcom/go2group/synapse/constant/SynapsePermission;)Z
    //   870: ifne +41 -> 911
    //   873: getstatic 7	com/go2group/synapse/rest/pub/TestSuitePublicREST:log	Lorg/apache/log4j/Logger;
    //   876: ldc -95
    //   878: invokevirtual 9	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   881: aload_0
    //   882: aload_0
    //   883: getfield 3	com/go2group/synapse/rest/pub/TestSuitePublicREST:i18n	Lcom/atlassian/jira/util/I18nHelper;
    //   886: ldc -94
    //   888: invokeinterface 18 2 0
    //   893: invokevirtual 19	com/go2group/synapse/rest/pub/TestSuitePublicREST:forbidden	(Ljava/lang/String;)Ljavax/ws/rs/core/Response;
    //   896: astore 10
    //   898: invokestatic 10	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   901: astore 11
    //   903: aload 11
    //   905: invokestatic 14	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   908: aload 10
    //   910: areturn
    //   911: aload_0
    //   912: aload 9
    //   914: invokevirtual 163	com/go2group/synapse/rest/pub/TestSuitePublicREST:hasEditPermission	(Lcom/atlassian/jira/issue/Issue;)Z
    //   917: ifne +41 -> 958
    //   920: getstatic 7	com/go2group/synapse/rest/pub/TestSuitePublicREST:log	Lorg/apache/log4j/Logger;
    //   923: ldc -92
    //   925: invokevirtual 9	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   928: aload_0
    //   929: aload_0
    //   930: getfield 3	com/go2group/synapse/rest/pub/TestSuitePublicREST:i18n	Lcom/atlassian/jira/util/I18nHelper;
    //   933: ldc -91
    //   935: invokeinterface 18 2 0
    //   940: invokevirtual 19	com/go2group/synapse/rest/pub/TestSuitePublicREST:forbidden	(Ljava/lang/String;)Ljavax/ws/rs/core/Response;
    //   943: astore 10
    //   945: invokestatic 10	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   948: astore 11
    //   950: aload 11
    //   952: invokestatic 14	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   955: aload 10
    //   957: areturn
    //   958: aload 8
    //   960: ifnull +264 -> 1224
    //   963: aload 8
    //   965: invokeinterface 76 1 0
    //   970: ifle +254 -> 1224
    //   973: iconst_0
    //   974: istore 10
    //   976: new 30	java/util/ArrayList
    //   979: dup
    //   980: invokespecial 31	java/util/ArrayList:<init>	()V
    //   983: astore 11
    //   985: new 30	java/util/ArrayList
    //   988: dup
    //   989: invokespecial 31	java/util/ArrayList:<init>	()V
    //   992: astore 12
    //   994: aload 8
    //   996: invokeinterface 33 1 0
    //   1001: astore 13
    //   1003: aload 13
    //   1005: invokeinterface 34 1 0
    //   1010: ifeq +95 -> 1105
    //   1013: aload 13
    //   1015: invokeinterface 35 1 0
    //   1020: checkcast 101	com/go2group/synapse/bean/TestStepInputBean
    //   1023: astore 14
    //   1025: aload 14
    //   1027: aload 9
    //   1029: invokeinterface 166 1 0
    //   1034: invokevirtual 167	com/go2group/synapse/bean/TestStepInputBean:setTcId	(Ljava/lang/Long;)V
    //   1037: aload 14
    //   1039: iinc 10 1
    //   1042: iload 10
    //   1044: invokestatic 168	java/lang/String:valueOf	(I)Ljava/lang/String;
    //   1047: invokevirtual 169	com/go2group/synapse/bean/TestStepInputBean:setSequenceNumber	(Ljava/lang/String;)V
    //   1050: aload_0
    //   1051: getfield 5	com/go2group/synapse/rest/pub/TestSuitePublicREST:testStepService	Lcom/go2group/synapse/service/TestStepService;
    //   1054: aload 14
    //   1056: iconst_0
    //   1057: invokeinterface 170 3 0
    //   1062: astore 15
    //   1064: aload 15
    //   1066: iload 10
    //   1068: invokestatic 168	java/lang/String:valueOf	(I)Ljava/lang/String;
    //   1071: invokevirtual 171	com/go2group/synapse/bean/TestStepOutputBean:setSequenceNumber	(Ljava/lang/String;)V
    //   1074: aload 11
    //   1076: aload 15
    //   1078: invokeinterface 41 2 0
    //   1083: pop
    //   1084: aload 12
    //   1086: aload 14
    //   1088: invokevirtual 172	com/go2group/synapse/bean/TestStepInputBean:getStep	()Ljava/lang/String;
    //   1091: bipush 50
    //   1093: invokestatic 174	com/go2group/synapse/util/PluginUtil:getEllipsisString	(Ljava/lang/String;I)Ljava/lang/String;
    //   1096: invokeinterface 41 2 0
    //   1101: pop
    //   1102: goto -99 -> 1003
    //   1105: aload 9
    //   1107: invokeinterface 153 1 0
    //   1112: astore 13
    //   1114: invokestatic 57	com/atlassian/jira/component/ComponentAccessor:getJiraAuthenticationContext	()Lcom/atlassian/jira/security/JiraAuthenticationContext;
    //   1117: invokeinterface 58 1 0
    //   1122: aload 13
    //   1124: invokestatic 59	com/go2group/synapse/util/PluginUtil:getAuditLogInputBean	(Lcom/atlassian/jira/user/ApplicationUser;Lcom/atlassian/jira/project/Project;)Lcom/go2group/synapse/core/audit/log/AuditLogInputBean;
    //   1127: astore 14
    //   1129: aload 14
    //   1131: getstatic 60	com/go2group/synapse/core/audit/log/ActionEnum:ADDED	Lcom/go2group/synapse/core/audit/log/ActionEnum;
    //   1134: invokevirtual 61	com/go2group/synapse/core/audit/log/AuditLogInputBean:setAction	(Lcom/go2group/synapse/core/audit/log/ActionEnum;)V
    //   1137: aload 14
    //   1139: getstatic 62	com/go2group/synapse/core/audit/log/ModuleEnum:TEST_SUITE	Lcom/go2group/synapse/core/audit/log/ModuleEnum;
    //   1142: invokevirtual 63	com/go2group/synapse/core/audit/log/AuditLogInputBean:setModule	(Lcom/go2group/synapse/core/audit/log/ModuleEnum;)V
    //   1145: aload 14
    //   1147: getstatic 64	com/go2group/synapse/core/audit/log/SourceEnum:REST	Lcom/go2group/synapse/core/audit/log/SourceEnum;
    //   1150: invokevirtual 65	com/go2group/synapse/core/audit/log/SourceEnum:getName	()Ljava/lang/String;
    //   1153: invokevirtual 66	com/go2group/synapse/core/audit/log/AuditLogInputBean:setSource	(Ljava/lang/String;)V
    //   1156: aload 14
    //   1158: new 67	java/util/Date
    //   1161: dup
    //   1162: invokespecial 68	java/util/Date:<init>	()V
    //   1165: invokevirtual 69	com/go2group/synapse/core/audit/log/AuditLogInputBean:setLogTime	(Ljava/util/Date;)V
    //   1168: aload 14
    //   1170: new 44	java/lang/StringBuilder
    //   1173: dup
    //   1174: invokespecial 45	java/lang/StringBuilder:<init>	()V
    //   1177: ldc -81
    //   1179: invokevirtual 48	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1182: aload 12
    //   1184: invokevirtual 46	java/lang/StringBuilder:append	(Ljava/lang/Object;)Ljava/lang/StringBuilder;
    //   1187: ldc -80
    //   1189: invokevirtual 48	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1192: aload 9
    //   1194: invokeinterface 155 1 0
    //   1199: invokevirtual 48	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1202: ldc 73
    //   1204: invokevirtual 48	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1207: invokevirtual 49	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   1210: invokevirtual 74	com/go2group/synapse/core/audit/log/AuditLogInputBean:setLog	(Ljava/lang/String;)V
    //   1213: aload_0
    //   1214: getfield 6	com/go2group/synapse/rest/pub/TestSuitePublicREST:auditLogService	Lcom/go2group/synapse/core/audit/log/service/AuditLogService;
    //   1217: aload 14
    //   1219: invokeinterface 75 2 0
    //   1224: new 177	com/go2group/synapse/bean/IssueWrapperBean
    //   1227: dup
    //   1228: invokespecial 178	com/go2group/synapse/bean/IssueWrapperBean:<init>	()V
    //   1231: astore 10
    //   1233: aload 10
    //   1235: aload 9
    //   1237: invokeinterface 166 1 0
    //   1242: invokevirtual 179	com/go2group/synapse/bean/IssueWrapperBean:setId	(Ljava/lang/Long;)V
    //   1245: aload 10
    //   1247: aload 9
    //   1249: invokeinterface 155 1 0
    //   1254: invokevirtual 180	com/go2group/synapse/bean/IssueWrapperBean:setKey	(Ljava/lang/String;)V
    //   1257: aload 6
    //   1259: invokevirtual 42	com/go2group/synapse/rest/pub/TestSuiteRestBean:getTestSuitePath	()Ljava/lang/String;
    //   1262: ifnull +193 -> 1455
    //   1265: aload 6
    //   1267: aload 9
    //   1269: invokeinterface 155 1 0
    //   1274: invokevirtual 181	com/go2group/synapse/rest/pub/TestSuiteRestBean:setTestCaseKey	(Ljava/lang/String;)V
    //   1277: invokestatic 20	com/atlassian/jira/component/ComponentAccessor:getProjectManager	()Lcom/atlassian/jira/project/ProjectManager;
    //   1280: aload 6
    //   1282: invokevirtual 21	com/go2group/synapse/rest/pub/TestSuiteRestBean:getProjectKey	()Ljava/lang/String;
    //   1285: invokeinterface 22 2 0
    //   1290: astore 11
    //   1292: aload 11
    //   1294: ifnonnull +41 -> 1335
    //   1297: getstatic 7	com/go2group/synapse/rest/pub/TestSuitePublicREST:log	Lorg/apache/log4j/Logger;
    //   1300: ldc 23
    //   1302: invokevirtual 9	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   1305: aload_0
    //   1306: aload_0
    //   1307: getfield 3	com/go2group/synapse/rest/pub/TestSuitePublicREST:i18n	Lcom/atlassian/jira/util/I18nHelper;
    //   1310: ldc 24
    //   1312: invokeinterface 18 2 0
    //   1317: invokevirtual 25	com/go2group/synapse/rest/pub/TestSuitePublicREST:error	(Ljava/lang/String;)Ljavax/ws/rs/core/Response;
    //   1320: astore 12
    //   1322: invokestatic 10	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   1325: astore 13
    //   1327: aload 13
    //   1329: invokestatic 14	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   1332: aload 12
    //   1334: areturn
    //   1335: aload_0
    //   1336: aload 11
    //   1338: getstatic 26	com/go2group/synapse/constant/SynapsePermission:MANAGE_TESTSUITES	Lcom/go2group/synapse/constant/SynapsePermission;
    //   1341: invokevirtual 27	com/go2group/synapse/rest/pub/TestSuitePublicREST:hasSynapsePermission	(Lcom/atlassian/jira/project/Project;Lcom/go2group/synapse/constant/SynapsePermission;)Z
    //   1344: ifne +41 -> 1385
    //   1347: getstatic 7	com/go2group/synapse/rest/pub/TestSuitePublicREST:log	Lorg/apache/log4j/Logger;
    //   1350: ldc 28
    //   1352: invokevirtual 9	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   1355: aload_0
    //   1356: aload_0
    //   1357: getfield 3	com/go2group/synapse/rest/pub/TestSuitePublicREST:i18n	Lcom/atlassian/jira/util/I18nHelper;
    //   1360: ldc 29
    //   1362: invokeinterface 18 2 0
    //   1367: invokevirtual 19	com/go2group/synapse/rest/pub/TestSuitePublicREST:forbidden	(Ljava/lang/String;)Ljavax/ws/rs/core/Response;
    //   1370: astore 12
    //   1372: invokestatic 10	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   1375: astore 13
    //   1377: aload 13
    //   1379: invokestatic 14	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   1382: aload 12
    //   1384: areturn
    //   1385: aload_0
    //   1386: aload 6
    //   1388: invokevirtual 42	com/go2group/synapse/rest/pub/TestSuiteRestBean:getTestSuitePath	()Ljava/lang/String;
    //   1391: aload 9
    //   1393: aload 6
    //   1395: invokevirtual 43	com/go2group/synapse/rest/pub/TestSuiteRestBean:getTestSuiteId	()Ljava/lang/Integer;
    //   1398: ifnonnull +7 -> 1405
    //   1401: aconst_null
    //   1402: goto +26 -> 1428
    //   1405: new 44	java/lang/StringBuilder
    //   1408: dup
    //   1409: invokespecial 45	java/lang/StringBuilder:<init>	()V
    //   1412: aload 6
    //   1414: invokevirtual 43	com/go2group/synapse/rest/pub/TestSuiteRestBean:getTestSuiteId	()Ljava/lang/Integer;
    //   1417: invokevirtual 46	java/lang/StringBuilder:append	(Ljava/lang/Object;)Ljava/lang/StringBuilder;
    //   1420: ldc 47
    //   1422: invokevirtual 48	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1425: invokevirtual 49	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   1428: aload 11
    //   1430: invokeinterface 50 1 0
    //   1435: iconst_1
    //   1436: invokespecial 51	com/go2group/synapse/rest/pub/TestSuitePublicREST:linkTestCaseWithTestSuite	(Ljava/lang/String;Lcom/atlassian/jira/issue/Issue;Ljava/lang/String;Ljava/lang/Long;Z)Ljava/lang/Integer;
    //   1439: astore 12
    //   1441: aload 6
    //   1443: aload 12
    //   1445: invokevirtual 87	com/go2group/synapse/rest/pub/TestSuiteRestBean:setCreatedTestSuiteId	(Ljava/lang/Integer;)V
    //   1448: aload 6
    //   1450: aload 10
    //   1452: invokevirtual 182	com/go2group/synapse/rest/pub/TestSuiteRestBean:setCreatedTestCase	(Lcom/go2group/synapse/bean/IssueWrapperBean;)V
    //   1455: aload 6
    //   1457: invokestatic 88	javax/ws/rs/core/Response:ok	(Ljava/lang/Object;)Ljavax/ws/rs/core/Response$ResponseBuilder;
    //   1460: invokevirtual 89	javax/ws/rs/core/Response$ResponseBuilder:build	()Ljavax/ws/rs/core/Response;
    //   1463: astore 11
    //   1465: invokestatic 10	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   1468: astore 12
    //   1470: aload 12
    //   1472: invokestatic 14	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   1475: aload 11
    //   1477: areturn
    //   1478: astore 10
    //   1480: getstatic 7	com/go2group/synapse/rest/pub/TestSuitePublicREST:log	Lorg/apache/log4j/Logger;
    //   1483: aload 10
    //   1485: invokevirtual 53	com/go2group/synapse/core/exception/InvalidDataException:getMessage	()Ljava/lang/String;
    //   1488: invokevirtual 55	org/apache/log4j/Logger:error	(Ljava/lang/Object;)V
    //   1491: getstatic 7	com/go2group/synapse/rest/pub/TestSuitePublicREST:log	Lorg/apache/log4j/Logger;
    //   1494: aload 10
    //   1496: invokevirtual 53	com/go2group/synapse/core/exception/InvalidDataException:getMessage	()Ljava/lang/String;
    //   1499: aload 10
    //   1501: invokevirtual 54	org/apache/log4j/Logger:debug	(Ljava/lang/Object;Ljava/lang/Throwable;)V
    //   1504: aload_0
    //   1505: aload 10
    //   1507: invokevirtual 86	com/go2group/synapse/rest/pub/TestSuitePublicREST:error	(Ljava/lang/Exception;)Ljavax/ws/rs/core/Response;
    //   1510: astore 11
    //   1512: invokestatic 10	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   1515: astore 12
    //   1517: aload 12
    //   1519: invokestatic 14	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   1522: aload 11
    //   1524: areturn
    //   1525: astore 23
    //   1527: invokestatic 10	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   1530: astore 24
    //   1532: aload 24
    //   1534: invokestatic 14	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   1537: aload 23
    //   1539: athrow
    // Line number table:
    //   Java source line #244	-> byte code offset #0
    //   Java source line #245	-> byte code offset #4
    //   Java source line #246	-> byte code offset #10
    //   Java source line #247	-> byte code offset #15
    //   Java source line #404	-> byte code offset #23
    //   Java source line #405	-> byte code offset #28
    //   Java source line #247	-> byte code offset #33
    //   Java source line #251	-> byte code offset #36
    //   Java source line #252	-> byte code offset #43
    //   Java source line #253	-> byte code offset #51
    //   Java source line #404	-> byte code offset #68
    //   Java source line #405	-> byte code offset #73
    //   Java source line #253	-> byte code offset #78
    //   Java source line #256	-> byte code offset #81
    //   Java source line #257	-> byte code offset #90
    //   Java source line #258	-> byte code offset #99
    //   Java source line #260	-> byte code offset #102
    //   Java source line #261	-> byte code offset #105
    //   Java source line #263	-> byte code offset #108
    //   Java source line #264	-> byte code offset #116
    //   Java source line #265	-> byte code offset #123
    //   Java source line #266	-> byte code offset #133
    //   Java source line #268	-> byte code offset #145
    //   Java source line #269	-> byte code offset #152
    //   Java source line #270	-> byte code offset #161
    //   Java source line #271	-> byte code offset #171
    //   Java source line #272	-> byte code offset #180
    //   Java source line #273	-> byte code offset #189
    //   Java source line #274	-> byte code offset #196
    //   Java source line #275	-> byte code offset #211
    //   Java source line #276	-> byte code offset #226
    //   Java source line #277	-> byte code offset #241
    //   Java source line #278	-> byte code offset #251
    //   Java source line #279	-> byte code offset #254
    //   Java source line #281	-> byte code offset #262
    //   Java source line #282	-> byte code offset #272
    //   Java source line #283	-> byte code offset #284
    //   Java source line #284	-> byte code offset #299
    //   Java source line #285	-> byte code offset #314
    //   Java source line #286	-> byte code offset #329
    //   Java source line #288	-> byte code offset #337
    //   Java source line #289	-> byte code offset #342
    //   Java source line #290	-> byte code offset #349
    //   Java source line #291	-> byte code offset #361
    //   Java source line #293	-> byte code offset #369
    //   Java source line #294	-> byte code offset #381
    //   Java source line #295	-> byte code offset #396
    //   Java source line #297	-> byte code offset #411
    //   Java source line #298	-> byte code offset #433
    //   Java source line #299	-> byte code offset #459
    //   Java source line #300	-> byte code offset #470
    //   Java source line #302	-> byte code offset #479
    //   Java source line #304	-> byte code offset #488
    //   Java source line #306	-> byte code offset #504
    //   Java source line #307	-> byte code offset #523
    //   Java source line #308	-> byte code offset #534
    //   Java source line #310	-> byte code offset #570
    //   Java source line #311	-> byte code offset #586
    //   Java source line #312	-> byte code offset #622
    //   Java source line #404	-> byte code offset #632
    //   Java source line #405	-> byte code offset #637
    //   Java source line #312	-> byte code offset #642
    //   Java source line #315	-> byte code offset #645
    //   Java source line #316	-> byte code offset #657
    //   Java source line #319	-> byte code offset #683
    //   Java source line #320	-> byte code offset #696
    //   Java source line #321	-> byte code offset #705
    //   Java source line #322	-> byte code offset #720
    //   Java source line #323	-> byte code offset #728
    //   Java source line #324	-> byte code offset #736
    //   Java source line #325	-> byte code offset #747
    //   Java source line #326	-> byte code offset #759
    //   Java source line #327	-> byte code offset #809
    //   Java source line #333	-> byte code offset #820
    //   Java source line #329	-> byte code offset #823
    //   Java source line #330	-> byte code offset #825
    //   Java source line #333	-> byte code offset #838
    //   Java source line #331	-> byte code offset #841
    //   Java source line #332	-> byte code offset #843
    //   Java source line #336	-> byte code offset #856
    //   Java source line #337	-> byte code offset #873
    //   Java source line #338	-> byte code offset #881
    //   Java source line #404	-> byte code offset #898
    //   Java source line #405	-> byte code offset #903
    //   Java source line #338	-> byte code offset #908
    //   Java source line #342	-> byte code offset #911
    //   Java source line #343	-> byte code offset #920
    //   Java source line #344	-> byte code offset #928
    //   Java source line #404	-> byte code offset #945
    //   Java source line #405	-> byte code offset #950
    //   Java source line #344	-> byte code offset #955
    //   Java source line #347	-> byte code offset #958
    //   Java source line #348	-> byte code offset #973
    //   Java source line #350	-> byte code offset #976
    //   Java source line #351	-> byte code offset #985
    //   Java source line #352	-> byte code offset #994
    //   Java source line #354	-> byte code offset #1025
    //   Java source line #355	-> byte code offset #1037
    //   Java source line #356	-> byte code offset #1050
    //   Java source line #357	-> byte code offset #1064
    //   Java source line #358	-> byte code offset #1074
    //   Java source line #360	-> byte code offset #1084
    //   Java source line #361	-> byte code offset #1102
    //   Java source line #364	-> byte code offset #1105
    //   Java source line #365	-> byte code offset #1114
    //   Java source line #366	-> byte code offset #1129
    //   Java source line #367	-> byte code offset #1137
    //   Java source line #368	-> byte code offset #1145
    //   Java source line #369	-> byte code offset #1156
    //   Java source line #370	-> byte code offset #1168
    //   Java source line #371	-> byte code offset #1213
    //   Java source line #374	-> byte code offset #1224
    //   Java source line #375	-> byte code offset #1233
    //   Java source line #376	-> byte code offset #1245
    //   Java source line #378	-> byte code offset #1257
    //   Java source line #379	-> byte code offset #1265
    //   Java source line #380	-> byte code offset #1277
    //   Java source line #382	-> byte code offset #1292
    //   Java source line #383	-> byte code offset #1297
    //   Java source line #384	-> byte code offset #1305
    //   Java source line #404	-> byte code offset #1322
    //   Java source line #405	-> byte code offset #1327
    //   Java source line #384	-> byte code offset #1332
    //   Java source line #387	-> byte code offset #1335
    //   Java source line #388	-> byte code offset #1347
    //   Java source line #389	-> byte code offset #1355
    //   Java source line #404	-> byte code offset #1372
    //   Java source line #405	-> byte code offset #1377
    //   Java source line #389	-> byte code offset #1382
    //   Java source line #392	-> byte code offset #1385
    //   Java source line #393	-> byte code offset #1395
    //   Java source line #392	-> byte code offset #1436
    //   Java source line #394	-> byte code offset #1441
    //   Java source line #395	-> byte code offset #1448
    //   Java source line #397	-> byte code offset #1455
    //   Java source line #404	-> byte code offset #1465
    //   Java source line #405	-> byte code offset #1470
    //   Java source line #397	-> byte code offset #1475
    //   Java source line #398	-> byte code offset #1478
    //   Java source line #399	-> byte code offset #1480
    //   Java source line #400	-> byte code offset #1491
    //   Java source line #401	-> byte code offset #1504
    //   Java source line #404	-> byte code offset #1512
    //   Java source line #405	-> byte code offset #1517
    //   Java source line #401	-> byte code offset #1522
    //   Java source line #404	-> byte code offset #1525
    //   Java source line #405	-> byte code offset #1532
    //   Java source line #406	-> byte code offset #1537
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	1540	0	this	TestSuitePublicREST
    //   0	1540	1	jsonIssueString	String
    //   0	1540	2	headers	javax.ws.rs.core.HttpHeaders
    //   3	2	3	req	HttpServletRequest
    //   8	3	4	canProceed	boolean
    //   21	58	5	localResponse1	Response
    //   88	483	5	parser	JSONParser
    //   26	3	6	request	HttpServletRequest
    //   71	3	6	request	HttpServletRequest
    //   97	1359	6	testSuiteRestBean	TestSuiteRestBean
    //   100	588	7	key	String
    //   103	892	8	steps	List<com.go2group.synapse.bean.TestStepInputBean>
    //   106	1286	9	testCase	Issue
    //   114	3	10	obj	Object
    //   823	11	10	e	IOException
    //   841	115	10	e	Exception
    //   974	93	10	stepCount	int
    //   1231	220	10	issueWrapperBean	com.go2group.synapse.bean.IssueWrapperBean
    //   1478	28	10	e	InvalidDataException
    //   121	222	11	jsonObject	JSONObject
    //   901	3	11	request	HttpServletRequest
    //   948	3	11	request	HttpServletRequest
    //   983	92	11	createdSteps	List<com.go2group.synapse.bean.TestStepOutputBean>
    //   1290	233	11	project	Project
    //   143	3	12	stepsObj	org.json.simple.JSONArray
    //   282	35	12	suiteObject	JSONObject
    //   340	153	12	request1	HttpServletRequest
    //   992	391	12	stepTexts	List<String>
    //   1439	5	12	testSuiteId	Integer
    //   1468	3	12	request	HttpServletRequest
    //   1515	3	12	request	HttpServletRequest
    //   150	31	13	iterator	Iterator<String>
    //   347	667	13	strippedJson	String
    //   1112	11	13	project	Project
    //   1325	3	13	request	HttpServletRequest
    //   1375	3	13	request	HttpServletRequest
    //   178	66	14	bean	com.go2group.synapse.bean.TestStepInputBean
    //   359	152	14	toJira	JSONObject
    //   1023	64	14	step	com.go2group.synapse.bean.TestStepInputBean
    //   1127	91	14	auditLogInputBean	AuditLogInputBean
    //   187	3	15	stepObj	Object
    //   367	157	15	client	org.apache.http.client.HttpClient
    //   1062	15	15	createdStep	com.go2group.synapse.bean.TestStepOutputBean
    //   194	35	16	stepJSON	JSONObject
    //   379	40	16	baseUrl	String
    //   431	33	17	url	String
    //   468	58	18	request	org.apache.http.client.methods.HttpPost
    //   532	73	19	response	HttpResponse
    //   584	62	20	responseObject	JSONObject
    //   630	13	21	localResponse2	Response
    //   703	86	21	project	Project
    //   635	3	22	request	HttpServletRequest
    //   718	96	22	auditLogInputBean	AuditLogInputBean
    //   1525	13	23	localObject1	Object
    //   1530	3	24	request	HttpServletRequest
    // Exception table:
    //   from	to	target	type
    //   108	632	823	java/io/IOException
    //   645	820	823	java/io/IOException
    //   108	632	841	java/lang/Exception
    //   645	820	841	java/lang/Exception
    //   958	1322	1478	com/go2group/synapse/core/exception/InvalidDataException
    //   1335	1372	1478	com/go2group/synapse/core/exception/InvalidDataException
    //   1385	1465	1478	com/go2group/synapse/core/exception/InvalidDataException
    //   0	23	1525	finally
    //   36	68	1525	finally
    //   81	632	1525	finally
    //   645	898	1525	finally
    //   911	945	1525	finally
    //   958	1322	1525	finally
    //   1335	1372	1525	finally
    //   1385	1465	1525	finally
    //   1478	1512	1525	finally
    //   1525	1527	1525	finally
  }
  
  private Integer linkTestCaseWithTestSuite(String testSuitePath, Issue testCase, String testSuiteId, Long projectId, boolean linkTestCase)
    throws InvalidDataException
  {
    if (log.isDebugEnabled()) {
      log.debug("link TestCase With TestSuite : " + testSuiteId);
    }
    if (log.isDebugEnabled()) {
      log.debug("User provided Test suite : " + testSuitePath);
    }
    
    Integer createdSuiteId = null;
    if (StringUtils.isNotBlank(testSuitePath))
    {


      TestSuiteOutputBean childBean = null;
      if (StringUtils.isNotBlank(testSuiteId)) {
        if (log.isDebugEnabled()) {
          log.debug("Import Test cases has been initated from the Test Suite");
        }
        childBean = testSuiteService.getTestSuite(Integer.valueOf(testSuiteId));
      }
      if (log.isDebugEnabled()) {
        log.debug("Test suite bean for the selected Test Suite ID " + childBean);
      }
      String suitePath = testSuitePath;
      
      suitePath = suitePath.replace("\\/", "**-**");
      StringTokenizer stringTokenizer = new StringTokenizer(suitePath, "/");
      int totalTokens = stringTokenizer.countTokens();
      log.debug("totalTokens : " + totalTokens);
      while (stringTokenizer.hasMoreTokens()) {
        String tsName = stringTokenizer.nextToken().trim();
        if (tsName.contains("**-**")) {
          tsName = tsName.replace("**-**", "/");
        }
        log.debug("tsName : " + tsName);
        TestSuiteOutputBean tmpBean; if (childBean == null) {
          log.debug("Import Test cases has been initated from REST. Get the Test Suite given in the Request - " + tsName);
          

          List<TestSuiteOutputBean> childBeans = testSuiteService.getAllRootTestSuites(projectId);
          
          if ((childBeans != null) && (childBeans.size() > 0)) {
            for (Iterator localIterator = childBeans.iterator(); localIterator.hasNext();) { tmpBean = (TestSuiteOutputBean)localIterator.next();
              if (tmpBean.getName().equals(tsName)) {
                childBean = tmpBean;
              }
            }
            if (childBean != null) {
              log.debug("Found the root suite : " + childBean);
              continue;
            }
          }
        }
        
        if (childBean != null) {
          boolean foundSuite = false;
          Object testSuiteMemberBeans = childBean.getTestSuiteMembers(new boolean[0]);
          
          if ((testSuiteMemberBeans != null) && (((List)testSuiteMemberBeans).size() > 0))
          {
            for (TestSuiteMemberOutputBean testSuiteMemberBean : (List)testSuiteMemberBeans)
            {

              TestSuiteMemberOutputBean tempMembean = testSuiteService.getMember(testSuiteMemberBean.getID());
              
              if ((tempMembean != null) && (tempMembean.getMemberType().intValue() == 1))
              {
                if (tempMembean.getTestSuiteRepresenation().getName().equals(tsName)) {
                  log.debug("Found the Test Suite given by User");
                  foundSuite = true;
                  
                  TestSuiteOutputBean suiteOutputBean = testSuiteService.getTestSuite(Integer.valueOf(tempMembean.getMemberId().intValue()));
                  childBean = suiteOutputBean;
                  break;
                }
              }
            }
          }
          
          if (!foundSuite) {
            log.debug("Test Suite given by User not found");
            TestSuiteInputBean testSuiteInputBean = new TestSuiteInputBean();
            testSuiteInputBean.setProjectId(projectId);
            testSuiteInputBean.setParentSuiteId(Integer.valueOf(childBean.getID().intValue()));
            testSuiteInputBean.setTestSuiteName(tsName);
            TestSuiteOutputBean newSuiteBean = testSuiteService.addSubTestSuite(testSuiteInputBean);
            log.debug("Created new Test suite for the given name " + tsName);
            List<Integer> tsIds = new ArrayList();
            tsIds.add(childBean.getID());
            testSuiteService.addMember(tsIds, Long.valueOf(newSuiteBean.getID().intValue()), TestSuiteMemberTypeEnum.TESTSUITE, 
              ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser());
            log.debug("Added as member " + tsName);
            childBean = newSuiteBean;
          }
        } else {
          log.debug("No root suite found. Create the new root suite : " + tsName);
          TestSuiteOutputBean newSuiteBean = testSuiteService.addRootTestSuite(projectId, tsName);
          childBean = newSuiteBean;
          log.debug("created the root suite " + tsName);
        }
      }
      
      if (childBean != null) {
        createdSuiteId = childBean.getID();
      }
      if (linkTestCase) {
        log.debug("Adding the test cases to the test suite given by the user : " + childBean.getName());
        List<Integer> tsIds = new ArrayList();
        tsIds.add(childBean.getID());
        createdSuiteId = childBean.getID();
        TestSuiteMemberInputBean tsMemberInputBean = new TestSuiteMemberInputBean();
        tsMemberInputBean.setTestCaseId(testCase.getId());
        testSuiteService.addMember(tsIds, tsMemberInputBean.getTestCaseId(), TestSuiteMemberTypeEnum.TESTCASE, ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser());
      }
      
    }
    else if (linkTestCase) {
      log.debug("StringUtils.isNotBlank(testSuiteId) - " + StringUtils.isNotBlank(testSuiteId) + "test suite " + testSuiteId);
      
      if (StringUtils.isNotBlank(testSuiteId))
      {


        createdSuiteId = Integer.valueOf(testSuiteId);
        log.debug("Add the test cases to the User selected Test Suite");
        List<Integer> tsIds = new ArrayList();
        tsIds.add(Integer.valueOf(testSuiteId));
        TestSuiteMemberInputBean tsMemberInputBean = new TestSuiteMemberInputBean();
        tsMemberInputBean.setTestCaseId(testCase.getId());
        testSuiteService.addMember(tsIds, tsMemberInputBean.getTestCaseId(), TestSuiteMemberTypeEnum.TESTCASE, ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser());
      }
    }
    

    log.debug("done with linking test suites ");
    return createdSuiteId;
  }
  
  private RequestConfig getRequestConfig() {
    int CONNECTION_TIMEOUT_MS = 60000;
    return RequestConfig.custom()
      .setConnectionRequestTimeout(CONNECTION_TIMEOUT_MS)
      .setConnectTimeout(CONNECTION_TIMEOUT_MS)
      .setSocketTimeout(CONNECTION_TIMEOUT_MS)
      .build();
  }
  
  protected String getJSONFromResponse(HttpResponse response) throws IOException { BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
    StringBuffer buff = new StringBuffer();
    for (;;) {
      String line = reader.readLine();
      if (line == null) {
        break;
      }
      buff.append(line);
    }
    
    return buff.toString();
  }
  
  @Path("{projectKey}/testSuites")
  @GET
  @XsrfProtectionExcluded
  public Response getAllTestSuites(@PathParam("projectKey") String projectKey) {
    try {
      HttpServletRequest request = ExecutingHttpRequest.get();
      canProceed = DefaultRestRateLimiter.canRequestProceed(request);
      HttpServletRequest request; if (!canProceed) {
        return rateLimitExceeded("Too many Requests");
      }
      Object project;
      if (StringUtils.isNotBlank(projectKey)) {
        log.debug("Retrieving test suites for project :" + projectKey);
        project = null;
        project = ComponentAccessor.getProjectManager().getProjectObjByKeyIgnoreCase(projectKey);
        if (project == null) {
          log.debug("Project not found for key:" + projectKey);
          HttpServletRequest request; return notFound(i18n.getText("servererror.rest.invalid.project", projectKey));
        }
        HttpServletRequest request;
        if (!hasValidLicense()) {
          log.debug("Invalid license");
          return forbidden(i18n.getText("servererror.rest.invalid.license"));
        }
        ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
        if (!permissionUtil.hasBrowsePermission((Project)project, user)) {
          log.debug(i18n.getText("synapse.gadget.error.browse.permission.project"));
          HttpServletRequest request; return forbidden(i18n.getText("synapse.gadget.error.browse.permission.project"));
        }
        try
        {
          List<TestSuiteOutputBean> testSuites = testSuiteService.getAllRootTestSuites(((Project)project).getId());
          testSuiteRestBeans = new ArrayList();
          Object localObject1; if ((testSuites != null) && (testSuites.size() > 0))
            for (localObject1 = testSuites.iterator(); ((Iterator)localObject1).hasNext();) { TestSuiteOutputBean testSuite = (TestSuiteOutputBean)((Iterator)localObject1).next();
              TestSuiteRootRestBean testSuiteRestBean = new TestSuiteRootRestBean(testSuite);
              testSuiteRestBeans.add(testSuiteRestBean);
            }
          HttpServletRequest request;
          return Response.ok(testSuiteRestBeans).build();
        } catch (InvalidDataException e) { List<TestSuiteRootRestBean> testSuiteRestBeans;
          log.error(e.getMessage());
          log.debug(e.getMessage(), e);
          HttpServletRequest request; return error(e);
        }
      }
      log.debug("Project Key is invalid" + projectKey);
      HttpServletRequest request; return notFound(i18n.getText("errormessage.testsuite.validation.invalid.projectid", projectKey));
    } catch (Exception e) { boolean canProceed;
      log.error(e.getMessage());
      log.debug(e.getMessage(), e);
      HttpServletRequest request; return error(e);
    } finally {
      HttpServletRequest request = ExecutingHttpRequest.get();
      DefaultRestRateLimiter.endCall(request);
    }
  }
  
  @Path("/testSuite/{testSuiteId}")
  @GET
  @XsrfProtectionExcluded
  public Response getTestSuite(@PathParam("testSuiteId") String testSuiteId) {
    try {
      HttpServletRequest request = ExecutingHttpRequest.get();
      canProceed = DefaultRestRateLimiter.canRequestProceed(request);
      if (!canProceed) { HttpServletRequest request;
        return rateLimitExceeded("Too many Requests");
      }
      Object testSuite;
      if (StringUtils.isNotBlank(testSuiteId)) {
        log.debug("Retrieving test suite with id :" + testSuiteId);
        testSuite = testSuiteService.getTestSuite(Integer.valueOf(Integer.parseInt(testSuiteId)));
        
        if (testSuite != null) {
          Project project = ComponentAccessor.getProjectManager().getProjectObj(((TestSuiteOutputBean)testSuite).getProjectId());
          HttpServletRequest request;
          if (!hasValidLicense()) {
            log.debug("Invalid license");
            return forbidden(i18n.getText("servererror.rest.invalid.license"));
          }
          ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
          if (!permissionUtil.hasBrowsePermission(project, user)) {
            log.debug(i18n.getText("synapse.gadget.error.browse.permission.project"));
            HttpServletRequest request; return forbidden(i18n.getText("synapse.gadget.error.browse.permission.project"));
          }
          
          List<TestSuiteMemberOutputBean> testSuiteBeans = ((TestSuiteOutputBean)testSuite).getTestSuiteTypeMembers();
          List<TestSuiteRootRestBean> testSuiteRestBeans = new ArrayList();
          Iterator localIterator; if ((testSuiteBeans != null) && (testSuiteBeans.size() > 0))
            for (localIterator = testSuiteBeans.iterator(); localIterator.hasNext();) { testSuiteBean = (TestSuiteMemberOutputBean)localIterator.next();
              TestSuiteOutputBean testSuiteRepresenation = testSuiteService.getTestSuite(Integer.valueOf(testSuiteBean.getMemberId().intValue()));
              TestSuiteRootRestBean testSuiteRestBean = new TestSuiteRootRestBean(testSuiteRepresenation);
              testSuiteRestBeans.add(testSuiteRestBean);
            }
          TestSuiteMemberOutputBean testSuiteBean;
          TestSuiteMemberRestBean testSuiteBean = new TestSuiteMemberRestBean((TestSuiteOutputBean)testSuite, testSuiteRestBeans);
          testSuiteBean.setPath(testSuiteService.getRootPathName((TestSuiteOutputBean)testSuite));
          HttpServletRequest request; return Response.ok(testSuiteBean).build();
        }
      }
      
      log.debug("Test Suite Id is invalid" + testSuiteId);
      HttpServletRequest request; return notFound(i18n.getText("errormessage.testsuite.validation.invalid.testsuiteid", testSuiteId));
    } catch (Exception e) { boolean canProceed;
      log.error(e.getMessage());
      log.debug(e.getMessage(), e);
      HttpServletRequest request; return error(e);
    } finally {
      HttpServletRequest request = ExecutingHttpRequest.get();
      DefaultRestRateLimiter.endCall(request);
    }
  }
  
  @DELETE
  @Path("{projectKey}/removeTestSuiteMember")
  @XsrfProtectionExcluded
  public Response removeTestSuiteMember(@PathParam("projectKey") String projectKey, String data) {
    try {
      HttpServletRequest request = ExecutingHttpRequest.get();
      canProceed = DefaultRestRateLimiter.canRequestProceed(request);
      HttpServletRequest request; if (!canProceed) {
        return rateLimitExceeded("Too many Requests");
      }
      
      Project project = null;
      if (StringUtils.isNotBlank(projectKey)) {
        log.debug("Retrieving test suites for project :" + projectKey);
        project = ComponentAccessor.getProjectManager().getProjectObjByKeyIgnoreCase(projectKey);
        if (project == null) {
          log.debug("Project not found for key:" + projectKey);
          HttpServletRequest request; return notFound(i18n.getText("servererror.rest.invalid.project", projectKey));
        }
        HttpServletRequest request;
        if (!hasValidLicense()) {
          log.debug("Invalid license");
          return forbidden(i18n.getText("servererror.rest.invalid.license"));
        }
        ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
        if (!permissionUtil.hasBrowsePermission(project, user)) {
          log.debug(i18n.getText("synapse.gadget.error.browse.permission.project"));
          HttpServletRequest request; return forbidden(i18n.getText("synapse.gadget.error.browse.permission.project"));
        }
      }
      JSONParser parser = new JSONParser();
      Object obj = parser.parse(data);
      JSONObject jsonObject = (JSONObject)obj;
      String[] testCaseIds = null;
      if (jsonObject.containsKey("testCaseKeys")) {
        String testCaseKeys = (String)jsonObject.get("testCaseKeys");
        if ((testCaseKeys == null) || (testCaseKeys.isEmpty())) {
          throw new InvalidDataException("TestCaseId is Mandatory");
        }
        testCaseIds = testCaseKeys.split(",");
      }
      
      String[] testSuiteNames = null;
      String testSuiteKey; if (jsonObject.containsKey("testSuiteName")) {
        testSuiteKey = (String)jsonObject.get("testSuiteName");
        if ((testSuiteKey == null) || (testSuiteKey.isEmpty())) {
          throw new InvalidDataException("testSuiteName is Mandatory");
        }
        testSuiteNames = testSuiteKey.split(",");
      }
      
      for (String testSuiteName : testSuiteNames) {
        List<Issue> testCasesByTestSuites = testSuiteService.getTestCasesForFullSuitePath(testSuiteName, project.getId(), 0);
        Integer testSuiteId = testSuiteService.getTestSuiteByName(testSuiteName, project.getId());
        Long testCaseIssueId; for (String testCaseId : testCaseIds)
        {
          Issue testCaseIssue = issueManager.getIssueByCurrentKey(testCaseId.trim());
          Long testCaseIssueId; if (testCaseIssue != null) {
            testCaseIssueId = testCaseIssue.getId();
          } else {
            testCaseIssueId = Long.valueOf(testCaseId);
          }
          for (Issue testCase : testCasesByTestSuites) {
            if (testCase.getId().equals(testCaseIssueId))
              testSuiteService.removeMember(testSuiteId, testCaseIssueId);
          }
        }
      }
      HttpServletRequest request;
      return success();
    } catch (Exception e) { boolean canProceed;
      log.error(e.getMessage());
      log.debug(e.getMessage(), e);
      HttpServletRequest request; return error(e);
    } finally {
      HttpServletRequest request = ExecutingHttpRequest.get();
      DefaultRestRateLimiter.endCall(request);
    }
  }
}
