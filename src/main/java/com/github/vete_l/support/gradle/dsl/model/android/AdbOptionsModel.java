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
package com.github.vete_l.support.gradle.dsl.model.android;

import com.android.tools.idea.gradle.dsl.model.GradleDslBlockModel;
import com.android.tools.idea.gradle.dsl.model.values.GradleNotNullValue;
import com.android.tools.idea.gradle.dsl.model.values.GradleNullableValue;
import com.android.tools.idea.gradle.dsl.parser.android.AdbOptionsDslElement;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AdbOptionsModel extends GradleDslBlockModel {
  @NonNls private static final String INSTALL_OPTIONS = "installOptions";
  @NonNls private static final String TIME_OUT_IN_MS = "timeOutInMs";

  public AdbOptionsModel(@NotNull AdbOptionsDslElement dslElement) {
    super(dslElement);
  }

  @Nullable
  public List<GradleNotNullValue<String>> installOptions() {
    return myDslElement.getListProperty(INSTALL_OPTIONS, String.class);
  }

  @NotNull
  public AdbOptionsModel addInstallOption(@NotNull String installOption) {
    myDslElement.addToNewLiteralList(INSTALL_OPTIONS, installOption);
    return this;
  }

  @NotNull
  public AdbOptionsModel removeInstallOption(@NotNull String installOption) {
    myDslElement.removeFromExpressionList(INSTALL_OPTIONS, installOption);
    return this;
  }

  @NotNull
  public AdbOptionsModel removeAllInstallOptions() {
    myDslElement.removeProperty(INSTALL_OPTIONS);
    return this;
  }

  @NotNull
  public AdbOptionsModel replaceInstallOption(@NotNull String oldInstallOption, @NotNull String newInstallOption) {
    myDslElement.replaceInExpressionList(INSTALL_OPTIONS, oldInstallOption, newInstallOption);
    return this;
  }

  @NotNull
  public GradleNullableValue<Integer> timeOutInMs() {
    return myDslElement.getLiteralProperty(TIME_OUT_IN_MS, Integer.class);
  }

  @NotNull
  public AdbOptionsModel setTimeOutInMs(int timeOutInMs) {
    myDslElement.setNewLiteral(TIME_OUT_IN_MS, timeOutInMs);
    return this;
  }

  @NotNull
  public AdbOptionsModel removeTimeOutInMs() {
    myDslElement.removeProperty(TIME_OUT_IN_MS);
    return this;
  }
}
