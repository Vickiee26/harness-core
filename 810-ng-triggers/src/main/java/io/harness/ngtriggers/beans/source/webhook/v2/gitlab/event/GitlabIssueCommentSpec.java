/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Shield 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/06/PolyForm-Shield-1.0.0.txt.
 */

package io.harness.ngtriggers.beans.source.webhook.v2.gitlab.event;

import static io.harness.annotations.dev.HarnessTeam.CI;
import static io.harness.data.structure.EmptyPredicate.isEmpty;
import static io.harness.ngtriggers.beans.source.webhook.v2.gitlab.event.GitlabTriggerEvent.ISSUE_COMMENT;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import io.harness.annotations.dev.OwnedBy;
import io.harness.ngtriggers.beans.source.webhook.v2.TriggerEventDataCondition;
import io.harness.ngtriggers.beans.source.webhook.v2.git.GitAction;
import io.harness.ngtriggers.beans.source.webhook.v2.git.GitEvent;
import io.harness.ngtriggers.beans.source.webhook.v2.gitlab.action.GitlabIssueCommentAction;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@OwnedBy(CI)
public class GitlabIssueCommentSpec implements GitlabEventSpec {
    String connectorRef;
    String repoName;
    List<GitlabIssueCommentAction> actions;
    List<TriggerEventDataCondition> headerConditions;
    List<TriggerEventDataCondition> payloadConditions;
    String jexlCondition;
    boolean autoAbortPreviousExecutions;

    @Override
    public String fetchConnectorRef() {
        return connectorRef;
    }

    @Override
    public String fetchRepoName() {
        return repoName;
    }

    @Override
    public GitEvent fetchEvent() {
        return ISSUE_COMMENT;
    }

    @Override
    public List<GitAction> fetchActions() {
        if (isEmpty(actions)) {
            return emptyList();
        }

        return actions.stream().collect(toList());
    }

    @Override
    public List<TriggerEventDataCondition> fetchHeaderConditions() {
        return headerConditions;
    }

    @Override
    public List<TriggerEventDataCondition> fetchPayloadConditions() {
        return payloadConditions;
    }

    @Override
    public String fetchJexlCondition() {
        return jexlCondition;
    }

    @Override
    public boolean fetchAutoAbortPreviousExecutions() {
        return autoAbortPreviousExecutions;
    }
}