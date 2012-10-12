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

import org.wiredwidgets.cow.server.rss.FeedFromTaskList;
import org.wiredwidgets.cow.server.rss.RSS_Creator;
import org.jbpm.api.ProcessEngine;
import org.wiredwidgets.cow.server.service.*;
import org.wiredwidgets.cow.server.api.service.*;
import org.junit.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.wiredwidgets.cow.server.api.service.Group;
import org.wiredwidgets.cow.server.api.service.Groups;
import org.wiredwidgets.cow.server.api.service.User;
import org.wiredwidgets.cow.server.api.service.Users;
import org.wiredwidgets.cow.server.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class Test_RSS_Feed_Creation {
	
	@Autowired
    ProcessEngine engine;
	
	//@RequestMapping("/rss")
	public void test() {
		/*TaskServiceImpl ts = new TaskServiceImpl();
		Task t = new Task();
		
		t.setAssignee("Joe");
		t.setActivityName("Open a credit card");
		t.setDescription("This activity will open a credit card.");
		t.setProcessInstanceUrl("http://www.discover.com");
		
		String id = ts.createAdHocTask(t);
		
		ts.takeTask(id,"Joe");

		FeedFromTaskList f = new FeedFromTaskList();
		f.buildFeedByAssignee("Joe");
		*/
		
		org.jbpm.api.task.Task task = engine.getTaskService().newTask();
        task.setName("Open a credit card. ");
        
        task.setDescription("This activity will open a credit card.");
        
        task.setAssignee("Joe");
        engine.getTaskService().saveTask(task);
                
        FeedFromTaskList f = new FeedFromTaskList();
        //f.buildFeedByAssignee("Joe");
	}
	
	@RequestMapping("/rsscreator")
	public void testRSS_Creator(){
		RSS_Creator rssCreator = new RSS_Creator();
		rssCreator.setChannel("MyProject Build Results", "Continuous build results for RSS_Generator project", "http://rss_generator.mitre.org");
		rssCreator.addRSSItem("RSS Item", "This is an RSS item.", "http://rss_generator.mitre.org/item1",0);
		rssCreator.addRSSItem("RSS Item 2", "This is another RSS item.", "http://rss_generator.mitre.org/item2",0);
		rssCreator.generateRSSFile();
	}

}
