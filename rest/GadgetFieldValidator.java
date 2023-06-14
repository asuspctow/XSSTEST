package com.go2group.synapse.rest;

import com.atlassian.jira.util.I18nHelper;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.go2group.synapse.bean.ErrorCollection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.apache.log4j.Logger;





@Path("gadgetValidation")
@Produces({"application/json"})
public class GadgetFieldValidator
{
  private static final Logger log = Logger.getLogger(GadgetFieldValidator.class);
  private final I18nHelper i18n;
  
  public GadgetFieldValidator(@ComponentImport I18nHelper i18n)
  {
    this.i18n = i18n;
  }
  

  @Path("validate")
  @GET
  public Response validateTestPlanGadget(@QueryParam("project") String project, @QueryParam("testplan") String testplan, @QueryParam("testcycle") String testcycle)
  {
    log.debug("Invoked validateTestPlanGadget");
    
    ErrorCollection errorCollection = new ErrorCollection();
    
    log.debug("Received project value : " + project);
    
    validateProject(project, errorCollection);
    validateTestPlan(testplan, errorCollection);
    validateTestCycle(testcycle, errorCollection);
    
    return createValidationResponse(errorCollection);
  }
  
  @Path("testcaseburndown")
  @GET
  public Response validateTestCaseBurndown(@QueryParam("project") String project, @QueryParam("testplan") String testplan, @QueryParam("testcycle") String testcycle)
  {
    log.debug("Invoked burndownchart gadget validation");
    
    ErrorCollection errorCollection = new ErrorCollection();
    
    validateProject(project, errorCollection);
    validateTestPlan(testplan, errorCollection);
    validateTestCycle(testcycle, errorCollection);
    
    return createValidationResponse(errorCollection);
  }
  
  @Path("testcasetesterstatistics")
  @GET
  public Response validateTestCaseTesterStatistics(@QueryParam("project") String project, @QueryParam("testplan") String testplan, @QueryParam("testcycle") String testcycle)
  {
    log.debug("Invoked test case to tester statistics gadget validation");
    
    ErrorCollection errorCollection = new ErrorCollection();
    
    validateProject(project, errorCollection);
    validateTestPlan(testplan, errorCollection);
    

    return createValidationResponse(errorCollection);
  }
  
  @Path("defecttestcyclestatistics")
  @GET
  public Response validateDefectTestCycleStatistics(@QueryParam("project") String project, @QueryParam("testplan") String testplan, @QueryParam("testcycle") String testcycle)
  {
    log.debug("Invoked test case to Defect Test Cycle statistics gadget validation");
    
    ErrorCollection errorCollection = new ErrorCollection();
    
    validateProject(project, errorCollection);
    validateTestPlan(testplan, errorCollection);
    validateTestCycle(testcycle, errorCollection);
    
    return createValidationResponse(errorCollection);
  }
  
  @Path("testcasefieldstatistics")
  @GET
  public Response validateTestCaseFieldStatistics(@QueryParam("project") String project, @QueryParam("testplan") String testplan, @QueryParam("testcycle") String testcycle, @QueryParam("field") String field)
  {
    log.debug("Invoked test case to tester statistics gadget validation");
    
    ErrorCollection errorCollection = new ErrorCollection();
    
    validateProject(project, errorCollection);
    validateTestPlan(testplan, errorCollection);
    validateField(field, errorCollection);
    
    return createValidationResponse(errorCollection);
  }
  
  @Path("testcaseexecutionsduringperiod")
  @GET
  public Response validateTestCaseExecutionsDuringPeriod(@QueryParam("project") String project, @QueryParam("periodstartdate") String startDate, @QueryParam("periodenddate") String endDate) throws ParseException
  {
    log.debug("Invoked burndownchart gadget validation");
    
    ErrorCollection errorCollection = new ErrorCollection();
    
    validateProject(project, errorCollection);
    validateStartDate(startDate, errorCollection);
    validateEndDate(endDate, errorCollection);
    validateDates(startDate, endDate, errorCollection);
    
    return createValidationResponse(errorCollection);
  }
  
  private void validateStartDate(String startDate, ErrorCollection errorCollection) throws ParseException {
    log.debug("Received start date value : " + startDate);
    
    if ((startDate == null) || (startDate.trim().length() == 0)) {
      errorCollection.addError("periodstartdate", i18n.getText("synapse.gadget.testcase.executions.during.period.startdate.message"));
    }
  }
  
  private void validateEndDate(String endDate, ErrorCollection errorCollection) throws ParseException {
    log.debug("Receivedend date value : " + endDate);
    
    if ((endDate == null) || (endDate.trim().length() == 0)) {
      errorCollection.addError("periodenddate", i18n.getText("synapse.gadget.testcase.executions.during.period.enddate.message"));
    }
  }
  
  private void validateDates(String startDate, String endDate, ErrorCollection errorCollection) throws ParseException {
    log.debug("Received start date and end date value : " + startDate + "," + endDate);
    
    if ((startDate.trim().length() > 0) && (endDate.trim().length() > 0)) {
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
      Date date1 = sdf.parse(startDate);
      Date date2 = sdf.parse(endDate);
      if (date1.after(date2)) {
        errorCollection.addError("periodstartdate", i18n.getText("synapse.gadget.testcase.executions.during.period.date.error.message"));
      }
    }
  }
  
  private void validateProject(String project, ErrorCollection errorCollection) {
    log.debug("Received project value : " + project);
    
    if ((project == null) || (project.trim().length() == 0)) {
      errorCollection.addError("project", i18n.getText("synapse.gadget.common.validation.message.project"));
    }
  }
  
  private void validateTestPlan(String testPlan, ErrorCollection errorCollection) {
    log.debug("Received Test Plan value : " + testPlan);
    
    if ((testPlan == null) || (testPlan.trim().length() == 0)) {
      errorCollection.addError("testplan", i18n.getText("synapse.gadget.common.validation.message.testplan"));
    }
  }
  
  private void validateTestCycle(String testCycle, ErrorCollection errorCollection) {
    log.debug("Received Test Cycle value : " + testCycle);
    
    if ((testCycle == null) || (testCycle.trim().length() == 0)) {
      errorCollection.addError("testcycle", i18n.getText("synapse.gadget.common.validation.message.testcycle"));
    }
  }
  
  private void validateField(String field, ErrorCollection errorCollection) {
    log.debug("Received Field value : " + field);
    
    if ((field == null) || (field.trim().length() == 0)) {
      errorCollection.addError("field", i18n.getText("synapse.gadget.common.validation.message.field"));
    }
  }
  











































  private Response createErrorResponse(ErrorCollection errorCollection)
  {
    return 
      Response.status(400).entity(errorCollection).cacheControl(CacheControl.valueOf("no-cache")).build();
  }
  
  protected Response createValidationResponse(ErrorCollection errorCollection)
  {
    if (errorCollection.getErrors().isEmpty()) {
      return 
      
        Response.ok("No input validation errors found.").cacheControl(CacheControl.valueOf("no-cache")).build();
    }
    
    return createErrorResponse(errorCollection);
  }
}
