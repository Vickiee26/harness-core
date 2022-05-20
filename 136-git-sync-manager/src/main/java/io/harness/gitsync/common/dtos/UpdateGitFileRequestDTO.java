package io.harness.gitsync.common.dtos;

import static io.harness.annotations.dev.HarnessTeam.PL;

import io.harness.annotations.dev.OwnedBy;
import io.harness.beans.Scope;
import io.harness.delegate.beans.connector.scm.ScmConnector;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@OwnedBy(PL)
public class UpdateGitFileRequestDTO {
  Scope scope;
  String branchName;
  String fileContent;
  String filePath;
  String commitMessage;
  String oldCommitId;
  String oldFileSha;
  ScmConnector scmConnector;
}