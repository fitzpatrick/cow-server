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

package org.wiredwidgets.cow.server.convert;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.xml.datatype.XMLGregorianCalendar;
import org.jbpm.api.ProcessEngine;
import org.wiredwidgets.cow.server.api.service.Task;
import org.wiredwidgets.cow.server.api.service.Variable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;

/**
 *
 * @author JKRANES
 */
public class Sc2ToJbpmTask implements Converter<Task, org.jbpm.api.task.Task> {

    @Autowired
    ProcessEngine engine;

    @Override
    public org.jbpm.api.task.Task convert(Task source) {
        org.jbpm.api.task.Task target = this.engine.getTaskService().newTask();
        target.setAssignee(source.getAssignee());
        target.setDescription(source.getDescription());
        if (source.getDueDate() != null) {
            target.setDuedate(this.convert(source.getDueDate()));
        }
        target.setName(source.getName());
        // note that activity name cannot be set explicitly
        // target.setActivityName(source.getActivityName());
        target.setPriority(source.getPriority());
        if (source.getProgress() != null) {
            target.setProgress(source.getProgress());
        }

        // convert variables
        if (source.getVariables() != null && source.getVariables().getVariables().size() > 0) {
            Map<String, Object> variables = new HashMap<String, Object>();
            for (Variable variable : source.getVariables().getVariables()) {
                variables.put(variable.getName(), variable.getValue());
            }
            this.engine.getTaskService().setVariables(null, variables);
        }


        return target;
    }

    private Date convert(XMLGregorianCalendar source) {
        return source.toGregorianCalendar().getTime();
    }

}
