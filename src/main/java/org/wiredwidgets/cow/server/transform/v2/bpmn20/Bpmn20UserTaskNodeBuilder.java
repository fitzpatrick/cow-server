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

package org.wiredwidgets.cow.server.transform.v2.bpmn20;

import javax.xml.bind.JAXBElement;
import org.wiredwidgets.cow.server.api.model.v2.Task;
import org.wiredwidgets.cow.server.transform.v2.ProcessContext;
import org.omg.spec.bpmn._20100524.model.ResourceAssignmentExpression;
import org.omg.spec.bpmn._20100524.model.TFormalExpression;
import org.omg.spec.bpmn._20100524.model.TPotentialOwner;
import org.omg.spec.bpmn._20100524.model.TUserTask;

/**
 *
 * @author JKRANES
 */
public class Bpmn20UserTaskNodeBuilder extends Bpmn20FlowNodeBuilder<TUserTask, Task> {

    public Bpmn20UserTaskNodeBuilder(ProcessContext context, Task task) {
        super(context, new TUserTask(), task);
    }

    @Override
    protected void buildInternal() {
        
        Task source = getActivity();

        TUserTask t = getNode();
        t.setId(getContext().generateId("_"));
        source.setKey(t.getName());
        t.setName(source.getName());

        // handle assignment
        String actorName = null;
        if (source.getAssignee() != null) {
            actorName = source.getAssignee();
        }
        else if (getActivity().getCandidateUsers() != null) {
            actorName = getActivity().getCandidateUsers();
        }
        else if (source.getCandidateGroups() != null) {
            actorName = source.getCandidateGroups();
        }

        TFormalExpression formalExpr = new TFormalExpression();
        // formalExpr.setId(actorName);
        formalExpr.getContent().add(actorName);

        ResourceAssignmentExpression resourceExpr = new ResourceAssignmentExpression();
        resourceExpr.setExpression(factory.createFormalExpression(formalExpr));

        TPotentialOwner owner = new TPotentialOwner();
        owner.setResourceAssignmentExpression(resourceExpr);
        
        t.getResourceRoles().add(factory.createPotentialOwner(owner));

    }
    
    @Override
    protected JAXBElement<TUserTask> createNode() {
        return factory.createUserTask(getNode());
    }

}
