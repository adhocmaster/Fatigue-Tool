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

import org.joda.time.LocalDate;

import rabbit.data.access.model.INavData;
import rabbit.data.access.model.WorkspaceStorage;
import rabbit.data.internal.access.model.NavData;
import rabbit.data.internal.xml.IDataStore;
import rabbit.data.internal.xml.StoreNames;
import rabbit.data.internal.xml.schema.events.EventListType;
import rabbit.data.internal.xml.schema.events.NavEventListType;
import rabbit.data.internal.xml.schema.events.NavEventType;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * Accesses command event data.
 */
public class NavDataAccessor extends
    AbstractAccessor<INavData, NavEventType, NavEventListType> {

  /**
   * Constructor.
   * 
   * @param store The data store to get the data from.
   * @throws NullPointerException If any arguments are null.
   */
  @Inject
  NavDataAccessor(@Named(StoreNames.FOCUS_STORE) IDataStore store) {
    super(store);
  }

  @Override
  protected INavData createDataNode(
      LocalDate date, WorkspaceStorage ws, NavEventType type) throws Exception {
	return new NavData(date, ws, type.getNavSession(), type.getEvent(), type.getCount());
  }

  @Override
  protected Collection<NavEventListType> getCategories(EventListType doc) {
    return doc.getNavEvents();
  }

  @Override
  protected Collection<NavEventType> getElements(NavEventListType list) {
    return list.getNavEvent();
  }
}
