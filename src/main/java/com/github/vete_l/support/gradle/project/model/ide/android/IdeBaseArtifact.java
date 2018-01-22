/*
 * Copyright (C) 2017 The Android Open Source Project
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
package com.github.vete_l.support.gradle.project.model.ide.android;

import com.android.builder.model.BaseArtifact;
import com.android.tools.idea.gradle.project.model.ide.android.level2.IdeDependencies;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

public interface IdeBaseArtifact extends Serializable, BaseArtifact {
  boolean isTestArtifact();

  @NotNull
  IdeDependencies getLevel2Dependencies();

  @Override
  @Deprecated
  @NotNull
  com.android.tools.idea.gradle.project.model.ide.android.IdeDependencies getDependencies();
}
