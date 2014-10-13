/*
 * Copyright 2010 The Rabbit Eclipse Plug-in Project
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package rabbit.ui.internal.pages;

import static com.google.common.base.Predicates.instanceOf;
import static rabbit.ui.internal.pages.Category.DATE;
import static rabbit.ui.internal.pages.Category.FOCUS;
import static rabbit.ui.internal.pages.Category.WORKSPACE;
import static rabbit.ui.internal.viewers.Viewers.newTreeViewerColumn;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.DecoratingStyledCellLabelProvider;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.FilteredTree;
import org.joda.time.LocalDate;

import rabbit.data.access.IAccessor;
import rabbit.data.access.model.IProjectData;
import rabbit.data.access.model.WorkspaceStorage;
import rabbit.data.handler.DataHandler;
import rabbit.ui.Preference;
import rabbit.ui.internal.treebuilders.ProjectDataTreeBuilder;
import rabbit.ui.internal.treebuilders.ProjectDataTreeBuilder.IProjectDataProvider;
import rabbit.ui.internal.util.Categorizer;
import rabbit.ui.internal.util.CategoryProvider;
import rabbit.ui.internal.util.ProjectSessionName;
import rabbit.ui.internal.util.ICategorizer;
import rabbit.ui.internal.util.IConverter;
import rabbit.ui.internal.util.TreePathIntConverter;
import rabbit.ui.internal.util.TreePathValueProvider;
import rabbit.ui.internal.viewers.CompositeCellLabelProvider;
import rabbit.ui.internal.viewers.DateLabelProvider;
import rabbit.ui.internal.viewers.FilterableTreePathContentProvider;
import rabbit.ui.internal.viewers.ProjectLabelProvider;
import rabbit.ui.internal.viewers.TreePathContentProvider;
import rabbit.ui.internal.viewers.TreePathIntLabelProvider;
import rabbit.ui.internal.viewers.TreePathPatternFilter;
import rabbit.ui.internal.viewers.TreeViewerCellPainter;
import rabbit.ui.internal.viewers.TreeViewerColumnSorter;
import rabbit.ui.internal.viewers.TreeViewerColumnValueSorter;
import rabbit.ui.internal.viewers.Viewers;
import rabbit.ui.internal.viewers.WorkspaceStorageLabelProvider;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;

/**
 * A page for displaying launch events.
 */
public class ProjectPage extends AbsPage {

  private FilteredTree filteredTree;
  private CategoryProvider categoryProvider;
  private TreePathValueProvider valueProvider;
  private TreePathContentProvider contentProvider;

  public ProjectPage() {}

  @Override
  public void createContents(Composite parent) {
    Category[] supported = {WORKSPACE, DATE, FOCUS};
    categoryProvider = new CategoryProvider(supported, FOCUS);
    categoryProvider.addObserver(this);

    contentProvider = new TreePathContentProvider(
        new ProjectDataTreeBuilder(categoryProvider));
    contentProvider.addObserver(this);

    valueProvider = createValueProvider();
    valueProvider.addObserver(this);

    // The main label provider for the first column:
    CompositeCellLabelProvider mainLabels = new CompositeCellLabelProvider(
        new ProjectLabelProvider(),
        new DateLabelProvider(),
        new WorkspaceStorageLabelProvider());

    // The viewer:
    filteredTree = Viewers.newFilteredTree(parent,
        new TreePathPatternFilter(mainLabels));
    TreeViewer viewer = filteredTree.getViewer();
    FilterableTreePathContentProvider filteredContentProvider =
         new FilterableTreePathContentProvider(contentProvider);    
    filteredContentProvider.addFilter(instanceOf(Integer.class));
    viewer.setContentProvider(filteredContentProvider);

    // Column sorters:
    TreeViewerColumnSorter labelSorter =
        new InternalTreeViewerColumnLabelSorter(viewer, mainLabels);
    TreeViewerColumnSorter countSorter =
        new TreeViewerColumnValueSorter(viewer, valueProvider);

    // The columns:

    TreeViewerColumn mainColumn =
        newTreeViewerColumn(viewer, SWT.LEFT, "Hour of the Day (Session)", 350);
    mainColumn.getColumn().addSelectionListener(labelSorter);
    ILabelDecorator decorator =
        PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator();
    mainColumn.setLabelProvider(new DecoratingStyledCellLabelProvider(
        mainLabels, decorator, null));

    TreeViewerColumn countColumn =
        newTreeViewerColumn(viewer, SWT.RIGHT, "Focus Count", 100);
    countColumn.getColumn().addSelectionListener(countSorter);
    countColumn.setLabelProvider(
        new TreePathIntLabelProvider(valueProvider, mainLabels));

    TreeViewerColumn countGraphColumn =
        newTreeViewerColumn(viewer, SWT.LEFT, "", 100);
    countGraphColumn.getColumn().addSelectionListener(countSorter);
    countGraphColumn.setLabelProvider(new TreeViewerCellPainter(valueProvider) {
      @Override
      protected Color createColor(Display display) {
        return new Color(display, 118, 146, 60);
      }
    });
  }

  @Override
  public IContributionItem[] createToolBarItems(IToolBarManager toolBar) {
    List<IContributionItem> items = new CommonToolBarBuilder()
        .enableFilterControlAction(filteredTree, true)
        .enableTreeAction(filteredTree.getViewer())
        .enableGroupByAction(categoryProvider)        
        .enableColorByAction(valueProvider)

        .addGroupByAction(FOCUS)
        .addGroupByAction(DATE, FOCUS)
        .addGroupByAction(WORKSPACE, FOCUS)

        .addColorByAction(FOCUS)
        .addColorByAction(DATE)
        .addColorByAction(WORKSPACE)
        .build();

    for (IContributionItem item : items) {
      toolBar.add(item);
    }
    return items.toArray(new IContributionItem[items.size()]);
  }

  @Override
  public Job updateJob(Preference pref) {
    TreeViewer viewer = filteredTree.getViewer();
    return new UpdateJob<IProjectData>(viewer, pref, getAccessor()) {
      @Override
      protected Object getInput(final Collection<IProjectData> data) {
        return new IProjectDataProvider() {
          @Override
          public Collection<IProjectData> get() {
            return data;
          }
        };
      }
    };
  }

  @Override
  protected FilteredTree getFilteredTree() {
    return filteredTree;
  }

  @Override
  protected Category[] getSelectedCategories() {
    return categoryProvider.getSelected().toArray(new Category[0]);
  }

  @Override
  protected Category getVisualCategory() {
    return (Category) valueProvider.getVisualCategory();
  }

  @Override
  protected void setSelectedCategories(List<Category> categories) {
    Category[] selected = categories.toArray(new Category[0]);
    categoryProvider.setSelected(selected);
  }

  @Override
  protected void setVisualCategory(Category category) {
    valueProvider.setVisualCategory(category);
  }

  @Override
  protected void updateMaxValue() {
    valueProvider.setMaxValue(valueProvider.getVisualCategory());
  }
  
  private TreePathValueProvider createValueProvider() {
	  Map<Predicate<Object>, Category> categories = ImmutableMap.of(
		        instanceOf(ProjectSessionName.class), FOCUS,
		        instanceOf(LocalDate.class), DATE,
		        instanceOf(WorkspaceStorage.class), WORKSPACE);
	    ICategorizer categorizer = new Categorizer(categories);
	    IConverter<TreePath> converter = new TreePathIntConverter();
	    return new TreePathValueProvider(
	        categorizer, contentProvider, converter, FOCUS);
  }

  private IAccessor<IProjectData> getAccessor() {
    return DataHandler.getAccessor(IProjectData.class);
  }
}
