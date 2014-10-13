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
package rabbit.data.internal.xml.access;

import java.util.Collection;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.joda.time.Duration;
import org.joda.time.LocalDate;

import rabbit.data.access.model.IActivityData;
import rabbit.data.access.model.WorkspaceStorage;
import rabbit.data.internal.access.model.ActivityData;
import rabbit.data.internal.xml.IDataStore;
import rabbit.data.internal.xml.StoreNames;
import rabbit.data.internal.xml.schema.events.ActivityEventListType;
import rabbit.data.internal.xml.schema.events.ActivityEventType;
import rabbit.data.internal.xml.schema.events.EventListType;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * Accesses command event data.
 */
public class ActivityDataAccessor extends
    AbstractAccessor<IActivityData, ActivityEventType, ActivityEventListType> {

  /**
   * Constructor.
   * 
   * @param store The data store to get the data from.
   * @throws NullPointerException If any arguments are null.
   */
  @Inject
  ActivityDataAccessor(@Named(StoreNames.ACTIVITY_STORE) IDataStore store) {
    super(store);
  }

  @Override
  protected IActivityData createDataNode(
      LocalDate date, WorkspaceStorage ws, ActivityEventType type) throws Exception {
	return new ActivityData(date, ws, type.getActivitySession(), new Duration(type.getDuration()), type.getEditSpeed());
  }

  @Override
  protected Collection<ActivityEventListType> getCategories(EventListType doc) {
    return doc.getActivityEvents();
  }

  @Override
  protected Collection<ActivityEventType> getElements(ActivityEventListType list) {
    return list.getActivityEvent();
  }
}
