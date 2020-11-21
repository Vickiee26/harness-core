package software.wings.service.impl.artifactstream;

import static io.harness.rule.OwnerRule.ANUBHAW;
import static io.harness.rule.OwnerRule.DEEPAK_PUTHRAYA;

import static software.wings.beans.Application.Builder.anApplication;
import static software.wings.utils.WingsTestConstants.ACCOUNT_ID;
import static software.wings.utils.WingsTestConstants.APP_ID;
import static software.wings.utils.WingsTestConstants.SERVICE_ID;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import io.harness.category.element.UnitTests;
import io.harness.persistence.HPersistence;
import io.harness.rule.Owner;

import software.wings.WingsBaseTest;
import software.wings.beans.Service;
import software.wings.beans.artifact.ArtifactStream;
import software.wings.beans.artifact.JenkinsArtifactStream;
import software.wings.scheduler.BackgroundJobScheduler;
import software.wings.service.intfc.ArtifactStreamService;
import software.wings.service.intfc.ServiceResourceService;
import software.wings.service.intfc.yaml.YamlDirectoryService;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.InjectMocks;
import org.mockito.Mock;

/**
 * Created by peeyushaggarwal on 5/4/16.
 */
public class ArtifactStreamResourceServiceTest extends WingsBaseTest {
  private static final JenkinsArtifactStream artifactStream = JenkinsArtifactStream.builder()
                                                                  .appId(APP_ID)
                                                                  .name("     Jenkins artifact    ")
                                                                  .sourceName("job1")
                                                                  .jobname("job1")
                                                                  .settingId("JENKINS_SETTING_ID")
                                                                  .serviceId(SERVICE_ID)
                                                                  .artifactPaths(asList("dist/svr-*.war"))
                                                                  .metadataOnly(true)
                                                                  .build();

  @Inject HPersistence persistence;

  @Mock private BackgroundJobScheduler jobScheduler;
  @Mock private YamlDirectoryService yamlDirectoryService;
  @Mock private ServiceResourceService serviceResourceService;

  @InjectMocks @Inject private ArtifactStreamService artifactStreamService;

  /**
   * setup for test.
   */
  @Before
  public void setUp() {
    persistence.save(anApplication().uuid(APP_ID).accountId(ACCOUNT_ID).build());
    persistence.save(Service.builder().uuid(SERVICE_ID).appId(APP_ID).build());
  }

  /**
   * Should create artifact stream.
   */
  @Test
  @Owner(developers = {ANUBHAW, DEEPAK_PUTHRAYA})
  @Category(UnitTests.class)
  public void shouldCreateArtifactStream() {
    ArtifactStream stream = artifactStreamService.create(artifactStream);
    assertThat(stream).isNotNull();
    // Checking that the name has been saved after Normalization
    assertThat(stream.getName()).isEqualTo("Jenkins artifact");
  }

  /**
   * Should list all artifact streams.
   */
  @Test
  @Owner(developers = ANUBHAW)
  @Category(UnitTests.class)
  public void shouldListAllArtifactStreams() {
    List<ArtifactStream> artifactStreams = Lists.newArrayList();
    artifactStreams.add(artifactStreamService.create(artifactStream));
    when(serviceResourceService.findServicesByApp(APP_ID))
        .thenReturn(asList(Service.builder().artifactStreamIds(asList(artifactStreams.get(0).getUuid())).build()));
    assertThat(artifactStreamService.listByAppId(artifactStream.getAppId())).hasSameElementsAs(artifactStreams);
  }

  /**
   * Should delete artifact stream.
   */
  @Test
  @Owner(developers = ANUBHAW)
  @Category(UnitTests.class)
  public void shouldDeleteArtifactStream() {
    ArtifactStream dbArtifactStream = artifactStreamService.create(artifactStream);
    artifactStreamService.deleteWithBinding(dbArtifactStream.getAppId(), dbArtifactStream.getUuid(), false, false);
    assertThat(artifactStreamService.listByAppId(artifactStream.getAppId())).hasSize(0);
  }
}
