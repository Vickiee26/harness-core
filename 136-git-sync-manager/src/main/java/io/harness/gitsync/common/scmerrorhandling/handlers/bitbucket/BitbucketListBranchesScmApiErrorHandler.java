/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.gitsync.common.scmerrorhandling.handlers.bitbucket;

import static io.harness.annotations.dev.HarnessTeam.PL;

import io.harness.annotations.dev.OwnedBy;
import io.harness.exception.NestedExceptionUtils;
import io.harness.exception.ScmResourceNotFoundException;
import io.harness.exception.ScmUnauthorizedException;
import io.harness.exception.ScmUnexpectedException;
import io.harness.exception.WingsException;
import io.harness.gitsync.common.scmerrorhandling.handlers.ScmApiErrorHandler;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@OwnedBy(PL)
public class BitbucketListBranchesScmApiErrorHandler implements ScmApiErrorHandler {
  public static final String LIST_BRANCH_FAILED_MESSAGE = "Listing branches from Bitbucket failed. ";
  @Override
  public void handleError(int statusCode, String errorMessage) throws WingsException {
    switch (statusCode) {
      case 401:
      case 403:
        throw NestedExceptionUtils.hintWithExplanationException(ScmErrorHints.INVALID_CREDENTIALS,
            LIST_BRANCH_FAILED_MESSAGE + ScmErrorExplanations.INVALID_CONNECTOR_CREDS,
            new ScmUnauthorizedException(errorMessage));
      case 404:
        throw NestedExceptionUtils.hintWithExplanationException(ScmErrorHints.REPO_NOT_FOUND,
            LIST_BRANCH_FAILED_MESSAGE + ScmErrorExplanations.REPO_NOT_FOUND,
            new ScmResourceNotFoundException(errorMessage));
      default:
        log.error(String.format("Error while listing bitbucket branches: [%s: %s]", statusCode, errorMessage));
        throw new ScmUnexpectedException(errorMessage);
    }
  }
}