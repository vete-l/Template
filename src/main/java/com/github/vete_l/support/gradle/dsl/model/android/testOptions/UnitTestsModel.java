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
package com.github.vete_l.support.gradle.dsl.model.android.testOptions;

import com.android.tools.idea.gradle.dsl.model.GradleDslBlockModel;
import com.android.tools.idea.gradle.dsl.model.values.GradleNullableValue;
import com.android.tools.idea.gradle.dsl.parser.android.testOptions.UnitTestsDslElement;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class UnitTestsModel extends GradleDslBlockModel {
  @NonNls private static final String RETURN_DEFAULT_VALUES = "returnDefaultValues";

  public UnitTestsModel(@NotNull UnitTestsDslElement dslElement) {
    super(dslElement);
  }

  @NotNull
  public GradleNullableValue<Boolean> returnDefaultValues() {
    return myDslElement.getLiteralProperty(RETURN_DEFAULT_VALUES, Boolean.class);
  }

  @NotNull
  public UnitTestsModel setReturnDefaultValues(boolean returnDefaultValues) {
    myDslElement.setNewLiteral(RETURN_DEFAULT_VALUES, returnDefaultValues);
    return this;
  }

  @NotNull
  public UnitTestsModel removeReturnDefaultValues() {
    myDslElement.removeProperty(RETURN_DEFAULT_VALUES);
    return this;
  }
}
