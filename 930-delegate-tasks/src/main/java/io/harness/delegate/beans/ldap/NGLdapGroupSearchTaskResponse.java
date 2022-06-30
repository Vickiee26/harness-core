/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Shield 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/06/PolyForm-Shield-1.0.0.txt.
 */

package io.harness.delegate.beans.ldap;

import static io.harness.annotations.dev.HarnessTeam.PL;

import io.harness.annotations.dev.OwnedBy;
import io.harness.delegate.beans.DelegateMetaInfo;
import io.harness.delegate.beans.DelegateTaskNotifyResponseData;

import software.wings.beans.sso.LdapGroupResponse;

import java.util.Collection;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@OwnedBy(PL)
@Getter
@Builder
public class NGLdapGroupSearchTaskResponse implements DelegateTaskNotifyResponseData {
  @Setter private DelegateMetaInfo delegateMetaInfo;
  private final Collection<LdapGroupResponse> ldapListGroupsResponses;
}