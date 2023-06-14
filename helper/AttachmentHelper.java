package com.go2group.synapse.helper;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.util.JiraHome;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.log4j.Logger;



public class AttachmentHelper
{
  private static final Logger log = Logger.getLogger(AttachmentHelper.class);
  private static final String SYNAPSE_ATTACHMENT_DIR = "/synapse/attachments/";
  
  public AttachmentHelper() {}
  
  public void createAttachment(Integer attachmentId, AttachmentFile attachmentFile) {
    log.debug("Creating attachment in filesystem : " + attachmentId);
    
    File f = new File(getJiraDataDirectory(), "/synapse/attachments/");
    if (!f.exists()) {
      boolean success = f.mkdirs();
      if (!success) {
        log.error("Unable to create synapse attachment directory in " + getJiraDataDirectory());
        log.error("Attachment creation failed");
        return;
      }
    }
    try
    {
      BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(f, attachmentId.toString())));
      InputStream inputStream = null;
      

      if (attachmentFile.getFile() != null) {
        inputStream = new FileInputStream(attachmentFile.getFile());
      }
      else {
        inputStream = attachmentFile.getInputFileStream();
      }
      BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
      try {
        int inputData;
        while ((inputData = bufferedInputStream.read()) != -1) {
          bos.write(inputData);
        }
      }
      finally {
        if (attachmentFile.getFile() != null) {
          if (inputStream != null) {
            inputStream.close();
          }
          if (bufferedInputStream != null) {
            bufferedInputStream.close();
          }
        }
        if (bos != null) {
          bos.close();
        }
      }
    }
    catch (FileNotFoundException e) {
      log.debug(e.getMessage(), e);
      log.error("Unable to create file, attachment creation failed");
    } catch (IOException e) {
      log.debug(e.getMessage(), e);
      log.error("Error writing file content, attachment creation failed");
    }
  }
  
  public BufferedInputStream getFileContent(Integer attachmentId)
  {
    log.debug("Reading the file content with attachmentId:" + attachmentId);
    try
    {
      return new BufferedInputStream(new FileInputStream(new File(getJiraDataDirectory(), "/synapse/attachments/" + attachmentId.toString())));
    }
    catch (FileNotFoundException e) {
      log.debug(e.getMessage(), e);
      log.error(e.getMessage());
    }
    
    return null;
  }
  
  public void removeFile(Integer attachmentId)
  {
    log.debug("Removing file from filesystem with attachmentId:" + attachmentId);
    
    File attachmentFile = new File(getJiraDataDirectory(), "/synapse/attachments/" + attachmentId.toString());
    
    if (attachmentFile.exists()) {
      boolean result = attachmentFile.delete();
      
      if (result) {
        log.debug("File successfully deleted!");
      } else {
        log.warn("Failed to delete file with attachmentID:" + attachmentId);
      }
    }
  }
  
  private File getJiraDataDirectory() {
    JiraHome jirahome = (JiraHome)ComponentAccessor.getComponentOfType(JiraHome.class);
    return jirahome.getDataDirectory();
  }
}
