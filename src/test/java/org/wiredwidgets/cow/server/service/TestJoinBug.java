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

import static org.junit.Assert.*;
import java.util.List;
import org.jbpm.api.Configuration;
import org.jbpm.api.ExecutionService;
import org.jbpm.api.NewDeployment;
import org.jbpm.api.ProcessEngine;
import org.jbpm.api.ProcessInstance;
import org.jbpm.api.RepositoryService;
import org.jbpm.api.task.Task;
import org.junit.Test;


/**
 *
 * @author JKRANES
 */


// @ContextConfiguration("/applicationContext.xml")
public class TestJoinBug  {

    private Process process = null;

    @Test
    public void TestJoinBug() {
        ProcessEngine processEngine = Configuration.getProcessEngine();
        RepositoryService repService = processEngine.getRepositoryService();
        ExecutionService executionService = processEngine.getExecutionService();
        org.jbpm.api.TaskService taskService = processEngine.getTaskService();

        NewDeployment newDeployment = repService.createDeployment();
        newDeployment.addResourceFromClasspath("join-issue.jpdl.xml");
        String deploymentId = newDeployment.deploy();    
        ProcessInstance processInstance = executionService.startProcessInstanceByKey("join_test");

        // tasks 1,2 and 3 are all active

        List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
        assertEquals(3, tasks.size());

        Task task1 = taskService.createTaskQuery().processInstanceId(processInstance.getId()).activityName("task1").uniqueResult();
        assertNotNull(task1);

        Task task2 = taskService.createTaskQuery().processInstanceId(processInstance.getId()).activityName("task2").uniqueResult();
        assertNotNull(task2);

        Task task3 = taskService.createTaskQuery().processInstanceId(processInstance.getId()).activityName("task3").uniqueResult();
        assertNotNull(task3);

        // complete task 1
        taskService.completeTask(task1.getId());

        tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();

        // task3 should be still active, but it is not
        // ** TEST FAILS HERE -- the process is now complete and no tasks are active **
        // assertEquals(1, tasks.size());

    }
}
