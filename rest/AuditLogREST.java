package com.go2group.synapse.rest;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.UserProjectHistoryManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.json.JSONArray;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.csvreader.CsvWriter;
import com.go2group.synapse.bean.ActiveJobOutputBean;
import com.go2group.synapse.bean.DataMapResponseWrapper;
import com.go2group.synapse.bean.HtmlResponseWrapper;
import com.go2group.synapse.bean.currentactivity.CurrentActivityOutputBean;
import com.go2group.synapse.core.audit.log.ActionEnum;
import com.go2group.synapse.core.audit.log.AuditLogDisplayBean;
import com.go2group.synapse.core.audit.log.AuditLogInputBean;
import com.go2group.synapse.core.audit.log.AuditLogSearchBean;
import com.go2group.synapse.core.audit.log.ModuleEnum;
import com.go2group.synapse.core.audit.log.ModuleOutputBean;
import com.go2group.synapse.core.audit.log.SourceEnum;
import com.go2group.synapse.core.audit.log.service.AuditLogService;
import com.go2group.synapse.core.constant.PluginConstant;
import com.go2group.synapse.core.exception.InvalidDataException;
import com.go2group.synapse.enums.CurrentActivityActionEnum;
import com.go2group.synapse.enums.CurrentActivityStatusEnum;
import com.go2group.synapse.helper.FileHelper;
import com.go2group.synapse.service.ActiveJobService;
import com.go2group.synapse.service.currentactivity.CurrentActivityService;
import com.go2group.synapse.util.PluginUtil;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;






@Path("auditLog")
@Consumes({"application/json"})
@Produces({"application/json"})
public class AuditLogREST
{
  private static final Logger log = Logger.getLogger(AuditLogREST.class);
  private final AuditLogService auditLogService;
  private final I18nHelper i18n;
  private final CurrentActivityService currentActivityService;
  private static UserProjectHistoryManager userProjectHistoryManager = (UserProjectHistoryManager)ComponentAccessor.getOSGiComponentInstanceOfType(UserProjectHistoryManager.class);
  private final ActiveJobService activeJobService;
  
  @Autowired
  public AuditLogREST(@ComponentImport I18nHelper i18n, AuditLogService auditLogService, CurrentActivityService currentActivityService, ActiveJobService activeJobService) {
    this.i18n = i18n;
    this.auditLogService = auditLogService;
    this.currentActivityService = currentActivityService;
    this.activeJobService = activeJobService;
  }
  
  @Path("searchLog")
  @POST
  public Response searchLog(String data, @QueryParam("pageNumber") Integer pageNumber) {
    try {
      JSONObject jsonObject = new JSONObject(data);
      JSONArray auditData = jsonObject.has("auditData") ? jsonObject.getJSONArray("auditData") : null;
      
      List<String> auditlog = new ArrayList();
      if ((auditData != null) && (auditData.length() > 0)) {
        for (int index = 0; index < auditData.length(); index++) {
          auditlog.add(auditData.getString(index).toLowerCase());
        }
      }
      int displayPageSize = 100;
      AuditLogSearchBean auditLogSearch = auditLogService.searchLog(auditlog, pageNumber, Integer.valueOf(displayPageSize));
      
      return Response.ok(auditLogSearch).build();
    } catch (Exception ex) {
      log.error(ex.getMessage());
      log.debug(ex.getMessage(), ex); }
    return 
    

      Response.serverError().entity(i18n.getText("errormessage.unexpected.server.error") + i18n.getText("servererror.rest.testrun.assign.failed")).build();
  }
  
  @Path("exportLog")
  @POST
  public Response exportLog(String data, @QueryParam("pageNumber") final Integer pageNumber)
  {
    final CsvWriter csvWriter = null;
    final Character delimiter = null;
    final ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
    Long userID = user.getId();
    Project project = userProjectHistoryManager.getCurrentProject(10, user);
    final Long projectID = project.getId();
    
    try
    {
      JSONObject jsonObject = new JSONObject(data);
      final JSONArray auditData = jsonObject.has("auditData") ? jsonObject.getJSONArray("auditData") : null;
      final String progressKey = jsonObject.getString("progressKey");
      Thread thread = new Thread(new Runnable()
      {
        public void run()
        {
          try {
            try {
              activeJobService.createKey(progressKey, null);
            } catch (InvalidDataException e) {
              AuditLogREST.log.debug("move test suite -  excption : " + e.getMessage(), e);
            }
            exportLogs(progressKey, csvWriter, delimiter, auditData, pageNumber, user, projectID);


          }
          catch (Exception e)
          {


            e.printStackTrace();



          }
          


        }
        



      });
      currentActivityService.addCurrentActivity("AUDIT_LOG", user.getId(), project.getId(), CurrentActivityActionEnum.ADD_AUDIT_LOG.getName(), new Timestamp(new Date().getTime()), CurrentActivityStatusEnum.STARTED.getName());
      thread.start();
      
      return Response.ok(new HtmlResponseWrapper("SUCCESS")).build();
    }
    catch (Exception ex) {
      log.error(ex.getMessage());
      log.debug(ex.getMessage(), ex); }
    return 
      Response.serverError().entity(i18n.getText("errormessage.unexpected.server.error") + i18n.getText("servererror.rest.testrun.assign.failed")).build();
  }
  
  public void exportLogs(String progressKey, CsvWriter csvWriter, Character delimiter, JSONArray auditData, Integer pageNumber, ApplicationUser user, Long projectID)
  {
    try {
      SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
      JiraHome jirahome = (JiraHome)ComponentAccessor.getComponentOfType(JiraHome.class);
      String filePath = jirahome.getExportDirectory() + "/AuditLog-" + sdf.format(new Date()) + ".csv";
      File file = new File(filePath);
      file.createNewFile();
      csvWriter = initWriter(file, csvWriter, delimiter);
      populateHeader(csvWriter);
      


      AuditLogInputBean auditLogInputBean = PluginUtil.getAuditLogInputBean(user);
      auditLogInputBean.setAction(ActionEnum.EXPORTED);
      auditLogInputBean.setModule(ModuleEnum.AUDIT_LOG);
      auditLogInputBean.setSource(SourceEnum.WEB_PAGE.getName());
      auditLogInputBean.setLogTime(new Date());
      String auditLog = filePath;
      auditLogInputBean.setLog(auditLog);
      auditLogService.createAuditLog(auditLogInputBean);
      

      if (auditData == null) {
        populateFields(progressKey, csvWriter, pageNumber, user.getId(), projectID);
      } else {
        populateSearchFields(progressKey, auditData, csvWriter, pageNumber, user.getId(), projectID);
      }
      return;
    }
    catch (Exception ex) {
      log.error(ex.getMessage());
      log.debug(ex.getMessage(), ex);
    } finally {
      if (csvWriter != null) {
        try {
          csvWriter.flush();
          csvWriter.close();
        } catch (Exception e) {
          log.debug(e.getMessage(), e);
          log.error(e.getMessage());
        }
      }
    }
  }
  
  private CsvWriter initWriter(File file, CsvWriter csvWriter, Character delimiter) throws IOException {
    if (csvWriter == null) {
      FileOutputStream os = new FileOutputStream(file);
      os.write(239);
      os.write(187);
      os.write(191);
      if (delimiter != null) {
        csvWriter = new CsvWriter(os, delimiter.charValue(), StandardCharsets.UTF_8);
      } else {
        csvWriter = new CsvWriter(os, ',', StandardCharsets.UTF_8);
      }
    }
    return csvWriter;
  }
  
  private void populateHeader(CsvWriter csvWriter) throws Exception
  {
    writeFirstData(csvWriter, i18n.getText("synapse.audit.log.page.tr.c1"));
    writeWithComma(csvWriter, i18n.getText("synapse.audit.log.page.tr.c2"));
    writeWithComma(csvWriter, i18n.getText("synapse.audit.log.page.tr.c3"));
    writeWithComma(csvWriter, i18n.getText("synapse.audit.log.page.tr.c4"));
    writeWithComma(csvWriter, i18n.getText("synapse.audit.log.page.tr.c5"));
    writeEndRecord(csvWriter);
  }
  
  private void writeEndRecord(CsvWriter csvWriter) throws Exception
  {
    getWriter(csvWriter).endRecord();
  }
  
  private CsvWriter getWriter(CsvWriter csvWriter) {
    return csvWriter;
  }
  
  private void writeFirstData(CsvWriter csvWriter, Object value) throws IOException {
    getWriter(csvWriter).write(getString(value));
  }
  
  private void writeWithComma(CsvWriter csvWriter, Object value) throws IOException {
    getWriter(csvWriter).write(getString(value));
  }
  
  private String getString(Object object) {
    if (object == null) {
      return "";
    }
    return object.toString();
  }
  

  private void populateFields(String progressKey, CsvWriter csvWriter, Integer pageNumber, Long userID, Long projectID)
    throws Exception
  {
    int offset = pageNumber.intValue() >= 1 ? (pageNumber.intValue() - 1) * PluginConstant.PAGE_SIZE_100.intValue() : 0;
    
    for (;;)
    {
      Collection<AuditLogDisplayBean> auditLogs = auditLogService.getAuditLogs(offset, PluginConstant.PAGE_SIZE_100.intValue());
      
      List<AuditLogDisplayBean> auditLogList = new ArrayList();
      auditLogList.addAll(auditLogs);
      

      if ((auditLogList == null) || (auditLogList.size() <= 0)) {
        break;
      }
      
      CurrentActivityOutputBean currentActivity = currentActivityService.getCurrentActivity("AUDIT_LOG", userID, projectID, CurrentActivityActionEnum.ADD_AUDIT_LOG.getName());
      if (currentActivity != null) {
        currentActivityService.updateCurrentActivity(currentActivity.getId(), CurrentActivityStatusEnum.PROCESSING.getName());
      }
      int maxvalue = auditLogList.size();
      
      displayFieldsValues(progressKey, csvWriter, pageNumber, auditLogList, maxvalue);
      
      offset = pageNumber.intValue() * PluginConstant.PAGE_SIZE_100.intValue();
      Integer localInteger1 = pageNumber;Integer localInteger2 = pageNumber = Integer.valueOf(pageNumber.intValue() + 1);
      

      currentActivity = currentActivityService.getCurrentActivity("AUDIT_LOG", userID, projectID, CurrentActivityActionEnum.ADD_AUDIT_LOG.getName());
      if (currentActivity != null) {
        currentActivityService.updateCurrentActivity(currentActivity.getId(), CurrentActivityStatusEnum.COMPLETED.getName());
      }
    }
  }
  

  private void populateSearchFields(String progressKey, JSONArray auditData, CsvWriter csvWriter, Integer pageNumber, Long userID, Long projectID)
    throws Exception
  {
    List<String> auditlog = new ArrayList();
    if ((auditData != null) && (auditData.length() > 0)) {
      for (int index = 0; index < auditData.length(); index++) {
        auditlog.add(auditData.getString(index).toLowerCase());
      }
    }
    int offset = pageNumber.intValue() >= 1 ? (pageNumber.intValue() - 1) * PluginConstant.PAGE_SIZE_100.intValue() : 0;
    
    for (;;)
    {
      int displayPageSize = PluginConstant.PAGE_SIZE_100.intValue();
      
      AuditLogSearchBean auditLogSearch = auditLogService.searchLog(auditlog, Integer.valueOf(offset), Integer.valueOf(displayPageSize));
      
      List<AuditLogDisplayBean> searchLogs = auditLogSearch.getSearchedLogs();
      if ((searchLogs == null) || (searchLogs.size() <= 0)) {
        break;
      }
      
      CurrentActivityOutputBean currentActivity = currentActivityService.getCurrentActivity("AUDIT_LOG", userID, projectID, CurrentActivityActionEnum.ADD_AUDIT_LOG.getName());
      if (currentActivity != null) {
        currentActivityService.updateCurrentActivity(currentActivity.getId(), CurrentActivityStatusEnum.PROCESSING.getName());
      }
      int maxvalue = searchLogs.size();
      displayFieldsValues(progressKey, csvWriter, pageNumber, searchLogs, maxvalue);
      offset = pageNumber.intValue() * PluginConstant.PAGE_SIZE_100.intValue();
      Integer localInteger1 = pageNumber;Integer localInteger2 = pageNumber = Integer.valueOf(pageNumber.intValue() + 1);
      
      currentActivity = currentActivityService.getCurrentActivity("AUDIT_LOG", userID, projectID, CurrentActivityActionEnum.ADD_AUDIT_LOG.getName());
      if (currentActivity != null) {
        currentActivityService.updateCurrentActivity(currentActivity.getId(), CurrentActivityStatusEnum.COMPLETED.getName());
      }
    }
  }
  
  private void displayFieldsValues(String progressKey, CsvWriter csvWriter, Integer pageNumber, List<AuditLogDisplayBean> auditLogs, int maxvalue)
    throws Exception
  {
    ArrayList<String> projects = new ArrayList();
    ArrayList<String> modules = new ArrayList();
    ArrayList<String> users = new ArrayList();
    ArrayList<String> logTime = new ArrayList();
    ArrayList<String> log = new ArrayList();
    
    for (AuditLogDisplayBean searchlog : auditLogs) {
      projects.add(searchlog.getProject());
      modules.add(searchlog.getModule().getName());
      users.add(searchlog.getUser());
      logTime.add(searchlog.getLogTime());
      log.add(searchlog.getLog());
    }
    
    float progressUnit = 0.1F;
    if (auditLogs.size() > 0) {
      progressUnit = 1.0F / auditLogs.size();
    }
    for (int i = 0; i < maxvalue; i++) {
      writeWithComma(csvWriter, projects.get(i));
      writeWithComma(csvWriter, modules.get(i));
      writeWithComma(csvWriter, users.get(i));
      writeWithComma(csvWriter, logTime.get(i));
      writeWithComma(csvWriter, log.get(i));
      writeEndRecord(csvWriter);
      ActiveJobOutputBean job = activeJobService.getJobDetails(progressKey);
      if (job != null) {
        String value = job.getValue();
        if (StringUtils.isNotBlank(value)) {
          float prgValue = Float.valueOf(value).floatValue();
          prgValue += progressUnit;
          if (prgValue > 1.0D) {
            prgValue = 1.0F;
          }
          activeJobService.updateValueForGivenKey(progressKey, String.valueOf(prgValue));
        }
      }
    }
  }
  


  @Path("exportStatus")
  @POST
  public Response exportStatus()
  {
    ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
    Long userID = user.getId();
    Project project = userProjectHistoryManager.getCurrentProject(10, user);
    Long projectID = project.getId();
    
    try
    {
      CurrentActivityOutputBean currentActivity = currentActivityService.getCurrentActivity("AUDIT_LOG", userID, projectID, CurrentActivityActionEnum.ADD_AUDIT_LOG.getName());
      AuditLogDisplayBean auditLogs = auditLogService.getAuditLog(userID, projectID, ActionEnum.EXPORTED, ModuleEnum.AUDIT_LOG);
      DataMapResponseWrapper dataMapResponseWrapper = new DataMapResponseWrapper();
      if (currentActivity != null)
      {
        String status = currentActivity.getStatus();
        
        if ((status.equals("COMPLETED")) && (!status.isEmpty())) {
          dataMapResponseWrapper.setData1(status);
          if (auditLogs != null) {
            dataMapResponseWrapper.setData2(auditLogs.getLog());
          }
        }
      }
      return Response.ok(dataMapResponseWrapper).build();
    }
    catch (Exception ex) {
      log.error(ex.getMessage());
      log.debug(ex.getMessage(), ex); }
    return 
      Response.serverError().entity(i18n.getText("errormessage.unexpected.server.error") + i18n.getText("servererror.rest.testrun.assign.failed")).build();
  }
  


  @Path("deleteAcivity")
  @POST
  public Response deleteAcivity()
  {
    ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
    Long userID = user.getId();
    Project project = userProjectHistoryManager.getCurrentProject(10, user);
    Long projectID = project.getId();
    
    try
    {
      CurrentActivityOutputBean currentActivity = currentActivityService.getCurrentActivity("AUDIT_LOG", userID, projectID, CurrentActivityActionEnum.ADD_AUDIT_LOG.getName(), CurrentActivityStatusEnum.COMPLETED.getName());
      if (currentActivity != null) {
        currentActivityService.deleteCurrentActivity(currentActivity.getId());
      }
      return Response.ok(new HtmlResponseWrapper("SUCCESS")).build();
    }
    catch (Exception ex) {
      log.error(ex.getMessage());
      log.debug(ex.getMessage(), ex); }
    return 
      Response.serverError().entity(i18n.getText("errormessage.unexpected.server.error") + i18n.getText("servererror.rest.testrun.assign.failed")).build();
  }
  

  @Path("download")
  @GET
  public Response download(@QueryParam("path") String csvPath, @QueryParam("filename") String csvFileName, @Context HttpServletResponse response)
  {
    try
    {
      ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
      boolean isValidFile = FileHelper.validateFileInput(user, csvPath);
      if (!isValidFile) {
        return Response.status(404).build();
      }
      
      String csv = URLDecoder.decode(csvPath, "UTF-8");
      String csvFile = URLDecoder.decode(csvFileName, "UTF-8");
      SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
      response.setContentType("application/octet-stream");
      response.setHeader("Content-Disposition", "attachment; filename= AuditLog-" + sdf.format(new Date()) + ".zip");
      ServletOutputStream out = response.getOutputStream();
      
      ZipOutputStream zipStream = new ZipOutputStream(out);
      zipStream.putNextEntry(new ZipEntry(csvFile));
      
      byte[] bytes = Files.readAllBytes(Paths.get(csv, new String[0]));
      zipStream.write(bytes, 0, bytes.length);
      zipStream.closeEntry();
      zipStream.close();
      return Response.ok("SUCCESS").build();
    }
    catch (InvalidDataException e) {
      log.debug(e.getMessage(), e);
      log.error(e.getMessage());
      return Response.serverError().entity(e.getMessage()).build();
    } catch (Exception ex) {
      log.debug(ex.getMessage(), ex);
      log.error(ex.getMessage());
      return Response.serverError().entity(i18n.getText("errormessage.unexpected.server.error") + " " + ex.getMessage()).build();
    }
  }
}
