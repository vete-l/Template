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
import com.android.tools.idea.gradle.dsl.model.android.splits.AbiModel;
import com.android.tools.idea.gradle.dsl.model.android.splits.DensityModel;
import com.android.tools.idea.gradle.dsl.model.android.splits.LanguageModel;
import com.android.tools.idea.gradle.dsl.parser.android.SplitsDslElement;
import com.android.tools.idea.gradle.dsl.parser.android.splits.AbiDslElement;
import com.android.tools.idea.gradle.dsl.parser.android.splits.DensityDslElement;
import com.android.tools.idea.gradle.dsl.parser.android.splits.LanguageDslElement;
import org.jetbrains.annotations.NotNull;

import static com.android.tools.idea.gradle.dsl.parser.android.splits.AbiDslElement.ABI_BLOCK_NAME;
import static com.android.tools.idea.gradle.dsl.parser.android.splits.DensityDslElement.DENSITY_BLOCK_NAME;
import static com.android.tools.idea.gradle.dsl.parser.android.splits.LanguageDslElement.LANGUAGE_BLOCK_NAME;

public class SplitsModel extends GradleDslBlockModel {
  public SplitsModel(@NotNull SplitsDslElement dslElement) {
    super(dslElement);
  }

  @NotNull
  public AbiModel abi() {
    AbiDslElement abiDslElement = myDslElement.getPropertyElement(ABI_BLOCK_NAME, AbiDslElement.class);
    if (abiDslElement == null) {
      abiDslElement = new AbiDslElement(myDslElement);
      myDslElement.setNewElement(ABI_BLOCK_NAME, abiDslElement);
    }
    return new AbiModel(abiDslElement);
  }

  @NotNull
  public SplitsModel removeAbi() {
    myDslElement.removeProperty(ABI_BLOCK_NAME);
    return this;
  }


  @NotNull
  public DensityModel density() {
    DensityDslElement densityDslElement = myDslElement.getPropertyElement(DENSITY_BLOCK_NAME, DensityDslElement.class);
    if (densityDslElement == null) {
      densityDslElement = new DensityDslElement(myDslElement);
      myDslElement.setNewElement(DENSITY_BLOCK_NAME, densityDslElement);
    }
    return new DensityModel(densityDslElement);
  }

  @NotNull
  public SplitsModel removeDensity() {
    myDslElement.removeProperty(DENSITY_BLOCK_NAME);
    return this;
  }

  @NotNull
  public LanguageModel language() {
    LanguageDslElement languageDslElement = myDslElement.getPropertyElement(LANGUAGE_BLOCK_NAME, LanguageDslElement.class);
    if (languageDslElement == null) {
      languageDslElement = new LanguageDslElement(myDslElement);
      myDslElement.setNewElement(LANGUAGE_BLOCK_NAME, languageDslElement);
    }
    return new LanguageModel(languageDslElement);
  }

  @NotNull
  public SplitsModel removeLanguage() {
    myDslElement.removeProperty(LANGUAGE_BLOCK_NAME);
    return this;
  }
}
