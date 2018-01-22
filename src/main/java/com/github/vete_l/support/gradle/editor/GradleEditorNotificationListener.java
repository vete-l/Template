/*
 * Copyright (C) 2015 The Android Open Source Project
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
package com.github.vete_l.support.gradle.editor;

import com.intellij.util.messages.Topic;

public interface GradleEditorNotificationListener {

  Topic<GradleEditorNotificationListener> TOPIC =
    Topic.create("Document/PSI notification via enhanced gradle editor", GradleEditorNotificationListener.class);

  /**
   * Is expected to be called just before document/PSI modification via enhanced gradle editor begins.
   */
  void beforeChange();

  /**
   * Is expected to be called just after document/PSI modification via enhanced gradle editor ends.
   */
  void afterChange();
}
