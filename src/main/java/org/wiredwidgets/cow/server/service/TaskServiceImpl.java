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

package org.wiredwidgets.cow.server.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.log4j.Logger;
import org.jbpm.api.IdentityService;
import org.jbpm.api.history.HistoryTaskQuery;
import org.jbpm.pvm.internal.history.model.HistoryTaskImpl;
import org.wiredwidgets.cow.server.api.service.HistoryActivity;
import org.wiredwidgets.cow.server.api.service.HistoryTask;
import org.wiredwidgets.cow.server.api.service.Participation;
import org.wiredwidgets.cow.server.api.service.Task;
import org.wiredwidgets.cow.server.api.service.Variable;
import org.wiredwidgets.cow.server.api.model.v2.Process;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.transaction.annotation.Transactional;
import org.wiredwidgets.cow.server.api.model.v2.Activities;
import org.wiredwidgets.cow.server.api.model.v2.Activity;
/**
 *
 * @author JKRANES
 */
@Transactional
public class TaskServiceImpl extends AbstractCowServiceImpl implements TaskService {

    @Autowired
    IdentityService identityService;
    @Autowired
    ProcessInstanceService processInstanceService;
    public static Logger log = Logger.getLogger(TaskServiceImpl.class);
    
    private static TypeDescriptor JBPM_PARTICIPATION_LIST = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(org.jbpm.api.task.Participation.class));
    private static TypeDescriptor COW_PARTICIPATION_LIST = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(Participation.class));
    private static TypeDescriptor JBPM_TASK_LIST = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(org.jbpm.api.task.Task.class));
    private static TypeDescriptor COW_TASK_LIST = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(Task.class));
    private static TypeDescriptor JBPM_HISTORY_TASK_LIST = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(org.jbpm.api.history.HistoryTask.class));
    private static TypeDescriptor COW_HISTORY_TASK_LIST = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(HistoryTask.class));
    private static TypeDescriptor JBPM_HISTORY_ACTIVITY_LIST = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(org.jbpm.api.history.HistoryActivityInstance.class));
    private static TypeDescriptor COW_HISTORY_ACTIVITY_LIST = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(HistoryActivity.class));

    @Override
    public String createAdHocTask(Task task) {
        org.jbpm.api.task.Task newTask = this.createOrUpdateTask(task);
        return newTask.getId();
    }

    @Override
    public void updateTask(Task task) {
        this.createOrUpdateTask(task);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Task> findPersonalTasks(String assignee) {
        List<org.jbpm.api.task.Task> tasks = taskService.findPersonalTasks(assignee);
        return this.convertTasks(tasks);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Task> findAllUnassignedTasks() {
        List<org.jbpm.api.task.Task> tasks = taskService.createTaskQuery().unassigned().list();
        return this.convertTasks(tasks);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Task> findGroupTasks(String user) {
        List<org.jbpm.api.task.Task> tasks = taskService.findGroupTasks(user);
        return this.convertTasks(tasks);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Task> findOrphanedTasks() {

        // create a map where key = groupId, and value = List of users in that group
        Map<String, List<String>> groupsMap = new HashMap<String, List<String>>();
        for (org.jbpm.api.identity.User user : identityService.findUsers()) {
            for (String groupId : identityService.findGroupIdsByUser(user.getId())) {
                if (groupsMap.get(groupId) == null) {
                    groupsMap.put(groupId, new ArrayList<String>());
                }
                groupsMap.get(groupId).add(user.getId());
            }
        }

        List<org.jbpm.api.task.Task> orphaned = new ArrayList<org.jbpm.api.task.Task>();

        for (org.jbpm.api.task.Task task : taskService.createTaskQuery().list()) {
            log.debug("task: " + task.getId());
            boolean isOrphaned = true;
            if (task.getAssignee() != null && identityService.findUserById(task.getAssignee()) != null) {
                log.debug("assigned and assignee exists");
                isOrphaned = false;
            } else if(task.getAssignee() == null) {
                
                for (org.jbpm.api.task.Participation participation : taskService.getTaskParticipations(task.getId())) {
                    log.debug("participation type: " + participation.getType());
                    if (participation.getType().equals(org.jbpm.api.task.Participation.CANDIDATE)) {
                        log.debug("candidate: " + participation.getGroupId());
                        // does the group have at least one user, other than 'root' ?
                        if (groupsMap.get(participation.getGroupId()) != null 
                                && groupsMap.get(participation.getGroupId()).size() > 1) {
                            isOrphaned = false;
                        }
                    }
                } // for participation
            }  // end else
            if (isOrphaned) {
                orphaned.add(task);
            }
        } // for task
        return convertTasks(orphaned);
    }

    @Transactional(readOnly = true)
    @Override
    public Task getTask(String id) {
        return this.converter.convert(taskService.getTask(id), Task.class);
    }

    @Override
    public void completeTask(String id, String outcome, Map<String, String> variables) {
        log.debug("Completing task id: " + id);
        log.debug("Outcome: " + outcome);
        log.debug("Variables: " + variables);
        org.jbpm.api.task.Task task = taskService.getTask(id);

        if (variables != null && variables.size() > 0) {
            // taskService.completeTask(id, variables);   // Should work but does not
            // Workaround for bug: see https://jira.jboss.org/browse/JBPM-2951
            
            // executionId is null for ad-hoc tasks
            if (task.getExecutionId() != null) {
                setVariables(task.getExecutionId(), variables);
            }
        }
        if (outcome == null) {
            taskService.completeTask(id);
        } else {
            taskService.completeTask(id, outcome);
        }
        
        // XXX Workaround for https://jira.jboss.org/browse/JBPM-2952
        org.jbpm.api.history.HistoryTask ht = historyService.createHistoryTaskQuery().taskId(id).uniqueResult();
        if (ht instanceof HistoryTaskImpl) {
            HistoryTaskImpl impl = (HistoryTaskImpl) ht;
            if (impl.getState() == null) {
                impl.setState("completed");
                impl.setEndTime(new Date());
                if (task.getAssignee() != null) {
                    impl.setAssignee(task.getAssignee());
                }
            }
        }
    }

    @Override
    public void takeTask(String taskId, String userId) {
    	taskService.takeTask(taskId, userId);
    }

    @Override
    public void removeTaskAssignment(String taskId) {
        taskService.assignTask(taskId, null);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Task> findAllTasks() {
        List<org.jbpm.api.task.Task> tasks = taskService.createTaskQuery().list();
        return this.convertTasks(tasks);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Task> findAllTasksByProcessInstance(String id) {
        List<org.jbpm.api.task.Task> tasks = taskService.createTaskQuery().processInstanceId(id).list();
        return this.convertTasks(tasks);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Task> findAllTasksByProcessKey(String key) {
        List<org.jbpm.api.task.Task> tasks = new ArrayList<org.jbpm.api.task.Task>();
        List<org.jbpm.api.ProcessDefinition> definitions = this.repositoryService.createProcessDefinitionQuery().processDefinitionKey(key).list();
        for (org.jbpm.api.ProcessDefinition definition : definitions) {
            tasks.addAll(this.taskService.createTaskQuery().processDefinitionId(definition.getId()).list());
        }
        return this.convertTasks(tasks);
    }

    @Transactional(readOnly = true)
    @Override
    public HistoryTask getHistoryTask(String id) {
        return this.converter.convert(historyService.createHistoryTaskQuery().taskId(id).uniqueResult(), HistoryTask.class);
    }

    @Transactional(readOnly = true)
    @Override
    public List<HistoryTask> getHistoryTasks(String processId) {
        HistoryTaskQuery htq = this.historyService.createHistoryTaskQuery();
        htq.executionId(processId);
        return convertHistoryTasks(htq.list());
    }

    @Transactional(readOnly = true)
    @Override
    public List<HistoryTask> getHistoryTasks(String assignee, Date startDate, Date endDate) {
        HistoryTaskQuery htq = this.historyService.createHistoryTaskQuery();
        htq = htq.state("completed");
        if (assignee != null) {
            htq = htq.assignee(assignee);
        }
        // XXX see below -- cannot query by end date
//        if (startDate != null) {
//            htq = htq.startedAfter(startDate);
//        }

        // we can at least rule out any tasks started after the requested end date
        if (endDate != null) {
            htq = htq.startedBefore(endDate);
        }

        // use current date for endDate if null
        if (endDate == null) {
            endDate = new Date();
        }

        // use epoch for start date if null
        if (startDate == null) {
            startDate = new Date(0);
        }

        // XXX brute force iteration -- JBPM API does not support query by completion date
        List<org.jbpm.api.history.HistoryTask> results = new ArrayList<org.jbpm.api.history.HistoryTask>();
        List<org.jbpm.api.history.HistoryTask> candidates = htq.list();
        for (org.jbpm.api.history.HistoryTask task : candidates) {
            if (task.getEndTime().getTime() > startDate.getTime() && task.getEndTime().getTime() < endDate.getTime()) {
                results.add(task);
            }
        }
        return this.convertHistoryTasks(results);
    }

    @Override
    public List<HistoryActivity> getHistoryActivities(String processInstanceId) {
        return this.convertHistoryActivities(historyService.createHistoryActivityInstanceQuery().processInstanceId(processInstanceId).list());
    }

    @Transactional(readOnly = true)
    @Override
    public List<Participation> getTaskParticipations(String taskId) {
        return this.convertParticipations(this.taskService.getTaskParticipations(taskId));
    }

    @Override
    public void addTaskParticipatingUser(String taskId, String userId, String type) {
        this.taskService.addTaskParticipatingUser(taskId, userId, type);
    }

    @Override
    public void removeTaskParticipatingUser(String taskId, String userId, String type) {
        this.taskService.removeTaskParticipatingUser(taskId, userId, type);
    }

    @Override
    public void addTaskParticipatingGroup(String taskId, String groupId, String type) {
        this.taskService.addTaskParticipatingGroup(taskId, groupId, type);
    }

    @Override
    public void removeTaskParticipatingGroup(String taskId, String groupId, String type) {
        this.taskService.removeTaskParticipatingGroup(taskId, groupId, type);
    }
    
    private org.jbpm.api.task.Task createOrUpdateTask(Task source) {
        org.jbpm.api.task.Task target = null;
        boolean newTask = false;
        if (source.getId() == null) {
            newTask = true;
            target = this.taskService.newTask();
            this.taskService.saveTask(target);
        } else {
            target = this.taskService.getTask(source.getId());
        }
        if (target == null) {
            return null;
        }
        if (source.getAssignee() != null) {
            target.setAssignee(source.getAssignee());
        }
        if (source.getDescription() != null) {
            target.setDescription(source.getDescription());
        }
        if (source.getDueDate() != null) {
            target.setDuedate(this.convert(source.getDueDate()));
        }
        if (source.getName() != null) {
            target.setName(source.getName());
        }
        if (source.getPriority() != null) {
            target.setPriority(source.getPriority());
        }
        if (source.getProgress() != null) {
            target.setProgress(source.getProgress());
        }

        // convert variables
        if (source.getVariables() != null && source.getVariables().getVariables().size() > 0) {
            Map<String, Object> variables = new HashMap<String, Object>();
            for (Variable variable : source.getVariables().getVariables()) {
                variables.put(variable.getName(), variable.getValue());
            }
            this.taskService.setVariables(target.getId(), variables);
        }
        return target;
    }

    private Date convert(XMLGregorianCalendar source) {
        return source.toGregorianCalendar().getTime();
    }

    private List<Participation> convertParticipations(List<org.jbpm.api.task.Participation> source) {
        return (List<Participation>) converter.convert(source, JBPM_PARTICIPATION_LIST, COW_PARTICIPATION_LIST);
    }

    private List<Task> convertTasks(List<org.jbpm.api.task.Task> source) {
        return (List<Task>) converter.convert(source, JBPM_TASK_LIST, COW_TASK_LIST);
    }

    private List<HistoryTask> convertHistoryTasks(List<org.jbpm.api.history.HistoryTask> source) {
        return (List<HistoryTask>) this.converter.convert(source, JBPM_HISTORY_TASK_LIST, COW_HISTORY_TASK_LIST);
    }

    private List<HistoryActivity> convertHistoryActivities(List<org.jbpm.api.history.HistoryActivityInstance> source) {
        return (List<HistoryActivity>) this.converter.convert(source, JBPM_HISTORY_ACTIVITY_LIST, COW_HISTORY_ACTIVITY_LIST);
    }

    @Transactional(readOnly = true)
    @Override
    public Activity getWorkflowActivity(String processInstanceId, String key) {
        Process process = processInstanceService.getV2Process(processInstanceId);
        if  (process != null ) {
            return findActivity(process.getActivity().getValue(), key);
        }
        else {
            return null;
        }
    }
    
    private Activity findActivity(Activity activity, String key) {
        if (activity.getKey() != null && activity.getKey().equals(key)) {
            return activity;
        }
        else if (activity instanceof Activities) {
            for (JAXBElement<? extends Activity> jaxbActivity : ((Activities) activity).getActivities()) {
                Activity child = findActivity(jaxbActivity.getValue(), key);
                if (child != null) {
                    return child;
                }
            }
        }
        return null; 
    }
    

    
}
