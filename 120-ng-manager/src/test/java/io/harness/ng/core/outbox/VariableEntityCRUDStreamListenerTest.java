package io.harness.ng.core.outbox;

import static io.harness.annotations.dev.HarnessTeam.PL;
import static io.harness.eventsframework.EventsFrameworkMetadataConstants.ORGANIZATION_ENTITY;
import static io.harness.eventsframework.EventsFrameworkMetadataConstants.PROJECT_ENTITY;
import static io.harness.rule.OwnerRule.VIKAS_M;

import static junit.framework.TestCase.assertEquals;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.harness.CategoryTest;
import io.harness.annotations.dev.OwnedBy;
import io.harness.category.element.UnitTests;
import io.harness.eventsframework.EventsFrameworkMetadataConstants;
import io.harness.eventsframework.consumer.Message;
import io.harness.eventsframework.entity_crud.organization.OrganizationEntityChangeDTO;
import io.harness.eventsframework.entity_crud.project.ProjectEntityChangeDTO;
import io.harness.ng.core.event.VariableEntityCRUDEventHandler;
import io.harness.ng.core.event.VariableEntityCRUDStreamListener;
import io.harness.rule.Owner;
import io.harness.utils.featureflaghelper.NGFeatureFlagHelperService;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.protobuf.ByteString;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;

@OwnedBy(PL)
public class VariableEntityCRUDStreamListenerTest extends CategoryTest {
  private VariableEntityCRUDEventHandler variableEntityCRUDEventHandler;
  @Inject @InjectMocks VariableEntityCRUDStreamListener variableEntityCRUDStreamListener;
  private NGFeatureFlagHelperService ngFeatureFlagHelperService;

  @Before
  public void setup() {
    variableEntityCRUDEventHandler = mock(VariableEntityCRUDEventHandler.class);
    ngFeatureFlagHelperService = mock(NGFeatureFlagHelperService.class);
    variableEntityCRUDStreamListener =
        spy(new VariableEntityCRUDStreamListener(variableEntityCRUDEventHandler, ngFeatureFlagHelperService));
    when(ngFeatureFlagHelperService.isEnabled(anyString(), any())).thenReturn(true);
  }

  @Test
  @Owner(developers = VIKAS_M)
  @Category(UnitTests.class)
  public void testOrganizationDeleteEvent() {
    String accountIdentifier = randomAlphabetic(10);
    String identifier = randomAlphabetic(10);
    Message message = Message.newBuilder()
                          .setMessage(io.harness.eventsframework.producer.Message.newBuilder()
                                          .putAllMetadata(ImmutableMap.of("accountId", accountIdentifier,
                                              EventsFrameworkMetadataConstants.ENTITY_TYPE, ORGANIZATION_ENTITY,
                                              EventsFrameworkMetadataConstants.ACTION,
                                              EventsFrameworkMetadataConstants.DELETE_ACTION))
                                          .setData(getOrganizationPayload(accountIdentifier, identifier))
                                          .build())
                          .build();
    final ArgumentCaptor<String> idCaptor = ArgumentCaptor.forClass(String.class);
    when(variableEntityCRUDEventHandler.deleteAssociatedVariables(any(), any(), any())).thenReturn(true);
    variableEntityCRUDStreamListener.handleMessage(message);
    verify(variableEntityCRUDEventHandler, times(1)).deleteAssociatedVariables(any(), idCaptor.capture(), any());
    assertEquals(idCaptor.getValue(), identifier);
  }

  @Test
  @Owner(developers = VIKAS_M)
  @Category(UnitTests.class)
  public void testProjectDeleteEvent() {
    String accountIdentifier = randomAlphabetic(10);
    String orgIdentifier = randomAlphabetic(10);
    String identifier = randomAlphabetic(10);
    Message message = Message.newBuilder()
                          .setMessage(io.harness.eventsframework.producer.Message.newBuilder()
                                          .putAllMetadata(ImmutableMap.of("accountId", accountIdentifier,
                                              EventsFrameworkMetadataConstants.ENTITY_TYPE, PROJECT_ENTITY,
                                              EventsFrameworkMetadataConstants.ACTION,
                                              EventsFrameworkMetadataConstants.DELETE_ACTION))
                                          .setData(getProjectPayload(accountIdentifier, orgIdentifier, identifier))
                                          .build())
                          .build();
    final ArgumentCaptor<String> idCaptor = ArgumentCaptor.forClass(String.class);
    when(variableEntityCRUDEventHandler.deleteAssociatedVariables(any(), any(), any())).thenReturn(true);
    variableEntityCRUDStreamListener.handleMessage(message);
    verify(variableEntityCRUDEventHandler, times(1)).deleteAssociatedVariables(any(), any(), idCaptor.capture());
    assertEquals(idCaptor.getValue(), identifier);
  }

  private ByteString getProjectPayload(String accountIdentifier, String orgIdentifier, String identifier) {
    return ProjectEntityChangeDTO.newBuilder()
        .setIdentifier(identifier)
        .setOrgIdentifier(orgIdentifier)
        .setAccountIdentifier(accountIdentifier)
        .build()
        .toByteString();
  }

  private ByteString getOrganizationPayload(String accountIdentifier, String identifier) {
    return OrganizationEntityChangeDTO.newBuilder()
        .setIdentifier(identifier)
        .setAccountIdentifier(accountIdentifier)
        .build()
        .toByteString();
  }
}