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
package rabbit.data.internal.xml.store;

import rabbit.data.internal.xml.IDataStore;
import rabbit.data.internal.xml.StoreNames;
import rabbit.data.internal.xml.convert.IConverter;
import rabbit.data.internal.xml.merge.IMerger;
import rabbit.data.internal.xml.schema.events.ActivityEventListType;
import rabbit.data.internal.xml.schema.events.ActivityEventType;
import rabbit.data.internal.xml.schema.events.CommandEventListType;
import rabbit.data.internal.xml.schema.events.CommandEventType;
import rabbit.data.internal.xml.schema.events.EventListType;
import rabbit.data.internal.xml.schema.events.FocusEventListType;
import rabbit.data.internal.xml.schema.events.FocusEventType;
import rabbit.data.store.model.ActivityEvent;
import rabbit.data.store.model.CommandEvent;
import rabbit.data.store.model.FocusEvent;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

/**
 * Stores {@link CommandEvent}
 */
@Singleton
public final class FocusEventStorer extends
    AbstractStorer<FocusEvent, FocusEventType, FocusEventListType> {

  /**
   * Constructor.
   * 
   * @param converter Converter for converting an event to its corresponding XML
   *          type.
   * @param merger Merger for merging two XML types.
   * @param store The data store to store the data to.
   */
  @Inject
  FocusEventStorer(
      IConverter<FocusEvent, FocusEventType> converter,
      IMerger<FocusEventType> merger,
      @Named(StoreNames.FOCUS_STORE) IDataStore store) {
    super(converter, merger, store);
  }

  @Override
  protected List<FocusEventListType> getCategories(EventListType events) {
    return events.getFocusEvents();
  }

  @Override
  protected List<FocusEventType> getElements(FocusEventListType list) {
    return list.getFocusEvent();
  }

  @Override
  protected FocusEventListType newCategory(XMLGregorianCalendar date) {
	FocusEventListType type = objectFactory.createFocusEventListType();
    type.setDate(date);
    return type;
  }
}
