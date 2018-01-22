/*
 * Copyright (C) 2013 The Android Open Source Project
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
package com.github.vete_l.support.gradle.variant.view;

import com.android.tools.idea.gradle.project.facet.ndk.NdkFacet;
import com.android.tools.idea.gradle.project.model.AndroidModuleModel;
import com.android.tools.idea.gradle.project.model.NdkModuleModel;
import com.android.tools.idea.gradle.project.sync.GradleSyncState;
import com.android.tools.idea.gradle.util.GradleUtil;
import com.android.tools.idea.gradle.util.ModuleTypeComparator;
import com.android.tools.idea.gradle.variant.conflict.Conflict;
import com.android.tools.idea.gradle.variant.conflict.ConflictSet;
import com.google.common.annotations.VisibleForTesting;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.TableSpeedSearch;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.AbstractTableCellEditor;
import com.intellij.util.ui.JBUI;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.android.model.impl.JpsAndroidModuleProperties;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.text.Collator;
import java.util.*;
import java.util.List;

import static com.android.tools.idea.gradle.variant.conflict.ConflictResolution.solveSelectionConflict;
import static com.intellij.ui.TableUtil.scrollSelectionToVisible;
import static com.intellij.util.ui.JBUI.scale;
import static com.intellij.util.ui.UIUtil.getTableFocusCellHighlightBorder;
import static com.intellij.util.ui.UIUtil.getToolTipBackground;

/**
 * The contents of the "Build Variants" tool window.
 */
public class BuildVariantView {
  private static final Object[] TABLE_COLUMN_NAMES = new Object[]{"Module", "Build Variant"};

  private static final int MODULE_COLUMN_INDEX = 0;
  private static final int VARIANT_COLUMN_INDEX = 1;

  private static final Color CONFLICT_CELL_BACKGROUND = MessageType.ERROR.getPopupBackground();

  private final Project myProject;
  private BuildVariantUpdater myUpdater;

  private JPanel myToolWindowPanel;
  private JBTable myVariantsTable;
  private JPanel myNotificationPanel;

  private final List<BuildVariantSelectionChangeListener> myBuildVariantSelectionChangeListeners = new ArrayList<>();
  private final List<Conflict> myConflicts = new ArrayList<>();

  @NotNull
  public static BuildVariantView getInstance(@NotNull Project project) {
    return ServiceManager.getService(project, BuildVariantView.class);
  }

  public BuildVariantView(@NotNull Project project) {
    myProject = project;
    myUpdater = new BuildVariantUpdater();
    ((JComponent)myVariantsTable.getParent().getParent()).setBorder(IdeBorderFactory.createEmptyBorder());
  }

  @VisibleForTesting
  void setUpdater(@NotNull BuildVariantUpdater updater) {
    myUpdater = updater;
  }

  public void addListener(@NotNull BuildVariantSelectionChangeListener listener) {
    //noinspection SynchronizeOnThis
    synchronized (this) {
      myBuildVariantSelectionChangeListeners.add(listener);
    }
  }

  public void removeListener(@NotNull BuildVariantSelectionChangeListener listener) {
    //noinspection SynchronizeOnThis
    synchronized (this) {
      myBuildVariantSelectionChangeListeners.remove(listener);
    }
  }

  private void createUIComponents() {
    myVariantsTable = new BuildVariantTable();
    new TableSpeedSearch(myVariantsTable);
    myNotificationPanel = new NotificationPanel();
    myNotificationPanel.setVisible(false);
  }

  /**
   * Creates the contents of the "Build Variants" tool window.
   *
   * @param toolWindow the tool window whose contents will be created.
   */
  public void createToolWindowContent(@NotNull ToolWindow toolWindow) {
    ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
    Content content = contentFactory.createContent(myToolWindowPanel, "", false);
    toolWindow.getContentManager().addContent(content);
    updateContents();
  }

  public void updateContents() {
    GradleSyncState gradleSyncState = GradleSyncState.getInstance(myProject);
    // When sync is 'skipped' (i.e. Gradle models are retrieved from the disk cache, instead of Gradle,) the contents of the "Build
    // Variants" view are already displayed. Invoking 'projectImportStarted' will show the "Loading..." message, and, depending on
    // timing, the message may never disappear.
    // See https://code.google.com/p/android/issues/detail?id=232529
    if (gradleSyncState.isSyncInProgress() && !gradleSyncState.isSyncSkipped()) {
      projectImportStarted();
      return;
    }

    List<Object[]> rows = new ArrayList<>();
    List<BuildVariantItem[]> variantNamesPerRow = new ArrayList<>();

    for (Module module : getGradleModulesWithAndroidProjects()) {
      AndroidFacet androidFacet = AndroidFacet.getInstance(module);
      NdkFacet ndkFacet = NdkFacet.getInstance(module);

      assert androidFacet != null || ndkFacet != null; // getGradleModules() returns only relevant modules.

      String variantName = null;

      if (androidFacet != null) {
        JpsAndroidModuleProperties facetProperties = androidFacet.getProperties();
        variantName = facetProperties.SELECTED_BUILD_VARIANT;
      }

      BuildVariantItem[] variantNames = getVariantItems(module);
      if (variantNames != null) {
        if (androidFacet != null) {
          AndroidModuleModel androidModel = AndroidModuleModel.get(module);
          // AndroidModel may be null when applying a quick fix (e.g. "Fix Gradle version")
          if (androidModel != null) {
            variantName = androidModel.getSelectedVariant().getName();
          }
        }
        else {
          // As only the modules backed by either AndroidGradleModel or NativeAndroidGradleModel are shown in the Build Variants View,
          // when a module is not backed by AndroidGradleModel, it surely contains a valid NativeAndroidGradleModel.
          NdkModuleModel ndkModuleModel = NdkModuleModel.get(module);
          if (ndkModuleModel != null) {
            variantName = ndkModuleModel.getSelectedVariant().getName();
          }
        }

        variantNamesPerRow.add(variantNames);
      }

      if (variantName != null) {
        Object[] row = {module, variantName};
        rows.add(row);
      }
    }
    Runnable setModelTask = () -> getVariantsTable().setModel(rows, variantNamesPerRow);
    Application application = ApplicationManager.getApplication();
    if (application.isDispatchThread()) {
      setModelTask.run();
    }
    else {
      application.invokeLater(setModelTask);
    }
  }

  public void projectImportStarted() {
    getVariantsTable().setLoading(true);
  }

  @NotNull
  private List<Module> getGradleModulesWithAndroidProjects() {
    List<Module> gradleModules = new ArrayList<>();
    for (Module module : ModuleManager.getInstance(myProject).getModules()) {
      AndroidFacet androidFacet = AndroidFacet.getInstance(module);
      if (androidFacet != null && androidFacet.requiresAndroidModel() && androidFacet.getAndroidModel() != null) {
        gradleModules.add(module);
        continue;
      }
      NdkFacet ndkFacet = NdkFacet.getInstance(module);
      if (ndkFacet != null && ndkFacet.getNdkModuleModel() != null) {
        gradleModules.add(module);
      }
    }

    if (!gradleModules.isEmpty()) {
      gradleModules.sort(ModuleTypeComparator.INSTANCE);
      return gradleModules;
    }
    return Collections.emptyList();
  }

  @NotNull
  private BuildVariantTable getVariantsTable() {
    return (BuildVariantTable)myVariantsTable;
  }

  @Nullable
  private static BuildVariantItem[] getVariantItems(@NotNull Module module) {
    Collection<String> variantNames = getVariantNames(module);
    if (variantNames == null) {
      return null;
    }

    BuildVariantItem[] items = new BuildVariantItem[variantNames.size()];
    int i = 0;
    for (String name : variantNames) {
      items[i++] = new BuildVariantItem(module.getName(), name);
    }
    Arrays.sort(items);
    return items;
  }

  @Nullable
  private static Collection<String> getVariantNames(@NotNull Module module) {
    AndroidModuleModel androidModel = AndroidModuleModel.get(module);
    if (androidModel != null) {
      return androidModel.getVariantNames();
    }

    NdkModuleModel ndkModuleModel = NdkModuleModel.get(module);
    if (ndkModuleModel != null) {
      return ndkModuleModel.getVariantNames();
    }

    return null;
  }

  public void updateContents(@NotNull List<Conflict> conflicts) {
    myNotificationPanel.setVisible(!conflicts.isEmpty());
    ((NotificationPanel)myNotificationPanel).myCurrentConflictIndex = -1;
    myConflicts.clear();
    myConflicts.addAll(conflicts);
    updateContents();
  }

  public void findAndSelect(@NotNull Module module) {
    findAndSelect(module, MODULE_COLUMN_INDEX);
  }

  public void findAndSelectVariantEditor(@NotNull Module module) {
    findAndSelect(module, VARIANT_COLUMN_INDEX);
  }

  private void findAndSelect(@NotNull Module module, int columnIndex) {
    int rowCount = myVariantsTable.getRowCount();
    for (int row = 0; row < rowCount; row++) {
      if (module.equals(myVariantsTable.getValueAt(row, MODULE_COLUMN_INDEX))) {
        myVariantsTable.getSelectionModel().setSelectionInterval(row, row);
        myVariantsTable.getColumnModel().getSelectionModel().setSelectionInterval(columnIndex, columnIndex);
        scrollSelectionToVisible(myVariantsTable);
        myVariantsTable.requestFocusInWindow();
        break;
      }
    }
  }

  public interface BuildVariantSelectionChangeListener {
    /**
     * Indicates that a user selected a build variant from the "Build Variants" tool window.
     * <p/>
     * This notification occurs:
     * <ul>
     * <li>after the user selected a build variant from the drop-down</li>
     * <li>project structure has been updated according to selected build variant</li>
     * </ul>
     * <p/>
     * This listener will not be invoked if the project structure update fails.
     *
     */
    void selectionChanged();
  }

  private class NotificationPanel extends JPanel {
    int myCurrentConflictIndex = -1;

    NotificationPanel() {
      super(new BorderLayout());
      Color color = EditorColorsManager.getInstance().getGlobalScheme().getColor(EditorColors.NOTIFICATION_BACKGROUND);
      setBackground(color == null ? getToolTipBackground() : color);
      setBorder(BorderFactory.createEmptyBorder(1, 15, 1, 15)); // Same as EditorNotificationPanel
      setPreferredSize(new Dimension(-1, scale(24)));

      JLabel textLabel = new JLabel("Variant selection conflicts found.");
      textLabel.setOpaque(false);
      add(textLabel, BorderLayout.CENTER);

      DefaultActionGroup group = new DefaultActionGroup();
      ActionManager actionManager = ActionManager.getInstance();

      AnAction nextConflictAction = new AnAction() {
        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
          navigateConflicts(true);
        }
      };
      nextConflictAction.copyFrom(actionManager.getAction(IdeActions.ACTION_NEXT_OCCURENCE));
      group.add(nextConflictAction);

      AnAction prevConflictAction = new AnAction() {
        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
          navigateConflicts(false);
        }
      };
      prevConflictAction.copyFrom(actionManager.getAction(IdeActions.ACTION_PREVIOUS_OCCURENCE));
      group.add(prevConflictAction);

      ActionToolbar toolbar = actionManager.createActionToolbar("", group, true);
      toolbar.setReservePlaceAutoPopupIcon(false);
      toolbar.setMinimumButtonSize(JBUI.size(23, 23)); // a little smaller than default (25 x 25)

      JComponent toolbarComponent = toolbar.getComponent();
      toolbarComponent.setBorder(null);
      toolbarComponent.setOpaque(false);
      add(toolbarComponent, BorderLayout.EAST);
    }

    private void navigateConflicts(boolean forward) {
      int conflictCount = myConflicts.size();
      if (conflictCount == 0) {
        return;
      }
      if (forward) {
        myCurrentConflictIndex++;
        if (myCurrentConflictIndex >= conflictCount) {
          myCurrentConflictIndex = 0;
        }
      }
      else {
        myCurrentConflictIndex--;
        if (myCurrentConflictIndex < 0) {
          myCurrentConflictIndex = conflictCount - 1;
        }
      }
      Conflict conflict = myConflicts.get(myCurrentConflictIndex);
      findAndSelect(conflict.getSource());
    }
  }

  private static class BuildVariantItem implements Comparable<BuildVariantItem> {
    @NotNull final String myModuleName;
    @NotNull final String myBuildVariantName;

    BuildVariantItem(@NotNull String moduleName, @NotNull String buildVariantName) {
      myModuleName = moduleName;
      myBuildVariantName = buildVariantName;
    }

    @Override
    public int compareTo(@Nullable BuildVariantItem o) {
      return o != null ? Collator.getInstance().compare(myBuildVariantName, o.myBuildVariantName) : 1;
    }

    boolean hasBuildVariantName(@Nullable Object name) {
      return myBuildVariantName.equals(name);
    }

    @Override
    public String toString() {
      return myBuildVariantName;
    }
  }

  private class BuildVariantTable extends JBTable {
    private boolean myLoading;
    private final List<TableCellEditor> myCellEditors = new ArrayList<>();

    private final ModuleTableCell myModuleCellRenderer = new ModuleTableCell();
    private final ModuleTableCell myModuleCellEditor = new ModuleTableCell();
    private final VariantsCellRenderer myVariantsCellRenderer = new VariantsCellRenderer();

    BuildVariantTable() {
      super(new BuildVariantTableModel(Collections.emptyList()));
      addKeyListener(new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
          int column = getSelectedColumn();
          int row = getSelectedRow();
          if (column == VARIANT_COLUMN_INDEX && row >= 0 && e.getKeyCode() == KeyEvent.VK_F2 && editCellAt(row, column)) {
            Component editorComponent = getEditorComponent();
            if (editorComponent instanceof ComboBox) {
              editorComponent.requestFocusInWindow();
              ((ComboBox)editorComponent).showPopup();
            }
          }
        }
      });
    }

    @Nullable
    Conflict findConflict(int row) {
      for (Conflict conflict : myConflicts) {
        Object module = getValueAt(row, MODULE_COLUMN_INDEX);
        if (conflict.getSource().equals(module)) {
          return conflict;
        }
      }
      return null;
    }

    @Override
    public boolean isCellEditable(int row, int column) {
      return true;
    }

    void setLoading(boolean loading) {
      myLoading = loading;
      setPaintBusy(myLoading);
      clearContents();
      String text = myLoading ? "Loading..." : "Nothing to Show";
      getEmptyText().setText(text);
    }

    private void clearContents() {
      setModel(new BuildVariantTableModel(Collections.emptyList()));
      myCellEditors.clear();
    }

    void setModel(@NotNull List<Object[]> rows, @NotNull List<BuildVariantItem[]> variantNamesPerRow) {
      setLoading(false);
      if (rows.isEmpty()) {
        // This is most likely an old-style (pre-Gradle) Android project. Just leave the table empty.
        setModel(new BuildVariantTableModel(rows));
        return;
      }

      boolean hasVariants = !variantNamesPerRow.isEmpty();
      List<Object[]> content = hasVariants ? rows : Collections.emptyList();

      setModel(new BuildVariantTableModel(content));
      addBuildVariants(variantNamesPerRow);
    }

    private void addBuildVariants(@NotNull List<BuildVariantItem[]> variantNamesPerRow) {
      for (int row = 0; row < variantNamesPerRow.size(); row++) {
        BuildVariantItem[] items = variantNamesPerRow.get(row);
        BuildVariantItem selected = null;
        for (BuildVariantItem item : items) {
          if (item.hasBuildVariantName(getValueAt(row, VARIANT_COLUMN_INDEX))) {
            selected = item;
            break;
          }
        }

        ComboBox<BuildVariantItem> editor = new ComboBox<>(items);
        if (selected != null) {
          editor.setSelectedItem(selected);
        }

        editor.setBorder(BorderFactory.createEmptyBorder());
        editor.addItemListener(e -> {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            BuildVariantItem selectedVariant = (BuildVariantItem)e.getItem();
            buildVariantSelected(selectedVariant.myModuleName, selectedVariant.myBuildVariantName);
          }
        });
        DefaultCellEditor defaultCellEditor = new DefaultCellEditor(editor);

        editor.addKeyListener(new KeyAdapter() {
          @Override
          public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
              defaultCellEditor.cancelCellEditing();
            }
          }
        });

        myCellEditors.add(defaultCellEditor);
      }
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
      return column == MODULE_COLUMN_INDEX ? myModuleCellRenderer : myVariantsCellRenderer;
    }

    @Override
    public TableCellEditor getCellEditor(int row, int column) {
      if (column == VARIANT_COLUMN_INDEX && row >= 0 && row < myCellEditors.size()) {
        return myCellEditors.get(row);
      }
      return myModuleCellEditor;
    }
  }

  @VisibleForTesting
  void buildVariantSelected(@NotNull String moduleName, @NotNull String variantName) {
    if (myUpdater.updateSelectedVariant(myProject, moduleName, variantName)) {
      invokeListeners();
    }
  }

  private void invokeListeners() {
    Runnable invokeListenersTask = () -> {
      updateContents();
      for (BuildVariantSelectionChangeListener listener : myBuildVariantSelectionChangeListeners) {
        listener.selectionChanged();
      }
    };

    Application application = ApplicationManager.getApplication();
    if (application.isUnitTestMode()) {
      invokeListenersTask.run();
    }
    else {
      application.invokeLater(invokeListenersTask);
    }
  }

  private static class BuildVariantTableModel extends DefaultTableModel {
    BuildVariantTableModel(List<Object[]> rows) {
      super(rows.toArray(new Object[rows.size()][TABLE_COLUMN_NAMES.length]), TABLE_COLUMN_NAMES);
    }
  }

  private static class VariantsCellRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

      if (c instanceof JLabel) {
        JLabel component = (JLabel)c;

        Color background = isSelected ? table.getSelectionBackground() : table.getBackground();
        Conflict conflictFound = ((BuildVariantTable)table).findConflict(row);
        if (conflictFound != null) {
          background = CONFLICT_CELL_BACKGROUND;
        }
        component.setBackground(background);

        String toolTip = conflictFound != null ? conflictFound.toString() : null;
        component.setToolTipText(toolTip);

        // add some padding to table cells. It is hard to read text of combo box.
        component.setBorder(BorderFactory.createCompoundBorder(component.getBorder(), BorderFactory.createEmptyBorder(3, 2, 4, 2)));
      }

      return c;
    }
  }

  private static class ModuleTableCell extends AbstractTableCellEditor implements TableCellRenderer {
    private static final Border EMPTY_BORDER = BorderFactory.createEmptyBorder(1, 1, 1, 1);

    @Nullable private Conflict myConflict;

    private JPanel myPanel;
    private JLabel myModuleNameLabel;
    private JPanel myButtonsPanel;
    private JButton myInfoButton;
    private JButton myFixButton;

    private Object myValue;

    ModuleTableCell() {
      myModuleNameLabel = new JLabel();
      myModuleNameLabel.setOpaque(false);

      myInfoButton = createButton(AllIcons.General.BalloonInformation);
      myInfoButton.setToolTipText("More info");
      myInfoButton.addActionListener(e -> {
        if (myValue instanceof Module) {
          Module module = (Module)myValue;
          ModuleVariantsInfoGraph dialog = new ModuleVariantsInfoGraph(module);
          dialog.show();
        }
      });

      myFixButton = createButton(AllIcons.Actions.QuickfixBulb);
      myFixButton.setToolTipText("Fix problem");
      myFixButton.addActionListener(e -> {
        if (myConflict != null) {
          Project project = myConflict.getSource().getProject();
          boolean solved = solveSelectionConflict(myConflict);
          if (solved) {
            ConflictSet conflicts = ConflictSet.findConflicts(project);
            conflicts.showSelectionConflicts();
          }
        }
        stopCellEditing();
      });

      myButtonsPanel = new JPanel();
      myButtonsPanel.setOpaque(false);
      myButtonsPanel.add(myInfoButton);
      myButtonsPanel.add(myFixButton);

      myPanel = new JPanel(new BorderLayout()) {
        @Override
        public String getToolTipText(MouseEvent e) {
          String toolTip = getToolTipTextIfUnderX(myModuleNameLabel, e.getX());
          if (toolTip != null) {
            return toolTip;
          }
          int x = e.getX() - myButtonsPanel.getX();
          toolTip = getToolTipTextIfUnderX(myInfoButton, x);
          if (toolTip != null) {
            return toolTip;
          }
          toolTip = getToolTipTextIfUnderX(myFixButton, x);
          if (toolTip != null) {
            return toolTip;
          }
          return super.getToolTipText(e);
        }
      };

      myPanel.add(myModuleNameLabel, BorderLayout.CENTER);
      myPanel.add(myButtonsPanel, BorderLayout.EAST);
    }

    @NotNull
    private static JButton createButton(@NotNull Icon icon) {
      JButton button = new JButton(icon);
      button.setBorder(null);
      button.setBorderPainted(false);
      button.setContentAreaFilled(false);
      return button;
    }

    @Nullable
    private static String getToolTipTextIfUnderX(@NotNull JComponent c, int x) {
      if (c.isVisible() && x >= c.getX() && x <= c.getX() + c.getWidth()) {
        return c.getToolTipText();
      }
      return null;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
      setUpComponent(table, value, true, true, row);
      return myPanel;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      setUpComponent(table, value, isSelected, hasFocus, row);
      return myPanel;
    }

    private void setUpComponent(@NotNull JTable table, @Nullable Object value, boolean isSelected, boolean hasFocus, int row) {
      myValue = value;

      String moduleName = null;
      Icon moduleIcon = null;
      boolean isAndriodGradleModule = false;
      if (value instanceof Module) {
        Module module = (Module)value;
        if (!module.isDisposed()) {
          moduleName = module.getName();
          moduleIcon = GradleUtil.getModuleIcon(module);
          isAndriodGradleModule = AndroidModuleModel.get(module) != null;
        }
      }

      myModuleNameLabel.setText(moduleName == null ? "" : moduleName);
      myModuleNameLabel.setIcon(moduleIcon);

      Color background = isSelected ? table.getSelectionBackground() : table.getBackground();

      if (isAndriodGradleModule) {
        myInfoButton.setVisible(true);
        myConflict = ((BuildVariantTable)table).findConflict(row);

        myModuleNameLabel.setToolTipText(myConflict != null ? myConflict.toString() : null);
        myFixButton.setVisible(myConflict != null);
        if (myConflict != null) {
          background = CONFLICT_CELL_BACKGROUND;
        }
      }
      else {
        // TODO: Consider showing dependency graph and conflict resolution options for native android modules also.
        myInfoButton.setVisible(false);
        myFixButton.setVisible(false);
      }

      myPanel.setBackground(background);

      Border border = hasFocus ? getTableFocusCellHighlightBorder() : EMPTY_BORDER;
      myPanel.setBorder(border);
    }

    @Override
    public Object getCellEditorValue() {
      return myValue;
    }
  }
}
