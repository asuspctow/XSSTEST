package com.go2group.synapse.exception;

import com.go2group.synapse.core.exception.SynapseException;
import com.go2group.synapse.core.exception.SynapseException.ExceptionLevel;

public class BuildIncompleteException extends SynapseException
{
  private static final long serialVersionUID = 9003683697089525477L;
  
  public BuildIncompleteException() {}
  
  public BuildIncompleteException(String message)
  {
    super(message);
    level = SynapseException.ExceptionLevel.ERROR;
  }
}
