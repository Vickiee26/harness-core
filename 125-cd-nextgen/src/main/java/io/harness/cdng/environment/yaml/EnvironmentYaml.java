package io.harness.cdng.environment.yaml;

import io.harness.beans.ParameterField;
import io.harness.cdng.visitor.YamlTypes;
import io.harness.cdng.visitor.helpers.pipelineinfrastructure.EnvironmentYamlVisitorHelper;
import io.harness.common.SwaggerConstants;
import io.harness.data.structure.EmptyPredicate;
import io.harness.ng.core.environment.beans.EnvironmentType;
import io.harness.walktree.beans.LevelNode;
import io.harness.walktree.visitor.SimpleVisitorHelper;
import io.harness.walktree.visitor.Visitable;
import io.harness.yaml.core.intfc.OverridesApplier;

import io.swagger.annotations.ApiModelProperty;
import java.util.Map;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Wither;

@Data
@Builder
@SimpleVisitorHelper(helperClass = EnvironmentYamlVisitorHelper.class)
public class EnvironmentYaml implements OverridesApplier<EnvironmentYaml>, Visitable {
  @ApiModelProperty(dataType = SwaggerConstants.STRING_CLASSPATH) @Wither ParameterField<String> name;
  @ApiModelProperty(dataType = SwaggerConstants.STRING_CLASSPATH) @Wither ParameterField<String> identifier;
  @ApiModelProperty(dataType = SwaggerConstants.STRING_CLASSPATH) @Wither private ParameterField<String> description;
  @Wither EnvironmentType type;
  @Wither Map<String, String> tags;

  // For Visitor Framework Impl
  String metadata;

  @Override
  public EnvironmentYaml applyOverrides(EnvironmentYaml overrideConfig) {
    EnvironmentYaml resultant = this;
    if (overrideConfig.getName() != null) {
      resultant = resultant.withName(overrideConfig.getName());
    }
    if (overrideConfig.getIdentifier() != null) {
      resultant = resultant.withIdentifier(overrideConfig.getIdentifier());
    }
    if (overrideConfig.getDescription() != null) {
      resultant = resultant.withDescription(overrideConfig.getDescription());
    }
    if (overrideConfig.getType() != null) {
      resultant = resultant.withType(overrideConfig.getType());
    }
    if (EmptyPredicate.isNotEmpty(overrideConfig.getTags())) {
      resultant = resultant.withTags(overrideConfig.getTags());
    }
    return resultant;
  }

  @Override
  public LevelNode getLevelNode() {
    return LevelNode.builder().qualifierName(YamlTypes.ENVIRONMENT_YAML).build();
  }
}
