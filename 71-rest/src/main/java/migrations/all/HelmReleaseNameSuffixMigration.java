package migrations.all;

import static io.harness.beans.PageRequest.PageRequestBuilder.aPageRequest;
import static io.harness.beans.PageRequest.UNLIMITED;
import static io.harness.beans.SearchFilter.Operator.EQ;

import static software.wings.sm.StateType.HELM_DEPLOY;

import io.harness.persistence.HIterator;

import software.wings.beans.Application;
import software.wings.beans.CanaryOrchestrationWorkflow;
import software.wings.beans.GraphNode;
import software.wings.beans.PhaseStep;
import software.wings.beans.Workflow;
import software.wings.beans.WorkflowPhase;
import software.wings.dl.WingsPersistence;
import software.wings.service.intfc.WorkflowService;
import software.wings.sm.StateType;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import migrations.Migration;
@Slf4j
public class HelmReleaseNameSuffixMigration implements Migration {
  // This migration appends "harness and ${infra.helm.shortId} to the release name prefix".
  // After this migration release prefix will technically become release name.
  // Example:- previous release name prefix "abc-def" with  will become "abc-def-harness-${infra.helm.shortId}"
  // ${infra.helm.shortId} will be evaluated at runtime and will have first 7 characters of the infra mapping Id

  private static final String HELM_RELEASE_NAME_PREFIX_KEY = "helmReleaseNamePrefix";
  private static final String HELM_RELEASE_NAME_SUFFIX_VALUE = "-harness-${infra.helm.shortId}";

  @Inject private WingsPersistence wingsPersistence;
  @Inject private WorkflowService workflowService;

  @Override
  public void migrate() {
    log.info("Retrieving applications");

    try (HIterator<Application> apps = new HIterator<>(wingsPersistence.createQuery(Application.class).fetch())) {
      for (Application application : apps) {
        log.info("Updating app {}", application.getUuid());
        List<Workflow> workflows =
            workflowService
                .listWorkflows(
                    aPageRequest().withLimit(UNLIMITED).addFilter("appId", EQ, application.getUuid()).build())
                .getResponse();

        for (Workflow workflow : workflows) {
          updateWorkflowsWithHelmReleaseNamePrefix(workflow, HELM_DEPLOY);
        }

        log.info("Completed updating app {}", application.getUuid());
      }
    }

    log.info("Updated all apps");
    log.info("Finished running HelmReleaseNameSuffixMigration");
  }

  private void updateWorkflowsWithHelmReleaseNamePrefix(Workflow workflow, StateType stateType) {
    boolean workflowModified = false;

    if (workflow.getOrchestrationWorkflow() instanceof CanaryOrchestrationWorkflow) {
      CanaryOrchestrationWorkflow coWorkflow = (CanaryOrchestrationWorkflow) workflow.getOrchestrationWorkflow();

      if (coWorkflow.getWorkflowPhases() == null) {
        return;
      }

      for (WorkflowPhase workflowPhase : coWorkflow.getWorkflowPhases()) {
        List<WorkflowPhase> workflowPhases = new ArrayList<>();
        workflowPhases.add(workflowPhase);

        WorkflowPhase rollbackPhase = coWorkflow.getRollbackWorkflowPhaseIdMap().get(workflowPhase.getUuid());
        if (rollbackPhase != null) {
          workflowPhases.add(rollbackPhase);
        }

        for (WorkflowPhase phase : workflowPhases) {
          for (PhaseStep phaseStep : phase.getPhaseSteps()) {
            for (GraphNode node : phaseStep.getSteps()) {
              if (stateType.name().equals(node.getType())) {
                Map<String, Object> properties = node.getProperties();
                if (properties != null && properties.containsKey(HELM_RELEASE_NAME_PREFIX_KEY)) {
                  String releaseNamePrefix = (String) properties.get(HELM_RELEASE_NAME_PREFIX_KEY);

                  if (!releaseNamePrefix.contains(HELM_RELEASE_NAME_SUFFIX_VALUE)) {
                    workflowModified = true;
                    releaseNamePrefix += HELM_RELEASE_NAME_SUFFIX_VALUE;
                    properties.put(HELM_RELEASE_NAME_PREFIX_KEY, releaseNamePrefix);
                  }
                }
              }
            }
          }
        }
      }
    }

    if (workflowModified) {
      try {
        log.info("Updating workflow: {} - {}", workflow.getUuid(), workflow.getName());
        workflowService.updateWorkflow(workflow, false);
        Thread.sleep(100);
      } catch (Exception e) {
        log.error("Error updating workflow", e);
      }
    }
  }
}
