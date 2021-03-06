/*
 * Copyright (C) 2013 The Android Open Source Project
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

import com.android.tools.idea.project.hyperlink.NotificationHyperlink;
import com.android.tools.idea.sdk.wizard.SdkQuickfixUtils;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class OpenAndroidSdkManagerHyperlink extends NotificationHyperlink {
  public OpenAndroidSdkManagerHyperlink() {
    super("openAndroidSdkManager", "Open Android SDK Manager");
  }

  @Override
  protected void execute(@NotNull Project project) {
    SdkQuickfixUtils.showAndroidSdkManager();
  }
}
