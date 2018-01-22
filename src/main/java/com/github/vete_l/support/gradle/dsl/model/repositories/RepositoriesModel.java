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
package com.github.vete_l.support.gradle.dsl.model.repositories;

import com.android.tools.idea.gradle.dsl.model.GradleDslBlockModel;
import com.android.tools.idea.gradle.dsl.parser.elements.*;
import com.android.tools.idea.gradle.dsl.parser.repositories.FlatDirRepositoryDslElement;
import com.android.tools.idea.gradle.dsl.parser.repositories.MavenRepositoryDslElement;
import com.android.tools.idea.gradle.dsl.parser.repositories.RepositoriesDslElement;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.android.tools.idea.gradle.dsl.model.repositories.FlatDirRepositoryModel.FLAT_DIR_ATTRIBUTE_NAME;
import static com.android.tools.idea.gradle.dsl.model.repositories.GoogleDefaultRepositoryModel.GOOGLE_METHOD_NAME;
import static com.android.tools.idea.gradle.dsl.model.repositories.JCenterDefaultRepositoryModel.JCENTER_METHOD_NAME;
import static com.android.tools.idea.gradle.dsl.model.repositories.MavenCentralRepositoryModel.MAVEN_CENTRAL_METHOD_NAME;
import static com.android.tools.idea.gradle.dsl.parser.repositories.MavenRepositoryDslElement.JCENTER_BLOCK_NAME;
import static com.android.tools.idea.gradle.dsl.parser.repositories.MavenRepositoryDslElement.MAVEN_BLOCK_NAME;
import static com.android.tools.idea.gradle.dsl.parser.repositories.RepositoriesDslElement.REPOSITORIES_BLOCK_NAME;

public class RepositoriesModel extends GradleDslBlockModel {
  public RepositoriesModel(@NotNull RepositoriesDslElement dslElement) {
    super(dslElement);
  }

  @NotNull
  public List<RepositoryModel> repositories() {
    GradleDslElementList repositoriesElementList = myDslElement.getPropertyElement(REPOSITORIES_BLOCK_NAME, GradleDslElementList.class);
    if (repositoriesElementList == null) {
      return ImmutableList.of();
    }

    List<RepositoryModel> result = Lists.newArrayList();
    for (GradleDslElement element : repositoriesElementList.getElements()) {
      if (element instanceof GradleDslMethodCall) {
        if (MAVEN_CENTRAL_METHOD_NAME.equals(element.getName())) {
          result.add(new MavenCentralRepositoryModel(null));
        }
        else if (JCENTER_METHOD_NAME.equals(element.getName())) {
          result.add(new JCenterDefaultRepositoryModel());
        }
        else if (GOOGLE_METHOD_NAME.equals(element.getName())) {
          result.add(new GoogleDefaultRepositoryModel());
        }
      }
      else if (element instanceof MavenRepositoryDslElement) {
        if (MAVEN_BLOCK_NAME.equals(element.getName())) {
          result.add(new MavenRepositoryModel((MavenRepositoryDslElement)element));
        }
        else if (JCENTER_BLOCK_NAME.equals(element.getName())) {
          result.add(new JCenterRepositoryModel((MavenRepositoryDslElement)element));
        }
      }
      else if (element instanceof FlatDirRepositoryDslElement) {
        result.add(new FlatDirRepositoryModel((FlatDirRepositoryDslElement)element));
      }
      else if (element instanceof GradleDslExpressionMap) {
        if (MAVEN_CENTRAL_METHOD_NAME.equals(element.getName())) {
          result.add(new MavenCentralRepositoryModel((GradleDslExpressionMap)element));
        }
        else if (FLAT_DIR_ATTRIBUTE_NAME.equals(element.getName())) {
          result.add(new FlatDirRepositoryModel((GradlePropertiesDslElement)element));
        }
      }
    }
    return result;
  }

  /**
   * Adds a repository by method name if it is not already in the list of repositories.
   *
   * @param methodName Name of method to call.
   */
  public void addRepositoryByMethodName(@NotNull String methodName) {
    GradleDslElementList repositoriesElementList = getRepositoryElementList();
    // Check if it is already there
    if (containsMethodCall(methodName)) {
      return;
    }
    repositoriesElementList.addNewElement(new GradleDslMethodCall(repositoriesElementList, methodName, /* no statement */null));
  }

  /**
   * Looks for a repository by method name.
   *
   * @param methodName Method name of the repository
   * @return {@code true} if there is a call to {@code methodName}, {@code false} other wise.
   */
  public boolean containsMethodCall(@NotNull String methodName) {
    GradleDslElementList list = getRepositoryElementList();
    List<GradleDslMethodCall> elements = list.getElements(GradleDslMethodCall.class);
    for (GradleDslMethodCall element : elements) {
      if (methodName.equals(element.getName())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Adds a repository by url if it is not already in the list of repositories.
   *
   * @param url address to use.
   */
  public void addMavenRepositoryByUrl(@NotNull String url, @NotNull String name) {
    GradleDslElementList repositoriesElementList = getRepositoryElementList();
    // Check if it is already there
    if (containsMavenRepositoryByUrl(url)) {
      return;
    }
    MavenRepositoryDslElement newElement = new MavenRepositoryDslElement(repositoriesElementList, MAVEN_BLOCK_NAME);
    newElement.setNewLiteral("url", url);
    // name is an optional property, it can be nullable but at this point only non null values are used.
    newElement.setNewLiteral("name", name);
    repositoriesElementList.addNewElement(newElement);
  }

  /**
   * Looks for a repository by URL.
   *
   * @param repositoryUrl the URL of the repository to find.
   * @return {@code true} if there is a repository using {@code repositoryUrl} as URL, {@code false} otherwise.
   */
  public boolean containsMavenRepositoryByUrl(@NotNull String repositoryUrl) {
    GradleDslElementList list = getRepositoryElementList();
    List<MavenRepositoryDslElement> elements = list.getElements(MavenRepositoryDslElement.class);
    for (MavenRepositoryDslElement element : elements) {
      String urlElement = element.getLiteralProperty("url", String.class).value();
      if (repositoryUrl.equals(urlElement)) {
        return true;
      }
    }
    return false;
  }

  @NotNull
  private GradleDslElementList getRepositoryElementList() {
    GradleDslElementList repositoriesElementList = myDslElement.getPropertyElement(REPOSITORIES_BLOCK_NAME, GradleDslElementList.class);
    if (repositoriesElementList == null) {
      repositoriesElementList = new GradleDslElementList(myDslElement, REPOSITORIES_BLOCK_NAME);
      myDslElement.addParsedElement(REPOSITORIES_BLOCK_NAME, repositoriesElementList);
    }
    return repositoriesElementList;
  }
}
