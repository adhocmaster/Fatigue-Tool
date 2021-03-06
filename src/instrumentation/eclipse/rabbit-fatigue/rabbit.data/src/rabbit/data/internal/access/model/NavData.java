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
package rabbit.data.internal.access.model;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;

import javax.annotation.Nullable;

import org.joda.time.LocalDate;

import rabbit.data.access.model.INavData;
import rabbit.data.access.model.IKey;
import rabbit.data.access.model.WorkspaceStorage;

/**
 * Contains launch information.
 */
public class NavData implements INavData {
  
  /**
   * An immutable map of data.
   */
  private final Map<IKey<? extends Object>, Object> data;
  
  /**
   * Constructor.
   * @param date The date of the session.
   * @param workspace The workspace of the session.
   * @param focusSession The focus session.
   * * @param event The focus event.
   * @param count The count of the focus.
   * @throws NullPointerException If any of the arguments are null.
   */
  public NavData(LocalDate date, WorkspaceStorage workspace,
		String focusSession, String event, int count) {
	  data = new KeyMapBuilder()
      .put(DATE,      		   checkNotNull(date,      "date"))
      .put(WORKSPACE, 		   checkNotNull(workspace, "workspace"))
      .put(NAV_SESSION,        checkNotNull(focusSession,   "focusSession"))
      .put(EVENT,   		   checkNotNull(event,   "event"))
      .put(COUNT,   		   checkNotNull(count,   "count"))
      .build();
  }

  @SuppressWarnings("unchecked")
    @Override
    public <T> T get(@Nullable IKey<T> key) {
      return (T) data.get(key);
    }
  }
