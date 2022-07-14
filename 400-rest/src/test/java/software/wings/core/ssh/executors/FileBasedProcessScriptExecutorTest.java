/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package software.wings.core.ssh.executors;

import static io.harness.filesystem.FileIo.deleteFileIfExists;
import static io.harness.logging.CommandExecutionStatus.FAILURE;
import static io.harness.logging.CommandExecutionStatus.SUCCESS;
import static io.harness.rule.OwnerRule.AADITI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.joor.Reflect.on;

import io.harness.CategoryTest;
import io.harness.category.element.UnitTests;
import io.harness.logging.CommandExecutionStatus;
import io.harness.logging.LogCallback;
import io.harness.rule.Owner;
import io.harness.shell.AbstractScriptExecutor;
import io.harness.shell.ScriptProcessExecutor;
import io.harness.shell.ShellExecutorConfig;

import software.wings.delegatetasks.DelegateFileManager;

import com.google.common.io.CharStreams;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;

public class FileBasedProcessScriptExecutorTest extends CategoryTest {
  @Mock private DelegateFileManager delegateFileManager;
  @Mock private LogCallback logCallback;

  @Rule public TemporaryFolder testFolder = new TemporaryFolder();

  private ScriptProcessExecutor scriptProcessExecutor;
  private FileBasedProcessScriptExecutor fileBasedProcessScriptExecutor;
  private ShellExecutorConfig shellExecutorConfig;

  @Test
  @Owner(developers = AADITI)
  @Category(UnitTests.class)
  public void testScpOneFileSuccess() throws IOException {
    scriptProcessExecutor = new ScriptProcessExecutor(logCallback, false, ShellExecutorConfig.builder().build());
    fileBasedProcessScriptExecutor = new FileBasedProcessScriptExecutor(
        delegateFileManager, logCallback, false, ShellExecutorConfig.builder().build());
    on(scriptProcessExecutor).set("logCallback", logCallback);

    File file = testFolder.newFile();
    CharStreams.asWriter(new FileWriter(file)).append("ANY_TEXT").close();

    AbstractScriptExecutor.FileProvider fileProvider = new AbstractScriptExecutor.FileProvider() {
      @Override
      public Pair<String, Long> getInfo() throws IOException {
        File file1 = new File(file.getAbsolutePath());
        return ImmutablePair.of(file1.getName(), file1.length());
      }

      @Override
      public void downloadToStream(OutputStream outputStream) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
          IOUtils.copy(fis, outputStream);
        }
      }
    };
    CommandExecutionStatus commandExecutionStatus = fileBasedProcessScriptExecutor.scpOneFile("/tmp", fileProvider);
    assertThat(commandExecutionStatus).isEqualTo(SUCCESS);
    File tempFile = new File("/tmp/" + fileProvider.getInfo().getKey());
    boolean exists = tempFile.exists();
    assertThat(exists).isTrue();

    // cleanup
    deleteFileIfExists(tempFile.getAbsolutePath());
  }

  @Test
  @Owner(developers = AADITI)
  @Category(UnitTests.class)
  public void testScpOneFileFails() {
    scriptProcessExecutor = new ScriptProcessExecutor(logCallback, true, ShellExecutorConfig.builder().build());
    fileBasedProcessScriptExecutor = new FileBasedProcessScriptExecutor(
        delegateFileManager, logCallback, true, ShellExecutorConfig.builder().build());
    on(scriptProcessExecutor).set("logCallback", logCallback);

    AbstractScriptExecutor.FileProvider fileProvider = new AbstractScriptExecutor.FileProvider() {
      @Override
      public Pair<String, Long> getInfo() throws IOException {
        return ImmutablePair.of(null, 0L);
      }

      @Override
      public void downloadToStream(OutputStream outputStream) throws IOException {
        try (FileInputStream fis = new FileInputStream("")) {
          IOUtils.copy(fis, outputStream);
        }
      }
    };
    CommandExecutionStatus commandExecutionStatus =
        fileBasedProcessScriptExecutor.scpOneFile("/randomdir", fileProvider);
    assertThat(commandExecutionStatus).isEqualTo(FAILURE);
  }
}