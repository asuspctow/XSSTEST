package com.go2group.synapse.helper;

import java.io.File;
import java.io.InputStream;



public class AttachmentFile
{
  private String name;
  private String mimeType;
  private File file;
  private InputStream inputFileStream;
  
  public AttachmentFile(String name, String mimeType, File inputFile)
  {
    this.name = name;
    this.mimeType = mimeType;
    file = inputFile;
  }
  
  public AttachmentFile(String name, String mimeType, InputStream inputFileStream)
  {
    this.name = name;
    this.mimeType = mimeType;
    this.inputFileStream = inputFileStream;
  }
  
  public String getName() {
    return name;
  }
  
  public String getMimeType() {
    return mimeType;
  }
  
  public File getFile() {
    return file;
  }
  
  public String toString()
  {
    return name;
  }
  
  public InputStream getInputFileStream() {
    return inputFileStream;
  }
}
