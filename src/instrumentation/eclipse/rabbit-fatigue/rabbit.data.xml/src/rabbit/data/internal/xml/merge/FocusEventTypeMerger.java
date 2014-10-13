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

import rabbit.data.internal.xml.schema.events.FocusEventType;

/**
 * Merger for {@link FocusEventType}.
 */
public class FocusEventTypeMerger extends AbstractMerger<FocusEventType> {
  
  public FocusEventTypeMerger() {
  }

  @Override
  protected FocusEventType doMerge(FocusEventType t1, FocusEventType t2) {
	FocusEventType result = new FocusEventType();
    result.setFocusSession(t1.getFocusSession());
    result.setEvent(t1.getEvent() + "::" + t2.getEvent());
    result.setCount(t1.getCount() + t2.getCount());
    return result;
  }

  @Override
	public boolean doIsMergeable(FocusEventType t1, FocusEventType t2) {
		return (t1.getFocusSession() != null)
				&& (t1.getFocusSession().equals(t2.getFocusSession()) && t1
						.getEvent() != null)
				&& (t1.getEvent().equals(t2.getEvent()));
	}

}
