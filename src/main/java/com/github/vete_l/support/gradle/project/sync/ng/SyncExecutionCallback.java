/*
 * Copyright (C) 2017 The Android Open Source Project
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
package com.github.vete_l.support.gradle.project.sync.ng;

import com.intellij.openapi.util.ActionCallback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class SyncExecutionCallback extends ActionCallback {
  @Nullable private SyncAction.ProjectModels myModels;
  @Nullable private Throwable mySyncError;

  static class Factory {
    @NotNull
    SyncExecutionCallback create() {
      return new SyncExecutionCallback();
    }
  }

  @Nullable
  SyncAction.ProjectModels getModels() {
    return myModels;
  }

  void setDone(@Nullable SyncAction.ProjectModels models) {
    myModels = models;
    setDone();
  }

  @Nullable
  Throwable getSyncError() {
    return mySyncError;
  }

  void setRejected(@NotNull Throwable error) {
    mySyncError = error;
    setRejected();
  }
}
