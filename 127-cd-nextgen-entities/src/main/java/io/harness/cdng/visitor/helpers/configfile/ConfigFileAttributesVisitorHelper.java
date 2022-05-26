/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.cdng.visitor.helpers.configfile;

import io.harness.cdng.configfile.ConfigFileAttributes;
import io.harness.walktree.visitor.validation.ConfigValidator;
import io.harness.walktree.visitor.validation.ValidationVisitor;

public class ConfigFileAttributesVisitorHelper implements ConfigValidator {
  @Override
  public void validate(Object object, ValidationVisitor visitor) {
    // Nothing to validate.
  }

  @Override
  public Object createDummyVisitableElement(Object originalElement) {
    ConfigFileAttributes configFileAttributes = (ConfigFileAttributes) originalElement;
    return ConfigFileAttributes.builder()
        .store(configFileAttributes.getStore())
        .type(configFileAttributes.getType())
        .build();
  }
}