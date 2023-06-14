package com.go2group.synapse.rest;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.go2group.synapse.bean.PreferenceBean;
import com.go2group.synapse.core.exception.InvalidDataException;
import com.go2group.synapse.service.PreferenceService;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.apache.log4j.Logger;


@Path("preference")
@Consumes({"application/json"})
@Produces({"application/json"})
public class PreferenceREST
{
  private static Logger log = Logger.getLogger(PreferenceREST.class);
  private PreferenceService preferenceService;
  
  public PreferenceREST(PreferenceService preferenceService)
  {
    this.preferenceService = preferenceService;
  }
  

  @Path("reorderPreference")
  @POST
  public Response reorderPreference(@QueryParam("prefId") Integer prefId, @QueryParam("refPrefId") Integer refPrefId)
  {
    log.debug("reorderPreference - starts " + prefId + " refPrefId " + refPrefId);
    refPrefId = (refPrefId == null) || (refPrefId.intValue() == -1) ? null : refPrefId;
    try {
      preferenceService.reorderPreference(prefId, refPrefId);
    }
    catch (InvalidDataException e) {
      e.printStackTrace();
    }
    



    return null;
  }
  
  @Path("addPreference")
  @POST
  public Response addPreference(@QueryParam("jiraFieldsSelected") String jiraFieldsSelected, @QueryParam("source") String source, @QueryParam("module") String module)
  {
    log.debug("addPreference - starts jiraFieldsSelected" + jiraFieldsSelected);
    
    List<String> filteredFieldIds = new ArrayList();
    filteredFieldIds.add(jiraFieldsSelected);
    
    PreferenceBean preferenceBean = new PreferenceBean();
    preferenceBean.setFields(filteredFieldIds);
    preferenceBean.setModule(module);
    String userPreference = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser().getKey();
    if ((source != null) && (source.equals("admin"))) {
      userPreference = "Application";
    }
    preferenceBean.setUserName(userPreference);
    try
    {
      preferenceService.savePreference(preferenceBean);
    } catch (InvalidDataException e) {
      log.debug(e.getMessage(), e);
    }
    return Response.ok().build();
  }
  


  @Path("removePreference")
  @POST
  public Response removePreference(@QueryParam("jiraFieldsRemoved") String jiraFieldsRemoved, @QueryParam("source") String source, @QueryParam("module") String module)
  {
    log.debug("addPreference - starts jiraFieldsRemoved" + jiraFieldsRemoved);
    
    List<String> filteredFieldIds = new ArrayList();
    filteredFieldIds.add(jiraFieldsRemoved);
    
    PreferenceBean preferenceBean = new PreferenceBean();
    
    preferenceBean.setModule(module);
    if (jiraFieldsRemoved != null) {
      preferenceBean.setFieldsToRemove(filteredFieldIds);
    }
    String userPreference = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser().getKey();
    if ((source != null) && (source.equals("admin"))) {
      userPreference = "Application";
    }
    preferenceBean.setUserName(userPreference);
    try
    {
      preferenceService.savePreference(preferenceBean);
    } catch (InvalidDataException e) {
      log.debug(e.getMessage(), e);
    }
    return Response.ok().build();
  }
}
