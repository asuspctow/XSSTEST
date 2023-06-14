package com.go2group.synapse.listener;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.event.events.PluginEnabledEvent;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.go2group.synapse.core.audit.log.service.AuditLogService;
import com.go2group.synapse.service.ConfigService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Component
public class SynapsePluginEnabledListener implements InitializingBean, DisposableBean
{
  private static final Logger log = Logger.getLogger(SynapsePluginEnabledListener.class);
  private final EventPublisher eventPublisher;
  private final ConfigService configService;
  private final AuditLogService auditLogService;
  
  public SynapsePluginEnabledListener(@ComponentImport EventPublisher eventPublisher, ConfigService configService, AuditLogService auditLogService)
  {
    this.eventPublisher = eventPublisher;
    this.configService = configService;
    this.auditLogService = auditLogService;
  }
  
  public void destroy() throws Exception
  {
    eventPublisher.unregister(this);
  }
  
  public void afterPropertiesSet() throws Exception
  {
    eventPublisher.register(this);
  }
  
  @EventListener
  public void onPluginEnabled(PluginEnabledEvent pluginEnabledEvent)
  {
    configService.setupIssueTypes();
    log.info("Issue types setup completed successfully");
    try
    {
      log.info("Audit log will be initialized");
      auditLogService.initialize();
      log.info("Audit log initialized successfully");
    }
    catch (Exception localException) {}
  }
}
