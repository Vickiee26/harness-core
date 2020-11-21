package io.harness.redesign.states.http;

import static io.harness.annotations.dev.HarnessTeam.CDC;
import static io.harness.data.structure.UUIDGenerator.generateUuid;
import static io.harness.delegate.beans.TaskData.DEFAULT_ASYNC_CALL_TIMEOUT;

import io.harness.ambiance.Ambiance;
import io.harness.annotations.Redesign;
import io.harness.annotations.dev.OwnedBy;
import io.harness.beans.DelegateTask;
import io.harness.beans.DelegateTask.DelegateTaskBuilder;
import io.harness.delegate.beans.ErrorNotifyResponseData;
import io.harness.delegate.beans.TaskData;
import io.harness.delegate.task.http.HttpTaskParameters;
import io.harness.facilitator.modes.task.TaskExecutable;
import io.harness.pms.execution.Status;
import io.harness.pms.steps.StepType;
import io.harness.state.Step;
import io.harness.state.io.FailureInfo;
import io.harness.state.io.StepInputPackage;
import io.harness.state.io.StepResponse;
import io.harness.state.io.StepResponse.StepOutcome;
import io.harness.state.io.StepResponse.StepResponseBuilder;
import io.harness.tasks.Cd1SetupFields;
import io.harness.tasks.ResponseData;

import software.wings.api.HttpStateExecutionData;
import software.wings.beans.TaskType;
import software.wings.sm.states.HttpState.HttpStateExecutionResponse;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@OwnedBy(CDC)
@Redesign
@Slf4j
public class BasicHttpStep implements Step, TaskExecutable<BasicHttpStepParameters> {
  public static final StepType STEP_TYPE = StepType.newBuilder().setType("BASIC_HTTP").build();
  private static final int socketTimeoutMillis = 10000;

  @Override
  public DelegateTask obtainTask(
      Ambiance ambiance, BasicHttpStepParameters stepParameters, StepInputPackage inputPackage) {
    HttpTaskParameters httpTaskParameters = HttpTaskParameters.builder()
                                                .url(stepParameters.getUrl())
                                                .body(stepParameters.getBody())
                                                .header(stepParameters.getHeader())
                                                .method(stepParameters.getMethod())
                                                .socketTimeoutMillis(socketTimeoutMillis)
                                                .build();

    String waitId = generateUuid();
    DelegateTaskBuilder delegateTaskBuilder =
        DelegateTask.builder()
            .accountId(ambiance.getSetupAbstractions().get("accountId"))
            .waitId(waitId)
            .data(TaskData.builder()
                      .taskType(TaskType.HTTP.name())
                      .parameters(new Object[] {httpTaskParameters})
                      .timeout(DEFAULT_ASYNC_CALL_TIMEOUT)
                      .build())
            .setupAbstraction(Cd1SetupFields.INFRASTRUCTURE_MAPPING_ID_FIELD, waitId);
    String appId = ambiance.getSetupAbstractions().get("appId");
    if (appId != null) {
      delegateTaskBuilder.setupAbstraction(Cd1SetupFields.APP_ID_FIELD, appId);
    }
    return delegateTaskBuilder.build();
  }

  @Override
  public StepResponse handleTaskResult(
      Ambiance ambiance, BasicHttpStepParameters stepParameters, Map<String, ResponseData> responseDataMap) {
    StepResponseBuilder responseBuilder = StepResponse.builder();
    ResponseData notifyResponseData = responseDataMap.values().iterator().next();
    if (notifyResponseData instanceof ErrorNotifyResponseData) {
      ErrorNotifyResponseData errorNotifyResponseData = (ErrorNotifyResponseData) notifyResponseData;
      responseBuilder.status(Status.FAILED);
      responseBuilder
          .failureInfo(FailureInfo.builder()
                           .errorMessage(errorNotifyResponseData.getErrorMessage())
                           .failureTypes(errorNotifyResponseData.getFailureTypes())
                           .build())
          .build();
    } else {
      HttpStateExecutionResponse httpStateExecutionResponse = (HttpStateExecutionResponse) notifyResponseData;
      HttpStateExecutionData executionData = HttpStateExecutionData.builder()
                                                 .httpUrl(stepParameters.getUrl())
                                                 .httpMethod(stepParameters.getMethod())
                                                 .httpResponseCode(httpStateExecutionResponse.getHttpResponseCode())
                                                 .httpResponseBody(httpStateExecutionResponse.getHttpResponseBody())
                                                 .status(httpStateExecutionResponse.getExecutionStatus())
                                                 .errorMsg(httpStateExecutionResponse.getErrorMessage())
                                                 .build();
      // Just Place holder for now till we have assertions
      if (httpStateExecutionResponse.getHttpResponseCode() == 500) {
        responseBuilder.status(Status.FAILED);
      } else {
        responseBuilder.status(Status.SUCCEEDED);
      }
      responseBuilder.stepOutcome(StepOutcome.builder().name("http").outcome(executionData).build());
    }
    return responseBuilder.build();
  }
}
