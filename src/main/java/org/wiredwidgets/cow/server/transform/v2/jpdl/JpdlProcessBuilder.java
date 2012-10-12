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

package org.wiredwidgets.cow.server.transform.v2.jpdl;

import org.jbpm._4_4.jpdl.BooleanValueType;
import org.jbpm._4_4.jpdl.On;
import org.jbpm._4_4.jpdl.Process;
import org.jbpm._4_4.jpdl.Timer;
import org.jbpm._4_4.jpdl.Variable;
import org.jbpm._4_4.jpdl.WireObjectType;
import org.jbpm.api.model.Event;
import org.wiredwidgets.cow.server.api.model.v2.Activity;
import org.wiredwidgets.cow.server.transform.v2.AbstractProcessBuilder;
import org.wiredwidgets.cow.server.transform.v2.Builder;

/**
 *
 * @author JKRANES
 */
public class JpdlProcessBuilder extends AbstractProcessBuilder<Process> {
    
    // NOTE: this expression specifies the name of a Spring bean to be used as the event listener
    // See http://docs.jboss.org/jbpm/v4/devguide/html_single/#spring_usage
    private static String EVENT_LISTENER_EXPR = "#{taskEventListener}";
    
    @Override
    public Process build(org.wiredwidgets.cow.server.api.model.v2.Process source) {
        Process target = new Process();
        target.setKey(source.getKey());
        target.setName(source.getName());

        JpdlProcessContext context = new JpdlProcessContext(source, target);
        
        // event listeners
        target.getSwimlaneOrOnOrTimer().add(addEventListener(Event.START));
        target.getSwimlaneOrOnOrTimer().add(addEventListener(Event.ASSIGN));
        
        Builder startBuilder = new JpdlStartNodeBuilder(context);
        startBuilder.build(null);

        Activity activity = source.getActivity().getValue();
        Builder builder = createActivityBuilder(context, activity);
       // builder.setPath("1");
        builder.build(null);
        startBuilder.link(builder);

        Builder endBuilder = new JpdlEndNodeBuilder(context);
        endBuilder.build(null);
        builder.link(endBuilder);
        return target;
    }

    private On addEventListener(String eventName) {
         // add event listener
        On on = new On();
        on.setEvent(eventName);
        Timer.EventListener listener = new Timer.EventListener();
        // note that we use the expr property rather than class in order to use a Spring bean
        listener.setExpr(EVENT_LISTENER_EXPR);
        listener.setPropagation(BooleanValueType.TRUE);
        WireObjectType.Field field = new WireObjectType.Field();
        field.setName("eventName");
        Variable.String variable = new Variable.String();
        variable.setValue(eventName);
        field.setString(variable);
        listener.getDescriptionOrFactoryOrConstructor().add(field);
        on.getEventListenerGroup().add(listener);
        return on;
    }    
    
}
