package software.wings.service.impl.apm;

import static io.harness.data.structure.EmptyPredicate.isNotEmpty;
import static io.harness.delegate.task.mixin.HttpConnectionExecutionCapabilityGenerator.HttpCapabilityDetailsLevel.QUERY;

import io.harness.delegate.beans.executioncapability.ExecutionCapability;
import io.harness.delegate.beans.executioncapability.ExecutionCapabilityDemander;
import io.harness.delegate.task.TaskParameters;
import io.harness.delegate.task.mixin.HttpConnectionExecutionCapabilityGenerator;
import io.harness.security.encryption.EncryptedDataDetail;

import software.wings.delegatetasks.delegatecapability.CapabilityHelper;
import software.wings.service.impl.analysis.AnalysisComparisonStrategy;
import software.wings.sm.StateType;
import software.wings.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class APMDataCollectionInfo implements TaskParameters, ExecutionCapabilityDemander {
  private String baseUrl;
  private String validationUrl;
  private Map<String, String> headers;
  private Map<String, String> options;
  private List<EncryptedDataDetail> encryptedDataDetails;
  private List<APMMetricInfo> canaryMetricInfos;
  private Map<String, List<APMMetricInfo>> metricEndpoints;
  private Map<String, String> hosts;
  private StateType stateType;
  private long startTime;
  private int dataCollectionMinute;
  private String applicationId;
  private String stateExecutionId;
  private String workflowId;
  private String workflowExecutionId;
  private String serviceId;
  private String accountId;
  private AnalysisComparisonStrategy strategy;
  private int dataCollectionFrequency;
  private int dataCollectionTotalTime;
  private String cvConfigId;
  private int initialDelaySeconds;

  @Override
  public List<ExecutionCapability> fetchRequiredExecutionCapabilities() {
    List<ExecutionCapability> executionCapabilities = new ArrayList<>();
    executionCapabilities.add(HttpConnectionExecutionCapabilityGenerator.buildHttpConnectionExecutionCapability(
        Utils.appendPathToBaseUrl(baseUrl, validationUrl), QUERY));
    executionCapabilities.addAll(
        CapabilityHelper.fetchExecutionCapabilitiesForEncryptedDataDetails(encryptedDataDetails));
    return executionCapabilities;
  }

  public boolean isCanaryUrlPresent() {
    return isNotEmpty(canaryMetricInfos);
  }
}
