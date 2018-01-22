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
package com.github.vete_l.support.gradle.dsl.model;

import com.android.tools.idea.gradle.dsl.parser.GradleDslFile;
import com.android.tools.idea.gradle.dsl.parser.elements.GradleDslElement;
import com.android.tools.idea.gradle.dsl.parser.elements.GradleDslExpression;
import com.google.common.collect.ImmutableList;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;

import static com.android.tools.idea.gradle.util.PropertiesFiles.getProperties;

/**
 * Represents a gradle.properties file.
 */
public class GradlePropertiesModel extends GradleFileModel {
  private static final Logger LOG = Logger.getInstance(GradlePropertiesModel.class);

  @Nullable
  public static GradlePropertiesModel parsePropertiesFile(@NotNull VirtualFile file, @NotNull Project project, @NotNull String moduleName) {
    File propertiesFile = VfsUtilCore.virtualToIoFile(file);
    try {
      Properties properties = getProperties(propertiesFile);
      GradlePropertiesFile gradlePropertiesFile = new GradlePropertiesFile(properties, file, project, moduleName);
      return new GradlePropertiesModel(gradlePropertiesFile);
    }
    catch (IOException e) {
      LOG.warn("Failed to process " + file.getPath(), e);
      return null;
    }
  }

  private GradlePropertiesModel(@NotNull GradleDslFile gradleDslFile) {
    super(gradleDslFile);
  }

  private static final class GradlePropertiesFile extends GradleDslFile {
    private final Properties myProperties;

    private GradlePropertiesFile(@NotNull Properties properties,
                                 @NotNull VirtualFile file,
                                 @NotNull Project project,
                                 @NotNull String moduleName) {
      super(file, project, moduleName);
      myProperties = properties;
    }

    @Override
    public void parse() {
      // There is nothing to parse in a properties file as it's just a java properties file.
    }

    @Override
    @Nullable
    public GradleDslExpression getPropertyElement(@NotNull String property) {
      String value = myProperties.getProperty(property);
      if (value == null) {
        return null;
      }

      GradlePropertyElement propertyElement = new GradlePropertyElement(this, property);
      propertyElement.setValue(value);
      return  propertyElement;
    }
  }

  private static class GradlePropertyElement extends GradleDslExpression {
    @Nullable private Object myValue;

    private GradlePropertyElement(@Nullable GradleDslElement parent, @NotNull String name) {
      super(parent, null, name, null);
    }

    @Nullable
    @Override
    public Object getValue() {
      return myValue;
    }

    @Nullable
    @Override
    public <T> T getValue(@NotNull Class<T> clazz) {
      Object value = getValue();
      if (clazz.isInstance(value)) {
        return clazz.cast(value);
      }
      return null;
    }

    @Override
    public void setValue(@NotNull Object value) {
      myValue = value;
    }

    @NotNull
    @Override
    protected Collection<GradleDslElement> getChildren() {
      return ImmutableList.of();
    }

    @Override
    protected void apply() {
      // There is nothing to apply here as this is just a dummy dsl element to represent a property.
    }

    @Override
    protected void reset() {
      // There is nothing to reset here as this is just a dummy dsl element to represent a property.
    }
  }
}
