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

import org.jbpm._4_4.jpdl.BooleanValueType;
import org.jbpm._4_4.jpdl.On;
import org.jbpm._4_4.jpdl.Process;
import org.jbpm._4_4.jpdl.Timer;
import org.jbpm._4_4.jpdl.Variable;
import org.jbpm._4_4.jpdl.WireObjectType;
import org.wiredwidgets.cow.server.api.model.v2.ServiceTask;
import org.wiredwidgets.cow.server.transform.v2.Builder;
import org.wiredwidgets.cow.server.transform.v2.ProcessContext;

/**
 *
 * @author JKRANES
 */
public class JpdlServiceTaskNodeBuilder extends JpdlNodeBuilder<Process.State, ServiceTask> {

    public JpdlServiceTaskNodeBuilder(ProcessContext context, ServiceTask task) {
        super(context, new Process.State(), task);
    }

    @Override
    protected void buildInternal() {
        Process.State target = getNode();
        
        // name is system generated unique value
        target.setName(getContext().generateId("servicetask"));
        
        ServiceTask source = getActivity();
        
        // set the key to the generated name, so we can later use this value to link active tasks back
        // to the process definition
        source.setKey(target.getName());
         
        target.setDescription(source.getDescription());
        target.getOnOrTransition().add(addEventListener(source.getMethod(), source.getUrl(), source.getContent(), source.getVar()));
          
      
    }

    @Override
    protected void addTransition(Builder target, String transitionName) {
        Process.State.Transition transition = new Process.State.Transition();
        transition.setTo(target.getLinkTargetName());
        if (transitionName != null) {
            transition.setName(transitionName);
        }
        getNode().getOnOrTransition().add(transition);
    }

    @Override
    public String getLinkTargetName() {
        return getNode().getName();
    }
    
    private On addEventListener(String method, String url, String  content, String var) {
    
        On on = new On();
        on.setEvent("start");
        Timer.EventListener listener = new Timer.EventListener();
        // note that we use the expr property rather than class in order to use a Spring bean
        listener.setExpr("#{serviceInvokerListener}");
        listener.setCache(BooleanValueType.FALSE);

        addProperty(listener, "method", method);
        addProperty(listener, "url", url);
        addProperty(listener, "content", content);
        addProperty(listener, "var", var);
        on.getEventListenerGroup().add(listener);
        return on;      
    }
    
    private void addProperty(Timer.EventListener listener, String name, String value) {
        if (value != null) {
            WireObjectType.Property prop = new WireObjectType.Property();
            prop.setName(name);
            Variable.String variable = new Variable.String();
            variable.setValue(value);
            prop.setString(variable);        
            listener.getDescriptionOrFactoryOrConstructor().add(prop);
        }
    }
     
}