package com.go2group.synapse.rest.pub;

import com.atlassian.annotations.security.XsrfProtectionExcluded;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.go2group.synapse.bean.IssueWrapperBean;
import com.go2group.synapse.bean.RequirementLinkInputBean;
import com.go2group.synapse.bean.TestCaseDetailsOutputBean;
import com.go2group.synapse.bean.TestStepInputBean;
import com.go2group.synapse.bean.TestStepOutputBean;
import com.go2group.synapse.bean.TestSuiteOutputBean;
import com.go2group.synapse.constant.SynapsePermission;
import com.go2group.synapse.core.audit.log.ActionEnum;
import com.go2group.synapse.core.audit.log.AuditLogInputBean;
import com.go2group.synapse.core.audit.log.ModuleEnum;
import com.go2group.synapse.core.audit.log.SourceEnum;
import com.go2group.synapse.core.audit.log.service.AuditLogService;
import com.go2group.synapse.core.exception.InvalidDataException;
import com.go2group.synapse.enums.TestRunTypeEnum;
import com.go2group.synapse.manager.PermissionUtilAbstract;
import com.go2group.synapse.service.TestCaseToRequirementLinkService;
import com.go2group.synapse.service.TestCycleService;
import com.go2group.synapse.service.TestPlanMemberService;
import com.go2group.synapse.service.TestRunService;
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
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;








@Path("public/testCase/{issueKey}")
@Consumes({"application/json"})
@Produces({"application/json"})
public class TestCasePublicREST
  extends AbstractPublicREST
{
  private static final Logger log = Logger.getLogger(TestCasePublicREST.class);
  

  private final IssueManager issueManager;
  
  private final TestStepService testStepService;
  
  private final I18nHelper i18n;
  
  private final TestCaseToRequirementLinkService tc2rLinkService;
  
  private final TestCycleService cycleService;
  
  private final TestRunService runService;
  
  private final TestSuiteService testSuiteService;
  
  private final TestPlanMemberService tpMemberService;
  
  private final AuditLogService auditLogService;
  

  public TestCasePublicREST(@ComponentImport IssueManager issueManager, PermissionUtilAbstract permissionUtil, @ComponentImport I18nHelper i18n, TestStepService testStepService, TestCaseToRequirementLinkService tc2rLinkService, TestCycleService cycleService, TestRunService runService, TestSuiteService testSuiteService, TestPlanMemberService tpMemberService, AuditLogService auditLogService)
  {
    super(permissionUtil);
    this.issueManager = issueManager;
    this.testStepService = testStepService;
    this.i18n = i18n;
    this.tc2rLinkService = tc2rLinkService;
    this.cycleService = cycleService;
    this.runService = runService;
    this.testSuiteService = testSuiteService;
    this.tpMemberService = tpMemberService;
    this.auditLogService = auditLogService;
  }
  
  /* Error */
  @Path("addSteps")
  @POST
  @XsrfProtectionExcluded
  public Response addSteps(@PathParam("issueKey") String issueKey, List<TestStepInputBean> steps)
  {
    // Byte code:
    //   0: getstatic 11	com/go2group/synapse/rest/pub/TestCasePublicREST:log	Lorg/apache/log4j/Logger;
    //   3: invokevirtual 12	org/apache/log4j/Logger:isDebugEnabled	()Z
    //   6: ifeq +53 -> 59
    //   9: getstatic 11	com/go2group/synapse/rest/pub/TestCasePublicREST:log	Lorg/apache/log4j/Logger;
    //   12: new 13	java/lang/StringBuilder
    //   15: dup
    //   16: invokespecial 14	java/lang/StringBuilder:<init>	()V
    //   19: ldc 15
    //   21: invokevirtual 16	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   24: aload_1
    //   25: invokevirtual 16	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   28: invokevirtual 17	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   31: invokevirtual 18	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   34: getstatic 11	com/go2group/synapse/rest/pub/TestCasePublicREST:log	Lorg/apache/log4j/Logger;
    //   37: new 13	java/lang/StringBuilder
    //   40: dup
    //   41: invokespecial 14	java/lang/StringBuilder:<init>	()V
    //   44: ldc 19
    //   46: invokevirtual 16	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   49: aload_2
    //   50: invokevirtual 20	java/lang/StringBuilder:append	(Ljava/lang/Object;)Ljava/lang/StringBuilder;
    //   53: invokevirtual 17	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   56: invokevirtual 18	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   59: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   62: astore_3
    //   63: aload_3
    //   64: invokestatic 22	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:canRequestProceed	(Ljavax/servlet/http/HttpServletRequest;)Z
    //   67: istore 4
    //   69: iload 4
    //   71: ifne +24 -> 95
    //   74: aload_0
    //   75: ldc 23
    //   77: invokevirtual 24	com/go2group/synapse/rest/pub/TestCasePublicREST:rateLimitExceeded	(Ljava/lang/String;)Ljavax/ws/rs/core/Response;
    //   80: astore 5
    //   82: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   85: astore 6
    //   87: aload 6
    //   89: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   92: aload 5
    //   94: areturn
    //   95: aload_0
    //   96: getfield 2	com/go2group/synapse/rest/pub/TestCasePublicREST:issueManager	Lcom/atlassian/jira/issue/IssueManager;
    //   99: aload_1
    //   100: invokeinterface 26 2 0
    //   105: astore 5
    //   107: aload 5
    //   109: ifnonnull +59 -> 168
    //   112: getstatic 11	com/go2group/synapse/rest/pub/TestCasePublicREST:log	Lorg/apache/log4j/Logger;
    //   115: new 13	java/lang/StringBuilder
    //   118: dup
    //   119: invokespecial 14	java/lang/StringBuilder:<init>	()V
    //   122: ldc 27
    //   124: invokevirtual 16	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   127: aload_1
    //   128: invokevirtual 16	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   131: invokevirtual 17	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   134: invokevirtual 18	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   137: aload_0
    //   138: aload_0
    //   139: getfield 4	com/go2group/synapse/rest/pub/TestCasePublicREST:i18n	Lcom/atlassian/jira/util/I18nHelper;
    //   142: ldc 28
    //   144: aload_1
    //   145: invokeinterface 29 3 0
    //   150: invokevirtual 30	com/go2group/synapse/rest/pub/TestCasePublicREST:notFound	(Ljava/lang/String;)Ljavax/ws/rs/core/Response;
    //   153: astore 6
    //   155: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   158: astore 7
    //   160: aload 7
    //   162: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   165: aload 6
    //   167: areturn
    //   168: aload_0
    //   169: invokevirtual 31	com/go2group/synapse/rest/pub/TestCasePublicREST:hasValidLicense	()Z
    //   172: ifne +41 -> 213
    //   175: getstatic 11	com/go2group/synapse/rest/pub/TestCasePublicREST:log	Lorg/apache/log4j/Logger;
    //   178: ldc 32
    //   180: invokevirtual 18	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   183: aload_0
    //   184: aload_0
    //   185: getfield 4	com/go2group/synapse/rest/pub/TestCasePublicREST:i18n	Lcom/atlassian/jira/util/I18nHelper;
    //   188: ldc 33
    //   190: invokeinterface 34 2 0
    //   195: invokevirtual 35	com/go2group/synapse/rest/pub/TestCasePublicREST:forbidden	(Ljava/lang/String;)Ljavax/ws/rs/core/Response;
    //   198: astore 6
    //   200: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   203: astore 7
    //   205: aload 7
    //   207: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   210: aload 6
    //   212: areturn
    //   213: aload_0
    //   214: aload 5
    //   216: invokevirtual 36	com/go2group/synapse/rest/pub/TestCasePublicREST:hasEditPermission	(Lcom/atlassian/jira/issue/Issue;)Z
    //   219: ifne +41 -> 260
    //   222: getstatic 11	com/go2group/synapse/rest/pub/TestCasePublicREST:log	Lorg/apache/log4j/Logger;
    //   225: ldc 37
    //   227: invokevirtual 18	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   230: aload_0
    //   231: aload_0
    //   232: getfield 4	com/go2group/synapse/rest/pub/TestCasePublicREST:i18n	Lcom/atlassian/jira/util/I18nHelper;
    //   235: ldc 38
    //   237: invokeinterface 34 2 0
    //   242: invokevirtual 35	com/go2group/synapse/rest/pub/TestCasePublicREST:forbidden	(Ljava/lang/String;)Ljavax/ws/rs/core/Response;
    //   245: astore 6
    //   247: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   250: astore 7
    //   252: aload 7
    //   254: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   257: aload 6
    //   259: areturn
    //   260: aload_0
    //   261: aload 5
    //   263: invokeinterface 39 1 0
    //   268: getstatic 40	com/go2group/synapse/constant/SynapsePermission:MANAGE_TESTCASES	Lcom/go2group/synapse/constant/SynapsePermission;
    //   271: invokevirtual 41	com/go2group/synapse/rest/pub/TestCasePublicREST:hasSynapsePermission	(Lcom/atlassian/jira/project/Project;Lcom/go2group/synapse/constant/SynapsePermission;)Z
    //   274: ifne +41 -> 315
    //   277: getstatic 11	com/go2group/synapse/rest/pub/TestCasePublicREST:log	Lorg/apache/log4j/Logger;
    //   280: ldc 42
    //   282: invokevirtual 18	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   285: aload_0
    //   286: aload_0
    //   287: getfield 4	com/go2group/synapse/rest/pub/TestCasePublicREST:i18n	Lcom/atlassian/jira/util/I18nHelper;
    //   290: ldc 43
    //   292: invokeinterface 34 2 0
    //   297: invokevirtual 35	com/go2group/synapse/rest/pub/TestCasePublicREST:forbidden	(Ljava/lang/String;)Ljavax/ws/rs/core/Response;
    //   300: astore 6
    //   302: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   305: astore 7
    //   307: aload 7
    //   309: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   312: aload 6
    //   314: areturn
    //   315: iconst_0
    //   316: istore 6
    //   318: aload_0
    //   319: getfield 3	com/go2group/synapse/rest/pub/TestCasePublicREST:testStepService	Lcom/go2group/synapse/service/TestStepService;
    //   322: aload 5
    //   324: invokeinterface 44 2 0
    //   329: astore 7
    //   331: aload 7
    //   333: ifnull +23 -> 356
    //   336: aload 7
    //   338: invokevirtual 45	com/go2group/synapse/bean/TestCaseDetailsOutputBean:getTestSteps	()Ljava/util/List;
    //   341: ifnull +15 -> 356
    //   344: aload 7
    //   346: invokevirtual 45	com/go2group/synapse/bean/TestCaseDetailsOutputBean:getTestSteps	()Ljava/util/List;
    //   349: invokeinterface 46 1 0
    //   354: istore 6
    //   356: new 47	java/util/ArrayList
    //   359: dup
    //   360: invokespecial 48	java/util/ArrayList:<init>	()V
    //   363: astore 8
    //   365: aload_2
    //   366: invokeinterface 49 1 0
    //   371: astore 9
    //   373: aload 9
    //   375: invokeinterface 50 1 0
    //   380: ifeq +216 -> 596
    //   383: aload 9
    //   385: invokeinterface 51 1 0
    //   390: checkcast 52	com/go2group/synapse/bean/TestStepInputBean
    //   393: astore 10
    //   395: aload 10
    //   397: aload 5
    //   399: invokeinterface 53 1 0
    //   404: invokevirtual 54	com/go2group/synapse/bean/TestStepInputBean:setTcId	(Ljava/lang/Long;)V
    //   407: aload 10
    //   409: iinc 6 1
    //   412: iload 6
    //   414: invokestatic 55	java/lang/String:valueOf	(I)Ljava/lang/String;
    //   417: invokevirtual 56	com/go2group/synapse/bean/TestStepInputBean:setSequenceNumber	(Ljava/lang/String;)V
    //   420: aload_0
    //   421: getfield 3	com/go2group/synapse/rest/pub/TestCasePublicREST:testStepService	Lcom/go2group/synapse/service/TestStepService;
    //   424: aload 10
    //   426: iconst_0
    //   427: invokeinterface 57 3 0
    //   432: astore 11
    //   434: aload_0
    //   435: getfield 2	com/go2group/synapse/rest/pub/TestCasePublicREST:issueManager	Lcom/atlassian/jira/issue/IssueManager;
    //   438: aload 11
    //   440: invokevirtual 58	com/go2group/synapse/bean/TestStepOutputBean:getTcId	()Ljava/lang/Long;
    //   443: invokeinterface 59 2 0
    //   448: astore 12
    //   450: invokestatic 60	com/atlassian/jira/component/ComponentAccessor:getJiraAuthenticationContext	()Lcom/atlassian/jira/security/JiraAuthenticationContext;
    //   453: invokeinterface 61 1 0
    //   458: aload 12
    //   460: invokeinterface 39 1 0
    //   465: invokestatic 62	com/go2group/synapse/util/PluginUtil:getAuditLogInputBean	(Lcom/atlassian/jira/user/ApplicationUser;Lcom/atlassian/jira/project/Project;)Lcom/go2group/synapse/core/audit/log/AuditLogInputBean;
    //   468: astore 13
    //   470: aload 13
    //   472: getstatic 63	com/go2group/synapse/core/audit/log/ActionEnum:CREATED	Lcom/go2group/synapse/core/audit/log/ActionEnum;
    //   475: invokevirtual 64	com/go2group/synapse/core/audit/log/AuditLogInputBean:setAction	(Lcom/go2group/synapse/core/audit/log/ActionEnum;)V
    //   478: aload 13
    //   480: getstatic 65	com/go2group/synapse/core/audit/log/ModuleEnum:TEST_CASE	Lcom/go2group/synapse/core/audit/log/ModuleEnum;
    //   483: invokevirtual 66	com/go2group/synapse/core/audit/log/AuditLogInputBean:setModule	(Lcom/go2group/synapse/core/audit/log/ModuleEnum;)V
    //   486: aload 13
    //   488: getstatic 67	com/go2group/synapse/core/audit/log/SourceEnum:REST	Lcom/go2group/synapse/core/audit/log/SourceEnum;
    //   491: invokevirtual 68	com/go2group/synapse/core/audit/log/SourceEnum:getName	()Ljava/lang/String;
    //   494: invokevirtual 69	com/go2group/synapse/core/audit/log/AuditLogInputBean:setSource	(Ljava/lang/String;)V
    //   497: aload 13
    //   499: new 70	java/util/Date
    //   502: dup
    //   503: invokespecial 71	java/util/Date:<init>	()V
    //   506: invokevirtual 72	com/go2group/synapse/core/audit/log/AuditLogInputBean:setLogTime	(Ljava/util/Date;)V
    //   509: aload 13
    //   511: new 13	java/lang/StringBuilder
    //   514: dup
    //   515: invokespecial 14	java/lang/StringBuilder:<init>	()V
    //   518: ldc 73
    //   520: invokevirtual 16	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   523: aload 11
    //   525: invokevirtual 74	com/go2group/synapse/bean/TestStepOutputBean:getStep	()Ljava/lang/String;
    //   528: bipush 50
    //   530: invokestatic 76	com/go2group/synapse/util/PluginUtil:getEllipsisString	(Ljava/lang/String;I)Ljava/lang/String;
    //   533: invokevirtual 16	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   536: ldc 77
    //   538: invokevirtual 16	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   541: aload 12
    //   543: invokeinterface 78 1 0
    //   548: invokevirtual 16	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   551: ldc 79
    //   553: invokevirtual 16	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   556: invokevirtual 17	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   559: invokevirtual 80	com/go2group/synapse/core/audit/log/AuditLogInputBean:setLog	(Ljava/lang/String;)V
    //   562: aload_0
    //   563: getfield 10	com/go2group/synapse/rest/pub/TestCasePublicREST:auditLogService	Lcom/go2group/synapse/core/audit/log/service/AuditLogService;
    //   566: aload 13
    //   568: invokeinterface 81 2 0
    //   573: aload 11
    //   575: iload 6
    //   577: invokestatic 55	java/lang/String:valueOf	(I)Ljava/lang/String;
    //   580: invokevirtual 82	com/go2group/synapse/bean/TestStepOutputBean:setSequenceNumber	(Ljava/lang/String;)V
    //   583: aload 8
    //   585: aload 11
    //   587: invokeinterface 83 2 0
    //   592: pop
    //   593: goto -220 -> 373
    //   596: aload_0
    //   597: getfield 7	com/go2group/synapse/rest/pub/TestCasePublicREST:runService	Lcom/go2group/synapse/service/TestRunService;
    //   600: aload 5
    //   602: invokeinterface 53 1 0
    //   607: ldc 84
    //   609: invokeinterface 85 3 0
    //   614: aload 8
    //   616: invokestatic 86	javax/ws/rs/core/Response:ok	(Ljava/lang/Object;)Ljavax/ws/rs/core/Response$ResponseBuilder;
    //   619: invokevirtual 87	javax/ws/rs/core/Response$ResponseBuilder:build	()Ljavax/ws/rs/core/Response;
    //   622: astore 9
    //   624: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   627: astore 10
    //   629: aload 10
    //   631: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   634: aload 9
    //   636: areturn
    //   637: astore 6
    //   639: getstatic 11	com/go2group/synapse/rest/pub/TestCasePublicREST:log	Lorg/apache/log4j/Logger;
    //   642: aload 6
    //   644: invokevirtual 89	com/go2group/synapse/core/exception/InvalidDataException:getMessage	()Ljava/lang/String;
    //   647: invokevirtual 90	org/apache/log4j/Logger:error	(Ljava/lang/Object;)V
    //   650: getstatic 11	com/go2group/synapse/rest/pub/TestCasePublicREST:log	Lorg/apache/log4j/Logger;
    //   653: aload 6
    //   655: invokevirtual 89	com/go2group/synapse/core/exception/InvalidDataException:getMessage	()Ljava/lang/String;
    //   658: aload 6
    //   660: invokevirtual 91	org/apache/log4j/Logger:debug	(Ljava/lang/Object;Ljava/lang/Throwable;)V
    //   663: aload_0
    //   664: aload 6
    //   666: invokevirtual 92	com/go2group/synapse/rest/pub/TestCasePublicREST:error	(Ljava/lang/Exception;)Ljavax/ws/rs/core/Response;
    //   669: astore 7
    //   671: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   674: astore 8
    //   676: aload 8
    //   678: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   681: aload 7
    //   683: areturn
    //   684: astore_3
    //   685: getstatic 11	com/go2group/synapse/rest/pub/TestCasePublicREST:log	Lorg/apache/log4j/Logger;
    //   688: aload_3
    //   689: invokevirtual 94	java/lang/Exception:getMessage	()Ljava/lang/String;
    //   692: invokevirtual 90	org/apache/log4j/Logger:error	(Ljava/lang/Object;)V
    //   695: getstatic 11	com/go2group/synapse/rest/pub/TestCasePublicREST:log	Lorg/apache/log4j/Logger;
    //   698: aload_3
    //   699: invokevirtual 94	java/lang/Exception:getMessage	()Ljava/lang/String;
    //   702: aload_3
    //   703: invokevirtual 91	org/apache/log4j/Logger:debug	(Ljava/lang/Object;Ljava/lang/Throwable;)V
    //   706: aload_0
    //   707: aload_3
    //   708: invokevirtual 92	com/go2group/synapse/rest/pub/TestCasePublicREST:error	(Ljava/lang/Exception;)Ljavax/ws/rs/core/Response;
    //   711: astore 4
    //   713: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   716: astore 5
    //   718: aload 5
    //   720: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   723: aload 4
    //   725: areturn
    //   726: astore 14
    //   728: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   731: astore 15
    //   733: aload 15
    //   735: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   738: aload 14
    //   740: athrow
    // Line number table:
    //   Java source line #125	-> byte code offset #0
    //   Java source line #126	-> byte code offset #9
    //   Java source line #127	-> byte code offset #34
    //   Java source line #130	-> byte code offset #59
    //   Java source line #131	-> byte code offset #63
    //   Java source line #132	-> byte code offset #69
    //   Java source line #133	-> byte code offset #74
    //   Java source line #202	-> byte code offset #82
    //   Java source line #203	-> byte code offset #87
    //   Java source line #133	-> byte code offset #92
    //   Java source line #136	-> byte code offset #95
    //   Java source line #138	-> byte code offset #107
    //   Java source line #139	-> byte code offset #112
    //   Java source line #140	-> byte code offset #137
    //   Java source line #202	-> byte code offset #155
    //   Java source line #203	-> byte code offset #160
    //   Java source line #140	-> byte code offset #165
    //   Java source line #144	-> byte code offset #168
    //   Java source line #145	-> byte code offset #175
    //   Java source line #146	-> byte code offset #183
    //   Java source line #202	-> byte code offset #200
    //   Java source line #203	-> byte code offset #205
    //   Java source line #146	-> byte code offset #210
    //   Java source line #150	-> byte code offset #213
    //   Java source line #151	-> byte code offset #222
    //   Java source line #152	-> byte code offset #230
    //   Java source line #202	-> byte code offset #247
    //   Java source line #203	-> byte code offset #252
    //   Java source line #152	-> byte code offset #257
    //   Java source line #156	-> byte code offset #260
    //   Java source line #157	-> byte code offset #277
    //   Java source line #158	-> byte code offset #285
    //   Java source line #202	-> byte code offset #302
    //   Java source line #203	-> byte code offset #307
    //   Java source line #158	-> byte code offset #312
    //   Java source line #163	-> byte code offset #315
    //   Java source line #164	-> byte code offset #318
    //   Java source line #165	-> byte code offset #331
    //   Java source line #166	-> byte code offset #344
    //   Java source line #169	-> byte code offset #356
    //   Java source line #170	-> byte code offset #365
    //   Java source line #172	-> byte code offset #395
    //   Java source line #173	-> byte code offset #407
    //   Java source line #174	-> byte code offset #420
    //   Java source line #177	-> byte code offset #434
    //   Java source line #178	-> byte code offset #450
    //   Java source line #179	-> byte code offset #470
    //   Java source line #180	-> byte code offset #478
    //   Java source line #181	-> byte code offset #486
    //   Java source line #182	-> byte code offset #497
    //   Java source line #183	-> byte code offset #509
    //   Java source line #184	-> byte code offset #562
    //   Java source line #187	-> byte code offset #573
    //   Java source line #188	-> byte code offset #583
    //   Java source line #189	-> byte code offset #593
    //   Java source line #190	-> byte code offset #596
    //   Java source line #191	-> byte code offset #614
    //   Java source line #202	-> byte code offset #624
    //   Java source line #203	-> byte code offset #629
    //   Java source line #191	-> byte code offset #634
    //   Java source line #192	-> byte code offset #637
    //   Java source line #193	-> byte code offset #639
    //   Java source line #194	-> byte code offset #650
    //   Java source line #195	-> byte code offset #663
    //   Java source line #202	-> byte code offset #671
    //   Java source line #203	-> byte code offset #676
    //   Java source line #195	-> byte code offset #681
    //   Java source line #197	-> byte code offset #684
    //   Java source line #198	-> byte code offset #685
    //   Java source line #199	-> byte code offset #695
    //   Java source line #200	-> byte code offset #706
    //   Java source line #202	-> byte code offset #713
    //   Java source line #203	-> byte code offset #718
    //   Java source line #200	-> byte code offset #723
    //   Java source line #202	-> byte code offset #726
    //   Java source line #203	-> byte code offset #733
    //   Java source line #204	-> byte code offset #738
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	741	0	this	TestCasePublicREST
    //   0	741	1	issueKey	String
    //   0	741	2	steps	List<TestStepInputBean>
    //   62	2	3	request	HttpServletRequest
    //   684	24	3	e	Exception
    //   67	657	4	canProceed	boolean
    //   80	13	5	localResponse	Response
    //   105	496	5	tcIssue	Object
    //   716	3	5	request	HttpServletRequest
    //   85	228	6	request	HttpServletRequest
    //   316	260	6	stepCount	int
    //   637	28	6	e	InvalidDataException
    //   158	3	7	request	HttpServletRequest
    //   203	3	7	request	HttpServletRequest
    //   250	3	7	request	HttpServletRequest
    //   305	3	7	request	HttpServletRequest
    //   329	353	7	testCaseDetailsBean	TestCaseDetailsOutputBean
    //   363	252	8	createdSteps	List<TestStepOutputBean>
    //   674	3	8	request	HttpServletRequest
    //   371	264	9	localObject1	Object
    //   393	32	10	step	TestStepInputBean
    //   627	3	10	request	HttpServletRequest
    //   432	154	11	createdStep	TestStepOutputBean
    //   448	94	12	testCase	Issue
    //   468	99	13	auditLogInputBean	AuditLogInputBean
    //   726	13	14	localObject2	Object
    //   731	3	15	request	HttpServletRequest
    // Exception table:
    //   from	to	target	type
    //   315	624	637	com/go2group/synapse/core/exception/InvalidDataException
    //   0	82	684	java/lang/Exception
    //   95	155	684	java/lang/Exception
    //   168	200	684	java/lang/Exception
    //   213	247	684	java/lang/Exception
    //   260	302	684	java/lang/Exception
    //   315	624	684	java/lang/Exception
    //   637	671	684	java/lang/Exception
    //   0	82	726	finally
    //   95	155	726	finally
    //   168	200	726	finally
    //   213	247	726	finally
    //   260	302	726	finally
    //   315	624	726	finally
    //   637	671	726	finally
    //   684	713	726	finally
    //   726	728	726	finally
  }
  
  /* Error */
  @Path("addAutomationSteps")
  @POST
  @XsrfProtectionExcluded
  public Response addAutomationSteps(@PathParam("issueKey") String issueKey, List<com.go2group.synapse.bean.AutomationInputBean> steps)
  {
    // Byte code:
    //   0: getstatic 11	com/go2group/synapse/rest/pub/TestCasePublicREST:log	Lorg/apache/log4j/Logger;
    //   3: invokevirtual 12	org/apache/log4j/Logger:isDebugEnabled	()Z
    //   6: ifeq +53 -> 59
    //   9: getstatic 11	com/go2group/synapse/rest/pub/TestCasePublicREST:log	Lorg/apache/log4j/Logger;
    //   12: new 13	java/lang/StringBuilder
    //   15: dup
    //   16: invokespecial 14	java/lang/StringBuilder:<init>	()V
    //   19: ldc 15
    //   21: invokevirtual 16	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   24: aload_1
    //   25: invokevirtual 16	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   28: invokevirtual 17	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   31: invokevirtual 18	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   34: getstatic 11	com/go2group/synapse/rest/pub/TestCasePublicREST:log	Lorg/apache/log4j/Logger;
    //   37: new 13	java/lang/StringBuilder
    //   40: dup
    //   41: invokespecial 14	java/lang/StringBuilder:<init>	()V
    //   44: ldc 19
    //   46: invokevirtual 16	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   49: aload_2
    //   50: invokevirtual 20	java/lang/StringBuilder:append	(Ljava/lang/Object;)Ljava/lang/StringBuilder;
    //   53: invokevirtual 17	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   56: invokevirtual 18	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   59: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   62: astore_3
    //   63: aload_3
    //   64: invokestatic 22	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:canRequestProceed	(Ljavax/servlet/http/HttpServletRequest;)Z
    //   67: istore 4
    //   69: iload 4
    //   71: ifne +24 -> 95
    //   74: aload_0
    //   75: ldc 23
    //   77: invokevirtual 24	com/go2group/synapse/rest/pub/TestCasePublicREST:rateLimitExceeded	(Ljava/lang/String;)Ljavax/ws/rs/core/Response;
    //   80: astore 5
    //   82: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   85: astore 6
    //   87: aload 6
    //   89: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   92: aload 5
    //   94: areturn
    //   95: aload_0
    //   96: getfield 2	com/go2group/synapse/rest/pub/TestCasePublicREST:issueManager	Lcom/atlassian/jira/issue/IssueManager;
    //   99: aload_1
    //   100: invokeinterface 26 2 0
    //   105: astore 5
    //   107: aload 5
    //   109: ifnonnull +59 -> 168
    //   112: getstatic 11	com/go2group/synapse/rest/pub/TestCasePublicREST:log	Lorg/apache/log4j/Logger;
    //   115: new 13	java/lang/StringBuilder
    //   118: dup
    //   119: invokespecial 14	java/lang/StringBuilder:<init>	()V
    //   122: ldc 27
    //   124: invokevirtual 16	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   127: aload_1
    //   128: invokevirtual 16	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   131: invokevirtual 17	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   134: invokevirtual 18	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   137: aload_0
    //   138: aload_0
    //   139: getfield 4	com/go2group/synapse/rest/pub/TestCasePublicREST:i18n	Lcom/atlassian/jira/util/I18nHelper;
    //   142: ldc 28
    //   144: aload_1
    //   145: invokeinterface 29 3 0
    //   150: invokevirtual 30	com/go2group/synapse/rest/pub/TestCasePublicREST:notFound	(Ljava/lang/String;)Ljavax/ws/rs/core/Response;
    //   153: astore 6
    //   155: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   158: astore 7
    //   160: aload 7
    //   162: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   165: aload 6
    //   167: areturn
    //   168: aload_0
    //   169: invokevirtual 31	com/go2group/synapse/rest/pub/TestCasePublicREST:hasValidLicense	()Z
    //   172: ifne +41 -> 213
    //   175: getstatic 11	com/go2group/synapse/rest/pub/TestCasePublicREST:log	Lorg/apache/log4j/Logger;
    //   178: ldc 32
    //   180: invokevirtual 18	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   183: aload_0
    //   184: aload_0
    //   185: getfield 4	com/go2group/synapse/rest/pub/TestCasePublicREST:i18n	Lcom/atlassian/jira/util/I18nHelper;
    //   188: ldc 33
    //   190: invokeinterface 34 2 0
    //   195: invokevirtual 35	com/go2group/synapse/rest/pub/TestCasePublicREST:forbidden	(Ljava/lang/String;)Ljavax/ws/rs/core/Response;
    //   198: astore 6
    //   200: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   203: astore 7
    //   205: aload 7
    //   207: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   210: aload 6
    //   212: areturn
    //   213: aload_0
    //   214: aload 5
    //   216: invokevirtual 36	com/go2group/synapse/rest/pub/TestCasePublicREST:hasEditPermission	(Lcom/atlassian/jira/issue/Issue;)Z
    //   219: ifne +41 -> 260
    //   222: getstatic 11	com/go2group/synapse/rest/pub/TestCasePublicREST:log	Lorg/apache/log4j/Logger;
    //   225: ldc 37
    //   227: invokevirtual 18	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   230: aload_0
    //   231: aload_0
    //   232: getfield 4	com/go2group/synapse/rest/pub/TestCasePublicREST:i18n	Lcom/atlassian/jira/util/I18nHelper;
    //   235: ldc 38
    //   237: invokeinterface 34 2 0
    //   242: invokevirtual 35	com/go2group/synapse/rest/pub/TestCasePublicREST:forbidden	(Ljava/lang/String;)Ljavax/ws/rs/core/Response;
    //   245: astore 6
    //   247: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   250: astore 7
    //   252: aload 7
    //   254: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   257: aload 6
    //   259: areturn
    //   260: aload_0
    //   261: aload 5
    //   263: invokeinterface 39 1 0
    //   268: getstatic 40	com/go2group/synapse/constant/SynapsePermission:MANAGE_TESTCASES	Lcom/go2group/synapse/constant/SynapsePermission;
    //   271: invokevirtual 41	com/go2group/synapse/rest/pub/TestCasePublicREST:hasSynapsePermission	(Lcom/atlassian/jira/project/Project;Lcom/go2group/synapse/constant/SynapsePermission;)Z
    //   274: ifne +41 -> 315
    //   277: getstatic 11	com/go2group/synapse/rest/pub/TestCasePublicREST:log	Lorg/apache/log4j/Logger;
    //   280: ldc 42
    //   282: invokevirtual 18	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   285: aload_0
    //   286: aload_0
    //   287: getfield 4	com/go2group/synapse/rest/pub/TestCasePublicREST:i18n	Lcom/atlassian/jira/util/I18nHelper;
    //   290: ldc 43
    //   292: invokeinterface 34 2 0
    //   297: invokevirtual 35	com/go2group/synapse/rest/pub/TestCasePublicREST:forbidden	(Ljava/lang/String;)Ljavax/ws/rs/core/Response;
    //   300: astore 6
    //   302: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   305: astore 7
    //   307: aload 7
    //   309: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   312: aload 6
    //   314: areturn
    //   315: aload_2
    //   316: invokeinterface 49 1 0
    //   321: astore 6
    //   323: aload 6
    //   325: invokeinterface 50 1 0
    //   330: ifeq +177 -> 507
    //   333: aload 6
    //   335: invokeinterface 51 1 0
    //   340: checkcast 95	com/go2group/synapse/bean/AutomationInputBean
    //   343: astore 7
    //   345: aload 7
    //   347: aload 5
    //   349: invokeinterface 53 1 0
    //   354: invokevirtual 96	com/go2group/synapse/bean/AutomationInputBean:setTcId	(Ljava/lang/Long;)V
    //   357: aload_0
    //   358: getfield 3	com/go2group/synapse/rest/pub/TestCasePublicREST:testStepService	Lcom/go2group/synapse/service/TestStepService;
    //   361: aload 7
    //   363: invokeinterface 97 2 0
    //   368: astore 8
    //   370: aload_0
    //   371: getfield 2	com/go2group/synapse/rest/pub/TestCasePublicREST:issueManager	Lcom/atlassian/jira/issue/IssueManager;
    //   374: aload 8
    //   376: invokevirtual 98	com/go2group/synapse/bean/AutomationStepOuputBean:getTcId	()Ljava/lang/Long;
    //   379: invokeinterface 59 2 0
    //   384: astore 9
    //   386: invokestatic 60	com/atlassian/jira/component/ComponentAccessor:getJiraAuthenticationContext	()Lcom/atlassian/jira/security/JiraAuthenticationContext;
    //   389: invokeinterface 61 1 0
    //   394: aload 9
    //   396: invokeinterface 39 1 0
    //   401: invokestatic 62	com/go2group/synapse/util/PluginUtil:getAuditLogInputBean	(Lcom/atlassian/jira/user/ApplicationUser;Lcom/atlassian/jira/project/Project;)Lcom/go2group/synapse/core/audit/log/AuditLogInputBean;
    //   404: astore 10
    //   406: aload 10
    //   408: getstatic 63	com/go2group/synapse/core/audit/log/ActionEnum:CREATED	Lcom/go2group/synapse/core/audit/log/ActionEnum;
    //   411: invokevirtual 64	com/go2group/synapse/core/audit/log/AuditLogInputBean:setAction	(Lcom/go2group/synapse/core/audit/log/ActionEnum;)V
    //   414: aload 10
    //   416: getstatic 65	com/go2group/synapse/core/audit/log/ModuleEnum:TEST_CASE	Lcom/go2group/synapse/core/audit/log/ModuleEnum;
    //   419: invokevirtual 66	com/go2group/synapse/core/audit/log/AuditLogInputBean:setModule	(Lcom/go2group/synapse/core/audit/log/ModuleEnum;)V
    //   422: aload 10
    //   424: getstatic 67	com/go2group/synapse/core/audit/log/SourceEnum:REST	Lcom/go2group/synapse/core/audit/log/SourceEnum;
    //   427: invokevirtual 68	com/go2group/synapse/core/audit/log/SourceEnum:getName	()Ljava/lang/String;
    //   430: invokevirtual 69	com/go2group/synapse/core/audit/log/AuditLogInputBean:setSource	(Ljava/lang/String;)V
    //   433: aload 10
    //   435: new 70	java/util/Date
    //   438: dup
    //   439: invokespecial 71	java/util/Date:<init>	()V
    //   442: invokevirtual 72	com/go2group/synapse/core/audit/log/AuditLogInputBean:setLogTime	(Ljava/util/Date;)V
    //   445: aload 10
    //   447: new 13	java/lang/StringBuilder
    //   450: dup
    //   451: invokespecial 14	java/lang/StringBuilder:<init>	()V
    //   454: ldc 73
    //   456: invokevirtual 16	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   459: aload 8
    //   461: invokevirtual 99	com/go2group/synapse/bean/AutomationStepOuputBean:getTriggerKey	()Ljava/lang/String;
    //   464: invokevirtual 16	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   467: ldc 77
    //   469: invokevirtual 16	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   472: aload 9
    //   474: invokeinterface 78 1 0
    //   479: invokevirtual 16	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   482: ldc 79
    //   484: invokevirtual 16	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   487: invokevirtual 17	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   490: invokevirtual 80	com/go2group/synapse/core/audit/log/AuditLogInputBean:setLog	(Ljava/lang/String;)V
    //   493: aload_0
    //   494: getfield 10	com/go2group/synapse/rest/pub/TestCasePublicREST:auditLogService	Lcom/go2group/synapse/core/audit/log/service/AuditLogService;
    //   497: aload 10
    //   499: invokeinterface 81 2 0
    //   504: goto -181 -> 323
    //   507: aload_0
    //   508: getfield 3	com/go2group/synapse/rest/pub/TestCasePublicREST:testStepService	Lcom/go2group/synapse/service/TestStepService;
    //   511: aload 5
    //   513: invokeinterface 44 2 0
    //   518: invokevirtual 100	com/go2group/synapse/bean/TestCaseDetailsOutputBean:getAutomationSteps	()Ljava/util/List;
    //   521: astore 6
    //   523: aload 6
    //   525: invokestatic 86	javax/ws/rs/core/Response:ok	(Ljava/lang/Object;)Ljavax/ws/rs/core/Response$ResponseBuilder;
    //   528: invokevirtual 87	javax/ws/rs/core/Response$ResponseBuilder:build	()Ljavax/ws/rs/core/Response;
    //   531: astore 7
    //   533: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   536: astore 8
    //   538: aload 8
    //   540: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   543: aload 7
    //   545: areturn
    //   546: astore 6
    //   548: getstatic 11	com/go2group/synapse/rest/pub/TestCasePublicREST:log	Lorg/apache/log4j/Logger;
    //   551: aload 6
    //   553: invokevirtual 89	com/go2group/synapse/core/exception/InvalidDataException:getMessage	()Ljava/lang/String;
    //   556: invokevirtual 90	org/apache/log4j/Logger:error	(Ljava/lang/Object;)V
    //   559: getstatic 11	com/go2group/synapse/rest/pub/TestCasePublicREST:log	Lorg/apache/log4j/Logger;
    //   562: aload 6
    //   564: invokevirtual 89	com/go2group/synapse/core/exception/InvalidDataException:getMessage	()Ljava/lang/String;
    //   567: aload 6
    //   569: invokevirtual 91	org/apache/log4j/Logger:debug	(Ljava/lang/Object;Ljava/lang/Throwable;)V
    //   572: aload_0
    //   573: aload 6
    //   575: invokevirtual 92	com/go2group/synapse/rest/pub/TestCasePublicREST:error	(Ljava/lang/Exception;)Ljavax/ws/rs/core/Response;
    //   578: astore 7
    //   580: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   583: astore 8
    //   585: aload 8
    //   587: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   590: aload 7
    //   592: areturn
    //   593: astore_3
    //   594: getstatic 11	com/go2group/synapse/rest/pub/TestCasePublicREST:log	Lorg/apache/log4j/Logger;
    //   597: aload_3
    //   598: invokevirtual 94	java/lang/Exception:getMessage	()Ljava/lang/String;
    //   601: invokevirtual 90	org/apache/log4j/Logger:error	(Ljava/lang/Object;)V
    //   604: getstatic 11	com/go2group/synapse/rest/pub/TestCasePublicREST:log	Lorg/apache/log4j/Logger;
    //   607: aload_3
    //   608: invokevirtual 94	java/lang/Exception:getMessage	()Ljava/lang/String;
    //   611: aload_3
    //   612: invokevirtual 91	org/apache/log4j/Logger:debug	(Ljava/lang/Object;Ljava/lang/Throwable;)V
    //   615: aload_0
    //   616: aload_3
    //   617: invokevirtual 92	com/go2group/synapse/rest/pub/TestCasePublicREST:error	(Ljava/lang/Exception;)Ljavax/ws/rs/core/Response;
    //   620: astore 4
    //   622: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   625: astore 5
    //   627: aload 5
    //   629: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   632: aload 4
    //   634: areturn
    //   635: astore 11
    //   637: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   640: astore 12
    //   642: aload 12
    //   644: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   647: aload 11
    //   649: athrow
    // Line number table:
    //   Java source line #212	-> byte code offset #0
    //   Java source line #213	-> byte code offset #9
    //   Java source line #214	-> byte code offset #34
    //   Java source line #216	-> byte code offset #59
    //   Java source line #217	-> byte code offset #63
    //   Java source line #218	-> byte code offset #69
    //   Java source line #219	-> byte code offset #74
    //   Java source line #279	-> byte code offset #82
    //   Java source line #280	-> byte code offset #87
    //   Java source line #219	-> byte code offset #92
    //   Java source line #222	-> byte code offset #95
    //   Java source line #224	-> byte code offset #107
    //   Java source line #225	-> byte code offset #112
    //   Java source line #226	-> byte code offset #137
    //   Java source line #279	-> byte code offset #155
    //   Java source line #280	-> byte code offset #160
    //   Java source line #226	-> byte code offset #165
    //   Java source line #230	-> byte code offset #168
    //   Java source line #231	-> byte code offset #175
    //   Java source line #232	-> byte code offset #183
    //   Java source line #279	-> byte code offset #200
    //   Java source line #280	-> byte code offset #205
    //   Java source line #232	-> byte code offset #210
    //   Java source line #236	-> byte code offset #213
    //   Java source line #237	-> byte code offset #222
    //   Java source line #238	-> byte code offset #230
    //   Java source line #279	-> byte code offset #247
    //   Java source line #280	-> byte code offset #252
    //   Java source line #238	-> byte code offset #257
    //   Java source line #242	-> byte code offset #260
    //   Java source line #243	-> byte code offset #277
    //   Java source line #244	-> byte code offset #285
    //   Java source line #279	-> byte code offset #302
    //   Java source line #280	-> byte code offset #307
    //   Java source line #244	-> byte code offset #312
    //   Java source line #249	-> byte code offset #315
    //   Java source line #251	-> byte code offset #345
    //   Java source line #252	-> byte code offset #357
    //   Java source line #255	-> byte code offset #370
    //   Java source line #256	-> byte code offset #386
    //   Java source line #257	-> byte code offset #406
    //   Java source line #258	-> byte code offset #414
    //   Java source line #259	-> byte code offset #422
    //   Java source line #260	-> byte code offset #433
    //   Java source line #261	-> byte code offset #445
    //   Java source line #262	-> byte code offset #493
    //   Java source line #264	-> byte code offset #504
    //   Java source line #266	-> byte code offset #507
    //   Java source line #268	-> byte code offset #523
    //   Java source line #279	-> byte code offset #533
    //   Java source line #280	-> byte code offset #538
    //   Java source line #268	-> byte code offset #543
    //   Java source line #269	-> byte code offset #546
    //   Java source line #270	-> byte code offset #548
    //   Java source line #271	-> byte code offset #559
    //   Java source line #272	-> byte code offset #572
    //   Java source line #279	-> byte code offset #580
    //   Java source line #280	-> byte code offset #585
    //   Java source line #272	-> byte code offset #590
    //   Java source line #274	-> byte code offset #593
    //   Java source line #275	-> byte code offset #594
    //   Java source line #276	-> byte code offset #604
    //   Java source line #277	-> byte code offset #615
    //   Java source line #279	-> byte code offset #622
    //   Java source line #280	-> byte code offset #627
    //   Java source line #277	-> byte code offset #632
    //   Java source line #279	-> byte code offset #635
    //   Java source line #280	-> byte code offset #642
    //   Java source line #281	-> byte code offset #647
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	650	0	this	TestCasePublicREST
    //   0	650	1	issueKey	String
    //   0	650	2	steps	List<com.go2group.synapse.bean.AutomationInputBean>
    //   62	2	3	request	HttpServletRequest
    //   593	24	3	e	Exception
    //   67	566	4	canProceed	boolean
    //   80	13	5	localResponse	Response
    //   105	407	5	tcIssue	Object
    //   625	3	5	request	HttpServletRequest
    //   85	249	6	request	HttpServletRequest
    //   521	3	6	createdSteps	List<com.go2group.synapse.bean.AutomationStepOuputBean>
    //   546	28	6	e	InvalidDataException
    //   158	3	7	request	HttpServletRequest
    //   203	3	7	request	HttpServletRequest
    //   250	3	7	request	HttpServletRequest
    //   305	3	7	request	HttpServletRequest
    //   343	248	7	step	com.go2group.synapse.bean.AutomationInputBean
    //   368	92	8	createdStep	com.go2group.synapse.bean.AutomationStepOuputBean
    //   536	3	8	request	HttpServletRequest
    //   583	3	8	request	HttpServletRequest
    //   384	89	9	testCase	Issue
    //   404	94	10	auditLogInputBean	AuditLogInputBean
    //   635	13	11	localObject1	Object
    //   640	3	12	request	HttpServletRequest
    // Exception table:
    //   from	to	target	type
    //   315	533	546	com/go2group/synapse/core/exception/InvalidDataException
    //   0	82	593	java/lang/Exception
    //   95	155	593	java/lang/Exception
    //   168	200	593	java/lang/Exception
    //   213	247	593	java/lang/Exception
    //   260	302	593	java/lang/Exception
    //   315	533	593	java/lang/Exception
    //   546	580	593	java/lang/Exception
    //   0	82	635	finally
    //   95	155	635	finally
    //   168	200	635	finally
    //   213	247	635	finally
    //   260	302	635	finally
    //   315	533	635	finally
    //   546	580	635	finally
    //   593	622	635	finally
    //   635	637	635	finally
  }
  
  @Path("convertToAutomation")
  @PUT
  public Response convertToAutomationTestCase(@PathParam("issueKey") String issueKey)
  {
    log.debug("Converting Test Case to Automation type :" + issueKey);
    try {
      HttpServletRequest request = ExecutingHttpRequest.get();
      canProceed = DefaultRestRateLimiter.canRequestProceed(request);
      HttpServletRequest request; if (!canProceed) {
        return rateLimitExceeded("Too many Requests");
      }
      
      Object tcIssue = issueManager.getIssueByKeyIgnoreCase(issueKey);
      
      if (tcIssue == null) {
        log.debug("Test Case Issue not found for key:" + issueKey);
        HttpServletRequest request; return notFound(i18n.getText("servererror.rest.testcase.notfound", issueKey));
      }
      

      if (!hasValidLicense()) {
        log.debug("Invalid license");
        HttpServletRequest request; return forbidden(i18n.getText("servererror.rest.invalid.license"));
      }
      

      if (!hasEditPermission((Issue)tcIssue)) {
        log.debug("Does not have enough edit permission on the issue");
        HttpServletRequest request; return forbidden(i18n.getText("servererror.rest.no.edit.permission"));
      }
      
      HttpServletRequest request;
      if (!hasSynapsePermission(((Issue)tcIssue).getProjectObject(), SynapsePermission.MANAGE_TESTCASES)) {
        log.debug("User does not have permission to Manage Test Cases");
        return forbidden(i18n.getText("servererror.rest.no.manage.testcase.permission"));
      }
      
      testStepService.convertTestCaseType(((Issue)tcIssue).getId(), TestRunTypeEnum.AUTOMATION);
      

      AuditLogInputBean auditLogInputBean = PluginUtil.getAuditLogInputBean(ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser(), ((Issue)tcIssue).getProjectObject());
      auditLogInputBean.setAction(ActionEnum.UPDATED);
      auditLogInputBean.setModule(ModuleEnum.TEST_CASE);
      auditLogInputBean.setSource(SourceEnum.REST.getName());
      auditLogInputBean.setLogTime(new Date());
      auditLogInputBean.setLog("Converted Test case '" + ((Issue)tcIssue).getKey() + "' to Automation through REST");
      auditLogService.createAuditLog(auditLogInputBean);
      
      HttpServletRequest request;
      return Response.ok().build();
    } catch (InvalidDataException e) { boolean canProceed;
      log.debug(e.getMessage(), e);
      log.error(e.getMessage());
      HttpServletRequest request; return Response.serverError().entity(e.getMessage()).build();
    } finally {
      HttpServletRequest request = ExecutingHttpRequest.get();
      DefaultRestRateLimiter.endCall(request);
    }
  }
  
  @Path("convertToManual/{issueKey}")
  @PUT
  public Response convertToManualTestCase(@PathParam("issueKey") String issueKey) {
    log.info("Converting Test Case to Manual type :" + issueKey);
    HttpServletRequest request = ExecutingHttpRequest.get();
    boolean canProceed = DefaultRestRateLimiter.canRequestProceed(request);
    if (!canProceed) {
      return rateLimitExceeded("Too many Requests");
    }
    try
    {
      Issue tcIssue = issueManager.getIssueByKeyIgnoreCase(issueKey);
      Response localResponse1;
      if (tcIssue == null) {
        log.debug("Test Case Issue not found for key:" + issueKey);
        return notFound(i18n.getText("servererror.rest.testcase.notfound", issueKey));
      }
      

      if (!hasValidLicense()) {
        log.debug("Invalid license");
        return forbidden(i18n.getText("servererror.rest.invalid.license"));
      }
      

      if (!hasEditPermission(tcIssue)) {
        log.debug("Does not have enough edit permission on the issue");
        return forbidden(i18n.getText("servererror.rest.no.edit.permission"));
      }
      

      if (!hasSynapsePermission(tcIssue.getProjectObject(), SynapsePermission.MANAGE_TESTCASES)) {
        log.debug("User does not have permission to Manage Test Cases");
        return forbidden(i18n.getText("servererror.rest.no.manage.testcase.permission"));
      }
      
      testStepService.convertTestCaseType(tcIssue.getId(), TestRunTypeEnum.MANUAL);
      

      auditLogInputBean = PluginUtil.getAuditLogInputBean(ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser(), tcIssue.getProjectObject());
      ((AuditLogInputBean)auditLogInputBean).setAction(ActionEnum.UPDATED);
      ((AuditLogInputBean)auditLogInputBean).setModule(ModuleEnum.TEST_CASE);
      ((AuditLogInputBean)auditLogInputBean).setSource(SourceEnum.REST.getName());
      ((AuditLogInputBean)auditLogInputBean).setLogTime(new Date());
      ((AuditLogInputBean)auditLogInputBean).setLog("Converted Test case '" + tcIssue.getKey() + "' to Manual through REST");
      auditLogService.createAuditLog((AuditLogInputBean)auditLogInputBean);
      
      return success();
    } catch (InvalidDataException e) { Object auditLogInputBean;
      log.debug(e.getMessage(), e);
      log.error(e.getMessage());
      return error(e);
    } finally {
      DefaultRestRateLimiter.endCall(request);
    }
  }
  
  /* Error */
  @Path("steps")
  @GET
  @XsrfProtectionExcluded
  public Response getSteps(@PathParam("issueKey") String issueKey)
  {
    // Byte code:
    //   0: getstatic 11	com/go2group/synapse/rest/pub/TestCasePublicREST:log	Lorg/apache/log4j/Logger;
    //   3: new 13	java/lang/StringBuilder
    //   6: dup
    //   7: invokespecial 14	java/lang/StringBuilder:<init>	()V
    //   10: ldc 115
    //   12: invokevirtual 16	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   15: aload_1
    //   16: invokevirtual 16	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   19: invokevirtual 17	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   22: invokevirtual 18	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   25: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   28: astore_2
    //   29: aload_2
    //   30: invokestatic 22	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:canRequestProceed	(Ljavax/servlet/http/HttpServletRequest;)Z
    //   33: istore_3
    //   34: iload_3
    //   35: ifne +24 -> 59
    //   38: aload_0
    //   39: ldc 23
    //   41: invokevirtual 24	com/go2group/synapse/rest/pub/TestCasePublicREST:rateLimitExceeded	(Ljava/lang/String;)Ljavax/ws/rs/core/Response;
    //   44: astore 4
    //   46: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   49: astore 5
    //   51: aload 5
    //   53: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   56: aload 4
    //   58: areturn
    //   59: aload_0
    //   60: getfield 2	com/go2group/synapse/rest/pub/TestCasePublicREST:issueManager	Lcom/atlassian/jira/issue/IssueManager;
    //   63: aload_1
    //   64: invokeinterface 26 2 0
    //   69: astore 4
    //   71: aload 4
    //   73: ifnonnull +59 -> 132
    //   76: getstatic 11	com/go2group/synapse/rest/pub/TestCasePublicREST:log	Lorg/apache/log4j/Logger;
    //   79: new 13	java/lang/StringBuilder
    //   82: dup
    //   83: invokespecial 14	java/lang/StringBuilder:<init>	()V
    //   86: ldc 27
    //   88: invokevirtual 16	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   91: aload_1
    //   92: invokevirtual 16	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   95: invokevirtual 17	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   98: invokevirtual 18	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   101: aload_0
    //   102: aload_0
    //   103: getfield 4	com/go2group/synapse/rest/pub/TestCasePublicREST:i18n	Lcom/atlassian/jira/util/I18nHelper;
    //   106: ldc 28
    //   108: aload_1
    //   109: invokeinterface 29 3 0
    //   114: invokevirtual 30	com/go2group/synapse/rest/pub/TestCasePublicREST:notFound	(Ljava/lang/String;)Ljavax/ws/rs/core/Response;
    //   117: astore 5
    //   119: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   122: astore 6
    //   124: aload 6
    //   126: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   129: aload 5
    //   131: areturn
    //   132: aload_0
    //   133: aload 4
    //   135: invokevirtual 116	com/go2group/synapse/rest/pub/TestCasePublicREST:hasViewPermission	(Lcom/atlassian/jira/issue/Issue;)Z
    //   138: ifne +58 -> 196
    //   141: getstatic 11	com/go2group/synapse/rest/pub/TestCasePublicREST:log	Lorg/apache/log4j/Logger;
    //   144: new 13	java/lang/StringBuilder
    //   147: dup
    //   148: invokespecial 14	java/lang/StringBuilder:<init>	()V
    //   151: ldc 117
    //   153: invokevirtual 16	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   156: aload_1
    //   157: invokevirtual 16	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   160: invokevirtual 17	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   163: invokevirtual 18	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   166: aload_0
    //   167: aload_0
    //   168: getfield 4	com/go2group/synapse/rest/pub/TestCasePublicREST:i18n	Lcom/atlassian/jira/util/I18nHelper;
    //   171: ldc 118
    //   173: invokeinterface 34 2 0
    //   178: invokevirtual 35	com/go2group/synapse/rest/pub/TestCasePublicREST:forbidden	(Ljava/lang/String;)Ljavax/ws/rs/core/Response;
    //   181: astore 5
    //   183: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   186: astore 6
    //   188: aload 6
    //   190: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   193: aload 5
    //   195: areturn
    //   196: aload_0
    //   197: getfield 3	com/go2group/synapse/rest/pub/TestCasePublicREST:testStepService	Lcom/go2group/synapse/service/TestStepService;
    //   200: aload 4
    //   202: invokeinterface 44 2 0
    //   207: astore 5
    //   209: aload 5
    //   211: invokevirtual 119	com/go2group/synapse/bean/TestCaseDetailsOutputBean:getType	()Lcom/go2group/synapse/enums/TestRunTypeEnum;
    //   214: invokevirtual 120	com/go2group/synapse/enums/TestRunTypeEnum:getValue	()Ljava/lang/Integer;
    //   217: getstatic 112	com/go2group/synapse/enums/TestRunTypeEnum:MANUAL	Lcom/go2group/synapse/enums/TestRunTypeEnum;
    //   220: invokevirtual 120	com/go2group/synapse/enums/TestRunTypeEnum:getValue	()Ljava/lang/Integer;
    //   223: invokevirtual 121	java/lang/Integer:equals	(Ljava/lang/Object;)Z
    //   226: ifeq +38 -> 264
    //   229: aload_0
    //   230: getfield 3	com/go2group/synapse/rest/pub/TestCasePublicREST:testStepService	Lcom/go2group/synapse/service/TestStepService;
    //   233: aload 4
    //   235: invokeinterface 44 2 0
    //   240: invokevirtual 45	com/go2group/synapse/bean/TestCaseDetailsOutputBean:getTestSteps	()Ljava/util/List;
    //   243: invokestatic 86	javax/ws/rs/core/Response:ok	(Ljava/lang/Object;)Ljavax/ws/rs/core/Response$ResponseBuilder;
    //   246: invokevirtual 87	javax/ws/rs/core/Response$ResponseBuilder:build	()Ljavax/ws/rs/core/Response;
    //   249: astore 6
    //   251: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   254: astore 7
    //   256: aload 7
    //   258: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   261: aload 6
    //   263: areturn
    //   264: aload_0
    //   265: getfield 3	com/go2group/synapse/rest/pub/TestCasePublicREST:testStepService	Lcom/go2group/synapse/service/TestStepService;
    //   268: aload 4
    //   270: invokeinterface 44 2 0
    //   275: invokevirtual 100	com/go2group/synapse/bean/TestCaseDetailsOutputBean:getAutomationSteps	()Ljava/util/List;
    //   278: invokestatic 86	javax/ws/rs/core/Response:ok	(Ljava/lang/Object;)Ljavax/ws/rs/core/Response$ResponseBuilder;
    //   281: invokevirtual 87	javax/ws/rs/core/Response$ResponseBuilder:build	()Ljavax/ws/rs/core/Response;
    //   284: astore 6
    //   286: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   289: astore 7
    //   291: aload 7
    //   293: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   296: aload 6
    //   298: areturn
    //   299: astore 5
    //   301: getstatic 11	com/go2group/synapse/rest/pub/TestCasePublicREST:log	Lorg/apache/log4j/Logger;
    //   304: aload 5
    //   306: invokevirtual 89	com/go2group/synapse/core/exception/InvalidDataException:getMessage	()Ljava/lang/String;
    //   309: invokevirtual 90	org/apache/log4j/Logger:error	(Ljava/lang/Object;)V
    //   312: getstatic 11	com/go2group/synapse/rest/pub/TestCasePublicREST:log	Lorg/apache/log4j/Logger;
    //   315: aload 5
    //   317: invokevirtual 89	com/go2group/synapse/core/exception/InvalidDataException:getMessage	()Ljava/lang/String;
    //   320: aload 5
    //   322: invokevirtual 91	org/apache/log4j/Logger:debug	(Ljava/lang/Object;Ljava/lang/Throwable;)V
    //   325: aload_0
    //   326: aload 5
    //   328: invokevirtual 92	com/go2group/synapse/rest/pub/TestCasePublicREST:error	(Ljava/lang/Exception;)Ljavax/ws/rs/core/Response;
    //   331: astore 6
    //   333: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   336: astore 7
    //   338: aload 7
    //   340: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   343: aload 6
    //   345: areturn
    //   346: astore_2
    //   347: getstatic 11	com/go2group/synapse/rest/pub/TestCasePublicREST:log	Lorg/apache/log4j/Logger;
    //   350: aload_2
    //   351: invokevirtual 94	java/lang/Exception:getMessage	()Ljava/lang/String;
    //   354: invokevirtual 90	org/apache/log4j/Logger:error	(Ljava/lang/Object;)V
    //   357: getstatic 11	com/go2group/synapse/rest/pub/TestCasePublicREST:log	Lorg/apache/log4j/Logger;
    //   360: aload_2
    //   361: invokevirtual 94	java/lang/Exception:getMessage	()Ljava/lang/String;
    //   364: aload_2
    //   365: invokevirtual 91	org/apache/log4j/Logger:debug	(Ljava/lang/Object;Ljava/lang/Throwable;)V
    //   368: aload_0
    //   369: aload_2
    //   370: invokevirtual 92	com/go2group/synapse/rest/pub/TestCasePublicREST:error	(Ljava/lang/Exception;)Ljavax/ws/rs/core/Response;
    //   373: astore_3
    //   374: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   377: astore 4
    //   379: aload 4
    //   381: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   384: aload_3
    //   385: areturn
    //   386: astore 8
    //   388: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   391: astore 9
    //   393: aload 9
    //   395: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   398: aload 8
    //   400: athrow
    // Line number table:
    //   Java source line #405	-> byte code offset #0
    //   Java source line #406	-> byte code offset #25
    //   Java source line #407	-> byte code offset #29
    //   Java source line #408	-> byte code offset #34
    //   Java source line #409	-> byte code offset #38
    //   Java source line #442	-> byte code offset #46
    //   Java source line #443	-> byte code offset #51
    //   Java source line #409	-> byte code offset #56
    //   Java source line #412	-> byte code offset #59
    //   Java source line #414	-> byte code offset #71
    //   Java source line #415	-> byte code offset #76
    //   Java source line #416	-> byte code offset #101
    //   Java source line #442	-> byte code offset #119
    //   Java source line #443	-> byte code offset #124
    //   Java source line #416	-> byte code offset #129
    //   Java source line #420	-> byte code offset #132
    //   Java source line #421	-> byte code offset #141
    //   Java source line #422	-> byte code offset #166
    //   Java source line #442	-> byte code offset #183
    //   Java source line #443	-> byte code offset #188
    //   Java source line #422	-> byte code offset #193
    //   Java source line #426	-> byte code offset #196
    //   Java source line #427	-> byte code offset #209
    //   Java source line #428	-> byte code offset #229
    //   Java source line #442	-> byte code offset #251
    //   Java source line #443	-> byte code offset #256
    //   Java source line #428	-> byte code offset #261
    //   Java source line #430	-> byte code offset #264
    //   Java source line #442	-> byte code offset #286
    //   Java source line #443	-> byte code offset #291
    //   Java source line #430	-> byte code offset #296
    //   Java source line #432	-> byte code offset #299
    //   Java source line #433	-> byte code offset #301
    //   Java source line #434	-> byte code offset #312
    //   Java source line #435	-> byte code offset #325
    //   Java source line #442	-> byte code offset #333
    //   Java source line #443	-> byte code offset #338
    //   Java source line #435	-> byte code offset #343
    //   Java source line #437	-> byte code offset #346
    //   Java source line #438	-> byte code offset #347
    //   Java source line #439	-> byte code offset #357
    //   Java source line #440	-> byte code offset #368
    //   Java source line #442	-> byte code offset #374
    //   Java source line #443	-> byte code offset #379
    //   Java source line #440	-> byte code offset #384
    //   Java source line #442	-> byte code offset #386
    //   Java source line #443	-> byte code offset #393
    //   Java source line #444	-> byte code offset #398
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	401	0	this	TestCasePublicREST
    //   0	401	1	issueKey	String
    //   28	2	2	request	HttpServletRequest
    //   346	24	2	e	Exception
    //   33	352	3	canProceed	boolean
    //   44	13	4	localResponse	Response
    //   69	200	4	tcIssue	Object
    //   377	3	4	request	HttpServletRequest
    //   49	145	5	request	HttpServletRequest
    //   207	3	5	caseDetailsOutputBean	TestCaseDetailsOutputBean
    //   299	28	5	e	InvalidDataException
    //   122	3	6	request	HttpServletRequest
    //   186	158	6	request	HttpServletRequest
    //   254	3	7	request	HttpServletRequest
    //   289	3	7	request	HttpServletRequest
    //   336	3	7	request	HttpServletRequest
    //   386	13	8	localObject1	Object
    //   391	3	9	request	HttpServletRequest
    // Exception table:
    //   from	to	target	type
    //   196	251	299	com/go2group/synapse/core/exception/InvalidDataException
    //   264	286	299	com/go2group/synapse/core/exception/InvalidDataException
    //   0	46	346	java/lang/Exception
    //   59	119	346	java/lang/Exception
    //   132	183	346	java/lang/Exception
    //   196	251	346	java/lang/Exception
    //   264	286	346	java/lang/Exception
    //   299	333	346	java/lang/Exception
    //   0	46	386	finally
    //   59	119	386	finally
    //   132	183	386	finally
    //   196	251	386	finally
    //   264	286	386	finally
    //   299	333	386	finally
    //   346	374	386	finally
    //   386	388	386	finally
  }
  
  @Path("updateStep")
  @PUT
  @XsrfProtectionExcluded
  public Response updateStep(@PathParam("issueKey") String issueKey, TestStepInputBean testStep)
  {
    try
    {
      log.debug("Updating test step :" + testStep);
      HttpServletRequest request = ExecutingHttpRequest.get();
      canProceed = DefaultRestRateLimiter.canRequestProceed(request);
      HttpServletRequest request; if (!canProceed) {
        return rateLimitExceeded("Too many Requests");
      }
      
      Object tcIssue = issueManager.getIssueByKeyIgnoreCase(issueKey);
      
      if (tcIssue == null) {
        log.debug("Test Case Issue not found for key:" + issueKey);
        HttpServletRequest request; return notFound(i18n.getText("servererror.rest.testcase.notfound", issueKey));
      }
      

      if (!hasValidLicense()) {
        log.debug("Invalid license");
        HttpServletRequest request; return forbidden(i18n.getText("servererror.rest.invalid.license"));
      }
      

      if (!hasEditPermission((Issue)tcIssue)) {
        log.debug("Does not have enough edit permission on the issue");
        HttpServletRequest request; return forbidden(i18n.getText("servererror.rest.no.edit.permission"));
      }
      

      if (!hasSynapsePermission(((Issue)tcIssue).getProjectObject(), SynapsePermission.MANAGE_TESTCASES)) {
        log.debug("User does not have permission to Manage Test Cases");
        HttpServletRequest request; return forbidden(i18n.getText("servererror.rest.no.manage.testcase.permission"));
      }
      
      if ((testStep != null) && (testStep.getID() == null)) {
        TestCaseDetailsOutputBean caseDetailsOutputBean = testStepService.getTestCaseDetails(((Issue)tcIssue).getId());
        
        List<TestStepOutputBean> testSteps = caseDetailsOutputBean.getTestSteps();
        if ((caseDetailsOutputBean.getTestSteps() != null) && (caseDetailsOutputBean.getTestSteps().size() > 0) && 
          (testStep.getSequenceNumber() != null) && (isInteger(testStep.getSequenceNumber()))) {
          TestStepOutputBean stepOutputBean = (TestStepOutputBean)testSteps.get(Integer.valueOf(testStep.getSequenceNumber()).intValue() - 1);
          testStep.setID(stepOutputBean.getID());
        }
      }
      
      testStep.setTcId(((Issue)tcIssue).getId());
      TestStepOutputBean updatedTestStep = testStepService.updateStep(testStep);
      

      Issue testCase = issueManager.getIssueObject(updatedTestStep.getTcId());
      AuditLogInputBean auditLogInputBean = PluginUtil.getAuditLogInputBean(ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser(), testCase.getProjectObject());
      auditLogInputBean.setAction(ActionEnum.UPDATED);
      auditLogInputBean.setModule(ModuleEnum.TEST_CASE);
      auditLogInputBean.setSource(SourceEnum.REST.getName());
      auditLogInputBean.setLogTime(new Date());
      auditLogInputBean.setLog("Updated Test Step '" + PluginUtil.getEllipsisString(updatedTestStep.getStep(), 50) + "' in Test Case '" + testCase.getKey() + "' through REST");
      auditLogService.createAuditLog(auditLogInputBean);
      

      runService.updateTestCaseChangeToRun(((Issue)tcIssue).getId(), "step");
      HttpServletRequest request; return Response.ok(updatedTestStep).build();
    } catch (Exception e) {
      boolean canProceed;
      log.error(e.getMessage());
      log.debug(e.getMessage(), e);
      HttpServletRequest request; return error(e);
    } finally {
      HttpServletRequest request = ExecutingHttpRequest.get();
      DefaultRestRateLimiter.endCall(request);
    }
  }
  
  @Path("updateTestReference")
  @POST
  @XsrfProtectionExcluded
  public Response updateTestReference(@PathParam("issueKey") String issueKey, String data) {
    try {
      log.debug("Updating test reference for testCase" + issueKey);
      HttpServletRequest request = ExecutingHttpRequest.get();
      canProceed = DefaultRestRateLimiter.canRequestProceed(request);
      HttpServletRequest request; if (!canProceed) {
        return rateLimitExceeded("Too many Requests");
      }
      
      Object tcIssue = issueManager.getIssueByKeyIgnoreCase(issueKey);
      
      if (tcIssue == null) {
        log.debug("Test Case Issue not found for key:" + issueKey);
        HttpServletRequest request; return notFound(i18n.getText("servererror.rest.testcase.notfound", issueKey));
      }
      

      if (!hasValidLicense()) {
        log.debug("Invalid license");
        HttpServletRequest request; return forbidden(i18n.getText("servererror.rest.invalid.license"));
      }
      

      if (!hasEditPermission((Issue)tcIssue)) {
        log.debug("Does not have enough edit permission on the issue");
        HttpServletRequest request; return forbidden(i18n.getText("servererror.rest.no.edit.permission"));
      }
      

      if (!hasSynapsePermission(((Issue)tcIssue).getProjectObject(), SynapsePermission.MANAGE_TESTCASES)) {
        log.debug("User does not have permission to Manage Test Cases");
        HttpServletRequest request; return forbidden(i18n.getText("servererror.rest.no.manage.testcase.permission"));
      }
      JSONParser parser = new JSONParser();
      String automationReference = null;
      try {
        Object obj = parser.parse(data);
        jsonObject = (JSONObject)obj;
        if (jsonObject.containsKey("automationReference"))
          automationReference = (String)jsonObject.get("automationReference");
      } catch (Exception e) {
        JSONObject jsonObject;
        log.debug(i18n.getText("errormessage.validation.noinput", "automationReference"));
        HttpServletRequest request; return Response.serverError().entity(i18n.getText("errormessage.validation.noinput", "automationReference")).build(); }
      HttpServletRequest request;
      if (automationReference != null) {
        testStepService.updateAutomationTestIdentifier(automationReference.trim(), ((Issue)tcIssue).getId());
      } else {
        log.debug(i18n.getText("errormessage.validation.noinput", "automationReference"));
        return Response.serverError().entity(i18n.getText("errormessage.validation.noinput", "automationReference")).build();
      }
      

      AuditLogInputBean auditLogInputBean = PluginUtil.getAuditLogInputBean(ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser(), ((Issue)tcIssue).getProjectObject());
      auditLogInputBean.setAction(ActionEnum.UPDATED);
      auditLogInputBean.setModule(ModuleEnum.TEST_CASE);
      auditLogInputBean.setSource(SourceEnum.REST.getName());
      auditLogInputBean.setLogTime(new Date());
      auditLogInputBean.setLog("Updated Test Reference '" + PluginUtil.getEllipsisString(automationReference, 50) + "' in Test Case '" + ((Issue)tcIssue).getKey() + "' through REST");
      auditLogService.createAuditLog(auditLogInputBean);
      
      HttpServletRequest request;
      return success();
    } catch (Exception e) {
      boolean canProceed;
      log.error(e.getMessage());
      log.debug(e.getMessage(), e);
      HttpServletRequest request; return error(e);
    } finally {
      HttpServletRequest request = ExecutingHttpRequest.get();
      DefaultRestRateLimiter.endCall(request);
    }
  }
  
  private boolean isInteger(String sequenceNumber) {
    try {
      Integer.valueOf(sequenceNumber);
    } catch (Exception e) {
      log.debug(e.getMessage(), e);
      return false;
    }
    return true;
  }
  
  @Path("deleteStep/{stepId}")
  @DELETE
  @XsrfProtectionExcluded
  public Response deleteStep(@PathParam("issueKey") String issueKey, @PathParam("stepId") int stepId) {
    try {
      log.debug("Deleting test step with id:" + stepId);
      HttpServletRequest request = ExecutingHttpRequest.get();
      canProceed = DefaultRestRateLimiter.canRequestProceed(request);
      HttpServletRequest request; if (!canProceed) {
        return rateLimitExceeded("Too many Requests");
      }
      
      Object tcIssue = issueManager.getIssueByKeyIgnoreCase(issueKey);
      
      if (tcIssue == null) {
        log.debug("Test Case Issue not found for key:" + issueKey);
        HttpServletRequest request; return notFound(i18n.getText("servererror.rest.testcase.notfound", issueKey));
      }
      

      if (!hasValidLicense()) {
        log.debug("Invalid license");
        HttpServletRequest request; return forbidden(i18n.getText("servererror.rest.invalid.license"));
      }
      

      if (!hasEditPermission((Issue)tcIssue)) {
        log.debug("Does not have enough edit permission on the issue");
        HttpServletRequest request; return forbidden(i18n.getText("servererror.rest.no.edit.permission"));
      }
      

      if (!hasSynapsePermission(((Issue)tcIssue).getProjectObject(), SynapsePermission.MANAGE_TESTCASES)) {
        log.debug("User does not have permission to Manage Test Cases");
        HttpServletRequest request; return forbidden(i18n.getText("servererror.rest.no.manage.testcase.permission"));
      }
      
      TestCaseDetailsOutputBean caseDetailsOutputBean = testStepService.getTestCaseDetails(((Issue)tcIssue).getId());
      ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
      
      List<TestStepOutputBean> testSteps = caseDetailsOutputBean.getTestSteps();
      String stepText; if (caseDetailsOutputBean.getTestSteps() != null) {
        stepText = "";
        Integer stepNo = Integer.valueOf(0);
        for (int i = 0; i < testSteps.size(); i++) {
          if (stepId == ((TestStepOutputBean)testSteps.get(i)).getID().intValue()) {
            stepNo = Integer.valueOf(i + 1);
            stepText = ((TestStepOutputBean)testSteps.get(i)).getStep();
            break;
          }
        }
        testStepService.removeStep(Integer.valueOf(stepId), stepNo, user);
        

        AuditLogInputBean auditLogInputBean = PluginUtil.getAuditLogInputBean(ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser(), ((Issue)tcIssue).getProjectObject());
        auditLogInputBean.setAction(ActionEnum.DELETED);
        auditLogInputBean.setModule(ModuleEnum.TEST_CASE);
        auditLogInputBean.setSource(SourceEnum.REST.getName());
        auditLogInputBean.setLogTime(new Date());
        auditLogInputBean.setLog("Deleted Test Step '" + PluginUtil.getEllipsisString(stepText, 50) + "' from Test Case '" + ((Issue)tcIssue).getKey() + "' through REST");
        auditLogService.createAuditLog(auditLogInputBean);
      }
      
      runService.updateTestCaseChangeToRun(((Issue)tcIssue).getId(), "step");
      HttpServletRequest request; return success();
    } catch (Exception e) {
      boolean canProceed;
      log.error(e.getMessage());
      log.debug(e.getMessage(), e);
      HttpServletRequest request; return error(e);
    } finally {
      HttpServletRequest request = ExecutingHttpRequest.get();
      DefaultRestRateLimiter.endCall(request);
    }
  }
  
  @Path("deleteStepBySequenceNo/{stepNo}")
  @DELETE
  @XsrfProtectionExcluded
  public Response deleteStepBySequenceNo(@PathParam("issueKey") String issueKey, @PathParam("stepNo") int stepNo) {
    try {
      log.debug("Deleting test step with sequence no:" + stepNo);
      HttpServletRequest request = ExecutingHttpRequest.get();
      canProceed = DefaultRestRateLimiter.canRequestProceed(request);
      HttpServletRequest request; if (!canProceed) {
        return rateLimitExceeded("Too many Requests");
      }
      
      Object tcIssue = issueManager.getIssueByKeyIgnoreCase(issueKey);
      
      if (tcIssue == null) {
        log.debug("Test Case Issue not found for key:" + issueKey);
        HttpServletRequest request; return notFound(i18n.getText("servererror.rest.testcase.notfound", issueKey));
      }
      

      if (!hasValidLicense()) {
        log.debug("Invalid license");
        HttpServletRequest request; return forbidden(i18n.getText("servererror.rest.invalid.license"));
      }
      

      if (!hasEditPermission((Issue)tcIssue)) {
        log.debug("Does not have enough edit permission on the issue");
        HttpServletRequest request; return forbidden(i18n.getText("servererror.rest.no.edit.permission"));
      }
      

      if (!hasSynapsePermission(((Issue)tcIssue).getProjectObject(), SynapsePermission.MANAGE_TESTCASES)) {
        log.debug("User does not have permission to Manage Test Cases");
        HttpServletRequest request; return forbidden(i18n.getText("servererror.rest.no.manage.testcase.permission"));
      }
      
      TestCaseDetailsOutputBean caseDetailsOutputBean = testStepService.getTestCaseDetails(((Issue)tcIssue).getId());
      ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
      
      List<TestStepOutputBean> testSteps = caseDetailsOutputBean.getTestSteps();
      TestStepOutputBean stepOutputBean; if ((testSteps != null) && (testSteps.size() > 0)) {
        stepOutputBean = (TestStepOutputBean)testSteps.get(stepNo - 1);
        String stepText = stepOutputBean.getStep();
        testStepService.removeStep(stepOutputBean.getID(), Integer.valueOf(stepNo), user);
        

        AuditLogInputBean auditLogInputBean = PluginUtil.getAuditLogInputBean(ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser(), ((Issue)tcIssue).getProjectObject());
        auditLogInputBean.setAction(ActionEnum.DELETED);
        auditLogInputBean.setModule(ModuleEnum.TEST_CASE);
        auditLogInputBean.setSource(SourceEnum.REST.getName());
        auditLogInputBean.setLogTime(new Date());
        auditLogInputBean.setLog("Deleted Test Step '" + PluginUtil.getEllipsisString(stepText, 50) + "' by sequence no. from Test Case '" + ((Issue)tcIssue).getKey() + "' through REST");
        auditLogService.createAuditLog(auditLogInputBean);
        

        runService.updateTestCaseChangeToRun(((Issue)tcIssue).getId(), "step");
      } else {
        log.debug("Step not found for the Sequence no given for the Test Case:" + issueKey);
        HttpServletRequest request; return notFound(i18n.getText("servererror.rest.testcase.step.notfound", stepNo + "", issueKey));
      }
      HttpServletRequest request;
      return success();
    } catch (Exception e) {
      boolean canProceed;
      log.error(e.getMessage());
      log.debug(e.getMessage(), e);
      HttpServletRequest request; return error(e);
    } finally {
      HttpServletRequest request = ExecutingHttpRequest.get();
      DefaultRestRateLimiter.endCall(request);
    }
  }
  
  @Path("createTestCase")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @POST
  @XsrfProtectionExcluded
  public Response createTestCase(String jsonIssueString, @Context HttpHeaders headers) {
    HttpServletRequest req = ExecutingHttpRequest.get();
    boolean canProceed = DefaultRestRateLimiter.canRequestProceed(req);
    if (!canProceed) {
      return rateLimitExceeded("Too many Requests");
    }
    

    if (!hasValidLicense()) {
      log.debug("Invalid license");
      return forbidden(i18n.getText("servererror.rest.invalid.license"));
    }
    
    JSONParser parser = new JSONParser();
    
    String key = null;
    Issue testCase = null;
    List<TestStepInputBean> steps = new ArrayList();
    HttpServletRequest request1;
    try {
      Object obj = parser.parse(jsonIssueString);
      JSONObject jsonObject = (JSONObject)obj;
      if (jsonObject.containsKey("testcasesteps")) {
        JSONArray stepsObj = (JSONArray)jsonObject.get("testcasesteps");
        

        Iterator<String> iterator = stepsObj.iterator();
        
        while (iterator.hasNext()) {
          TestStepInputBean bean = new TestStepInputBean();
          Object stepObj = iterator.next();
          JSONObject stepJSON = (JSONObject)stepObj;
          bean.setStep((String)stepJSON.get("step"));
          bean.setExpectedResult((String)stepJSON.get("expectedResult"));
          bean.setStepData((String)stepJSON.get("stepData"));
          steps.add(bean);
        }
        
        jsonObject.remove("testcasesteps");
      }
      request1 = ExecutingHttpRequest.get();
      String strippedJson = jsonObject.toJSONString();
      JSONObject toJira = (JSONObject)parser.parse(strippedJson);
      HttpClient client = HttpClientBuilder.create().build();
      
      String baseUrl = ComponentAccessor.getApplicationProperties().getString("jira.baseurl");
      if ((baseUrl != null) && (baseUrl.endsWith("/"))) {
        baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
      }
      String url = baseUrl + "/rest/api/2/issue/";
      log.debug("url : " + url);
      HttpPost request = new HttpPost(url);
      request.setConfig(getRequestConfig());
      
      request.addHeader("Content-Type", "application/json;charset=UTF-8");
      
      request.addHeader("Authorization", request1.getHeader("Authorization"));
      
      request.setEntity(new StringEntity(toJira.toJSONString(), "UTF-8"));
      HttpResponse response = client.execute(request);
      log.debug("response : " + response.getStatusLine().getStatusCode());
      
      JSONObject responseObject = (JSONObject)parser.parse(getJSONFromResponse(response));
      log.debug("Status code of JIRA create issue : " + response.getStatusLine().getStatusCode());
      if ((response.getStatusLine().getStatusCode() != 200) && (response.getStatusLine().getStatusCode() != 201)) {
        return Response.ok(responseObject).build();
      }
      
      key = (String)responseObject.get("key");
      log.debug("key : " + key);
      
      testCase = issueManager.getIssueByCurrentKey(key);
      

      AuditLogInputBean auditLogInputBean = PluginUtil.getAuditLogInputBean(ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser(), testCase.getProjectObject());
      auditLogInputBean.setAction(ActionEnum.CREATED);
      auditLogInputBean.setModule(ModuleEnum.TEST_CASE);
      auditLogInputBean.setSource(SourceEnum.REST.getName());
      auditLogInputBean.setLogTime(new Date());
      auditLogInputBean.setLog("Created Test case '" + key + "' through REST");
      auditLogService.createAuditLog(auditLogInputBean);
      


      if (!hasSynapsePermission(testCase.getProjectObject(), SynapsePermission.MANAGE_TESTCASES)) {
        log.debug("User does not have permission to Manage Test Cases");
        return forbidden(i18n.getText("servererror.rest.no.manage.testcase.permission"));
      }
      

      if (!hasEditPermission(testCase)) {
        log.debug("Does not have enough edit permission on the issue");
        return forbidden(i18n.getText("servererror.rest.no.edit.permission"));
      }
    } catch (IOException e) {
      log.debug(e.getMessage(), e);
    } catch (Exception e) {
      log.debug(e.getMessage(), e);
    }
    try {
      int stepCount;
      if ((steps != null) && (steps.size() > 0)) {
        stepCount = 0;
        createdSteps = new ArrayList();
        for (TestStepInputBean step : steps)
        {
          step.setTcId(testCase.getId());
          step.setSequenceNumber(String.valueOf(++stepCount));
          TestStepOutputBean createdStep = testStepService.addStep(step, false);
          createdStep.setSequenceNumber(String.valueOf(stepCount));
          createdSteps.add(createdStep);
          

          AuditLogInputBean auditLogInputBean = PluginUtil.getAuditLogInputBean(ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser(), testCase.getProjectObject());
          auditLogInputBean.setAction(ActionEnum.CREATED);
          auditLogInputBean.setModule(ModuleEnum.TEST_CASE);
          auditLogInputBean.setSource(SourceEnum.REST.getName());
          auditLogInputBean.setLogTime(new Date());
          auditLogInputBean.setLog("Created Test Step '" + PluginUtil.getEllipsisString(createdStep.getStep(), 50) + "' in Test Case '" + testCase.getKey() + "' through REST");
          auditLogService.createAuditLog(auditLogInputBean);
        }
      }
      
      IssueWrapperBean issueWrapperBean = new IssueWrapperBean();
      issueWrapperBean.setId(testCase.getId());
      issueWrapperBean.setKey(testCase.getKey());
      HttpServletRequest request;
      return Response.ok(issueWrapperBean).build();
    } catch (InvalidDataException e) { List<TestStepOutputBean> createdSteps;
      log.error(e.getMessage());
      log.debug(e.getMessage(), e);
      HttpServletRequest request; return error(e);
    } finally {
      HttpServletRequest request = ExecutingHttpRequest.get();
      DefaultRestRateLimiter.endCall(request);
    }
  }
  
  /* Error */
  @Path("getDefects")
  @GET
  @XsrfProtectionExcluded
  public Response getDefects(@PathParam("issueKey") String issueKey)
  {
    // Byte code:
    //   0: getstatic 11	com/go2group/synapse/rest/pub/TestCasePublicREST:log	Lorg/apache/log4j/Logger;
    //   3: new 13	java/lang/StringBuilder
    //   6: dup
    //   7: invokespecial 14	java/lang/StringBuilder:<init>	()V
    //   10: ldc 115
    //   12: invokevirtual 16	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   15: aload_1
    //   16: invokevirtual 16	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   19: invokevirtual 17	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   22: invokevirtual 18	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   25: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   28: astore_2
    //   29: aload_2
    //   30: invokestatic 22	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:canRequestProceed	(Ljavax/servlet/http/HttpServletRequest;)Z
    //   33: istore_3
    //   34: iload_3
    //   35: ifne +24 -> 59
    //   38: aload_0
    //   39: ldc 23
    //   41: invokevirtual 24	com/go2group/synapse/rest/pub/TestCasePublicREST:rateLimitExceeded	(Ljava/lang/String;)Ljavax/ws/rs/core/Response;
    //   44: astore 4
    //   46: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   49: astore 5
    //   51: aload 5
    //   53: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   56: aload 4
    //   58: areturn
    //   59: aload_0
    //   60: getfield 2	com/go2group/synapse/rest/pub/TestCasePublicREST:issueManager	Lcom/atlassian/jira/issue/IssueManager;
    //   63: aload_1
    //   64: invokeinterface 26 2 0
    //   69: astore 4
    //   71: aload 4
    //   73: ifnonnull +59 -> 132
    //   76: getstatic 11	com/go2group/synapse/rest/pub/TestCasePublicREST:log	Lorg/apache/log4j/Logger;
    //   79: new 13	java/lang/StringBuilder
    //   82: dup
    //   83: invokespecial 14	java/lang/StringBuilder:<init>	()V
    //   86: ldc 27
    //   88: invokevirtual 16	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   91: aload_1
    //   92: invokevirtual 16	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   95: invokevirtual 17	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   98: invokevirtual 18	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   101: aload_0
    //   102: aload_0
    //   103: getfield 4	com/go2group/synapse/rest/pub/TestCasePublicREST:i18n	Lcom/atlassian/jira/util/I18nHelper;
    //   106: ldc 28
    //   108: aload_1
    //   109: invokeinterface 29 3 0
    //   114: invokevirtual 30	com/go2group/synapse/rest/pub/TestCasePublicREST:notFound	(Ljava/lang/String;)Ljavax/ws/rs/core/Response;
    //   117: astore 5
    //   119: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   122: astore 6
    //   124: aload 6
    //   126: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   129: aload 5
    //   131: areturn
    //   132: aload_0
    //   133: aload 4
    //   135: invokevirtual 116	com/go2group/synapse/rest/pub/TestCasePublicREST:hasViewPermission	(Lcom/atlassian/jira/issue/Issue;)Z
    //   138: ifne +58 -> 196
    //   141: getstatic 11	com/go2group/synapse/rest/pub/TestCasePublicREST:log	Lorg/apache/log4j/Logger;
    //   144: new 13	java/lang/StringBuilder
    //   147: dup
    //   148: invokespecial 14	java/lang/StringBuilder:<init>	()V
    //   151: ldc 117
    //   153: invokevirtual 16	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   156: aload_1
    //   157: invokevirtual 16	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   160: invokevirtual 17	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   163: invokevirtual 18	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   166: aload_0
    //   167: aload_0
    //   168: getfield 4	com/go2group/synapse/rest/pub/TestCasePublicREST:i18n	Lcom/atlassian/jira/util/I18nHelper;
    //   171: ldc 118
    //   173: invokeinterface 34 2 0
    //   178: invokevirtual 35	com/go2group/synapse/rest/pub/TestCasePublicREST:forbidden	(Ljava/lang/String;)Ljavax/ws/rs/core/Response;
    //   181: astore 5
    //   183: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   186: astore 6
    //   188: aload 6
    //   190: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   193: aload 5
    //   195: areturn
    //   196: new 214	com/go2group/synapse/bean/JQLBugBean
    //   199: dup
    //   200: invokespecial 215	com/go2group/synapse/bean/JQLBugBean:<init>	()V
    //   203: astore 5
    //   205: aload_0
    //   206: getfield 2	com/go2group/synapse/rest/pub/TestCasePublicREST:issueManager	Lcom/atlassian/jira/issue/IssueManager;
    //   209: aload_1
    //   210: invokeinterface 26 2 0
    //   215: astore 6
    //   217: new 13	java/lang/StringBuilder
    //   220: dup
    //   221: invokespecial 14	java/lang/StringBuilder:<init>	()V
    //   224: ldc -40
    //   226: invokevirtual 16	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   229: aload 6
    //   231: invokeinterface 53 1 0
    //   236: invokevirtual 20	java/lang/StringBuilder:append	(Ljava/lang/Object;)Ljava/lang/StringBuilder;
    //   239: ldc -39
    //   241: invokevirtual 16	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   244: invokevirtual 17	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   247: astore 7
    //   249: aload 5
    //   251: aload 7
    //   253: invokevirtual 218	java/lang/String:toString	()Ljava/lang/String;
    //   256: invokevirtual 219	com/go2group/synapse/bean/JQLBugBean:setTestCaseIdParams	(Ljava/lang/String;)V
    //   259: aload_0
    //   260: getfield 6	com/go2group/synapse/rest/pub/TestCasePublicREST:cycleService	Lcom/go2group/synapse/service/TestCycleService;
    //   263: aload 5
    //   265: invokeinterface 220 2 0
    //   270: astore 8
    //   272: aload 8
    //   274: invokestatic 60	com/atlassian/jira/component/ComponentAccessor:getJiraAuthenticationContext	()Lcom/atlassian/jira/security/JiraAuthenticationContext;
    //   277: invokeinterface 61 1 0
    //   282: invokestatic 221	com/go2group/synapse/util/PluginUtil:getIssueWrapperWithViewPermission	(Ljava/util/Collection;Lcom/atlassian/jira/user/ApplicationUser;)Ljava/util/List;
    //   285: astore 9
    //   287: aload 9
    //   289: invokestatic 86	javax/ws/rs/core/Response:ok	(Ljava/lang/Object;)Ljavax/ws/rs/core/Response$ResponseBuilder;
    //   292: invokevirtual 87	javax/ws/rs/core/Response$ResponseBuilder:build	()Ljavax/ws/rs/core/Response;
    //   295: astore 10
    //   297: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   300: astore 11
    //   302: aload 11
    //   304: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   307: aload 10
    //   309: areturn
    //   310: astore 5
    //   312: getstatic 11	com/go2group/synapse/rest/pub/TestCasePublicREST:log	Lorg/apache/log4j/Logger;
    //   315: aload 5
    //   317: invokevirtual 89	com/go2group/synapse/core/exception/InvalidDataException:getMessage	()Ljava/lang/String;
    //   320: invokevirtual 90	org/apache/log4j/Logger:error	(Ljava/lang/Object;)V
    //   323: getstatic 11	com/go2group/synapse/rest/pub/TestCasePublicREST:log	Lorg/apache/log4j/Logger;
    //   326: aload 5
    //   328: invokevirtual 89	com/go2group/synapse/core/exception/InvalidDataException:getMessage	()Ljava/lang/String;
    //   331: aload 5
    //   333: invokevirtual 91	org/apache/log4j/Logger:debug	(Ljava/lang/Object;Ljava/lang/Throwable;)V
    //   336: aload_0
    //   337: aload 5
    //   339: invokevirtual 92	com/go2group/synapse/rest/pub/TestCasePublicREST:error	(Ljava/lang/Exception;)Ljavax/ws/rs/core/Response;
    //   342: astore 6
    //   344: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   347: astore 7
    //   349: aload 7
    //   351: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   354: aload 6
    //   356: areturn
    //   357: astore_2
    //   358: getstatic 11	com/go2group/synapse/rest/pub/TestCasePublicREST:log	Lorg/apache/log4j/Logger;
    //   361: aload_2
    //   362: invokevirtual 94	java/lang/Exception:getMessage	()Ljava/lang/String;
    //   365: invokevirtual 90	org/apache/log4j/Logger:error	(Ljava/lang/Object;)V
    //   368: getstatic 11	com/go2group/synapse/rest/pub/TestCasePublicREST:log	Lorg/apache/log4j/Logger;
    //   371: aload_2
    //   372: invokevirtual 94	java/lang/Exception:getMessage	()Ljava/lang/String;
    //   375: aload_2
    //   376: invokevirtual 91	org/apache/log4j/Logger:debug	(Ljava/lang/Object;Ljava/lang/Throwable;)V
    //   379: aload_0
    //   380: aload_2
    //   381: invokevirtual 92	com/go2group/synapse/rest/pub/TestCasePublicREST:error	(Ljava/lang/Exception;)Ljavax/ws/rs/core/Response;
    //   384: astore_3
    //   385: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   388: astore 4
    //   390: aload 4
    //   392: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   395: aload_3
    //   396: areturn
    //   397: astore 12
    //   399: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   402: astore 13
    //   404: aload 13
    //   406: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   409: aload 12
    //   411: athrow
    // Line number table:
    //   Java source line #908	-> byte code offset #0
    //   Java source line #909	-> byte code offset #25
    //   Java source line #910	-> byte code offset #29
    //   Java source line #911	-> byte code offset #34
    //   Java source line #912	-> byte code offset #38
    //   Java source line #948	-> byte code offset #46
    //   Java source line #949	-> byte code offset #51
    //   Java source line #912	-> byte code offset #56
    //   Java source line #915	-> byte code offset #59
    //   Java source line #917	-> byte code offset #71
    //   Java source line #918	-> byte code offset #76
    //   Java source line #919	-> byte code offset #101
    //   Java source line #948	-> byte code offset #119
    //   Java source line #949	-> byte code offset #124
    //   Java source line #919	-> byte code offset #129
    //   Java source line #923	-> byte code offset #132
    //   Java source line #924	-> byte code offset #141
    //   Java source line #925	-> byte code offset #166
    //   Java source line #948	-> byte code offset #183
    //   Java source line #949	-> byte code offset #188
    //   Java source line #925	-> byte code offset #193
    //   Java source line #930	-> byte code offset #196
    //   Java source line #931	-> byte code offset #205
    //   Java source line #932	-> byte code offset #217
    //   Java source line #933	-> byte code offset #249
    //   Java source line #934	-> byte code offset #259
    //   Java source line #935	-> byte code offset #272
    //   Java source line #936	-> byte code offset #287
    //   Java source line #948	-> byte code offset #297
    //   Java source line #949	-> byte code offset #302
    //   Java source line #936	-> byte code offset #307
    //   Java source line #938	-> byte code offset #310
    //   Java source line #939	-> byte code offset #312
    //   Java source line #940	-> byte code offset #323
    //   Java source line #941	-> byte code offset #336
    //   Java source line #948	-> byte code offset #344
    //   Java source line #949	-> byte code offset #349
    //   Java source line #941	-> byte code offset #354
    //   Java source line #943	-> byte code offset #357
    //   Java source line #944	-> byte code offset #358
    //   Java source line #945	-> byte code offset #368
    //   Java source line #946	-> byte code offset #379
    //   Java source line #948	-> byte code offset #385
    //   Java source line #949	-> byte code offset #390
    //   Java source line #946	-> byte code offset #395
    //   Java source line #948	-> byte code offset #397
    //   Java source line #949	-> byte code offset #404
    //   Java source line #950	-> byte code offset #409
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	412	0	this	TestCasePublicREST
    //   0	412	1	issueKey	String
    //   28	2	2	request	HttpServletRequest
    //   357	24	2	e	Exception
    //   33	363	3	canProceed	boolean
    //   44	13	4	localResponse1	Response
    //   69	65	4	tcIssue	Object
    //   388	3	4	request	HttpServletRequest
    //   49	145	5	request	HttpServletRequest
    //   203	61	5	bugBean	com.go2group.synapse.bean.JQLBugBean
    //   310	28	5	e	InvalidDataException
    //   122	3	6	request	HttpServletRequest
    //   186	3	6	request	HttpServletRequest
    //   215	140	6	issue	Issue
    //   247	5	7	testCaseIds	String
    //   347	3	7	request	HttpServletRequest
    //   270	3	8	bugsId	java.util.Set<String>
    //   285	3	9	issueWrapperBeans	List<IssueWrapperBean>
    //   295	13	10	localResponse2	Response
    //   300	3	11	request	HttpServletRequest
    //   397	13	12	localObject1	Object
    //   402	3	13	request	HttpServletRequest
    // Exception table:
    //   from	to	target	type
    //   196	297	310	com/go2group/synapse/core/exception/InvalidDataException
    //   0	46	357	java/lang/Exception
    //   59	119	357	java/lang/Exception
    //   132	183	357	java/lang/Exception
    //   196	297	357	java/lang/Exception
    //   310	344	357	java/lang/Exception
    //   0	46	397	finally
    //   59	119	397	finally
    //   132	183	397	finally
    //   196	297	397	finally
    //   310	344	397	finally
    //   357	385	397	finally
    //   397	399	397	finally
  }
  
  private RequestConfig getRequestConfig()
  {
    int CONNECTION_TIMEOUT_MS = 60000;
    return RequestConfig.custom()
      .setConnectionRequestTimeout(CONNECTION_TIMEOUT_MS)
      .setConnectTimeout(CONNECTION_TIMEOUT_MS)
      .setSocketTimeout(CONNECTION_TIMEOUT_MS)
      .build();
  }
  
  protected String getJSONFromResponse(HttpResponse response) throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
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
  
  @DELETE
  @Path("deleteLinkedRequirements")
  @XsrfProtectionExcluded
  public Response deleteLinkedRequirements(@PathParam("issueKey") String issueKey, RequirementLinkInputBean requirementLinkInputBean) {
    try {
      if (log.isDebugEnabled()) {
        log.debug("Get Linked Requirement to Test cases:" + issueKey);
      }
      HttpServletRequest request = ExecutingHttpRequest.get();
      canProceed = DefaultRestRateLimiter.canRequestProceed(request);
      HttpServletRequest request; if (!canProceed) {
        return rateLimitExceeded("Too many Requests");
      }
      
      Object tcIssue = issueManager.getIssueByKeyIgnoreCase(issueKey);
      
      if (tcIssue == null) {
        log.debug("Requirement issue not found for key:" + issueKey);
        HttpServletRequest request; return notFound(i18n.getText("servererror.rest.testcase.notfound", issueKey));
      }
      

      if (!hasValidLicense()) {
        log.debug("Invalid license");
        HttpServletRequest request; return forbidden(i18n.getText("servererror.rest.invalid.license"));
      }
      

      if (!hasEditPermission((Issue)tcIssue)) {
        log.debug("Does not have enough edit permission on the issue");
        HttpServletRequest request; return forbidden(i18n.getText("servererror.rest.no.edit.permission"));
      }
      
      HttpServletRequest request;
      if (!hasSynapsePermission(((Issue)tcIssue).getProjectObject(), SynapsePermission.MANAGE_TESTCASES)) {
        log.debug("User does not have permission to Manage Requirements");
        return forbidden(i18n.getText("servererror.rest.no.manage.testcase.permission"));
      }
      
      ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
      for (String reqKey : requirementLinkInputBean.getRequirementKeys()) {
        tc2rLinkService.delinkRequirement(((Issue)tcIssue).getKey(), reqKey, true, user);
      }
      AuditLogInputBean auditLogInputBean;
      if (requirementLinkInputBean != null)
      {
        auditLogInputBean = PluginUtil.getAuditLogInputBean(user, ((Issue)tcIssue).getProjectObject());
        auditLogInputBean.setAction(ActionEnum.DELINKED);
        auditLogInputBean.setModule(ModuleEnum.TEST_CASE);
        auditLogInputBean.setSource(SourceEnum.REST.getName());
        auditLogInputBean.setLogTime(new Date());
        auditLogInputBean.setLog("Deleted Test Case '" + ((Issue)tcIssue).getKey() + "' from Requirement(s) '" + requirementLinkInputBean.getRequirementKeys() + "' through REST");
        auditLogService.createAuditLog(auditLogInputBean);
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
  
  @GET
  @Path("linkedRequirements")
  @XsrfProtectionExcluded
  public Response getLinkedRequirements(@PathParam("issueKey") String tcKey) {
    try {
      if (log.isDebugEnabled()) {
        log.debug("Get Linked requirement to Test Cases" + tcKey);
      }
      HttpServletRequest request = ExecutingHttpRequest.get();
      canProceed = DefaultRestRateLimiter.canRequestProceed(request);
      HttpServletRequest request; if (!canProceed) {
        return rateLimitExceeded("Too many Requests");
      }
      
      Object tcIssue = issueManager.getIssueByKeyIgnoreCase(tcKey);
      
      if (tcIssue == null) {
        log.debug("Test case issue not found for key:" + tcKey);
        HttpServletRequest request; return notFound(i18n.getText("servererror.rest.testcase.notfound", tcKey));
      }
      

      if (!hasValidLicense()) {
        log.debug("Invalid license");
        HttpServletRequest request; return forbidden(i18n.getText("servererror.rest.invalid.license"));
      }
      

      if (!hasViewPermission((Issue)tcIssue)) {
        log.debug("Does not have enough view permission on the issue");
        HttpServletRequest request; return forbidden(i18n.getText("servererror.rest.no.view.permission"));
      }
      
      List<Issue> reqIds = tc2rLinkService.getRequirements((Issue)tcIssue, ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser());
      log.debug("Linked requirements : " + reqIds);
      List<IssueWrapperBean> issues = new ArrayList();
      for (Object localObject1 = reqIds.iterator(); ((Iterator)localObject1).hasNext();) { Issue issue = (Issue)((Iterator)localObject1).next();
        if ((issue != null) && (permissionUtil.hasViewPermission(issue))) {
          IssueWrapperBean issueWrapperBean = new IssueWrapperBean();
          issueWrapperBean.setId(issue.getId());
          issueWrapperBean.setKey(issue.getKey());
          issueWrapperBean.setSummary(issue.getSummary());
          issueWrapperBean.setResolution(issue.getResolution() != null);
          issues.add(issueWrapperBean);
        }
      }
      log.debug("Linked requirements wrapper : " + issues);
      HttpServletRequest request; return Response.ok(issues).build();
    } catch (Exception e) { boolean canProceed;
      log.error(e.getMessage());
      log.debug(e.getMessage(), e);
      HttpServletRequest request; return error(e);
    } finally {
      HttpServletRequest request = ExecutingHttpRequest.get();
      DefaultRestRateLimiter.endCall(request);
    }
  }
  
  @GET
  @Path("linkedTestSuites")
  @XsrfProtectionExcluded
  public Response getLinkedTestSuites(@PathParam("issueKey") String tcKey) {
    if (log.isDebugEnabled()) {
      log.debug("Get Linked test suites to Test Cases" + tcKey);
    }
    HttpServletRequest request = ExecutingHttpRequest.get();
    boolean canProceed = DefaultRestRateLimiter.canRequestProceed(request);
    if (!canProceed) {
      return rateLimitExceeded("Too many Requests");
    }
    
    Issue issue = issueManager.getIssueByKeyIgnoreCase(tcKey);
    
    if (issue == null) {
      log.debug("Test case issue not found for key:" + tcKey);
      return notFound(i18n.getText("servererror.rest.testcase.notfound", tcKey));
    }
    

    if (!hasValidLicense()) {
      log.debug("Invalid license");
      return forbidden(i18n.getText("servererror.rest.invalid.license"));
    }
    

    if (!hasViewPermission(issue)) {
      log.debug("Does not have enough view permission on the issue");
      return forbidden(i18n.getText("servererror.rest.no.view.permission"));
    }
    try
    {
      List<TestSuiteOutputBean> tcSuites = testSuiteService.getTestSuitesInTestCase(issue.getId());
      
      testCaseIssueBean = new TestCaseIssueBean();
      testCaseIssueBean.setSummary(issue.getSummary());
      testCaseIssueBean.setProjectKey(issue.getProjectObject().getKey());
      List<String> testSuites = new ArrayList();
      for (Object localObject1 = tcSuites.iterator(); ((Iterator)localObject1).hasNext();) { TestSuiteOutputBean tcSuite = (TestSuiteOutputBean)((Iterator)localObject1).next();
        testSuites.add(getTestSuiteHierarchy(tcSuite));
      }
      testCaseIssueBean.setTestSuites(testSuites);
      return Response.ok(testCaseIssueBean).build();
    } catch (Exception e) {
      TestCaseIssueBean testCaseIssueBean;
      log.error(e.getMessage());
      log.debug(e.getMessage(), e);
      return error(e);
    } finally {
      DefaultRestRateLimiter.endCall(request);
    }
  }
  
  /* Error */
  @GET
  @Path("linkedTestPlans")
  @XsrfProtectionExcluded
  public Response getLinkedTestPlans(@PathParam("issueKey") String tcKey)
  {
    // Byte code:
    //   0: getstatic 11	com/go2group/synapse/rest/pub/TestCasePublicREST:log	Lorg/apache/log4j/Logger;
    //   3: invokevirtual 12	org/apache/log4j/Logger:isDebugEnabled	()Z
    //   6: ifeq +29 -> 35
    //   9: getstatic 11	com/go2group/synapse/rest/pub/TestCasePublicREST:log	Lorg/apache/log4j/Logger;
    //   12: new 13	java/lang/StringBuilder
    //   15: dup
    //   16: invokespecial 14	java/lang/StringBuilder:<init>	()V
    //   19: ldc_w 260
    //   22: invokevirtual 16	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   25: aload_1
    //   26: invokevirtual 16	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   29: invokevirtual 17	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   32: invokevirtual 18	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   35: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   38: astore_2
    //   39: aload_2
    //   40: invokestatic 22	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:canRequestProceed	(Ljavax/servlet/http/HttpServletRequest;)Z
    //   43: istore_3
    //   44: iload_3
    //   45: ifne +24 -> 69
    //   48: aload_0
    //   49: ldc 23
    //   51: invokevirtual 24	com/go2group/synapse/rest/pub/TestCasePublicREST:rateLimitExceeded	(Ljava/lang/String;)Ljavax/ws/rs/core/Response;
    //   54: astore 4
    //   56: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   59: astore 5
    //   61: aload 5
    //   63: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   66: aload 4
    //   68: areturn
    //   69: aload_0
    //   70: getfield 2	com/go2group/synapse/rest/pub/TestCasePublicREST:issueManager	Lcom/atlassian/jira/issue/IssueManager;
    //   73: aload_1
    //   74: invokeinterface 26 2 0
    //   79: astore 4
    //   81: aload 4
    //   83: ifnonnull +59 -> 142
    //   86: getstatic 11	com/go2group/synapse/rest/pub/TestCasePublicREST:log	Lorg/apache/log4j/Logger;
    //   89: new 13	java/lang/StringBuilder
    //   92: dup
    //   93: invokespecial 14	java/lang/StringBuilder:<init>	()V
    //   96: ldc -8
    //   98: invokevirtual 16	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   101: aload_1
    //   102: invokevirtual 16	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   105: invokevirtual 17	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   108: invokevirtual 18	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   111: aload_0
    //   112: aload_0
    //   113: getfield 4	com/go2group/synapse/rest/pub/TestCasePublicREST:i18n	Lcom/atlassian/jira/util/I18nHelper;
    //   116: ldc 28
    //   118: aload_1
    //   119: invokeinterface 29 3 0
    //   124: invokevirtual 30	com/go2group/synapse/rest/pub/TestCasePublicREST:notFound	(Ljava/lang/String;)Ljavax/ws/rs/core/Response;
    //   127: astore 5
    //   129: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   132: astore 6
    //   134: aload 6
    //   136: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   139: aload 5
    //   141: areturn
    //   142: aload_0
    //   143: invokevirtual 31	com/go2group/synapse/rest/pub/TestCasePublicREST:hasValidLicense	()Z
    //   146: ifne +41 -> 187
    //   149: getstatic 11	com/go2group/synapse/rest/pub/TestCasePublicREST:log	Lorg/apache/log4j/Logger;
    //   152: ldc 32
    //   154: invokevirtual 18	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   157: aload_0
    //   158: aload_0
    //   159: getfield 4	com/go2group/synapse/rest/pub/TestCasePublicREST:i18n	Lcom/atlassian/jira/util/I18nHelper;
    //   162: ldc 33
    //   164: invokeinterface 34 2 0
    //   169: invokevirtual 35	com/go2group/synapse/rest/pub/TestCasePublicREST:forbidden	(Ljava/lang/String;)Ljavax/ws/rs/core/Response;
    //   172: astore 5
    //   174: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   177: astore 6
    //   179: aload 6
    //   181: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   184: aload 5
    //   186: areturn
    //   187: aload_0
    //   188: aload 4
    //   190: invokevirtual 116	com/go2group/synapse/rest/pub/TestCasePublicREST:hasViewPermission	(Lcom/atlassian/jira/issue/Issue;)Z
    //   193: ifne +41 -> 234
    //   196: getstatic 11	com/go2group/synapse/rest/pub/TestCasePublicREST:log	Lorg/apache/log4j/Logger;
    //   199: ldc -7
    //   201: invokevirtual 18	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   204: aload_0
    //   205: aload_0
    //   206: getfield 4	com/go2group/synapse/rest/pub/TestCasePublicREST:i18n	Lcom/atlassian/jira/util/I18nHelper;
    //   209: ldc 118
    //   211: invokeinterface 34 2 0
    //   216: invokevirtual 35	com/go2group/synapse/rest/pub/TestCasePublicREST:forbidden	(Ljava/lang/String;)Ljavax/ws/rs/core/Response;
    //   219: astore 5
    //   221: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   224: astore 6
    //   226: aload 6
    //   228: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   231: aload 5
    //   233: areturn
    //   234: aload_0
    //   235: getfield 7	com/go2group/synapse/rest/pub/TestCasePublicREST:runService	Lcom/go2group/synapse/service/TestRunService;
    //   238: aload 4
    //   240: invokeinterface 53 1 0
    //   245: invokestatic 60	com/atlassian/jira/component/ComponentAccessor:getJiraAuthenticationContext	()Lcom/atlassian/jira/security/JiraAuthenticationContext;
    //   248: invokeinterface 61 1 0
    //   253: invokeinterface 270 3 0
    //   258: astore 5
    //   260: aload_0
    //   261: getfield 9	com/go2group/synapse/rest/pub/TestCasePublicREST:tpMemberService	Lcom/go2group/synapse/service/TestPlanMemberService;
    //   264: aload 4
    //   266: invokeinterface 53 1 0
    //   271: invokeinterface 271 2 0
    //   276: astore 6
    //   278: new 272	java/util/HashMap
    //   281: dup
    //   282: invokespecial 273	java/util/HashMap:<init>	()V
    //   285: astore 7
    //   287: aload 6
    //   289: invokeinterface 49 1 0
    //   294: astore 8
    //   296: aload 8
    //   298: invokeinterface 50 1 0
    //   303: ifeq +68 -> 371
    //   306: aload 8
    //   308: invokeinterface 51 1 0
    //   313: checkcast 252	com/atlassian/jira/issue/Issue
    //   316: astore 9
    //   318: new 274	com/go2group/synapse/rest/pub/TestPlanRestBean
    //   321: dup
    //   322: invokespecial 275	com/go2group/synapse/rest/pub/TestPlanRestBean:<init>	()V
    //   325: astore 10
    //   327: aload 10
    //   329: aload 9
    //   331: invokeinterface 255 1 0
    //   336: invokevirtual 276	com/go2group/synapse/rest/pub/TestPlanRestBean:setTestPlanSummary	(Ljava/lang/String;)V
    //   339: aload 10
    //   341: aload 9
    //   343: invokeinterface 78 1 0
    //   348: invokevirtual 277	com/go2group/synapse/rest/pub/TestPlanRestBean:setTestPlanKey	(Ljava/lang/String;)V
    //   351: aload 7
    //   353: aload 9
    //   355: invokeinterface 78 1 0
    //   360: aload 10
    //   362: invokeinterface 278 3 0
    //   367: pop
    //   368: goto -72 -> 296
    //   371: aload 5
    //   373: ifnull +151 -> 524
    //   376: aload 5
    //   378: invokeinterface 46 1 0
    //   383: ifle +141 -> 524
    //   386: aload 5
    //   388: invokeinterface 49 1 0
    //   393: astore 8
    //   395: aload 8
    //   397: invokeinterface 50 1 0
    //   402: ifeq +122 -> 524
    //   405: aload 8
    //   407: invokeinterface 51 1 0
    //   412: checkcast 279	com/go2group/synapse/bean/TestRunOutputBean
    //   415: astore 9
    //   417: aload 9
    //   419: invokevirtual 280	com/go2group/synapse/bean/TestRunOutputBean:getCycle	()Lcom/go2group/synapse/bean/TestCycleOutputBean;
    //   422: astore 10
    //   424: aload_0
    //   425: getfield 2	com/go2group/synapse/rest/pub/TestCasePublicREST:issueManager	Lcom/atlassian/jira/issue/IssueManager;
    //   428: aload 10
    //   430: invokevirtual 281	com/go2group/synapse/bean/TestCycleOutputBean:getTpId	()Ljava/lang/Long;
    //   433: invokeinterface 59 2 0
    //   438: astore 11
    //   440: aload 11
    //   442: ifnull +79 -> 521
    //   445: aload 7
    //   447: aload 11
    //   449: invokeinterface 78 1 0
    //   454: invokeinterface 282 2 0
    //   459: checkcast 274	com/go2group/synapse/rest/pub/TestPlanRestBean
    //   462: astore 12
    //   464: aload 12
    //   466: ifnull +55 -> 521
    //   469: new 283	com/go2group/synapse/rest/pub/TestCycleRestBean
    //   472: dup
    //   473: invokespecial 284	com/go2group/synapse/rest/pub/TestCycleRestBean:<init>	()V
    //   476: astore 13
    //   478: aload 13
    //   480: aload 10
    //   482: invokevirtual 285	com/go2group/synapse/bean/TestCycleOutputBean:getID	()Ljava/lang/Integer;
    //   485: invokevirtual 286	com/go2group/synapse/rest/pub/TestCycleRestBean:setTestCycleId	(Ljava/lang/Integer;)V
    //   488: aload 13
    //   490: aload 10
    //   492: invokevirtual 287	com/go2group/synapse/bean/TestCycleOutputBean:getName	()Ljava/lang/String;
    //   495: invokevirtual 288	com/go2group/synapse/rest/pub/TestCycleRestBean:setTestCycleName	(Ljava/lang/String;)V
    //   498: aload 13
    //   500: aload 10
    //   502: invokevirtual 289	com/go2group/synapse/bean/TestCycleOutputBean:getStatus	()Ljava/lang/String;
    //   505: invokevirtual 290	com/go2group/synapse/rest/pub/TestCycleRestBean:setStatus	(Ljava/lang/String;)V
    //   508: aload 12
    //   510: invokevirtual 291	com/go2group/synapse/rest/pub/TestPlanRestBean:getTestCycles	()Ljava/util/List;
    //   513: aload 13
    //   515: invokeinterface 83 2 0
    //   520: pop
    //   521: goto -126 -> 395
    //   524: new 262	com/go2group/synapse/rest/pub/TestCaseIssueBean
    //   527: dup
    //   528: invokespecial 263	com/go2group/synapse/rest/pub/TestCaseIssueBean:<init>	()V
    //   531: astore 8
    //   533: aload 8
    //   535: aload 4
    //   537: invokeinterface 255 1 0
    //   542: invokevirtual 264	com/go2group/synapse/rest/pub/TestCaseIssueBean:setSummary	(Ljava/lang/String;)V
    //   545: aload 8
    //   547: aload 4
    //   549: invokeinterface 39 1 0
    //   554: invokeinterface 265 1 0
    //   559: invokevirtual 266	com/go2group/synapse/rest/pub/TestCaseIssueBean:setProjectKey	(Ljava/lang/String;)V
    //   562: aload 8
    //   564: aload 7
    //   566: invokeinterface 292 1 0
    //   571: invokevirtual 293	com/go2group/synapse/rest/pub/TestCaseIssueBean:setTestPlans	(Ljava/util/Collection;)V
    //   574: aload 8
    //   576: invokestatic 86	javax/ws/rs/core/Response:ok	(Ljava/lang/Object;)Ljavax/ws/rs/core/Response$ResponseBuilder;
    //   579: invokevirtual 87	javax/ws/rs/core/Response$ResponseBuilder:build	()Ljavax/ws/rs/core/Response;
    //   582: astore 9
    //   584: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   587: astore 10
    //   589: aload 10
    //   591: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   594: aload 9
    //   596: areturn
    //   597: astore 5
    //   599: getstatic 11	com/go2group/synapse/rest/pub/TestCasePublicREST:log	Lorg/apache/log4j/Logger;
    //   602: aload 5
    //   604: invokevirtual 94	java/lang/Exception:getMessage	()Ljava/lang/String;
    //   607: invokevirtual 90	org/apache/log4j/Logger:error	(Ljava/lang/Object;)V
    //   610: getstatic 11	com/go2group/synapse/rest/pub/TestCasePublicREST:log	Lorg/apache/log4j/Logger;
    //   613: aload 5
    //   615: invokevirtual 94	java/lang/Exception:getMessage	()Ljava/lang/String;
    //   618: aload 5
    //   620: invokevirtual 91	org/apache/log4j/Logger:debug	(Ljava/lang/Object;Ljava/lang/Throwable;)V
    //   623: aload_0
    //   624: aload 5
    //   626: invokevirtual 92	com/go2group/synapse/rest/pub/TestCasePublicREST:error	(Ljava/lang/Exception;)Ljavax/ws/rs/core/Response;
    //   629: astore 6
    //   631: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   634: astore 7
    //   636: aload 7
    //   638: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   641: aload 6
    //   643: areturn
    //   644: astore 14
    //   646: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   649: astore 15
    //   651: aload 15
    //   653: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   656: aload 14
    //   658: athrow
    // Line number table:
    //   Java source line #1158	-> byte code offset #0
    //   Java source line #1159	-> byte code offset #9
    //   Java source line #1162	-> byte code offset #35
    //   Java source line #1163	-> byte code offset #39
    //   Java source line #1164	-> byte code offset #44
    //   Java source line #1165	-> byte code offset #48
    //   Java source line #1235	-> byte code offset #56
    //   Java source line #1236	-> byte code offset #61
    //   Java source line #1165	-> byte code offset #66
    //   Java source line #1168	-> byte code offset #69
    //   Java source line #1170	-> byte code offset #81
    //   Java source line #1171	-> byte code offset #86
    //   Java source line #1172	-> byte code offset #111
    //   Java source line #1235	-> byte code offset #129
    //   Java source line #1236	-> byte code offset #134
    //   Java source line #1172	-> byte code offset #139
    //   Java source line #1176	-> byte code offset #142
    //   Java source line #1177	-> byte code offset #149
    //   Java source line #1178	-> byte code offset #157
    //   Java source line #1235	-> byte code offset #174
    //   Java source line #1236	-> byte code offset #179
    //   Java source line #1178	-> byte code offset #184
    //   Java source line #1182	-> byte code offset #187
    //   Java source line #1183	-> byte code offset #196
    //   Java source line #1184	-> byte code offset #204
    //   Java source line #1235	-> byte code offset #221
    //   Java source line #1236	-> byte code offset #226
    //   Java source line #1184	-> byte code offset #231
    //   Java source line #1188	-> byte code offset #234
    //   Java source line #1190	-> byte code offset #260
    //   Java source line #1191	-> byte code offset #278
    //   Java source line #1193	-> byte code offset #287
    //   Java source line #1194	-> byte code offset #318
    //   Java source line #1195	-> byte code offset #327
    //   Java source line #1196	-> byte code offset #339
    //   Java source line #1197	-> byte code offset #351
    //   Java source line #1198	-> byte code offset #368
    //   Java source line #1200	-> byte code offset #371
    //   Java source line #1201	-> byte code offset #386
    //   Java source line #1203	-> byte code offset #417
    //   Java source line #1205	-> byte code offset #424
    //   Java source line #1207	-> byte code offset #440
    //   Java source line #1209	-> byte code offset #445
    //   Java source line #1211	-> byte code offset #464
    //   Java source line #1212	-> byte code offset #469
    //   Java source line #1213	-> byte code offset #478
    //   Java source line #1214	-> byte code offset #488
    //   Java source line #1215	-> byte code offset #498
    //   Java source line #1216	-> byte code offset #508
    //   Java source line #1219	-> byte code offset #521
    //   Java source line #1222	-> byte code offset #524
    //   Java source line #1223	-> byte code offset #533
    //   Java source line #1224	-> byte code offset #545
    //   Java source line #1225	-> byte code offset #562
    //   Java source line #1227	-> byte code offset #574
    //   Java source line #1235	-> byte code offset #584
    //   Java source line #1236	-> byte code offset #589
    //   Java source line #1227	-> byte code offset #594
    //   Java source line #1229	-> byte code offset #597
    //   Java source line #1230	-> byte code offset #599
    //   Java source line #1231	-> byte code offset #610
    //   Java source line #1232	-> byte code offset #623
    //   Java source line #1235	-> byte code offset #631
    //   Java source line #1236	-> byte code offset #636
    //   Java source line #1232	-> byte code offset #641
    //   Java source line #1235	-> byte code offset #644
    //   Java source line #1236	-> byte code offset #651
    //   Java source line #1237	-> byte code offset #656
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	659	0	this	TestCasePublicREST
    //   0	659	1	tcKey	String
    //   38	2	2	request	HttpServletRequest
    //   43	2	3	canProceed	boolean
    //   54	13	4	localResponse	Response
    //   79	469	4	issue	Object
    //   59	173	5	request	HttpServletRequest
    //   258	129	5	tRuns	List<com.go2group.synapse.bean.TestRunOutputBean>
    //   597	28	5	e	Exception
    //   132	3	6	request	HttpServletRequest
    //   177	3	6	request	HttpServletRequest
    //   224	3	6	request	HttpServletRequest
    //   276	366	6	testPlans	List<Issue>
    //   285	280	7	testPlansMap	java.util.Map<String, TestPlanRestBean>
    //   634	3	7	request	HttpServletRequest
    //   294	112	8	localIterator	Iterator
    //   531	44	8	testCaseIssueBean	TestCaseIssueBean
    //   316	38	9	testPlan	Issue
    //   415	180	9	tRun	com.go2group.synapse.bean.TestRunOutputBean
    //   325	36	10	testPlanRestBean	TestPlanRestBean
    //   422	79	10	tCycle	com.go2group.synapse.bean.TestCycleOutputBean
    //   587	3	10	request	HttpServletRequest
    //   438	10	11	tpIssue	Issue
    //   462	47	12	testPlanRestBean	TestPlanRestBean
    //   476	38	13	cycleRestBean	TestCycleRestBean
    //   644	13	14	localObject1	Object
    //   649	3	15	request	HttpServletRequest
    // Exception table:
    //   from	to	target	type
    //   234	584	597	java/lang/Exception
    //   35	56	644	finally
    //   69	129	644	finally
    //   142	174	644	finally
    //   187	221	644	finally
    //   234	584	644	finally
    //   597	631	644	finally
    //   644	646	644	finally
  }
  
  /* Error */
  @GET
  @Path("automationReference")
  @XsrfProtectionExcluded
  public Response getTestReference(@PathParam("issueKey") String tcKey)
  {
    // Byte code:
    //   0: getstatic 11	com/go2group/synapse/rest/pub/TestCasePublicREST:log	Lorg/apache/log4j/Logger;
    //   3: invokevirtual 12	org/apache/log4j/Logger:isDebugEnabled	()Z
    //   6: ifeq +29 -> 35
    //   9: getstatic 11	com/go2group/synapse/rest/pub/TestCasePublicREST:log	Lorg/apache/log4j/Logger;
    //   12: new 13	java/lang/StringBuilder
    //   15: dup
    //   16: invokespecial 14	java/lang/StringBuilder:<init>	()V
    //   19: ldc_w 260
    //   22: invokevirtual 16	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   25: aload_1
    //   26: invokevirtual 16	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   29: invokevirtual 17	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   32: invokevirtual 18	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   35: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   38: astore_2
    //   39: aload_2
    //   40: invokestatic 22	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:canRequestProceed	(Ljavax/servlet/http/HttpServletRequest;)Z
    //   43: istore_3
    //   44: iload_3
    //   45: ifne +24 -> 69
    //   48: aload_0
    //   49: ldc 23
    //   51: invokevirtual 24	com/go2group/synapse/rest/pub/TestCasePublicREST:rateLimitExceeded	(Ljava/lang/String;)Ljavax/ws/rs/core/Response;
    //   54: astore 4
    //   56: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   59: astore 5
    //   61: aload 5
    //   63: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   66: aload 4
    //   68: areturn
    //   69: aload_0
    //   70: getfield 2	com/go2group/synapse/rest/pub/TestCasePublicREST:issueManager	Lcom/atlassian/jira/issue/IssueManager;
    //   73: aload_1
    //   74: invokeinterface 26 2 0
    //   79: astore 4
    //   81: aload 4
    //   83: ifnonnull +59 -> 142
    //   86: getstatic 11	com/go2group/synapse/rest/pub/TestCasePublicREST:log	Lorg/apache/log4j/Logger;
    //   89: new 13	java/lang/StringBuilder
    //   92: dup
    //   93: invokespecial 14	java/lang/StringBuilder:<init>	()V
    //   96: ldc -8
    //   98: invokevirtual 16	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   101: aload_1
    //   102: invokevirtual 16	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   105: invokevirtual 17	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   108: invokevirtual 18	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   111: aload_0
    //   112: aload_0
    //   113: getfield 4	com/go2group/synapse/rest/pub/TestCasePublicREST:i18n	Lcom/atlassian/jira/util/I18nHelper;
    //   116: ldc 28
    //   118: aload_1
    //   119: invokeinterface 29 3 0
    //   124: invokevirtual 30	com/go2group/synapse/rest/pub/TestCasePublicREST:notFound	(Ljava/lang/String;)Ljavax/ws/rs/core/Response;
    //   127: astore 5
    //   129: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   132: astore 6
    //   134: aload 6
    //   136: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   139: aload 5
    //   141: areturn
    //   142: aload_0
    //   143: invokevirtual 31	com/go2group/synapse/rest/pub/TestCasePublicREST:hasValidLicense	()Z
    //   146: ifne +41 -> 187
    //   149: getstatic 11	com/go2group/synapse/rest/pub/TestCasePublicREST:log	Lorg/apache/log4j/Logger;
    //   152: ldc 32
    //   154: invokevirtual 18	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   157: aload_0
    //   158: aload_0
    //   159: getfield 4	com/go2group/synapse/rest/pub/TestCasePublicREST:i18n	Lcom/atlassian/jira/util/I18nHelper;
    //   162: ldc 33
    //   164: invokeinterface 34 2 0
    //   169: invokevirtual 35	com/go2group/synapse/rest/pub/TestCasePublicREST:forbidden	(Ljava/lang/String;)Ljavax/ws/rs/core/Response;
    //   172: astore 5
    //   174: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   177: astore 6
    //   179: aload 6
    //   181: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   184: aload 5
    //   186: areturn
    //   187: aload_0
    //   188: aload 4
    //   190: invokevirtual 116	com/go2group/synapse/rest/pub/TestCasePublicREST:hasViewPermission	(Lcom/atlassian/jira/issue/Issue;)Z
    //   193: ifne +41 -> 234
    //   196: getstatic 11	com/go2group/synapse/rest/pub/TestCasePublicREST:log	Lorg/apache/log4j/Logger;
    //   199: ldc -7
    //   201: invokevirtual 18	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   204: aload_0
    //   205: aload_0
    //   206: getfield 4	com/go2group/synapse/rest/pub/TestCasePublicREST:i18n	Lcom/atlassian/jira/util/I18nHelper;
    //   209: ldc 118
    //   211: invokeinterface 34 2 0
    //   216: invokevirtual 35	com/go2group/synapse/rest/pub/TestCasePublicREST:forbidden	(Ljava/lang/String;)Ljavax/ws/rs/core/Response;
    //   219: astore 5
    //   221: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   224: astore 6
    //   226: aload 6
    //   228: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   231: aload 5
    //   233: areturn
    //   234: new 262	com/go2group/synapse/rest/pub/TestCaseIssueBean
    //   237: dup
    //   238: invokespecial 263	com/go2group/synapse/rest/pub/TestCaseIssueBean:<init>	()V
    //   241: astore 5
    //   243: aload 5
    //   245: aload 4
    //   247: invokeinterface 255 1 0
    //   252: invokevirtual 264	com/go2group/synapse/rest/pub/TestCaseIssueBean:setSummary	(Ljava/lang/String;)V
    //   255: aload 5
    //   257: aload 4
    //   259: invokeinterface 39 1 0
    //   264: invokeinterface 265 1 0
    //   269: invokevirtual 266	com/go2group/synapse/rest/pub/TestCaseIssueBean:setProjectKey	(Ljava/lang/String;)V
    //   272: aload_0
    //   273: getfield 3	com/go2group/synapse/rest/pub/TestCasePublicREST:testStepService	Lcom/go2group/synapse/service/TestStepService;
    //   276: aload 4
    //   278: invokeinterface 44 2 0
    //   283: astore 6
    //   285: aload 5
    //   287: aload 6
    //   289: invokevirtual 294	com/go2group/synapse/bean/TestCaseDetailsOutputBean:getAutomationIdentifier	()Ljava/lang/String;
    //   292: invokevirtual 295	com/go2group/synapse/rest/pub/TestCaseIssueBean:setAutomationReference	(Ljava/lang/String;)V
    //   295: aload 5
    //   297: invokestatic 86	javax/ws/rs/core/Response:ok	(Ljava/lang/Object;)Ljavax/ws/rs/core/Response$ResponseBuilder;
    //   300: invokevirtual 87	javax/ws/rs/core/Response$ResponseBuilder:build	()Ljavax/ws/rs/core/Response;
    //   303: astore 7
    //   305: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   308: astore 8
    //   310: aload 8
    //   312: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   315: aload 7
    //   317: areturn
    //   318: astore 5
    //   320: getstatic 11	com/go2group/synapse/rest/pub/TestCasePublicREST:log	Lorg/apache/log4j/Logger;
    //   323: aload 5
    //   325: invokevirtual 94	java/lang/Exception:getMessage	()Ljava/lang/String;
    //   328: invokevirtual 90	org/apache/log4j/Logger:error	(Ljava/lang/Object;)V
    //   331: getstatic 11	com/go2group/synapse/rest/pub/TestCasePublicREST:log	Lorg/apache/log4j/Logger;
    //   334: aload 5
    //   336: invokevirtual 94	java/lang/Exception:getMessage	()Ljava/lang/String;
    //   339: aload 5
    //   341: invokevirtual 91	org/apache/log4j/Logger:debug	(Ljava/lang/Object;Ljava/lang/Throwable;)V
    //   344: aload_0
    //   345: aload 5
    //   347: invokevirtual 92	com/go2group/synapse/rest/pub/TestCasePublicREST:error	(Ljava/lang/Exception;)Ljavax/ws/rs/core/Response;
    //   350: astore 6
    //   352: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   355: astore 7
    //   357: aload 7
    //   359: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   362: aload 6
    //   364: areturn
    //   365: astore 9
    //   367: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   370: astore 10
    //   372: aload 10
    //   374: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   377: aload 9
    //   379: athrow
    // Line number table:
    //   Java source line #1244	-> byte code offset #0
    //   Java source line #1245	-> byte code offset #9
    //   Java source line #1248	-> byte code offset #35
    //   Java source line #1249	-> byte code offset #39
    //   Java source line #1250	-> byte code offset #44
    //   Java source line #1251	-> byte code offset #48
    //   Java source line #1288	-> byte code offset #56
    //   Java source line #1289	-> byte code offset #61
    //   Java source line #1251	-> byte code offset #66
    //   Java source line #1254	-> byte code offset #69
    //   Java source line #1256	-> byte code offset #81
    //   Java source line #1257	-> byte code offset #86
    //   Java source line #1258	-> byte code offset #111
    //   Java source line #1288	-> byte code offset #129
    //   Java source line #1289	-> byte code offset #134
    //   Java source line #1258	-> byte code offset #139
    //   Java source line #1262	-> byte code offset #142
    //   Java source line #1263	-> byte code offset #149
    //   Java source line #1264	-> byte code offset #157
    //   Java source line #1288	-> byte code offset #174
    //   Java source line #1289	-> byte code offset #179
    //   Java source line #1264	-> byte code offset #184
    //   Java source line #1268	-> byte code offset #187
    //   Java source line #1269	-> byte code offset #196
    //   Java source line #1270	-> byte code offset #204
    //   Java source line #1288	-> byte code offset #221
    //   Java source line #1289	-> byte code offset #226
    //   Java source line #1270	-> byte code offset #231
    //   Java source line #1275	-> byte code offset #234
    //   Java source line #1276	-> byte code offset #243
    //   Java source line #1277	-> byte code offset #255
    //   Java source line #1278	-> byte code offset #272
    //   Java source line #1279	-> byte code offset #285
    //   Java source line #1280	-> byte code offset #295
    //   Java source line #1288	-> byte code offset #305
    //   Java source line #1289	-> byte code offset #310
    //   Java source line #1280	-> byte code offset #315
    //   Java source line #1282	-> byte code offset #318
    //   Java source line #1283	-> byte code offset #320
    //   Java source line #1284	-> byte code offset #331
    //   Java source line #1285	-> byte code offset #344
    //   Java source line #1288	-> byte code offset #352
    //   Java source line #1289	-> byte code offset #357
    //   Java source line #1285	-> byte code offset #362
    //   Java source line #1288	-> byte code offset #365
    //   Java source line #1289	-> byte code offset #372
    //   Java source line #1290	-> byte code offset #377
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	380	0	this	TestCasePublicREST
    //   0	380	1	tcKey	String
    //   38	2	2	request	HttpServletRequest
    //   43	2	3	canProceed	boolean
    //   54	13	4	localResponse1	Response
    //   79	198	4	issue	Object
    //   59	173	5	request	HttpServletRequest
    //   241	55	5	testCaseIssueBean	TestCaseIssueBean
    //   318	28	5	e	Exception
    //   132	3	6	request	HttpServletRequest
    //   177	3	6	request	HttpServletRequest
    //   224	3	6	request	HttpServletRequest
    //   283	80	6	caseDetailsOutputBean	TestCaseDetailsOutputBean
    //   303	13	7	localResponse2	Response
    //   355	3	7	request	HttpServletRequest
    //   308	3	8	request	HttpServletRequest
    //   365	13	9	localObject1	Object
    //   370	3	10	request	HttpServletRequest
    // Exception table:
    //   from	to	target	type
    //   234	305	318	java/lang/Exception
    //   35	56	365	finally
    //   69	129	365	finally
    //   142	174	365	finally
    //   187	221	365	finally
    //   234	305	365	finally
    //   318	352	365	finally
    //   365	367	365	finally
  }
  
  /* Error */
  @GET
  @Path("timeTracking")
  @XsrfProtectionExcluded
  public Response getTimeTracking(@PathParam("issueKey") String tcKey)
  {
    // Byte code:
    //   0: getstatic 11	com/go2group/synapse/rest/pub/TestCasePublicREST:log	Lorg/apache/log4j/Logger;
    //   3: invokevirtual 12	org/apache/log4j/Logger:isDebugEnabled	()Z
    //   6: ifeq +29 -> 35
    //   9: getstatic 11	com/go2group/synapse/rest/pub/TestCasePublicREST:log	Lorg/apache/log4j/Logger;
    //   12: new 13	java/lang/StringBuilder
    //   15: dup
    //   16: invokespecial 14	java/lang/StringBuilder:<init>	()V
    //   19: ldc_w 260
    //   22: invokevirtual 16	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   25: aload_1
    //   26: invokevirtual 16	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   29: invokevirtual 17	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   32: invokevirtual 18	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   35: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   38: astore_2
    //   39: aload_2
    //   40: invokestatic 22	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:canRequestProceed	(Ljavax/servlet/http/HttpServletRequest;)Z
    //   43: istore_3
    //   44: iload_3
    //   45: ifne +24 -> 69
    //   48: aload_0
    //   49: ldc 23
    //   51: invokevirtual 24	com/go2group/synapse/rest/pub/TestCasePublicREST:rateLimitExceeded	(Ljava/lang/String;)Ljavax/ws/rs/core/Response;
    //   54: astore 4
    //   56: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   59: astore 5
    //   61: aload 5
    //   63: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   66: aload 4
    //   68: areturn
    //   69: aload_0
    //   70: getfield 2	com/go2group/synapse/rest/pub/TestCasePublicREST:issueManager	Lcom/atlassian/jira/issue/IssueManager;
    //   73: aload_1
    //   74: invokeinterface 26 2 0
    //   79: astore 4
    //   81: aload 4
    //   83: ifnonnull +59 -> 142
    //   86: getstatic 11	com/go2group/synapse/rest/pub/TestCasePublicREST:log	Lorg/apache/log4j/Logger;
    //   89: new 13	java/lang/StringBuilder
    //   92: dup
    //   93: invokespecial 14	java/lang/StringBuilder:<init>	()V
    //   96: ldc -8
    //   98: invokevirtual 16	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   101: aload_1
    //   102: invokevirtual 16	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   105: invokevirtual 17	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   108: invokevirtual 18	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   111: aload_0
    //   112: aload_0
    //   113: getfield 4	com/go2group/synapse/rest/pub/TestCasePublicREST:i18n	Lcom/atlassian/jira/util/I18nHelper;
    //   116: ldc 28
    //   118: aload_1
    //   119: invokeinterface 29 3 0
    //   124: invokevirtual 30	com/go2group/synapse/rest/pub/TestCasePublicREST:notFound	(Ljava/lang/String;)Ljavax/ws/rs/core/Response;
    //   127: astore 5
    //   129: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   132: astore 6
    //   134: aload 6
    //   136: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   139: aload 5
    //   141: areturn
    //   142: aload_0
    //   143: invokevirtual 31	com/go2group/synapse/rest/pub/TestCasePublicREST:hasValidLicense	()Z
    //   146: ifne +41 -> 187
    //   149: getstatic 11	com/go2group/synapse/rest/pub/TestCasePublicREST:log	Lorg/apache/log4j/Logger;
    //   152: ldc 32
    //   154: invokevirtual 18	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   157: aload_0
    //   158: aload_0
    //   159: getfield 4	com/go2group/synapse/rest/pub/TestCasePublicREST:i18n	Lcom/atlassian/jira/util/I18nHelper;
    //   162: ldc 33
    //   164: invokeinterface 34 2 0
    //   169: invokevirtual 35	com/go2group/synapse/rest/pub/TestCasePublicREST:forbidden	(Ljava/lang/String;)Ljavax/ws/rs/core/Response;
    //   172: astore 5
    //   174: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   177: astore 6
    //   179: aload 6
    //   181: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   184: aload 5
    //   186: areturn
    //   187: aload_0
    //   188: aload 4
    //   190: invokevirtual 116	com/go2group/synapse/rest/pub/TestCasePublicREST:hasViewPermission	(Lcom/atlassian/jira/issue/Issue;)Z
    //   193: ifne +41 -> 234
    //   196: getstatic 11	com/go2group/synapse/rest/pub/TestCasePublicREST:log	Lorg/apache/log4j/Logger;
    //   199: ldc -7
    //   201: invokevirtual 18	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   204: aload_0
    //   205: aload_0
    //   206: getfield 4	com/go2group/synapse/rest/pub/TestCasePublicREST:i18n	Lcom/atlassian/jira/util/I18nHelper;
    //   209: ldc 118
    //   211: invokeinterface 34 2 0
    //   216: invokevirtual 35	com/go2group/synapse/rest/pub/TestCasePublicREST:forbidden	(Ljava/lang/String;)Ljavax/ws/rs/core/Response;
    //   219: astore 5
    //   221: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   224: astore 6
    //   226: aload 6
    //   228: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   231: aload 5
    //   233: areturn
    //   234: new 262	com/go2group/synapse/rest/pub/TestCaseIssueBean
    //   237: dup
    //   238: invokespecial 263	com/go2group/synapse/rest/pub/TestCaseIssueBean:<init>	()V
    //   241: astore 5
    //   243: aload 5
    //   245: aload 4
    //   247: invokeinterface 78 1 0
    //   252: invokevirtual 296	com/go2group/synapse/rest/pub/TestCaseIssueBean:setIssueKey	(Ljava/lang/String;)V
    //   255: aload 5
    //   257: aload 4
    //   259: invokeinterface 53 1 0
    //   264: invokevirtual 297	java/lang/Long:toString	()Ljava/lang/String;
    //   267: invokevirtual 298	com/go2group/synapse/rest/pub/TestCaseIssueBean:setIssueId	(Ljava/lang/String;)V
    //   270: aload_0
    //   271: getfield 3	com/go2group/synapse/rest/pub/TestCasePublicREST:testStepService	Lcom/go2group/synapse/service/TestStepService;
    //   274: aload 4
    //   276: invokeinterface 44 2 0
    //   281: astore 6
    //   283: aload 5
    //   285: aload 6
    //   287: invokevirtual 299	com/go2group/synapse/bean/TestCaseDetailsOutputBean:getFormattedEstimate	()Ljava/lang/String;
    //   290: invokevirtual 300	com/go2group/synapse/rest/pub/TestCaseIssueBean:setEstimate	(Ljava/lang/String;)V
    //   293: aload 5
    //   295: aload 6
    //   297: invokevirtual 301	com/go2group/synapse/bean/TestCaseDetailsOutputBean:getFormattedForecast	()Ljava/lang/String;
    //   300: invokevirtual 302	com/go2group/synapse/rest/pub/TestCaseIssueBean:setForecast	(Ljava/lang/String;)V
    //   303: aload 5
    //   305: invokestatic 86	javax/ws/rs/core/Response:ok	(Ljava/lang/Object;)Ljavax/ws/rs/core/Response$ResponseBuilder;
    //   308: invokevirtual 87	javax/ws/rs/core/Response$ResponseBuilder:build	()Ljavax/ws/rs/core/Response;
    //   311: astore 7
    //   313: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   316: astore 8
    //   318: aload 8
    //   320: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   323: aload 7
    //   325: areturn
    //   326: astore 5
    //   328: getstatic 11	com/go2group/synapse/rest/pub/TestCasePublicREST:log	Lorg/apache/log4j/Logger;
    //   331: aload 5
    //   333: invokevirtual 94	java/lang/Exception:getMessage	()Ljava/lang/String;
    //   336: invokevirtual 90	org/apache/log4j/Logger:error	(Ljava/lang/Object;)V
    //   339: getstatic 11	com/go2group/synapse/rest/pub/TestCasePublicREST:log	Lorg/apache/log4j/Logger;
    //   342: aload 5
    //   344: invokevirtual 94	java/lang/Exception:getMessage	()Ljava/lang/String;
    //   347: aload 5
    //   349: invokevirtual 91	org/apache/log4j/Logger:debug	(Ljava/lang/Object;Ljava/lang/Throwable;)V
    //   352: aload_0
    //   353: aload 5
    //   355: invokevirtual 92	com/go2group/synapse/rest/pub/TestCasePublicREST:error	(Ljava/lang/Exception;)Ljavax/ws/rs/core/Response;
    //   358: astore 6
    //   360: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   363: astore 7
    //   365: aload 7
    //   367: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   370: aload 6
    //   372: areturn
    //   373: astore 9
    //   375: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   378: astore 10
    //   380: aload 10
    //   382: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   385: aload 9
    //   387: athrow
    // Line number table:
    //   Java source line #1298	-> byte code offset #0
    //   Java source line #1299	-> byte code offset #9
    //   Java source line #1302	-> byte code offset #35
    //   Java source line #1303	-> byte code offset #39
    //   Java source line #1304	-> byte code offset #44
    //   Java source line #1305	-> byte code offset #48
    //   Java source line #1342	-> byte code offset #56
    //   Java source line #1343	-> byte code offset #61
    //   Java source line #1305	-> byte code offset #66
    //   Java source line #1308	-> byte code offset #69
    //   Java source line #1310	-> byte code offset #81
    //   Java source line #1311	-> byte code offset #86
    //   Java source line #1312	-> byte code offset #111
    //   Java source line #1342	-> byte code offset #129
    //   Java source line #1343	-> byte code offset #134
    //   Java source line #1312	-> byte code offset #139
    //   Java source line #1316	-> byte code offset #142
    //   Java source line #1317	-> byte code offset #149
    //   Java source line #1318	-> byte code offset #157
    //   Java source line #1342	-> byte code offset #174
    //   Java source line #1343	-> byte code offset #179
    //   Java source line #1318	-> byte code offset #184
    //   Java source line #1322	-> byte code offset #187
    //   Java source line #1323	-> byte code offset #196
    //   Java source line #1324	-> byte code offset #204
    //   Java source line #1342	-> byte code offset #221
    //   Java source line #1343	-> byte code offset #226
    //   Java source line #1324	-> byte code offset #231
    //   Java source line #1328	-> byte code offset #234
    //   Java source line #1329	-> byte code offset #243
    //   Java source line #1330	-> byte code offset #255
    //   Java source line #1331	-> byte code offset #270
    //   Java source line #1332	-> byte code offset #283
    //   Java source line #1333	-> byte code offset #293
    //   Java source line #1334	-> byte code offset #303
    //   Java source line #1342	-> byte code offset #313
    //   Java source line #1343	-> byte code offset #318
    //   Java source line #1334	-> byte code offset #323
    //   Java source line #1336	-> byte code offset #326
    //   Java source line #1337	-> byte code offset #328
    //   Java source line #1338	-> byte code offset #339
    //   Java source line #1339	-> byte code offset #352
    //   Java source line #1342	-> byte code offset #360
    //   Java source line #1343	-> byte code offset #365
    //   Java source line #1339	-> byte code offset #370
    //   Java source line #1342	-> byte code offset #373
    //   Java source line #1343	-> byte code offset #380
    //   Java source line #1344	-> byte code offset #385
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	388	0	this	TestCasePublicREST
    //   0	388	1	tcKey	String
    //   38	2	2	request	HttpServletRequest
    //   43	2	3	canProceed	boolean
    //   54	13	4	localResponse1	Response
    //   79	196	4	issue	Object
    //   59	173	5	request	HttpServletRequest
    //   241	63	5	testCaseIssueBean	TestCaseIssueBean
    //   326	28	5	e	Exception
    //   132	3	6	request	HttpServletRequest
    //   177	3	6	request	HttpServletRequest
    //   224	3	6	request	HttpServletRequest
    //   281	90	6	caseDetailsOutputBean	TestCaseDetailsOutputBean
    //   311	13	7	localResponse2	Response
    //   363	3	7	request	HttpServletRequest
    //   316	3	8	request	HttpServletRequest
    //   373	13	9	localObject1	Object
    //   378	3	10	request	HttpServletRequest
    // Exception table:
    //   from	to	target	type
    //   234	313	326	java/lang/Exception
    //   35	56	373	finally
    //   69	129	373	finally
    //   142	174	373	finally
    //   187	221	373	finally
    //   234	313	373	finally
    //   326	360	373	finally
    //   373	375	373	finally
  }
  
  private String getTestSuiteHierarchy(TestSuiteOutputBean tsuiteBean)
    throws InvalidDataException
  {
    String testSuiteHierarchy = testSuiteService.getRootPathName(tsuiteBean);
    return testSuiteHierarchy;
  }
}
