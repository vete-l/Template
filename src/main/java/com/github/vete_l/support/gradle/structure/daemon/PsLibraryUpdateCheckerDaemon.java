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

import com.android.ide.common.repository.GradleVersion;
import com.android.tools.idea.gradle.structure.configurables.PsContext;
import com.android.tools.idea.gradle.structure.daemon.AvailableLibraryUpdateStorage.AvailableLibraryUpdates;
import com.android.tools.idea.gradle.structure.model.PsArtifactDependencySpec;
import com.android.tools.idea.gradle.structure.model.PsLibraryDependency;
import com.android.tools.idea.gradle.structure.model.android.PsAndroidModule;
import com.android.tools.idea.gradle.structure.model.repositories.search.ArtifactRepository;
import com.android.tools.idea.gradle.structure.model.repositories.search.FoundArtifact;
import com.android.tools.idea.gradle.structure.model.repositories.search.SearchRequest;
import com.android.tools.idea.gradle.structure.model.repositories.search.SearchResult;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.Futures;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.EventDispatcher;
import com.intellij.util.ui.update.MergingUpdateQueue;
import com.intellij.util.ui.update.Update;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.EventListener;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.intellij.openapi.util.text.StringUtil.isNotEmpty;
import static com.intellij.util.ui.update.MergingUpdateQueue.ANY_COMPONENT;

public class PsLibraryUpdateCheckerDaemon extends PsDaemon {
  @NotNull private final MergingUpdateQueue myMainQueue;
  @NotNull private final MergingUpdateQueue myResultsUpdaterQueue;

  @NotNull private final AtomicBoolean myRunning = new AtomicBoolean(true);

  @NotNull private final EventDispatcher<AvailableUpdatesListener> myEventDispatcher =
    EventDispatcher.create(AvailableUpdatesListener.class);

  public PsLibraryUpdateCheckerDaemon(@NotNull PsContext context) {
    super(context);
    myMainQueue = createQueue("Project Structure Daemon Update Checker", null);
    myResultsUpdaterQueue = createQueue("Project Structure Available Update Results Updater", ANY_COMPONENT);
  }

  public void queueAutomaticUpdateCheck() {
    long searchTimeMillis = getAvailableUpdates().lastSearchTimeMillis;
    if (searchTimeMillis > 0) {
      long elapsed = System.currentTimeMillis() - searchTimeMillis;
      long daysPastSinceLastUpdate = TimeUnit.MILLISECONDS.toDays(elapsed);
      if (daysPastSinceLastUpdate < 3) {
        // Display stored updates from previous search
        myResultsUpdaterQueue.queue(new UpdatesAvailable());
        return;
      }
    }
    queueUpdateCheck();
  }

  public void queueUpdateCheck() {
    myMainQueue.queue(new SearchForAvailableUpdates());
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
  public AvailableLibraryUpdates getAvailableUpdates() {
    Project project = getContext().getProject().getResolvedModel();
    return AvailableLibraryUpdateStorage.getInstance(project).getState();
  }

  public void add(@NotNull AvailableUpdatesListener listener, @NotNull Disposable parentDisposable) {
    myEventDispatcher.addListener(listener, parentDisposable);
  }

  @Override
  public boolean isRunning() {
    return myRunning.get();
  }

  private void search(@NotNull Collection<ArtifactRepository> repositories,
                      @NotNull Collection<LibraryUpdateId> ids) {
    myRunning.set(true);
    getAvailableUpdates().clear();

    int resultCount = repositories.size() * ids.size();
    List<Future<SearchResult>> jobs = Lists.newArrayListWithExpectedSize(resultCount);

    Set<SearchRequest> requests = Sets.newHashSet();
    ids.forEach(id -> {
      SearchRequest request = new SearchRequest(id.getName(), id.getGroupId(), 1, 0);
      requests.add(request);
    });

    Set<SearchResult> results = Sets.newHashSet();
    List<Exception> errors = Lists.newArrayList();

    Application application = ApplicationManager.getApplication();
    application.executeOnPooledThread(() -> {
      for (ArtifactRepository repository : repositories) {
        for (SearchRequest request : requests) {
          jobs.add(application.executeOnPooledThread(() -> repository.search(request)));
        }
      }

      for (Future<SearchResult> job : jobs) {
        try {
          SearchResult result = Futures.getChecked(job, Exception.class);
          List<FoundArtifact> artifacts = result.getArtifacts();
          if (artifacts.size() == 1) {
            FoundArtifact artifact = artifacts.get(0);
            if (!artifact.getVersions().isEmpty()) {
              results.add(result);
            }
          }
        }
        catch (Exception e) {
          errors.add(e);
        }
      }

      AvailableLibraryUpdates updates = getAvailableUpdates();

      for (SearchResult result : results) {
        List<FoundArtifact> artifacts = result.getArtifacts();
        updates.add(artifacts.get(0));
      }

      updates.lastSearchTimeMillis = System.currentTimeMillis();

      myResultsUpdaterQueue.queue(new UpdatesAvailable());
    });
  }

  private class SearchForAvailableUpdates extends Update {
    public SearchForAvailableUpdates() {
      super(getContext().getProject());
    }

    @Override
    public void run() {
      Set<ArtifactRepository> repositories = Sets.newHashSet();
      Set<LibraryUpdateId> ids = Sets.newHashSet();

      getContext().getProject().forEachModule(module -> {
        repositories.addAll(module.getArtifactRepositories());

        if (module instanceof PsAndroidModule) {
          PsAndroidModule androidModule = (PsAndroidModule)module;

          androidModule.forEachDeclaredDependency(dependency -> {
            if (dependency instanceof PsLibraryDependency) {
              PsLibraryDependency libraryDependency = (PsLibraryDependency)dependency;
              PsArtifactDependencySpec spec = libraryDependency.getDeclaredSpec();
              if (spec != null && isNotEmpty(spec.getVersion())) {
                GradleVersion version = GradleVersion.tryParse(spec.getVersion());
                if (version != null) {
                  ids.add(new LibraryUpdateId(spec.getName(), spec.getGroup()));
                }
              }
            }
          });
        }
      });

      if (!repositories.isEmpty() && !ids.isEmpty()) {
        search(repositories, ids);
      }
    }
  }

  private class UpdatesAvailable extends Update {
    public UpdatesAvailable() {
      super(getContext().getProject());
    }

    @Override
    public void run() {
      myEventDispatcher.getMulticaster().availableUpdates();
      myRunning.set(false);
    }
  }

  public interface AvailableUpdatesListener extends EventListener {
    void availableUpdates();
  }
}
