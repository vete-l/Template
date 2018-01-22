/*
 * Copyright (C) 2015 The Android Open Source Project
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
package com.github.vete_l.support.gradle.dsl.parser.settings;

import com.android.tools.idea.gradle.dsl.model.values.GradleNullableValue;
import com.android.tools.idea.gradle.dsl.parser.elements.GradleDslElement;
import com.android.tools.idea.gradle.dsl.parser.elements.GradleDslNewExpression;
import com.android.tools.idea.gradle.dsl.parser.elements.GradlePropertiesDslElement;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class ProjectPropertiesDslElement extends GradlePropertiesDslElement {
  @NonNls private static final String PROJECT_DIR = "projectDir";
  @NonNls private static final String BUILD_FILE_NAME = "buildFileName";

  public ProjectPropertiesDslElement(@Nullable GradleDslElement parent, @NotNull String name) {
    super(parent, null, name);
  }

  @Nullable
  public File projectDir() {
    GradleDslNewExpression projectDir = getPropertyElement(PROJECT_DIR, GradleDslNewExpression.class);
    if (projectDir != null) {
      return projectDir.getValue(File.class);
    }
    return null;
  }

  @NotNull
  public GradleNullableValue<String> buildFileName() {
    return getLiteralProperty(BUILD_FILE_NAME, String.class);
  }

  @Nullable
  public static String getStandardProjectKey(@NotNull String projectReference) {
    String standardForm = projectReference.replaceAll("\\s", "").replace("\"", "'");
    if (standardForm.startsWith("project(':") && standardForm.endsWith("')")) {
      return standardForm;
    }
    return null;
  }
}
