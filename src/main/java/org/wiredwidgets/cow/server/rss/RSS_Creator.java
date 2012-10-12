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
import com.sun.syndication.feed.synd.*;
import com.sun.syndication.io.*;

/**
 * @author FITZPATRICK
 * Class for creating RSS feeds
 * @return
 */
public class RSS_Creator {
	/* SyndFeed variable for building feed within class*/
	private SyndFeed feed;
	
	/* Variable for transforming feed to RSS XML */
	private SyndFeedOutput output;
	
	/* List for maintaining RSS feed entries */
	private List <SyndEntry> items = new ArrayList<SyndEntry>();
	
	/*
	 * Default constructor
	 */
	public RSS_Creator()
	{
		feed = new SyndFeedImpl();
		output = new SyndFeedOutput();
		feed.setFeedType("rss_2.0");
	}
	
	/*
	 * Set channel tags for title, description, and link
	 */
	public void setChannel(String title, String description, String link){
		feed.setTitle(title);
		feed.setDescription(description);
		feed.setLink(link);
	}
	
	/*
	 * Add RSS item to RSS feed
	 */
	public void addRSSItem(String title, String description, String link, Integer priority){
		SyndEntry item = new SyndEntryImpl();
		item.setTitle(title);
		item.setLink(link);
		
		/* Set RSS item description  */
		SyndContent itemDescription = new SyndContentImpl();
		itemDescription.setType("text/html");
		itemDescription.setValue("(Priority: " + priority.intValue() + ") " + description);
		item.setDescription(itemDescription);
		
		/* Add RSS item to feed */
		items.add(item);
		feed.setEntries(items);
	}
	
	/*
	 * Generate RSS feed and return string
	 */
	public String generateRSSFile(){
		try {
			output = new SyndFeedOutput();
			return output.outputString(feed);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
