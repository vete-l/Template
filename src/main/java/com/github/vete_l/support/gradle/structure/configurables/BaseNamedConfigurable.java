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
package com.github.vete_l.support.gradle.structure.configurables;

import com.android.tools.idea.gradle.dsl.model.GradleBuildModel;
import com.android.tools.idea.gradle.structure.model.PsModule;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.ui.NamedConfigurable;
import com.intellij.ui.navigation.History;
import com.intellij.ui.navigation.Place;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public abstract class BaseNamedConfigurable<T extends PsModule> extends NamedConfigurable<T>
  implements SearchableConfigurable, Place.Navigator {

  @NotNull private final T myModule;

  private String myDisplayName;
  private History myHistory;

  protected BaseNamedConfigurable(@NotNull T module) {
    myModule = module;
    myDisplayName = module.getName();
  }

  @NotNull
  protected T getModule() {
    return myModule;
  }

  @Override
  public boolean isModified() {
    return myModule.isModified();
  }

  @Override
  public void apply() throws ConfigurationException {
    if (myModule.isModified()) {
      GradleBuildModel parsedModel = myModule.getParsedModel();
      if (parsedModel != null && parsedModel.isModified()) {
        String name = String.format("Applying changes to module '%1$s'", myModule.getName());
        new WriteCommandAction(myModule.getParent().getResolvedModel(), name) {
          @Override
          protected void run(@NotNull Result result) throws Throwable {
            parsedModel.applyChanges();
            myModule.setModified(false);
          }
        }.execute();
      }
    }
  }

  @Override
  public void reset() {
  }

  @Override
  @Nullable
  public Icon getIcon(boolean expanded) {
    return myModule.getIcon();
  }

  @Override
  @Nls
  public String getDisplayName() {
    return myDisplayName;
  }

  @Override
  public void setDisplayName(String name) {
    myDisplayName = name;
  }

  @Override
  public T getEditableObject() {
    return myModule;
  }

  @Override
  public String getBannerSlogan() {
    return "Module '" + myDisplayName + "'";
  }

  @Override
  public void setHistory(History history) {
    myHistory = history;
  }

  @Nullable
  protected History getHistory() {
    return myHistory;
  }

  public void putNavigationPath(@NotNull Place place, @NotNull String value) {
  }

  @Override
  @Nullable
  public String getHelpTopic() {
    return null;
  }

  @Override
  @Nullable
  public Runnable enableSearch(String option) {
    return null;
  }
}
