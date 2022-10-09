package io.harness.delegate.task.ecs;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.delegate.beans.storeconfig.GitStoreDelegateConfig;
import io.harness.expression.ExpressionReflectionUtils.NestedAnnotationResolver;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
@OwnedBy(HarnessTeam.CDP)
public class EcsGitFetchRunTaskFileConfig implements NestedAnnotationResolver {
  GitStoreDelegateConfig gitStoreDelegateConfig;
  boolean succeedIfFileNotFound;
}
