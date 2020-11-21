package software.wings.graphql.schema.type.cloudProvider;

import io.harness.ccm.health.CEHealthStatus;

import software.wings.graphql.schema.type.QLUser;
import software.wings.security.PermissionAttribute.ResourceType;
import software.wings.security.annotations.Scope;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

@Value
@Builder
@FieldNameConstants(innerTypeName = "QLPcfConfigKeys")
@Scope(ResourceType.SETTING)
public class QLPcfCloudProvider implements QLCloudProvider {
  private String id;
  private String name;
  private String description;
  private Long createdAt;
  private QLUser createdBy;
  private String type;
  private boolean isContinuousEfficiencyEnabled;
  private CEHealthStatus ceHealthStatus;

  public static class QLPcfCloudProviderBuilder implements QLCloudProviderBuilder {}
}
