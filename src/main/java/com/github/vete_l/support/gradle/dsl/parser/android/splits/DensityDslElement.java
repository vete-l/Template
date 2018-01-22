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
package com.github.vete_l.support.gradle.dsl.parser.android.splits;

import com.android.tools.idea.gradle.dsl.parser.elements.GradleDslBlockElement;
import com.android.tools.idea.gradle.dsl.parser.elements.GradleDslElement;
import com.android.tools.idea.gradle.dsl.parser.elements.GradleDslMethodCall;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class DensityDslElement extends GradleDslBlockElement {
  @NonNls public static final String DENSITY_BLOCK_NAME = "density";

  public DensityDslElement(@NotNull GradleDslElement parent) {
    super(parent, DENSITY_BLOCK_NAME);
  }

  @Override
  public void addParsedElement(@NotNull String property, @NotNull GradleDslElement element) {
    if (property.equals("compatibleScreens") || property.equals("include") || property.equals("exclude")) {
      addToParsedExpressionList(property, element);
      return;
    }

    if (property.equals("reset") && element instanceof GradleDslMethodCall) {
      addParsedResettingElement("reset", element, "include");
      return;
    }

    super.addParsedElement(property, element);
  }
}
