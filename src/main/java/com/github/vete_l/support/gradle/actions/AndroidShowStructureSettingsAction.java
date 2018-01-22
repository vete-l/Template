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
package com.github.vete_l.support.gradle.actions;

import com.android.tools.idea.IdeInfo;
import com.android.tools.idea.gradle.project.GradleExperimentalSettings;
import com.android.tools.idea.gradle.project.GradleProjectInfo;
import com.android.tools.idea.gradle.project.sync.GradleSyncInvoker;
import com.android.tools.idea.gradle.structure.AndroidProjectStructureConfigurable;
import com.android.tools.idea.project.AndroidProjectInfo;
import com.android.tools.idea.structure.dialog.ProjectStructureConfigurable;
import com.android.tools.idea.structure.dialog.ProjectStructureConfigurable.ProjectStructureChangeListener;
import com.intellij.ide.actions.ShowStructureSettingsAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.wireless.android.sdk.stats.GradleSyncStats.Trigger.TRIGGER_PROJECT_MODIFIED;

/**
 * Displays the "Project Structure" dialog.
 */
public class AndroidShowStructureSettingsAction extends ShowStructureSettingsAction {
  @Override
  public void update(AnActionEvent e) {
    Project project = e.getProject();
    if (project != null && AndroidProjectInfo.getInstance(project).requiresAndroidModel()) {
      e.getPresentation().setEnabledAndVisible(GradleProjectInfo.getInstance(project).isBuildWithGradle());
    }
    super.update(e);
  }

  @Override
  public void actionPerformed(AnActionEvent e) {
    Project project = e.getProject();
    if (project == null && IdeInfo.getInstance().isAndroidStudio()) {
      project = ProjectManager.getInstance().getDefaultProject();
      showAndroidProjectStructure(project);
      return;
    }

    if (project != null && GradleProjectInfo.getInstance(project).isBuildWithGradle()) {
      showAndroidProjectStructure(project);
      return;
    }

    super.actionPerformed(e);
  }

  private static void showAndroidProjectStructure(@NotNull Project project) {
    if (GradleExperimentalSettings.getInstance().USE_NEW_PROJECT_STRUCTURE_DIALOG) {
      ProjectStructureConfigurable projectStructure = ProjectStructureConfigurable.getInstance(project);
      AtomicBoolean needsSync = new AtomicBoolean();
      ProjectStructureChangeListener changeListener = () -> needsSync.set(true);
      projectStructure.add(changeListener);
      projectStructure.showDialog();
      projectStructure.remove(changeListener);
      if (needsSync.get()) {
        GradleSyncInvoker.getInstance().requestProjectSyncAndSourceGeneration(project, TRIGGER_PROJECT_MODIFIED, null);
      }
      return;
    }
    AndroidProjectStructureConfigurable.getInstance(project).showDialog();
  }
}
