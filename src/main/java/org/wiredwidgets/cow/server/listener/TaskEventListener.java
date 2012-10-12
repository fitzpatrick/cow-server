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

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.jbpm.api.TaskService;
import org.jbpm.api.task.Participation;
import org.jbpm.api.task.Task;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Event listener to handle events related to tasks
 * @author JKRANES
 */
public class TaskEventListener extends BpmEventListener {

    private static Logger log = Logger.getLogger(TaskEventListener.class);
    
    @Autowired
    TaskService taskService; 
    
    @Autowired
    NotifierRegistry notifierRegistry;    
    
    String eventName;
        
    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }
    
    @Override
    public void run() {
        // TaskService taskService = processEngine.getTaskService();
        String activityName = activity.getName();
        String processId = execution.getId();
        log.debug("Listener: " + activity.getName() + ", " + eventName + " (" + processId + ")");
        List<Task> tasks = taskService.createTaskQuery().activityName(activityName).executionId(processId).list();

        List<NoticeMessage> messages = new ArrayList<NoticeMessage>();

        for (Task task : tasks) {
            log.debug("Task: " + task.getId() + " (" + task.getExecutionId() + ")");
            log.debug("Assignee: " + task.getAssignee());
            if (task.getAssignee() != null) {
                addMessage(messages, processId, task.getFormResourceName(), task.getDescription(), eventName, task.getAssignee(), Participation.OWNER);
            }
            List<Participation> participations = taskService.getTaskParticipations(task.getId());
            for (Participation participation : participations) {
                if (participation.getUserId() != null) {
                    log.debug(participation.getType() + ": " + participation.getUserId());
                    addMessage(messages, processId, task.getFormResourceName(), task.getDescription(), eventName, participation.getUserId(), participation.getType());
                }
                if (participation.getGroupId() != null) {
                    log.debug(participation.getType() + ": " + participation.getGroupId());
                    addMessage(messages, processId, task.getFormResourceName(), task.getDescription(), eventName, participation.getGroupId(), Participation.CANDIDATE);
                }
            }
        }

        for (NoticeMessage message : messages) {
            for (Notifier notifier : notifierRegistry.getNotifiers()) {
                notifier.sendNotice(message);
            }
        }
    }

    private void addMessage(List<NoticeMessage> messages, String processId, String activityName, String activityDescription, String eventName, String userId, String participationType) {
        NoticeMessage message = new NoticeMessage();
        message.setProcessId(processId);
        message.setActivityName(activityName);
        message.setActivityDescription(activityDescription);
        message.setEventName(eventName);
        message.setParticipant(userId);
        message.setParticipantType(participationType);
        messages.add(message);
    }

}
