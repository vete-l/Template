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
package com.github.vete_l.support.gradle.dsl.parser.elements;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElement;
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElementFactory;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrStatement;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrApplicationStatement;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.path.GrMethodCallExpression;

import java.io.File;
import java.util.Collection;
import java.util.List;

/**
 * Represents a {@link GrMethodCallExpression} element.
 */
public final class GradleDslMethodCall extends GradleDslExpression {
  private final
  @NotNull List<GradleDslElement> myArguments = Lists.newArrayList();
  private final
  @NotNull List<GradleDslElement> myToBeRemovedArguments = Lists.newArrayList();

  @Nullable private String myStatementName;

  private GradleDslElement myToBeAddedArgument;

  /**
   * Create a new method call.
   *
   * @param parent        the parent element.
   * @param methodName    method name.
   * @param statementName the statement name this method call need to be added to,  Ex: to create "compile project(':xyz')",
   *                      use "compile" as statement name and "project" as method name, or {@code null} if the method needs to be added
   *                      without any application statement.
   */
  public GradleDslMethodCall(@NotNull GradleDslElement parent, @NotNull String methodName, @Nullable String statementName) {
    super(parent, null, methodName, null);
    myStatementName = statementName;
  }

  public GradleDslMethodCall(@NotNull GradleDslElement parent,
                             @NotNull GrMethodCallExpression methodCall,
                             @NotNull String name) {
    super(parent, methodCall, name, methodCall);
  }

  public void addParsedExpression(@NotNull GradleDslExpression expression) {
    expression.myParent = this;
    myArguments.add(expression);
  }

  public void addParsedExpressionMap(@NotNull GradleDslExpressionMap expressionMap) {
    expressionMap.myParent = this;
    myArguments.add(expressionMap);
  }

  public void addNewArgument(@NotNull GradleDslExpression argument) {
    addNewArgumentInternal(argument);
  }

  public void addNewArgument(@NotNull GradleDslExpressionMap mapArgument) {
    addNewArgumentInternal(mapArgument);
  }

  private void addNewArgumentInternal(@NotNull GradleDslElement argument) {
    assert argument instanceof GradleDslExpression || argument instanceof GradleDslExpressionMap;
    // Only adding expression or map arguments to an empty method is supported.
    // The other combinations are not supported as there is no real use case.
    if (getArguments().isEmpty()) {
      myToBeAddedArgument = argument;
      setModified(true);
    }
  }

  @NotNull
  public List<GradleDslElement> getArguments() {
    if (myToBeRemovedArguments.isEmpty() && myToBeAddedArgument == null) {
      return ImmutableList.copyOf(myArguments);
    }

    List<GradleDslElement> result = Lists.newArrayList();

    for (GradleDslElement argument : myArguments) {
      if (argument instanceof GradleDslReference) {
        // See if the reference is pointing to a list or map.
        GradleDslExpressionList listValue = ((GradleDslReference)argument).getValue(GradleDslExpressionList.class);
        if (listValue != null) {
          result.addAll(listValue.getExpressions());
          continue;
        }

        GradleDslExpressionMap mapValue = ((GradleDslReference)argument).getValue(GradleDslExpressionMap.class);
        if (mapValue != null) {
          result.add(mapValue);
          continue;
        }
      }
      result.add(argument);
    }

    if (myToBeAddedArgument != null) {
      result.add(myToBeAddedArgument);
    }

    for (GradleDslElement argument : myToBeRemovedArguments) {
      result.remove(argument);
    }

    return result;
  }

  @Override
  @NotNull
  protected Collection<GradleDslElement> getChildren() {
    return getArguments();
  }

  @Nullable
  @Override
  public Object getValue() {
    GroovyPsiElement psiElement = getPsiElement();
    return psiElement != null ? psiElement.getText() : null;
  }

  @Nullable
  @Override
  public <T> T getValue(@NotNull Class<T> clazz) {
    if (clazz.isAssignableFrom(File.class)) {
      return clazz.cast(getFileValue());
    }
    Object value = getValue();
    if (clazz.isInstance(value)) {
      return clazz.cast(value);
    }
    return null;
  }

  @Nullable
  private File getFileValue() {
    if (!myName.equals("file")) {
      return null;
    }

    List<GradleDslElement> arguments = getArguments();
    if (arguments.isEmpty()) {
      return null;
    }

    GradleDslElement pathArgument = arguments.get(0);
    if (!(pathArgument instanceof GradleDslExpression)) {
      return null;
    }

    String path = ((GradleDslExpression)pathArgument).getValue(String.class);
    if (path == null) {
      return null;
    }

    return new File(path);
  }

  @Override
  public void setValue(@NotNull Object value) {
    if (value instanceof File) {
      setFileValue((File)value);
    }
    // TODO: Add support to set the full method definition as a String.
  }

  private void setFileValue(@NotNull File file) {
    if (!myName.equals("file")) {
      return;
    }

    List<GradleDslElement> arguments = getArguments();
    if (arguments.isEmpty()) {
      GradleDslLiteral argument = new GradleDslLiteral(this, myName);
      argument.setValue(file.getPath());
      myToBeAddedArgument = argument;
      return;
    }

    GradleDslElement pathArgument = arguments.get(0);
    if (!(pathArgument instanceof GradleDslExpression)) {
      return;
    }

    ((GradleDslExpression)pathArgument).setValue(file.getPath());
  }

  public void remove(GradleDslElement argument) {
    if (myArguments.contains(argument)) {
      myToBeRemovedArguments.add(argument);
      setModified(true);
    }
  }

  @Override
  protected void apply() {
    for (GradleDslElement argument : myToBeRemovedArguments) {
      if (myArguments.remove(argument)) {
        argument.delete();
      }
    }

    GroovyPsiElement psiElement = getPsiElement();
    if (psiElement instanceof GrMethodCallExpression) {
      GrMethodCallExpression methodCall = (GrMethodCallExpression)psiElement;
      if (myToBeAddedArgument != null) {
        myToBeAddedArgument.setPsiElement(methodCall.getArgumentList());
        myToBeAddedArgument.applyChanges();
        myArguments.add(myToBeAddedArgument);
      }
    }

    for (GradleDslElement argument : myArguments) {
      if (argument.isModified()) {
        argument.applyChanges();
      }
    }
  }

  @Override
  protected void reset() {
    myToBeAddedArgument = null;
    myToBeRemovedArguments.clear();
    for (GradleDslElement argument : myArguments) {
      if (argument.isModified()) {
        argument.resetState();
      }
    }
  }

  @Override
  @Nullable
  public GroovyPsiElement create() {
    GroovyPsiElement psiElement = getPsiElement();
    if (psiElement != null) {
      return psiElement;
    }

    if (myParent == null) {
      return null;
    }

    GroovyPsiElement parentPsiElement = myParent.create();
    if (parentPsiElement == null) {
      return null;
    }

    GroovyPsiElementFactory factory = GroovyPsiElementFactory.getInstance(parentPsiElement.getProject());
    String statementText = (myStatementName != null ? myStatementName + " " : "") + myName + "()";
    GrStatement statement = factory.createStatementFromText(statementText);
    PsiElement addedElement = parentPsiElement.addBefore(statement, parentPsiElement.getLastChild());

    if (addedElement instanceof GrApplicationStatement) {
      GrExpression[] expressionArguments = ((GrApplicationStatement)addedElement).getArgumentList().getExpressionArguments();
      if (expressionArguments.length == 1 && expressionArguments[0] instanceof GrMethodCallExpression) {
        setPsiElement(expressionArguments[0]);
        return getPsiElement();
      }
    }

    if (addedElement instanceof GrMethodCallExpression) {
      setPsiElement((GrMethodCallExpression)addedElement);
      return getPsiElement();
    }

    return null;
  }
}
