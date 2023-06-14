package com.go2group.synapse.helper;

import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.go2group.synapse.bean.RequirementTree;
import com.go2group.synapse.constant.SynapsePermission;
import com.go2group.synapse.core.exception.InvalidDataException;
import com.go2group.synapse.core.tree.bean.Span;
import com.go2group.synapse.core.tree.bean.TreeActionBean;
import com.go2group.synapse.core.tree.bean.TreeBranch;
import com.go2group.synapse.core.tree.bean.TreeLeaf;
import com.go2group.synapse.core.tree.bean.TreeNode;
import com.go2group.synapse.core.tree.bean.TreeRoot;
import com.go2group.synapse.core.tree.bean.TreeTrunk;
import com.go2group.synapse.core.util.PermissionUtilCoreAbstract;
import com.go2group.synapse.core.util.PluginUtil;
import com.go2group.synapse.enums.ReqTreeItemInfoNameEnum;
import com.go2group.synapse.service.impl.DefaultRequirementService;
import com.go2group.synapserm.bean.ReqSuiteTreeBranchInputBean;
import com.go2group.synapserm.bean.ReqSuiteTreeItemInputBean;
import com.go2group.synapserm.bean.ReqSuiteTreeLeafInputBean;
import com.go2group.synapserm.bean.ReqSuiteTreeNodeInputBean;
import com.go2group.synapserm.bean.ReqSuiteTreeRootInputBean;
import com.go2group.synapserm.bean.ReqSuiteTreeTrunkInputBean;
import java.util.Collection;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;


@ExportAsService
@Component("defaultRequirementTreeHelper")
public class DefaultRequirementTreeHelper
  extends com.go2group.synapserm.helper.DefaultRequirementTreeHelper
{
  private static final String EXPORT_ICON_CLASS = "highlight-icon icon-default aui-icon aui-icon-small aui-iconfont-export";
  private static final String LEAF_COUNT_SPAN_STYLE = "width: 25px;text-decoration:none;float:right;margin:2px;position: absolute;top: 20px;";
  private static final String COUNT_SPAN_DIALOG_CSS_CLASS = "aui-lozenge aui-badge syn-aui-badge syn-tool-tip syn-wide-inline-dialog-trigger";
  private static final String COUNT_SPAN_SIMPLE_CSS_CLASS = "aui-lozenge aui-badge syn-aui-badge syn-tool-tip";
  private final DefaultRequirementService requirementService;
  private final I18nHelper i18nHelper;
  private final PermissionUtilCoreAbstract permissionUtil;
  private final ProjectManager projectManager;
  
  public DefaultRequirementTreeHelper(@ComponentImport I18nHelper i18nHelper, @ComponentImport ProjectManager projectManager, DefaultRequirementService requirementService, PermissionUtilCoreAbstract permissionUtil)
  {
    this.requirementService = requirementService;
    this.i18nHelper = i18nHelper;
    this.permissionUtil = permissionUtil;
    this.projectManager = projectManager;
  }
  
  public TreeRoot createSimpleRoot(ReqSuiteTreeRootInputBean rootInputBean) throws InvalidDataException
  {
    return super.createSimpleRoot(rootInputBean);
  }
  
  public TreeNode createSimpleNode(ReqSuiteTreeNodeInputBean nodeInputBean) throws InvalidDataException
  {
    return super.createSimpleNode(nodeInputBean);
  }
  
  public TreeBranch createSimpleBranch(ReqSuiteTreeBranchInputBean branchInputBean) throws InvalidDataException
  {
    return super.createSimpleBranch(branchInputBean);
  }
  
  public TreeBranch createBranch(ReqSuiteTreeBranchInputBean branchInputBean) throws InvalidDataException
  {
    return super.createBranch(branchInputBean);
  }
  
  public TreeNode createNode(ReqSuiteTreeNodeInputBean nodeInputBean) throws InvalidDataException
  {
    if ((StringUtils.isNotBlank(nodeInputBean.getProjectId())) && (StringUtils.isNumeric(nodeInputBean.getProjectId()))) {
      TreeNode node = super.createNode(nodeInputBean);
      Project currentProject = projectManager.getProjectObj(Long.valueOf(nodeInputBean.getProjectId()));
      if (currentProject != null)
      {
        boolean hasPermission = permissionUtil.hasSynapsePermission(currentProject, SynapsePermission.MANAGE_REQUIREMENTS);
        if (hasPermission)
        {
          try {
            reqTree = requirementService.getRequirementTree(nodeInputBean.getRequirement().getId());
          } catch (InvalidDataException e) { RequirementTree reqTree;
            throw new InvalidDataException(e.getMessage()); }
          RequirementTree reqTree;
          int childCount = 0;
          if (reqTree != null) {
            childCount = reqTree.getChildrenCount(nodeInputBean.getFilters(), nodeInputBean.getJqlIssues());
          }
          Span childrenCountSpan = new Span();
          childrenCountSpan.setId("count-" + String.valueOf(nodeInputBean.getRequirement().getId()));
          childrenCountSpan.setName(String.valueOf(childCount));
          childrenCountSpan.setStyle("border: 0px;disabled:true;width: 25px;vertical-align:top;display:inline;");
          childrenCountSpan.setTextBox(true);
          childrenCountSpan.setReadOnly(true);
          childrenCountSpan.setCssClass("member-counter ajs-dirty-warning-exempt aui-badge syn-aui-badge syn-tool-tip");
          childrenCountSpan.addAttribute("synToolTip", i18nHelper.getText("synapse.requirement.tool.tip.child.count"));
          childrenCountSpan.addEvent("onchange", "refreshReqParent('" + nodeInputBean.getTreeId() + "', '" + nodeInputBean.getRequirement().getId() + "')");
          node.addUpperRowItem("childrenCountSpan", childrenCountSpan);
          try
          {
            boolean testCaseCountEnabled = true;
            boolean testPlanCountEnabled = true;
            Map<String, Object> extraInfoMap = nodeInputBean.getExtraInfo();
            if (extraInfoMap != null) {
              testCaseCountEnabled = extraInfoMap.get(ReqTreeItemInfoNameEnum.TEST_CASE_COUNT_ENABLED.getName()) != null ? ((Boolean)extraInfoMap.get(ReqTreeItemInfoNameEnum.TEST_CASE_COUNT_ENABLED.getName())).booleanValue() : true;
              testPlanCountEnabled = extraInfoMap.get(ReqTreeItemInfoNameEnum.TEST_PLAN_COUNT_ENABLED.getName()) != null ? ((Boolean)extraInfoMap.get(ReqTreeItemInfoNameEnum.TEST_PLAN_COUNT_ENABLED.getName())).booleanValue() : true; }
            Span testPlanCountSpan;
            if ((testPlanCountEnabled) || (testCaseCountEnabled)) {
              int testCaseCount = testCaseCountEnabled ? requirementService.getAllTransitiveTestCasesCount(nodeInputBean.getRequirement().getId()) : 0;
              

              Span tcCountSpan = new Span();
              tcCountSpan.setId("tc-count");
              tcCountSpan.addAttribute("synToolTip", i18nHelper.getText("synapse.requirement.tool.tip.tc.count"));
              tcCountSpan.setStyle("width: 25px;text-decoration: none;margin: 2px;position: absolute;top: 20px;");
              if (testCaseCount > 0) {
                tcCountSpan.setHref(nodeInputBean.getContextPath() + "/ShowTestCaseCoverageDetail.jspa?reqId=" + nodeInputBean.getRequirement().getId());
                tcCountSpan.setCssClass("aui-lozenge aui-badge syn-aui-badge syn-tool-tip syn-wide-inline-dialog-trigger");
              } else {
                tcCountSpan.setCssClass("aui-lozenge aui-badge syn-aui-badge syn-tool-tip");
              }
              
              String countString = testCaseCountEnabled ? String.valueOf(testCaseCount) : "-";
              tcCountSpan.setName(countString);
              tcCountSpan.setAnchor(true);
              tcCountSpan.addEvent("onclick", "");
              node.addRightSideItem("tcCountSpan", tcCountSpan);
              

              testPlanCountSpan = new Span();
              int tpCount = 0;
              if (testCaseCount > 0) {
                tpCount = testPlanCountEnabled ? requirementService.getTestPlanCount(nodeInputBean.getRequirement()) : 0;
              }
              testPlanCountSpan.setId("tp-count");
              testPlanCountSpan.addAttribute("synToolTip", i18nHelper.getText("synapse.requirement.tool.tip.tp.count"));
              testPlanCountSpan.setStyle("width: 25px;text-decoration: none;margin:2px;position: absolute;top: 20px;left: 40px;");
              countString = testPlanCountEnabled ? String.valueOf(tpCount) : "-";
              if (tpCount > 0) {
                testPlanCountSpan.setName(countString);
                testPlanCountSpan.setHref(nodeInputBean.getContextPath() + "/ShowTestPlanCoverageDetail.jspa?reqId=" + nodeInputBean.getRequirement().getId());
                testPlanCountSpan.setCssClass("aui-lozenge aui-badge syn-aui-badge syn-tool-tip syn-wide-inline-dialog-trigger");
              } else {
                testPlanCountSpan.setName(countString);
                testPlanCountSpan.setCssClass("aui-lozenge aui-badge syn-aui-badge syn-tool-tip");
              }
              testPlanCountSpan.setAnchor(true);
              testPlanCountSpan.addEvent("onclick", "");
              node.addRightSideItem("testPlanCountSpan", testPlanCountSpan);
              
              TreeActionBean exportRequirement = new TreeActionBean();
              exportRequirement.setId("export-req-" + String.valueOf(nodeInputBean.getRequirement().getId()));
              exportRequirement.setIcon(true);
              exportRequirement.setIconClass("highlight-icon icon-default aui-icon aui-icon-small aui-iconfont-export");
              exportRequirement.setHref(nodeInputBean.getContextPath() + "/si/" + PluginUtil.getPluginKey() + ":synapseissue-word/" + nodeInputBean.getRequirement().getKey() + "/" + nodeInputBean.getRequirement().getKey() + ".doc");
              exportRequirement.setName(i18nHelper.getText("synapse.common.label.export"));
              exportRequirement.setTitle(i18nHelper.getText("synapse.common.label.export"));
              exportRequirement.setActionClass("aui aui-style");
              exportRequirement.setOnClick("");
              exportRequirement.setStyle("float:right;");
              node.addAction("exportRequirement", exportRequirement);
              
              if ((testCaseCountEnabled) && (testCaseCount == 0)) {
                TreeActionBean errorAnchor = new TreeActionBean();
                errorAnchor.setId("error-" + String.valueOf(nodeInputBean.getRequirement().getId()));
                errorAnchor.setIcon(true);
                errorAnchor.setIconClass("aui-icon aui-icon-small aui-iconfont-error");
                errorAnchor.setName("");
                errorAnchor.setTitle(i18nHelper.getText("synapse.requirement.testcase.error"));
                errorAnchor.setActionClass("");
                errorAnchor.setOnClick("");
                errorAnchor.setStyle("display:inline-block;top:20px;color:red;float:right;");
                
                node.addAction("errorAnchor", errorAnchor);
              }
              else if ((testPlanCountEnabled) && (tpCount == 0)) {
                TreeActionBean warnAnchor = new TreeActionBean();
                warnAnchor.setId("warn-" + String.valueOf(nodeInputBean.getRequirement().getId()));
                warnAnchor.setIcon(true);
                warnAnchor.setIconClass("aui-icon aui-icon-small aui-iconfont-warning");
                warnAnchor.setName("");
                warnAnchor.setTitle(i18nHelper.getText("synapse.requirement.testcase.warning"));
                warnAnchor.setActionClass("");
                warnAnchor.setOnClick("");
                warnAnchor.setStyle("display:inline-block;top:20px;color:orange;float: right;");
                node.addAction("warnAnchor", warnAnchor);
              }
            }
            













            if (nodeInputBean.isSprintEnabled()) {
              Span sprintSpan = new Span();
              sprintSpan.setId("sprint-" + String.valueOf(nodeInputBean.getRequirement().getId()));
              sprintSpan.setStyle("padding-left:3px;");
              sprintSpan.setName(CustomFieldHelper.getSprintValue(nodeInputBean.getRequirement()));
              node.addSecondRowItem("sprintSpan", sprintSpan);
            }
            
            if (nodeInputBean.isFixVersionEnabled()) {
              Span fixVersionSpan = new Span();
              fixVersionSpan.setId("fix-version-" + String.valueOf(nodeInputBean.getRequirement().getId()));
              fixVersionSpan.setStyle("padding-left:3px;");
              String fixVersions = "";
              Collection<Version> versions = nodeInputBean.getRequirement().getFixVersions();
              if (versions != null) {
                for (Version version : versions) {
                  fixVersions = fixVersions + "," + version.getName().trim();
                }
              }
              if (fixVersions.length() > 1) {
                fixVersions = fixVersions.substring(1);
              }
              fixVersionSpan.setName(fixVersions);
              node.addSecondRowItem("fixVersionSpan", fixVersionSpan);
            }
            
            if (nodeInputBean.isComponentEnabled()) {
              Span compSpan = new Span();
              compSpan.setId("component-" + String.valueOf(nodeInputBean.getRequirement().getId()));
              compSpan.setStyle("padding-left:3px;");
              String componentNames = "";
              Collection<ProjectComponent> components = nodeInputBean.getRequirement().getComponents();
              if (components != null) {
                for (ProjectComponent component : components) {
                  componentNames = componentNames + "," + component.getName();
                }
              }
              if (componentNames.length() > 1) {
                componentNames = "\t" + componentNames.substring(1);
              }
              compSpan.setName(componentNames);
              node.addSecondRowItem("compSpan", compSpan);
            }
          } catch (InvalidDataException e) {
            throw new InvalidDataException(e.getMessage());
          }
        } else {
          node.getActions().remove("addReq");
          node.getActions().remove("linkReq");
          node.getActions().remove("delReq");
          
          node.setItemSelectable(false);
        }
        
        return node;
      }
    }
    return null;
  }
  
  public TreeLeaf createLeaf(ReqSuiteTreeLeafInputBean leafInputBean)
    throws InvalidDataException
  {
    if ((StringUtils.isNotBlank(leafInputBean.getProjectId())) && (StringUtils.isNumeric(leafInputBean.getProjectId()))) {
      Project currentProject = projectManager.getProjectObj(Long.valueOf(leafInputBean.getProjectId()));
      if (currentProject != null) {
        TreeLeaf leaf = super.createLeaf(leafInputBean);
        
        boolean hasPermission = permissionUtil.hasSynapsePermission(currentProject, SynapsePermission.MANAGE_REQUIREMENTS);
        if (hasPermission) {
          TreeActionBean exportRequirement = new TreeActionBean();
          exportRequirement.setId("export-req-" + String.valueOf(leafInputBean.getRequirement().getId()));
          exportRequirement.setIcon(true);
          exportRequirement.setIconClass("highlight-icon icon-default aui-icon aui-icon-small aui-iconfont-export");
          exportRequirement.setHref(leafInputBean.getContextPath() + "/si/com.go2group.jira.plugin.synapse:synapseissue-word/" + leafInputBean.getRequirement().getKey() + "/" + leafInputBean.getRequirement().getKey() + ".doc");
          exportRequirement.setName(i18nHelper.getText("synapse.common.label.export"));
          exportRequirement.setTitle(i18nHelper.getText("synapse.common.label.export"));
          exportRequirement.setActionClass("aui aui-style");
          exportRequirement.setOnClick("");
          exportRequirement.setStyle("float:right;");
          leaf.addAction("exportRequirement", exportRequirement);
          

          boolean testCaseCountEnabled = true;
          boolean testPlanCountEnabled = true;
          Map<String, Object> extraInforMap = leafInputBean.getExtraInfo();
          if (extraInforMap != null) {
            testCaseCountEnabled = extraInforMap.get(ReqTreeItemInfoNameEnum.TEST_CASE_COUNT_ENABLED.getName()) != null ? ((Boolean)extraInforMap.get(ReqTreeItemInfoNameEnum.TEST_CASE_COUNT_ENABLED.getName())).booleanValue() : true;
            testPlanCountEnabled = extraInforMap.get(ReqTreeItemInfoNameEnum.TEST_PLAN_COUNT_ENABLED.getName()) != null ? ((Boolean)extraInforMap.get(ReqTreeItemInfoNameEnum.TEST_PLAN_COUNT_ENABLED.getName())).booleanValue() : true; }
          Span testPlanCountSpan;
          if ((testPlanCountEnabled) || (testCaseCountEnabled)) {
            int testCaseCount = testCaseCountEnabled ? requirementService.getAllTransitiveTestCasesCount(leafInputBean.getRequirement().getId()) : 0;
            
            Span tcCountSpan = null;
            tcCountSpan = new Span();
            tcCountSpan.setId("tc-count");
            tcCountSpan.addAttribute("synToolTip", i18nHelper.getText("synapse.requirement.tool.tip.tc.count"));
            tcCountSpan.setStyle("width: 25px;text-decoration:none;float:right;margin:2px;position: absolute;top: 20px;");
            if (testCaseCount > 0) {
              tcCountSpan.setHref(leafInputBean.getContextPath() + "/ShowTestCaseCoverageDetail.jspa?reqId=" + leafInputBean.getRequirement().getId());
              tcCountSpan.setCssClass("aui-lozenge aui-badge syn-aui-badge syn-tool-tip syn-wide-inline-dialog-trigger");
            } else {
              tcCountSpan.setCssClass("aui-lozenge aui-badge syn-aui-badge syn-tool-tip");
            }
            String countString = testCaseCountEnabled ? String.valueOf(testCaseCount) : "-";
            tcCountSpan.setName(countString);
            tcCountSpan.setAnchor(true);
            tcCountSpan.addEvent("onclick", "");
            
            if (tcCountSpan != null) {
              leaf.addRightSideItem("tcCountSpan", tcCountSpan);
            }
            

            testPlanCountSpan = new Span();
            int tpCount = 0;
            if (testCaseCount > 0) {
              tpCount = testPlanCountEnabled ? requirementService.getTestPlanCount(leafInputBean.getRequirement()) : 0;
            }
            
            testPlanCountSpan.setId("tp-count");
            testPlanCountSpan.addAttribute("synToolTip", i18nHelper.getText("synapse.requirement.tool.tip.tp.count"));
            testPlanCountSpan.setStyle("width: 25px;text-decoration: none;float: right;margin:2px;position: absolute;top: 20px;left: 40px;");
            if (tpCount > 0) {
              testPlanCountSpan.setHref(leafInputBean.getContextPath() + "/ShowTestPlanCoverageDetail.jspa?reqId=" + leafInputBean.getRequirement().getId());
              testPlanCountSpan.setCssClass("aui-lozenge aui-badge syn-aui-badge syn-tool-tip syn-wide-inline-dialog-trigger");
            } else {
              testPlanCountSpan.setCssClass("aui-lozenge aui-badge syn-aui-badge syn-tool-tip");
            }
            countString = testPlanCountEnabled ? String.valueOf(tpCount) : "-";
            testPlanCountSpan.setName(countString);
            
            testPlanCountSpan.setAnchor(true);
            testPlanCountSpan.addEvent("onclick", "");
            leaf.addRightSideItem("testPlanCountSpan", testPlanCountSpan);
            

            if ((testCaseCountEnabled) && (testCaseCount == 0)) {
              TreeActionBean errorAnchor = new TreeActionBean();
              errorAnchor.setId("error-" + String.valueOf(leafInputBean.getRequirement().getId()));
              errorAnchor.setIcon(true);
              errorAnchor.setIconClass("aui-icon aui-icon-small aui-iconfont-error");
              errorAnchor.setName("");
              errorAnchor.setTitle(i18nHelper.getText("synapse.requirement.testcase.error"));
              errorAnchor.setActionClass("");
              errorAnchor.setOnClick("");
              errorAnchor.setStyle("display:inline-block;top:20px;color:red;float:right;");
              leaf.addAction("errorAnchor", errorAnchor);
            }
            else if ((testPlanCountEnabled) && (tpCount == 0)) {
              TreeActionBean warnAnchor = new TreeActionBean();
              warnAnchor.setId("warn-" + String.valueOf(leafInputBean.getRequirement().getId()));
              warnAnchor.setIcon(true);
              warnAnchor.setIconClass("aui-icon aui-icon-small aui-iconfont-warning");
              warnAnchor.setName("");
              warnAnchor.setTitle(i18nHelper.getText("synapse.requirement.testcase.warning"));
              warnAnchor.setActionClass("");
              warnAnchor.setOnClick("");
              warnAnchor.setStyle("display:inline-block;top:20px;color:orange;float:right;");
              leaf.addAction("warnAnchor", warnAnchor);
            }
          }
          












          if (leafInputBean.isSprintEnabled()) {
            Span sprintSpan = new Span();
            sprintSpan.setId("sprint-" + String.valueOf(leafInputBean.getRequirement().getId()));
            sprintSpan.setStyle("padding-left:3px;");
            sprintSpan.setName(CustomFieldHelper.getSprintValue(leafInputBean.getRequirement()));
            leaf.addSecondRowItem("sprintSpan", sprintSpan);
          }
          
          if (leafInputBean.isFixVersionEnabled()) {
            Span fixVersionSpan = new Span();
            fixVersionSpan.setId("sprint-" + String.valueOf(leafInputBean.getRequirement().getId()));
            fixVersionSpan.setStyle("padding-left:3px;");
            String fixVersions = "";
            Collection<Version> versions = leafInputBean.getRequirement().getFixVersions();
            if (versions != null) {
              for (Version version : versions) {
                fixVersions = fixVersions + "," + version.getName().trim();
              }
            }
            if (fixVersions.length() > 1) {
              fixVersions = fixVersions.substring(1);
            }
            fixVersionSpan.setName(fixVersions);
            leaf.addSecondRowItem("fixVersionSpan", fixVersionSpan);
          }
          
          if (leafInputBean.isComponentEnabled()) {
            Span compSpan = new Span();
            compSpan.setId("sprint-" + String.valueOf(leafInputBean.getRequirement().getId()));
            compSpan.setStyle("padding-left:3px;");
            String componentNames = "";
            Collection<ProjectComponent> components = leafInputBean.getRequirement().getComponents();
            if (components != null) {
              for (ProjectComponent component : components) {
                componentNames = componentNames + "," + component.getName();
              }
            }
            if (componentNames.length() > 1) {
              componentNames = "\t" + componentNames.substring(1);
            }
            compSpan.setName(componentNames);
            leaf.addSecondRowItem("compSpan", compSpan);
          }
        } else {
          leaf.getActions().remove("addReq");
          leaf.getActions().remove("linkReq");
          leaf.getActions().remove("delReq");
          
          leaf.setItemSelectable(false);
        }
        
        return leaf;
      }
    }
    return null;
  }
  
  public TreeTrunk createTrunk(ReqSuiteTreeTrunkInputBean branchInputBean) throws InvalidDataException
  {
    return super.createTrunk(branchInputBean);
  }
  
  public TreeNode createChildren(ReqSuiteTreeItemInputBean itemInputBean) throws InvalidDataException
  {
    return super.createChildren(itemInputBean);
  }
}
