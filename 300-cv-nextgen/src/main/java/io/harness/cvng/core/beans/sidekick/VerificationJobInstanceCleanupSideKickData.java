/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.cvng.core.beans.sidekick;

import io.harness.cvng.core.entities.SideKick.SideKickData;
import io.harness.cvng.core.entities.SideKick.Type;

import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class VerificationJobInstanceCleanupSideKickData implements SideKickData {
  @NotNull String verificationJobInstanceIdentifier;
  List<String> sourceIdentifiers;
  String accountIdentifier;
  String orgIdentifier;
  String projectIdentifier;

  @Override
  public Type getType() {
    return Type.VERIFICATION_JOB_INSTANCE_CLEANUP;
  }
}