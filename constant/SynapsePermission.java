package com.go2group.synapse.constant;

import java.util.List;

public class SynapsePermission extends com.go2group.synapse.core.constant.SynapsePermission { public static SynapsePermission MANAGE_REQUIREMENTS = new SynapsePermission("manage-requirements");
  public static SynapsePermission MANAGE_TESTCASES = new SynapsePermission("manage-testcases");
  public static SynapsePermission MANAGE_TESTSUITES = new SynapsePermission("manage-testsuites");
  public static SynapsePermission MANAGE_TESTPLANS = new SynapsePermission("manage-testplans");
  public static SynapsePermission EXECUTE_TESTRUNS = new SynapsePermission("execute-testruns");
  public static SynapsePermission BROWSE_SYNAPSE_PANELS = new SynapsePermission("browse-synapse-panels");
  
  protected SynapsePermission(String key) {
    super(key);
    values.add(this);
  }
}
