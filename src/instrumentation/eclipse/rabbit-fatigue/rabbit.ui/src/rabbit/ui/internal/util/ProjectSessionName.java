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
public final class ProjectSessionName {

  private final String projectSessionName;

  /**
   * @param projectSessionName the name of this focus.
   * @throws NullPointerException if any argument is null.
   */
  public ProjectSessionName(String focusSessionName) {
    this.projectSessionName = checkNotNull(focusSessionName);
  }

  /**
   * @return the the name of this project session.
   */
  public String getProjectSessionName() {
    return projectSessionName;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
        .addValue(getProjectSessionName()).toString();
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(getProjectSessionName());
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ProjectSessionName) {
      ProjectSessionName l = (ProjectSessionName) obj;
      return Objects.equal(getProjectSessionName(), l.getProjectSessionName());
    }
    return false;
  }
}
