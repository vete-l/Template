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
package com.github.vete_l.support.gradle.structure.configurables.android.dependencies.module;

import com.android.tools.idea.gradle.structure.configurables.PsContext;
import com.android.tools.idea.gradle.structure.configurables.android.dependencies.module.treeview.DependencySelection;
import com.android.tools.idea.gradle.structure.configurables.android.dependencies.module.treeview.ResolvedDependenciesTreeBuilder;
import com.android.tools.idea.gradle.structure.configurables.android.dependencies.treeview.*;
import com.android.tools.idea.gradle.structure.configurables.ui.SelectionChangeEventDispatcher;
import com.android.tools.idea.gradle.structure.configurables.ui.SelectionChangeListener;
import com.android.tools.idea.gradle.structure.configurables.ui.ToolWindowPanel;
import com.android.tools.idea.gradle.structure.configurables.ui.treeview.AbstractBaseCollapseAllAction;
import com.android.tools.idea.gradle.structure.configurables.ui.treeview.AbstractBaseExpandAllAction;
import com.android.tools.idea.gradle.structure.configurables.ui.treeview.AbstractPsModelNode;
import com.android.tools.idea.gradle.structure.configurables.ui.treeview.NodeHyperlinkSupport;
import com.android.tools.idea.gradle.structure.model.android.PsAndroidDependency;
import com.android.tools.idea.gradle.structure.model.android.PsAndroidModule;
import com.android.tools.idea.gradle.structure.model.android.PsModuleAndroidDependency;
import com.google.common.collect.Lists;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.treeStructure.Tree;
import icons.AndroidIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Set;

import static com.android.tools.idea.gradle.structure.configurables.ui.UiUtil.setUp;
import static com.intellij.util.containers.ContainerUtil.getFirstItem;
import static java.awt.event.MouseEvent.MOUSE_PRESSED;

public class ResolvedDependenciesPanel extends ToolWindowPanel implements DependencySelection {
  @NotNull private final Tree myTree;
  @NotNull private final ResolvedDependenciesTreeBuilder myTreeBuilder;
  @NotNull private final PsContext myContext;
  @NotNull private final NodeHyperlinkSupport<ModuleDependencyNode> myHyperlinkSupport;

  @NotNull private final SelectionChangeEventDispatcher<PsAndroidDependency> myEventDispatcher = new SelectionChangeEventDispatcher<>();

  private boolean myIgnoreTreeSelectionEvents;

  public ResolvedDependenciesPanel(@NotNull PsAndroidModule module,
                                   @NotNull PsContext context,
                                   @NotNull DependencySelection dependencySelection) {
    this("Resolved Dependencies", module, context, dependencySelection, ToolWindowAnchor.RIGHT);
  }

  public ResolvedDependenciesPanel(@NotNull String title,
                                   @NotNull PsAndroidModule module,
                                   @NotNull PsContext context,
                                   @NotNull DependencySelection dependencySelection,
                                   @Nullable ToolWindowAnchor anchor) {
    super(title, AndroidIcons.Variant, anchor);
    myContext = context;

    DefaultTreeModel treeModel = new DefaultTreeModel(new DefaultMutableTreeNode());
    myTree = new Tree(treeModel) {
      @Override
      protected void processMouseEvent(MouseEvent e) {
        int id = e.getID();
        if (id == MOUSE_PRESSED) {
          ModuleDependencyNode node = myHyperlinkSupport.getIfHyperlink(e);
          if (node != null) {
            PsModuleAndroidDependency moduleDependency = node.getModels().get(0);
            String name = moduleDependency.getName();
            myContext.setSelectedModule(name, ResolvedDependenciesPanel.this);
            // Do not call super, to avoid selecting the 'module' node when clicking a hyperlink.
            return;
          }
        }
        super.processMouseEvent(e);
      }
    };

    setHeaderActions();
    getHeader().setPreferredFocusedComponent(myTree);

    myTreeBuilder = new ResolvedDependenciesTreeBuilder(module, myTree, treeModel, dependencySelection, this);

    module.add(event -> myTreeBuilder.reset(), this);

    JScrollPane scrollPane = setUp(myTreeBuilder);
    add(scrollPane, BorderLayout.CENTER);

    TreeSelectionListener treeSelectionListener = e -> {
      if (myIgnoreTreeSelectionEvents) {
        return;
      }

      PsAndroidDependency selected = getSelection();
      if (selected == null) {
        AbstractPsModelNode selectedNode = getSelectionIfSingle();
        if (selectedNode != null && !(selectedNode instanceof AbstractDependencyNode)) {
          // A non-dependency node was selected (e.g. a variant/artifact node)
          notifySelectionChanged(null);
        }
      }
      else {
        notifySelectionChanged(selected);
      }
    };
    myTree.addTreeSelectionListener(treeSelectionListener);
    myTree.addMouseListener(new PopupHandler() {
      @Override
      public void invokePopup(Component comp, int x, int y) {
        popupInvoked(x, y);
      }
    });

    myHyperlinkSupport = new NodeHyperlinkSupport<>(myTree, ModuleDependencyNode.class, myContext, false);
  }

  private void notifySelectionChanged(@Nullable PsAndroidDependency selected) {
    myEventDispatcher.selectionChanged(selected);
  }

  private void setHeaderActions() {
    List<AnAction> additionalActions = Lists.newArrayList();

    additionalActions.add(new SelectNodesMatchingCurrentSelectionAction() {
      @Override
      @NotNull
      protected AbstractPsNodeTreeBuilder getTreeBuilder() {
        return myTreeBuilder;
      }
    });

    additionalActions.add(Separator.getInstance());

    additionalActions.add(new AbstractBaseExpandAllAction(myTree) {
      @Override
      public void actionPerformed(AnActionEvent e) {
        myIgnoreTreeSelectionEvents = true;
        myTree.requestFocusInWindow();
        myTreeBuilder.expandAllNodes();
        myIgnoreTreeSelectionEvents = false;
      }
    });

    additionalActions.add(new AbstractBaseCollapseAllAction(myTree) {
      @Override
      public void actionPerformed(AnActionEvent e) {
        collapseAllNodes();
      }
    });

    additionalActions.add(Separator.getInstance());
    //DefaultActionGroup settingsGroup = new DefaultActionGroup();
    //settingsGroup.add(new ToggleAction("Group Similar") {
    //  @Override
    //  public boolean isSelected(AnActionEvent e) {
    //    return PsUISettings.getInstance().RESOLVED_DEPENDENCIES_GROUP_VARIANTS;
    //  }
    //
    //  @Override
    //  public void setSelected(AnActionEvent e, boolean state) {
    //    PsUISettings settings = PsUISettings.getInstance();
    //    if (settings.RESOLVED_DEPENDENCIES_GROUP_VARIANTS != state) {
    //      settings.RESOLVED_DEPENDENCIES_GROUP_VARIANTS = state;
    //      settings.fireUISettingsChanged();
    //    }
    //  }
    //});
    //
    //additionalActions.add(new DumbAwareAction("", "", AllIcons.General.Gear) {
    //  @Override
    //  public void actionPerformed(AnActionEvent e) {
    //    InputEvent inputEvent = e.getInputEvent();
    //    ActionManagerImpl actionManager = (ActionManagerImpl)ActionManager.getInstance();
    //    ActionPopupMenu popupMenu =
    //      actionManager.createActionPopupMenu(POPUP_PLACE, settingsGroup, new MenuItemPresentationFactory(true));
    //    int x = 0;
    //    int y = 0;
    //    if (inputEvent instanceof MouseEvent) {
    //      x = ((MouseEvent)inputEvent).getX();
    //      y = ((MouseEvent)inputEvent).getY();
    //    }
    //    popupMenu.getComponent().show(inputEvent.getComponent(), x, y);
    //  }
    //});

    getHeader().setAdditionalActions(additionalActions);
  }

  private void collapseAllNodes() {
    myTree.requestFocusInWindow();

    myIgnoreTreeSelectionEvents = true;
    myTreeBuilder.collapseAllNodes();
    myIgnoreTreeSelectionEvents = false;
  }

  private void popupInvoked(int x, int y) {
    ModuleDependencyNode node = myHyperlinkSupport.getNodeForLocation(x, y);

    if (node != null) {
      PsModuleAndroidDependency moduleDependency = node.getModels().get(0);

      String name = moduleDependency.getName();
      DefaultActionGroup group = new DefaultActionGroup();
      group.add(new GoToModuleAction(name, myContext, myTree));

      ActionPopupMenu popupMenu = ActionManager.getInstance().createActionPopupMenu("", group);
      popupMenu.getComponent().show(myTree, x, y);
    }
  }

  @Override
  public void setSelection(@Nullable PsAndroidDependency selection) {
    if (selection == null) {
      myTreeBuilder.clearSelection();
    }
    else {
      myIgnoreTreeSelectionEvents = true;
      myTreeBuilder.selectMatchingNodes(selection, true);
      myIgnoreTreeSelectionEvents = false;
    }
  }

  public void add(@NotNull SelectionChangeListener<PsAndroidDependency> listener) {
    myEventDispatcher.addListener(listener, this);
  }

  @Override
  @Nullable
  public PsAndroidDependency getSelection() {
    AbstractPsModelNode selection = getSelectionIfSingle();
    if (selection instanceof AbstractDependencyNode) {
      AbstractDependencyNode node = (AbstractDependencyNode)selection;
      List<?> models = node.getModels();
      if (!models.isEmpty()) {
        return (PsAndroidDependency)models.get(0);
      }
    }
    return null;
  }
  
  @Nullable
  private AbstractPsModelNode getSelectionIfSingle() {
    Set<AbstractPsModelNode> selection = myTreeBuilder.getSelectedElements(AbstractPsModelNode.class);
    if (selection.size() == 1) {
      return getFirstItem(selection);
    }
    return null;
  }

  @Override
  public void dispose() {
    super.dispose();
    Disposer.dispose(myTreeBuilder);
    Disposer.dispose(myHyperlinkSupport);
  }
}
