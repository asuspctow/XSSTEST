package com.go2group.synapse.handler;

import com.go2group.synapse.automation.bean.AutomationResultBean;
import com.go2group.synapse.automation.bean.AutomationRunResultBean;
import com.go2group.synapse.enums.AutomationErrorTypeEnum;
import com.go2group.synapse.enums.AutomationTestCaseStatusEnum;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;









public class JunitAutomationXMLHandler
  extends SynapseAutomationHandler
{
  private static final Logger log = Logger.getLogger(JunitAutomationXMLHandler.class);
  
  private AutomationResultBean resultBean = new AutomationResultBean();
  
  private AutomationRunResultBean automationRunResultBean = new AutomationRunResultBean();
  
  private StringBuffer content = new StringBuffer();
  


  public JunitAutomationXMLHandler() {}
  

  public void startElement(String uri, String localName, String qName, Attributes attributes)
    throws SAXException
  {
    switch (qName)
    {
    case "testsuite": 
      resultBean.setCycleName(attributes.getValue("name"));
      break;
    
    case "testcase": 
      automationRunResultBean = new AutomationRunResultBean();
      automationRunResultBean.initialise();
      
      automationRunResultBean.setTestCaseLinkName(attributes.getValue("classname") + ":" + attributes
        .getValue("name"));
      automationRunResultBean.setResult(attributes.getValue("status"));
      break;
    
    case "error": 
      if ((attributes != null) && (StringUtils.isNotBlank(attributes.getValue("type")))) {
        automationRunResultBean.setErrorType(attributes.getValue("type"));
      } else {
        automationRunResultBean.setErrorType(AutomationErrorTypeEnum.SYNAPSE_ERROR.getName());
      }
      automationRunResultBean.setResult(AutomationTestCaseStatusEnum.TEST_CASE_STATUS_ERROR.getName());
      break;
    case "failure": 
      if ((attributes != null) && (StringUtils.isNotBlank(attributes.getValue("type")))) {
        automationRunResultBean.setErrorType(attributes.getValue("type"));
      } else {
        automationRunResultBean.setErrorType(AutomationErrorTypeEnum.SYNAPSE_ERROR.getName());
      }
      automationRunResultBean.setResult(AutomationTestCaseStatusEnum.TEST_CASE_STATUS_FAILED.getName());
    }
    
    clearContentBuffer();
  }
  
  public void endElement(String uri, String localName, String qName) throws SAXException {
    switch (qName)
    {
    case "testcase": 
      resultBean.getAutomationRunResultMap().put(automationRunResultBean.getTestCaseLinkName(), automationRunResultBean);
      
      break;
    case "error": 
    case "failure": 
      automationRunResultBean.setErrorMessage(content.toString());
      break;
    case "testsuite": 
      log.info("i am in last element : " + resultBean.getAutomationRunResultMap().size());
    }
    
    clearContentBuffer();
  }
  
  public void characters(char[] ch, int start, int length)
    throws SAXException
  {
    content.append(ch, start, length);
  }
  
  private void clearContentBuffer() { content.delete(0, content.length()); }
  
  public AutomationResultBean getAutomationResultBean() {
    return resultBean;
  }
}
