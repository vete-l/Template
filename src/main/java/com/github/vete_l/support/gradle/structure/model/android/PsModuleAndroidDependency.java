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

import com.android.tools.idea.gradle.dsl.model.dependencies.DependencyModel;
import com.android.tools.idea.gradle.dsl.model.dependencies.ModuleDependencyModel;
import com.android.tools.idea.gradle.structure.model.PsModuleDependency;
import com.intellij.openapi.module.Module;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

import static com.android.tools.idea.gradle.util.GradleUtil.getModuleIcon;
import static icons.StudioIcons.Shell.Filetree.ANDROID_MODULE;

public class PsModuleAndroidDependency extends PsAndroidDependency implements PsModuleDependency {
  @NotNull private final String myGradlePath;
  @NotNull private final String myName;

  @Nullable private final String myConfigurationName;
  @Nullable private final Module myResolvedModel;

  PsModuleAndroidDependency(@NotNull PsAndroidModule parent,
                            @NotNull String gradlePath,
                            @NotNull PsAndroidArtifact artifact,
                            @Nullable String configurationName,
                            @Nullable Module resolvedModel,
                            @Nullable ModuleDependencyModel parsedModel) {
    super(parent, artifact, parsedModel);
    myGradlePath = gradlePath;
    myConfigurationName = configurationName;
    myResolvedModel = resolvedModel;
    String name = null;
    if (resolvedModel != null) {
      name = resolvedModel.getName();
    }
    else if (parsedModel != null) {
      name = parsedModel.name();
    }
    assert name != null;
    myName = name;
  }

  @Override
  @NotNull
  public String getGradlePath() {
    return myGradlePath;
  }

  @Override
  @Nullable
  public String getConfigurationName() {
    return myConfigurationName;
  }

  @Override
  @NotNull
  public String getName() {
    return myName;
  }

  @Override
  @Nullable
  public Icon getIcon() {
    if (myResolvedModel != null) {
      return getModuleIcon(myResolvedModel);
    }
    return ANDROID_MODULE;
  }

  @Override
  public void addParsedModel(@NotNull DependencyModel parsedModel) {
    assert parsedModel instanceof ModuleDependencyModel;
    super.addParsedModel(parsedModel);
  }

  @Override
  @NotNull
  public String toText(@NotNull TextType type) {
    return myName;
  }

  @Override
  @Nullable
  public Module getResolvedModel() {
    return myResolvedModel;
  }
}
