package software.wings.graphql.datafetcher.cloudProvider;

import io.harness.ccm.config.CloudCostAware;
import io.harness.exception.InvalidRequestException;
import lombok.experimental.UtilityClass;
import software.wings.beans.SettingAttribute;
import software.wings.graphql.datafetcher.user.UserController;
import software.wings.graphql.schema.type.QLCloudProviderType;
import software.wings.graphql.schema.type.cloudProvider.QLAwsCloudProvider;
import software.wings.graphql.schema.type.cloudProvider.QLAzureCloudProvider;
import software.wings.graphql.schema.type.cloudProvider.QLCloudProviderBuilder;
import software.wings.graphql.schema.type.cloudProvider.QLGcpCloudProvider;
import software.wings.graphql.schema.type.cloudProvider.QLKubernetesClusterCloudProvider;
import software.wings.graphql.schema.type.cloudProvider.QLPcfCloudProvider;
import software.wings.graphql.schema.type.cloudProvider.QLPhysicalDataCenterCloudProvider;
import software.wings.graphql.schema.type.cloudProvider.QLSpotInstCloudProvider;

@UtilityClass
public class CloudProviderController {
  public static QLCloudProviderBuilder populateCloudProvider(SettingAttribute settingAttribute) {
    return getCloudProviderBuilder(settingAttribute)
        .id(settingAttribute.getUuid())
        .name(settingAttribute.getName())
        .createdAt(settingAttribute.getCreatedAt())
        .createdBy(UserController.populateUser(settingAttribute.getCreatedBy()))
        .type(settingAttribute.getValue().getType())
        .isContinuousEfficiencyEnabled(getCloudCostEnabledBoolean(settingAttribute));
  }

  private static QLCloudProviderBuilder getCloudProviderBuilder(SettingAttribute settingAttribute) {
    QLCloudProviderBuilder cloudProviderBuilder;
    switch (settingAttribute.getValue().getSettingType()) {
      case AZURE:
        cloudProviderBuilder = QLAzureCloudProvider.builder();
        break;
      case PHYSICAL_DATA_CENTER:
        cloudProviderBuilder = QLPhysicalDataCenterCloudProvider.builder();
        break;
      case AWS:
        cloudProviderBuilder = QLAwsCloudProvider.builder();
        break;
      case GCP:
        cloudProviderBuilder = QLGcpCloudProvider.builder();
        break;
      case KUBERNETES:
      case KUBERNETES_CLUSTER:
        cloudProviderBuilder = QLKubernetesClusterCloudProvider.builder();
        break;
      case PCF:
        cloudProviderBuilder = QLPcfCloudProvider.builder();
        break;
      case SPOT_INST:
        cloudProviderBuilder = QLSpotInstCloudProvider.builder();
        break;
      default:
        throw new InvalidRequestException("Unknown Cloud ProviderType " + settingAttribute.getValue().getSettingType());
    }

    return cloudProviderBuilder;
  }

  public static boolean getCloudCostEnabledBoolean(SettingAttribute settingAttribute) {
    if (settingAttribute.getValue() instanceof CloudCostAware
        && ((CloudCostAware) settingAttribute.getValue()).getCcmConfig() != null) {
      return ((CloudCostAware) settingAttribute.getValue()).getCcmConfig().isCloudCostEnabled();
    }
    return false;
  }

  public static void checkIfInputIsNotPresent(QLCloudProviderType type, Object input) {
    if (input == null) {
      throw new InvalidRequestException(
          String.format("No input provided with the request for %s cloud provider", type.getStringValue()));
    }
  }
}
