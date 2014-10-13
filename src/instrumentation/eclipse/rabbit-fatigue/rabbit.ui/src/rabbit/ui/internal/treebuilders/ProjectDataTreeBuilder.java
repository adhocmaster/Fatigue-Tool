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
package rabbit.ui.internal.treebuilders;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;

import java.util.Collection;
import java.util.List;

import org.eclipse.jface.viewers.TreePath;
import org.joda.time.Duration;

import rabbit.data.access.model.IProjectData;
import rabbit.ui.IProvider;
import rabbit.ui.internal.pages.Category;
import rabbit.ui.internal.util.ProjectSessionName;
import rabbit.ui.internal.util.ICategory;
import rabbit.ui.internal.util.ICategoryProvider;
import rabbit.ui.internal.viewers.ITreePathBuilder;

/**
 * A {@link ProjectDataTreeBuilder} takes input as {@link IFocusDataProvider}
 * and builds tree leaves based on the order of the categories provided by the
 * {@link IFocusDataProvider}, the last segment of every path will be either an
 * {@link Integer} data node ({@link IFocusData#COUNT}) or an {@link Duration}
 * data node ({@link IFocusData#DURATION}). Each {@link IFocusData} provided
 * by the provider will be transformed into two paths (one ends with
 * {@link Integer} and the other ends with {@link Duration}).
 */
public final class ProjectDataTreeBuilder implements ITreePathBuilder {

  private final ICategoryProvider provider;

  /**
   * Provides {@link IFocusData}.
   */
  public static interface IProjectDataProvider extends IProvider<IProjectData> {}

  public ProjectDataTreeBuilder(ICategoryProvider provider) {
    this.provider = checkNotNull(provider);
  }

  @Override
  public List<TreePath> build(Object input) {
    if (!(input instanceof IProjectDataProvider)) {
      return emptyList();
    }

    Collection<IProjectData> dataCol = ((IProjectDataProvider) input).get();
    if (dataCol == null) {
      return emptyList();
    }

    List<TreePath> result = newArrayList();
    for (IProjectData data : dataCol) {

      List<Object> segments = newArrayList();
      for (ICategory category : provider.getSelected()) {
        if (!(category instanceof Category)) {
          continue;
        }

        switch ((Category) category) {
        case PROJECT_ACT:
          segments.add(new ProjectSessionName(data.get(IProjectData.PROJECT_SESSION)));
          break;
        case DATE:
          segments.add(data.get(IProjectData.DATE));
          break;
        case WORKSPACE:
          segments.add(data.get(IProjectData.WORKSPACE));
          break;
        default:
          break;
        }
      }
      
      segments.add(data.get(IProjectData.COUNT));
      result.add(new TreePath(segments.toArray()));
      
    }
    return result;
  }
}
