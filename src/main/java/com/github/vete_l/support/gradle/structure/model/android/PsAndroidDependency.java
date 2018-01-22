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
package com.github.vete_l.support.gradle.structure.model.android;

import com.android.tools.idea.gradle.project.model.AndroidModuleModel;
import com.android.tools.idea.gradle.dsl.model.dependencies.DependencyModel;
import com.android.tools.idea.gradle.structure.model.PsDependency;
import com.google.common.collect.Sets;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class PsAndroidDependency extends PsDependency implements PsAndroidModel {
  @NotNull private final Set<PsDependencyContainer> myContainers = Sets.newHashSet();

  PsAndroidDependency(@NotNull PsAndroidModule parent,
                      @NotNull PsAndroidArtifact container,
                      @Nullable DependencyModel parsedModel) {
    super(parent, parsedModel);
    addContainer(container);
  }

  @Override
  @NotNull
  public AndroidModuleModel getGradleModel() {
    return getParent().getGradleModel();
  }

  @Override
  @NotNull
  public PsAndroidModule getParent() {
    return (PsAndroidModule)super.getParent();
  }

  void addContainer(@NotNull PsAndroidArtifact artifact) {
    myContainers.add(new PsDependencyContainer(artifact));
  }

  @TestOnly
  @NotNull
  public Collection<String> getVariants() {
    return myContainers.stream().map(PsDependencyContainer::getVariant).collect(Collectors.toSet());
  }

  @NotNull
  public Collection<PsDependencyContainer> getContainers() {
    return myContainers;
  }

  public boolean isIn(@NotNull String artifactName, @Nullable String variantName) {
    for (PsDependencyContainer container : myContainers) {
      if (artifactName.equals(container.getArtifact())) {
        if (variantName == null) {
          return true;
        }
        if (variantName.equals(container.getVariant())) {
          return true;
        }
      }
    }
    return false;
  }
}
