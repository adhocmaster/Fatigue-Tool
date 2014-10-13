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
package rabbit.data.store.model;

import static com.google.common.base.Preconditions.checkNotNull;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.ui.IWorkbenchPart;
import org.joda.time.DateTime;
import org.joda.time.Interval;

/**
 * Represents a workbench focus event.
 */
public class FocusEvent extends DiscreteEvent {

  private final String focusSession;
  private final String event;

  /**
   * Constructs a new event.
   * 
   * @param interval The time interval.
   * @param part The workbench part.
   * @throws NullPointerException If any of the arguments are null.
   */
  public FocusEvent(DateTime time, String focusSession, String event) {
    super(time);
    this.focusSession = checkNotNull(focusSession);
    this.event = checkNotNull(event);
  }

  /**
   * Gets the focusSession.
   * 
   * @return The focus session ID.
   */
  public final String getFocusSession() {
	    return focusSession;
  }
  
  /**
   * Gets the event.
   * 
   * @return The event details.
   */
  public final String getEvent() {
	    return event;
  }
}
