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
package com.github.vete_l.support.gradle.project.facet.gradle;

import com.android.tools.idea.IdeInfo;
import com.intellij.facet.FacetConfiguration;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.facet.ui.FacetValidatorsManager;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import static com.intellij.openapi.util.text.StringUtil.isNotEmpty;
import static com.intellij.util.xmlb.XmlSerializer.deserializeInto;
import static com.intellij.util.xmlb.XmlSerializer.serializeInto;

/**
 * Configuration options for the Android-Gradle facet. In Android Studio, these options <em>cannot</em> be directly changed by users.
 */
public class GradleFacetConfiguration implements FacetConfiguration {
  @NonNls public String GRADLE_PROJECT_PATH;

  @NotNull
  @Override
  public FacetEditorTab[] createEditorTabs(FacetEditorContext editorContext,
                                           FacetValidatorsManager validatorsManager) {
    if (!IdeInfo.getInstance().isAndroidStudio() && isNotEmpty(GRADLE_PROJECT_PATH)) {
      // IntelliJ only
      return new FacetEditorTab[]{new GradleFacetEditorTab(editorContext.getProject(), GRADLE_PROJECT_PATH)};
    }
    return new FacetEditorTab[0];
  }

  @Override
  public void readExternal(Element element) throws InvalidDataException {
    deserializeInto(this, element);
  }

  @Override
  public void writeExternal(Element element) throws WriteExternalException {
    serializeInto(this, element);
  }
}