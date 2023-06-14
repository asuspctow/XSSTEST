package com.go2group.synapse.exception;

import com.go2group.synapse.core.exception.SynapseException;
import com.go2group.synapse.core.exception.SynapseException.ExceptionLevel;

public class ApplicationAccessException extends SynapseException
{
  private static final long serialVersionUID = 9003683697089525477L;
  
  public ApplicationAccessException() {}
  
  public ApplicationAccessException(String message)
  {
    super(message);
    level = SynapseException.ExceptionLevel.ERROR;
  }
}
