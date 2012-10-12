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

import org.wiredwidgets.cow.server.api.model.v2.Activity;
import org.wiredwidgets.cow.server.transform.v2.AbstractProcessBuilder;
import org.wiredwidgets.cow.server.transform.v2.Builder;
import org.omg.spec.bpmn._20100524.model.Definitions;
import org.omg.spec.bpmn._20100524.model.TProcess;
import org.omg.spec.bpmn._20100524.model.ObjectFactory;


/**
 *
 * @author JKRANES
 */
public class Bpmn20ProcessBuilder extends AbstractProcessBuilder<Definitions> {

    @Override
    public Definitions build(org.wiredwidgets.cow.server.api.model.v2.Process source) {

        ObjectFactory factory = new ObjectFactory();
        Definitions definitions = new Definitions();
        definitions.setName(source.getName());
        TProcess process = new TProcess();
        process.setId("0");
        process.setName("test");
        definitions.getRootElements().add(factory.createProcess(process));

        Bpmn20ProcessContext context = new Bpmn20ProcessContext(source, definitions, process);

        Builder startBuilder = new Bpmn20StartNodeBuilder(context);
        startBuilder.build(null);

        Activity activity = source.getActivity().getValue();
        Builder builder = createActivityBuilder(context, activity);

        builder.build(null);
        startBuilder.link(builder);

        Builder endBuilder = new Bpmn20EndNodeBuilder(context);
        endBuilder.build(null);

        builder.link(endBuilder);

        return definitions;
    }

}
