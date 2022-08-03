/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.repositories.rollback;

import static io.harness.annotations.dev.HarnessTeam.CDP;

import io.harness.annotation.HarnessRepo;
import io.harness.annotations.dev.OwnedBy;
import io.harness.cdng.rollback.RollbackData;

import java.util.Optional;
import org.springframework.data.repository.PagingAndSortingRepository;

@HarnessRepo
@OwnedBy(CDP)
public interface RollbackDataRepository
    extends PagingAndSortingRepository<RollbackData, String>, RollbackDataRepositoryCustom {
  Optional<RollbackData> findByKeyAndStageStatus(String rollbackKey, String status);

  Optional<RollbackData> findByStageExecutionId(String executionId);
}