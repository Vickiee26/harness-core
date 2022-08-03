/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.pms.preflight;

import static io.harness.annotations.dev.HarnessTeam.PIPELINE;

import io.harness.annotations.dev.OwnedBy;
import io.harness.pms.preflight.connector.ConnectorWrapperResponse;
import io.harness.pms.preflight.inputset.PipelineWrapperResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@OwnedBy(PIPELINE)
@Data
@Builder
@Schema(name = "PreFlightDTO", description = "This contains the response of a Preflight Check for a Pipeline.")
public class PreFlightDTO {
  PipelineWrapperResponse pipelineInputWrapperResponse;
  ConnectorWrapperResponse connectorWrapperResponse;
  PreFlightStatus status;
  PreFlightErrorInfo errorInfo;
}