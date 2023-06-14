package com.go2group.synapse.enums;

import javax.ws.rs.core.Response.Status.Family;
import javax.ws.rs.core.Response.StatusType;

public enum SynapseResponseStatusEnum implements Response.StatusType
{
  RATE_LIMIT_EXCEEDED(429, "Too many Requests");
  

  private int code;
  
  private String reason;
  
  private Response.Status.Family family;
  
  private SynapseResponseStatusEnum(int statusCode, String reasonPhrase)
  {
    code = statusCode;
    reason = reasonPhrase;
    switch (code / 100) {
    case 1:  family = Response.Status.Family.INFORMATIONAL; break;
    case 2:  family = Response.Status.Family.SUCCESSFUL; break;
    case 3:  family = Response.Status.Family.REDIRECTION; break;
    case 4:  family = Response.Status.Family.CLIENT_ERROR; break;
    case 5:  family = Response.Status.Family.SERVER_ERROR; break;
    default:  family = Response.Status.Family.OTHER;
    }
  }
  
  public int getStatusCode()
  {
    return code;
  }
  
  public Response.Status.Family getFamily()
  {
    return family;
  }
  
  public String getReasonPhrase()
  {
    return reason;
  }
}
