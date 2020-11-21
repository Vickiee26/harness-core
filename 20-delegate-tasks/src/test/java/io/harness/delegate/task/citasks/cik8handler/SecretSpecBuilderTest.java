package io.harness.delegate.task.citasks.cik8handler;

import static io.harness.data.encoding.EncodingUtils.encodeBase64;
import static io.harness.delegate.beans.ci.pod.SecretParams.Type.TEXT;
import static io.harness.delegate.task.citasks.cik8handler.SecretSpecBuilder.SECRET_KEY;
import static io.harness.rule.OwnerRule.ALEKSANDAR;
import static io.harness.rule.OwnerRule.HARSH;
import static io.harness.rule.OwnerRule.SHUBHAM;

import static java.util.Collections.singletonList;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import io.harness.CategoryTest;
import io.harness.category.element.UnitTests;
import io.harness.delegate.beans.ci.pod.ConnectorDetails;
import io.harness.delegate.beans.ci.pod.ImageDetailsWithConnector;
import io.harness.delegate.beans.ci.pod.SecretParams;
import io.harness.delegate.beans.ci.pod.SecretVariableDTO;
import io.harness.delegate.beans.ci.pod.SecretVariableDetails;
import io.harness.delegate.beans.connector.ConnectorConfigDTO;
import io.harness.delegate.beans.connector.ConnectorType;
import io.harness.delegate.beans.connector.docker.DockerAuthType;
import io.harness.delegate.beans.connector.docker.DockerAuthenticationDTO;
import io.harness.delegate.beans.connector.docker.DockerConnectorDTO;
import io.harness.delegate.beans.connector.docker.DockerUserNamePasswordDTO;
import io.harness.delegate.beans.connector.gitconnector.GitAuthType;
import io.harness.delegate.beans.connector.gitconnector.GitConfigDTO;
import io.harness.delegate.beans.connector.gitconnector.GitConnectionType;
import io.harness.delegate.beans.connector.gitconnector.GitHTTPAuthenticationDTO;
import io.harness.delegate.beans.connector.gitconnector.GitSSHAuthenticationDTO;
import io.harness.encryption.Scope;
import io.harness.encryption.SecretRefData;
import io.harness.exception.InvalidArgumentsException;
import io.harness.k8s.model.ImageDetails;
import io.harness.rule.Owner;
import io.harness.security.encryption.EncryptedDataDetail;
import io.harness.security.encryption.EncryptedRecordData;
import io.harness.security.encryption.EncryptionType;
import io.harness.security.encryption.SecretDecryptionService;

import io.fabric8.kubernetes.api.model.Secret;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class SecretSpecBuilderTest extends CategoryTest {
  @Mock private SecretDecryptionService secretDecryptionService;

  @InjectMocks private SecretSpecBuilder secretSpecBuilder;

  private static final String imageName = "IMAGE";
  private static final String tag = "TAG";
  private static final String namespace = "default";
  private static final String podName = "pod";
  private static final String containerName = "container";
  private static final String registryUrl = "https://index.docker.io/v1/";
  private static final String registrySecretName = "hs-index-docker-io-v1-username-hs";
  private static final String userName = "usr";
  private static final String password = "pwd";
  private static final String gitRepoUrl = "https://github.com/wings-software/portal.git";
  private static final String gitSecretName = "hs-wings-software-portal-hs";
  private static final String sshSettingId = "setting-id";
  private static final String encryptedKey = "encryptedKey";
  private static final String passwordRefId = "git_password";

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  private ConnectorDetails getConnectorDetails(ConnectorConfigDTO config, ConnectorType type) {
    return ConnectorDetails.builder()
        .connectorType(type)
        .connectorConfig(config)
        .encryptedDataDetails(new ArrayList<>())
        .build();
  }

  private GitConfigDTO getGitConfigWithSshKeys() {
    GitSSHAuthenticationDTO gitSSHAuthenticationDTO =
        GitSSHAuthenticationDTO.builder().encryptedSshKey(encryptedKey).build();
    return GitConfigDTO.builder()
        .url(gitRepoUrl)
        .branchName("master")
        .gitAuth(gitSSHAuthenticationDTO)
        .gitAuthType(GitAuthType.SSH)
        .gitConnectionType(GitConnectionType.REPO)
        .build();
  }

  private GitConfigDTO getGitConfigWithHttpKeys() {
    GitHTTPAuthenticationDTO gitHTTPAuthenticationDTO =
        GitHTTPAuthenticationDTO.builder()
            .username(userName)
            .passwordRef(
                SecretRefData.builder().identifier(passwordRefId).decryptedValue(password.toCharArray()).build())
            .build();
    return GitConfigDTO.builder()
        .url(gitRepoUrl)
        .branchName("master")
        .gitAuth(gitHTTPAuthenticationDTO)
        .gitAuthType(GitAuthType.HTTP)
        .gitConnectionType(GitConnectionType.REPO)
        .build();
  }

  @Test
  @Owner(developers = HARSH)
  @Category(UnitTests.class)
  public void shouldConvertCustomSecretTextVariables() throws IOException {
    SecretVariableDetails secretVariableDetails =
        SecretVariableDetails.builder()
            .secretVariableDTO(SecretVariableDTO.builder()
                                   .name("abc")
                                   .type(SecretVariableDTO.Type.TEXT)
                                   .secret(SecretRefData.builder()
                                               .decryptedValue("pass".toCharArray())
                                               .identifier("secret_id")
                                               .scope(Scope.ACCOUNT)
                                               .build())
                                   .build())
            .encryptedDataDetailList(singletonList(
                EncryptedDataDetail.builder()
                    .encryptedData(EncryptedRecordData.builder().encryptionType(EncryptionType.KMS).build())
                    .build()))
            .build();
    when(secretDecryptionService.decrypt(
             secretVariableDetails.getSecretVariableDTO(), secretVariableDetails.getEncryptedDataDetailList()))
        .thenReturn(secretVariableDetails.getSecretVariableDTO());
    Map<String, SecretParams> decryptedSecrets =
        secretSpecBuilder.decryptCustomSecretVariables(singletonList(secretVariableDetails));
    assertThat(decryptedSecrets.get("abc").getValue()).isEqualTo(encodeBase64("pass"));
    assertThat(decryptedSecrets.get("abc").getSecretKey()).isEqualTo(SECRET_KEY + "abc");
    assertThat(decryptedSecrets.get("abc").getType()).isEqualTo(TEXT);
  }

  @Test
  @Owner(developers = HARSH)
  @Category(UnitTests.class)
  public void shouldConvertCustomSecretFile() throws IOException {
    SecretVariableDetails secretVariableDetails =
        SecretVariableDetails.builder()
            .secretVariableDTO(SecretVariableDTO.builder()
                                   .name("abc")
                                   .type(SecretVariableDTO.Type.FILE)
                                   .secret(SecretRefData.builder()
                                               .decryptedValue("pass".toCharArray())
                                               .identifier("secret_id")
                                               .scope(Scope.ACCOUNT)
                                               .build())
                                   .build())
            .encryptedDataDetailList(singletonList(
                EncryptedDataDetail.builder()
                    .encryptedData(EncryptedRecordData.builder().encryptionType(EncryptionType.KMS).build())
                    .build()))
            .build();
    when(secretDecryptionService.decrypt(
             secretVariableDetails.getSecretVariableDTO(), secretVariableDetails.getEncryptedDataDetailList()))
        .thenReturn(secretVariableDetails.getSecretVariableDTO());
    Map<String, SecretParams> decryptedSecrets =
        secretSpecBuilder.decryptCustomSecretVariables(singletonList(secretVariableDetails));
    assertThat(decryptedSecrets.get("abc").getValue()).isEqualTo(encodeBase64("pass"));
    assertThat(decryptedSecrets.get("abc").getSecretKey()).isEqualTo(SECRET_KEY + "abc");
    assertThat(decryptedSecrets.get("abc").getType()).isEqualTo(SecretParams.Type.FILE);
  }

  @Test
  @Owner(developers = SHUBHAM)
  @Category(UnitTests.class)
  public void getRegistrySecretSpecWithEmptyCred() {
    ImageDetails imageDetails1 = ImageDetails.builder().name(imageName).tag(tag).build();
    ImageDetailsWithConnector imageDetailsWithConnector1 =
        ImageDetailsWithConnector.builder().imageDetails(imageDetails1).build();
    assertNull(secretSpecBuilder.getRegistrySecretSpec(imageDetailsWithConnector1, namespace));

    ImageDetails imageDetails2 = ImageDetails.builder().name(imageName).tag(tag).registryUrl(registryUrl).build();
    ImageDetailsWithConnector imageDetailsWithConnector2 =
        ImageDetailsWithConnector.builder().imageDetails(imageDetails2).build();
    assertNull(secretSpecBuilder.getRegistrySecretSpec(imageDetailsWithConnector2, namespace));

    ImageDetails imageDetails3 =
        ImageDetails.builder().name(imageName).tag(tag).registryUrl(registryUrl).username(userName).build();
    ImageDetailsWithConnector imageDetailsWithConnector3 =
        ImageDetailsWithConnector.builder().imageDetails(imageDetails3).build();
    assertNull(secretSpecBuilder.getRegistrySecretSpec(imageDetailsWithConnector3, namespace));

    ImageDetails imageDetails4 =
        ImageDetails.builder().name(imageName).tag(tag).registryUrl(registryUrl).password(password).build();
    ImageDetailsWithConnector imageDetailsWithConnector4 =
        ImageDetailsWithConnector.builder().imageDetails(imageDetails4).build();
    assertNull(secretSpecBuilder.getRegistrySecretSpec(imageDetailsWithConnector4, namespace));
  }

  @Test
  @Owner(developers = SHUBHAM)
  @Category(UnitTests.class)
  public void getRegistrySecretSpecWithCred() {
    DockerUserNamePasswordDTO dockerUserNamePasswordDTO =
        DockerUserNamePasswordDTO.builder()
            .username("username")
            .passwordRef(SecretRefData.builder().decryptedValue("password".toCharArray()).build())
            .build();

    DockerConnectorDTO dockerConnectorDTO =
        DockerConnectorDTO.builder()
            .dockerRegistryUrl("https://index.docker.io/v1/")
            .auth(DockerAuthenticationDTO.builder()
                      .authType(DockerAuthType.USER_PASSWORD)
                      .credentials(DockerUserNamePasswordDTO.builder().username("username").build())
                      .build())
            .build();
    ConnectorDetails connectorDetails = getConnectorDetails(dockerConnectorDTO, ConnectorType.DOCKER);

    ImageDetails imageDetails = ImageDetails.builder()
                                    .name(imageName)
                                    .tag(tag)
                                    .registryUrl(registryUrl)
                                    .username(userName)
                                    .password(password)
                                    .build();

    when(secretDecryptionService.decrypt(eq(DockerUserNamePasswordDTO.builder().username("username").build()),
             eq(connectorDetails.getEncryptedDataDetails())))
        .thenReturn(dockerUserNamePasswordDTO);
    ImageDetailsWithConnector imageDetailsWithConnector =
        ImageDetailsWithConnector.builder().imageDetails(imageDetails).imageConnectorDetails(connectorDetails).build();

    Secret secret = secretSpecBuilder.getRegistrySecretSpec(imageDetailsWithConnector, namespace);
    assertEquals(registrySecretName, secret.getMetadata().getName());
    assertEquals(namespace, secret.getMetadata().getNamespace());
  }

  @Test()
  @Owner(developers = SHUBHAM)
  @Category(UnitTests.class)
  public void getGitSecretSpecWithEmptyCred() throws UnsupportedEncodingException {
    when(secretDecryptionService.decrypt(any(), any())).thenReturn(null);
    assertThatThrownBy(
        ()
            -> secretSpecBuilder.getGitSecretSpec(
                ConnectorDetails.builder().connectorConfig(GitConfigDTO.builder().build()).build(), namespace),
        null)
        .isInstanceOf(InvalidArgumentsException.class);
  }

  @Test
  @Owner(developers = SHUBHAM)
  @Category(UnitTests.class)
  public void getGitSecretSpecWithHttpKeys() throws UnsupportedEncodingException {
    GitConfigDTO gitConfig = getGitConfigWithHttpKeys();
    ConnectorDetails connectorDetails = getConnectorDetails(gitConfig, ConnectorType.GIT);
    when(secretDecryptionService.decrypt(gitConfig.getGitAuth(), connectorDetails.getEncryptedDataDetails()))
        .thenReturn(gitConfig.getGitAuth());

    Secret secret = secretSpecBuilder.getGitSecretSpec(connectorDetails, namespace);
    assertEquals(gitSecretName, secret.getMetadata().getName());
    assertEquals(namespace, secret.getMetadata().getNamespace());
  }

  @Test
  @Owner(developers = SHUBHAM)
  @Category(UnitTests.class)
  public void getGitSecretSpecWithSshKeys() throws UnsupportedEncodingException {
    GitConfigDTO gitConfig = getGitConfigWithSshKeys();
    ConnectorDetails connectorDetails = getConnectorDetails(gitConfig, ConnectorType.GIT);

    when(secretDecryptionService.decrypt(gitConfig.getGitAuth(), connectorDetails.getEncryptedDataDetails()))
        .thenReturn(gitConfig.getGitAuth());

    Secret secret = secretSpecBuilder.getGitSecretSpec(connectorDetails, namespace);
    assertEquals(gitSecretName, secret.getMetadata().getName());
    assertEquals(namespace, secret.getMetadata().getNamespace());
  }

  @Test()
  @Owner(developers = ALEKSANDAR)
  @Category(UnitTests.class)
  public void shouldDecryptDockerConfig() {
    Map<String, ConnectorDetails> map = new HashMap<>();
    DockerUserNamePasswordDTO decryptableEntity =
        DockerUserNamePasswordDTO.builder()
            .username("username")
            .passwordRef(SecretRefData.builder().decryptedValue("password".toCharArray()).build())
            .build();
    ConnectorDetails setting =
        ConnectorDetails.builder()
            .connectorType(ConnectorType.DOCKER)
            .identifier("docker")
            .connectorConfig(
                DockerConnectorDTO.builder()
                    .dockerRegistryUrl("https://index.docker.io/v1/")
                    .auth(DockerAuthenticationDTO.builder()
                              .authType(DockerAuthType.USER_PASSWORD)
                              .credentials(DockerUserNamePasswordDTO.builder().username("username").build())
                              .build())
                    .build())
            .build();
    map.put("docker", setting);
    when(secretDecryptionService.decrypt(any(), any())).thenReturn(decryptableEntity);
    Map<String, String> data = secretSpecBuilder.decryptPublishArtifactSecretVariables(map).values().stream().collect(
        Collectors.toMap(SecretParams::getSecretKey, SecretParams::getValue));
    assertThat(data)
        .containsKeys("USERNAME_docker", "PASSWORD_docker", "ENDPOINT_docker")
        .containsEntry("USERNAME_docker", encodeBase64("username"))
        .containsEntry("PASSWORD_docker", encodeBase64("password"))
        .containsEntry("ENDPOINT_docker", encodeBase64("https://index.docker.io/v1/"));
  }

  @Test()
  @Owner(developers = ALEKSANDAR)
  @Category(UnitTests.class)
  public void shouldCreateSecret() {
    Map<String, String> map = new HashMap<>();
    map.put("secret", "secret");
    Secret secret = secretSpecBuilder.createSecret("name", "namespace", map);
    assertThat(secret.getData()).isEqualTo(map);
    assertThat(secret.getMetadata().getName()).isEqualTo("name");
    assertThat(secret.getMetadata().getNamespace()).isEqualTo("namespace");
  }
}
