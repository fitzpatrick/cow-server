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

package org.wiredwidgets.cow.server.listener;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.XMPPConnection;
import org.springframework.beans.factory.annotation.Autowired;

/**
* XMPP connection listener that receives a notification if the chat server ends abruptly.
* Once the chat server goes down, this disconnects the xmpp connection and clears the 
* group chat map to close all chat sessions.
* 
* @author FITZPATRICK
*/
public class XmppConnectionListener implements ConnectionListener {

	private static Logger log = Logger.getLogger(XmppConnectionListener.class);
	
	@Autowired
    private XmppChatNotifier xmppChatNotifier;
	
	@Autowired
	private XMPPConnection xmppConnection;
	
	@Override
	public void connectionClosed() {
		log.info("Connection closed to chat server...");
		xmppConnection.disconnect();
        xmppChatNotifier.clearChatMap();
	}

	@Override
	public void connectionClosedOnError(Exception e) {
		log.info("Connection closed to chat server...");
		xmppConnection.disconnect();
        xmppChatNotifier.clearChatMap();
	}

	@Override
	public void reconnectingIn(int seconds) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reconnectionSuccessful() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reconnectionFailed(Exception e) {
		// TODO Auto-generated method stub
		
	}

}
