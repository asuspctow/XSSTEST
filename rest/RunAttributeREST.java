package com.go2group.synapse.rest;

import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.json.JSONArray;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.go2group.synapse.bean.DataMapResponseWrapper;
import com.go2group.synapse.bean.HtmlResponseWrapper;
import com.go2group.synapse.bean.runattribute.RunAttributeMapOutputBean;
import com.go2group.synapse.bean.runattribute.RunAttributeOutputBean;
import com.go2group.synapse.bean.runattribute.RunAttributeValueOutputBean;
import com.go2group.synapse.core.exception.InvalidDataException;
import com.go2group.synapse.service.runattribute.RunAttributeHelper;
import com.go2group.synapse.service.runattribute.RunAttributeService;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;




@Path("runAttribute")
@Consumes({"application/json"})
@Produces({"application/json"})
public class RunAttributeREST
{
  private static Logger log = Logger.getLogger(RunAttributeREST.class);
  private RunAttributeService runAttributeService;
  private I18nHelper i18nHelper;
  
  @Autowired
  public RunAttributeREST(@ComponentImport I18nHelper i18nHelper, RunAttributeService runAttributeService)
  {
    this.runAttributeService = runAttributeService;
    this.i18nHelper = i18nHelper;
  }
  
  @Path("addAttribute")
  @POST
  public Response addAttribute(String runAttributeData) {
    log.debug("addAttribute - " + runAttributeData);
    try {
      JSONObject jsonObject = new JSONObject(runAttributeData);
      String projectKey = jsonObject.getString("projectKey");
      String name = jsonObject.getString("name");
      RunAttributeOutputBean runAttribute = runAttributeService.addRunAttribute(projectKey, name);
      if (runAttribute != null) {
        return Response.ok(runAttribute).build();
      }
    }
    catch (InvalidDataException|JSONException e) {
      log.debug(e.getMessage(), e);
      log.error(e.getMessage());
      return Response.serverError().entity(e.getMessage()).build();
    }
    return Response.serverError().entity("Run attribute Was not added").build();
  }
  
  @Path("getRunAttributes")
  @GET
  public Response getRunAttributes(@QueryParam("projectKey") String projectKey, @Context HttpServletRequest request) {
    log.debug("getRunAttributes - projectKey:" + projectKey);
    try {
      List<RunAttributeOutputBean> runAttributes = runAttributeService.getRunAttributes(projectKey);
      if ((runAttributes != null) && (runAttributes.size() > 0)) {
        String html = RunAttributeHelper.getRunAttributeRows(runAttributes, request.getContextPath(), i18nHelper);
        DataMapResponseWrapper data = new DataMapResponseWrapper();
        data.setData1Key("html");
        data.setData1(html);
        data.setData2Key("runAttributeCount");
        data.setData2(Integer.valueOf(runAttributes.size()));
        
        return Response.ok(data).build();
      }
      return Response.ok("").build();
    } catch (Exception e) {
      log.debug(e.getMessage(), e);
      log.error(e.getMessage());
      return Response.serverError().entity(e.getMessage()).build();
    }
  }
  
  @Path("updateRunAttribute")
  @PUT
  public Response updateRunAttribute(String runAttributeData) {
    log.debug("updateRunAttribute - " + runAttributeData);
    try {
      JSONObject jsonObject = new JSONObject(runAttributeData);
      Integer runAttributeId = Integer.valueOf(jsonObject.getInt("runAttributeId"));
      String newName = jsonObject.getString("newRunAttribute");
      String projectKey = jsonObject.getString("projectKey");
      
      RunAttributeOutputBean runAttribute = runAttributeService.updateRunAttribute(projectKey, runAttributeId, newName);
      if (runAttribute != null) {
        return Response.ok(runAttribute).build();
      }
    } catch (InvalidDataException|JSONException e) {
      log.debug(e.getMessage(), e);
      log.error(e.getMessage());
      return Response.serverError().entity(e.getMessage()).build();
    }
    return Response.serverError().entity("Run attribute was not updated").build();
  }
  
  @Path("deleteRunAttribute")
  @DELETE
  public Response deleteRunAttribute(String runAttributeData) {
    log.debug("deleteRunAttribute - " + runAttributeData);
    try {
      JSONObject jsonObject = new JSONObject(runAttributeData);
      Integer runAttributeId = Integer.valueOf(jsonObject.getInt("runAttributeId"));
      boolean isDeleted = runAttributeService.deleteRunAttribute(runAttributeId);
      if (isDeleted) {
        return Response.ok(new HtmlResponseWrapper("deleted")).build();
      }
    } catch (InvalidDataException|JSONException e) {
      log.debug(e.getMessage(), e);
      log.error(e.getMessage());
      return Response.serverError().entity(e.getMessage()).build();
    }
    return Response.serverError().entity("Run attribute was not deleted").build();
  }
  
  @Path("addAttributeValue")
  @POST
  public Response addAttributeValue(String runAttributeValueData) {
    log.debug("addAttribute - " + runAttributeValueData);
    try {
      JSONObject jsonObject = new JSONObject(runAttributeValueData);
      Integer attributeId = Integer.valueOf(jsonObject.getInt("attributeId"));
      String value = jsonObject.getString("value");
      RunAttributeValueOutputBean runAttributeValue = runAttributeService.addRunAttributeValue(attributeId, value);
      if (runAttributeValue != null) {
        return Response.ok(runAttributeValue).build();
      }
    } catch (InvalidDataException|JSONException e) {
      log.debug(e.getMessage(), e);
      log.error(e.getMessage());
      return Response.serverError().entity(e.getMessage()).build();
    }
    return Response.serverError().entity("Run attribute value was not added").build();
  }
  
  @Path("updateRunAttributeValue")
  @PUT
  public Response updateRunAttributeValue(String runAttributeValueData) {
    log.debug("updateRunAttributeValue - " + runAttributeValueData);
    try {
      JSONObject jsonObject = new JSONObject(runAttributeValueData);
      Integer runAttributeValueId = Integer.valueOf(jsonObject.getInt("runAttributeValueId"));
      String value = jsonObject.getString("newRunAttributeValue");
      

      RunAttributeValueOutputBean runAttributeValue = runAttributeService.updateRunAttributeValue(runAttributeValueId, value);
      if (runAttributeValue != null) {
        return Response.ok(runAttributeValue).build();
      }
    } catch (InvalidDataException|JSONException e) {
      log.debug(e.getMessage(), e);
      log.error(e.getMessage());
      return Response.serverError().entity(e.getMessage()).build();
    }
    return Response.serverError().entity("Run attribute value was not updated").build();
  }
  
  @Path("deleteRunAttributeValue")
  @DELETE
  public Response deleteRunAttributeValue(String runAttributeValueData) {
    log.debug("deleteRunAttributeValue - " + runAttributeValueData);
    try {
      JSONObject jsonObject = new JSONObject(runAttributeValueData);
      Integer attributeValueId = Integer.valueOf(jsonObject.getInt("attributeValueId"));
      boolean isDeleted = runAttributeService.deleteAttributeValue(attributeValueId);
      if (isDeleted) {
        return Response.ok(new HtmlResponseWrapper("deleted")).build();
      }
    }
    catch (InvalidDataException|JSONException e) {
      log.debug(e.getMessage(), e);
      log.error(e.getMessage());
      return Response.serverError().entity(e.getMessage()).build();
    }
    return Response.serverError().entity("Run attribute value was not deleted").build();
  }
  
  @Path("addAttributeRunMap")
  @POST
  public Response addAttributeRunMap(String attributeRunMapData) {
    log.debug("addAttributeRunMap - " + attributeRunMapData);
    try {
      JSONObject jsonObject = new JSONObject(attributeRunMapData);
      Integer testRunId = Integer.valueOf(jsonObject.getInt("testRunId"));
      
      List<Integer> ids = new ArrayList();
      JSONArray jsonArray = jsonObject.getJSONArray("attributeValueIds");
      for (int index = 0; index < jsonArray.length(); index++) {
        ids.add(Integer.valueOf(jsonArray.getInt(index)));
      }
      
      List<RunAttributeMapOutputBean> runAttributeMapOutputBeans = runAttributeService.addRunAttributeMap(testRunId, ids);
      if (runAttributeMapOutputBeans != null) {
        return Response.ok(new HtmlResponseWrapper("mapped")).build();
      }
    } catch (InvalidDataException|JSONException e) {
      log.debug(e.getMessage(), e);
      log.error(e.getMessage());
      return Response.serverError().entity(e.getMessage()).build();
    }
    return Response.serverError().entity("Attribute Value and Test Run were not mapped").build();
  }
}
