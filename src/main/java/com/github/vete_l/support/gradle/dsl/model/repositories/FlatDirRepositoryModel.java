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
package com.github.vete_l.support.gradle.dsl.model.repositories;

import com.android.tools.idea.gradle.dsl.model.values.GradleNotNullValue;
import com.android.tools.idea.gradle.dsl.model.values.GradleNullableValue;
import com.android.tools.idea.gradle.dsl.parser.elements.GradlePropertiesDslElement;
import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Represents a repository defined with flatDir {dirs "..."} or flatDir dirs : ["..."].
 */
public class FlatDirRepositoryModel extends RepositoryModel {
  @NonNls public static final String FLAT_DIR_ATTRIBUTE_NAME = "flatDir";

  @NonNls private static final String DIRS = "dirs";

  public FlatDirRepositoryModel(@NotNull GradlePropertiesDslElement dslElement) {
    super(dslElement, "flatDir");
  }

  @NotNull
  public List<GradleNotNullValue<String>> dirs() {
    assert myDslElement != null;

    List<GradleNotNullValue<String>> dirs = myDslElement.getListProperty(DIRS, String.class);
    if (dirs != null) {
      return dirs;
    }

    GradleNullableValue<String> dir = myDslElement.getLiteralProperty(DIRS, String.class);
    if (dir.value() != null) {
      assert dir instanceof GradleNotNullValue;
      return ImmutableList.of((GradleNotNullValue<String>)dir);
    }

    return ImmutableList.of();
  }
}
