/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.beans.steps.stepinfo.security.shared;

import static io.harness.annotations.dev.HarnessTeam.STO;
import static io.harness.beans.SwaggerConstants.STRING_CLASSPATH;
import static io.harness.yaml.schema.beans.SupportedPossibleFieldTypes.runtime;

import io.harness.annotations.dev.OwnedBy;
import io.harness.pms.yaml.ParameterField;
import io.harness.yaml.YamlSchemaTypes;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.codehaus.jackson.annotate.JsonProperty;

@Data
@OwnedBy(STO)
public class STOYamlVeracodeToolData {
  @ApiModelProperty(dataType = STRING_CLASSPATH, name = "app_id")
  @JsonProperty("app_id")
  protected ParameterField<String> appId;

  @ApiModelProperty(dataType = STRING_CLASSPATH, name = "project_name")
  @JsonProperty("project_name")
  protected ParameterField<String> projectName;
}
