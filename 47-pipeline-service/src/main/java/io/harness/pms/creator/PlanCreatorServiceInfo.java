package io.harness.pms.creator;

import io.harness.pms.plan.PlanCreationServiceGrpc.PlanCreationServiceBlockingStub;

import java.util.Map;
import java.util.Set;
import lombok.Value;

@Value
public class PlanCreatorServiceInfo {
  Map<String, Set<String>> supportedTypes;
  PlanCreationServiceBlockingStub planCreationClient;
}
