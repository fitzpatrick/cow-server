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

import org.wiredwidgets.cow.server.api.service.User;
import java.util.Properties;
import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.apache.log4j.Logger;
import org.apache.qpid.transport.TransportException;
import org.jbpm.api.HistoryService;
import org.jbpm.pvm.internal.history.model.HistoryTaskImpl;
import org.wiredwidgets.cow.server.api.service.Task;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Notifies the message subscribers when different COW actions have been performed.  These tasks include taking a task,
 * completing a task, starting a workflow, and the creation of users.
 * 
 * @author FITZPATRICK
 */

public class AmqpNotifier {

    private static Logger log = Logger.getLogger(AmqpNotifier.class);
    @Autowired
    HistoryService historyService;
    
    String host;
    String port;
    
    private MessageProducer messageProducer;
    private Properties properties;
    private Session session;
    private Context context;
    private boolean initialized = false;
    

    /**
     * Initializes the qpid connection with a jndiName of cow, and a host and port taken from the cow-server.properties file.
     */
    public void init() {
    	String connectionFactorylocation = "amqp://guest:guest@clientid/test?brokerlist='tcp://" + host + ":" + port + "'";
    	String destinationType = "topic";
        String jndiName = "cow";
        String destinationName = "myTopic";
        
        try {
        	properties = new Properties();
	        properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.qpid.jndi.PropertiesFileInitialContextFactory");
	        properties.setProperty("connectionfactory.amqpConnectionFactory", connectionFactorylocation);
	        properties.setProperty(destinationType + "." + jndiName, destinationName);
	        
			context = new InitialContext(properties);
			ConnectionFactory connectionFactory = (ConnectionFactory) context.lookup("amqpConnectionFactory");
			Connection connection = connectionFactory.createConnection();
			connection.start();
			
			session=connection.createSession(false,Session.AUTO_ACKNOWLEDGE);
			Destination destination = (Destination) context.lookup(jndiName);

			messageProducer = session.createProducer(destination);
			initialized = true;
		} catch (Exception e){
			log.error(e.getMessage());
			initialized = false;
		}
    }

    /**
     * Publishes a message to qpid that describes what action a user has just performed on a task.  These actions may be 
     * either TaskTaken or TaskCompleted.  This calls checkInitialized to see if there is a valid connection to qpid.
     * 
     * @param task 			the task that has been acted upon
     * @param exchangeName	the exchange name for messaging
     * @param eventName		the event that correlates with the action
     * @param taskId		the id of the task that was acted upon
     */
    public void amqpTaskPublish(Task task, String exchangeName, String eventName, String taskId) {
        String info = "";
        if (checkInitialized()){
	        if (eventName.equals("TaskTaken")) {
	
	            try {
	                info = "eventType=" + eventName + ";" + "processID=" + task.getProcessInstanceId() + ";" + "taskID=" + task.getId() + ";" + "assignee=" + task.getAssignee() + ";";
	            } catch (Exception e) {
	                log.error(e.getMessage());
	            }
	        } else if (eventName.equals("TaskCompleted")) {
	            try {
	                org.jbpm.api.history.HistoryTask ht = historyService.createHistoryTaskQuery().taskId(taskId).uniqueResult();
	                if (ht instanceof HistoryTaskImpl) {
	                    HistoryTaskImpl impl = (HistoryTaskImpl) ht;
	                    String processName = impl.getExecutionId().split("\\.")[0];
	                    String processId = impl.getExecutionId().split("\\.")[1];
	                    info = "eventType=" + eventName + ";" + "processID=" + processName + "." + processId + ";" + "taskID=" + impl.getId() + ";" + "assignee=" + impl.getAssignee() + ";";
	                }
	            } catch (Exception e) {
	                log.error(e.getMessage());
	            }
	        }
	        sendMessage(info, exchangeName);
        }
    }

    /**
     * Publishes a message to qpid that describes what action a user has just performed on a process.  This calls 
     * checkInitialized to see if there is a valid connection to qpid.
     * 
     * @param processId		the id of the process that was acted upon
     * @param exchangeName	the exchange name for messaging
     * @param eventName		the event that correlates with the action
     */
    public void amqpProcessPublish(String processId, String exchangeName, String eventName) {
    	if (checkInitialized()){
	    	String info = "eventType=" + eventName + ";" + "processID=" + processId + ";";
	        sendMessage(info, exchangeName);
    	}
    }
    
    /**
     * Publishes a message to qpid that notifies that a new user was created.  This calls checkInitialized to see 
     * if there is a valid connection to qpid.
     * 
     * @param user			the user that was just created
     * @param exchangeName	the exchange name for messaging
     * @param eventName		the event that correlates with the action
     */
    public void amqpNewUserPublish(User user, String exchangeName, String eventName){
    	if (checkInitialized()){
	    	String info = "eventType=" + eventName + ";" + "userID=" + user.getId() + ";";
	    	sendMessage(info, exchangeName);
    	}
    }
    
    /**
     * Get method needed by spring to use the host from the cow-server.properties file
     * 
     * @return	returns the host string of the qpid server
     */
    public String getHost() {
        return host;
    }

    /**
     * Set method needed by spring to use the host from the cow-server.properties file
     * 
     * @param host	string value of the host from the cow-server.properties file
     */
    public void setHost(String host) {
        this.host = host;
    }
    
    /**
     * Get method needed by spring to use the port from the cow-server.properties file
     * 
     * @return	returns the port string of the qpid server
     */
    public String getPort() {
        return port;
    }
    
    /**
     * Set method needed by spring to use the port from the cow-server.properties file
     * 
     * @param port	string value of the port from the cow-server.properties file
     */
    public void setPort(String port) {
        this.port = port;
    }
    
    // Sends a message using the exchange name over qpid
    private void sendMessage(String message, String exchangeName){
    	TextMessage textMessage;
    	properties.setProperty("topic" + "." + "cow", exchangeName);
    	
    	try {
    		context = new InitialContext(properties);
        	Destination destination = (Destination) context.lookup("cow");
        	messageProducer = session.createProducer(destination);
    	    textMessage = session.createTextMessage(message);
			messageProducer.send(textMessage);
		} catch (Exception e) {
			log.error(e.getMessage());
			initialized = false;
		} 
    }
    
    // Checks to see if the qpid connection has been initialized.  If not, then it tries to connect again.
    private boolean checkInitialized(){
    	if (initialized){
    		return true;
    	} else{
    		init();
    		if (initialized){
    			return true;
    		}
    	}
    	return false;
    }
}
