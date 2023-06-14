package org.json.simple.parser;


public class ParseException
  extends Exception
{
  private static final long serialVersionUID = -7880698968187728548L;
  
  public static final int ERROR_UNEXPECTED_CHAR = 0;
  
  public static final int ERROR_UNEXPECTED_TOKEN = 1;
  
  public static final int ERROR_UNEXPECTED_EXCEPTION = 2;
  
  private int errorType;
  private Object unexpectedObject;
  private int position;
  
  public ParseException(int errorType)
  {
    this(-1, errorType, null);
  }
  
  public ParseException(int errorType, Object unexpectedObject) {
    this(-1, errorType, unexpectedObject);
  }
  
  public ParseException(int position, int errorType, Object unexpectedObject) {
    this.position = position;
    this.errorType = errorType;
    this.unexpectedObject = unexpectedObject;
  }
  
  public int getErrorType() {
    return errorType;
  }
  
  public void setErrorType(int errorType) {
    this.errorType = errorType;
  }
  




  public int getPosition()
  {
    return position;
  }
  
  public void setPosition(int position) {
    this.position = position;
  }
  







  public Object getUnexpectedObject()
  {
    return unexpectedObject;
  }
  
  public void setUnexpectedObject(Object unexpectedObject) {
    this.unexpectedObject = unexpectedObject;
  }
  
  public String toString() {
    StringBuffer sb = new StringBuffer();
    
    switch (errorType) {
    case 0: 
      sb.append("Unexpected character (").append(unexpectedObject).append(") at position ").append(position).append(".");
      break;
    case 1: 
      sb.append("Unexpected token ").append(unexpectedObject).append(" at position ").append(position).append(".");
      break;
    case 2: 
      sb.append("Unexpected exception at position ").append(position).append(": ").append(unexpectedObject);
      break;
    default: 
      sb.append("Unkown error at position ").append(position).append(".");
    }
    
    return sb.toString();
  }
}