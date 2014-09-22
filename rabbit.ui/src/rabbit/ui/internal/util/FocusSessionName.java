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
package rabbit.ui.internal.util;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Objects;

/**
 * Represents a name of a focus with the associated focus type ID.
 */
public final class FocusSessionName {

  private final String focusSessionName;

  /**
   * @param focusSessionName the name of this focus.
   * @throws NullPointerException if any argument is null.
   */
  public FocusSessionName(String focusSessionName) {
    this.focusSessionName = checkNotNull(focusSessionName);
  }

  /**
   * @return the the name of this focus session.
   */
  public String getFocusSessionName() {
    return focusSessionName;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
        .addValue(getFocusSessionName()).toString();
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(getFocusSessionName());
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof FocusSessionName) {
      FocusSessionName l = (FocusSessionName) obj;
      return Objects.equal(getFocusSessionName(), l.getFocusSessionName());
    }
    return false;
  }
}
