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
package com.github.vete_l.support.gradle.structure.configurables.android.dependencies.module.treeview;

import com.android.tools.idea.gradle.structure.configurables.ui.dependencies.PsDependencyComparator;
import com.android.tools.idea.gradle.structure.configurables.android.dependencies.treeview.AndroidArtifactNode;
import com.android.tools.idea.gradle.structure.configurables.android.dependencies.treeview.ArtifactComparator;
import com.android.tools.idea.gradle.structure.configurables.ui.PsUISettings;
import com.android.tools.idea.gradle.structure.configurables.ui.treeview.AbstractPsModelNode;
import com.android.tools.idea.gradle.structure.configurables.ui.treeview.AbstractPsResettableNode;
import com.android.tools.idea.gradle.structure.model.PsDependency;
import com.android.tools.idea.gradle.structure.model.android.*;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.intellij.util.containers.SortedList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.android.builder.model.AndroidProject.ARTIFACT_MAIN;
import static com.android.tools.idea.gradle.structure.configurables.android.dependencies.module.treeview.DependencyNodes.createNodesFor;

class ResolvedDependenciesTreeRootNode extends AbstractPsResettableNode<PsAndroidModule> {
  private boolean myGroupVariants = PsUISettings.getInstance().RESOLVED_DEPENDENCIES_GROUP_VARIANTS;

  ResolvedDependenciesTreeRootNode(@NotNull PsAndroidModule module) {
    super(module);
  }

  boolean settingsChanged() {
    if (PsUISettings.getInstance().RESOLVED_DEPENDENCIES_GROUP_VARIANTS != myGroupVariants) {
      // If the "Group Variants" setting changed, remove all children nodes, so the subsequent call to "queueUpdate" will recreate them.
      myGroupVariants = PsUISettings.getInstance().RESOLVED_DEPENDENCIES_GROUP_VARIANTS;
      reset();
      return true;
    }
    return false;
  }

  @Override
  @NotNull
  protected List<? extends AbstractPsModelNode> createChildren() {
    Map<String, PsVariant> variantsByName = Maps.newHashMap();
    for (PsAndroidModule module : getModels()) {
      module.forEachVariant(variant -> variantsByName.put(variant.getName(), variant));
    }

    PsAndroidModule androidModule = getModels().get(0);
    if (myGroupVariants) {
      return createGroupedChildren(androidModule, variantsByName);
    }
    return createChildren(androidModule, variantsByName);
  }

  @NotNull
  private List<? extends AbstractPsModelNode> createGroupedChildren(@NotNull PsAndroidModule module,
                                                                    @NotNull Map<String, PsVariant> variantsByName) {
    Map<String, List<PsDependencyContainer>> containersWithMainArtifactByVariant = Maps.newHashMap();

    Map<List<PsDependencyContainer>, List<PsDependency>> groupedDependencies = groupDependencies(module);
    for (List<PsDependencyContainer> containers : groupedDependencies.keySet()) {
      for (PsDependencyContainer container : containers) {
        if (container.getArtifact().endsWith(ARTIFACT_MAIN)) {
          containersWithMainArtifactByVariant.put(container.getVariant(), containers);
          break;
        }
      }
    }

    List<AndroidArtifactNode> children = Lists.newArrayList();

    for (List<PsDependencyContainer> containers : groupedDependencies.keySet()) {
      List<PsAndroidArtifact> groupArtifacts = extractArtifacts(containers, variantsByName);

      AndroidArtifactNode mainArtifactNode = null;
      if (!containersWithMainArtifactByVariant.values().contains(containers)) {
        // This is a node for "Unit Test" or "Android Test"
        if (containers.size() == 1) {
          // This is not a group. Create the "main" artifact node for the same variant
          PsDependencyContainer container = containers.get(0);
          String variantName = container.getVariant();
          PsVariant variant = variantsByName.get(variantName);
          assert variant != null;
          PsAndroidArtifact mainArtifact = variant.findArtifact(ARTIFACT_MAIN);
          if (mainArtifact != null) {
            List<PsDependencyContainer> mainArtifactContainers = containersWithMainArtifactByVariant.get(variantName);
            if (mainArtifactContainers != null) {
              List<PsDependency> mainArtifactDependencies = groupedDependencies.get(mainArtifactContainers);
              mainArtifactNode = createArtifactNode(mainArtifact, mainArtifactDependencies, null);
            }
          }
        }
        else {
          // Create the node that contains all the containers with "main" artifacts
          for (PsDependencyContainer container : containers) {
            List<PsDependencyContainer> mainArtifactContainers = containersWithMainArtifactByVariant.get(container.getVariant());
            if (mainArtifactContainers != null) {
              List<PsAndroidArtifact> mainArtifacts = extractArtifacts(mainArtifactContainers, variantsByName);
              mainArtifactNode = createArtifactNode(mainArtifacts, groupedDependencies.get(mainArtifactContainers), null);
              break;
            }
          }
        }
      }

      Collections.sort(groupArtifacts, ArtifactComparator.INSTANCE);
      AndroidArtifactNode artifactNode = createArtifactNode(groupArtifacts, groupedDependencies.get(containers), mainArtifactNode);
      if (artifactNode != null) {
        children.add(artifactNode);
      }
    }

    Collections.sort(children, (a1, a2) -> a1.getName().compareTo(a2.getName()));
    return children;
  }

  @VisibleForTesting
  @NotNull
  static Map<List<PsDependencyContainer>, List<PsDependency>> groupDependencies(@NotNull PsAndroidModule module) {
    Map<PsDependencyContainer, List<PsDependency>> dependenciesByContainer = Maps.newHashMap();

    // Key: variant name
    Map<String, PsDependencyContainer> containerWithMainArtifact = Maps.newHashMap();

    module.forEachDependency(dependency -> {
      Collection<PsDependencyContainer> containers = dependency.getContainers();
      for (PsDependencyContainer container : containers) {
        if (container.getArtifact().equals(ARTIFACT_MAIN)) {
          containerWithMainArtifact.put(container.getVariant(), container);
        }
        List<PsDependency> containerDependencies = dependenciesByContainer.get(container);
        if (containerDependencies == null) {
          containerDependencies = new SortedList<>(PsDependencyComparator.INSTANCE);
          dependenciesByContainer.put(container, containerDependencies);
        }
        containerDependencies.add(dependency);
      }
    });

    List<List<PsDependencyContainer>> containerGroups = Lists.newArrayList();
    List<PsDependencyContainer> containers = Lists.newArrayList(dependenciesByContainer.keySet());

    List<PsDependencyContainer> currentGroup = Lists.newArrayList();
    while (!containers.isEmpty()) {
      PsDependencyContainer container1 = containers.get(0);
      currentGroup.add(container1);

      if (containers.size() > 1) {
        for (int i =  1; i < containers.size(); i++) {
          PsDependencyContainer container2 = containers.get(i);
          if (haveSameDependencies(container1, container2, dependenciesByContainer)) {
            if (containerWithMainArtifact.values().contains(container1)) {
              // This is "main" artifact, no need to check any further
              currentGroup.add(container2);
            }
            else {
              // Check that the "main" artifacts in these variants are also similar.
              PsDependencyContainer mainArtifactContainer1 = containerWithMainArtifact.get(container1.getVariant());
              PsDependencyContainer mainArtifactContainer2 = containerWithMainArtifact.get(container2.getVariant());

              if (mainArtifactContainer1 == null && mainArtifactContainer2 == null) {
                currentGroup.add(container2);
              }
              if (mainArtifactContainer1 != null &&
                  mainArtifactContainer2 != null &&
                  haveSameDependencies(mainArtifactContainer1, mainArtifactContainer2, dependenciesByContainer)) {
                currentGroup.add(container2);
              }
            }
          }
        }
      }
      containerGroups.add(currentGroup);

      containers.removeAll(currentGroup);
      currentGroup = Lists.newArrayList();
    }

    Map<List<PsDependencyContainer>, List<PsDependency>> dependenciesByContainers = Maps.newHashMap();
    for (List<PsDependencyContainer> group : containerGroups) {
      PsDependencyContainer container = group.get(0);
      dependenciesByContainers.put(group, dependenciesByContainer.get(container));
    }
    return dependenciesByContainers;
  }

  private static boolean haveSameDependencies(@NotNull PsDependencyContainer c1,
                                              @NotNull PsDependencyContainer c2,
                                              @NotNull Map<PsDependencyContainer, List<PsDependency>> dependenciesByContainer) {
    if (c1.getArtifact().equals(c2.getArtifact())) {
      List<PsDependency> d1 = dependenciesByContainer.get(c1);
      List<PsDependency> d2 = dependenciesByContainer.get(c2);
      return d1.equals(d2);
    }
    return false;
  }

  @NotNull
  private static List<PsAndroidArtifact> extractArtifacts(@NotNull List<PsDependencyContainer> containers,
                                                          @NotNull Map<String, PsVariant> variantsByName) {
    List<PsAndroidArtifact> groupArtifacts = Lists.newArrayList();
    for (PsDependencyContainer container : containers) {
      PsAndroidArtifact foundArtifact = extractArtifact(container, variantsByName);
      groupArtifacts.add(foundArtifact);
    }
    return groupArtifacts;
  }

  @Nullable
  private static PsAndroidArtifact extractArtifact(@NotNull PsDependencyContainer container,
                                                   @NotNull Map<String, PsVariant> variantsByName) {
    PsVariant variant = variantsByName.get(container.getVariant());
    assert variant != null;
    PsAndroidArtifact artifact = variant.findArtifact(container.getArtifact());
    assert artifact != null;
    return artifact;
  }

  @Nullable
  private AndroidArtifactNode createArtifactNode(@NotNull List<PsAndroidArtifact> artifacts,
                                                 @NotNull List<PsDependency> dependencies,
                                                 @Nullable AndroidArtifactNode mainArtifactNode) {
    if (!dependencies.isEmpty() || mainArtifactNode != null) {
      AndroidArtifactNode artifactNode = new AndroidArtifactNode(this, artifacts);
      populate(artifactNode, dependencies, mainArtifactNode);
      return artifactNode;
    }
    return null;
  }

  @NotNull
  private List<? extends AndroidArtifactNode> createChildren(@NotNull PsAndroidModule module,
                                                             @NotNull Map<String, PsVariant> variantsByName) {
    List<AndroidArtifactNode> childrenNodes = Lists.newArrayList();

    // [Outer map] key: variant name, value: dependencies by artifact
    // [Inner map] key: artifact name, value: dependencies
    Map<String, Map<String, List<PsDependency>>> dependenciesByVariantAndArtifact = Maps.newHashMap();

    module.forEachDependency(dependency -> {
      if (!dependency.isDeclared()) {
        return; // Only show "declared" dependencies as top-level dependencies.
      }
      for (PsDependencyContainer container : dependency.getContainers()) {
        Map<String, List<PsDependency>> dependenciesByArtifact =
          dependenciesByVariantAndArtifact.get(container.getVariant());

        if (dependenciesByArtifact == null) {
          dependenciesByArtifact = Maps.newHashMap();
          dependenciesByVariantAndArtifact.put(container.getVariant(), dependenciesByArtifact);
        }

        List<PsDependency> artifactDependencies = dependenciesByArtifact.get(container.getArtifact());
        if (artifactDependencies == null) {
          artifactDependencies = Lists.newArrayList();
          dependenciesByArtifact.put(container.getArtifact(), artifactDependencies);
        }

        artifactDependencies.add(dependency);
      }
    });

    List<String> variantNames = Lists.newArrayList(dependenciesByVariantAndArtifact.keySet());
    Collections.sort(variantNames);

    for (String variantName : variantNames) {
      PsVariant variant = variantsByName.get(variantName);

      Map<String, List<PsDependency>> dependenciesByArtifact = dependenciesByVariantAndArtifact.get(variantName);

      if (dependenciesByArtifact != null) {
        List<String> artifactNames = Lists.newArrayList(dependenciesByArtifact.keySet());
        //noinspection TestOnlyProblems
        Collections.sort(artifactNames, ArtifactComparator.byName());

        for (String artifactName : artifactNames) {
          PsAndroidArtifact artifact = variant.findArtifact(artifactName);
          assert artifact != null;

          AndroidArtifactNode mainArtifactNode = null;
          String mainArtifactName = ARTIFACT_MAIN;
          if (!mainArtifactName.equals(artifactName)) {
            // Add "main" artifact as a dependency of "unit test" or "android test" artifact.
            PsAndroidArtifact mainArtifact = variant.findArtifact(mainArtifactName);
            if (mainArtifact != null) {
              List<PsDependency> artifactDependencies = dependenciesByArtifact.get(mainArtifactName);
              if (artifactDependencies == null) {
                artifactDependencies = Collections.emptyList();
              }
              mainArtifactNode = createArtifactNode(mainArtifact, artifactDependencies, null);
            }
          }

          AndroidArtifactNode artifactNode = createArtifactNode(artifact, dependenciesByArtifact.get(artifactName), mainArtifactNode);
          if (artifactNode != null) {
            childrenNodes.add(artifactNode);
          }
        }
      }
    }

    return childrenNodes;
  }

  @Nullable
  private AndroidArtifactNode createArtifactNode(@NotNull PsAndroidArtifact artifact,
                                                 @NotNull List<PsDependency> dependencies,
                                                 @Nullable AndroidArtifactNode mainArtifactNode) {
    if (!dependencies.isEmpty() || mainArtifactNode != null) {
      AndroidArtifactNode artifactNode = new AndroidArtifactNode(this, artifact);
      populate(artifactNode, dependencies, mainArtifactNode);
      return artifactNode;
    }
    return null;
  }

  private static void populate(@NotNull AndroidArtifactNode artifactNode,
                               @NotNull List<PsDependency> dependencies,
                               @Nullable AndroidArtifactNode mainArtifactNode) {
    List<AbstractPsModelNode<?>> children = createNodesFor(artifactNode, dependencies);
    if (mainArtifactNode != null) {
      children.add(0, mainArtifactNode);
    }
    artifactNode.setChildren(children);
  }

}
