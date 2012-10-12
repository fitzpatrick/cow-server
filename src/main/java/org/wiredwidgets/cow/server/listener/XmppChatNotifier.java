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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wiredwidgets.cow.server.listener;

import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author JKRANES
 */
public class XmppChatNotifier implements Notifier {

    private static Logger log = Logger.getLogger(XmppChatNotifier.class);
    
    @Autowired
    private XMPPConnection xmppConnection;
    
    @Autowired
    private XmppConnectionListener xmppConnectionListener;
    
    String notifyUser;
    String notifyPassword;
    
    Map<String, MultiUserChat> mucMap = new HashMap<String, MultiUserChat>();
    Map<String, Chat> chatMap = new HashMap<String, Chat>();

    @Override
    public void sendNotice(NoticeMessage message) {
        log.debug("Process: " + message.getProcessId());
        log.debug("Activity: " + message.getActivityName());
        log.debug("Participant: " + message.getParticipant());
        log.debug("Type: " + message.getParticipantType());

        // Create a connection to the jabber.org server.
        try {          
        	
            if (!xmppConnection.isConnected()){//!xmppConnection.isAuthenticated()) {
                xmppConnection.connect();
                xmppConnection.addConnectionListener(xmppConnectionListener);
            }
            
            if (!xmppConnection.isAuthenticated()){
                log.info("Logging in to chat server [" + xmppConnection.getServiceName() + "] as [" +  notifyUser + "]");
                xmppConnection.login(notifyUser, notifyPassword);
            }

            String messageString = "New task available from workflow engine: "
                    + "Process: " + message.getProcessId()
                    + "; Task: " + message.getActivityName();

            if (message.getEventName().equals("assign") && (message.getParticipantType().equals("assignee") 
                    || message.getParticipantType().equals("owner"))) {

                ChatManager chatmanager = xmppConnection.getChatManager();
                String chatUser = message.getParticipant() + "@" + xmppConnection.getServiceName();
                
                if (chatMap.get(chatUser) == null) {
                    chatMap.put(chatUser, chatmanager.createChat(chatUser, new MessageListener() {

                        @Override
                        public void processMessage(Chat chat, Message message) {
                            log.debug("Received message: " + message);
                        }
                    }));
                }

                log.debug("Sending chat message to: " + chatUser);
                chatMap.get(chatUser).sendMessage(messageString);
            } else if (message.getParticipantType().equals("candidate")) {
            	// assumes there is a chat room created for each role
                String chatRoom = message.getParticipant() + "@conference." + xmppConnection.getServiceName();
                
                if (mucMap.get(chatRoom) == null) {
                    log.debug("Creating new MultiUserChat for room " + chatRoom);
                    mucMap.put(chatRoom, new MultiUserChat(xmppConnection, chatRoom));
                }
                if (!mucMap.get(chatRoom).isJoined()) {
                    log.debug("Joining MultiUserChat for room " + chatRoom + " as " + notifyUser);
                    mucMap.get(chatRoom).join(notifyUser, notifyPassword);
                }
                
                log.debug("Sending MultiUserChat message: " + messageString);
                mucMap.get(chatRoom).sendMessage(messageString);
            }

        } catch (Exception e) {
        	log.error(e);
            // throw new RuntimeException(e);
        }
    }

    public String getNotifyPassword() {
        return notifyPassword;
    }

    public void setNotifyPassword(String notifyPassword) {
        this.notifyPassword = notifyPassword;
    }

    public String getNotifyUser() {
        return notifyUser;
    }

    public void setNotifyUser(String notifyUser) {
        this.notifyUser = notifyUser;
    }
    
    public void clearChatMap(){
    	mucMap.clear();
    }
    
    // Should be configured from the Spring bean as the destroy-method
    public void shutdown() {
        log.info("Shutting down XMPP connection....");
        xmppConnection.disconnect();
    }
        
}
