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

import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import rabbit.data.internal.xml.IDataStore;
import rabbit.data.internal.xml.StoreNames;
import rabbit.data.internal.xml.convert.IConverter;
import rabbit.data.internal.xml.merge.IMerger;
import rabbit.data.internal.xml.schema.events.EventListType;
import rabbit.data.internal.xml.schema.events.ProjectEventListType;
import rabbit.data.internal.xml.schema.events.ProjectEventType;
import rabbit.data.store.model.CommandEvent;
import rabbit.data.store.model.ProjectEvent;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/**
 * Stores {@link CommandEvent}
 */
@Singleton
public final class ProjectEventStorer extends
    AbstractStorer<ProjectEvent, ProjectEventType, ProjectEventListType> {

  /**
   * Constructor.
   * 
   * @param converter Converter for converting an event to its corresponding XML
   *          type.
   * @param merger Merger for merging two XML types.
   * @param store The data store to store the data to.
   */
  @Inject
  ProjectEventStorer(
      IConverter<ProjectEvent, ProjectEventType> converter,
      IMerger<ProjectEventType> merger,
      @Named(StoreNames.PROJECT_STORE) IDataStore store) {
    super(converter, merger, store);
  }

  @Override
  protected List<ProjectEventListType> getCategories(EventListType events) {
    return events.getProjectEvents();
  }

  @Override
  protected List<ProjectEventType> getElements(ProjectEventListType list) {
    return list.getProjectEvent();
  }

  @Override
  protected ProjectEventListType newCategory(XMLGregorianCalendar date) {
	  ProjectEventListType type = objectFactory.createProjectEventListType();
    type.setDate(date);
    return type;
  }
}
