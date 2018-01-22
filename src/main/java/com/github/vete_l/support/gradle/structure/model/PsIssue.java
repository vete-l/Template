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
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

import static com.intellij.icons.AllIcons.Actions.Download;
import static com.intellij.icons.AllIcons.General.*;
import static com.intellij.ui.JBColor.*;

public final class PsIssue {
  @NotNull private final String myText;
  @NotNull private final PsPath myPath;
  @NotNull private final PsIssueType myType;
  @NotNull private final Severity mySeverity;

  @Nullable private final String myDescription;
  @Nullable private PsPath myExtraPath;
  @Nullable private PsPath myQuickFixPath;

  public PsIssue(@NotNull String text, @NotNull PsPath path, @NotNull PsIssueType type, @NotNull Severity severity) {
    myText = text;
    myPath = path;
    myType = type;
    mySeverity = severity;
    myDescription = null;
  }

  public PsIssue(@NotNull String text,
                 @NotNull String description,
                 @NotNull PsPath path,
                 @NotNull PsIssueType type,
                 @NotNull Severity severity) {
    myText = text;
    myDescription = description;
    myPath = path;
    myType = type;
    mySeverity = severity;
  }

  @NotNull
  public String getText() {
    return myText;
  }

  @Nullable
  public String getDescription() {
    return myDescription;
  }

  @NotNull
  public Severity getSeverity() {
    return mySeverity;
  }

  @NotNull
  public PsPath getPath() {
    return myPath;
  }

  @Nullable
  public PsPath getExtraPath() {
    return myExtraPath;
  }

  public void setExtraPath(@Nullable PsPath extraPath) {
    myExtraPath = extraPath;
  }

  @Nullable
  public PsPath getQuickFixPath() {
    return myQuickFixPath;
  }

  public void setQuickFixPath(@Nullable PsPath quickFixPath) {
    myQuickFixPath = quickFixPath;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PsIssue that = (PsIssue)o;
    return Objects.equal(myText, that.myText)
           && Objects.equal(myDescription, that.myDescription)
           && Objects.equal(myPath, that.myPath)
           && Objects.equal(myType, that.myType)
           && mySeverity == that.mySeverity;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(myText, myDescription, myPath, myType, mySeverity);
  }

  @Override
  public String toString() {
    return mySeverity.name() + ": " + myText;
  }

  @NotNull
  public PsIssueType getType() {
    return myType;
  }

  public enum Severity {
    ERROR("Error", BalloonError, RED, 0),
    WARNING("Warning", BalloonWarning, YELLOW, 1),
    INFO("Information", BalloonInformation, GRAY, 2),
    UPDATE("Update", Download, GRAY, 3);

    @NotNull private final String myText;
    @NotNull private final Icon myIcon;
    @NotNull private final Color myColor;
    private final int myPriority;

    Severity(@NotNull String text, @NotNull Icon icon, @NotNull Color color, int priority) {
      myText = text;
      myColor = color;
      myIcon = icon;
      myPriority = priority;
    }

    @NotNull
    public String getText() {
      return myText;
    }

    @NotNull
    public Icon getIcon() {
      return myIcon;
    }

    @NotNull
    public Color getColor() {
      return myColor;
    }

    public int getPriority() {
      return myPriority;
    }
  }
}
