package io.harness.registrars;

import static io.harness.annotations.dev.HarnessTeam.CDC;

import io.harness.annotations.dev.OwnedBy;
import io.harness.registries.registrar.TimeoutRegistrar;
import io.harness.timeout.Dimension;
import io.harness.timeout.TimeoutTrackerFactory;
import io.harness.timeout.trackers.active.ActiveTimeoutTrackerFactory;

import com.google.inject.Inject;
import com.google.inject.Injector;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;

@OwnedBy(CDC)
public class OrchestrationBeansTimeoutRegistrar implements TimeoutRegistrar {
  @Inject private Injector injector;

  @Override
  public void register(Set<Pair<Dimension, TimeoutTrackerFactory<?>>> resolverClasses) {
    resolverClasses.add(
        Pair.of(ActiveTimeoutTrackerFactory.DIMENSION, injector.getInstance(ActiveTimeoutTrackerFactory.class)));
  }
}
