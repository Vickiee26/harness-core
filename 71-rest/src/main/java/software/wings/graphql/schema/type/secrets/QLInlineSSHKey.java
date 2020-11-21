package software.wings.graphql.schema.type.secrets;

import software.wings.graphql.schema.type.QLObject;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

@Value
@Builder
@FieldNameConstants(innerTypeName = "QLInlineSSHKeyKeys")
public class QLInlineSSHKey implements QLObject {
  String sshKeySecretFileId;
  String passphraseSecretId;
}
