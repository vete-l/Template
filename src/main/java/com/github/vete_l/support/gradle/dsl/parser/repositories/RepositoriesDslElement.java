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
package com.github.vete_l.support.gradle.dsl.parser.repositories;

import com.android.tools.idea.gradle.dsl.parser.elements.GradleDslBlockElement;
import com.android.tools.idea.gradle.dsl.parser.elements.GradleDslElement;
import com.android.tools.idea.gradle.dsl.parser.elements.GradleDslElementList;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RepositoriesDslElement extends GradleDslBlockElement {
  @NonNls public static final String REPOSITORIES_BLOCK_NAME = "repositories";

  public RepositoriesDslElement(@Nullable GradleDslElement parent) {
    super(parent, REPOSITORIES_BLOCK_NAME);
  }

  @Override
  public void setParsedElement(@NotNull String name, @NotNull GradleDslElement repository) {
    // Because we need to preserve the the order of the repositories defined, storing all the repository elements in a dummy element list.
    // TODO: Consider extending RepositoriesDslElement directly from GradleDslElementList instead of GradlePropertiesDslElement.
    GradleDslElementList repositoriesListElement = getOrCreateRepositoriesElement();
    repositoriesListElement.addParsedElement(repository);
  }

  @Override
  public void addParsedElement(@NotNull String name, @NotNull GradleDslElement repository) {
    GradleDslElementList repositoriesListElement = getOrCreateRepositoriesElement();
    repositoriesListElement.addParsedElement(repository);
  }

  @NotNull
  private GradleDslElementList getOrCreateRepositoriesElement() {
    GradleDslElementList elementList = getPropertyElement(REPOSITORIES_BLOCK_NAME, GradleDslElementList.class);
    if (elementList == null) {
      elementList = new GradleDslElementList(this, REPOSITORIES_BLOCK_NAME);
      super.addParsedElement(REPOSITORIES_BLOCK_NAME, elementList);
    }
    return elementList;
  }
}
