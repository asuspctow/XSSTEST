package com.go2group.synapse.rest;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.go2group.synapse.core.exception.InvalidDataException;
import com.go2group.synapse.service.TestStepService;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;





@Path("testCase")
@Consumes({"application/json"})
@Produces({"application/json"})
public class TestCaseREST
{
  private final TestStepService testStepService;
  private final IssueManager issueManager;
  private final I18nHelper i18n;
  private static final Logger log = Logger.getLogger(TestCaseREST.class);
  
  @Autowired
  public TestCaseREST(@ComponentImport IssueManager issueManager, @ComponentImport I18nHelper i18n, TestStepService testStepService) {
    this.testStepService = testStepService;
    this.issueManager = issueManager;
    this.i18n = i18n;
  }
  
  @Path("saveAutomationTestIdentifier")
  @PUT
  public Response saveAutomationTestIdentifier(@QueryParam("automationIdentifier") String automationIdentifier, @QueryParam("tcIssueId") Long tcIssueId, @Context HttpServletRequest request)
  {
    log.debug("Saving automation test identifier started..:" + tcIssueId);
    Issue issue = issueManager.getIssueObject(tcIssueId);
    
    String valueToUpdate = StringUtils.isBlank(automationIdentifier) ? "" : automationIdentifier;
    try {
      if (issue != null) {
        if ((StringUtils.isNotBlank(valueToUpdate)) && (!isValidJavaIdentifier(valueToUpdate))) {
          return 
          

            Response.serverError().entity(i18n.getText("synapse.testcase.testreference.validation.error.message")).build();
        }
        testStepService.updateAutomationTestIdentifier(valueToUpdate, issue.getId());
      }
      
      return Response.ok().build();
    } catch (InvalidDataException e) {
      log.error(e.getMessage());
      log.debug(e.getMessage(), e);
      return 
        Response.serverError()
        .entity(e.getMessage())
        .build();
    } catch (Exception e) {
      log.error(e.getMessage());
      log.debug(e.getMessage(), e); }
    return 
    

      Response.serverError().entity(i18n.getText("errormessage.unexpected.server.error") + i18n.getText("servererror.rest.teststep.add.failed")).build();
  }
  
  public boolean isValidJavaIdentifier(String automationIdentifier) throws InvalidDataException
  {
    if (automationIdentifier.length() > 500) {
      throw new InvalidDataException(i18n.getText("synapse.testcase.testreference.validation.length.message"));
    }
    char[] chars = automationIdentifier.toCharArray();
    for (int i = 0; i < chars.length; i++) {
      if ((!Character.isJavaIdentifierStart(chars[i])) && (!Character.isLetterOrDigit(chars[i])) && (!Character.isJavaIdentifierPart(chars[i])) && (chars[i] != ' ') && (chars[i] != ':') && (chars[i] != '.')) {
        throw new InvalidDataException(i18n.getText("synapse.testcase.testreference.validation.char.message", Integer.valueOf(i + 1)) + " " + i18n.getText("synapse.testcase.testreference.validation.error.message"));
      }
    }
    return true;
  }
}
