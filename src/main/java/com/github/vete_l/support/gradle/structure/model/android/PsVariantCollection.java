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

import com.android.tools.idea.gradle.structure.model.PsModelCollection;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

class PsVariantCollection implements PsModelCollection<PsVariant> {
  @NotNull private final Map<String, PsVariant> myVariantsByName = Maps.newHashMap();

  PsVariantCollection(@NotNull PsAndroidModule parent) {
    parent.getGradleModel().getAndroidProject().forEachVariant(ideVariant ->
    {
      List<String> productFlavors = Lists.newArrayList();
      for (String productFlavorName : ideVariant.getProductFlavors()) {
        PsProductFlavor productFlavor = parent.findProductFlavor(productFlavorName);
        if (productFlavor != null) {
          productFlavors.add(productFlavor.getName());
        }
        else {
          // TODO handle case when product flavor is not found.
        }
      }
      String buildType = ideVariant.getBuildType();

      PsVariant variant = new PsVariant(parent, ideVariant.getName(), buildType, productFlavors, ideVariant);
      myVariantsByName.put(ideVariant.getName(), variant);
    });
  }

  @Override
  @Nullable
  public <S extends PsVariant> S findElement(@NotNull String name, @NotNull Class<S> type) {
    PsVariant found = myVariantsByName.get(name);
    return type.isInstance(found) ? type.cast(found) : null;
  }

  @Override
  public void forEach(@NotNull Consumer<PsVariant> consumer) {
    myVariantsByName.values().forEach(consumer);
  }
}
