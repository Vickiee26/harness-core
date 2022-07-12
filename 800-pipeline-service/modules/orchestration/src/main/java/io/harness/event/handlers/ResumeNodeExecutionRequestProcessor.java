/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.event.handlers;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.engine.OrchestrationEngine;
import io.harness.pms.contracts.execution.events.ResumeNodeExecutionRequest;
import io.harness.pms.contracts.execution.events.SdkResponseEventProto;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@OwnedBy(HarnessTeam.PIPELINE)
@Singleton
public class ResumeNodeExecutionRequestProcessor implements SdkResponseProcessor {
  @Inject OrchestrationEngine engine;

  @Override
  public void handleEvent(SdkResponseEventProto event) {
    ResumeNodeExecutionRequest request = event.getResumeNodeExecutionRequest();
    engine.resumeNodeExecution(event.getAmbiance(), request.getResponseMap(), request.getAsyncError());
  }
}