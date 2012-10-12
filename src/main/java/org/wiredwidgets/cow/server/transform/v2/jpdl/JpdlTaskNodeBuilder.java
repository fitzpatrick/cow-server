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
package org.wiredwidgets.cow.server.transform.v2.jpdl;

import org.jbpm._4_4.jpdl.Process;
import org.wiredwidgets.cow.server.api.model.v2.Task;
import org.wiredwidgets.cow.server.transform.v2.Builder;
import org.wiredwidgets.cow.server.transform.v2.ProcessContext;

/**
 *
 * @author JKRANES
 */
public class JpdlTaskNodeBuilder extends JpdlNodeBuilder<Process.Task, Task> {

    public JpdlTaskNodeBuilder(ProcessContext context, Task task) {
        super(context, new Process.Task(), task);
    }

    @Override
    protected void buildInternal() {
        Process.Task t = getNode();
        
        Task source = getActivity();
        
        // name is system generated unique value
        t.setName(getContext().generateId("task"));
        
        // set the key to the generated name, so we can later use this value to link active tasks back
        // to the process definition
        source.setKey(t.getName());
        
        // use the 'form' attribute to hold the user-entered short name, since we have
        // used the 'name' attribute for the system-generated name
        t.setForm(source.getName());
       
        t.setDescription(source.getDescription());
        t.setAssignee(source.getAssignee());
        t.setCandidateGroups(source.getCandidateGroups());
        t.setCandidateUsers(source.getCandidateUsers());
        t.setDuedate(source.getDueDate());
    }

    @Override
    protected void addTransition(Builder target, String transitionName) {
        Process.Task.Transition transition = new Process.Task.Transition();
        transition.setTo(target.getLinkTargetName());
        if (transitionName != null) {
            transition.setName(transitionName);
        }
        getNode().getOnOrTimerOrTransition().add(transition);
    }

    @Override
    public String getLinkTargetName() {
        return getNode().getName();
    }
}