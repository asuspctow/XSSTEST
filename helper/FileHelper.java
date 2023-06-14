package com.go2group.synapse.helper;

import com.atlassian.jira.user.ApplicationUser;
import com.go2group.synapse.core.exception.InvalidDataException;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class FileHelper
{
  public FileHelper() {}
  
  public static boolean validateFileInput(ApplicationUser user, String fileNameWithPath) throws InvalidDataException
  {
    String decodedFileName = "";
    try {
      decodedFileName = URLDecoder.decode(fileNameWithPath, StandardCharsets.UTF_8.name());
    } catch (UnsupportedEncodingException e) {
      throw new InvalidDataException(String.valueOf(400), "Invalid encoded file name");
    }
    int dotIndex = decodedFileName.indexOf("..");
    if (dotIndex >= 0) {
      throw new InvalidDataException(String.valueOf(400), "Invalid characters found in file name or path");
    }
    
    List<String> validPaths = getValidSynapsePaths();
    boolean validPathFound = false;
    for (String validPath : validPaths) {
      int validPathIndex = decodedFileName.indexOf(validPath);
      if (validPathIndex >= 0) {
        validPathFound = true;
      }
    }
    if (!validPathFound) {
      throw new InvalidDataException(String.valueOf(400), "Invalid path");
    }
    
    File file = new File(decodedFileName);
    boolean isExist = file.exists();
    if (!isExist) {
      throw new InvalidDataException(String.valueOf(400), "File with name '" + fileNameWithPath + "' does not exist.");
    }
    return true;
  }
  
  public static List<String> getValidSynapsePaths() {
    List<String> validPaths = new java.util.ArrayList();
    validPaths.add("jira/home/export/");
    
    return validPaths;
  }
}
