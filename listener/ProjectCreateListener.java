package com.go2group.synapse.listener;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.ProjectCreatedEvent;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.project.Project;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.go2group.synapse.ao.RunAttribute;
import com.go2group.synapse.core.ao.Column;
import com.go2group.synapse.core.ao.ColumnConfig;
import com.go2group.synapse.core.enums.TestRunColumnConfigEnum;
import java.util.ArrayList;
import java.util.List;
import net.java.ao.DBParam;
import net.java.ao.Query;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;






@Component
public class ProjectCreateListener
  implements InitializingBean, DisposableBean
{
  private static final Logger log = Logger.getLogger(ProjectCreateListener.class);
  private final EventPublisher eventPublisher;
  private final ActiveObjects activeObjects;
  
  public ProjectCreateListener(@ComponentImport EventPublisher eventPublisher, @ComponentImport ActiveObjects activeObjects) {
    this.eventPublisher = eventPublisher;
    this.activeObjects = activeObjects;
  }
  
  public void destroy() throws Exception
  {
    eventPublisher.unregister(this);
  }
  
  public void afterPropertiesSet()
    throws Exception
  {
    eventPublisher.register(this);
  }
  
  @EventListener
  public void onProjectCreated(ProjectCreatedEvent createEvent)
  {
    log.debug("project created event");
    Project createdProject = createEvent.getProject();
    populateTestCycleColumns(activeObjects, createdProject.getId());
  }
  
  private List<RunAttribute> getRunAttributesForProject(ActiveObjects activeObjects, long projectId)
  {
    RunAttribute[] runAttributes = (RunAttribute[])activeObjects.find(RunAttribute.class, 
      Query.select().where("PROJECT = ?", new Object[] { Long.valueOf(projectId) }).order("ID ASC"));
    List<RunAttribute> runAttributesList = new ArrayList();
    if ((runAttributes != null) && (runAttributes.length > 0)) {
      for (RunAttribute runAttribute : runAttributes) {
        runAttributesList.add(runAttribute);
      }
      return runAttributesList;
    }
    return new ArrayList();
  }
  
  private void populateTestCycleColumns(ActiveObjects activeObjects, Long pid) {
    log.info("populateTestCycleColumns start");
    
    ColumnConfig[] columnConfigs = (ColumnConfig[])activeObjects.find(ColumnConfig.class, Query.select().where("MODULE_NAME = ? AND PROJECT = ?", new Object[] { "test-cycle-all-runs", pid }));
    ColumnConfig columnConfig = null;
    if ((columnConfigs != null) && (columnConfigs.length > 0)) {
      columnConfig = columnConfigs[0];
    } else {
      columnConfig = (ColumnConfig)activeObjects.create(ColumnConfig.class, new DBParam[] { new DBParam("USER", Long.valueOf(0L)), new DBParam("PROJECT", pid), new DBParam("MODULE_NAME", "test-cycle-all-runs"), new DBParam("CATEGORY", "web-page"), new DBParam("DEFAULT", 
      


        Boolean.valueOf(true)) });
    }
    
    FieldManager fieldManager = ComponentAccessor.getFieldManager();
    if (columnConfig != null) {
      Field field = fieldManager.getField("issuekey");
      activeObjects.create(Column.class, new DBParam[] { new DBParam("COLUMN_CONFIG_ID", Integer.valueOf(columnConfig.getID())), new DBParam("FIELD_ID", field
        .getId()), new DBParam("FIELD_NAME", field
        .getName()), new DBParam("ORDER", 
        Integer.valueOf(1)), new DBParam("STYLE", null), new DBParam("TYPE", "jira") });
      


      field = fieldManager.getField("summary");
      activeObjects.create(Column.class, new DBParam[] { new DBParam("COLUMN_CONFIG_ID", Integer.valueOf(columnConfig.getID())), new DBParam("FIELD_ID", field
        .getId()), new DBParam("FIELD_NAME", field
        .getName()), new DBParam("ORDER", 
        Integer.valueOf(2)), new DBParam("STYLE", null), new DBParam("TYPE", "jira") });
      


      activeObjects.create(Column.class, new DBParam[] { new DBParam("COLUMN_CONFIG_ID", Integer.valueOf(columnConfig.getID())), new DBParam("FIELD_ID", TestRunColumnConfigEnum.TEST_RUN_TESTER
        .getKey()), new DBParam("FIELD_NAME", TestRunColumnConfigEnum.TEST_RUN_TESTER
        .getName()), new DBParam("ORDER", 
        Integer.valueOf(3)), new DBParam("STYLE", null), new DBParam("TYPE", "synapse") });
      


      activeObjects.create(Column.class, new DBParam[] { new DBParam("COLUMN_CONFIG_ID", Integer.valueOf(columnConfig.getID())), new DBParam("FIELD_ID", TestRunColumnConfigEnum.TEST_RUN_RESULT
        .getKey()), new DBParam("FIELD_NAME", TestRunColumnConfigEnum.TEST_RUN_RESULT
        .getName()), new DBParam("ORDER", 
        Integer.valueOf(4)), new DBParam("STYLE", null), new DBParam("TYPE", "synapse") });
      


      field = fieldManager.getField("labels");
      activeObjects.create(Column.class, new DBParam[] { new DBParam("COLUMN_CONFIG_ID", Integer.valueOf(columnConfig.getID())), new DBParam("FIELD_ID", field
        .getId()), new DBParam("FIELD_NAME", field
        .getName()), new DBParam("ORDER", 
        Integer.valueOf(5)), new DBParam("STYLE", null), new DBParam("TYPE", "jira") });
      

      field = fieldManager.getField("components");
      activeObjects.create(Column.class, new DBParam[] { new DBParam("COLUMN_CONFIG_ID", Integer.valueOf(columnConfig.getID())), new DBParam("FIELD_ID", field
        .getId()), new DBParam("FIELD_NAME", field
        .getName()), new DBParam("ORDER", 
        Integer.valueOf(6)), new DBParam("STYLE", null), new DBParam("TYPE", "jira") });
      

      activeObjects.create(Column.class, new DBParam[] { new DBParam("COLUMN_CONFIG_ID", Integer.valueOf(columnConfig.getID())), new DBParam("FIELD_ID", TestRunColumnConfigEnum.TEST_RUN_COMMENTS
        .getKey()), new DBParam("FIELD_NAME", TestRunColumnConfigEnum.TEST_RUN_COMMENTS
        .getName()), new DBParam("ORDER", 
        Integer.valueOf(7)), new DBParam("STYLE", null), new DBParam("TYPE", "synapse") });
      

      activeObjects.create(Column.class, new DBParam[] { new DBParam("COLUMN_CONFIG_ID", Integer.valueOf(columnConfig.getID())), new DBParam("FIELD_ID", TestRunColumnConfigEnum.TEST_RUN_ATTACHMENT_COUNT
        .getKey()), new DBParam("FIELD_NAME", TestRunColumnConfigEnum.TEST_RUN_ATTACHMENT_COUNT
        .getName()), new DBParam("ORDER", 
        Integer.valueOf(8)), new DBParam("STYLE", null), new DBParam("TYPE", "synapse") });
      

      activeObjects.create(Column.class, new DBParam[] { new DBParam("COLUMN_CONFIG_ID", Integer.valueOf(columnConfig.getID())), new DBParam("FIELD_ID", TestRunColumnConfigEnum.TEST_RUN_DEFECT_COUNT
        .getKey()), new DBParam("FIELD_NAME", TestRunColumnConfigEnum.TEST_RUN_DEFECT_COUNT
        .getName()), new DBParam("ORDER", 
        Integer.valueOf(9)), new DBParam("STYLE", null), new DBParam("TYPE", "synapse") });
      


      List<RunAttribute> runAttributes = getRunAttributesForProject(activeObjects, pid.longValue());
      if (runAttributes != null) {
        if (runAttributes.size() >= 1) {
          activeObjects.create(Column.class, new DBParam[] { new DBParam("COLUMN_CONFIG_ID", Integer.valueOf(columnConfig.getID())), new DBParam("FIELD_ID", TestRunColumnConfigEnum.TEST_RUN_ATTRIBUTES_ONE
            .getKey()), new DBParam("FIELD_NAME", 
            ((RunAttribute)runAttributes.get(0)).getName()), new DBParam("ORDER", Integer.valueOf(10)), new DBParam("STYLE", null), new DBParam("TYPE", "synapse") });
        }
        

        if (runAttributes.size() == 2) {
          activeObjects.create(Column.class, new DBParam[] { new DBParam("COLUMN_CONFIG_ID", Integer.valueOf(columnConfig.getID())), new DBParam("FIELD_ID", TestRunColumnConfigEnum.TEST_RUN_ATTRIBUTES_TWO
            .getKey()), new DBParam("FIELD_NAME", 
            ((RunAttribute)runAttributes.get(1)).getName()), new DBParam("ORDER", Integer.valueOf(11)), new DBParam("STYLE", null), new DBParam("TYPE", "synapse") });
        }
      }
      


      log.info("populateTestCycleColumns end");
    }
  }
}
