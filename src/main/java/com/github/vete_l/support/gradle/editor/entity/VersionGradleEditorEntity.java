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
package com.github.vete_l.support.gradle.editor.entity;

import com.android.tools.idea.gradle.editor.metadata.GradleEditorEntityMetaData;
import com.android.tools.idea.gradle.editor.value.GradleEditorEntityValueManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Set;

/**
 * {@link GradleEditorEntity} which holds a single value (e.g. compile sdk version), which, in turn, is managed
 * by {@link GradleEditorEntityValueManager}.
 */
public class VersionGradleEditorEntity extends AbstractSimpleGradleEditorEntity {

  public VersionGradleEditorEntity(@NotNull String name,
                                   @NotNull Collection<GradleEditorSourceBinding> definitionValueSourceBindings,
                                   @NotNull GradleEditorSourceBinding entityLocation,
                                   @NotNull Set<GradleEditorEntityMetaData> metadata,
                                   @NotNull GradleEditorSourceBinding declarationValueLocation,
                                   @NotNull String currentVersion,
                                   @NotNull GradleEditorEntityValueManager valueManager,
                                   @Nullable String helpUrl) {
    super(name, currentVersion, definitionValueSourceBindings, entityLocation, metadata, declarationValueLocation, valueManager, helpUrl);
  }
}
