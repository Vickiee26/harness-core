/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.cdng.azure.webapp;

import static io.harness.annotations.dev.HarnessTeam.CDP;
import static io.harness.azure.model.AzureConstants.DEPLOY_TO_SLOT;
import static io.harness.azure.model.AzureConstants.SAVE_EXISTING_CONFIGURATIONS;
import static io.harness.azure.model.AzureConstants.UPDATE_SLOT_CONFIGURATION_SETTINGS;
import static io.harness.cdng.stepsdependency.constants.OutcomeExpressionConstants.APPLICATION_SETTINGS;
import static io.harness.cdng.stepsdependency.constants.OutcomeExpressionConstants.CONNECTION_STRINGS;
import static io.harness.cdng.stepsdependency.constants.OutcomeExpressionConstants.STARTUP_SCRIPT;
import static io.harness.k8s.K8sCommandUnitConstants.FetchFiles;
import static io.harness.rule.OwnerRule.ABOSII;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import io.harness.annotations.dev.OwnedBy;
import io.harness.category.element.UnitTests;
import io.harness.cdng.CDNGTestBase;
import io.harness.cdng.CDStepHelper;
import io.harness.cdng.azure.webapp.beans.AzureSlotDeploymentPassThroughData;
import io.harness.cdng.azure.webapp.beans.AzureWebAppPreDeploymentDataOutput;
import io.harness.cdng.azure.webapp.beans.AzureWebAppSlotDeploymentDataOutput;
import io.harness.cdng.infra.beans.AzureWebAppInfrastructureOutcome;
import io.harness.cdng.manifest.yaml.BitbucketStore;
import io.harness.cdng.manifest.yaml.GitStoreConfig;
import io.harness.cdng.manifest.yaml.harness.HarnessStore;
import io.harness.cdng.manifest.yaml.storeConfig.StoreConfig;
import io.harness.delegate.beans.logstreaming.UnitProgressData;
import io.harness.delegate.beans.storeconfig.FetchType;
import io.harness.delegate.task.TaskParameters;
import io.harness.delegate.task.azure.appservice.AzureAppServicePreDeploymentData;
import io.harness.delegate.task.azure.appservice.webapp.ng.AzureWebAppInfraDelegateConfig;
import io.harness.delegate.task.azure.appservice.webapp.ng.request.AzureWebAppFetchPreDeploymentDataRequest;
import io.harness.delegate.task.azure.appservice.webapp.ng.request.AzureWebAppSlotDeploymentRequest;
import io.harness.delegate.task.azure.appservice.webapp.ng.response.AzureWebAppFetchPreDeploymentDataResponse;
import io.harness.delegate.task.azure.appservice.webapp.ng.response.AzureWebAppSlotDeploymentResponse;
import io.harness.delegate.task.azure.appservice.webapp.ng.response.AzureWebAppTaskResponse;
import io.harness.delegate.task.azure.appservice.webapp.response.AzureAppDeploymentData;
import io.harness.delegate.task.azure.artifact.AzureArtifactConfig;
import io.harness.delegate.task.git.GitFetchResponse;
import io.harness.git.model.FetchFilesResult;
import io.harness.git.model.GitFile;
import io.harness.logging.UnitProgress;
import io.harness.plancreator.steps.common.StepElementParameters;
import io.harness.pms.contracts.ambiance.Ambiance;
import io.harness.pms.contracts.execution.Status;
import io.harness.pms.contracts.execution.tasks.TaskRequest;
import io.harness.pms.contracts.steps.StepCategory;
import io.harness.pms.plan.execution.SetupAbstractionKeys;
import io.harness.pms.sdk.core.resolver.outputs.ExecutionSweepingOutputService;
import io.harness.pms.sdk.core.steps.executables.TaskChainResponse;
import io.harness.pms.sdk.core.steps.io.StepInputPackage;
import io.harness.pms.sdk.core.steps.io.StepResponse;
import io.harness.pms.yaml.ParameterField;
import io.harness.rule.Owner;

import software.wings.beans.TaskType;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@OwnedBy(CDP)
public class AzureWebAppSlotDeploymentStepTest extends CDNGTestBase {
  private static final String APP_SETTINGS_FILE_CONTENT = "[{\"name\": \"app\", \"value\": \"test\"}]";
  private static final String CONN_STRINGS_FILE_CONTENT =
      "[{\"name\": \"conn\", \"value\": \"test\", \"type\": \"MySql\"}]";
  private static final String STARTUP_SCRIPT_FILE_CONTENT = "echo 'test'";

  @Mock private AzureWebAppStepHelper azureWebAppStepHelper;
  @Mock private CDStepHelper cdStepHelper;
  @Mock private ExecutionSweepingOutputService executionSweepingOutputService;

  @InjectMocks private AzureWebAppSlotDeploymentStep slotDeploymentStep;

  @Mock private AzureArtifactConfig azureArtifactConfig;
  @Mock private AzureWebAppInfraDelegateConfig infraDelegateConfig;
  private final AzureWebAppInfrastructureOutcome infrastructure = AzureWebAppInfrastructureOutcome.builder().build();
  private final Ambiance ambiance =
      Ambiance.newBuilder().putSetupAbstractions(SetupAbstractionKeys.accountId, "accountId").build();
  private final StepInputPackage stepInputPackage = StepInputPackage.builder().build();
  private final TaskRequest taskRequest = TaskRequest.newBuilder().build();
  private final TaskRequest gitTaskRequest = TaskRequest.newBuilder().build();

  @Before
  public void setupTest() {
    doReturn(azureArtifactConfig).when(azureWebAppStepHelper).getPrimaryArtifactConfig(ambiance);
    doReturn(infrastructure).when(cdStepHelper).getInfrastructureOutcome(ambiance);
    doReturn(infraDelegateConfig).when(azureWebAppStepHelper).getInfraDelegateConfig(ambiance, infrastructure);
    doReturn(taskRequest)
        .when(azureWebAppStepHelper)
        .prepareTaskRequest(
            any(StepElementParameters.class), eq(ambiance), any(TaskParameters.class), any(TaskType.class), anyList());
    doReturn(gitTaskRequest)
        .when(azureWebAppStepHelper)
        .prepareGitFetchTaskRequest(any(StepElementParameters.class), eq(ambiance), anyMap(), anyList());
    doAnswer(invocation -> {
      GitFetchResponse fetchResponse = invocation.getArgument(1);
      return fetchResponse.getFilesFromMultipleRepo().entrySet().stream().collect(
          Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getFiles().get(0).getFileContent()));
    })
        .when(azureWebAppStepHelper)
        .getConfigValuesFromGitFetchResponse(eq(ambiance), any(GitFetchResponse.class));
  }

  @Test
  @Owner(developers = ABOSII)
  @Category(UnitTests.class)
  public void testStartChainLinkNoConfigs() {
    final StepElementParameters stepElementParameters = createTestStepElementParameters();
    final List<String> expectedCommandUnits =
        asList(SAVE_EXISTING_CONFIGURATIONS, UPDATE_SLOT_CONFIGURATION_SETTINGS, DEPLOY_TO_SLOT);
    doReturn(emptyMap()).when(azureWebAppStepHelper).fetchWebAppConfig(ambiance);

    ArgumentCaptor<TaskParameters> taskParametersArgumentCaptor = ArgumentCaptor.forClass(TaskParameters.class);
    TaskChainResponse taskChainResponse =
        slotDeploymentStep.startChainLinkAfterRbac(ambiance, stepElementParameters, stepInputPackage);

    verify(azureWebAppStepHelper)
        .prepareTaskRequest(eq(stepElementParameters), eq(ambiance), taskParametersArgumentCaptor.capture(),
            eq(TaskType.AZURE_WEB_APP_TASK_NG), eq(expectedCommandUnits));
    TaskParameters taskParameters = taskParametersArgumentCaptor.getValue();
    assertThat(taskParameters).isInstanceOf(AzureWebAppFetchPreDeploymentDataRequest.class);
    AzureWebAppFetchPreDeploymentDataRequest dataRequest = (AzureWebAppFetchPreDeploymentDataRequest) taskParameters;
    assertThat(dataRequest.getArtifact()).isSameAs(azureArtifactConfig);
    assertThat(dataRequest.getInfrastructure()).isSameAs(infraDelegateConfig);
    assertThat(dataRequest.getApplicationSettings()).isEmpty();
    assertThat(dataRequest.getConnectionStrings()).isEmpty();
    assertThat(dataRequest.getStartupCommand()).isNullOrEmpty();
    assertThat(dataRequest.getTimeoutIntervalInMin()).isEqualTo(10);
    assertThat(taskChainResponse.isChainEnd()).isFalse();
    assertThat(taskChainResponse.getTaskRequest()).isSameAs(taskRequest);
  }

  @Test
  @Owner(developers = ABOSII)
  @Category(UnitTests.class)
  public void testStartChainLinkGitConfigs() {
    final StepElementParameters stepElementParameters = createTestStepElementParameters();
    final Map<String, GitStoreConfig> gitStoreConfigMap =
        ImmutableMap.of(APPLICATION_SETTINGS, createTestGitStoreConfig());
    final Map<String, StoreConfig> otherTypesConfigMap = ImmutableMap.of(CONNECTION_STRINGS, createTestHarnessStore());
    final Map<String, StoreConfig> configsMap =
        ImmutableMap.<String, StoreConfig>builder().putAll(gitStoreConfigMap).putAll(otherTypesConfigMap).build();

    doReturn(configsMap).when(azureWebAppStepHelper).fetchWebAppConfig(ambiance);

    TaskChainResponse taskChainResponse =
        slotDeploymentStep.startChainLinkAfterRbac(ambiance, stepElementParameters, stepInputPackage);
    verify(azureWebAppStepHelper)
        .prepareGitFetchTaskRequest(eq(stepElementParameters), eq(ambiance), eq(gitStoreConfigMap),
            eq(asList(FetchFiles, SAVE_EXISTING_CONFIGURATIONS, UPDATE_SLOT_CONFIGURATION_SETTINGS, DEPLOY_TO_SLOT)));
    assertThat(taskChainResponse.isChainEnd()).isFalse();
    assertThat(taskChainResponse.getTaskRequest()).isSameAs(gitTaskRequest);
    assertThat(taskChainResponse.getPassThroughData()).isInstanceOf(AzureSlotDeploymentPassThroughData.class);
    AzureSlotDeploymentPassThroughData passThroughData =
        (AzureSlotDeploymentPassThroughData) taskChainResponse.getPassThroughData();
    assertThat(passThroughData.getUnprocessedConfigs()).isEqualTo(otherTypesConfigMap);
  }

  @Test
  @Owner(developers = ABOSII)
  @Category(UnitTests.class)
  public void testStartChainLinkHarnessStoreConfigs() {
    final StepElementParameters stepElementParameters = createTestStepElementParameters();
    final Map<String, HarnessStore> harnessStoreConfigs =
        ImmutableMap.of(APPLICATION_SETTINGS, createTestHarnessStore(), CONNECTION_STRINGS, createTestHarnessStore());
    final Map<String, StoreConfig> configsMap =
        ImmutableMap.<String, StoreConfig>builder().putAll(harnessStoreConfigs).build();
    doReturn(configsMap).when(azureWebAppStepHelper).fetchWebAppConfig(ambiance);

    ArgumentCaptor<TaskParameters> taskParametersArgumentCaptor = ArgumentCaptor.forClass(TaskParameters.class);
    TaskChainResponse taskChainResponse =
        slotDeploymentStep.startChainLinkAfterRbac(ambiance, stepElementParameters, stepInputPackage);

    verify(azureWebAppStepHelper)
        .prepareTaskRequest(eq(stepElementParameters), eq(ambiance), taskParametersArgumentCaptor.capture(),
            eq(TaskType.AZURE_WEB_APP_TASK_NG), anyList());
    verify(azureWebAppStepHelper).fetchWebAppConfigsFromHarnessStore(ambiance, harnessStoreConfigs);

    // After we fetch files from harness store we're going to queue fetch predeployment data task
    assertThat(taskParametersArgumentCaptor.getValue()).isInstanceOf(AzureWebAppFetchPreDeploymentDataRequest.class);
    assertThat(taskChainResponse.isChainEnd()).isFalse();
    assertThat(taskChainResponse.getTaskRequest()).isSameAs(taskRequest);
    assertThat(taskChainResponse.getPassThroughData()).isInstanceOf(AzureSlotDeploymentPassThroughData.class);
    AzureSlotDeploymentPassThroughData passThroughData =
        (AzureSlotDeploymentPassThroughData) taskChainResponse.getPassThroughData();
    assertThat(passThroughData.getUnprocessedConfigs()).isEmpty();
  }

  @Test
  @Owner(developers = ABOSII)
  @Category(UnitTests.class)
  public void testExecuteNextLinkGitFetchResponse() throws Exception {
    final StepElementParameters stepElementParameters = createTestStepElementParameters();
    final GitFetchResponse gitFetchResponse =
        GitFetchResponse.builder()
            .filesFromMultipleRepo(ImmutableMap.of(APPLICATION_SETTINGS,
                FetchFilesResult.builder()
                    .files(singletonList(GitFile.builder().fileContent(APP_SETTINGS_FILE_CONTENT).build()))
                    .build(),
                CONNECTION_STRINGS,
                FetchFilesResult.builder()
                    .files(singletonList(GitFile.builder().fileContent(CONN_STRINGS_FILE_CONTENT).build()))
                    .build()))
            .build();
    final AzureSlotDeploymentPassThroughData passThroughData =
        AzureSlotDeploymentPassThroughData.builder()
            .configs(ImmutableMap.of(STARTUP_SCRIPT, "echo 'test'"))
            .unprocessedConfigs(emptyMap())
            .build();

    ArgumentCaptor<TaskParameters> taskParametersArgumentCaptor = ArgumentCaptor.forClass(TaskParameters.class);
    TaskChainResponse taskChainResponse = slotDeploymentStep.executeNextLinkWithSecurityContext(
        ambiance, stepElementParameters, stepInputPackage, passThroughData, () -> gitFetchResponse);

    verify(azureWebAppStepHelper)
        .prepareTaskRequest(eq(stepElementParameters), eq(ambiance), taskParametersArgumentCaptor.capture(),
            eq(TaskType.AZURE_WEB_APP_TASK_NG), anyList());

    // Next task will fetch predeployment data since no unprocessed configs left
    assertThat(taskParametersArgumentCaptor.getValue()).isInstanceOf(AzureWebAppFetchPreDeploymentDataRequest.class);
    assertThat(taskChainResponse.isChainEnd()).isFalse();
    assertThat(taskChainResponse.getTaskRequest()).isSameAs(taskRequest);
    assertThat(taskChainResponse.getPassThroughData()).isInstanceOf(AzureSlotDeploymentPassThroughData.class);
    AzureSlotDeploymentPassThroughData newPassThroughData =
        (AzureSlotDeploymentPassThroughData) taskChainResponse.getPassThroughData();
    assertThat(newPassThroughData.getConfigs()).containsKeys(APPLICATION_SETTINGS, CONNECTION_STRINGS, STARTUP_SCRIPT);
    assertThat(newPassThroughData.getConfigs())
        .containsValues(APP_SETTINGS_FILE_CONTENT, CONN_STRINGS_FILE_CONTENT, STARTUP_SCRIPT_FILE_CONTENT);
  }

  @Test
  @Owner(developers = ABOSII)
  @Category(UnitTests.class)
  public void testExecuteNextLinkUnprocessedGitConfigs() throws Exception {
    final StepElementParameters stepElementParameters = createTestStepElementParameters();
    final Map<String, GitStoreConfig> unprocessedGitConfigs =
        ImmutableMap.of(APPLICATION_SETTINGS, createTestGitStoreConfig());
    final Map<String, StoreConfig> allUnprocessedConfigs = ImmutableMap.<String, StoreConfig>builder()
                                                               .putAll(unprocessedGitConfigs)
                                                               .put(STARTUP_SCRIPT, createTestHarnessStore())
                                                               .build();

    final AzureSlotDeploymentPassThroughData passThroughData =
        AzureSlotDeploymentPassThroughData.builder()
            .unprocessedConfigs(allUnprocessedConfigs)
            .configs(ImmutableMap.of(CONNECTION_STRINGS, CONN_STRINGS_FILE_CONTENT))
            .build();
    final AzureWebAppTaskResponse azureWebAppTaskResponse = AzureWebAppTaskResponse.builder().build();

    TaskChainResponse taskChainResponse = slotDeploymentStep.executeNextLinkWithSecurityContext(
        ambiance, stepElementParameters, stepInputPackage, passThroughData, () -> azureWebAppTaskResponse);

    verify(azureWebAppStepHelper)
        .prepareGitFetchTaskRequest(stepElementParameters, ambiance, unprocessedGitConfigs,
            asList(FetchFiles, SAVE_EXISTING_CONFIGURATIONS, UPDATE_SLOT_CONFIGURATION_SETTINGS, DEPLOY_TO_SLOT));
    assertThat(taskChainResponse.getPassThroughData()).isInstanceOf(AzureSlotDeploymentPassThroughData.class);
    AzureSlotDeploymentPassThroughData newPassThroughData =
        (AzureSlotDeploymentPassThroughData) taskChainResponse.getPassThroughData();
    assertThat(newPassThroughData.getUnprocessedConfigs()).containsKeys(STARTUP_SCRIPT);
  }

  @Test
  @Owner(developers = ABOSII)
  @Category(UnitTests.class)
  public void testExecuteNextLinkUnprocessedHarnessStoreWithPreDeploymentData() throws Exception {
    final StepElementParameters stepElementParameters = createTestStepElementParameters();
    final Map<String, HarnessStore> unprocessedHarnessStore =
        ImmutableMap.of(APPLICATION_SETTINGS, createTestHarnessStore());
    final Map<String, StoreConfig> allUnprocessedConfigs =
        ImmutableMap.<String, StoreConfig>builder()
            .putAll(unprocessedHarnessStore) // make compiler understand generics
            .build();

    final AzureSlotDeploymentPassThroughData passThroughData =
        AzureSlotDeploymentPassThroughData.builder()
            .unprocessedConfigs(allUnprocessedConfigs)
            .configs(ImmutableMap.of(CONNECTION_STRINGS, CONN_STRINGS_FILE_CONTENT))
            .preDeploymentData(AzureAppServicePreDeploymentData.builder().build())
            .build();
    final AzureWebAppTaskResponse azureWebAppTaskResponse = AzureWebAppTaskResponse.builder().build();
    final ArgumentCaptor<TaskParameters> taskParametersArgumentCaptor = ArgumentCaptor.forClass(TaskParameters.class);

    doReturn(ImmutableMap.of(APPLICATION_SETTINGS, APP_SETTINGS_FILE_CONTENT))
        .when(azureWebAppStepHelper)
        .fetchWebAppConfigsFromHarnessStore(ambiance, unprocessedHarnessStore);

    TaskChainResponse taskChainResponse = slotDeploymentStep.executeNextLinkWithSecurityContext(
        ambiance, stepElementParameters, stepInputPackage, passThroughData, () -> azureWebAppTaskResponse);

    verify(azureWebAppStepHelper)
        .prepareTaskRequest(eq(stepElementParameters), eq(ambiance), taskParametersArgumentCaptor.capture(),
            eq(TaskType.AZURE_WEB_APP_TASK_NG), anyList());

    // Next task will be slot deployment since no unprocessed configs left and predeployment data is already present
    assertThat(taskParametersArgumentCaptor.getValue()).isInstanceOf(AzureWebAppSlotDeploymentRequest.class);

    verify(azureWebAppStepHelper).fetchWebAppConfigsFromHarnessStore(ambiance, unprocessedHarnessStore);
    assertThat(taskChainResponse.isChainEnd()).isTrue();
    assertThat(taskChainResponse.getPassThroughData()).isInstanceOf(AzureSlotDeploymentPassThroughData.class);
    AzureSlotDeploymentPassThroughData newPassThroughData =
        (AzureSlotDeploymentPassThroughData) taskChainResponse.getPassThroughData();
    assertThat(newPassThroughData.getUnprocessedConfigs()).isEmpty();
    assertThat(newPassThroughData.getConfigs()).containsKeys(APPLICATION_SETTINGS, CONNECTION_STRINGS);
  }

  @Test
  @Owner(developers = ABOSII)
  @Category(UnitTests.class)
  public void testExecuteNextLinkWithFetchPreDeploymentDataResponse() throws Exception {
    final StepElementParameters stepElementParameters = createTestStepElementParameters();
    final AzureSlotDeploymentPassThroughData passThroughData = AzureSlotDeploymentPassThroughData.builder()
                                                                   .configs(emptyMap())
                                                                   .unprocessedConfigs(emptyMap())
                                                                   .infrastructure(infrastructure)
                                                                   .build();
    final AzureAppServicePreDeploymentData preDeploymentData = AzureAppServicePreDeploymentData.builder().build();
    final AzureWebAppTaskResponse azureWebAppTaskResponse =
        AzureWebAppTaskResponse.builder()
            .requestResponse(
                AzureWebAppFetchPreDeploymentDataResponse.builder().preDeploymentData(preDeploymentData).build())
            .build();
    final ArgumentCaptor<TaskParameters> taskParametersArgumentCaptor = ArgumentCaptor.forClass(TaskParameters.class);
    final ArgumentCaptor<AzureWebAppPreDeploymentDataOutput> preDeploymentDataOutputArgumentCaptor =
        ArgumentCaptor.forClass(AzureWebAppPreDeploymentDataOutput.class);

    TaskChainResponse taskChainResponse = slotDeploymentStep.executeNextLinkWithSecurityContext(
        ambiance, stepElementParameters, stepInputPackage, passThroughData, () -> azureWebAppTaskResponse);

    verify(azureWebAppStepHelper)
        .prepareTaskRequest(eq(stepElementParameters), eq(ambiance), taskParametersArgumentCaptor.capture(),
            eq(TaskType.AZURE_WEB_APP_TASK_NG), anyList());
    verify(executionSweepingOutputService)
        .consume(eq(ambiance), eq(AzureWebAppPreDeploymentDataOutput.OUTPUT_NAME),
            preDeploymentDataOutputArgumentCaptor.capture(), eq(StepCategory.STEP.name()));

    assertThat(taskChainResponse.isChainEnd()).isTrue();
    assertThat(taskChainResponse.getTaskRequest()).isSameAs(taskRequest);
    assertThat(taskParametersArgumentCaptor.getValue()).isInstanceOf(AzureWebAppSlotDeploymentRequest.class);
    AzureWebAppSlotDeploymentRequest slotDeploymentRequest =
        (AzureWebAppSlotDeploymentRequest) taskParametersArgumentCaptor.getValue();
    assertThat(slotDeploymentRequest.getPreDeploymentData()).isSameAs(preDeploymentData);
    assertThat(slotDeploymentRequest.getArtifact()).isSameAs(azureArtifactConfig);
    assertThat(slotDeploymentRequest.getInfrastructure()).isSameAs(infraDelegateConfig);
    AzureWebAppPreDeploymentDataOutput preDeploymentDataOutput = preDeploymentDataOutputArgumentCaptor.getValue();
    assertThat(preDeploymentDataOutput.getPreDeploymentData()).isSameAs(preDeploymentData);
  }

  @Test
  @Owner(developers = ABOSII)
  @Category(UnitTests.class)
  public void testFinalizeExecution() throws Exception {
    final StepElementParameters stepElementParameters = createTestStepElementParameters();
    final String deploymentProgressMarker = "TestSlotDeployProgressMarker";
    final List<AzureAppDeploymentData> appDeploymentData =
        asList(AzureAppDeploymentData.builder().build(), AzureAppDeploymentData.builder().build());
    final List<UnitProgress> unitProgresses = singletonList(UnitProgress.newBuilder().build());
    final UnitProgressData unitProgressData = UnitProgressData.builder().unitProgresses(unitProgresses).build();
    final AzureWebAppTaskResponse azureWebAppTaskResponse =
        AzureWebAppTaskResponse.builder()
            .requestResponse(AzureWebAppSlotDeploymentResponse.builder()
                                 .deploymentProgressMarker(deploymentProgressMarker)
                                 .azureAppDeploymentData(appDeploymentData)
                                 .build())
            .commandUnitsProgress(unitProgressData)
            .build();
    final AzureSlotDeploymentPassThroughData passThroughData = AzureSlotDeploymentPassThroughData.builder().build();
    final ArgumentCaptor<AzureWebAppSlotDeploymentDataOutput> slotDeploymentDataOutputArgumentCaptor =
        ArgumentCaptor.forClass(AzureWebAppSlotDeploymentDataOutput.class);

    StepResponse stepResponse = slotDeploymentStep.finalizeExecutionWithSecurityContext(
        ambiance, stepElementParameters, passThroughData, () -> azureWebAppTaskResponse);

    verify(executionSweepingOutputService)
        .consume(eq(ambiance), eq(AzureWebAppSlotDeploymentDataOutput.OUTPUT_NAME),
            slotDeploymentDataOutputArgumentCaptor.capture(), eq(StepCategory.STEP.name()));

    assertThat(stepResponse.getStatus()).isEqualTo(Status.SUCCEEDED);
    assertThat(stepResponse.getUnitProgressList()).isEqualTo(unitProgresses);
    AzureWebAppSlotDeploymentDataOutput slotDeploymentDataOutput = slotDeploymentDataOutputArgumentCaptor.getValue();
    assertThat(slotDeploymentDataOutput.getDeploymentProgressMarker()).isEqualTo(deploymentProgressMarker);
  }

  private StepElementParameters createTestStepElementParameters() {
    return StepElementParameters.builder()
        .spec(AzureWebAppSlotDeploymentStepParameters.infoBuilder().build())
        .timeout(ParameterField.createValueField("10m"))
        .build();
  }

  private GitStoreConfig createTestGitStoreConfig() {
    return BitbucketStore.builder()
        .branch(ParameterField.createValueField("main"))
        .gitFetchType(FetchType.BRANCH)
        .paths(ParameterField.createValueField(singletonList("test")))
        .build();
  }

  private HarnessStore createTestHarnessStore() {
    return HarnessStore.builder().files(ParameterField.createValueField(singletonList("project:/test"))).build();
  }
}