package com.go2group.synapse.rest.pub;

import javax.xml.bind.annotation.XmlElement;

public class TestRunAttachmentRestBean {
  @XmlElement
  private String fileName;
  @XmlElement
  private String mimeType;
  @XmlElement
  private Integer id;
  @XmlElement
  private String filePath;
  
  public TestRunAttachmentRestBean() {}
  
  public String getFileName() { return fileName; }
  
  public void setFileName(String fileName)
  {
    this.fileName = fileName;
  }
  
  public String getMimeType() {
    return mimeType;
  }
  
  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }
  
  public Integer getId() {
    return id;
  }
  
  public void setId(Integer id) {
    this.id = id;
  }
  
  public String getFilePath() {
    return filePath;
  }
  
  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }
}
