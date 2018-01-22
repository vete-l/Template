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
package com.github.vete_l.support.gradle.dsl.model.android.productFlavors;

import com.android.tools.idea.gradle.dsl.model.GradleDslBlockModel;
import com.android.tools.idea.gradle.dsl.model.values.GradleNotNullValue;
import com.android.tools.idea.gradle.dsl.parser.android.productFlavors.NdkOptionsDslElement;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class NdkOptionsModel extends GradleDslBlockModel {
  @NonNls private static final String ABI_FILTERS = "abiFilters";

  public NdkOptionsModel(@NotNull NdkOptionsDslElement dslElement) {
    super(dslElement);
  }

  @Nullable
  public List<GradleNotNullValue<String>> abiFilters() {
    return myDslElement.getListProperty(ABI_FILTERS, String.class);
  }

  @NotNull
  public NdkOptionsModel addAbiFilter(@NotNull String abiFilter) {
    myDslElement.addToNewLiteralList(ABI_FILTERS, abiFilter);
    return this;
  }

  @NotNull
  public NdkOptionsModel removeAbiFilter(@NotNull String abiFilter) {
    myDslElement.removeFromExpressionList(ABI_FILTERS, abiFilter);
    return this;
  }

  @NotNull
  public NdkOptionsModel removeAllAbiFilters() {
    myDslElement.removeProperty(ABI_FILTERS);
    return this;
  }

  @NotNull
  public NdkOptionsModel replaceAbiFilter(@NotNull String oldAbiFilter, @NotNull String newAbiFilter) {
    myDslElement.replaceInExpressionList(ABI_FILTERS, oldAbiFilter, newAbiFilter);
    return this;
  }
}
