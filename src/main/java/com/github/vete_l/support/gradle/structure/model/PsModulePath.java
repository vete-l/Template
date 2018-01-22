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
package com.github.vete_l.support.gradle.structure.model;

import com.google.common.base.Objects;
import org.jetbrains.annotations.NotNull;

public final class PsModulePath extends PsPath {
  @NotNull private final String myModuleName;

  public PsModulePath(@NotNull PsModule module) {
    myModuleName = module.getName();
  }

  @NotNull
  public String getModuleName() {
    return myModuleName;
  }

  @Override
  @NotNull
  public String toText(@NotNull TexType type) {
    switch (type) {
      case FOR_COMPARE_TO:
      case PLAIN_TEXT:
        return myModuleName;
      default:
        return "";
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PsModulePath that = (PsModulePath)o;
    return Objects.equal(myModuleName, that.myModuleName);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(myModuleName);
  }
}
