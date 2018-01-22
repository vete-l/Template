/*
 * Copyright (C) 2016 The Android Open Source Project
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
package com.github.vete_l.support.gradle.project.sync.errors;

import com.android.SdkConstants;
import com.android.annotations.Nullable;
import com.android.tools.idea.gradle.project.sync.hyperlink.CreateGradleWrapperHyperlink;
import com.android.tools.idea.project.hyperlink.NotificationHyperlink;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.intellij.openapi.util.text.StringUtil.isNotEmpty;

public class Gradle2RequiredErrorHandler extends BaseSyncErrorHandler {
  @Override
  @Nullable
  protected String findErrorMessage(@NotNull Throwable rootCause, @NotNull Project project) {
    String text = rootCause.getMessage();
    if (isNotEmpty(text) && text.endsWith("org/codehaus/groovy/runtime/typehandling/ShortTypeHandling")) {
      updateUsageTracker();
      return String.format("Gradle %1$s is required.", SdkConstants.GRADLE_MINIMUM_VERSION);
    }
    return null;
  }

  @Override
  @NotNull
  protected List<NotificationHyperlink> getQuickFixHyperlinks(@NotNull Project project, @NotNull String text) {
    List<NotificationHyperlink> hyperlinks = new ArrayList<>();
    hyperlinks.add(new CreateGradleWrapperHyperlink());
    return hyperlinks;
  }
}