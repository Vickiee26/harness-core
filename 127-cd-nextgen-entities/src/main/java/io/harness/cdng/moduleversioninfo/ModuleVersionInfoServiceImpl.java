/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.cdng.moduleversioninfo;

import static io.harness.persistence.HQuery.excludeValidate;

import io.harness.beans.PageRequest;
import io.harness.beans.PageResponse;
import io.harness.cdng.moduleversioninfo.entity.ModuleVersionInfo;
import io.harness.cdng.moduleversioninfo.service.ModuleVersionInfoService;
import io.harness.persistence.HPersistence;

import com.google.inject.Inject;

public class ModuleVersionInfoServiceImpl implements ModuleVersionInfoService {
  @Inject private HPersistence persistence;

  @Override
  public PageResponse<ModuleVersionInfo> getCurrentVersionOfAllModules(PageRequest<ModuleVersionInfo> pageRequest) {
    return persistence.query(ModuleVersionInfo.class, pageRequest, excludeValidate);
  }
}
