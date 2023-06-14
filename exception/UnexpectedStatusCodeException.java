package com.go2group.synapse.exception;

import com.go2group.synapse.core.exception.SynapseException;
import com.go2group.synapse.core.exception.SynapseException.ExceptionLevel;

public class UnexpectedStatusCodeException extends SynapseException
{
  private static final long serialVersionUID = 5806226308023746981L;
  
  public UnexpectedStatusCodeException() {}
  
  public UnexpectedStatusCodeException(int message)
  {
    super(String.valueOf(message));
    level = SynapseException.ExceptionLevel.ERROR;
  }
}
