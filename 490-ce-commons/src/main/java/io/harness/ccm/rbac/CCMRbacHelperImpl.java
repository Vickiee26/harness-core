/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.ccm.rbac;

import static io.harness.ccm.rbac.CCMRbacPermissions.BUDGET_CREATE_AND_EDIT;
import static io.harness.ccm.rbac.CCMRbacPermissions.BUDGET_DELETE;
import static io.harness.ccm.rbac.CCMRbacPermissions.BUDGET_VIEW;
import static io.harness.ccm.rbac.CCMRbacPermissions.COST_CATEGORY_CREATE_AND_EDIT;
import static io.harness.ccm.rbac.CCMRbacPermissions.COST_CATEGORY_DELETE;
import static io.harness.ccm.rbac.CCMRbacPermissions.COST_CATEGORY_VIEW;
import static io.harness.ccm.rbac.CCMRbacPermissions.COST_OVERVIEW_VIEW;
import static io.harness.ccm.rbac.CCMRbacPermissions.CURRENCY_PREFERENCE_SET_AND_EDIT;
import static io.harness.ccm.rbac.CCMRbacPermissions.CURRENCY_PREFERENCE_VIEW;
import static io.harness.ccm.rbac.CCMRbacPermissions.FOLDER_CREATE_AND_EDIT;
import static io.harness.ccm.rbac.CCMRbacPermissions.FOLDER_DELETE;
import static io.harness.ccm.rbac.CCMRbacPermissions.FOLDER_VIEW;
import static io.harness.ccm.rbac.CCMRbacPermissions.PERSPECTIVE_CREATE_AND_EDIT;
import static io.harness.ccm.rbac.CCMRbacPermissions.PERSPECTIVE_DELETE;
import static io.harness.ccm.rbac.CCMRbacPermissions.PERSPECTIVE_VIEW;
import static io.harness.ccm.rbac.CCMRbacPermissions.RULE_CREATE_AND_EDIT;
import static io.harness.ccm.rbac.CCMRbacPermissions.RULE_DELETE;
import static io.harness.ccm.rbac.CCMRbacPermissions.RULE_ENFORCEMENT_CREATE_AND_EDIT;
import static io.harness.ccm.rbac.CCMRbacPermissions.RULE_ENFORCEMENT_DELETE;
import static io.harness.ccm.rbac.CCMRbacPermissions.RULE_ENFORCEMENT_VIEW;
import static io.harness.ccm.rbac.CCMRbacPermissions.RULE_SET_CREATE_AND_EDIT;
import static io.harness.ccm.rbac.CCMRbacPermissions.RULE_SET_DELETE;
import static io.harness.ccm.rbac.CCMRbacPermissions.RULE_SET_VIEW;
import static io.harness.ccm.rbac.CCMRbacPermissions.RULE_VIEW;
import static io.harness.ccm.rbac.CCMResources.BUDGET;
import static io.harness.ccm.rbac.CCMResources.COST_CATEGORY;
import static io.harness.ccm.rbac.CCMResources.CURRENCY_PREFERENCE;
import static io.harness.ccm.rbac.CCMResources.FOLDER;
import static io.harness.ccm.rbac.CCMResources.GOVERNANCE_RULE;
import static io.harness.ccm.rbac.CCMResources.GOVERNANCE_RULE_ENFORCEMENT;
import static io.harness.ccm.rbac.CCMResources.GOVERNANCE_RULE_SET;
import static io.harness.ccm.rbac.CCMResources.PERSPECTIVE;

import io.harness.accesscontrol.acl.api.Resource;
import io.harness.accesscontrol.acl.api.ResourceScope;
import io.harness.accesscontrol.clients.AccessControlClient;

import com.google.inject.Inject;

public class CCMRbacHelperImpl implements CCMRbacHelper {
  @Inject AccessControlClient accessControlClient;
  private static final String PERMISSION_MISSING_MESSAGE = "User not Authorized: Missing permission %s on %s";
  private static final String VIEW_PERMISSION = "View";
  private static final String EDIT_PERMISSION = "Create/Edit";
  private static final String DELETE_PERMISSION = "Delete";
  private static final String RESOURCE_COST_CATEGORY = "Cost Categories";
  private static final String RESOURCE_FOLDER = "Folders";
  private static final String RESOURCE_PERSPECTIVE = "Perspectives";
  private static final String RESOURCE_BUDGET = "Budgets";
  private static final String RESOURCE_CURRENCY_PREFERENCES = "Currency Preferences";
  private static final String RESOURCE_CCM_CLOUD_ASSET_GOVERNANCE = "CloudAssetGovernance";

  @Override
  public void checkFolderViewPermission(String accountIdentifier, String orgIdentifier, String projectIdentifier) {
    accessControlClient.checkForAccessOrThrow(ResourceScope.of(accountIdentifier, orgIdentifier, projectIdentifier),
        Resource.of(FOLDER, null), FOLDER_VIEW,
        String.format(PERMISSION_MISSING_MESSAGE, VIEW_PERMISSION, RESOURCE_FOLDER));
  }

  @Override
  public void checkFolderEditPermission(String accountIdentifier, String orgIdentifier, String projectIdentifier) {
    accessControlClient.checkForAccessOrThrow(ResourceScope.of(accountIdentifier, orgIdentifier, projectIdentifier),
        Resource.of(FOLDER, null), FOLDER_CREATE_AND_EDIT,
        String.format(PERMISSION_MISSING_MESSAGE, EDIT_PERMISSION, RESOURCE_FOLDER));
  }

  @Override
  public void checkFolderDeletePermission(String accountIdentifier, String orgIdentifier, String projectIdentifier) {
    accessControlClient.checkForAccessOrThrow(ResourceScope.of(accountIdentifier, orgIdentifier, projectIdentifier),
        Resource.of(FOLDER, null), FOLDER_DELETE,
        String.format(PERMISSION_MISSING_MESSAGE, DELETE_PERMISSION, RESOURCE_FOLDER));
  }

  @Override
  public void checkPerspectiveViewPermission(String accountIdentifier, String orgIdentifier, String projectIdentifier) {
    accessControlClient.checkForAccessOrThrow(ResourceScope.of(accountIdentifier, orgIdentifier, projectIdentifier),
        Resource.of(PERSPECTIVE, null), PERSPECTIVE_VIEW,
        String.format(PERMISSION_MISSING_MESSAGE, VIEW_PERMISSION, RESOURCE_PERSPECTIVE));
    // Check if user has folder view permission
    accessControlClient.checkForAccessOrThrow(ResourceScope.of(accountIdentifier, orgIdentifier, projectIdentifier),
        Resource.of(FOLDER, null), FOLDER_VIEW,
        String.format(PERMISSION_MISSING_MESSAGE, VIEW_PERMISSION, RESOURCE_FOLDER));
  }

  @Override
  public void checkPerspectiveEditPermission(String accountIdentifier, String orgIdentifier, String projectIdentifier) {
    accessControlClient.checkForAccessOrThrow(ResourceScope.of(accountIdentifier, orgIdentifier, projectIdentifier),
        Resource.of(PERSPECTIVE, null), PERSPECTIVE_CREATE_AND_EDIT,
        String.format(PERMISSION_MISSING_MESSAGE, EDIT_PERMISSION, RESOURCE_PERSPECTIVE));
  }

  @Override
  public void checkPerspectiveDeletePermission(
      String accountIdentifier, String orgIdentifier, String projectIdentifier) {
    accessControlClient.checkForAccessOrThrow(ResourceScope.of(accountIdentifier, orgIdentifier, projectIdentifier),
        Resource.of(PERSPECTIVE, null), PERSPECTIVE_DELETE,
        String.format(PERMISSION_MISSING_MESSAGE, DELETE_PERMISSION, RESOURCE_PERSPECTIVE));
  }

  @Override
  public void checkBudgetViewPermission(String accountIdentifier, String orgIdentifier, String projectIdentifier) {
    accessControlClient.checkForAccessOrThrow(ResourceScope.of(accountIdentifier, orgIdentifier, projectIdentifier),
        Resource.of(BUDGET, null), BUDGET_VIEW,
        String.format(PERMISSION_MISSING_MESSAGE, VIEW_PERMISSION, RESOURCE_BUDGET));
    checkPerspectiveOnlyViewPermission(accountIdentifier, orgIdentifier, projectIdentifier);
  }

  @Override
  public void checkBudgetEditPermission(String accountIdentifier, String orgIdentifier, String projectIdentifier) {
    accessControlClient.checkForAccessOrThrow(ResourceScope.of(accountIdentifier, orgIdentifier, projectIdentifier),
        Resource.of(BUDGET, null), BUDGET_CREATE_AND_EDIT,
        String.format(PERMISSION_MISSING_MESSAGE, EDIT_PERMISSION, RESOURCE_BUDGET));
  }

  @Override
  public void checkBudgetDeletePermission(String accountIdentifier, String orgIdentifier, String projectIdentifier) {
    accessControlClient.checkForAccessOrThrow(ResourceScope.of(accountIdentifier, orgIdentifier, projectIdentifier),
        Resource.of(BUDGET, null), BUDGET_DELETE,
        String.format(PERMISSION_MISSING_MESSAGE, DELETE_PERMISSION, RESOURCE_BUDGET));
  }

  @Override
  public void checkCostCategoryViewPermission(
      String accountIdentifier, String orgIdentifier, String projectIdentifier) {
    accessControlClient.checkForAccessOrThrow(ResourceScope.of(accountIdentifier, orgIdentifier, projectIdentifier),
        Resource.of(COST_CATEGORY, null), COST_CATEGORY_VIEW,
        String.format(PERMISSION_MISSING_MESSAGE, VIEW_PERMISSION, RESOURCE_COST_CATEGORY));
  }

  @Override
  public void checkCostCategoryEditPermission(
      String accountIdentifier, String orgIdentifier, String projectIdentifier) {
    accessControlClient.checkForAccessOrThrow(ResourceScope.of(accountIdentifier, orgIdentifier, projectIdentifier),
        Resource.of(COST_CATEGORY, null), COST_CATEGORY_CREATE_AND_EDIT,
        String.format(PERMISSION_MISSING_MESSAGE, EDIT_PERMISSION, RESOURCE_COST_CATEGORY));
  }

  @Override
  public void checkCostCategoryDeletePermission(
      String accountIdentifier, String orgIdentifier, String projectIdentifier) {
    accessControlClient.checkForAccessOrThrow(ResourceScope.of(accountIdentifier, orgIdentifier, projectIdentifier),
        Resource.of(COST_CATEGORY, null), COST_CATEGORY_DELETE,
        String.format(PERMISSION_MISSING_MESSAGE, DELETE_PERMISSION, RESOURCE_COST_CATEGORY));
  }

  @Override
  public void checkRecommendationsViewPermission(
      String accountIdentifier, String orgIdentifier, String projectIdentifier) {
    checkPerspectiveOnlyViewPermission(accountIdentifier, orgIdentifier, projectIdentifier);
  }

  @Override
  public void checkAnomalyViewPermission(String accountIdentifier, String orgIdentifier, String projectIdentifier) {
    checkPerspectiveOnlyViewPermission(accountIdentifier, orgIdentifier, projectIdentifier);
  }

  @Override
  public boolean hasCostOverviewPermission(String accountIdentifier, String orgIdentifier, String projectIdentifier) {
    return accessControlClient.hasAccess(ResourceScope.of(accountIdentifier, orgIdentifier, projectIdentifier),
        Resource.of(PERSPECTIVE, null), COST_OVERVIEW_VIEW);
  }

  public void checkPerspectiveOnlyViewPermission(
      String accountIdentifier, String orgIdentifier, String projectIdentifier) {
    accessControlClient.checkForAccessOrThrow(ResourceScope.of(accountIdentifier, orgIdentifier, projectIdentifier),
        Resource.of(PERSPECTIVE, null), PERSPECTIVE_VIEW,
        String.format(PERMISSION_MISSING_MESSAGE, VIEW_PERMISSION, RESOURCE_PERSPECTIVE));
  }

  @Override
  public void checkCurrencyPreferenceViewPermission(
      String accountIdentifier, String orgIdentifier, String projectIdentifier) {
    accessControlClient.checkForAccessOrThrow(ResourceScope.of(accountIdentifier, orgIdentifier, projectIdentifier),
        Resource.of(CURRENCY_PREFERENCE, null), CURRENCY_PREFERENCE_VIEW,
        String.format(PERMISSION_MISSING_MESSAGE, VIEW_PERMISSION, RESOURCE_CURRENCY_PREFERENCES));
  }

  @Override
  public void checkCurrencyPreferenceEditPermission(
      String accountIdentifier, String orgIdentifier, String projectIdentifier) {
    accessControlClient.checkForAccessOrThrow(ResourceScope.of(accountIdentifier, orgIdentifier, projectIdentifier),
        Resource.of(CURRENCY_PREFERENCE, null), CURRENCY_PREFERENCE_SET_AND_EDIT,
        String.format(PERMISSION_MISSING_MESSAGE, EDIT_PERMISSION, RESOURCE_CURRENCY_PREFERENCES));
  }

  @Override
  public void checkRuleEditPermission(String accountIdentifier, String orgIdentifier, String projectIdentifier) {
    accessControlClient.checkForAccessOrThrow(ResourceScope.of(accountIdentifier, orgIdentifier, projectIdentifier),
        Resource.of(GOVERNANCE_RULE, null), RULE_CREATE_AND_EDIT,
        String.format(PERMISSION_MISSING_MESSAGE, EDIT_PERMISSION, RESOURCE_CCM_CLOUD_ASSET_GOVERNANCE));
  }

  @Override
  public void checkRuleViewPermission(String accountIdentifier, String orgIdentifier, String projectIdentifier) {
    accessControlClient.checkForAccessOrThrow(ResourceScope.of(accountIdentifier, orgIdentifier, projectIdentifier),
        Resource.of(GOVERNANCE_RULE, null), RULE_VIEW,
        String.format(PERMISSION_MISSING_MESSAGE, VIEW_PERMISSION, RESOURCE_CCM_CLOUD_ASSET_GOVERNANCE));
  }

  @Override
  public void checkRuleDeletePermission(String accountIdentifier, String orgIdentifier, String projectIdentifier) {
    accessControlClient.checkForAccessOrThrow(ResourceScope.of(accountIdentifier, orgIdentifier, projectIdentifier),
        Resource.of(GOVERNANCE_RULE, null), RULE_DELETE,
        String.format(PERMISSION_MISSING_MESSAGE, DELETE_PERMISSION, RESOURCE_CCM_CLOUD_ASSET_GOVERNANCE));
  }

  @Override
  public void checkRuleSetEditPermission(String accountIdentifier, String orgIdentifier, String projectIdentifier) {
    accessControlClient.checkForAccessOrThrow(ResourceScope.of(accountIdentifier, orgIdentifier, projectIdentifier),
        Resource.of(GOVERNANCE_RULE_SET, null), RULE_SET_CREATE_AND_EDIT,
        String.format(PERMISSION_MISSING_MESSAGE, EDIT_PERMISSION, RESOURCE_CCM_CLOUD_ASSET_GOVERNANCE));
  }

  @Override
  public void checkRuleSetViewPermission(String accountIdentifier, String orgIdentifier, String projectIdentifier) {
    accessControlClient.checkForAccessOrThrow(ResourceScope.of(accountIdentifier, orgIdentifier, projectIdentifier),
        Resource.of(GOVERNANCE_RULE_SET, null), RULE_SET_VIEW,
        String.format(PERMISSION_MISSING_MESSAGE, VIEW_PERMISSION, RESOURCE_CCM_CLOUD_ASSET_GOVERNANCE));
  }

  @Override
  public void checkRuleSetDeletePermission(String accountIdentifier, String orgIdentifier, String projectIdentifier) {
    accessControlClient.checkForAccessOrThrow(ResourceScope.of(accountIdentifier, orgIdentifier, projectIdentifier),
        Resource.of(GOVERNANCE_RULE_SET, null), RULE_SET_DELETE,
        String.format(PERMISSION_MISSING_MESSAGE, DELETE_PERMISSION, RESOURCE_CCM_CLOUD_ASSET_GOVERNANCE));
  }

  @Override
  public void checkRuleEnforcementEditPermission(
      String accountIdentifier, String orgIdentifier, String projectIdentifier) {
    accessControlClient.checkForAccessOrThrow(ResourceScope.of(accountIdentifier, orgIdentifier, projectIdentifier),
        Resource.of(GOVERNANCE_RULE_ENFORCEMENT, null), RULE_ENFORCEMENT_CREATE_AND_EDIT,
        String.format(PERMISSION_MISSING_MESSAGE, EDIT_PERMISSION, RESOURCE_CCM_CLOUD_ASSET_GOVERNANCE));
  }

  @Override
  public void checkRuleEnforcementViewPermission(
      String accountIdentifier, String orgIdentifier, String projectIdentifier) {
    accessControlClient.checkForAccessOrThrow(ResourceScope.of(accountIdentifier, orgIdentifier, projectIdentifier),
        Resource.of(GOVERNANCE_RULE_ENFORCEMENT, null), RULE_ENFORCEMENT_VIEW,
        String.format(PERMISSION_MISSING_MESSAGE, VIEW_PERMISSION, RESOURCE_CCM_CLOUD_ASSET_GOVERNANCE));
  }

  @Override
  public void checkRuleEnforcementDeletePermission(
      String accountIdentifier, String orgIdentifier, String projectIdentifier) {
    accessControlClient.checkForAccessOrThrow(ResourceScope.of(accountIdentifier, orgIdentifier, projectIdentifier),
        Resource.of(GOVERNANCE_RULE_ENFORCEMENT, null), RULE_ENFORCEMENT_DELETE,
        String.format(PERMISSION_MISSING_MESSAGE, DELETE_PERMISSION, RESOURCE_CCM_CLOUD_ASSET_GOVERNANCE));
  }
}
