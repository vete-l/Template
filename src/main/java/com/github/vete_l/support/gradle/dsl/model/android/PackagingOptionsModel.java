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
import com.android.tools.idea.gradle.dsl.parser.android.PackagingOptionsDslElement;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PackagingOptionsModel extends GradleDslBlockModel {
  @NonNls private static final String EXCLUDES = "excludes";
  @NonNls private static final String MERGES = "merges";
  @NonNls private static final String PICK_FIRSTS = "pickFirsts";

  public PackagingOptionsModel(@NotNull PackagingOptionsDslElement dslElement) {
    super(dslElement);
  }

  @Nullable
  public List<GradleNotNullValue<String>> excludes() {
    return myDslElement.getListProperty(EXCLUDES, String.class);
  }

  @NotNull
  public PackagingOptionsModel addExclude(@NotNull String exclude) {
    myDslElement.addToNewLiteralList(EXCLUDES, exclude);
    return this;
  }

  @NotNull
  public PackagingOptionsModel removeExclude(@NotNull String exclude) {
    myDslElement.removeFromExpressionList(EXCLUDES, exclude);
    return this;
  }

  @NotNull
  public PackagingOptionsModel removeAllExclude() {
    myDslElement.removeProperty(EXCLUDES);
    return this;
  }

  @NotNull
  public PackagingOptionsModel replaceExclude(@NotNull String oldExclude, @NotNull String newExclude) {
    myDslElement.replaceInExpressionList(EXCLUDES, oldExclude, newExclude);
    return this;
  }

  @Nullable
  public List<GradleNotNullValue<String>> merges() {
    return myDslElement.getListProperty(MERGES, String.class);
  }

  @NotNull
  public PackagingOptionsModel addMerge(@NotNull String merge) {
    myDslElement.addToNewLiteralList(MERGES, merge);
    return this;
  }

  @NotNull
  public PackagingOptionsModel removeMerge(@NotNull String merge) {
    myDslElement.removeFromExpressionList(MERGES, merge);
    return this;
  }

  @NotNull
  public PackagingOptionsModel removeAllMerges() {
    myDslElement.removeProperty(MERGES);
    return this;
  }

  @NotNull
  public PackagingOptionsModel replaceMerge(@NotNull String oldMerge, @NotNull String newMerge) {
    myDslElement.replaceInExpressionList(MERGES, oldMerge, newMerge);
    return this;
  }

  @Nullable
  public List<GradleNotNullValue<String>> pickFirsts() {
    return myDslElement.getListProperty(PICK_FIRSTS, String.class);
  }

  @NotNull
  public PackagingOptionsModel addPickFirst(@NotNull String pickFirst) {
    myDslElement.addToNewLiteralList(PICK_FIRSTS, pickFirst);
    return this;
  }

  @NotNull
  public PackagingOptionsModel removePickFirst(@NotNull String pickFirst) {
    myDslElement.removeFromExpressionList(PICK_FIRSTS, pickFirst);
    return this;
  }

  @NotNull
  public PackagingOptionsModel removeAllPickFirsts() {
    myDslElement.removeProperty(PICK_FIRSTS);
    return this;
  }

  @NotNull
  public PackagingOptionsModel replacePickFirst(@NotNull String oldPickFirst, @NotNull String newPickFirst) {
    myDslElement.replaceInExpressionList(PICK_FIRSTS, oldPickFirst, newPickFirst);
    return this;
  }
}
