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
import org.wiredwidgets.cow.server.transform.v2.AbstractProcessContext;

/**
 * JPDL nodes do not have a common ancestor, hence the use of <Object> here
 * @author JKRANES
 */
public class JpdlProcessContext extends AbstractProcessContext<Object, Process> {


    public JpdlProcessContext(org.wiredwidgets.cow.server.api.model.v2.Process source, Process target) {
        super(source, target);
    }

    @Override
    public void addNode(Object node) {
        getTarget().getSwimlaneOrOnOrTimer().add(node);
    }

    
}
