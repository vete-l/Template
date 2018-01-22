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
package com.github.vete_l.support.gradle.structure.configurables;

import com.android.tools.idea.structure.dialog.CounterDisplayConfigurable;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.Disposer;
import com.intellij.util.EventDispatcher;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public abstract class AbstractCounterDisplayConfigurable extends JPanel implements Configurable, CounterDisplayConfigurable, Disposable {
  @NotNull private final PsContext myContext;

  private final EventDispatcher<CountChangeListener> myEventDispatcher = EventDispatcher.create(CountChangeListener.class);

  protected AbstractCounterDisplayConfigurable(@NotNull PsContext context) {
    super(new BorderLayout());
    myContext = context;
  }

  @NotNull
  protected PsContext getContext() {
    return myContext;
  }

  protected void fireCountChangeListener() {
    myEventDispatcher.getMulticaster().countChanged();
  }

  @Override
  public void add(@NotNull CountChangeListener listener, @NotNull Disposable parentDisposable) {
    myEventDispatcher.addListener(listener, parentDisposable);
  }

  @Override
  public void dispose() {
    Disposer.dispose(this);
  }

  @Override
  @NotNull
  public JComponent createComponent() {
    return this;
  }

  @Override
  public boolean isModified() {
    return false;
  }

  @Override
  public void apply() throws ConfigurationException {
  }

  @Override
  public void disposeUIResources() {
    Disposer.dispose(this);
  }
}
