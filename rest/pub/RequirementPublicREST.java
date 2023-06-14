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
import com.go2group.synapse.bean.IssueWrapperBean;
import com.go2group.synapse.bean.RequirementTree;
import com.go2group.synapse.bean.RequirementTree.ParentTree;
import com.go2group.synapse.bean.TestCaseLinkInputBean;
import com.go2group.synapse.config.SynapseConfig;
import com.go2group.synapse.constant.SynapsePermission;
import com.go2group.synapse.core.audit.log.ActionEnum;
import com.go2group.synapse.core.audit.log.AuditLogInputBean;
import com.go2group.synapse.core.audit.log.ModuleEnum;
import com.go2group.synapse.core.audit.log.SourceEnum;
import com.go2group.synapse.core.audit.log.service.AuditLogService;
import com.go2group.synapse.core.exception.InvalidDataException;
import com.go2group.synapse.manager.PermissionUtilAbstract;
import com.go2group.synapse.service.RequirementService;
import com.go2group.synapse.service.TestCaseToRequirementLinkService;
import com.go2group.synapse.service.TestCycleService;
import com.go2group.synapse.service.impl.ratelimit.DefaultRestRateLimiter;
import com.go2group.synapse.util.PluginUtil;
import com.go2group.synapserm.bean.ReqIssueBean;
import com.go2group.synapserm.bean.ReqRestOutputBean;
import com.go2group.synapserm.bean.ReqSuiteMemberOutputBean;
import com.go2group.synapserm.bean.ReqSuiteOutputBean;
import com.go2group.synapserm.constant.PluginConstant;
import com.go2group.synapserm.service.RequirementSuiteService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;






@Path("public/requirement")
@Consumes({"application/json"})
@Produces({"application/json"})
public class RequirementPublicREST
  extends AbstractPublicREST
{
  private static final Logger log = Logger.getLogger(RequirementPublicREST.class);
  
  private final IssueManager issueManager;
  
  private final I18nHelper i18n;
  
  private final SynapseConfig synapseConfig;
  
  private final TestCaseToRequirementLinkService tc2rLinkService;
  
  private final RequirementService requirementService;
  
  private final RequirementSuiteService requirementSuiteService;
  
  private final TestCycleService cycleService;
  
  private final AuditLogService auditLogService;
  

  public RequirementPublicREST(@ComponentImport IssueManager issueManager, @ComponentImport I18nHelper i18n, PermissionUtilAbstract permissionUtil, SynapseConfig synapseConfig, TestCaseToRequirementLinkService tc2rLinkService, RequirementService requirementService, TestCycleService cycleService, RequirementSuiteService requirementSuiteService, AuditLogService auditLogService)
  {
    super(permissionUtil);
    this.issueManager = issueManager;
    this.i18n = i18n;
    this.synapseConfig = synapseConfig;
    this.tc2rLinkService = tc2rLinkService;
    this.requirementService = requirementService;
    this.cycleService = cycleService;
    this.requirementSuiteService = requirementSuiteService;
    this.auditLogService = auditLogService;
  }
  
  /* Error */
  @javax.ws.rs.POST
  @Path("{reqKey}/linkTestCase")
  @XsrfProtectionExcluded
  public Response linkTestCase(@PathParam("reqKey") String reqKey, TestCaseLinkInputBean tcLinkInput)
  {
    // Byte code:
    //   0: getstatic 10	com/go2group/synapse/rest/pub/RequirementPublicREST:log	Lorg/apache/log4j/Logger;
    //   3: invokevirtual 11	org/apache/log4j/Logger:isDebugEnabled	()Z
    //   6: ifeq +56 -> 62
    //   9: getstatic 10	com/go2group/synapse/rest/pub/RequirementPublicREST:log	Lorg/apache/log4j/Logger;
    //   12: new 12	java/lang/StringBuilder
    //   15: dup
    //   16: invokespecial 13	java/lang/StringBuilder:<init>	()V
    //   19: ldc 14
    //   21: invokevirtual 15	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   24: aload_1
    //   25: invokevirtual 15	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   28: invokevirtual 16	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   31: invokevirtual 17	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   34: getstatic 10	com/go2group/synapse/rest/pub/RequirementPublicREST:log	Lorg/apache/log4j/Logger;
    //   37: new 12	java/lang/StringBuilder
    //   40: dup
    //   41: invokespecial 13	java/lang/StringBuilder:<init>	()V
    //   44: ldc 18
    //   46: invokevirtual 15	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   49: aload_2
    //   50: invokevirtual 19	com/go2group/synapse/bean/TestCaseLinkInputBean:getTestCaseKeys	()Ljava/util/List;
    //   53: invokevirtual 20	java/lang/StringBuilder:append	(Ljava/lang/Object;)Ljava/lang/StringBuilder;
    //   56: invokevirtual 16	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   59: invokevirtual 17	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   62: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   65: astore_3
    //   66: aload_3
    //   67: invokestatic 22	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:canRequestProceed	(Ljavax/servlet/http/HttpServletRequest;)Z
    //   70: istore 4
    //   72: iload 4
    //   74: ifne +24 -> 98
    //   77: aload_0
    //   78: ldc 23
    //   80: invokevirtual 24	com/go2group/synapse/rest/pub/RequirementPublicREST:rateLimitExceeded	(Ljava/lang/String;)Ljavax/ws/rs/core/Response;
    //   83: astore 5
    //   85: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   88: astore 6
    //   90: aload 6
    //   92: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   95: aload 5
    //   97: areturn
    //   98: aload_0
    //   99: getfield 2	com/go2group/synapse/rest/pub/RequirementPublicREST:issueManager	Lcom/atlassian/jira/issue/IssueManager;
    //   102: aload_1
    //   103: invokeinterface 26 2 0
    //   108: astore 5
    //   110: aload 5
    //   112: ifnonnull +59 -> 171
    //   115: getstatic 10	com/go2group/synapse/rest/pub/RequirementPublicREST:log	Lorg/apache/log4j/Logger;
    //   118: new 12	java/lang/StringBuilder
    //   121: dup
    //   122: invokespecial 13	java/lang/StringBuilder:<init>	()V
    //   125: ldc 27
    //   127: invokevirtual 15	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   130: aload_1
    //   131: invokevirtual 15	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   134: invokevirtual 16	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   137: invokevirtual 17	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   140: aload_0
    //   141: aload_0
    //   142: getfield 3	com/go2group/synapse/rest/pub/RequirementPublicREST:i18n	Lcom/atlassian/jira/util/I18nHelper;
    //   145: ldc 28
    //   147: aload_1
    //   148: invokeinterface 29 3 0
    //   153: invokevirtual 30	com/go2group/synapse/rest/pub/RequirementPublicREST:notFound	(Ljava/lang/String;)Ljavax/ws/rs/core/Response;
    //   156: astore 6
    //   158: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   161: astore 7
    //   163: aload 7
    //   165: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   168: aload 6
    //   170: areturn
    //   171: aload_0
    //   172: invokevirtual 31	com/go2group/synapse/rest/pub/RequirementPublicREST:hasValidLicense	()Z
    //   175: ifne +41 -> 216
    //   178: getstatic 10	com/go2group/synapse/rest/pub/RequirementPublicREST:log	Lorg/apache/log4j/Logger;
    //   181: ldc 32
    //   183: invokevirtual 17	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   186: aload_0
    //   187: aload_0
    //   188: getfield 3	com/go2group/synapse/rest/pub/RequirementPublicREST:i18n	Lcom/atlassian/jira/util/I18nHelper;
    //   191: ldc 33
    //   193: invokeinterface 34 2 0
    //   198: invokevirtual 35	com/go2group/synapse/rest/pub/RequirementPublicREST:forbidden	(Ljava/lang/String;)Ljavax/ws/rs/core/Response;
    //   201: astore 6
    //   203: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   206: astore 7
    //   208: aload 7
    //   210: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   213: aload 6
    //   215: areturn
    //   216: aload_0
    //   217: aload 5
    //   219: invokevirtual 36	com/go2group/synapse/rest/pub/RequirementPublicREST:hasEditPermission	(Lcom/atlassian/jira/issue/Issue;)Z
    //   222: ifne +41 -> 263
    //   225: getstatic 10	com/go2group/synapse/rest/pub/RequirementPublicREST:log	Lorg/apache/log4j/Logger;
    //   228: ldc 37
    //   230: invokevirtual 17	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   233: aload_0
    //   234: aload_0
    //   235: getfield 3	com/go2group/synapse/rest/pub/RequirementPublicREST:i18n	Lcom/atlassian/jira/util/I18nHelper;
    //   238: ldc 38
    //   240: invokeinterface 34 2 0
    //   245: invokevirtual 35	com/go2group/synapse/rest/pub/RequirementPublicREST:forbidden	(Ljava/lang/String;)Ljavax/ws/rs/core/Response;
    //   248: astore 6
    //   250: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   253: astore 7
    //   255: aload 7
    //   257: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   260: aload 6
    //   262: areturn
    //   263: aload_0
    //   264: aload 5
    //   266: invokeinterface 39 1 0
    //   271: getstatic 40	com/go2group/synapse/constant/SynapsePermission:MANAGE_REQUIREMENTS	Lcom/go2group/synapse/constant/SynapsePermission;
    //   274: invokevirtual 41	com/go2group/synapse/rest/pub/RequirementPublicREST:hasSynapsePermission	(Lcom/atlassian/jira/project/Project;Lcom/go2group/synapse/constant/SynapsePermission;)Z
    //   277: ifne +41 -> 318
    //   280: getstatic 10	com/go2group/synapse/rest/pub/RequirementPublicREST:log	Lorg/apache/log4j/Logger;
    //   283: ldc 42
    //   285: invokevirtual 17	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   288: aload_0
    //   289: aload_0
    //   290: getfield 3	com/go2group/synapse/rest/pub/RequirementPublicREST:i18n	Lcom/atlassian/jira/util/I18nHelper;
    //   293: ldc 43
    //   295: invokeinterface 34 2 0
    //   300: invokevirtual 35	com/go2group/synapse/rest/pub/RequirementPublicREST:forbidden	(Ljava/lang/String;)Ljavax/ws/rs/core/Response;
    //   303: astore 6
    //   305: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   308: astore 7
    //   310: aload 7
    //   312: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   315: aload 6
    //   317: areturn
    //   318: aload_2
    //   319: invokevirtual 19	com/go2group/synapse/bean/TestCaseLinkInputBean:getTestCaseKeys	()Ljava/util/List;
    //   322: ifnull +15 -> 337
    //   325: aload_2
    //   326: invokevirtual 19	com/go2group/synapse/bean/TestCaseLinkInputBean:getTestCaseKeys	()Ljava/util/List;
    //   329: invokeinterface 44 1 0
    //   334: ifne +41 -> 375
    //   337: getstatic 10	com/go2group/synapse/rest/pub/RequirementPublicREST:log	Lorg/apache/log4j/Logger;
    //   340: ldc 45
    //   342: invokevirtual 17	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   345: new 46	com/go2group/synapse/core/exception/InvalidDataException
    //   348: dup
    //   349: aload_0
    //   350: getfield 3	com/go2group/synapse/rest/pub/RequirementPublicREST:i18n	Lcom/atlassian/jira/util/I18nHelper;
    //   353: ldc 47
    //   355: aload_0
    //   356: getfield 4	com/go2group/synapse/rest/pub/RequirementPublicREST:synapseConfig	Lcom/go2group/synapse/config/SynapseConfig;
    //   359: ldc 49
    //   361: invokeinterface 50 2 0
    //   366: invokeinterface 29 3 0
    //   371: invokespecial 51	com/go2group/synapse/core/exception/InvalidDataException:<init>	(Ljava/lang/String;)V
    //   374: athrow
    //   375: new 52	java/util/ArrayList
    //   378: dup
    //   379: invokespecial 53	java/util/ArrayList:<init>	()V
    //   382: astore 6
    //   384: aload 6
    //   386: aload_1
    //   387: invokeinterface 54 2 0
    //   392: pop
    //   393: aload_2
    //   394: invokevirtual 19	com/go2group/synapse/bean/TestCaseLinkInputBean:getTestCaseKeys	()Ljava/util/List;
    //   397: invokeinterface 55 1 0
    //   402: astore 7
    //   404: aload 7
    //   406: invokeinterface 56 1 0
    //   411: ifeq +111 -> 522
    //   414: aload 7
    //   416: invokeinterface 57 1 0
    //   421: checkcast 58	java/lang/String
    //   424: astore 8
    //   426: aload_0
    //   427: getfield 2	com/go2group/synapse/rest/pub/RequirementPublicREST:issueManager	Lcom/atlassian/jira/issue/IssueManager;
    //   430: aload 8
    //   432: invokeinterface 26 2 0
    //   437: astore 9
    //   439: aload 9
    //   441: ifnull +31 -> 472
    //   444: aload_0
    //   445: getfield 5	com/go2group/synapse/rest/pub/RequirementPublicREST:tc2rLinkService	Lcom/go2group/synapse/service/TestCaseToRequirementLinkService;
    //   448: aload 8
    //   450: aload 6
    //   452: invokestatic 59	com/atlassian/jira/component/ComponentAccessor:getJiraAuthenticationContext	()Lcom/atlassian/jira/security/JiraAuthenticationContext;
    //   455: invokeinterface 60 1 0
    //   460: ldc 62
    //   462: iconst_1
    //   463: invokeinterface 63 6 0
    //   468: pop
    //   469: goto +50 -> 519
    //   472: getstatic 10	com/go2group/synapse/rest/pub/RequirementPublicREST:log	Lorg/apache/log4j/Logger;
    //   475: new 12	java/lang/StringBuilder
    //   478: dup
    //   479: invokespecial 13	java/lang/StringBuilder:<init>	()V
    //   482: ldc 64
    //   484: invokevirtual 15	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   487: aload 8
    //   489: invokevirtual 15	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   492: invokevirtual 16	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   495: invokevirtual 17	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   498: new 46	com/go2group/synapse/core/exception/InvalidDataException
    //   501: dup
    //   502: aload_0
    //   503: getfield 3	com/go2group/synapse/rest/pub/RequirementPublicREST:i18n	Lcom/atlassian/jira/util/I18nHelper;
    //   506: ldc 65
    //   508: aload 8
    //   510: invokeinterface 29 3 0
    //   515: invokespecial 51	com/go2group/synapse/core/exception/InvalidDataException:<init>	(Ljava/lang/String;)V
    //   518: athrow
    //   519: goto -115 -> 404
    //   522: aload_2
    //   523: ifnull +118 -> 641
    //   526: invokestatic 59	com/atlassian/jira/component/ComponentAccessor:getJiraAuthenticationContext	()Lcom/atlassian/jira/security/JiraAuthenticationContext;
    //   529: invokeinterface 60 1 0
    //   534: aload 5
    //   536: invokeinterface 39 1 0
    //   541: invokestatic 66	com/go2group/synapse/util/PluginUtil:getAuditLogInputBean	(Lcom/atlassian/jira/user/ApplicationUser;Lcom/atlassian/jira/project/Project;)Lcom/go2group/synapse/core/audit/log/AuditLogInputBean;
    //   544: astore 7
    //   546: aload 7
    //   548: getstatic 67	com/go2group/synapse/core/audit/log/ActionEnum:LINKED	Lcom/go2group/synapse/core/audit/log/ActionEnum;
    //   551: invokevirtual 68	com/go2group/synapse/core/audit/log/AuditLogInputBean:setAction	(Lcom/go2group/synapse/core/audit/log/ActionEnum;)V
    //   554: aload 7
    //   556: getstatic 69	com/go2group/synapse/core/audit/log/ModuleEnum:REQUIREMENT	Lcom/go2group/synapse/core/audit/log/ModuleEnum;
    //   559: invokevirtual 70	com/go2group/synapse/core/audit/log/AuditLogInputBean:setModule	(Lcom/go2group/synapse/core/audit/log/ModuleEnum;)V
    //   562: aload 7
    //   564: getstatic 71	com/go2group/synapse/core/audit/log/SourceEnum:REST	Lcom/go2group/synapse/core/audit/log/SourceEnum;
    //   567: invokevirtual 72	com/go2group/synapse/core/audit/log/SourceEnum:getName	()Ljava/lang/String;
    //   570: invokevirtual 73	com/go2group/synapse/core/audit/log/AuditLogInputBean:setSource	(Ljava/lang/String;)V
    //   573: aload 7
    //   575: new 74	java/util/Date
    //   578: dup
    //   579: invokespecial 75	java/util/Date:<init>	()V
    //   582: invokevirtual 76	com/go2group/synapse/core/audit/log/AuditLogInputBean:setLogTime	(Ljava/util/Date;)V
    //   585: new 12	java/lang/StringBuilder
    //   588: dup
    //   589: invokespecial 13	java/lang/StringBuilder:<init>	()V
    //   592: ldc 77
    //   594: invokevirtual 15	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   597: aload_2
    //   598: invokevirtual 19	com/go2group/synapse/bean/TestCaseLinkInputBean:getTestCaseKeys	()Ljava/util/List;
    //   601: invokevirtual 20	java/lang/StringBuilder:append	(Ljava/lang/Object;)Ljava/lang/StringBuilder;
    //   604: ldc 78
    //   606: invokevirtual 15	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   609: aload_1
    //   610: invokevirtual 15	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   613: ldc 79
    //   615: invokevirtual 15	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   618: invokevirtual 16	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   621: astore 8
    //   623: aload 7
    //   625: aload 8
    //   627: invokevirtual 80	com/go2group/synapse/core/audit/log/AuditLogInputBean:setLog	(Ljava/lang/String;)V
    //   630: aload_0
    //   631: getfield 9	com/go2group/synapse/rest/pub/RequirementPublicREST:auditLogService	Lcom/go2group/synapse/core/audit/log/service/AuditLogService;
    //   634: aload 7
    //   636: invokeinterface 81 2 0
    //   641: aload_0
    //   642: invokevirtual 82	com/go2group/synapse/rest/pub/RequirementPublicREST:success	()Ljavax/ws/rs/core/Response;
    //   645: astore 7
    //   647: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   650: astore 8
    //   652: aload 8
    //   654: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   657: aload 7
    //   659: areturn
    //   660: astore 6
    //   662: getstatic 10	com/go2group/synapse/rest/pub/RequirementPublicREST:log	Lorg/apache/log4j/Logger;
    //   665: aload 6
    //   667: invokevirtual 83	com/go2group/synapse/core/exception/InvalidDataException:getMessage	()Ljava/lang/String;
    //   670: invokevirtual 84	org/apache/log4j/Logger:error	(Ljava/lang/Object;)V
    //   673: getstatic 10	com/go2group/synapse/rest/pub/RequirementPublicREST:log	Lorg/apache/log4j/Logger;
    //   676: aload 6
    //   678: invokevirtual 83	com/go2group/synapse/core/exception/InvalidDataException:getMessage	()Ljava/lang/String;
    //   681: aload 6
    //   683: invokevirtual 85	org/apache/log4j/Logger:debug	(Ljava/lang/Object;Ljava/lang/Throwable;)V
    //   686: aload_0
    //   687: aload 6
    //   689: invokevirtual 86	com/go2group/synapse/rest/pub/RequirementPublicREST:error	(Ljava/lang/Exception;)Ljavax/ws/rs/core/Response;
    //   692: astore 7
    //   694: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   697: astore 8
    //   699: aload 8
    //   701: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   704: aload 7
    //   706: areturn
    //   707: astore_3
    //   708: getstatic 10	com/go2group/synapse/rest/pub/RequirementPublicREST:log	Lorg/apache/log4j/Logger;
    //   711: aload_3
    //   712: invokevirtual 88	java/lang/Exception:getMessage	()Ljava/lang/String;
    //   715: invokevirtual 84	org/apache/log4j/Logger:error	(Ljava/lang/Object;)V
    //   718: getstatic 10	com/go2group/synapse/rest/pub/RequirementPublicREST:log	Lorg/apache/log4j/Logger;
    //   721: aload_3
    //   722: invokevirtual 88	java/lang/Exception:getMessage	()Ljava/lang/String;
    //   725: aload_3
    //   726: invokevirtual 85	org/apache/log4j/Logger:debug	(Ljava/lang/Object;Ljava/lang/Throwable;)V
    //   729: aload_0
    //   730: aload_3
    //   731: invokevirtual 86	com/go2group/synapse/rest/pub/RequirementPublicREST:error	(Ljava/lang/Exception;)Ljavax/ws/rs/core/Response;
    //   734: astore 4
    //   736: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   739: astore 5
    //   741: aload 5
    //   743: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   746: aload 4
    //   748: areturn
    //   749: astore 10
    //   751: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   754: astore 11
    //   756: aload 11
    //   758: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   761: aload 10
    //   763: athrow
    // Line number table:
    //   Java source line #104	-> byte code offset #0
    //   Java source line #105	-> byte code offset #9
    //   Java source line #106	-> byte code offset #34
    //   Java source line #109	-> byte code offset #62
    //   Java source line #110	-> byte code offset #66
    //   Java source line #111	-> byte code offset #72
    //   Java source line #112	-> byte code offset #77
    //   Java source line #186	-> byte code offset #85
    //   Java source line #187	-> byte code offset #90
    //   Java source line #112	-> byte code offset #95
    //   Java source line #115	-> byte code offset #98
    //   Java source line #117	-> byte code offset #110
    //   Java source line #118	-> byte code offset #115
    //   Java source line #119	-> byte code offset #140
    //   Java source line #186	-> byte code offset #158
    //   Java source line #187	-> byte code offset #163
    //   Java source line #119	-> byte code offset #168
    //   Java source line #123	-> byte code offset #171
    //   Java source line #124	-> byte code offset #178
    //   Java source line #125	-> byte code offset #186
    //   Java source line #186	-> byte code offset #203
    //   Java source line #187	-> byte code offset #208
    //   Java source line #125	-> byte code offset #213
    //   Java source line #129	-> byte code offset #216
    //   Java source line #130	-> byte code offset #225
    //   Java source line #131	-> byte code offset #233
    //   Java source line #186	-> byte code offset #250
    //   Java source line #187	-> byte code offset #255
    //   Java source line #131	-> byte code offset #260
    //   Java source line #135	-> byte code offset #263
    //   Java source line #136	-> byte code offset #280
    //   Java source line #137	-> byte code offset #288
    //   Java source line #186	-> byte code offset #305
    //   Java source line #187	-> byte code offset #310
    //   Java source line #137	-> byte code offset #315
    //   Java source line #141	-> byte code offset #318
    //   Java source line #142	-> byte code offset #337
    //   Java source line #143	-> byte code offset #345
    //   Java source line #147	-> byte code offset #375
    //   Java source line #148	-> byte code offset #384
    //   Java source line #150	-> byte code offset #393
    //   Java source line #151	-> byte code offset #426
    //   Java source line #153	-> byte code offset #439
    //   Java source line #154	-> byte code offset #444
    //   Java source line #156	-> byte code offset #472
    //   Java source line #157	-> byte code offset #498
    //   Java source line #159	-> byte code offset #519
    //   Java source line #161	-> byte code offset #522
    //   Java source line #163	-> byte code offset #526
    //   Java source line #164	-> byte code offset #546
    //   Java source line #165	-> byte code offset #554
    //   Java source line #166	-> byte code offset #562
    //   Java source line #167	-> byte code offset #573
    //   Java source line #168	-> byte code offset #585
    //   Java source line #169	-> byte code offset #623
    //   Java source line #170	-> byte code offset #630
    //   Java source line #174	-> byte code offset #641
    //   Java source line #186	-> byte code offset #647
    //   Java source line #187	-> byte code offset #652
    //   Java source line #174	-> byte code offset #657
    //   Java source line #176	-> byte code offset #660
    //   Java source line #177	-> byte code offset #662
    //   Java source line #178	-> byte code offset #673
    //   Java source line #179	-> byte code offset #686
    //   Java source line #186	-> byte code offset #694
    //   Java source line #187	-> byte code offset #699
    //   Java source line #179	-> byte code offset #704
    //   Java source line #181	-> byte code offset #707
    //   Java source line #182	-> byte code offset #708
    //   Java source line #183	-> byte code offset #718
    //   Java source line #184	-> byte code offset #729
    //   Java source line #186	-> byte code offset #736
    //   Java source line #187	-> byte code offset #741
    //   Java source line #184	-> byte code offset #746
    //   Java source line #186	-> byte code offset #749
    //   Java source line #187	-> byte code offset #756
    //   Java source line #188	-> byte code offset #761
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	764	0	this	RequirementPublicREST
    //   0	764	1	reqKey	String
    //   0	764	2	tcLinkInput	TestCaseLinkInputBean
    //   65	2	3	request	HttpServletRequest
    //   707	24	3	e	Exception
    //   70	677	4	canProceed	boolean
    //   83	13	5	localResponse	Response
    //   108	427	5	reqIssue	Object
    //   739	3	5	request	HttpServletRequest
    //   88	228	6	request	HttpServletRequest
    //   382	69	6	reqKeyList	List<String>
    //   660	28	6	e	InvalidDataException
    //   161	3	7	request	HttpServletRequest
    //   206	3	7	request	HttpServletRequest
    //   253	3	7	request	HttpServletRequest
    //   308	107	7	request	HttpServletRequest
    //   544	161	7	auditLogInputBean	AuditLogInputBean
    //   424	85	8	tcKey	String
    //   621	5	8	auditLog	String
    //   650	3	8	request	HttpServletRequest
    //   697	3	8	request	HttpServletRequest
    //   437	3	9	tcIssue	Issue
    //   749	13	10	localObject1	Object
    //   754	3	11	request	HttpServletRequest
    // Exception table:
    //   from	to	target	type
    //   318	647	660	com/go2group/synapse/core/exception/InvalidDataException
    //   0	85	707	java/lang/Exception
    //   98	158	707	java/lang/Exception
    //   171	203	707	java/lang/Exception
    //   216	250	707	java/lang/Exception
    //   263	305	707	java/lang/Exception
    //   318	647	707	java/lang/Exception
    //   660	694	707	java/lang/Exception
    //   0	85	749	finally
    //   98	158	749	finally
    //   171	203	749	finally
    //   216	250	749	finally
    //   263	305	749	finally
    //   318	647	749	finally
    //   660	694	749	finally
    //   707	736	749	finally
    //   749	751	749	finally
  }
  
  @GET
  @Path("{reqKey}/linkedTestCases")
  @XsrfProtectionExcluded
  public Response getLinkedTestCases(@PathParam("reqKey") String reqKey)
  {
    try
    {
      if (log.isDebugEnabled()) {
        log.debug("Get Linked Test Cases to requirement:" + reqKey);
      }
      
      HttpServletRequest request = ExecutingHttpRequest.get();
      canProceed = DefaultRestRateLimiter.canRequestProceed(request);
      HttpServletRequest request; if (!canProceed) {
        return rateLimitExceeded("Too many Requests");
      }
      
      Object reqIssue = issueManager.getIssueByKeyIgnoreCase(reqKey);
      
      if (reqIssue == null) {
        log.debug("Requirement issue not found for key:" + reqKey);
        HttpServletRequest request; return notFound(i18n.getText("servererror.rest.requirement.notfound", reqKey));
      }
      

      if (!hasValidLicense()) {
        log.debug("Invalid license");
        HttpServletRequest request; return forbidden(i18n.getText("servererror.rest.invalid.license"));
      }
      


      if (!hasViewPermission((Issue)reqIssue)) {
        log.debug("Does not have enough view permission on the issue");
        HttpServletRequest request; return forbidden(i18n.getText("servererror.rest.no.edit.permission"));
      }
      

      if (!hasSynapsePermission(((Issue)reqIssue).getProjectObject(), SynapsePermission.BROWSE_SYNAPSE_PANELS)) {
        log.debug("User does not have permission to browse project");
        HttpServletRequest request; return forbidden(i18n.getText("synapse.gadget.error.browse.permission.project"));
      }
      

      List<String> reqKeys = new ArrayList();
      reqKeys.add(((Issue)reqIssue).getKey());
      Set<Long> tcIds = tc2rLinkService.getTestCasesInRequirement(reqKeys);
      log.debug("Linked test cases id : " + tcIds);
      List<IssueWrapperBean> issues = new ArrayList();
      for (Object localObject1 = tcIds.iterator(); ((Iterator)localObject1).hasNext();) { Long issueId = (Long)((Iterator)localObject1).next();
        Issue issue = issueManager.getIssueObject(issueId);
        if ((issue != null) && (permissionUtil.hasViewPermission(issue))) {
          IssueWrapperBean issueWrapperBean = new IssueWrapperBean();
          issueWrapperBean.setId(issue.getId());
          issueWrapperBean.setKey(issue.getKey());
          issueWrapperBean.setSummary(issue.getSummary());
          issueWrapperBean.setResolution(issue.getResolution() != null);
          issues.add(issueWrapperBean);
        }
      }
      log.debug("Linked test cases wrapper : " + issues);
      HttpServletRequest request; return Response.ok(issues).build();
    }
    catch (Exception e) {
      boolean canProceed;
      log.error(e.getMessage());
      log.debug(e.getMessage(), e);
      HttpServletRequest request; return error(e);
    } finally {
      HttpServletRequest request = ExecutingHttpRequest.get();
      DefaultRestRateLimiter.endCall(request);
    }
  }
  
  @DELETE
  @Path("{reqKey}/deleteLinkedTestCases")
  @XsrfProtectionExcluded
  public Response deleteLinkedTestCases(@PathParam("reqKey") String reqKey, TestCaseLinkInputBean testCaseLinkInputBean) {
    try {
      if (log.isDebugEnabled()) {
        log.debug("Get Linked Test Cases to requirement:" + reqKey);
      }
      

      HttpServletRequest request = ExecutingHttpRequest.get();
      canProceed = DefaultRestRateLimiter.canRequestProceed(request);
      HttpServletRequest request; if (!canProceed) {
        return rateLimitExceeded("Too many Requests");
      }
      
      Object reqIssue = issueManager.getIssueByKeyIgnoreCase(reqKey);
      
      if (reqIssue == null) {
        log.debug("Requirement issue not found for key:" + reqKey);
        HttpServletRequest request; return notFound(i18n.getText("servererror.rest.requirement.notfound", reqKey));
      }
      

      if (!hasValidLicense()) {
        log.debug("Invalid license");
        HttpServletRequest request; return forbidden(i18n.getText("servererror.rest.invalid.license"));
      }
      

      if (!hasEditPermission((Issue)reqIssue)) {
        log.debug("Does not have enough edit permission on the issue");
        HttpServletRequest request; return forbidden(i18n.getText("servererror.rest.no.edit.permission"));
      }
      
      HttpServletRequest request;
      if (!hasSynapsePermission(((Issue)reqIssue).getProjectObject(), SynapsePermission.MANAGE_REQUIREMENTS)) {
        log.debug("User does not have permission to Manage Requirements");
        return forbidden(i18n.getText("servererror.rest.no.manage.requirement.permission"));
      }
      

      ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
      for (String testCaseKey : testCaseLinkInputBean.getTestCaseKeys()) {
        tc2rLinkService.delinkRequirement(testCaseKey, ((Issue)reqIssue).getKey(), true, user);
      }
      AuditLogInputBean auditLogInputBean;
      if (testCaseLinkInputBean != null)
      {
        auditLogInputBean = PluginUtil.getAuditLogInputBean(user, ((Issue)reqIssue).getProjectObject());
        auditLogInputBean.setAction(ActionEnum.DELINKED);
        auditLogInputBean.setModule(ModuleEnum.REQUIREMENT);
        auditLogInputBean.setSource(SourceEnum.REST.getName());
        auditLogInputBean.setLogTime(new Date());
        String auditLog = "Delinked Test Case(s) '" + testCaseLinkInputBean.getTestCaseKeys() + "' from Requirement '" + ((Issue)reqIssue).getKey() + "' through REST";
        auditLogInputBean.setLog(auditLog);
        auditLogService.createAuditLog(auditLogInputBean);
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
  
  /* Error */
  @javax.ws.rs.POST
  @Path("{reqKey}/addChildren")
  @XsrfProtectionExcluded
  public Response addChildren(@PathParam("reqKey") String reqKey, com.go2group.synapse.bean.RequirementLinkInputBean reqLinkInput)
  {
    // Byte code:
    //   0: getstatic 10	com/go2group/synapse/rest/pub/RequirementPublicREST:log	Lorg/apache/log4j/Logger;
    //   3: invokevirtual 11	org/apache/log4j/Logger:isDebugEnabled	()Z
    //   6: ifeq +56 -> 62
    //   9: getstatic 10	com/go2group/synapse/rest/pub/RequirementPublicREST:log	Lorg/apache/log4j/Logger;
    //   12: new 12	java/lang/StringBuilder
    //   15: dup
    //   16: invokespecial 13	java/lang/StringBuilder:<init>	()V
    //   19: ldc 119
    //   21: invokevirtual 15	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   24: aload_1
    //   25: invokevirtual 15	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   28: invokevirtual 16	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   31: invokevirtual 17	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   34: getstatic 10	com/go2group/synapse/rest/pub/RequirementPublicREST:log	Lorg/apache/log4j/Logger;
    //   37: new 12	java/lang/StringBuilder
    //   40: dup
    //   41: invokespecial 13	java/lang/StringBuilder:<init>	()V
    //   44: ldc 120
    //   46: invokevirtual 15	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   49: aload_2
    //   50: invokevirtual 121	com/go2group/synapse/bean/RequirementLinkInputBean:getRequirementKeys	()Ljava/util/List;
    //   53: invokevirtual 20	java/lang/StringBuilder:append	(Ljava/lang/Object;)Ljava/lang/StringBuilder;
    //   56: invokevirtual 16	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   59: invokevirtual 17	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   62: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   65: astore_3
    //   66: aload_3
    //   67: invokestatic 22	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:canRequestProceed	(Ljavax/servlet/http/HttpServletRequest;)Z
    //   70: istore 4
    //   72: iload 4
    //   74: ifne +24 -> 98
    //   77: aload_0
    //   78: ldc 23
    //   80: invokevirtual 24	com/go2group/synapse/rest/pub/RequirementPublicREST:rateLimitExceeded	(Ljava/lang/String;)Ljavax/ws/rs/core/Response;
    //   83: astore 5
    //   85: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   88: astore 6
    //   90: aload 6
    //   92: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   95: aload 5
    //   97: areturn
    //   98: aload_0
    //   99: getfield 2	com/go2group/synapse/rest/pub/RequirementPublicREST:issueManager	Lcom/atlassian/jira/issue/IssueManager;
    //   102: aload_1
    //   103: invokeinterface 26 2 0
    //   108: astore 5
    //   110: aload 5
    //   112: ifnonnull +59 -> 171
    //   115: getstatic 10	com/go2group/synapse/rest/pub/RequirementPublicREST:log	Lorg/apache/log4j/Logger;
    //   118: new 12	java/lang/StringBuilder
    //   121: dup
    //   122: invokespecial 13	java/lang/StringBuilder:<init>	()V
    //   125: ldc 27
    //   127: invokevirtual 15	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   130: aload_1
    //   131: invokevirtual 15	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   134: invokevirtual 16	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   137: invokevirtual 17	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   140: aload_0
    //   141: aload_0
    //   142: getfield 3	com/go2group/synapse/rest/pub/RequirementPublicREST:i18n	Lcom/atlassian/jira/util/I18nHelper;
    //   145: ldc 28
    //   147: aload_1
    //   148: invokeinterface 29 3 0
    //   153: invokevirtual 30	com/go2group/synapse/rest/pub/RequirementPublicREST:notFound	(Ljava/lang/String;)Ljavax/ws/rs/core/Response;
    //   156: astore 6
    //   158: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   161: astore 7
    //   163: aload 7
    //   165: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   168: aload 6
    //   170: areturn
    //   171: aload_0
    //   172: invokevirtual 31	com/go2group/synapse/rest/pub/RequirementPublicREST:hasValidLicense	()Z
    //   175: ifne +41 -> 216
    //   178: getstatic 10	com/go2group/synapse/rest/pub/RequirementPublicREST:log	Lorg/apache/log4j/Logger;
    //   181: ldc 32
    //   183: invokevirtual 17	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   186: aload_0
    //   187: aload_0
    //   188: getfield 3	com/go2group/synapse/rest/pub/RequirementPublicREST:i18n	Lcom/atlassian/jira/util/I18nHelper;
    //   191: ldc 33
    //   193: invokeinterface 34 2 0
    //   198: invokevirtual 35	com/go2group/synapse/rest/pub/RequirementPublicREST:forbidden	(Ljava/lang/String;)Ljavax/ws/rs/core/Response;
    //   201: astore 6
    //   203: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   206: astore 7
    //   208: aload 7
    //   210: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   213: aload 6
    //   215: areturn
    //   216: aload_0
    //   217: aload 5
    //   219: invokevirtual 36	com/go2group/synapse/rest/pub/RequirementPublicREST:hasEditPermission	(Lcom/atlassian/jira/issue/Issue;)Z
    //   222: ifne +41 -> 263
    //   225: getstatic 10	com/go2group/synapse/rest/pub/RequirementPublicREST:log	Lorg/apache/log4j/Logger;
    //   228: ldc 37
    //   230: invokevirtual 17	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   233: aload_0
    //   234: aload_0
    //   235: getfield 3	com/go2group/synapse/rest/pub/RequirementPublicREST:i18n	Lcom/atlassian/jira/util/I18nHelper;
    //   238: ldc 38
    //   240: invokeinterface 34 2 0
    //   245: invokevirtual 35	com/go2group/synapse/rest/pub/RequirementPublicREST:forbidden	(Ljava/lang/String;)Ljavax/ws/rs/core/Response;
    //   248: astore 6
    //   250: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   253: astore 7
    //   255: aload 7
    //   257: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   260: aload 6
    //   262: areturn
    //   263: aload_0
    //   264: aload 5
    //   266: invokeinterface 39 1 0
    //   271: getstatic 40	com/go2group/synapse/constant/SynapsePermission:MANAGE_REQUIREMENTS	Lcom/go2group/synapse/constant/SynapsePermission;
    //   274: invokevirtual 41	com/go2group/synapse/rest/pub/RequirementPublicREST:hasSynapsePermission	(Lcom/atlassian/jira/project/Project;Lcom/go2group/synapse/constant/SynapsePermission;)Z
    //   277: ifne +41 -> 318
    //   280: getstatic 10	com/go2group/synapse/rest/pub/RequirementPublicREST:log	Lorg/apache/log4j/Logger;
    //   283: ldc 42
    //   285: invokevirtual 17	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   288: aload_0
    //   289: aload_0
    //   290: getfield 3	com/go2group/synapse/rest/pub/RequirementPublicREST:i18n	Lcom/atlassian/jira/util/I18nHelper;
    //   293: ldc 43
    //   295: invokeinterface 34 2 0
    //   300: invokevirtual 35	com/go2group/synapse/rest/pub/RequirementPublicREST:forbidden	(Ljava/lang/String;)Ljavax/ws/rs/core/Response;
    //   303: astore 6
    //   305: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   308: astore 7
    //   310: aload 7
    //   312: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   315: aload 6
    //   317: areturn
    //   318: aload_2
    //   319: invokevirtual 121	com/go2group/synapse/bean/RequirementLinkInputBean:getRequirementKeys	()Ljava/util/List;
    //   322: ifnull +15 -> 337
    //   325: aload_2
    //   326: invokevirtual 121	com/go2group/synapse/bean/RequirementLinkInputBean:getRequirementKeys	()Ljava/util/List;
    //   329: invokeinterface 44 1 0
    //   334: ifne +45 -> 379
    //   337: getstatic 10	com/go2group/synapse/rest/pub/RequirementPublicREST:log	Lorg/apache/log4j/Logger;
    //   340: ldc 122
    //   342: invokevirtual 17	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   345: new 46	com/go2group/synapse/core/exception/InvalidDataException
    //   348: dup
    //   349: aload_0
    //   350: getfield 3	com/go2group/synapse/rest/pub/RequirementPublicREST:i18n	Lcom/atlassian/jira/util/I18nHelper;
    //   353: ldc 47
    //   355: aload_0
    //   356: getfield 4	com/go2group/synapse/rest/pub/RequirementPublicREST:synapseConfig	Lcom/go2group/synapse/config/SynapseConfig;
    //   359: getstatic 123	com/go2group/synapse/constant/SynapseIssueType:REQUIREMENT	Lcom/go2group/synapse/constant/SynapseIssueType;
    //   362: invokevirtual 124	com/go2group/synapse/constant/SynapseIssueType:getKey	()Ljava/lang/String;
    //   365: invokeinterface 50 2 0
    //   370: invokeinterface 29 3 0
    //   375: invokespecial 51	com/go2group/synapse/core/exception/InvalidDataException:<init>	(Ljava/lang/String;)V
    //   378: athrow
    //   379: aload_2
    //   380: invokevirtual 121	com/go2group/synapse/bean/RequirementLinkInputBean:getRequirementKeys	()Ljava/util/List;
    //   383: invokeinterface 55 1 0
    //   388: astore 6
    //   390: aload 6
    //   392: invokeinterface 56 1 0
    //   397: ifeq +91 -> 488
    //   400: aload 6
    //   402: invokeinterface 57 1 0
    //   407: checkcast 58	java/lang/String
    //   410: astore 7
    //   412: aload_0
    //   413: getfield 2	com/go2group/synapse/rest/pub/RequirementPublicREST:issueManager	Lcom/atlassian/jira/issue/IssueManager;
    //   416: aload 7
    //   418: invokeinterface 26 2 0
    //   423: astore 8
    //   425: aload 8
    //   427: ifnull +29 -> 456
    //   430: aload_0
    //   431: getfield 6	com/go2group/synapse/rest/pub/RequirementPublicREST:requirementService	Lcom/go2group/synapse/service/RequirementService;
    //   434: aload 5
    //   436: invokeinterface 105 1 0
    //   441: aload 8
    //   443: invokeinterface 105 1 0
    //   448: invokeinterface 125 3 0
    //   453: goto +32 -> 485
    //   456: getstatic 10	com/go2group/synapse/rest/pub/RequirementPublicREST:log	Lorg/apache/log4j/Logger;
    //   459: ldc 126
    //   461: invokevirtual 17	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   464: new 46	com/go2group/synapse/core/exception/InvalidDataException
    //   467: dup
    //   468: aload_0
    //   469: getfield 3	com/go2group/synapse/rest/pub/RequirementPublicREST:i18n	Lcom/atlassian/jira/util/I18nHelper;
    //   472: ldc 65
    //   474: aload 7
    //   476: invokeinterface 29 3 0
    //   481: invokespecial 51	com/go2group/synapse/core/exception/InvalidDataException:<init>	(Ljava/lang/String;)V
    //   484: athrow
    //   485: goto -95 -> 390
    //   488: aload_2
    //   489: ifnull +124 -> 613
    //   492: invokestatic 59	com/atlassian/jira/component/ComponentAccessor:getJiraAuthenticationContext	()Lcom/atlassian/jira/security/JiraAuthenticationContext;
    //   495: invokeinterface 60 1 0
    //   500: aload 5
    //   502: invokeinterface 39 1 0
    //   507: invokestatic 66	com/go2group/synapse/util/PluginUtil:getAuditLogInputBean	(Lcom/atlassian/jira/user/ApplicationUser;Lcom/atlassian/jira/project/Project;)Lcom/go2group/synapse/core/audit/log/AuditLogInputBean;
    //   510: astore 6
    //   512: aload 6
    //   514: getstatic 127	com/go2group/synapse/core/audit/log/ActionEnum:ADDED	Lcom/go2group/synapse/core/audit/log/ActionEnum;
    //   517: invokevirtual 68	com/go2group/synapse/core/audit/log/AuditLogInputBean:setAction	(Lcom/go2group/synapse/core/audit/log/ActionEnum;)V
    //   520: aload 6
    //   522: getstatic 69	com/go2group/synapse/core/audit/log/ModuleEnum:REQUIREMENT	Lcom/go2group/synapse/core/audit/log/ModuleEnum;
    //   525: invokevirtual 70	com/go2group/synapse/core/audit/log/AuditLogInputBean:setModule	(Lcom/go2group/synapse/core/audit/log/ModuleEnum;)V
    //   528: aload 6
    //   530: getstatic 71	com/go2group/synapse/core/audit/log/SourceEnum:REST	Lcom/go2group/synapse/core/audit/log/SourceEnum;
    //   533: invokevirtual 72	com/go2group/synapse/core/audit/log/SourceEnum:getName	()Ljava/lang/String;
    //   536: invokevirtual 73	com/go2group/synapse/core/audit/log/AuditLogInputBean:setSource	(Ljava/lang/String;)V
    //   539: aload 6
    //   541: new 74	java/util/Date
    //   544: dup
    //   545: invokespecial 75	java/util/Date:<init>	()V
    //   548: invokevirtual 76	com/go2group/synapse/core/audit/log/AuditLogInputBean:setLogTime	(Ljava/util/Date;)V
    //   551: new 12	java/lang/StringBuilder
    //   554: dup
    //   555: invokespecial 13	java/lang/StringBuilder:<init>	()V
    //   558: ldc -128
    //   560: invokevirtual 15	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   563: aload_2
    //   564: invokevirtual 121	com/go2group/synapse/bean/RequirementLinkInputBean:getRequirementKeys	()Ljava/util/List;
    //   567: invokevirtual 20	java/lang/StringBuilder:append	(Ljava/lang/Object;)Ljava/lang/StringBuilder;
    //   570: ldc 78
    //   572: invokevirtual 15	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   575: aload 5
    //   577: invokeinterface 95 1 0
    //   582: invokevirtual 15	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   585: ldc -127
    //   587: invokevirtual 15	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   590: invokevirtual 16	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   593: astore 7
    //   595: aload 6
    //   597: aload 7
    //   599: invokevirtual 80	com/go2group/synapse/core/audit/log/AuditLogInputBean:setLog	(Ljava/lang/String;)V
    //   602: aload_0
    //   603: getfield 9	com/go2group/synapse/rest/pub/RequirementPublicREST:auditLogService	Lcom/go2group/synapse/core/audit/log/service/AuditLogService;
    //   606: aload 6
    //   608: invokeinterface 81 2 0
    //   613: aload_0
    //   614: invokevirtual 82	com/go2group/synapse/rest/pub/RequirementPublicREST:success	()Ljavax/ws/rs/core/Response;
    //   617: astore 6
    //   619: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   622: astore 7
    //   624: aload 7
    //   626: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   629: aload 6
    //   631: areturn
    //   632: astore 6
    //   634: getstatic 10	com/go2group/synapse/rest/pub/RequirementPublicREST:log	Lorg/apache/log4j/Logger;
    //   637: aload 6
    //   639: invokevirtual 83	com/go2group/synapse/core/exception/InvalidDataException:getMessage	()Ljava/lang/String;
    //   642: invokevirtual 84	org/apache/log4j/Logger:error	(Ljava/lang/Object;)V
    //   645: getstatic 10	com/go2group/synapse/rest/pub/RequirementPublicREST:log	Lorg/apache/log4j/Logger;
    //   648: aload 6
    //   650: invokevirtual 83	com/go2group/synapse/core/exception/InvalidDataException:getMessage	()Ljava/lang/String;
    //   653: aload 6
    //   655: invokevirtual 85	org/apache/log4j/Logger:debug	(Ljava/lang/Object;Ljava/lang/Throwable;)V
    //   658: aload_0
    //   659: aload 6
    //   661: invokevirtual 86	com/go2group/synapse/rest/pub/RequirementPublicREST:error	(Ljava/lang/Exception;)Ljavax/ws/rs/core/Response;
    //   664: astore 7
    //   666: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   669: astore 8
    //   671: aload 8
    //   673: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   676: aload 7
    //   678: areturn
    //   679: astore_3
    //   680: getstatic 10	com/go2group/synapse/rest/pub/RequirementPublicREST:log	Lorg/apache/log4j/Logger;
    //   683: aload_3
    //   684: invokevirtual 88	java/lang/Exception:getMessage	()Ljava/lang/String;
    //   687: invokevirtual 84	org/apache/log4j/Logger:error	(Ljava/lang/Object;)V
    //   690: getstatic 10	com/go2group/synapse/rest/pub/RequirementPublicREST:log	Lorg/apache/log4j/Logger;
    //   693: aload_3
    //   694: invokevirtual 88	java/lang/Exception:getMessage	()Ljava/lang/String;
    //   697: aload_3
    //   698: invokevirtual 85	org/apache/log4j/Logger:debug	(Ljava/lang/Object;Ljava/lang/Throwable;)V
    //   701: aload_0
    //   702: aload_3
    //   703: invokevirtual 86	com/go2group/synapse/rest/pub/RequirementPublicREST:error	(Ljava/lang/Exception;)Ljavax/ws/rs/core/Response;
    //   706: astore 4
    //   708: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   711: astore 5
    //   713: aload 5
    //   715: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   718: aload 4
    //   720: areturn
    //   721: astore 9
    //   723: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   726: astore 10
    //   728: aload 10
    //   730: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   733: aload 9
    //   735: athrow
    // Line number table:
    //   Java source line #344	-> byte code offset #0
    //   Java source line #345	-> byte code offset #9
    //   Java source line #346	-> byte code offset #34
    //   Java source line #348	-> byte code offset #62
    //   Java source line #349	-> byte code offset #66
    //   Java source line #350	-> byte code offset #72
    //   Java source line #351	-> byte code offset #77
    //   Java source line #423	-> byte code offset #85
    //   Java source line #424	-> byte code offset #90
    //   Java source line #351	-> byte code offset #95
    //   Java source line #354	-> byte code offset #98
    //   Java source line #356	-> byte code offset #110
    //   Java source line #357	-> byte code offset #115
    //   Java source line #358	-> byte code offset #140
    //   Java source line #423	-> byte code offset #158
    //   Java source line #424	-> byte code offset #163
    //   Java source line #358	-> byte code offset #168
    //   Java source line #362	-> byte code offset #171
    //   Java source line #363	-> byte code offset #178
    //   Java source line #364	-> byte code offset #186
    //   Java source line #423	-> byte code offset #203
    //   Java source line #424	-> byte code offset #208
    //   Java source line #364	-> byte code offset #213
    //   Java source line #368	-> byte code offset #216
    //   Java source line #369	-> byte code offset #225
    //   Java source line #370	-> byte code offset #233
    //   Java source line #423	-> byte code offset #250
    //   Java source line #424	-> byte code offset #255
    //   Java source line #370	-> byte code offset #260
    //   Java source line #374	-> byte code offset #263
    //   Java source line #375	-> byte code offset #280
    //   Java source line #376	-> byte code offset #288
    //   Java source line #423	-> byte code offset #305
    //   Java source line #424	-> byte code offset #310
    //   Java source line #376	-> byte code offset #315
    //   Java source line #381	-> byte code offset #318
    //   Java source line #382	-> byte code offset #337
    //   Java source line #383	-> byte code offset #345
    //   Java source line #386	-> byte code offset #379
    //   Java source line #388	-> byte code offset #412
    //   Java source line #390	-> byte code offset #425
    //   Java source line #391	-> byte code offset #430
    //   Java source line #393	-> byte code offset #456
    //   Java source line #394	-> byte code offset #464
    //   Java source line #396	-> byte code offset #485
    //   Java source line #398	-> byte code offset #488
    //   Java source line #400	-> byte code offset #492
    //   Java source line #401	-> byte code offset #512
    //   Java source line #402	-> byte code offset #520
    //   Java source line #403	-> byte code offset #528
    //   Java source line #404	-> byte code offset #539
    //   Java source line #405	-> byte code offset #551
    //   Java source line #406	-> byte code offset #595
    //   Java source line #407	-> byte code offset #602
    //   Java source line #411	-> byte code offset #613
    //   Java source line #423	-> byte code offset #619
    //   Java source line #424	-> byte code offset #624
    //   Java source line #411	-> byte code offset #629
    //   Java source line #413	-> byte code offset #632
    //   Java source line #414	-> byte code offset #634
    //   Java source line #415	-> byte code offset #645
    //   Java source line #416	-> byte code offset #658
    //   Java source line #423	-> byte code offset #666
    //   Java source line #424	-> byte code offset #671
    //   Java source line #416	-> byte code offset #676
    //   Java source line #418	-> byte code offset #679
    //   Java source line #419	-> byte code offset #680
    //   Java source line #420	-> byte code offset #690
    //   Java source line #421	-> byte code offset #701
    //   Java source line #423	-> byte code offset #708
    //   Java source line #424	-> byte code offset #713
    //   Java source line #421	-> byte code offset #718
    //   Java source line #423	-> byte code offset #721
    //   Java source line #424	-> byte code offset #728
    //   Java source line #425	-> byte code offset #733
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	736	0	this	RequirementPublicREST
    //   0	736	1	reqKey	String
    //   0	736	2	reqLinkInput	com.go2group.synapse.bean.RequirementLinkInputBean
    //   65	2	3	request	HttpServletRequest
    //   679	24	3	e	Exception
    //   70	649	4	canProceed	boolean
    //   83	13	5	localResponse	Response
    //   108	468	5	reqIssue	Object
    //   711	3	5	request	HttpServletRequest
    //   88	313	6	request	HttpServletRequest
    //   510	120	6	auditLogInputBean	AuditLogInputBean
    //   632	28	6	e	InvalidDataException
    //   161	3	7	request	HttpServletRequest
    //   206	3	7	request	HttpServletRequest
    //   253	3	7	request	HttpServletRequest
    //   308	3	7	request	HttpServletRequest
    //   410	65	7	childReqKey	String
    //   593	5	7	auditLog	String
    //   622	55	7	request	HttpServletRequest
    //   423	19	8	childReqIssue	Issue
    //   669	3	8	request	HttpServletRequest
    //   721	13	9	localObject1	Object
    //   726	3	10	request	HttpServletRequest
    // Exception table:
    //   from	to	target	type
    //   318	619	632	com/go2group/synapse/core/exception/InvalidDataException
    //   0	85	679	java/lang/Exception
    //   98	158	679	java/lang/Exception
    //   171	203	679	java/lang/Exception
    //   216	250	679	java/lang/Exception
    //   263	305	679	java/lang/Exception
    //   318	619	679	java/lang/Exception
    //   632	666	679	java/lang/Exception
    //   0	85	721	finally
    //   98	158	721	finally
    //   171	203	721	finally
    //   216	250	721	finally
    //   263	305	721	finally
    //   318	619	721	finally
    //   632	666	721	finally
    //   679	708	721	finally
    //   721	723	721	finally
  }
  
  /* Error */
  @GET
  @Path("{reqKey}/getChildren")
  @XsrfProtectionExcluded
  public Response getChildrens(@PathParam("reqKey") String reqKey)
  {
    // Byte code:
    //   0: getstatic 10	com/go2group/synapse/rest/pub/RequirementPublicREST:log	Lorg/apache/log4j/Logger;
    //   3: ldc -126
    //   5: invokevirtual 17	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   8: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   11: astore_2
    //   12: aload_2
    //   13: invokestatic 22	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:canRequestProceed	(Ljavax/servlet/http/HttpServletRequest;)Z
    //   16: istore_3
    //   17: iload_3
    //   18: ifne +24 -> 42
    //   21: aload_0
    //   22: ldc 23
    //   24: invokevirtual 24	com/go2group/synapse/rest/pub/RequirementPublicREST:rateLimitExceeded	(Ljava/lang/String;)Ljavax/ws/rs/core/Response;
    //   27: astore 4
    //   29: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   32: astore 5
    //   34: aload 5
    //   36: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   39: aload 4
    //   41: areturn
    //   42: aload_0
    //   43: getfield 2	com/go2group/synapse/rest/pub/RequirementPublicREST:issueManager	Lcom/atlassian/jira/issue/IssueManager;
    //   46: aload_1
    //   47: invokeinterface 26 2 0
    //   52: astore 4
    //   54: aload 4
    //   56: ifnonnull +47 -> 103
    //   59: getstatic 10	com/go2group/synapse/rest/pub/RequirementPublicREST:log	Lorg/apache/log4j/Logger;
    //   62: new 12	java/lang/StringBuilder
    //   65: dup
    //   66: invokespecial 13	java/lang/StringBuilder:<init>	()V
    //   69: ldc 27
    //   71: invokevirtual 15	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   74: aload_1
    //   75: invokevirtual 15	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   78: invokevirtual 16	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   81: invokevirtual 17	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   84: aload_0
    //   85: invokevirtual 131	com/go2group/synapse/rest/pub/RequirementPublicREST:notFound	()Ljavax/ws/rs/core/Response;
    //   88: astore 5
    //   90: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   93: astore 6
    //   95: aload 6
    //   97: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   100: aload 5
    //   102: areturn
    //   103: aload_0
    //   104: invokevirtual 31	com/go2group/synapse/rest/pub/RequirementPublicREST:hasValidLicense	()Z
    //   107: ifne +30 -> 137
    //   110: getstatic 10	com/go2group/synapse/rest/pub/RequirementPublicREST:log	Lorg/apache/log4j/Logger;
    //   113: ldc 32
    //   115: invokevirtual 17	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   118: aload_0
    //   119: invokevirtual 132	com/go2group/synapse/rest/pub/RequirementPublicREST:forbidden	()Ljavax/ws/rs/core/Response;
    //   122: astore 5
    //   124: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   127: astore 6
    //   129: aload 6
    //   131: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   134: aload 5
    //   136: areturn
    //   137: aload_0
    //   138: aload 4
    //   140: invokevirtual 90	com/go2group/synapse/rest/pub/RequirementPublicREST:hasViewPermission	(Lcom/atlassian/jira/issue/Issue;)Z
    //   143: ifne +30 -> 173
    //   146: getstatic 10	com/go2group/synapse/rest/pub/RequirementPublicREST:log	Lorg/apache/log4j/Logger;
    //   149: ldc -123
    //   151: invokevirtual 17	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   154: aload_0
    //   155: invokevirtual 132	com/go2group/synapse/rest/pub/RequirementPublicREST:forbidden	()Ljavax/ws/rs/core/Response;
    //   158: astore 5
    //   160: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   163: astore 6
    //   165: aload 6
    //   167: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   170: aload 5
    //   172: areturn
    //   173: aload_0
    //   174: aload 4
    //   176: invokeinterface 39 1 0
    //   181: getstatic 40	com/go2group/synapse/constant/SynapsePermission:MANAGE_REQUIREMENTS	Lcom/go2group/synapse/constant/SynapsePermission;
    //   184: invokevirtual 41	com/go2group/synapse/rest/pub/RequirementPublicREST:hasSynapsePermission	(Lcom/atlassian/jira/project/Project;Lcom/go2group/synapse/constant/SynapsePermission;)Z
    //   187: ifne +30 -> 217
    //   190: getstatic 10	com/go2group/synapse/rest/pub/RequirementPublicREST:log	Lorg/apache/log4j/Logger;
    //   193: ldc 42
    //   195: invokevirtual 17	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   198: aload_0
    //   199: invokevirtual 132	com/go2group/synapse/rest/pub/RequirementPublicREST:forbidden	()Ljavax/ws/rs/core/Response;
    //   202: astore 5
    //   204: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   207: astore 6
    //   209: aload 6
    //   211: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   214: aload 5
    //   216: areturn
    //   217: new 52	java/util/ArrayList
    //   220: dup
    //   221: invokespecial 53	java/util/ArrayList:<init>	()V
    //   224: astore 5
    //   226: new 52	java/util/ArrayList
    //   229: dup
    //   230: invokespecial 53	java/util/ArrayList:<init>	()V
    //   233: astore 6
    //   235: aload_0
    //   236: getfield 6	com/go2group/synapse/rest/pub/RequirementPublicREST:requirementService	Lcom/go2group/synapse/service/RequirementService;
    //   239: aload 4
    //   241: invokeinterface 105 1 0
    //   246: invokeinterface 134 2 0
    //   251: astore 6
    //   253: aload 6
    //   255: ifnull +23 -> 278
    //   258: aload 6
    //   260: invokeinterface 44 1 0
    //   265: ifle +13 -> 278
    //   268: aload 5
    //   270: aload 6
    //   272: invokeinterface 135 2 0
    //   277: pop
    //   278: aload_0
    //   279: aload 6
    //   281: aload 5
    //   283: invokespecial 136	com/go2group/synapse/rest/pub/RequirementPublicREST:recursiveChildren	(Ljava/util/List;Ljava/util/List;)Ljava/util/List;
    //   286: pop
    //   287: new 52	java/util/ArrayList
    //   290: dup
    //   291: invokespecial 53	java/util/ArrayList:<init>	()V
    //   294: astore 7
    //   296: aload 5
    //   298: invokeinterface 55 1 0
    //   303: astore 8
    //   305: aload 8
    //   307: invokeinterface 56 1 0
    //   312: ifeq +93 -> 405
    //   315: aload 8
    //   317: invokeinterface 57 1 0
    //   322: checkcast 137	com/atlassian/jira/issue/Issue
    //   325: astore 9
    //   327: new 103	com/go2group/synapse/bean/IssueWrapperBean
    //   330: dup
    //   331: invokespecial 104	com/go2group/synapse/bean/IssueWrapperBean:<init>	()V
    //   334: astore 10
    //   336: aload 10
    //   338: aload 9
    //   340: invokeinterface 105 1 0
    //   345: invokevirtual 106	com/go2group/synapse/bean/IssueWrapperBean:setId	(Ljava/lang/Long;)V
    //   348: aload 10
    //   350: aload 9
    //   352: invokeinterface 95 1 0
    //   357: invokevirtual 107	com/go2group/synapse/bean/IssueWrapperBean:setKey	(Ljava/lang/String;)V
    //   360: aload 10
    //   362: aload 9
    //   364: invokeinterface 108 1 0
    //   369: invokevirtual 109	com/go2group/synapse/bean/IssueWrapperBean:setSummary	(Ljava/lang/String;)V
    //   372: aload 10
    //   374: aload 9
    //   376: invokeinterface 110 1 0
    //   381: ifnull +7 -> 388
    //   384: iconst_1
    //   385: goto +4 -> 389
    //   388: iconst_0
    //   389: invokevirtual 111	com/go2group/synapse/bean/IssueWrapperBean:setResolution	(Z)V
    //   392: aload 7
    //   394: aload 10
    //   396: invokeinterface 54 2 0
    //   401: pop
    //   402: goto -97 -> 305
    //   405: getstatic 10	com/go2group/synapse/rest/pub/RequirementPublicREST:log	Lorg/apache/log4j/Logger;
    //   408: new 12	java/lang/StringBuilder
    //   411: dup
    //   412: invokespecial 13	java/lang/StringBuilder:<init>	()V
    //   415: ldc -118
    //   417: invokevirtual 15	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   420: aload 4
    //   422: invokeinterface 95 1 0
    //   427: invokevirtual 15	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   430: ldc -117
    //   432: invokevirtual 15	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   435: aload 7
    //   437: invokeinterface 44 1 0
    //   442: invokevirtual 140	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
    //   445: invokevirtual 16	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   448: invokevirtual 17	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   451: aload 7
    //   453: invokestatic 113	javax/ws/rs/core/Response:ok	(Ljava/lang/Object;)Ljavax/ws/rs/core/Response$ResponseBuilder;
    //   456: invokevirtual 114	javax/ws/rs/core/Response$ResponseBuilder:build	()Ljavax/ws/rs/core/Response;
    //   459: astore 8
    //   461: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   464: astore 9
    //   466: aload 9
    //   468: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   471: aload 8
    //   473: areturn
    //   474: astore 5
    //   476: getstatic 10	com/go2group/synapse/rest/pub/RequirementPublicREST:log	Lorg/apache/log4j/Logger;
    //   479: aload 5
    //   481: invokevirtual 83	com/go2group/synapse/core/exception/InvalidDataException:getMessage	()Ljava/lang/String;
    //   484: invokevirtual 84	org/apache/log4j/Logger:error	(Ljava/lang/Object;)V
    //   487: getstatic 10	com/go2group/synapse/rest/pub/RequirementPublicREST:log	Lorg/apache/log4j/Logger;
    //   490: aload 5
    //   492: invokevirtual 83	com/go2group/synapse/core/exception/InvalidDataException:getMessage	()Ljava/lang/String;
    //   495: aload 5
    //   497: invokevirtual 85	org/apache/log4j/Logger:debug	(Ljava/lang/Object;Ljava/lang/Throwable;)V
    //   500: aload_0
    //   501: aload 5
    //   503: invokevirtual 86	com/go2group/synapse/rest/pub/RequirementPublicREST:error	(Ljava/lang/Exception;)Ljavax/ws/rs/core/Response;
    //   506: astore 6
    //   508: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   511: astore 7
    //   513: aload 7
    //   515: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   518: aload 6
    //   520: areturn
    //   521: astore_2
    //   522: getstatic 10	com/go2group/synapse/rest/pub/RequirementPublicREST:log	Lorg/apache/log4j/Logger;
    //   525: aload_2
    //   526: invokevirtual 88	java/lang/Exception:getMessage	()Ljava/lang/String;
    //   529: invokevirtual 84	org/apache/log4j/Logger:error	(Ljava/lang/Object;)V
    //   532: getstatic 10	com/go2group/synapse/rest/pub/RequirementPublicREST:log	Lorg/apache/log4j/Logger;
    //   535: aload_2
    //   536: invokevirtual 88	java/lang/Exception:getMessage	()Ljava/lang/String;
    //   539: aload_2
    //   540: invokevirtual 85	org/apache/log4j/Logger:debug	(Ljava/lang/Object;Ljava/lang/Throwable;)V
    //   543: aload_0
    //   544: aload_2
    //   545: invokevirtual 86	com/go2group/synapse/rest/pub/RequirementPublicREST:error	(Ljava/lang/Exception;)Ljavax/ws/rs/core/Response;
    //   548: astore_3
    //   549: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   552: astore 4
    //   554: aload 4
    //   556: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   559: aload_3
    //   560: areturn
    //   561: astore 11
    //   563: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   566: astore 12
    //   568: aload 12
    //   570: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   573: aload 11
    //   575: athrow
    // Line number table:
    //   Java source line #432	-> byte code offset #0
    //   Java source line #434	-> byte code offset #8
    //   Java source line #435	-> byte code offset #12
    //   Java source line #436	-> byte code offset #17
    //   Java source line #437	-> byte code offset #21
    //   Java source line #497	-> byte code offset #29
    //   Java source line #498	-> byte code offset #34
    //   Java source line #437	-> byte code offset #39
    //   Java source line #440	-> byte code offset #42
    //   Java source line #442	-> byte code offset #54
    //   Java source line #443	-> byte code offset #59
    //   Java source line #444	-> byte code offset #84
    //   Java source line #497	-> byte code offset #90
    //   Java source line #498	-> byte code offset #95
    //   Java source line #444	-> byte code offset #100
    //   Java source line #448	-> byte code offset #103
    //   Java source line #449	-> byte code offset #110
    //   Java source line #450	-> byte code offset #118
    //   Java source line #497	-> byte code offset #124
    //   Java source line #498	-> byte code offset #129
    //   Java source line #450	-> byte code offset #134
    //   Java source line #454	-> byte code offset #137
    //   Java source line #455	-> byte code offset #146
    //   Java source line #456	-> byte code offset #154
    //   Java source line #497	-> byte code offset #160
    //   Java source line #498	-> byte code offset #165
    //   Java source line #456	-> byte code offset #170
    //   Java source line #460	-> byte code offset #173
    //   Java source line #461	-> byte code offset #190
    //   Java source line #462	-> byte code offset #198
    //   Java source line #497	-> byte code offset #204
    //   Java source line #498	-> byte code offset #209
    //   Java source line #462	-> byte code offset #214
    //   Java source line #465	-> byte code offset #217
    //   Java source line #466	-> byte code offset #226
    //   Java source line #468	-> byte code offset #235
    //   Java source line #469	-> byte code offset #253
    //   Java source line #470	-> byte code offset #268
    //   Java source line #472	-> byte code offset #278
    //   Java source line #474	-> byte code offset #287
    //   Java source line #475	-> byte code offset #296
    //   Java source line #476	-> byte code offset #327
    //   Java source line #477	-> byte code offset #336
    //   Java source line #478	-> byte code offset #348
    //   Java source line #479	-> byte code offset #360
    //   Java source line #480	-> byte code offset #372
    //   Java source line #481	-> byte code offset #392
    //   Java source line #482	-> byte code offset #402
    //   Java source line #484	-> byte code offset #405
    //   Java source line #485	-> byte code offset #451
    //   Java source line #497	-> byte code offset #461
    //   Java source line #498	-> byte code offset #466
    //   Java source line #485	-> byte code offset #471
    //   Java source line #487	-> byte code offset #474
    //   Java source line #488	-> byte code offset #476
    //   Java source line #489	-> byte code offset #487
    //   Java source line #490	-> byte code offset #500
    //   Java source line #497	-> byte code offset #508
    //   Java source line #498	-> byte code offset #513
    //   Java source line #490	-> byte code offset #518
    //   Java source line #492	-> byte code offset #521
    //   Java source line #493	-> byte code offset #522
    //   Java source line #494	-> byte code offset #532
    //   Java source line #495	-> byte code offset #543
    //   Java source line #497	-> byte code offset #549
    //   Java source line #498	-> byte code offset #554
    //   Java source line #495	-> byte code offset #559
    //   Java source line #497	-> byte code offset #561
    //   Java source line #498	-> byte code offset #568
    //   Java source line #499	-> byte code offset #573
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	576	0	this	RequirementPublicREST
    //   0	576	1	reqKey	String
    //   11	2	2	request	HttpServletRequest
    //   521	24	2	e	Exception
    //   16	544	3	canProceed	boolean
    //   27	13	4	localResponse	Response
    //   52	369	4	reqIssue	Object
    //   552	3	4	request	HttpServletRequest
    //   32	183	5	request	HttpServletRequest
    //   224	73	5	childReqs	List<Issue>
    //   474	28	5	e	InvalidDataException
    //   93	3	6	request	HttpServletRequest
    //   127	3	6	request	HttpServletRequest
    //   163	3	6	request	HttpServletRequest
    //   207	3	6	request	HttpServletRequest
    //   233	286	6	tmpChildReqs	List<Issue>
    //   294	158	7	issueWrapperBeans	List<IssueWrapperBean>
    //   511	3	7	request	HttpServletRequest
    //   303	169	8	localObject1	Object
    //   325	50	9	issue	Issue
    //   464	3	9	request	HttpServletRequest
    //   334	61	10	issueWrapperBean	IssueWrapperBean
    //   561	13	11	localObject2	Object
    //   566	3	12	request	HttpServletRequest
    // Exception table:
    //   from	to	target	type
    //   217	461	474	com/go2group/synapse/core/exception/InvalidDataException
    //   8	29	521	java/lang/Exception
    //   42	90	521	java/lang/Exception
    //   103	124	521	java/lang/Exception
    //   137	160	521	java/lang/Exception
    //   173	204	521	java/lang/Exception
    //   217	461	521	java/lang/Exception
    //   474	508	521	java/lang/Exception
    //   8	29	561	finally
    //   42	90	561	finally
    //   103	124	561	finally
    //   137	160	561	finally
    //   173	204	561	finally
    //   217	461	561	finally
    //   474	508	561	finally
    //   521	549	561	finally
    //   561	563	561	finally
  }
  
  /* Error */
  @GET
  @Path("{reqKey}/getOnlyImmediateChildren")
  @XsrfProtectionExcluded
  public Response getOnlyImmediateChildren(@PathParam("reqKey") String reqKey)
  {
    // Byte code:
    //   0: getstatic 10	com/go2group/synapse/rest/pub/RequirementPublicREST:log	Lorg/apache/log4j/Logger;
    //   3: ldc -126
    //   5: invokevirtual 17	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   8: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   11: astore_2
    //   12: aload_2
    //   13: invokestatic 22	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:canRequestProceed	(Ljavax/servlet/http/HttpServletRequest;)Z
    //   16: istore_3
    //   17: iload_3
    //   18: ifne +24 -> 42
    //   21: aload_0
    //   22: ldc 23
    //   24: invokevirtual 24	com/go2group/synapse/rest/pub/RequirementPublicREST:rateLimitExceeded	(Ljava/lang/String;)Ljavax/ws/rs/core/Response;
    //   27: astore 4
    //   29: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   32: astore 5
    //   34: aload 5
    //   36: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   39: aload 4
    //   41: areturn
    //   42: aload_0
    //   43: getfield 2	com/go2group/synapse/rest/pub/RequirementPublicREST:issueManager	Lcom/atlassian/jira/issue/IssueManager;
    //   46: aload_1
    //   47: invokeinterface 26 2 0
    //   52: astore 4
    //   54: aload 4
    //   56: ifnull +34 -> 90
    //   59: aload_0
    //   60: getfield 4	com/go2group/synapse/rest/pub/RequirementPublicREST:synapseConfig	Lcom/go2group/synapse/config/SynapseConfig;
    //   63: ldc -115
    //   65: invokeinterface 142 2 0
    //   70: aload 4
    //   72: invokeinterface 143 1 0
    //   77: invokeinterface 144 1 0
    //   82: invokeinterface 145 2 0
    //   87: ifne +47 -> 134
    //   90: getstatic 10	com/go2group/synapse/rest/pub/RequirementPublicREST:log	Lorg/apache/log4j/Logger;
    //   93: new 12	java/lang/StringBuilder
    //   96: dup
    //   97: invokespecial 13	java/lang/StringBuilder:<init>	()V
    //   100: ldc 27
    //   102: invokevirtual 15	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   105: aload_1
    //   106: invokevirtual 15	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   109: invokevirtual 16	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   112: invokevirtual 17	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   115: aload_0
    //   116: invokevirtual 131	com/go2group/synapse/rest/pub/RequirementPublicREST:notFound	()Ljavax/ws/rs/core/Response;
    //   119: astore 5
    //   121: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   124: astore 6
    //   126: aload 6
    //   128: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   131: aload 5
    //   133: areturn
    //   134: aload_0
    //   135: invokevirtual 31	com/go2group/synapse/rest/pub/RequirementPublicREST:hasValidLicense	()Z
    //   138: ifne +30 -> 168
    //   141: getstatic 10	com/go2group/synapse/rest/pub/RequirementPublicREST:log	Lorg/apache/log4j/Logger;
    //   144: ldc 32
    //   146: invokevirtual 17	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   149: aload_0
    //   150: invokevirtual 132	com/go2group/synapse/rest/pub/RequirementPublicREST:forbidden	()Ljavax/ws/rs/core/Response;
    //   153: astore 5
    //   155: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   158: astore 6
    //   160: aload 6
    //   162: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   165: aload 5
    //   167: areturn
    //   168: aload_0
    //   169: aload 4
    //   171: invokevirtual 90	com/go2group/synapse/rest/pub/RequirementPublicREST:hasViewPermission	(Lcom/atlassian/jira/issue/Issue;)Z
    //   174: ifne +30 -> 204
    //   177: getstatic 10	com/go2group/synapse/rest/pub/RequirementPublicREST:log	Lorg/apache/log4j/Logger;
    //   180: ldc -123
    //   182: invokevirtual 17	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   185: aload_0
    //   186: invokevirtual 132	com/go2group/synapse/rest/pub/RequirementPublicREST:forbidden	()Ljavax/ws/rs/core/Response;
    //   189: astore 5
    //   191: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   194: astore 6
    //   196: aload 6
    //   198: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   201: aload 5
    //   203: areturn
    //   204: aload_0
    //   205: aload 4
    //   207: invokeinterface 39 1 0
    //   212: getstatic 40	com/go2group/synapse/constant/SynapsePermission:MANAGE_REQUIREMENTS	Lcom/go2group/synapse/constant/SynapsePermission;
    //   215: invokevirtual 41	com/go2group/synapse/rest/pub/RequirementPublicREST:hasSynapsePermission	(Lcom/atlassian/jira/project/Project;Lcom/go2group/synapse/constant/SynapsePermission;)Z
    //   218: ifne +30 -> 248
    //   221: getstatic 10	com/go2group/synapse/rest/pub/RequirementPublicREST:log	Lorg/apache/log4j/Logger;
    //   224: ldc 42
    //   226: invokevirtual 17	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   229: aload_0
    //   230: invokevirtual 132	com/go2group/synapse/rest/pub/RequirementPublicREST:forbidden	()Ljavax/ws/rs/core/Response;
    //   233: astore 5
    //   235: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   238: astore 6
    //   240: aload 6
    //   242: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   245: aload 5
    //   247: areturn
    //   248: new 52	java/util/ArrayList
    //   251: dup
    //   252: invokespecial 53	java/util/ArrayList:<init>	()V
    //   255: astore 5
    //   257: aconst_null
    //   258: astore 6
    //   260: aload_0
    //   261: getfield 6	com/go2group/synapse/rest/pub/RequirementPublicREST:requirementService	Lcom/go2group/synapse/service/RequirementService;
    //   264: aload 4
    //   266: invokeinterface 105 1 0
    //   271: invokeinterface 134 2 0
    //   276: astore 6
    //   278: aload 6
    //   280: ifnull +13 -> 293
    //   283: aload 5
    //   285: aload 6
    //   287: invokeinterface 135 2 0
    //   292: pop
    //   293: new 52	java/util/ArrayList
    //   296: dup
    //   297: invokespecial 53	java/util/ArrayList:<init>	()V
    //   300: astore 7
    //   302: aload 5
    //   304: invokeinterface 55 1 0
    //   309: astore 8
    //   311: aload 8
    //   313: invokeinterface 56 1 0
    //   318: ifeq +93 -> 411
    //   321: aload 8
    //   323: invokeinterface 57 1 0
    //   328: checkcast 137	com/atlassian/jira/issue/Issue
    //   331: astore 9
    //   333: new 103	com/go2group/synapse/bean/IssueWrapperBean
    //   336: dup
    //   337: invokespecial 104	com/go2group/synapse/bean/IssueWrapperBean:<init>	()V
    //   340: astore 10
    //   342: aload 10
    //   344: aload 9
    //   346: invokeinterface 105 1 0
    //   351: invokevirtual 106	com/go2group/synapse/bean/IssueWrapperBean:setId	(Ljava/lang/Long;)V
    //   354: aload 10
    //   356: aload 9
    //   358: invokeinterface 95 1 0
    //   363: invokevirtual 107	com/go2group/synapse/bean/IssueWrapperBean:setKey	(Ljava/lang/String;)V
    //   366: aload 10
    //   368: aload 9
    //   370: invokeinterface 108 1 0
    //   375: invokevirtual 109	com/go2group/synapse/bean/IssueWrapperBean:setSummary	(Ljava/lang/String;)V
    //   378: aload 10
    //   380: aload 9
    //   382: invokeinterface 110 1 0
    //   387: ifnull +7 -> 394
    //   390: iconst_1
    //   391: goto +4 -> 395
    //   394: iconst_0
    //   395: invokevirtual 111	com/go2group/synapse/bean/IssueWrapperBean:setResolution	(Z)V
    //   398: aload 7
    //   400: aload 10
    //   402: invokeinterface 54 2 0
    //   407: pop
    //   408: goto -97 -> 311
    //   411: getstatic 10	com/go2group/synapse/rest/pub/RequirementPublicREST:log	Lorg/apache/log4j/Logger;
    //   414: new 12	java/lang/StringBuilder
    //   417: dup
    //   418: invokespecial 13	java/lang/StringBuilder:<init>	()V
    //   421: ldc -118
    //   423: invokevirtual 15	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   426: aload 4
    //   428: invokeinterface 95 1 0
    //   433: invokevirtual 15	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   436: ldc -117
    //   438: invokevirtual 15	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   441: aload 7
    //   443: invokeinterface 44 1 0
    //   448: invokevirtual 140	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
    //   451: invokevirtual 16	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   454: invokevirtual 17	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   457: aload 7
    //   459: invokestatic 113	javax/ws/rs/core/Response:ok	(Ljava/lang/Object;)Ljavax/ws/rs/core/Response$ResponseBuilder;
    //   462: invokevirtual 114	javax/ws/rs/core/Response$ResponseBuilder:build	()Ljavax/ws/rs/core/Response;
    //   465: astore 8
    //   467: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   470: astore 9
    //   472: aload 9
    //   474: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   477: aload 8
    //   479: areturn
    //   480: astore 5
    //   482: getstatic 10	com/go2group/synapse/rest/pub/RequirementPublicREST:log	Lorg/apache/log4j/Logger;
    //   485: aload 5
    //   487: invokevirtual 83	com/go2group/synapse/core/exception/InvalidDataException:getMessage	()Ljava/lang/String;
    //   490: invokevirtual 84	org/apache/log4j/Logger:error	(Ljava/lang/Object;)V
    //   493: getstatic 10	com/go2group/synapse/rest/pub/RequirementPublicREST:log	Lorg/apache/log4j/Logger;
    //   496: aload 5
    //   498: invokevirtual 83	com/go2group/synapse/core/exception/InvalidDataException:getMessage	()Ljava/lang/String;
    //   501: aload 5
    //   503: invokevirtual 85	org/apache/log4j/Logger:debug	(Ljava/lang/Object;Ljava/lang/Throwable;)V
    //   506: aload_0
    //   507: aload 5
    //   509: invokevirtual 86	com/go2group/synapse/rest/pub/RequirementPublicREST:error	(Ljava/lang/Exception;)Ljavax/ws/rs/core/Response;
    //   512: astore 6
    //   514: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   517: astore 7
    //   519: aload 7
    //   521: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   524: aload 6
    //   526: areturn
    //   527: astore_2
    //   528: getstatic 10	com/go2group/synapse/rest/pub/RequirementPublicREST:log	Lorg/apache/log4j/Logger;
    //   531: aload_2
    //   532: invokevirtual 88	java/lang/Exception:getMessage	()Ljava/lang/String;
    //   535: invokevirtual 84	org/apache/log4j/Logger:error	(Ljava/lang/Object;)V
    //   538: getstatic 10	com/go2group/synapse/rest/pub/RequirementPublicREST:log	Lorg/apache/log4j/Logger;
    //   541: aload_2
    //   542: invokevirtual 88	java/lang/Exception:getMessage	()Ljava/lang/String;
    //   545: aload_2
    //   546: invokevirtual 85	org/apache/log4j/Logger:debug	(Ljava/lang/Object;Ljava/lang/Throwable;)V
    //   549: aload_0
    //   550: aload_2
    //   551: invokevirtual 86	com/go2group/synapse/rest/pub/RequirementPublicREST:error	(Ljava/lang/Exception;)Ljavax/ws/rs/core/Response;
    //   554: astore_3
    //   555: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   558: astore 4
    //   560: aload 4
    //   562: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   565: aload_3
    //   566: areturn
    //   567: astore 11
    //   569: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   572: astore 12
    //   574: aload 12
    //   576: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   579: aload 11
    //   581: athrow
    // Line number table:
    //   Java source line #506	-> byte code offset #0
    //   Java source line #508	-> byte code offset #8
    //   Java source line #509	-> byte code offset #12
    //   Java source line #510	-> byte code offset #17
    //   Java source line #511	-> byte code offset #21
    //   Java source line #571	-> byte code offset #29
    //   Java source line #572	-> byte code offset #34
    //   Java source line #511	-> byte code offset #39
    //   Java source line #514	-> byte code offset #42
    //   Java source line #516	-> byte code offset #54
    //   Java source line #517	-> byte code offset #90
    //   Java source line #518	-> byte code offset #115
    //   Java source line #571	-> byte code offset #121
    //   Java source line #572	-> byte code offset #126
    //   Java source line #518	-> byte code offset #131
    //   Java source line #522	-> byte code offset #134
    //   Java source line #523	-> byte code offset #141
    //   Java source line #524	-> byte code offset #149
    //   Java source line #571	-> byte code offset #155
    //   Java source line #572	-> byte code offset #160
    //   Java source line #524	-> byte code offset #165
    //   Java source line #528	-> byte code offset #168
    //   Java source line #529	-> byte code offset #177
    //   Java source line #530	-> byte code offset #185
    //   Java source line #571	-> byte code offset #191
    //   Java source line #572	-> byte code offset #196
    //   Java source line #530	-> byte code offset #201
    //   Java source line #534	-> byte code offset #204
    //   Java source line #535	-> byte code offset #221
    //   Java source line #536	-> byte code offset #229
    //   Java source line #571	-> byte code offset #235
    //   Java source line #572	-> byte code offset #240
    //   Java source line #536	-> byte code offset #245
    //   Java source line #539	-> byte code offset #248
    //   Java source line #540	-> byte code offset #257
    //   Java source line #542	-> byte code offset #260
    //   Java source line #543	-> byte code offset #278
    //   Java source line #544	-> byte code offset #283
    //   Java source line #548	-> byte code offset #293
    //   Java source line #549	-> byte code offset #302
    //   Java source line #550	-> byte code offset #333
    //   Java source line #551	-> byte code offset #342
    //   Java source line #552	-> byte code offset #354
    //   Java source line #553	-> byte code offset #366
    //   Java source line #554	-> byte code offset #378
    //   Java source line #555	-> byte code offset #398
    //   Java source line #556	-> byte code offset #408
    //   Java source line #558	-> byte code offset #411
    //   Java source line #559	-> byte code offset #457
    //   Java source line #571	-> byte code offset #467
    //   Java source line #572	-> byte code offset #472
    //   Java source line #559	-> byte code offset #477
    //   Java source line #561	-> byte code offset #480
    //   Java source line #562	-> byte code offset #482
    //   Java source line #563	-> byte code offset #493
    //   Java source line #564	-> byte code offset #506
    //   Java source line #571	-> byte code offset #514
    //   Java source line #572	-> byte code offset #519
    //   Java source line #564	-> byte code offset #524
    //   Java source line #566	-> byte code offset #527
    //   Java source line #567	-> byte code offset #528
    //   Java source line #568	-> byte code offset #538
    //   Java source line #569	-> byte code offset #549
    //   Java source line #571	-> byte code offset #555
    //   Java source line #572	-> byte code offset #560
    //   Java source line #569	-> byte code offset #565
    //   Java source line #571	-> byte code offset #567
    //   Java source line #572	-> byte code offset #574
    //   Java source line #573	-> byte code offset #579
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	582	0	this	RequirementPublicREST
    //   0	582	1	reqKey	String
    //   11	2	2	request	HttpServletRequest
    //   527	24	2	e	Exception
    //   16	550	3	canProceed	boolean
    //   27	13	4	localResponse	Response
    //   52	375	4	reqIssue	Object
    //   558	3	4	request	HttpServletRequest
    //   32	214	5	request	HttpServletRequest
    //   255	48	5	childReqs	List<Issue>
    //   480	28	5	e	InvalidDataException
    //   124	3	6	request	HttpServletRequest
    //   158	3	6	request	HttpServletRequest
    //   194	3	6	request	HttpServletRequest
    //   238	3	6	request	HttpServletRequest
    //   258	267	6	tmpChildReqs	List<Issue>
    //   300	158	7	issueWrapperBeans	List<IssueWrapperBean>
    //   517	3	7	request	HttpServletRequest
    //   309	169	8	localObject1	Object
    //   331	50	9	issue	Issue
    //   470	3	9	request	HttpServletRequest
    //   340	61	10	issueWrapperBean	IssueWrapperBean
    //   567	13	11	localObject2	Object
    //   572	3	12	request	HttpServletRequest
    // Exception table:
    //   from	to	target	type
    //   248	467	480	com/go2group/synapse/core/exception/InvalidDataException
    //   8	29	527	java/lang/Exception
    //   42	121	527	java/lang/Exception
    //   134	155	527	java/lang/Exception
    //   168	191	527	java/lang/Exception
    //   204	235	527	java/lang/Exception
    //   248	467	527	java/lang/Exception
    //   480	514	527	java/lang/Exception
    //   8	29	567	finally
    //   42	121	567	finally
    //   134	155	567	finally
    //   168	191	567	finally
    //   204	235	567	finally
    //   248	467	567	finally
    //   480	514	567	finally
    //   527	555	567	finally
    //   567	569	567	finally
  }
  
  /* Error */
  @GET
  @Path("{reqKey}/getDefects")
  @XsrfProtectionExcluded
  public Response getDefects(@PathParam("reqKey") String reqKey)
  {
    // Byte code:
    //   0: getstatic 10	com/go2group/synapse/rest/pub/RequirementPublicREST:log	Lorg/apache/log4j/Logger;
    //   3: ldc -110
    //   5: invokevirtual 17	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   8: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   11: astore_2
    //   12: aload_2
    //   13: invokestatic 22	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:canRequestProceed	(Ljavax/servlet/http/HttpServletRequest;)Z
    //   16: istore_3
    //   17: iload_3
    //   18: ifne +24 -> 42
    //   21: aload_0
    //   22: ldc 23
    //   24: invokevirtual 24	com/go2group/synapse/rest/pub/RequirementPublicREST:rateLimitExceeded	(Ljava/lang/String;)Ljavax/ws/rs/core/Response;
    //   27: astore 4
    //   29: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   32: astore 5
    //   34: aload 5
    //   36: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   39: aload 4
    //   41: areturn
    //   42: aload_0
    //   43: getfield 2	com/go2group/synapse/rest/pub/RequirementPublicREST:issueManager	Lcom/atlassian/jira/issue/IssueManager;
    //   46: aload_1
    //   47: invokeinterface 26 2 0
    //   52: astore 4
    //   54: aload 4
    //   56: ifnonnull +47 -> 103
    //   59: getstatic 10	com/go2group/synapse/rest/pub/RequirementPublicREST:log	Lorg/apache/log4j/Logger;
    //   62: new 12	java/lang/StringBuilder
    //   65: dup
    //   66: invokespecial 13	java/lang/StringBuilder:<init>	()V
    //   69: ldc 27
    //   71: invokevirtual 15	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   74: aload_1
    //   75: invokevirtual 15	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   78: invokevirtual 16	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   81: invokevirtual 17	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   84: aload_0
    //   85: invokevirtual 131	com/go2group/synapse/rest/pub/RequirementPublicREST:notFound	()Ljavax/ws/rs/core/Response;
    //   88: astore 5
    //   90: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   93: astore 6
    //   95: aload 6
    //   97: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   100: aload 5
    //   102: areturn
    //   103: aload_0
    //   104: invokevirtual 31	com/go2group/synapse/rest/pub/RequirementPublicREST:hasValidLicense	()Z
    //   107: ifne +30 -> 137
    //   110: getstatic 10	com/go2group/synapse/rest/pub/RequirementPublicREST:log	Lorg/apache/log4j/Logger;
    //   113: ldc 32
    //   115: invokevirtual 17	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   118: aload_0
    //   119: invokevirtual 132	com/go2group/synapse/rest/pub/RequirementPublicREST:forbidden	()Ljavax/ws/rs/core/Response;
    //   122: astore 5
    //   124: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   127: astore 6
    //   129: aload 6
    //   131: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   134: aload 5
    //   136: areturn
    //   137: aload_0
    //   138: aload 4
    //   140: invokevirtual 90	com/go2group/synapse/rest/pub/RequirementPublicREST:hasViewPermission	(Lcom/atlassian/jira/issue/Issue;)Z
    //   143: ifne +30 -> 173
    //   146: getstatic 10	com/go2group/synapse/rest/pub/RequirementPublicREST:log	Lorg/apache/log4j/Logger;
    //   149: ldc -123
    //   151: invokevirtual 17	org/apache/log4j/Logger:debug	(Ljava/lang/Object;)V
    //   154: aload_0
    //   155: invokevirtual 132	com/go2group/synapse/rest/pub/RequirementPublicREST:forbidden	()Ljavax/ws/rs/core/Response;
    //   158: astore 5
    //   160: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   163: astore 6
    //   165: aload 6
    //   167: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   170: aload 5
    //   172: areturn
    //   173: new 147	com/go2group/synapse/bean/JQLBugBean
    //   176: dup
    //   177: invokespecial 148	com/go2group/synapse/bean/JQLBugBean:<init>	()V
    //   180: astore 5
    //   182: aload_0
    //   183: getfield 2	com/go2group/synapse/rest/pub/RequirementPublicREST:issueManager	Lcom/atlassian/jira/issue/IssueManager;
    //   186: aload_1
    //   187: invokeinterface 26 2 0
    //   192: astore 6
    //   194: new 12	java/lang/StringBuilder
    //   197: dup
    //   198: invokespecial 13	java/lang/StringBuilder:<init>	()V
    //   201: ldc -107
    //   203: invokevirtual 15	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   206: aload 6
    //   208: invokeinterface 105 1 0
    //   213: invokevirtual 20	java/lang/StringBuilder:append	(Ljava/lang/Object;)Ljava/lang/StringBuilder;
    //   216: ldc -106
    //   218: invokevirtual 15	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   221: invokevirtual 16	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   224: astore 7
    //   226: aload 5
    //   228: aload 7
    //   230: invokevirtual 151	com/go2group/synapse/bean/JQLBugBean:setRequirementIdParams	(Ljava/lang/String;)V
    //   233: aload_0
    //   234: getfield 7	com/go2group/synapse/rest/pub/RequirementPublicREST:cycleService	Lcom/go2group/synapse/service/TestCycleService;
    //   237: aload 5
    //   239: invokeinterface 152 2 0
    //   244: astore 8
    //   246: aload 8
    //   248: invokestatic 59	com/atlassian/jira/component/ComponentAccessor:getJiraAuthenticationContext	()Lcom/atlassian/jira/security/JiraAuthenticationContext;
    //   251: invokeinterface 60 1 0
    //   256: invokestatic 153	com/go2group/synapse/util/PluginUtil:getIssueWrapperWithViewPermission	(Ljava/util/Collection;Lcom/atlassian/jira/user/ApplicationUser;)Ljava/util/List;
    //   259: astore 9
    //   261: aload 9
    //   263: invokestatic 113	javax/ws/rs/core/Response:ok	(Ljava/lang/Object;)Ljavax/ws/rs/core/Response$ResponseBuilder;
    //   266: invokevirtual 114	javax/ws/rs/core/Response$ResponseBuilder:build	()Ljavax/ws/rs/core/Response;
    //   269: astore 10
    //   271: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   274: astore 11
    //   276: aload 11
    //   278: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   281: aload 10
    //   283: areturn
    //   284: astore 5
    //   286: getstatic 10	com/go2group/synapse/rest/pub/RequirementPublicREST:log	Lorg/apache/log4j/Logger;
    //   289: aload 5
    //   291: invokevirtual 83	com/go2group/synapse/core/exception/InvalidDataException:getMessage	()Ljava/lang/String;
    //   294: invokevirtual 84	org/apache/log4j/Logger:error	(Ljava/lang/Object;)V
    //   297: getstatic 10	com/go2group/synapse/rest/pub/RequirementPublicREST:log	Lorg/apache/log4j/Logger;
    //   300: aload 5
    //   302: invokevirtual 83	com/go2group/synapse/core/exception/InvalidDataException:getMessage	()Ljava/lang/String;
    //   305: aload 5
    //   307: invokevirtual 85	org/apache/log4j/Logger:debug	(Ljava/lang/Object;Ljava/lang/Throwable;)V
    //   310: aload_0
    //   311: aload 5
    //   313: invokevirtual 86	com/go2group/synapse/rest/pub/RequirementPublicREST:error	(Ljava/lang/Exception;)Ljavax/ws/rs/core/Response;
    //   316: astore 6
    //   318: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   321: astore 7
    //   323: aload 7
    //   325: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   328: aload 6
    //   330: areturn
    //   331: astore_2
    //   332: getstatic 10	com/go2group/synapse/rest/pub/RequirementPublicREST:log	Lorg/apache/log4j/Logger;
    //   335: aload_2
    //   336: invokevirtual 88	java/lang/Exception:getMessage	()Ljava/lang/String;
    //   339: invokevirtual 84	org/apache/log4j/Logger:error	(Ljava/lang/Object;)V
    //   342: getstatic 10	com/go2group/synapse/rest/pub/RequirementPublicREST:log	Lorg/apache/log4j/Logger;
    //   345: aload_2
    //   346: invokevirtual 88	java/lang/Exception:getMessage	()Ljava/lang/String;
    //   349: aload_2
    //   350: invokevirtual 85	org/apache/log4j/Logger:debug	(Ljava/lang/Object;Ljava/lang/Throwable;)V
    //   353: aload_0
    //   354: aload_2
    //   355: invokevirtual 86	com/go2group/synapse/rest/pub/RequirementPublicREST:error	(Ljava/lang/Exception;)Ljavax/ws/rs/core/Response;
    //   358: astore_3
    //   359: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   362: astore 4
    //   364: aload 4
    //   366: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   369: aload_3
    //   370: areturn
    //   371: astore 12
    //   373: invokestatic 21	com/atlassian/jira/web/ExecutingHttpRequest:get	()Ljavax/servlet/http/HttpServletRequest;
    //   376: astore 13
    //   378: aload 13
    //   380: invokestatic 25	com/go2group/synapse/service/impl/ratelimit/DefaultRestRateLimiter:endCall	(Ljavax/servlet/http/HttpServletRequest;)V
    //   383: aload 12
    //   385: athrow
    // Line number table:
    //   Java source line #580	-> byte code offset #0
    //   Java source line #582	-> byte code offset #8
    //   Java source line #583	-> byte code offset #12
    //   Java source line #584	-> byte code offset #17
    //   Java source line #585	-> byte code offset #21
    //   Java source line #626	-> byte code offset #29
    //   Java source line #627	-> byte code offset #34
    //   Java source line #585	-> byte code offset #39
    //   Java source line #588	-> byte code offset #42
    //   Java source line #590	-> byte code offset #54
    //   Java source line #591	-> byte code offset #59
    //   Java source line #592	-> byte code offset #84
    //   Java source line #626	-> byte code offset #90
    //   Java source line #627	-> byte code offset #95
    //   Java source line #592	-> byte code offset #100
    //   Java source line #596	-> byte code offset #103
    //   Java source line #597	-> byte code offset #110
    //   Java source line #598	-> byte code offset #118
    //   Java source line #626	-> byte code offset #124
    //   Java source line #627	-> byte code offset #129
    //   Java source line #598	-> byte code offset #134
    //   Java source line #602	-> byte code offset #137
    //   Java source line #603	-> byte code offset #146
    //   Java source line #604	-> byte code offset #154
    //   Java source line #626	-> byte code offset #160
    //   Java source line #627	-> byte code offset #165
    //   Java source line #604	-> byte code offset #170
    //   Java source line #608	-> byte code offset #173
    //   Java source line #609	-> byte code offset #182
    //   Java source line #610	-> byte code offset #194
    //   Java source line #611	-> byte code offset #226
    //   Java source line #612	-> byte code offset #233
    //   Java source line #613	-> byte code offset #246
    //   Java source line #614	-> byte code offset #261
    //   Java source line #626	-> byte code offset #271
    //   Java source line #627	-> byte code offset #276
    //   Java source line #614	-> byte code offset #281
    //   Java source line #616	-> byte code offset #284
    //   Java source line #617	-> byte code offset #286
    //   Java source line #618	-> byte code offset #297
    //   Java source line #619	-> byte code offset #310
    //   Java source line #626	-> byte code offset #318
    //   Java source line #627	-> byte code offset #323
    //   Java source line #619	-> byte code offset #328
    //   Java source line #621	-> byte code offset #331
    //   Java source line #622	-> byte code offset #332
    //   Java source line #623	-> byte code offset #342
    //   Java source line #624	-> byte code offset #353
    //   Java source line #626	-> byte code offset #359
    //   Java source line #627	-> byte code offset #364
    //   Java source line #624	-> byte code offset #369
    //   Java source line #626	-> byte code offset #371
    //   Java source line #627	-> byte code offset #378
    //   Java source line #628	-> byte code offset #383
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	386	0	this	RequirementPublicREST
    //   0	386	1	reqKey	String
    //   11	2	2	request	HttpServletRequest
    //   331	24	2	e	Exception
    //   16	354	3	canProceed	boolean
    //   27	13	4	localResponse1	Response
    //   52	87	4	reqIssue	Object
    //   362	3	4	request	HttpServletRequest
    //   32	139	5	request	HttpServletRequest
    //   180	58	5	bugBean	com.go2group.synapse.bean.JQLBugBean
    //   284	28	5	e	InvalidDataException
    //   93	3	6	request	HttpServletRequest
    //   127	3	6	request	HttpServletRequest
    //   163	3	6	request	HttpServletRequest
    //   192	137	6	issue	Issue
    //   224	5	7	reqIds	String
    //   321	3	7	request	HttpServletRequest
    //   244	3	8	bugsId	Set<String>
    //   259	3	9	issueWrapperBeans	List<IssueWrapperBean>
    //   269	13	10	localResponse2	Response
    //   274	3	11	request	HttpServletRequest
    //   371	13	12	localObject1	Object
    //   376	3	13	request	HttpServletRequest
    // Exception table:
    //   from	to	target	type
    //   173	271	284	com/go2group/synapse/core/exception/InvalidDataException
    //   8	29	331	java/lang/Exception
    //   42	90	331	java/lang/Exception
    //   103	124	331	java/lang/Exception
    //   137	160	331	java/lang/Exception
    //   173	271	331	java/lang/Exception
    //   284	318	331	java/lang/Exception
    //   8	29	371	finally
    //   42	90	371	finally
    //   103	124	371	finally
    //   137	160	371	finally
    //   173	271	371	finally
    //   284	318	371	finally
    //   331	359	371	finally
    //   371	373	371	finally
  }
  
  @GET
  @Path("{projectKey}/requirementSuites")
  @XsrfProtectionExcluded
  public Response getRequirementSuites(@PathParam("projectKey") String projectKey)
  {
    log.debug("Get Requirement Suites of a project request received..");
    try {
      HttpServletRequest request = ExecutingHttpRequest.get();
      canProceed = DefaultRestRateLimiter.canRequestProceed(request);
      HttpServletRequest request; if (!canProceed) {
        return rateLimitExceeded("Too many Requests");
      }
      
      if (StringUtils.isNotBlank(projectKey)) {
        log.debug("Retrieving requirement suites for project :" + projectKey);
        Project project = null;
        project = ComponentAccessor.getProjectManager().getProjectObjByKeyIgnoreCase(projectKey);
        if (project == null) {
          project = ComponentAccessor.getProjectManager().getProjectObjByName(projectKey);
        }
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
        try {
          Collection<ReqSuiteOutputBean> reqSuites = requirementSuiteService.getRootSuites(project.getId());
          rootRequirements = requirementService.getRootRequirements(project.getId());
          if (reqSuites == null) {
            reqSuites = new ArrayList();
          }
          if (rootRequirements == null) {
            rootRequirements = new ArrayList();
          }
          ReqRestOutputBean reqRestResponse = new ReqRestOutputBean(reqSuites, rootRequirements);
          HttpServletRequest request; return Response.ok(reqRestResponse).build();
        } catch (InvalidDataException e) { List<Issue> rootRequirements;
          log.error(e.getMessage());
          log.debug(e.getMessage(), e);
          HttpServletRequest request; return error(e);
        }
      } } catch (Exception e) { boolean canProceed;
      HttpServletRequest request;
      log.error(e.getMessage());
      log.debug(e.getMessage(), e);
      HttpServletRequest request; return error(e);
    } finally {
      HttpServletRequest request = ExecutingHttpRequest.get();
      DefaultRestRateLimiter.endCall(request);
    }
    return null;
  }
  
  @Path("requirementSuite/{reqSuiteId}")
  @GET
  @XsrfProtectionExcluded
  public Response getRequirementSuite(@PathParam("reqSuiteId") String reqSuiteId) {
    log.debug("Get Requirement Suite request received..");
    try {
      HttpServletRequest request = ExecutingHttpRequest.get();
      canProceed = DefaultRestRateLimiter.canRequestProceed(request);
      if (!canProceed) { HttpServletRequest request;
        return rateLimitExceeded("Too many Requests");
      }
      
      if (StringUtils.isNotBlank(reqSuiteId)) {
        log.debug("Retrieving requirement suite with id :" + reqSuiteId);
        ReqSuiteOutputBean reqSuite = requirementSuiteService.getSuite(Integer.valueOf(reqSuiteId));
        
        Project project = null;
        Response localResponse2; if ((reqSuite == null) || (reqSuite.getProject() == null)) {
          log.debug("Requirement suite not found for suite id:" + reqSuiteId);
          HttpServletRequest request; return notFound(i18n.getText("servererror.rest.requirement.suite.notfound", reqSuiteId));
        }
        if (project == null) {
          project = ComponentAccessor.getProjectManager().getProjectObj(reqSuite.getProject());
        }
        if (project == null) {
          log.debug("Project not found for key:" + reqSuite.getProject());
          HttpServletRequest request; return notFound(i18n.getText("servererror.rest.invalid.project", reqSuite.getProject()));
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
        
        Collection<ReqSuiteMemberOutputBean> reqMembers = requirementSuiteService.getSuiteMembers(Integer.valueOf(reqSuiteId), null, null, PluginConstant.NO_PAGINATION, Integer.valueOf(0));
        Collection<ReqSuiteOutputBean> reqSuites = new ArrayList();
        List<Issue> suiteRequirements = new ArrayList();
        if ((reqMembers != null) && (reqMembers.size() > 0)) {
          for (ReqSuiteMemberOutputBean member : reqMembers)
            if (member.getMemberType().equals(PluginConstant.REQ_SUITE_MEMBER_TYPE_SUITE)) {
              ReqSuiteOutputBean suite = requirementSuiteService.getSuite(Integer.valueOf(member.getMember().intValue()));
              reqSuites.add(suite);
            } else if (member.getMemberType().equals(PluginConstant.REQ_SUITE_MEMBER_TYPE_ISSUE)) {
              requirement = issueManager.getIssueObject(member.getMember());
              suiteRequirements.add(requirement);
            }
        }
        Issue requirement;
        String rootPath = requirementSuiteService.getRootPath(reqSuite);
        ReqRestOutputBean reqRestResponse = new ReqRestOutputBean(reqSuites, suiteRequirements, reqSuite.getId(), reqSuite.getName(), reqSuite.getProjectId(), rootPath);
        HttpServletRequest request; return Response.ok(reqRestResponse).build();
      } } catch (Exception e) { boolean canProceed;
      HttpServletRequest request;
      log.error(e.getMessage());
      log.debug(e.getMessage(), e);
      HttpServletRequest request; return error(e);
    } finally {
      HttpServletRequest request = ExecutingHttpRequest.get();
      DefaultRestRateLimiter.endCall(request);
    }
    return null;
  }
  
  @GET
  @Path("{reqKey}/linkedRequirementSuites")
  @XsrfProtectionExcluded
  public Response getLinkedRequirementSuites(@PathParam("reqKey") String reqKey) {
    try {
      if (log.isDebugEnabled()) {
        log.debug("Get Linked Requirement Suites to requirement:" + reqKey);
      }
      HttpServletRequest request = ExecutingHttpRequest.get();
      canProceed = DefaultRestRateLimiter.canRequestProceed(request);
      HttpServletRequest request; if (!canProceed) {
        return rateLimitExceeded("Too many Requests");
      }
      
      Object reqIssue = issueManager.getIssueByKeyIgnoreCase(reqKey);
      
      if (reqIssue == null) {
        log.debug("Requirement issue not found for key:" + reqKey);
        HttpServletRequest request; return notFound(i18n.getText("servererror.rest.requirement.notfound", reqKey));
      }
      
      if (!hasValidLicense()) {
        log.debug("Invalid license");
        HttpServletRequest request; return forbidden(i18n.getText("servererror.rest.invalid.license"));
      }
      
      if (!hasViewPermission((Issue)reqIssue)) {
        log.debug("Does not have enough view permission on the issue");
        HttpServletRequest request; return forbidden(i18n.getText("servererror.rest.no.edit.permission"));
      }
      
      if (!hasSynapsePermission(((Issue)reqIssue).getProjectObject(), SynapsePermission.BROWSE_SYNAPSE_PANELS)) {
        log.debug("User does not have permission to browse project");
        HttpServletRequest request; return forbidden(i18n.getText("synapse.gadget.error.browse.permission.project"));
      }
      
      RequirementTree requirementTree = requirementService.getRequirementTree(((Issue)reqIssue).getId());
      List<Issue> parents = getParents(requirementTree);
      
      ReqSuiteMemberOutputBean reqSuiteBean = null;
      if ((parents != null) && (parents.size() > 0)) {
        reqSuiteBean = requirementSuiteService.getMemberRepresentation(((Issue)parents.get(parents.size() - 1)).getId());
      } else {
        reqSuiteBean = requirementSuiteService.getMemberRepresentation(((Issue)reqIssue).getId());
        parents = new ArrayList();
      }
      String reqSuite = "";
      if ((reqSuiteBean != null) && (reqSuiteBean.getReqSuiteOutputBean() != null)) {
        reqSuite = requirementSuiteService.getRootPathName(reqSuiteBean);
      }
      if (reqSuite == null) {
        reqSuite = "";
      }
      ReqIssueBean reqIssueBean = new ReqIssueBean((Issue)reqIssue, reqSuite, parents);
      HttpServletRequest request; return Response.ok(reqIssueBean).build();
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
  
  private List<Issue> recursiveChildren(List<Issue> tmpChildReqs, List<Issue> childReqs)
  {
    List<Issue> childTmpReq = new ArrayList();
    for (Issue childReq : tmpChildReqs) {
      try {
        childTmpReq = requirementService.getChildren(childReq.getId());
      } catch (InvalidDataException e) {
        log.debug(e.getMessage(), e);
      }
      
      if ((childTmpReq != null) && (childTmpReq.size() > 0)) {
        childReqs.addAll(childTmpReq);
        recursiveChildren(childTmpReq, childReqs);
      }
    }
    return childReqs;
  }
  
  private List<Issue> getParents(RequirementTree requirementTree) {
    List<Issue> parentIssues = new ArrayList();
    RequirementTree.ParentTree parent = requirementTree.getParent();
    if (parent != null) {
      getParents(parent, parentIssues);
    }
    
    return parentIssues;
  }
  
  private void getParents(RequirementTree.ParentTree parentTree, List<Issue> parentBranch) {
    if (parentTree.getParent() == null) {
      parentBranch.add(parentTree.getIssue());
    } else {
      parentBranch.add(parentTree.getIssue());
      getParents(parentTree.getParent(), parentBranch);
    }
  }
}
