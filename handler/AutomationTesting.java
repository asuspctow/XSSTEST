package com.go2group.synapse.handler;

import com.go2group.synapse.automation.bean.AutomationResultBean;
import java.io.File;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.SAXException;

public class AutomationTesting
{
  public AutomationTesting() {}
  
  public static void main(String[] args)
  {
    File xmlFile = new File("Alm12Smoke.xml");
    SAXParserFactory factory = SAXParserFactory.newInstance();
    SAXParser parser = null;
    try {
      parser = factory.newSAXParser();
    }
    catch (ParserConfigurationException e) {
      e.printStackTrace();
    }
    catch (SAXException e) {
      e.printStackTrace();
    }
    SynapseAutomationHandler handler = new JunitAutomationXMLHandler();
    try {
      parser.parse(xmlFile, handler);
      
      AutomationResultBean automationResultBean = handler.getAutomationResultBean();
      System.out.println("automationResultBean : " + automationResultBean.getAutomationRunResultMap().size());
    }
    catch (SAXException e) {
      e.printStackTrace();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }
}
