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
package com.github.vete_l.support.gradle.structure.daemon;

import com.android.tools.idea.gradle.structure.configurables.PsContext;
import com.android.tools.idea.gradle.structure.daemon.AvailableLibraryUpdateStorage.AvailableLibraryUpdate;
import com.android.tools.idea.gradle.structure.daemon.AvailableLibraryUpdateStorage.AvailableLibraryUpdates;
import com.android.tools.idea.gradle.structure.daemon.analysis.PsAndroidModuleAnalyzer;
import com.android.tools.idea.gradle.structure.daemon.analysis.PsJavaModuleAnalyzer;
import com.android.tools.idea.gradle.structure.daemon.analysis.PsModelAnalyzer;
import com.android.tools.idea.gradle.structure.model.*;
import com.android.tools.idea.gradle.structure.model.android.PsAndroidModule;
import com.android.tools.idea.gradle.structure.model.java.PsJavaModule;
import com.android.tools.idea.gradle.structure.navigation.PsLibraryDependencyNavigationPath;
import com.android.tools.idea.gradle.structure.model.PsModulePath;
import com.android.tools.idea.gradle.structure.quickfix.PsLibraryDependencyVersionQuickFixPath;
import com.google.common.collect.Maps;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.application.RunResult;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.ActionCallback;
import com.intellij.openapi.util.Ref;
import com.intellij.util.EventDispatcher;
import com.intellij.util.ui.update.MergingUpdateQueue;
import com.intellij.util.ui.update.Update;
import org.jetbrains.annotations.NotNull;

import java.util.EventListener;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.android.tools.idea.gradle.structure.model.PsIssue.Severity.UPDATE;
import static com.android.tools.idea.gradle.structure.model.PsIssueType.LIBRARY_UPDATES_AVAILABLE;
import static com.intellij.util.ui.update.MergingUpdateQueue.ANY_COMPONENT;

public class PsAnalyzerDaemon extends PsDaemon {
  private static final Logger LOG = Logger.getInstance(PsAnalyzerDaemon.class);

  @NotNull private final MergingUpdateQueue myMainQueue;
  @NotNull private final MergingUpdateQueue myResultsUpdaterQueue;
  @NotNull private final PsIssueCollection myIssues;

  @NotNull private final Map<Class<?>, PsModelAnalyzer<?>> myModelAnalyzers = Maps.newHashMap();
  @NotNull private final AtomicBoolean myRunning = new AtomicBoolean(true);

  @NotNull private final EventDispatcher<IssuesUpdatedListener> myIssuesUpdatedEventDispatcher =
    EventDispatcher.create(IssuesUpdatedListener.class);

  public PsAnalyzerDaemon(@NotNull PsContext context, @NotNull PsLibraryUpdateCheckerDaemon libraryUpdateCheckerDaemon) {
    super(context);

    myMainQueue = createQueue("Project Structure Daemon Analyzer", null);
    myResultsUpdaterQueue = createQueue("Project Structure Analysis Results Updater", ANY_COMPONENT);
    myIssues = new PsIssueCollection(getContext());

    libraryUpdateCheckerDaemon.add(this::addApplicableUpdatesAsIssues, this);

    createModelAnalyzers();
  }


  public void recreateUpdateIssues() {
    removeIssues(LIBRARY_UPDATES_AVAILABLE);
    addApplicableUpdatesAsIssues();
  }

  private void addApplicableUpdatesAsIssues() {
    PsContext context = getContext();
    context.getProject().forEachModule(module -> {
      Ref<Boolean> updatesFound = new Ref<>(false);
      if (module instanceof PsAndroidModule) {
        PsAndroidModule androidModule = (PsAndroidModule)module;
        androidModule.forEachDeclaredDependency(dependency -> {
          if (dependency instanceof PsLibraryDependency) {
            boolean found = checkForUpdates((PsLibraryDependency)dependency);
            if (found) {
              updatesFound.set(true);
            }
          }
        });
      }
      else if (module instanceof PsJavaModule) {
        PsJavaModule javaModule = (PsJavaModule)module;
        javaModule.forEachDeclaredDependency(dependency -> {
          if (dependency instanceof PsLibraryDependency) {
            boolean found = checkForUpdates((PsLibraryDependency)dependency);
            if (found) {
              updatesFound.set(true);
            }
          }
        });
      }

      if (updatesFound.get()) {
        myResultsUpdaterQueue.queue(new IssuesComputed(module));
      }
    });
  }

  private boolean checkForUpdates(@NotNull PsLibraryDependency dependency) {
    PsContext context = getContext();
    AvailableLibraryUpdates results = context.getLibraryUpdateCheckerDaemon().getAvailableUpdates();
    PsArtifactDependencySpec spec = dependency.getDeclaredSpec();
    if (spec != null) {
      AvailableLibraryUpdate update = results.findUpdateFor(spec);
      if (update != null) {
        String text = String.format("Newer version available: <b>%1$s</b> (%2$s)", update.version, update.repository);

        PsLibraryDependencyNavigationPath mainPath = new PsLibraryDependencyNavigationPath(context, dependency);
        PsIssue issue = new PsIssue(text, mainPath, LIBRARY_UPDATES_AVAILABLE, UPDATE);
        issue.setExtraPath(new PsModulePath(dependency.getParent()));

        PsLibraryDependencyVersionQuickFixPath quickFix =
          new PsLibraryDependencyVersionQuickFixPath(dependency, update.version);
        quickFix.setHrefText("[Update]");
        issue.setQuickFixPath(quickFix);

        myIssues.add(issue);
        return true;
      }
    }
    return false;
  }

  private void createModelAnalyzers() {
    add(new PsAndroidModuleAnalyzer(getContext()));
    add(new PsJavaModuleAnalyzer(getContext()));
  }

  private void add(@NotNull PsModelAnalyzer<? extends PsModel> analyzer) {
    myModelAnalyzers.put(analyzer.getSupportedModelType(), analyzer);
  }

  public void add(@NotNull IssuesUpdatedListener listener, @NotNull Disposable parentDisposable) {
    myIssuesUpdatedEventDispatcher.addListener(listener, parentDisposable);
  }

  @Override
  public boolean isRunning() {
    return myRunning.get();
  }

  public void queueCheck(@NotNull PsModel model) {
    myMainQueue.queue(new AnalyzeStructure(model));
  }

  private void doCheck(@NotNull PsModel model) {
    myRunning.set(true);
    PsModelAnalyzer<?> analyzer = myModelAnalyzers.get(model.getClass());
    if (analyzer == null) {
      LOG.info("Failed to find analyzer for model of type " + model.getClass().getName());
      return;
    }
    RunResult<ActionCallback> result = new ReadAction<ActionCallback>() {
      @Override
      protected void run(@NotNull Result<ActionCallback> result) throws Throwable {
        if (isStopped()) {
          return;
        }
        analyzer.analyze(model, myIssues);
        result.setResult(ActionCallback.DONE);
      }
    }.execute();
    result.getResultObject().doWhenDone(() -> myResultsUpdaterQueue.queue(new IssuesComputed(model)));
  }

  @Override
  @NotNull
  protected MergingUpdateQueue getMainQueue() {
    return myMainQueue;
  }

  @Override
  @NotNull
  protected MergingUpdateQueue getResultsUpdaterQueue() {
    return myResultsUpdaterQueue;
  }

  @NotNull
  public PsIssueCollection getIssues() {
    return myIssues;
  }

  public void removeIssues(@NotNull PsIssueType type) {
    myIssues.remove(type);
    myResultsUpdaterQueue.queue(new IssuesComputed(getContext().getProject()));
  }

  private class AnalyzeStructure extends Update {
    @NotNull private final PsModel myModel;

    AnalyzeStructure(@NotNull PsModel model) {
      super(model);
      myModel = model;
    }

    @Override
    public void run() {
      try {
        doCheck(myModel);
      }
      catch (Throwable e) {
        LOG.error("Failed to analyze " + myModel, e);
      }
    }
  }

  private class IssuesComputed extends Update {
    @NotNull private final PsModel myModel;

    public IssuesComputed(@NotNull PsModel model) {
      super(model);
      myModel = model;
    }

    @Override
    public void run() {
      if (isStopped()) {
        myRunning.set(false);
        return;
      }
      myIssuesUpdatedEventDispatcher.getMulticaster().issuesUpdated(myModel);
      myRunning.set(false);
    }
  }

  public interface IssuesUpdatedListener extends EventListener {
    void issuesUpdated(@NotNull PsModel model);
  }
}
