/**
 * Approved for Public Release: 10-4800. Distribution Unlimited.
 * Copyright 2011 The MITRE Corporation,
 * Licensed under the Apache License,
 * Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under the License.
 */

package org.wiredwidgets.cow.server.completion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.api.history.HistoryProcessInstance;
import org.wiredwidgets.cow.server.api.service.HistoryActivity;

public class History {
	
	private Map<String, List<HistoryActivity>> activitiesMap = new HashMap<String, List<HistoryActivity>>();
        private String processIntanceState = null;
        
        /**
         * Constructor
         * @param activities 
         */
        public History(List<HistoryActivity> activities) {
            this(activities, HistoryProcessInstance.STATE_ACTIVE);
        }
	
        /**
         * Constructor
         * @param activities
         * @param processState 
         */
	public History(List<HistoryActivity> activities, String processState) {
		setActivities(activities);
                this.processIntanceState = processState;
	}
	
	/**
	 * Returns the list of HistoryActivity instances for the specified key
	 * In most cases there will be zero or one item in the list.  The exception to this 
	 * is the Loop structure, as the same activity may be repeated multiple times in a Loop.
	 * @param key
	 * @return list of HistoryActivity objects
	 */
	public List<HistoryActivity> getActivities(String key) {
            if (activitiesMap.get(key) == null) {
                // return empty list
                return new ArrayList<HistoryActivity>();
            }
            else {
		return activitiesMap.get(key);
            }
	}
	
	private void setActivities(List<HistoryActivity> activities) {
		for (HistoryActivity hi : activities) {
			String key = hi.getActivityName();
			if (activitiesMap.get(key) == null) {
				activitiesMap.put(key, new ArrayList<HistoryActivity>());
			}
			activitiesMap.get(key).add(hi);
		}
	}

    public String getProcessInstanceState() {
        return processIntanceState;
    }

}
