package com.go2group.synapse.jira.api.container.jira80;

import com.atlassian.jira.issue.search.SearchResults;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import org.apache.log4j.Logger;



public class SynapseSearchResults
{
  private static final Logger log = Logger.getLogger(SynapseSearchResults.class);
  
  public SynapseSearchResults() {}
  
  public static <T> List<T> getResults(SearchResults searchResults) { Class searchResultsClass = searchResults.getClass();
    Method method = null;
    try {
      method = searchResultsClass.getMethod("getIssues", new Class[0]);
    } catch (NoSuchMethodException e) {
      log.info(e.getMessage() + "; getIssues method");
      try {
        method = searchResultsClass.getMethod("getResults", new Class[0]);
      } catch (NoSuchMethodException e1) {
        log.info(e1.getMessage() + "; getResults method");
      }
    }
    Object issues = null;
    try {
      issues = method.invoke(searchResults, new Object[0]);
    } catch (IllegalAccessException|IllegalArgumentException|InvocationTargetException e) {
      log.info(e.getMessage() + "; invoke in getResults method in SynapseSearchResults");
    }
    if (issues != null) {
      List<T> issueList = (List)issues;
      return issueList;
    }
    return null;
  }
}
