package software.wings.graphql.schema.type.artifactSource;

import static io.harness.annotations.dev.HarnessTeam.CDC;

import io.harness.annotations.dev.OwnedBy;

import software.wings.security.PermissionAttribute;
import software.wings.security.annotations.Scope;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

@OwnedBy(CDC)
@Value
@Builder
@FieldNameConstants(innerTypeName = "QLArtifactoryArtifactSourceKeys")
@Scope(PermissionAttribute.ResourceType.SERVICE)
public class QLArtifactoryArtifactSource implements QLArtifactSource {
  String name;
  String id;
  Long createdAt;
  QLArtifactoryProps properties;
}
