package com.go2group.synapse.handler;

import com.atlassian.jira.issue.Issue;

public class ContentHolder {
  private StringBuffer content = new StringBuffer();
  
  public ContentHolder() {}
  
  public StringBuffer getContent() { return content; }
  
  private Issue firstIssue;
  public void setContent(StringBuffer content) {
    this.content = content;
  }
  
  public Issue getFirstIssue() {
    return firstIssue;
  }
  
  public void setFirstIssue(Issue firstIssue) {
    this.firstIssue = firstIssue;
  }
}
