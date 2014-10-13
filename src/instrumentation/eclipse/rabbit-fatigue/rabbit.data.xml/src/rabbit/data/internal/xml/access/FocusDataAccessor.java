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

import rabbit.data.access.model.IFocusData;
import rabbit.data.access.model.WorkspaceStorage;
import rabbit.data.internal.access.model.FocusData;
import rabbit.data.internal.xml.IDataStore;
import rabbit.data.internal.xml.StoreNames;
import rabbit.data.internal.xml.schema.events.EventListType;
import rabbit.data.internal.xml.schema.events.FocusEventListType;
import rabbit.data.internal.xml.schema.events.FocusEventType;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * Accesses command event data.
 */
public class FocusDataAccessor extends
    AbstractAccessor<IFocusData, FocusEventType, FocusEventListType> {

  /**
   * Constructor.
   * 
   * @param store The data store to get the data from.
   * @throws NullPointerException If any arguments are null.
   */
  @Inject
  FocusDataAccessor(@Named(StoreNames.FOCUS_STORE) IDataStore store) {
    super(store);
  }

  @Override
  protected IFocusData createDataNode(
      LocalDate date, WorkspaceStorage ws, FocusEventType type) throws Exception {
	return new FocusData(date, ws, type.getFocusSession(), type.getEvent(), type.getCount());
  }

  @Override
  protected Collection<FocusEventListType> getCategories(EventListType doc) {
    return doc.getFocusEvents();
  }

  @Override
  protected Collection<FocusEventType> getElements(FocusEventListType list) {
    return list.getFocusEvent();
  }
}
