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
package com.github.vete_l.support.gradle.dsl.parser.android;

import com.android.tools.idea.gradle.dsl.parser.elements.GradleDslBlockElement;
import com.android.tools.idea.gradle.dsl.parser.elements.GradleDslElement;
import org.jetbrains.annotations.NotNull;

public class LintOptionsDslElement extends GradleDslBlockElement {
  public static final String LINT_OPTIONS_BLOCK_NAME = "lintOptions";

  public LintOptionsDslElement(@NotNull GradleDslElement parent) {
    super(parent, LINT_OPTIONS_BLOCK_NAME);
  }

  @Override
  public void addParsedElement(@NotNull String property, @NotNull GradleDslElement element) {
    if (property.equals("check") ||
        property.equals("disable") ||
        property.equals("enable") ||
        property.equals("error") ||
        property.equals("fatal") ||
        property.equals("ignore") ||
        property.equals("warning")) {
      addToParsedExpressionList(property, element);
      return;
    }

    super.addParsedElement(property, element);
  }
}
