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
import com.android.tools.idea.gradle.dsl.parser.elements.GradleDslExpression;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class AaptOptionsDslElement extends GradleDslBlockElement {
  @NonNls public static final String AAPT_OPTIONS_BLOCK_NAME = "aaptOptions";

  public AaptOptionsDslElement(@NotNull GradleDslElement parent) {
    super(parent, AAPT_OPTIONS_BLOCK_NAME);
  }

  @Override
  public void setParsedElement(@NotNull String property, @NotNull GradleDslElement element) {
    if (property.equals("ignoreAssetsPattern")) {
      super.addParsedElement("ignoreAssets", element);
      return;
    }
    super.addParsedElement(property, element);
  }

  @Override
  public void addParsedElement(@NotNull String property, @NotNull GradleDslElement element) {
    if (element instanceof GradleDslExpression && (property.equals("additionalParameters") || property.equals("noCompress"))) {
      addAsParsedDslExpressionList(property, (GradleDslExpression)element);
      return;
    }
    if (property.equals("ignoreAssetsPattern")) {
      super.addParsedElement("ignoreAssets", element);
      return;
    }
    super.addParsedElement(property, element);
  }
}
