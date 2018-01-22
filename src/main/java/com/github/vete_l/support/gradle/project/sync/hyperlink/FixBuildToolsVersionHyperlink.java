/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.vete_l.support.gradle.project.sync.hyperlink;

import com.android.tools.idea.gradle.dsl.model.GradleBuildModel;
import com.android.tools.idea.gradle.dsl.model.android.AndroidModel;
import com.android.tools.idea.gradle.project.sync.GradleSyncInvoker;
import com.android.tools.idea.project.hyperlink.NotificationHyperlink;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import static com.android.tools.idea.gradle.dsl.model.GradleBuildModel.parseBuildFile;
import static com.google.wireless.android.sdk.stats.GradleSyncStats.Trigger.TRIGGER_PROJECT_MODIFIED;
import static com.intellij.openapi.command.WriteCommandAction.runWriteCommandAction;

public class FixBuildToolsVersionHyperlink extends NotificationHyperlink {
  @NotNull private final VirtualFile myBuildFile;
  @NotNull private final String myVersion;

  public FixBuildToolsVersionHyperlink(@NotNull VirtualFile buildFile, @NotNull String version) {
    super("fix.build.tools.version", "Update Build Tools version and sync project");
    myBuildFile = buildFile;
    myVersion = version;
  }

  @Override
  protected void execute(@NotNull Project project) {
    setBuildToolsVersion(project, myBuildFile, myVersion, true);
  }

  static void setBuildToolsVersion(@NotNull Project project, @NotNull VirtualFile buildFile, @NotNull String version, boolean requestSync) {
    // TODO check that the build file has the 'android' plugin applied.
    GradleBuildModel buildModel = parseBuildFile(buildFile, project);

    AndroidModel android = buildModel.android();
    if (android == null) {
      return;
    }

    if (version.equals(android.buildToolsVersion().value())) {
      return;
    }

    android.setBuildToolsVersion(version);
    runWriteCommandAction(project, buildModel::applyChanges);

    if (requestSync) {
      GradleSyncInvoker.getInstance().requestProjectSyncAndSourceGeneration(project, TRIGGER_PROJECT_MODIFIED, null);
    }
  }
}
