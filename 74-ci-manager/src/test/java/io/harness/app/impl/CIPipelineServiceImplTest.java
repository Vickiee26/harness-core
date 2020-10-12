package io.harness.app.impl;

import static io.harness.rule.OwnerRule.ALEKSANDAR;
import static io.harness.rule.OwnerRule.SHUBHAM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.inject.Inject;

import io.harness.app.beans.dto.CIPipelineFilterDTO;
import io.harness.beans.ParameterField;
import io.harness.beans.stages.IntegrationStage;
import io.harness.category.element.UnitTests;
import io.harness.cdng.pipeline.NgPipeline;
import io.harness.cdng.pipeline.beans.entities.NgPipelineEntity;
import io.harness.cdng.pipeline.service.NGPipelineService;
import io.harness.ngpipeline.pipeline.repository.PipelineRepository;
import io.harness.rule.Owner;
import io.harness.yaml.core.ExecutionElement;
import io.harness.yaml.core.StageElement;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;

public class CIPipelineServiceImplTest extends CIManagerTest {
  @Mock private PipelineRepository pipelineRepository;
  @InjectMocks @Inject NGPipelineService ngPipelineService;
  private final String ACCOUNT_ID = "ACCOUNT_ID";
  private final String ORG_ID = "ORG_ID";
  private final String PROJECT_ID = "PROJECT_ID";
  private final String TAG = "foo";
  private String inputYaml;
  private NgPipelineEntity pipeline;

  @Before
  public void setUp() {
    inputYaml = new Scanner(
        Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream("pipeline.yml")), "UTF-8")
                    .useDelimiter("\\A")
                    .next();

    pipeline =
        NgPipelineEntity.builder()
            .identifier("testIdentifier")
            .ngPipeline(NgPipeline.builder().description(ParameterField.createValueField("testDescription")).build())
            .uuid("testUUID")
            .build();
  }

  private CIPipelineFilterDTO getPipelineFilter() {
    return CIPipelineFilterDTO.builder()
        .accountIdentifier(ACCOUNT_ID)
        .orgIdentifier(ORG_ID)
        .projectIdentifier(PROJECT_ID)
        .tags(Arrays.asList(TAG))
        .build();
  }

  @Test
  @Owner(developers = ALEKSANDAR)
  @Category(UnitTests.class)
  public void createPipelineFromYAML() {
    ArgumentCaptor<NgPipelineEntity> pipelineCaptor = ArgumentCaptor.forClass(NgPipelineEntity.class);
    when(pipelineRepository.save(any(NgPipelineEntity.class))).thenReturn(pipeline);
    when(pipelineRepository.findById("testId")).thenReturn(Optional.ofNullable(pipeline));

    ngPipelineService.createPipeline(inputYaml, ACCOUNT_ID, ORG_ID, PROJECT_ID);

    verify(pipelineRepository).save(pipelineCaptor.capture());
    NgPipelineEntity ngPipelineEntity = pipelineCaptor.getValue();
    assertThat(ngPipelineEntity).isNotNull();
    assertThat(ngPipelineEntity.getIdentifier()).isEqualTo("cipipeline");

    assertThat(ngPipelineEntity.getNgPipeline().getStages()).hasSize(1);
    assertThat(ngPipelineEntity.getNgPipeline().getStages().get(0)).isInstanceOf(StageElement.class);
    StageElement stageElement = (StageElement) ngPipelineEntity.getNgPipeline().getStages().get(0);

    IntegrationStage integrationStage = (IntegrationStage) stageElement.getStageType();
    assertThat(integrationStage.getIdentifier()).isEqualTo("masterBuildUpload");
    assertThat(integrationStage.getGitConnector()).isNotNull();
    assertThat(integrationStage.getInfrastructure()).isNotNull();
    assertThat(integrationStage.getContainer()).isNotNull();
    assertThat(integrationStage.getCustomVariables()).isNotNull();

    ExecutionElement execution = integrationStage.getExecution();
    assertThat(execution).isNotNull();
    assertThat(execution.getSteps()).hasSize(5);
  }

  @Test
  @Owner(developers = ALEKSANDAR)
  @Category(UnitTests.class)
  public void createPipeline() {
    ArgumentCaptor<NgPipelineEntity> pipelineCaptor = ArgumentCaptor.forClass(NgPipelineEntity.class);
    when(pipelineRepository.save(any(NgPipelineEntity.class))).thenReturn(pipeline);
    when(pipelineRepository.findById("testId")).thenReturn(Optional.ofNullable(pipeline));

    ngPipelineService.createPipeline(inputYaml, ACCOUNT_ID, ORG_ID, PROJECT_ID);

    verify(pipelineRepository).save(pipelineCaptor.capture());
    NgPipelineEntity pipelineEntity = pipelineCaptor.getValue();
    assertThat(pipelineEntity.getIdentifier()).isEqualTo("cipipeline");
    assertThat(pipelineEntity.getNgPipeline().getDescription().getValue()).isEqualTo("testDescription");
  }

  @Test
  @Owner(developers = ALEKSANDAR)
  @Category(UnitTests.class)
  public void readPipeline() {
    when(pipelineRepository.findByAccountIdAndOrgIdentifierAndProjectIdentifierAndIdentifierAndDeletedNot(
             ACCOUNT_ID, ORG_ID, PROJECT_ID, "testId", true))
        .thenReturn(Optional.ofNullable(pipeline));

    NgPipelineEntity ngPipelineEntity = ngPipelineService.getPipeline("testId", ACCOUNT_ID, ORG_ID, PROJECT_ID);

    assertThat(ngPipelineEntity.getIdentifier()).isEqualTo("testIdentifier");
    assertThat(ngPipelineEntity.getNgPipeline().getDescription().getValue()).isEqualTo("testDescription");
  }

  @Test
  @Owner(developers = SHUBHAM)
  @Category(UnitTests.class)
  public void getPipelines() {
    CIPipelineFilterDTO ciPipelineFilterDTO = getPipelineFilter();
    when(pipelineRepository.findAll(any(), any())).thenReturn(new PageImpl<>(Arrays.asList(pipeline)));

    List<NgPipelineEntity> pipelineEntities =
        ngPipelineService
            .listPipelines(ciPipelineFilterDTO.getAccountIdentifier(), ciPipelineFilterDTO.getOrgIdentifier(),
                ciPipelineFilterDTO.getProjectIdentifier(), new Criteria(), Pageable.unpaged(), null)
            .getContent();
    assertThat(pipelineEntities).isNotEmpty();

    NgPipelineEntity ngPipelineEntity = pipelineEntities.get(0);
    assertThat(ngPipelineEntity.getIdentifier()).isEqualTo("testIdentifier");
    assertThat(ngPipelineEntity.getNgPipeline().getDescription().getValue()).isEqualTo("testDescription");
    assertThat(ngPipelineEntity.getUuid()).isEqualTo("testUUID");
  }
}