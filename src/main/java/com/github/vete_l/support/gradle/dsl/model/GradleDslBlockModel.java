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
package com.github.vete_l.support.gradle.dsl.model;

import com.android.tools.idea.gradle.dsl.model.values.GradleNotNullValue;
import com.android.tools.idea.gradle.dsl.model.values.GradleNullableValue;
import com.android.tools.idea.gradle.dsl.parser.elements.GradleDslElement;
import com.android.tools.idea.gradle.dsl.parser.elements.GradlePropertiesDslElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElement;

/**
 * Base class for the models representing block elements.
 */
public abstract class GradleDslBlockModel {
  protected GradlePropertiesDslElement myDslElement;

  protected GradleDslBlockModel(@NotNull GradlePropertiesDslElement dslElement) {
    myDslElement = dslElement;
  }

  @Nullable
  public GroovyPsiElement getPsiElement() {
    return myDslElement.getPsiElement();
  }

  public boolean hasValidPsiElement() {
    GroovyPsiElement psiElement = getPsiElement();
    return psiElement != null && psiElement.isValid();
  }

  @NotNull
  protected GradleNullableValue<String> getIntOrStringValue(@NotNull String propertyName) {
    Integer intValue = myDslElement.getLiteralProperty(propertyName, Integer.class).value();
    if (intValue != null) {
      GradleDslElement propertyElement = myDslElement.getPropertyElement(propertyName);
      assert propertyElement != null;
      return new GradleNotNullValue<>(propertyElement, intValue.toString());
    }
    return myDslElement.getLiteralProperty(propertyName, String.class);
  }
}
