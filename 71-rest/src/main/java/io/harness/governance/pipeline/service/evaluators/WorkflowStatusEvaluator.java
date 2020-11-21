package io.harness.governance.pipeline.service.evaluators;

import io.harness.govern.Switch;
import io.harness.governance.pipeline.enforce.GovernanceRuleStatus;
import io.harness.governance.pipeline.service.GovernanceStatusEvaluator;
import io.harness.governance.pipeline.service.model.PipelineGovernanceRule;

import software.wings.beans.HarnessTagLink;
import software.wings.beans.Workflow;
import software.wings.features.api.Usage;
import software.wings.service.intfc.HarnessTagService;

import com.google.inject.Inject;
import java.util.Collections;
import java.util.List;

public class WorkflowStatusEvaluator implements GovernanceStatusEvaluator<Workflow> {
  @Inject private HarnessTagService harnessTagService;

  @Override
  public GovernanceRuleStatus status(
      final String accountId, final Workflow workflow, final PipelineGovernanceRule rule) {
    List<HarnessTagLink> workflowTags = harnessTagService.fetchTagsForEntity(accountId, workflow);

    boolean tagsIncluded = false;

    switch (rule.getMatchType()) {
      case ALL:
        tagsIncluded = GovernanceStatusEvaluator.containsAll(workflowTags, rule.getTags());
        break;
      case ANY:
        tagsIncluded = GovernanceStatusEvaluator.containsAny(workflowTags, rule.getTags());
        break;
      default:
        Switch.unhandled(rule.getMatchType());
    }

    if (tagsIncluded) {
      Usage tagsLocation = Usage.builder()
                               .entityId(workflow.getUuid())
                               .entityType(EntityType.WORKFLOW.toString())
                               .entityName(workflow.getName())
                               .build();

      return new GovernanceRuleStatus(
          rule.getTags(), rule.getWeight(), true, rule.getMatchType(), Collections.singletonList(tagsLocation));
    } else {
      return new GovernanceRuleStatus(
          rule.getTags(), rule.getWeight(), false, rule.getMatchType(), Collections.emptyList());
    }
  }
}
