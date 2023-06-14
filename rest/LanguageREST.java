package com.go2group.synapse.rest;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.util.VelocityParamFactory;
import com.atlassian.velocity.VelocityManager;
import com.go2group.synapse.bean.language.FontBean;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.apache.log4j.Logger;



@Path("/language")
@Consumes({"application/json"})
@Produces({"application/json"})
public class LanguageREST
{
  private static final Logger log = Logger.getLogger(LanguageREST.class);
  
  public LanguageREST() {}
  
  @Path("/getfont")
  @GET
  public Response getFont(@QueryParam("language") String language) { try { log.debug("getFont - language:" + language);
      Map<String, Object> velocityParams = ComponentAccessor.getVelocityParamFactory().getDefaultVelocityParams();
      
      VelocityManager vm = ComponentAccessor.getVelocityManager();
      String fontBase64 = "";
      FontBean fontBean = new FontBean();
      if ((language.equals("zh_CN")) || (language.startsWith("en_"))) {
        fontBase64 = vm.getEncodedBody("", "/templates/web/language/simplified-chinese.vm", StandardCharsets.UTF_8.displayName(), velocityParams);
        fontBean.setFileName("SIMSUN-normal.ttf");
        fontBean.setFontName("SIMSUN");
        fontBean.setFontStyle("normal");
      } else if (language.equals("zh_TW")) {
        fontBase64 = vm.getEncodedBody("", "/templates/web/language/simplified-chinese.vm", StandardCharsets.UTF_8.displayName(), velocityParams);
        fontBean.setFileName("SIMSUN-normal.ttf");
        fontBean.setFontName("SIMSUN");
        fontBean.setFontStyle("normal");
      }
      fontBean.setFontBase64(fontBase64);
      
      log.debug("Returning font file.");
      return Response.ok().entity(fontBean).build();
    } catch (Exception e) {
      log.debug(e.getMessage(), e);
      log.error(e.getMessage());
    }
    return Response.ok("").build();
  }
}
