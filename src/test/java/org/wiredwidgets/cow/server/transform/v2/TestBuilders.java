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

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.transform.stream.StreamResult;
import org.junit.Test;
import static org.junit.Assert.*;
import org.wiredwidgets.cow.server.api.model.v2.Task;
import org.wiredwidgets.cow.server.api.model.v2.ObjectFactory;
import org.jbpm._4_4.jpdl.Process;
import org.jbpm._4_4.jpdl.TransitionType;
import org.junit.runner.RunWith;
import org.wiredwidgets.cow.server.api.model.v2.Activities;
import org.wiredwidgets.cow.server.api.model.v2.Decision;
import org.wiredwidgets.cow.server.api.model.v2.Loop;
import org.wiredwidgets.cow.server.api.model.v2.Option;
import org.wiredwidgets.cow.server.api.model.v2.ServiceTask;
import org.wiredwidgets.cow.server.transform.v2.jpdl.JpdlProcessBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.wiredwidgets.cow.server.api.model.v2.SubProcess;

/**
 *
 * @author JKRANES
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/applicationContext.xml")
public class TestBuilders {

    @Autowired
    JpdlProcessBuilder jpdlProcessBuilder;
    
    @Autowired
    Jaxb2Marshaller marshaller;

    @Test
    public void testTaskBuilder() {

        org.wiredwidgets.cow.server.api.model.v2.Process process = new org.wiredwidgets.cow.server.api.model.v2.Process();
        Task task = new Task();
        task.setName("test");
        ObjectFactory of = new ObjectFactory();
        process.setActivity(of.createTask(task));

        Process result = jpdlProcessBuilder.build(process);
        Process.Start start = (Process.Start)getProcessElement(result, Process.Start.class, 0);
        Process.Task t = (Process.Task)getProcessElement(result, Process.Task.class, 0);
        Process.End end = (Process.End)getProcessElement(result, Process.End.class, 0);

        // assertEquals(3, result.getSwimlaneOrOnOrTimer().size());
        assertEquals("test", t.getForm());
        TransitionType t1 = (TransitionType) start.getOnOrTransition().get(0);
        assertEquals(t1.getTo(), t.getName());

        Process.Task.Transition t2 = (Process.Task.Transition) getTaskTransition(t,0);
        assertEquals(t2.getTo(), end.getName());
    }
    
    @Test
    public void testServiceTaskBuilder() {

        org.wiredwidgets.cow.server.api.model.v2.Process process = new org.wiredwidgets.cow.server.api.model.v2.Process();
        ServiceTask task = new ServiceTask();
        task.setMethod("GET");
        task.setUrl("url");
        task.setVar("test");
        ObjectFactory of = new ObjectFactory();
        process.setActivity(of.createServiceTask(task));

        Process result = jpdlProcessBuilder.build(process);
    
        System.out.println(marshalToString(result));
        assertTrue(true);

    }    
    
    @Test
    public void testSubProcessBuilder() {
        org.wiredwidgets.cow.server.api.model.v2.Process process = new org.wiredwidgets.cow.server.api.model.v2.Process();
        SubProcess sub = new SubProcess();
        sub.setSubProcessKey("xxx");
        ObjectFactory of = new ObjectFactory();
        process.setActivity(of.createSubProcess(sub));
        Process result = jpdlProcessBuilder.build(process);
        System.out.println(marshalToString(result));
        assertTrue(true);      
    }
    
    private String marshalToString(Object obj) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        marshaller.marshal(obj, new StreamResult(baos));
        return baos.toString();
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



    @Test
    public void testSequentialSetBuilder() {

        org.wiredwidgets.cow.server.api.model.v2.Process process = new org.wiredwidgets.cow.server.api.model.v2.Process();

        ObjectFactory of = new ObjectFactory();
        Activities set = this.getTestSequentialSet(of);
        process.setActivity(of.createActivities(set));

        Process result = jpdlProcessBuilder.build(process);
        
        Process.Start start = (Process.Start)getProcessElement(result, Process.Start.class, 0);       
        Process.Task t1 = (Process.Task)getProcessElement(result, Process.Task.class, 0);
        Process.Task t2 = (Process.Task)getProcessElement(result, Process.Task.class, 1);
        Process.End end = (Process.End)getProcessElement(result, Process.End.class, 0);        

        // assertEquals(4, result.getSwimlaneOrOnOrTimer().size());

        // form attribute used to hold the namme
        assertEquals("test0", t1.getForm());
        assertEquals("test1", t2.getForm());
        TransitionType tt1 = (TransitionType) start.getOnOrTransition().get(0);
        assertEquals(tt1.getTo(), t1.getName());
        Process.Task.Transition tt2 = (Process.Task.Transition) getTaskTransition(t1,0);
        Process.Task.Transition tt3 = (Process.Task.Transition) getTaskTransition(t2,0);
        assertEquals(tt2.getTo(), t2.getName());
        assertEquals(tt3.getTo(), end.getName());
    }

    @Test
    public void testNestedSequentialSetBuilder() {

        org.wiredwidgets.cow.server.api.model.v2.Process process = new org.wiredwidgets.cow.server.api.model.v2.Process();

        ObjectFactory of = new ObjectFactory();
        Activities set = this.getTestSequentialSet(of);
        Activities set2 = this.getTestSequentialSet(of);
        set.getActivities().add(of.createActivities(set2));

        process.setActivity(of.createActivities(set));

        Process result = jpdlProcessBuilder.build(process);      
        Process.Task t1 = (Process.Task)getProcessElement(result, Process.Task.class, 0);      
        Process.Task t2 = (Process.Task)getProcessElement(result, Process.Task.class, 1);
        Process.Task t3 = (Process.Task)getProcessElement(result, Process.Task.class, 2);
        Process.Task t4 = (Process.Task)getProcessElement(result, Process.Task.class, 3);
        Process.End end = (Process.End)getProcessElement(result, Process.End.class, 0);    

        Process.Task.Transition tt1 = getTaskTransition(t1, 0);
        Process.Task.Transition tt2 = getTaskTransition(t2, 0);
        Process.Task.Transition tt3 = getTaskTransition(t3, 0);
        Process.Task.Transition tt4 = getTaskTransition(t4, 0);
        
        assertEquals(tt1.getTo(), t2.getName());
        assertEquals(tt2.getTo(), t3.getName());
        assertEquals(tt3.getTo(), t4.getName());
        assertEquals(tt4.getTo(), end.getName());
    }

    @Test
    public void testDecisionBuilder() {
        org.wiredwidgets.cow.server.api.model.v2.Process process = new org.wiredwidgets.cow.server.api.model.v2.Process();

        ObjectFactory of = new ObjectFactory();
        Decision decision = new Decision();
        Task decisionTask = new Task();
        decisionTask.setName("decision");
        decision.setTask(decisionTask);

        int optionCount = 2;
        for (int i = 0; i < optionCount; i++) {
            Task optionTask = new Task();
            optionTask.setName("option" + i);
            Option option = new Option();
            option.setActivity(of.createActivity(optionTask));
            decision.getOptions().add(option);
        }

        process.setActivity(of.createActivity(decision));

        Process result = jpdlProcessBuilder.build(process);
        Process.Task t1 = (Process.Task)getProcessElement(result, Process.Task.class, 0);      
        Process.Task t2 = (Process.Task)getProcessElement(result, Process.Task.class, 1);  
        Process.Task t3 = (Process.Task)getProcessElement(result, Process.Task.class, 2);      

        Process.End end = (Process.End)getProcessElement(result, Process.End.class, 0);    

        Process.Task.Transition tt1a = (Process.Task.Transition) getTaskTransition(t1, 0);
        Process.Task.Transition tt1b = (Process.Task.Transition) getTaskTransition(t1, 1);
        Process.Task.Transition tt2 = (Process.Task.Transition) getTaskTransition(t2, 0);
        Process.Task.Transition tt3 = (Process.Task.Transition) getTaskTransition(t3, 0);

        // decision task links to both option tasks
        assertEquals(tt1a.getTo(), t2.getName());
        assertEquals(tt1b.getTo(), t3.getName());

        // both options link to end
        assertEquals(tt2.getTo(), end.getName());
        assertEquals(tt3.getTo(), end.getName());
    }

    @Test
    public void testLoopBuilder() {
        org.wiredwidgets.cow.server.api.model.v2.Process process = new org.wiredwidgets.cow.server.api.model.v2.Process();

        ObjectFactory of = new ObjectFactory();
        Loop loop = new Loop();

        Activities set = this.getTestSequentialSet(of, 2);
        loop.setActivity(of.createActivities(set));

        Task decisionTask = new Task();
        decisionTask.setName("decision");
        loop.setLoopTask(decisionTask);

        process.setActivity(of.createActivity(loop));

        Process result = jpdlProcessBuilder.build(process);
        // 0 is start node
        // 1 and 2 are sequential tasks
        // 3 is the decision task
        Process.Task t1 = (Process.Task)getProcessElement(result, Process.Task.class, 0); 
        Process.Task t2 = (Process.Task)getProcessElement(result, Process.Task.class, 1);  
        Process.Task t3 = (Process.Task)getProcessElement(result, Process.Task.class, 2);   

        Process.End end = (Process.End)getProcessElement(result, Process.End.class, 0); 

        Process.Task.Transition tt1 = (Process.Task.Transition) getTaskTransition(t1, 0);
        Process.Task.Transition tt2 = (Process.Task.Transition) getTaskTransition(t2, 0);
        Process.Task.Transition tt3a = (Process.Task.Transition) getTaskTransition(t3, 0);
        Process.Task.Transition tt3b = (Process.Task.Transition) getTaskTransition(t3, 1);

        assertEquals(tt1.getTo(), t2.getName());
        assertEquals(tt2.getTo(), t3.getName());

        // first transtion loops back to the start
        assertEquals(tt3a.getTo(), t1.getName());
        assertEquals(tt3a.getName(), loop.getRepeatName());

        // second transition goes forward (to end in this case)
        assertEquals(tt3b.getName(), loop.getDoneName());
        assertEquals(tt3b.getTo(), end.getName());

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
    
    private Process.Task.Transition getTaskTransition(Process.Task task, int i) {
        return getTransitions(task).get(i);
    }    
    
    /*
     * Returns the nth instance of the specified element type
     */
    private Object getProcessElement(Process process, Class clazz, int i) {
        int count = 0;
        Object returnVal = null;
        for (Object element : process.getSwimlaneOrOnOrTimer()) {
            if (element.getClass().isAssignableFrom(clazz)) {
                if (i == count++) {
                    returnVal = element;
                    break;
                }
            }
        }
        return returnVal;
    }
}
