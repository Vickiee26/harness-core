/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.cvng.core.services.impl;

import io.harness.cvng.beans.DataCollectionInfo;
import io.harness.cvng.beans.DataCollectionRequest;
import io.harness.cvng.beans.DataCollectionRequestType;
import io.harness.cvng.beans.DataSourceType;
import io.harness.cvng.beans.SyncDataCollectionRequest;
import io.harness.cvng.core.beans.OnboardingRequestDTO;
import io.harness.cvng.core.beans.OnboardingResponseDTO;
import io.harness.cvng.core.beans.healthsource.HealthSourceRecordsRequest;
import io.harness.cvng.core.beans.healthsource.HealthSourceRecordsResponse;
import io.harness.cvng.core.beans.healthsource.LogRecord;
import io.harness.cvng.core.beans.healthsource.LogRecordsResponse;
import io.harness.cvng.core.beans.healthsource.MetricRecordsResponse;
import io.harness.cvng.core.beans.healthsource.QueryRecordsRequest;
import io.harness.cvng.core.beans.healthsource.TimeSeries;
import io.harness.cvng.core.beans.healthsource.TimeSeriesDataPoint;
import io.harness.cvng.core.beans.params.ProjectParams;
import io.harness.cvng.core.entities.CVConfig;
import io.harness.cvng.core.entities.MetricPack;
import io.harness.cvng.core.entities.VerificationTask;
import io.harness.cvng.core.services.api.DataCollectionInfoMapper;
import io.harness.cvng.core.services.api.HealthSourceOnboardingService;
import io.harness.cvng.core.services.api.MetricPackService;
import io.harness.cvng.core.services.api.OnboardingService;
import io.harness.cvng.core.utils.HealthSourceOnboardMappingUtils;
import io.harness.cvng.exception.NotImplementedForHealthSourceException;
import io.harness.data.structure.UUIDGenerator;
import io.harness.datacollection.entity.LogDataRecord;
import io.harness.datacollection.entity.TimeSeriesRecord;
import io.harness.delegate.beans.connector.ConnectorConfigDTO;
import io.harness.serializer.JsonUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class HealthSourceOnboardingServiceImpl implements HealthSourceOnboardingService {
  @Inject private OnboardingService onboardingService;

  @Inject private MetricPackService metricPackService;

  @Inject private Map<DataSourceType, DataCollectionInfoMapper> dataSourceTypeDataCollectionInfoMapperMap;

  @Override
  public HealthSourceRecordsResponse fetchSampleRawRecordsForHealthSource(
      HealthSourceRecordsRequest healthSourceRecordsRequest, ProjectParams projectParams) {
    DataCollectionRequest request;
    switch (healthSourceRecordsRequest.getProviderType()) {
      case SUMOLOGIC_METRICS:
        request = HealthSourceOnboardMappingUtils.getSumologicMetricDataCollectionRequest(healthSourceRecordsRequest);
        break;
      case SUMOLOGIC_LOG:
        request = HealthSourceOnboardMappingUtils.getSumoLogicLogDataCollectionRequest(healthSourceRecordsRequest);
        break;
      default:
        throw new NotImplementedForHealthSourceException("Not Implemented for health source provider.");
    }

    OnboardingRequestDTO onboardingRequestDTO =
        OnboardingRequestDTO.builder()
            .dataCollectionRequest(request)
            .connectorIdentifier(healthSourceRecordsRequest.getConnectorIdentifier())
            .accountId(projectParams.getAccountIdentifier())
            .orgIdentifier(projectParams.getOrgIdentifier())
            .projectIdentifier(projectParams.getProjectIdentifier())
            .tracingId(UUIDGenerator.generateUuid())
            .build();
    OnboardingResponseDTO onboardingResponseDTO =
        onboardingService.getOnboardingResponse(projectParams.getAccountIdentifier(), onboardingRequestDTO);
    Object result = onboardingResponseDTO.getResult();
    HealthSourceRecordsResponse healthSourceRecordsResponse =
        HealthSourceRecordsResponse.builder().providerType(healthSourceRecordsRequest.getProviderType()).build();
    if (!(result instanceof Collection) || ((Collection<?>) result).size() > 0) {
      healthSourceRecordsResponse.getRawRecords().add(result);
    }
    return healthSourceRecordsResponse;
  }

  @Override
  public MetricRecordsResponse fetchMetricData(QueryRecordsRequest queryRecordsRequest, ProjectParams projectParams) {
    String accountIdentifier = projectParams.getAccountIdentifier();
    String orgIdentifier = projectParams.getOrgIdentifier();
    String projectIdentifier = projectParams.getProjectIdentifier();
    DataCollectionInfo<ConnectorConfigDTO> dataCollectionInfo =
        getDataCollectionInfoForMetric(queryRecordsRequest, projectParams);
    DataCollectionRequest<ConnectorConfigDTO> request =
        SyncDataCollectionRequest.builder()
            .type(DataCollectionRequestType.SYNC_DATA_COLLECTION)
            .dataCollectionInfo(dataCollectionInfo)
            .endTime(Instant.ofEpochMilli(queryRecordsRequest.getEndTime()))
            .startTime(Instant.ofEpochMilli(queryRecordsRequest.getStartTime()))
            .build();

    OnboardingRequestDTO onboardingRequestDTO = OnboardingRequestDTO.builder()
                                                    .dataCollectionRequest(request)
                                                    .connectorIdentifier(queryRecordsRequest.getConnectorIdentifier())
                                                    .accountId(accountIdentifier)
                                                    .orgIdentifier(orgIdentifier)
                                                    .tracingId(UUIDGenerator.generateUuid())
                                                    .projectIdentifier(projectIdentifier)
                                                    .build();

    OnboardingResponseDTO response = onboardingService.getOnboardingResponse(accountIdentifier, onboardingRequestDTO);
    List<TimeSeriesRecord> timeSeriesRecords =
        JsonUtils.asList(JsonUtils.asJson(response.getResult()), new TypeReference<>() {});
    TimeSeries timeSeries =
        TimeSeries.builder().timeseriesName("sampleTimeseries").build(); // TODO Understand multiple Timeseries
    // map and collect
    timeSeriesRecords.forEach(timeSeriesRecord
        -> timeSeries.getData().add(TimeSeriesDataPoint.builder()
                                        .timestamp(timeSeriesRecord.getTimestamp())
                                        .value(timeSeriesRecord.getMetricValue())
                                        .build()));
    return MetricRecordsResponse.builder().timeSeriesData(Collections.singletonList(timeSeries)).build();
  }

  @NotNull
  private DataCollectionInfo<ConnectorConfigDTO> getDataCollectionInfoForMetric(
      QueryRecordsRequest queryRecordsRequest, ProjectParams projectParams) {
    String accountIdentifier = projectParams.getAccountIdentifier();
    String orgIdentifier = projectParams.getOrgIdentifier();
    String projectIdentifier = projectParams.getProjectIdentifier();
    CVConfig cvConfig;
    List<MetricPack> metricPacks = metricPackService.getMetricPacks(
        accountIdentifier, orgIdentifier, projectIdentifier, queryRecordsRequest.getProviderType());
    metricPackService.populateDataCollectionDsl(queryRecordsRequest.getProviderType(), metricPacks.get(0));
    if (queryRecordsRequest.getProviderType() == DataSourceType.SUMOLOGIC_METRICS) {
      cvConfig =
          HealthSourceOnboardMappingUtils.getCvConfigForNextGenMetric(queryRecordsRequest, projectParams, metricPacks);
    } else {
      throw new NotImplementedForHealthSourceException("Not Implemented for health source provider.");
    }

    DataCollectionInfoMapper<DataCollectionInfo<ConnectorConfigDTO>, CVConfig> dataCollectionInfoMapper =
        dataSourceTypeDataCollectionInfoMapperMap.get(queryRecordsRequest.getProviderType());

    DataCollectionInfo<ConnectorConfigDTO> dataCollectionInfo =
        dataCollectionInfoMapper.toDataCollectionInfo(cvConfig, VerificationTask.TaskType.SLI);

    dataCollectionInfo.setCollectHostData(false);
    return dataCollectionInfo;
  }

  @Override
  public LogRecordsResponse fetchLogData(QueryRecordsRequest queryRecordsRequest, ProjectParams projectParams) {
    String accountIdentifier = projectParams.getAccountIdentifier();
    String orgIdentifier = projectParams.getOrgIdentifier();
    String projectIdentifier = projectParams.getProjectIdentifier();
    DataCollectionInfo<ConnectorConfigDTO> dataCollectionInfo =
        getDataCollectionInfoForLog(queryRecordsRequest, projectParams);
    dataCollectionInfo.setCollectHostData(false);
    DataCollectionRequest<ConnectorConfigDTO> request =
        SyncDataCollectionRequest.builder()
            .type(DataCollectionRequestType.SYNC_DATA_COLLECTION)
            .dataCollectionInfo(dataCollectionInfo)
            .endTime(Instant.ofEpochMilli(queryRecordsRequest.getEndTime()))
            .startTime(Instant.ofEpochMilli(queryRecordsRequest.getStartTime()))
            .build();

    OnboardingRequestDTO onboardingRequestDTO = OnboardingRequestDTO.builder()
                                                    .dataCollectionRequest(request)
                                                    .connectorIdentifier(queryRecordsRequest.getConnectorIdentifier())
                                                    .accountId(accountIdentifier)
                                                    .orgIdentifier(orgIdentifier)
                                                    .projectIdentifier(projectIdentifier)
                                                    .tracingId(UUIDGenerator.generateUuid())
                                                    .build();
    OnboardingResponseDTO response = onboardingService.getOnboardingResponse(accountIdentifier, onboardingRequestDTO);
    List<LogDataRecord> logDataRecords =
        JsonUtils.asList(JsonUtils.asJson(response.getResult()), new TypeReference<>() {});
    List<LogRecord> logRecords = new ArrayList<>();
    logDataRecords.forEach(logDataRecord
        -> logRecords.add(LogRecord.builder()
                              .timestamp(logDataRecord.getTimestamp())
                              .message(logDataRecord.getLog())
                              .serviceInstance(logDataRecord.getHostname())
                              .build()));
    return LogRecordsResponse.builder().logRecords(logRecords).build();
  }

  @NotNull
  private DataCollectionInfo<ConnectorConfigDTO> getDataCollectionInfoForLog(
      QueryRecordsRequest queryRecordsRequest, ProjectParams projectParams) {
    CVConfig cvConfig;
    if (queryRecordsRequest.getProviderType() == DataSourceType.SUMOLOGIC_LOG) {
      cvConfig = HealthSourceOnboardMappingUtils.getCVConfigForNextGenLog(queryRecordsRequest, projectParams);
    } else {
      throw new NotImplementedForHealthSourceException("Not Implemented for health source provider.");
    }
    DataCollectionInfoMapper<DataCollectionInfo<ConnectorConfigDTO>, CVConfig> dataCollectionInfoMapper =
        dataSourceTypeDataCollectionInfoMapperMap.get(queryRecordsRequest.getProviderType());
    return dataCollectionInfoMapper.toDataCollectionInfo(cvConfig, VerificationTask.TaskType.DEPLOYMENT);
  }
}