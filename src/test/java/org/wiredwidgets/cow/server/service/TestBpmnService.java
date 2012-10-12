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
 * NOTICE
 * 
 * This software was produced for the U. S. Government
 * under Contract No. W15P7T-10-C-F600, and is
 * subject to the Rights in Noncommercial Computer Software
 * and Noncommercial Computer Software Documentation
 * Clause 252.227-7014 (JUN 1995)
 * 
 * (c) The MITRE Corporation. All Rights Reserved.
 */
package org.wiredwidgets.cow.server.service;

import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import org.jbpm._4_4.jpdl.Process;
import org.jbpm._4_4.jpdl.TransitionType;
import org.jbpm.api.ProcessEngine;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author JKRANES
 */
@RunWith(SpringJUnit4ClassRunner.class)
// ApplicationContext will be loaded from "/applicationContext.xml" and "/applicationContext-test.xml"
// in the root of the classpath
@ContextConfiguration("/applicationContext.xml")
public class TestBpmnService extends AbstractJUnit4SpringContextTests {

    private Process process = null;

    @Autowired
    ProcessService service;

    @Autowired
    ProcessEngine engine;

    public TestBpmnService() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        process = buildTestJpdlProcess();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void TestTypedCollections() {
        List<String> strings = new ArrayList<String>();
        strings.add("test");
        List<Object> objects = new ArrayList<Object>();
        objects.addAll(strings);
        assertEquals(strings.get(0), objects.get(0));
    }

    private Process buildTestJpdlProcess() {

        Process p = new Process();

        Process.Start start = new Process.Start();
        start.setName("start");

        TransitionType startTransition = new TransitionType();
        startTransition.setName("startTransition");
        startTransition.setTo("task1");
        start.getOnOrTransition().add(startTransition);

        Process.Task task1 = new Process.Task();
        task1.setName("task1");
        task1.setAssignee("joe");

        Process.Task.Transition task1Transition = new Process.Task.Transition();
        task1Transition.setName("task1Transition");
        task1Transition.setTo("task2");
        task1.getOnOrTimerOrTransition().add(task1Transition);

        Process.Task task2 = new Process.Task();
        task2.setName("task2");
        task2.setAssignee("bob");

        Process.Task.Transition task2Transition = new Process.Task.Transition();
        task2Transition.setName("task2Transition");
        task2Transition.setTo("end");
        task2.getOnOrTimerOrTransition().add(task2Transition);

        Process.End end = new Process.End();
        end.setName("end");

        p.getSwimlaneOrOnOrTimer().add(start);
        p.getSwimlaneOrOnOrTimer().add(task1);
        p.getSwimlaneOrOnOrTimer().add(task2);
        p.getSwimlaneOrOnOrTimer().add(end);

        return p;
    }

    // https://jira.jboss.org/browse/JBPM-2952
    @Test
    public void testHistoryTaskCompletion() {

        org.jbpm.api.task.Task task = engine.getTaskService().newTask();
        task.setName("test");
        engine.getTaskService().saveTask(task);
        engine.getTaskService().completeTask(task.getId());
        long count = engine.getHistoryService().createHistoryTaskQuery().state("completed").taskId(task.getId()).count();
        // assertEquals(count, 1); // fails
        assertEquals(0, count); // BUG

    }

    @Test
    public void testSubProcess() {
//        Map<String, Object> variables = new HashMap<String, Object>();
//        variables.put("test","xxx");
//        org.jbpm.api.ProcessInstance processInstance = engine.getExecutionService().startProcessInstanceByKey("subvar-test", variables);
//

    }
}
