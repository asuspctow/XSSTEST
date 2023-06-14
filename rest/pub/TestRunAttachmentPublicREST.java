package com.go2group.synapse.rest.pub;

import com.atlassian.annotations.security.XsrfProtectionExcluded;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.plugins.rest.common.multipart.FilePart;
import com.atlassian.plugins.rest.common.multipart.MultipartConfig;
import com.atlassian.plugins.rest.common.multipart.MultipartConfigClass;
import com.atlassian.plugins.rest.common.multipart.MultipartFormParam;
import com.go2group.synapse.bean.TestCycleOutputBean;
import com.go2group.synapse.bean.TestRunAttachmentOutputBean;
import com.go2group.synapse.bean.TestRunOutputBean;
import com.go2group.synapse.bean.TestRunStepOutputBean;
import com.go2group.synapse.core.audit.log.ActionEnum;
import com.go2group.synapse.core.audit.log.AuditLogInputBean;
import com.go2group.synapse.core.audit.log.ModuleEnum;
import com.go2group.synapse.core.audit.log.SourceEnum;
import com.go2group.synapse.core.audit.log.service.AuditLogService;
import com.go2group.synapse.core.exception.InvalidDataException;
import com.go2group.synapse.helper.AttachmentFile;
import com.go2group.synapse.manager.PermissionUtilAbstract;
import com.go2group.synapse.service.TestRunService;
import com.go2group.synapse.service.impl.ratelimit.DefaultRestRateLimiter;
import com.go2group.synapse.util.PluginUtil;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
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
import org.apache.log4j.Logger;
import webwork.config.Configuration;

@Path("public/attachment/{attachrunId}")
public class TestRunAttachmentPublicREST
  extends AbstractPublicREST
{
  private final TestRunService testRunService;
  private final AuditLogService auditLogService;
  private final I18nHelper i18n;
  private static final Logger log = Logger.getLogger(TestRunAttachmentPublicREST.class);
  

  protected TestRunAttachmentPublicREST(@ComponentImport I18nHelper i18n, PermissionUtilAbstract permissionUtil, TestRunService testRunService, AuditLogService auditLogService)
  {
    super(permissionUtil);
    this.testRunService = testRunService;
    this.i18n = i18n;
    this.auditLogService = auditLogService;
  }
  
  @POST
  @Path("testrun")
  @Consumes({"multipart/form-data"})
  @Produces({"application/json"})
  @MultipartConfigClass(SynapseAttachmentMultipartConfig.class)
  @XsrfProtectionExcluded
  public Response attachToTestRun(@MultipartFormParam("file") Collection<FilePart> fileParts, @PathParam("attachrunId") int runId)
  {
    try {
      HttpServletRequest request = ExecutingHttpRequest.get();
      boolean canProceed = DefaultRestRateLimiter.canRequestProceed(request);
      Response localResponse1; if (!canProceed) { HttpServletRequest request;
        return rateLimitExceeded("Too many Requests");
      }
      

      if (!hasValidLicense()) {
        log.debug("Invalid license");
        HttpServletRequest request; return forbidden(i18n.getText("servererror.rest.invalid.license"));
      }
      
      Issue operatingIssue = null;
      TestRunOutputBean testRunOutputBean = null;
      HttpServletRequest request;
      try { operatingIssue = testRunService.getTestPlanByRun(Integer.valueOf(runId));
        testRunOutputBean = testRunService.getTestRun(Integer.valueOf(runId));
      } catch (InvalidDataException e) {
        log.debug(e.getMessage());
        return notFound(i18n.getText("servererror.rest.testrun.notfound", Integer.valueOf(runId)));
      }
      
      if (operatingIssue == null)
      {
        try {
          operatingIssue = testRunService.getTestCaseByRun(Integer.valueOf(runId));
        } catch (InvalidDataException e) {
          log.debug(e.getMessage());
        }
        



        if (!hasViewPermission(operatingIssue)) {
          log.debug("Does not have view permission on the issue");
          HttpServletRequest request; return forbidden(i18n.getText("servererror.rest.no.view.permission"));
        }
      }
      String fileName = "";
      String contentType = "";
      for (FilePart filePart : fileParts) {
        fileName = filePart.getName();
        contentType = filePart.getContentType();
        file = null;
        try {
          file = getFileFromFilePart(filePart);
          AttachmentFile attachment = new AttachmentFile(fileName, contentType, file);
          testRunService.addTestRunAttachment(Integer.valueOf(runId), attachment);
        } catch (InvalidDataException e) {
          log.debug(e.getMessage(), e);
        } catch (IOException e1) {
          log.debug(e1.getMessage(), e1);
        }
      }
      
      File file;
      AuditLogInputBean auditLogInputBean = PluginUtil.getAuditLogInputBean(ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser(), operatingIssue.getProjectObject());
      auditLogInputBean.setAction(ActionEnum.ADDED);
      auditLogInputBean.setModule(ModuleEnum.TEST_RUN);
      auditLogInputBean.setSource(SourceEnum.REST.getName());
      auditLogInputBean.setLogTime(new Date());
      String auditLog = "Added Test Run Attachment '" + fileName + "' to Test Run 'TR" + runId + "' of Test Case '" + operatingIssue.getKey() + "' through REST";
      if (!testRunOutputBean.getCycle().isAdhocTestCycle())
      {
        auditLog = "Added Test Run Attachment '" + fileName + "' to Test Run '" + operatingIssue.getKey() + "'; Test Plan:" + testRunOutputBean.getCycle().getTestPlan().getKey() + "; Test Cycle:" + testRunOutputBean.getCycle().getName() + " through REST";
      }
      auditLogInputBean.setLog(auditLog);
      auditLogService.createAuditLog(auditLogInputBean);
      HttpServletRequest request;
      return success();
    } finally {
      HttpServletRequest request = ExecutingHttpRequest.get();
      DefaultRestRateLimiter.endCall(request);
    }
  }
  
  @POST
  @Path("testrun/step/{stepNo}")
  @Consumes({"multipart/form-data"})
  @Produces({"application/json"})
  @MultipartConfigClass(SynapseAttachmentMultipartConfig.class)
  @XsrfProtectionExcluded
  public Response attachToTestRunStep(@MultipartFormParam("file") Collection<FilePart> fileParts, @PathParam("attachrunId") int runId, @PathParam("stepNo") int stepNo)
  {
    try {
      HttpServletRequest request = ExecutingHttpRequest.get();
      boolean canProceed = DefaultRestRateLimiter.canRequestProceed(request);
      Response localResponse1; if (!canProceed) { HttpServletRequest request;
        return rateLimitExceeded("Too many Requests");
      }
      

      if (!hasValidLicense()) {
        log.debug("Invalid license");
        HttpServletRequest request; return forbidden(i18n.getText("servererror.rest.invalid.license"));
      }
      
      Issue operatingIssue = null;
      TestRunOutputBean testRunOutputBean = null;
      HttpServletRequest request;
      try { operatingIssue = testRunService.getTestPlanByRun(Integer.valueOf(runId));
        testRunOutputBean = testRunService.getTestRun(Integer.valueOf(runId));
      } catch (InvalidDataException e) {
        log.debug(e.getMessage());
        return notFound(i18n.getText("servererror.rest.testrun.notfound", Integer.valueOf(runId)));
      }
      
      if (operatingIssue == null)
      {
        try {
          operatingIssue = testRunService.getTestCaseByRun(Integer.valueOf(runId));
        } catch (InvalidDataException e) {
          log.debug(e.getMessage());
        }
        



        if (!hasViewPermission(operatingIssue)) {
          log.debug("Does not have view permission on the issue");
          HttpServletRequest request; return forbidden(i18n.getText("servererror.rest.no.view.permission"));
        }
      }
      String fileName = "";
      
      String contentType = "";
      for (FilePart filePart : fileParts) {
        fileName = filePart.getName();
        contentType = filePart.getContentType();
        file = null;
        try {
          file = getFileFromFilePart(filePart);
          
          AttachmentFile attachment = new AttachmentFile(fileName, contentType, file);
          List<TestRunStepOutputBean> testRunStepBeans = testRunService.getTestRunSteps(Integer.valueOf(runId));
          if (testRunStepBeans != null) {
            Integer stepId = ((TestRunStepOutputBean)testRunStepBeans.get(stepNo - 1)).getID();
            testRunService.addTestRunStepAttachment(stepId, attachment);
          }
        } catch (InvalidDataException e) {
          log.debug(e.getMessage(), e);
        } catch (IOException e1) {
          log.debug(e1.getMessage(), e1);
        }
      }
      
      File file;
      AuditLogInputBean auditLogInputBean = PluginUtil.getAuditLogInputBean(ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser(), operatingIssue.getProjectObject());
      auditLogInputBean.setAction(ActionEnum.ADDED);
      auditLogInputBean.setModule(ModuleEnum.TEST_RUN);
      auditLogInputBean.setSource(SourceEnum.REST.getName());
      auditLogInputBean.setLogTime(new Date());
      String auditLog = "Added Test Run Attachment '" + fileName + "' to Test Run 'TR" + runId + "' and Step '" + stepNo + "' of Test Case '" + operatingIssue.getKey() + "' through REST";
      if (!testRunOutputBean.getCycle().isAdhocTestCycle())
      {
        auditLog = "Added Test Run Attachment '" + fileName + "' to Test Run '" + operatingIssue.getKey() + "' and Step '" + stepNo + "'; Test Plan:" + testRunOutputBean.getCycle().getTestPlan().getKey() + "; Test Cycle:" + testRunOutputBean.getCycle().getName() + " through REST";
      }
      auditLogInputBean.setLog(auditLog);
      auditLogService.createAuditLog(auditLogInputBean);
      HttpServletRequest request;
      return success();
    } finally {
      HttpServletRequest request = ExecutingHttpRequest.get();
      DefaultRestRateLimiter.endCall(request);
    }
  }
  
  @Path("deleteAttachment/{attachId}")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @DELETE
  public Response deleteAttachment(@PathParam("attachId") Integer attachmentId) {
    log.debug("Deleting attachment with Id:" + attachmentId);
    try {
      HttpServletRequest request = ExecutingHttpRequest.get();
      boolean canProceed = DefaultRestRateLimiter.canRequestProceed(request);
      Response localResponse1; if (!canProceed) { HttpServletRequest request;
        return rateLimitExceeded("Too many Requests");
      }
      
      if (!hasValidLicense()) {
        log.debug("Invalid license");
        HttpServletRequest request; return forbidden(i18n.getText("servererror.rest.invalid.license"));
      }
      
      Object operatingIssue = null;
      TestRunAttachmentOutputBean attachmentOutputBean = null;
      String fileName = "";
      try {
        attachmentOutputBean = testRunService.getTestRunAttachment(attachmentId);
        Long tpIssueId = attachmentOutputBean.getTestRun().getCycle().getTpId();
        operatingIssue = ComponentAccessor.getIssueManager().getIssueObject(tpIssueId);
        fileName = attachmentOutputBean.getFileName();
      } catch (InvalidDataException e) {
        log.debug(e.getMessage());
        HttpServletRequest request; return notFound(i18n.getText("servererror.rest.testrun.notfound", attachmentId));
      }
      
      if (operatingIssue == null)
      {
        try {
          operatingIssue = testRunService.getTestCaseByRun(attachmentOutputBean.getTestRun().getID());
        } catch (InvalidDataException e) {
          log.debug(e.getMessage());
        }
        



        if (!hasViewPermission((Issue)operatingIssue)) {
          log.debug("Does not have view permission on the issue");
          HttpServletRequest request; return forbidden(i18n.getText("servererror.rest.no.view.permission"));
        }
      }
      try {
        testRunService.removeAttachment(attachmentId);
        
        AuditLogInputBean auditLogInputBean = PluginUtil.getAuditLogInputBean(ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser(), ((Issue)operatingIssue).getProjectObject());
        auditLogInputBean.setAction(ActionEnum.DELETED);
        auditLogInputBean.setModule(ModuleEnum.TEST_RUN);
        auditLogInputBean.setSource(SourceEnum.REST.getName());
        auditLogInputBean.setLogTime(new Date());
        stepInfo = "";
        if (attachmentOutputBean.getTestRunStep() != null) {
          stepInfo = " and Step '" + PluginUtil.getEllipsisString(attachmentOutputBean.getTestRunStep().getStep(), 50) + "'";
        }
        String auditLog = "Deleted Test Run Attachment '" + fileName + "' from Test Run 'TR" + attachmentOutputBean.getTestRun().getID() + "'" + (String)stepInfo + " of Test Case '" + ((Issue)operatingIssue).getKey() + "' through REST";
        if (!attachmentOutputBean.getTestRun().getCycle().isAdhocTestCycle())
        {
          auditLog = "Deleted Test Run Attachment '" + fileName + "' from Test Run '" + ((Issue)operatingIssue).getKey() + "'" + (String)stepInfo + "; Test Plan:" + attachmentOutputBean.getTestRun().getCycle().getTestPlan().getKey() + "; Test Cycle:" + attachmentOutputBean.getTestRun().getCycle().getName() + " through REST";
        }
        auditLogInputBean.setLog(auditLog);
        auditLogService.createAuditLog(auditLogInputBean);
      } catch (InvalidDataException e) {
        Object stepInfo;
        log.debug(e.getMessage(), e);
        log.error(e.getMessage());
        HttpServletRequest request; return Response.serverError().entity(e.getMessage()).build();
      }
      HttpServletRequest request;
      return success();
    } finally {
      HttpServletRequest request = ExecutingHttpRequest.get();
      DefaultRestRateLimiter.endCall(request);
    }
  }
  
  @Path("getAttachmentDetails")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @GET
  public Response getAttachmentDetails(@PathParam("attachrunId") int runId) {
    log.debug("Get attachment details with Id:" + runId);
    try {
      HttpServletRequest request = ExecutingHttpRequest.get();
      boolean canProceed = DefaultRestRateLimiter.canRequestProceed(request);
      Response localResponse1; if (!canProceed) { HttpServletRequest request;
        return rateLimitExceeded("Too many Requests");
      }
      
      if (!hasValidLicense()) {
        log.debug("Invalid license");
        HttpServletRequest request; return forbidden(i18n.getText("servererror.rest.invalid.license"));
      }
      
      Issue operatingIssue = null;
      HttpServletRequest request;
      try {
        operatingIssue = testRunService.getTestPlanByRun(Integer.valueOf(runId));
      }
      catch (InvalidDataException e) {
        log.debug(e.getMessage());
        return notFound(i18n.getText("servererror.rest.testrun.notfound", Integer.valueOf(runId)));
      }
      
      if (operatingIssue == null)
      {
        try {
          operatingIssue = testRunService.getTestCaseByRun(Integer.valueOf(runId));
        } catch (InvalidDataException e) {
          log.debug(e.getMessage());
        }
        



        if (!hasViewPermission(operatingIssue)) {
          log.debug("Does not have view permission on the issue");
          HttpServletRequest request; return forbidden(i18n.getText("servererror.rest.no.view.permission"));
        }
      }
      List<TestRunAttachmentRestBean> attachmentRestBeans = new ArrayList();
      try {
        Object attachmentOutputBeans = testRunService.getTestRunAttachments(Integer.valueOf(runId));
        
        for (TestRunAttachmentOutputBean attachmentOutputBean : (List)attachmentOutputBeans) {
          TestRunAttachmentRestBean attachmentRestBean = new TestRunAttachmentRestBean();
          attachmentRestBean.setFileName(attachmentOutputBean.getFileName());
          attachmentRestBean.setId(attachmentOutputBean.getID());
          attachmentRestBean.setMimeType(attachmentOutputBean.getMimeType());
          String baseUrl = ComponentAccessor.getApplicationProperties().getString("jira.baseurl");
          attachmentRestBean.setFilePath(baseUrl + "/plugins/servlet/downloadTRAttachment?attachmentId=" + attachmentOutputBean.getID());
          attachmentRestBeans.add(attachmentRestBean);
        }
      } catch (InvalidDataException e) {
        log.debug(((InvalidDataException)e).getMessage(), (Throwable)e);
        log.error(((InvalidDataException)e).getMessage());
        HttpServletRequest request; return Response.serverError().entity(((InvalidDataException)e).getMessage()).build(); }
      HttpServletRequest request;
      return Response.ok(attachmentRestBeans).build();
    } finally {
      HttpServletRequest request = ExecutingHttpRequest.get();
      DefaultRestRateLimiter.endCall(request);
    }
  }
  
  public static class SynapseAttachmentMultipartConfig implements MultipartConfig { public SynapseAttachmentMultipartConfig() {}
    
    public long getMaxFileSize() { return TestRunAttachmentPublicREST.access$000().intValue(); }
    
    public long getMaxSize()
    {
      return TestRunAttachmentPublicREST.access$000().intValue() * 10;
    }
  }
  
  private static Integer getMaxAttachmentSize() {
    Integer maxSize;
    try {
      String maxSizeStr = Configuration.getString("webwork.multipart.maxSize");
      if (maxSizeStr != null) {
        try {
          maxSize = new Integer(maxSizeStr);
        } catch (NumberFormatException e) { Integer maxSize;
          Integer maxSize = Integer.valueOf(Integer.MAX_VALUE);
          log.warn("Property 'webwork.multipart.maxSize' with value '" + maxSizeStr + "' is not a number. Defaulting to Integer.MAX_VALUE");
        }
      } else {
        Integer maxSize = Integer.valueOf(Integer.MAX_VALUE);
        log.warn("Property 'webwork.multipart.maxSize' is not set. Defaulting to Integer.MAX_VALUE");
      }
    } catch (IllegalArgumentException e1) {
      maxSize = Integer.valueOf(Integer.MAX_VALUE);
      log.warn("Failed getting string from Configuration for 'webwork.multipart.maxSize' property. Defaulting to Integer.MAX_VALUE", e1);
    }
    return maxSize;
  }
  
  private File getFileFromFilePart(FilePart filePart) throws IOException {
    File file = File.createTempFile("attachment-", ".tmp");
    file.deleteOnExit();
    filePart.write(file);
    return file;
  }
}
