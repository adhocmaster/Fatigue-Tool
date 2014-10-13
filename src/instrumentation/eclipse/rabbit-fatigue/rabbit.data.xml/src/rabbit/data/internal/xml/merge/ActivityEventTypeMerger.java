/*
 * Copyright 2010 The Rabbit Eclipse Plug-in Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package rabbit.data.internal.xml.merge;

import rabbit.data.internal.xml.schema.events.ActivityEventType;

/**
 * Merger for {@link ActivityEventType}.
 */
public class ActivityEventTypeMerger extends AbstractMerger<ActivityEventType> {
  
  public ActivityEventTypeMerger() {
  }

  @Override
  protected ActivityEventType doMerge(ActivityEventType t1, ActivityEventType t2) {
	ActivityEventType result = new ActivityEventType();
    result.setActivitySession(t1.getActivitySession());
    result.setDuration(t1.getDuration() + t2.getDuration());
    result.setEditSpeed(t1.getEditSpeed() + t2.getEditSpeed());
    return result;
  }

  @Override
  public boolean doIsMergeable(ActivityEventType t1, ActivityEventType t2) {
    return (t1.getActivitySession() != null)
        && (t1.getActivitySession().equals(t2.getActivitySession()));
  }

}
