package io.harness.ng.core.event;

import static io.harness.annotations.dev.HarnessTeam.PL;
import static io.harness.beans.FeatureName.HARD_DELETE_VARIABLES;
import static io.harness.eventsframework.EventsFrameworkMetadataConstants.ACTION;
import static io.harness.eventsframework.EventsFrameworkMetadataConstants.CREATE_ACTION;
import static io.harness.eventsframework.EventsFrameworkMetadataConstants.DELETE_ACTION;
import static io.harness.eventsframework.EventsFrameworkMetadataConstants.ENTITY_TYPE;
import static io.harness.eventsframework.EventsFrameworkMetadataConstants.ORGANIZATION_ENTITY;
import static io.harness.eventsframework.EventsFrameworkMetadataConstants.PROJECT_ENTITY;
import static io.harness.eventsframework.EventsFrameworkMetadataConstants.RESTORE_ACTION;

import io.harness.annotations.dev.OwnedBy;
import io.harness.eventsframework.consumer.Message;
import io.harness.eventsframework.entity_crud.organization.OrganizationEntityChangeDTO;
import io.harness.eventsframework.entity_crud.project.ProjectEntityChangeDTO;
import io.harness.exception.InvalidRequestException;
import io.harness.utils.featureflaghelper.NGFeatureFlagHelperService;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@OwnedBy(PL)
@Slf4j
@Singleton
public class VariableEntityCRUDStreamListener implements MessageListener {
  private final VariableEntityCRUDEventHandler variableEntityCRUDEventHandler;
  private final NGFeatureFlagHelperService ngFeatureFlagHelperService;

  @Inject
  public VariableEntityCRUDStreamListener(VariableEntityCRUDEventHandler variableEntityCRUDEventHandler,
      NGFeatureFlagHelperService ngFeatureFlagHelperService) {
    this.variableEntityCRUDEventHandler = variableEntityCRUDEventHandler;
    this.ngFeatureFlagHelperService = ngFeatureFlagHelperService;
  }

  @Override
  public boolean handleMessage(Message message) {
    if (message != null && message.hasMessage()) {
      Map<String, String> metadataMap = message.getMessage().getMetadataMap();
      if (metadataMap != null && metadataMap.get(ENTITY_TYPE) != null) {
        String entityType = metadataMap.get(ENTITY_TYPE);
        switch (entityType) {
          case ORGANIZATION_ENTITY:
            return processOrganizationChangeEvent(message);
          case PROJECT_ENTITY:
            return processProjectChangeEvent(message);
          default:
        }
      }
    }
    return true;
  }

  private boolean processOrganizationChangeEvent(Message message) {
    OrganizationEntityChangeDTO organizationEntityChangeDTO;
    try {
      organizationEntityChangeDTO = OrganizationEntityChangeDTO.parseFrom(message.getMessage().getData());
    } catch (InvalidProtocolBufferException e) {
      throw new InvalidRequestException(
          String.format("Exception in unpacking EntityChangeDTO for key %s", message.getId()), e);
    }
    String action = message.getMessage().getMetadataMap().get(ACTION);
    if (action != null) {
      switch (action) {
        case CREATE_ACTION:
          return processOrganizationCreateEvent(organizationEntityChangeDTO);
        case DELETE_ACTION:
          return processOrganizationDeleteEvent(organizationEntityChangeDTO);
        case RESTORE_ACTION:
          return processOrganizationRestoreEvent(organizationEntityChangeDTO);
        default:
      }
    }
    return true;
  }

  private boolean processOrganizationCreateEvent(OrganizationEntityChangeDTO organizationEntityChangeDTO) {
    return true;
  }

  private boolean processOrganizationDeleteEvent(OrganizationEntityChangeDTO organizationEntityChangeDTO) {
    if (!ngFeatureFlagHelperService.isEnabled(
            organizationEntityChangeDTO.getAccountIdentifier(), HARD_DELETE_VARIABLES)) {
      return true;
    }
    return variableEntityCRUDEventHandler.deleteAssociatedVariables(
        organizationEntityChangeDTO.getAccountIdentifier(), organizationEntityChangeDTO.getIdentifier(), null);
  }

  private boolean processOrganizationRestoreEvent(OrganizationEntityChangeDTO organizationEntityChangeDTO) {
    return true;
  }

  private boolean processProjectChangeEvent(Message message) {
    ProjectEntityChangeDTO projectEntityChangeDTO;
    try {
      projectEntityChangeDTO = ProjectEntityChangeDTO.parseFrom(message.getMessage().getData());
    } catch (InvalidProtocolBufferException e) {
      throw new InvalidRequestException(
          String.format("Exception in unpacking ProjectEntityChangeDTO for key %s", message.getId()), e);
    }
    String action = message.getMessage().getMetadataMap().get(ACTION);
    if (action != null) {
      switch (action) {
        case CREATE_ACTION:
          return processProjectCreateEvent(projectEntityChangeDTO);
        case DELETE_ACTION:
          return processProjectDeleteEvent(projectEntityChangeDTO);
        case RESTORE_ACTION:
          return processProjectRestoreEvent(projectEntityChangeDTO);
        default:
      }
    }
    return true;
  }

  private boolean processProjectCreateEvent(ProjectEntityChangeDTO projectEntityChangeDTO) {
    return true;
  }

  private boolean processProjectDeleteEvent(ProjectEntityChangeDTO projectEntityChangeDTO) {
    if (!ngFeatureFlagHelperService.isEnabled(projectEntityChangeDTO.getAccountIdentifier(), HARD_DELETE_VARIABLES)) {
      return true;
    }
    return variableEntityCRUDEventHandler.deleteAssociatedVariables(projectEntityChangeDTO.getAccountIdentifier(),
        projectEntityChangeDTO.getOrgIdentifier(), projectEntityChangeDTO.getIdentifier());
  }

  private boolean processProjectRestoreEvent(ProjectEntityChangeDTO projectEntityChangeDTO) {
    return true;
  }
}