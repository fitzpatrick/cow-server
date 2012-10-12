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
package org.wiredwidgets.cow.server.transform.v2;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import org.wiredwidgets.cow.server.api.model.v2.Task;
import org.wiredwidgets.cow.server.api.model.v2.ObjectFactory;
import org.jbpm._4_4.jpdl.Process;
import org.junit.runner.RunWith;
import org.wiredwidgets.cow.server.api.model.v2.Activities;
import org.wiredwidgets.cow.server.transform.v2.bpmn20.Bpmn20ProcessBuilder;
import org.wiredwidgets.cow.server.transform.v2.jpdl.JpdlProcessBuilder;
import org.omg.spec.bpmn._20100524.model.Definitions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author JKRANES
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/applicationContext.xml")
public class TestBypass {

    @Autowired
    JpdlProcessBuilder jpdlProcessBuilder;

    @Autowired
    Bpmn20ProcessBuilder bpmn20ProcessBuilder;

//    @Test
//    public void testTaskBuilderJpdl() {
//
//        org.wiredwidgets.cow.server.api.model.v2.Process process = new org.wiredwidgets.cow.server.api.model.v2.Process();
//        Task task = new Task();
//        task.setName("test");
//        task.setBypassable(true);
//        ObjectFactory of = new ObjectFactory();
//        process.setActivity(of.createTask(task));
//        
//        Process result = jpdlProcessBuilder.build(process);
//
//        assertEquals(result.getSwimlanesAndOnsAndTimers().get(1).getClass(), Process.Fork.class);
//
//    }
//
    @Test
    public void testTaskBuilderBpmn20() {

        org.wiredwidgets.cow.server.api.model.v2.Process process = new org.wiredwidgets.cow.server.api.model.v2.Process();

        ObjectFactory of = new ObjectFactory();
        Activities set = this.getTestSequentialSet(of);
        set.setBypassable(true);
        process.setActivity(of.createActivities(set));

        Definitions result = bpmn20ProcessBuilder.build(process);
        assertTrue(true);

    }

    private List<Process.Task.Transition> getTransitions(Process.Task task) {
        List<Object> objects = task.getOnOrTimerOrTransition();
        List<Process.Task.Transition> transitions = new ArrayList<Process.Task.Transition>();
        for (Object object : objects) {
            if (object instanceof Process.Task.Transition) {
                transitions.add((Process.Task.Transition)object);
            }
        }
        return transitions;
    }

    private Process.Task.Transition getTaskTransition(Process.Task task, int i) {
        return getTransitions(task).get(i);
    }
   
    private Activities getTestSequentialSet(ObjectFactory of) {
        return getTestSequentialSet(of, 2);
    }

    private Activities getTestSequentialSet(ObjectFactory of, int count) {
        Activities set = new Activities();
        set.setSequential(Boolean.TRUE);
        for (int i = 0; i < count; i++) {
            Task task = new Task();
            task.setName("test" + i);
            set.getActivities().add(of.createTask(task));
        }
        return set;
    }
}
