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

import org.jbpm._4_4.jpdl.ObjectFactory;
import org.jbpm._4_4.jpdl.ParameterType;
import org.jbpm._4_4.jpdl.Process;
import org.wiredwidgets.cow.server.api.model.v2.Parameter;
import org.wiredwidgets.cow.server.api.model.v2.SubProcess;
import org.wiredwidgets.cow.server.transform.v2.Builder;
import org.wiredwidgets.cow.server.transform.v2.ProcessContext;

/**
 *
 * @author JKRANES
 */
public class JpdlSubProcessNodeBuilder extends JpdlNodeBuilder<Process.SubProcess, SubProcess> {

    private ObjectFactory factory = new ObjectFactory();

    public JpdlSubProcessNodeBuilder(ProcessContext context, SubProcess subProcess) {
        super(context, new Process.SubProcess(), subProcess);
    }

    @Override
    protected void buildInternal() {
        
        SubProcess source = getActivity();
        Process.SubProcess t = getNode();

        // name is system generated unique value
        t.setName(getContext().generateId("subprocess"));

        // set the key to the generated name, so we can later use this value to link active tasks back
        // to the process definition
        source.setKey(t.getName());

        t.setSubProcessKey(source.getSubProcessKey());
        
        Parameter p = new Parameter();
        p.setSubvar("_complete");  
        p.setExpr("true");
      
        t.getParameterInOrParameterOutOrTimer().add(factory.createProcessSubProcessParameterIn(convert(p)));
        
        for (Parameter param : source.getParameterIns()) {
            t.getParameterInOrParameterOutOrTimer().add(factory.createProcessSubProcessParameterIn(convert(param)));
        }
        for (Parameter param : source.getParameterOuts()) {
            t.getParameterInOrParameterOutOrTimer().add(factory.createProcessSubProcessParameterOut(convert(param)));
        }     
        
        // Use an output param to signal that the subprocess has completed
        // This will be used in the completion status calculation
        Parameter p2 = new Parameter();
        p2.setVar("_" + source.getSubProcessKey() + "_complete");
        p2.setSubvar("_complete");
        t.getParameterInOrParameterOutOrTimer().add(factory.createProcessSubProcessParameterOut(convert(p2)));
    }

    private ParameterType convert(Parameter param) {
        ParameterType p = new ParameterType();
        p.setExpr(param.getExpr());
        p.setSubvar(param.getSubvar());
        p.setVar(param.getVar());
        return p;
    }

    @Override
    protected void addTransition(Builder target, String transitionName) {
        Process.SubProcess.Transition transition = new Process.SubProcess.Transition();

        transition.setTo(target.getLinkTargetName());
        if (transitionName != null) {
            transition.setName(transitionName);
        }
        // NOTE: the following line does not work, even though the equivalent syntax works for Tasks.
        // The reason for this is unclear.
        // getNode().getParameterInOrParameterOutOrTimer().add(transition);

        // Need to add the Transition as a JAXBElement, otherwise we get a marshalling exception
        getNode().getParameterInOrParameterOutOrTimer().add(factory.createProcessSubProcessTransition(transition));
    }

    @Override
    public String getLinkTargetName() {
        return getNode().getName();
    }
}