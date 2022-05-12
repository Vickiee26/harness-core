/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.ng.trialsignup;

import io.harness.connector.ConnectorResponseDTO;
import io.harness.connector.ConnectorValidationResult;
import io.harness.ng.core.dto.secrets.SecretResponseWrapper;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(name = "ScmConnectorResponse", description = "ScmConnectorResponse")
public class ScmConnectorResponse {
  ConnectorResponseDTO connectorResponseDTO;
  SecretResponseWrapper secretResponseWrapper;
  ConnectorValidationResult connectorValidationResult;
}