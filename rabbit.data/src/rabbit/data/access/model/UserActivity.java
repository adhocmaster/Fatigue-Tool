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
package rabbit.data.access.model;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Objects;

/**
 * Represents a name of a launch with the associated launch type ID.
 */
public final class UserActivity {

  private final String activityName;
  private final String activityTypeId;

  /**
   * @param launchName the name of this launch.
   * @param launchTypeId the launch type ID of this launch.
   * @throws NullPointerException if any argument is null.
   */
  public UserActivity(String launchName, String launchTypeId) {
    this.activityName = checkNotNull(launchName);
    this.activityTypeId = checkNotNull(launchTypeId);
  }

  /**
   * @return the the name of this launch.
   */
  public String getActivityName() {
    return activityName;
  }

  /**
   * @return the launch type ID of this launch.
   */
  public String getActivityTypeId() {
    return activityTypeId;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
        .addValue(getActivityName())
        .addValue(getActivityTypeId()).toString();
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(getActivityName(), getActivityTypeId());
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof UserActivity) {
    	UserActivity l = (UserActivity) obj;
      return Objects.equal(getActivityName(), l.getActivityName())
          && Objects.equal(getActivityTypeId(), l.getActivityTypeId());
    }
    return false;
  }
}
