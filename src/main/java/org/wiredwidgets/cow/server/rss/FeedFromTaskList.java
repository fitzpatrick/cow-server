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

package org.wiredwidgets.cow.server.rss;

import java.util.*;

import org.wiredwidgets.cow.server.api.service.*;
import org.wiredwidgets.cow.server.service.TaskService;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author FITZPATRICK
 * Class used for building the RSS file for the selected assignee
 * @return
 */
public class FeedFromTaskList {
	
	public String buildFeedByAssignee(@RequestParam String assignee, String requestURL, String requestQuery, TaskService taskService){
		RSS_Creator RSSByAssignee = new RSS_Creator();
		
		/* Sets the channel information (title, description, link) for the RSS feed */
		RSSByAssignee.setChannel(assignee + "'s Task List", "List of tasks for " + assignee, requestURL+"?"+requestQuery);
		
		/* Object for collecting assignees tasks */		
		Tasks tasks = new Tasks();
        tasks.getTasks().addAll(taskService.findPersonalTasks(assignee));
		List<Task> items = tasks.getTasks();
		
		/* Iterates through the assignees tasks and adds them as RSS items */
		for (Iterator<Task> it = items.iterator(); it.hasNext();){
			/* Gets current task and adds it as RSS item */
			Task curTask = it.next();
			RSSByAssignee.addRSSItem(curTask.getName(),curTask.getDescription(),curTask.getProcessInstanceUrl(),curTask.getPriority());
		}
		
		/* Returns the RSS data as a HTTP response */
		return RSSByAssignee.generateRSSFile();
	}
}
