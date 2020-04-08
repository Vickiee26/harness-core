package io.harness.batch.processing.events.deployment.writer;

import com.google.inject.Singleton;

import io.harness.batch.processing.ccm.CCMJobConstants;
import io.harness.batch.processing.ccm.CostEventSource;
import io.harness.batch.processing.ccm.CostEventType;
import io.harness.batch.processing.events.timeseries.data.CostEventData;
import io.harness.batch.processing.events.timeseries.service.intfc.CostEventService;
import io.harness.persistence.HPersistence;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import software.wings.api.DeploymentSummary;
import software.wings.beans.ResourceLookup;
import software.wings.beans.instance.HarnessServiceInfo;
import software.wings.service.intfc.instance.CloudToHarnessMappingService;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Singleton
public class DeploymentEventWriter implements ItemWriter<List<String>> {
  @Autowired private HPersistence hPersistence;
  @Autowired private CostEventService costEventService;
  @Autowired private CloudToHarnessMappingService cloudToHarnessMappingService;

  private JobParameters parameters;

  private static final String DEPLOYMENT_EVENT_DESCRIPTION = "Service %s got deployed to %s.";

  @BeforeStep
  public void beforeStep(final StepExecution stepExecution) {
    parameters = stepExecution.getJobExecution().getJobParameters();
  }

  @Override
  public void write(List<? extends List<String>> list) {
    String accountId = parameters.getString(CCMJobConstants.ACCOUNT_ID);
    int offset = 0;
    Instant startTime = CCMJobConstants.getFieldValueFromJobParams(parameters, CCMJobConstants.JOB_START_DATE);
    Instant endTime = CCMJobConstants.getFieldValueFromJobParams(parameters, CCMJobConstants.JOB_END_DATE);

    List<DeploymentSummary> deploymentSummaries =
        cloudToHarnessMappingService.getDeploymentSummary(accountId, String.valueOf(offset), startTime, endTime);

    do {
      logger.info("deploymentSummaries data size {}", deploymentSummaries.size());
      offset = offset + deploymentSummaries.size();
      createCostEvent(accountId, deploymentSummaries);
      deploymentSummaries =
          cloudToHarnessMappingService.getDeploymentSummary(accountId, String.valueOf(offset), startTime, endTime);
    } while (!deploymentSummaries.isEmpty());
  }

  private void createCostEvent(String accountId, List<DeploymentSummary> deploymentSummaries) {
    List<HarnessServiceInfo> harnessServiceInfoList =
        cloudToHarnessMappingService.getHarnessServiceInfoList((List<DeploymentSummary>) deploymentSummaries);
    Map<String, HarnessServiceInfo> infraMappingHarnessServiceInfo =
        harnessServiceInfoList.stream().collect(Collectors.toMap(
            HarnessServiceInfo::getInfraMappingId, Function.identity(), (existing, replacement) -> existing));

    List<String> resourceIdList =
        harnessServiceInfoList.stream().map(HarnessServiceInfo::getServiceId).collect(Collectors.toList());
    resourceIdList.addAll(
        harnessServiceInfoList.stream().map(HarnessServiceInfo::getEnvId).collect(Collectors.toList()));

    List<ResourceLookup> resourceList = cloudToHarnessMappingService.getResourceList(accountId, resourceIdList);
    Map<String, ResourceLookup> resourceLookupMap = resourceList.stream().collect(
        Collectors.toMap(ResourceLookup::getResourceId, Function.identity(), (existing, replacement) -> existing));

    List<CostEventData> cloudEventDataList = new ArrayList<>();
    deploymentSummaries.forEach(deploymentSummary -> {
      HarnessServiceInfo harnessServiceInfo = infraMappingHarnessServiceInfo.get(deploymentSummary.getInfraMappingId());
      String serviceId = harnessServiceInfo.getServiceId();
      String envId = harnessServiceInfo.getEnvId();
      String serviceName =
          resourceLookupMap.containsKey(serviceId) ? resourceLookupMap.get(serviceId).getResourceName() : serviceId;
      String envName = resourceLookupMap.containsKey(envId) ? resourceLookupMap.get(envId).getResourceName() : envId;
      String deploymentEventDescription = String.format(DEPLOYMENT_EVENT_DESCRIPTION, serviceName, envName);
      CostEventData cloudEventData = CostEventData.builder()
                                         .accountId(accountId)
                                         .appId(harnessServiceInfo.getAppId())
                                         .serviceId(serviceId)
                                         .envId(envId)
                                         .cloudProviderId(harnessServiceInfo.getCloudProviderId())
                                         .deploymentId(deploymentSummary.getUuid())
                                         .eventDescription(deploymentEventDescription)
                                         .costEventType(CostEventType.DEPLOYMENT.name())
                                         .costEventSource(CostEventSource.HARNESS_CD.name())
                                         .startTimestamp(deploymentSummary.getDeployedAt())
                                         .build();
      logger.debug("cloud event data {}", cloudEventData.toString());
      cloudEventDataList.add(cloudEventData);
    });

    if (!cloudEventDataList.isEmpty()) {
      costEventService.create(cloudEventDataList);
    }
  }
}
