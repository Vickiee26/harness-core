/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package software.wings.service.impl.aws.model.response;

import io.harness.annotations.dev.HarnessModule;
import io.harness.annotations.dev.TargetModule;
import io.harness.beans.ExecutionStatus;
import io.harness.delegate.beans.DelegateMetaInfo;

import software.wings.service.impl.aws.model.AwsResponse;

import com.amazonaws.services.cloudwatch.model.MetricDataResult;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@TargetModule(HarnessModule._950_DELEGATE_TASKS_BEANS)
public class AwsCloudWatchMetricDataResponse implements AwsResponse {
  private DelegateMetaInfo delegateMetaInfo;
  private ExecutionStatus executionStatus;
  private String errorMessage;
  private List<MetricDataResult> metricDataResults;

  @Builder
  public AwsCloudWatchMetricDataResponse(DelegateMetaInfo delegateMetaInfo, ExecutionStatus executionStatus,
      String errorMessage, List<MetricDataResult> metricDataResults) {
    this.delegateMetaInfo = delegateMetaInfo;
    this.executionStatus = executionStatus;
    this.errorMessage = errorMessage;
    this.metricDataResults = metricDataResults;
  }
}