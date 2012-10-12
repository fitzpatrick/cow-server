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

import org.wiredwidgets.cow.server.rss.RSS_Creator;

public class Test_Feed {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		RSS_Creator rssCreator = new RSS_Creator();
		rssCreator.setChannel("MyProject Build Results", "Continuous build results for RSS_Generator project", "http://rss_generator.mitre.org");
		rssCreator.addRSSItem("RSS Item", "This is an RSS item.", "http://rss_generator.mitre.org/item1",0);
		rssCreator.addRSSItem("RSS Item 2", "This is another RSS item.", "http://rss_generator.mitre.org/item2",0);
		rssCreator.generateRSSFile();
	}
}
