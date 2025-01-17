/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.gitsync.common.helper;

import static io.harness.data.structure.EmptyPredicate.isNotEmpty;
import static io.harness.exception.WingsException.ReportTarget.REST_API;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.eraro.ErrorCode;
import io.harness.eraro.ResponseMessage;
import io.harness.exception.ExceptionLogger;
import io.harness.exception.ExceptionUtils;
import io.harness.exception.ScmBadRequestException;
import io.harness.exception.ScmException;
import io.harness.exception.WingsException;
import io.harness.exception.ngexception.ErrorMetadataDTO;
import io.harness.gitsync.common.beans.ScmErrorDetails;
import io.harness.gitsync.common.dtos.GitErrorMetadata;

import java.util.List;
import lombok.experimental.UtilityClass;

@UtilityClass
@OwnedBy(HarnessTeam.PL)
public class ScmExceptionUtils {
  public ScmException getScmException(Throwable ex) {
    while (ex != null) {
      if (ex instanceof ScmException) {
        return (ScmException) ex;
      }
      ex = ex.getCause();
    }
    return null;
  }

  public String getHintMessage(Exception ex) {
    WingsException hintException = ExceptionUtils.cause(ErrorCode.HINT, ex);
    if (hintException == null) {
      return "";
    } else {
      return hintException.getMessage();
    }
  }

  public String getExplanationMessage(Exception ex) {
    WingsException explanationException = ExceptionUtils.cause(ErrorCode.EXPLANATION, ex);
    if (explanationException == null) {
      return "";
    } else {
      return explanationException.getMessage();
    }
  }

  public static String getMessage(WingsException ex) {
    List<ResponseMessage> responseMessageList = ExceptionLogger.getResponseMessageList(ex, REST_API);
    if (isNotEmpty(responseMessageList)) {
      return responseMessageList.get(responseMessageList.size() - 1).getMessage();
    }
    return "Unexpected error occurred while performing scm operation.";
  }

  public static GitErrorMetadata getGitErrorMetadata(WingsException ex) {
    ErrorMetadataDTO errorMetadata = ExceptionUtils.getErrorMetadata(ex, GitErrorMetadata.TYPE);
    if (errorMetadata == null) {
      return GitErrorMetadata.builder().build();
    }
    return (GitErrorMetadata) errorMetadata;
  }

  public static boolean isNestedScmBadRequestException(WingsException ex) {
    return ExceptionUtils.cause(ScmBadRequestException.class, ex) != null;
  }

  public static ScmErrorDetails getScmErrorDetails(Exception exception) {
    return ScmErrorDetails.builder()
        .error(exception.getMessage())
        .explanation(ScmExceptionUtils.getExplanationMessage(exception))
        .hint(ScmExceptionUtils.getHintMessage(exception))
        .build();
  }
}
