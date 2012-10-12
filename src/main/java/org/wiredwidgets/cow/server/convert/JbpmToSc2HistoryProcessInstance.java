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

import java.util.Map;
import javax.xml.datatype.XMLGregorianCalendar;
import org.apache.log4j.Logger;
import org.jbpm.api.HistoryService;
import org.jbpm.api.ProcessEngine;
import org.wiredwidgets.cow.server.api.service.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.wiredwidgets.cow.server.api.service.Variable;
import org.wiredwidgets.cow.server.api.service.Variables;

/**
 *
 * @author JKRANES
 */
public class JbpmToSc2HistoryProcessInstance extends AbstractConverter implements Converter<org.jbpm.api.history.HistoryProcessInstance, ProcessInstance> {

    @Autowired
    ProcessEngine engine;
    
    @Autowired
    HistoryService historyService;

    private static Logger log = Logger.getLogger(JbpmToSc2HistoryProcessInstance.class);

    @Override
    public ProcessInstance convert(org.jbpm.api.history.HistoryProcessInstance source) {

        ProcessInstance target = new ProcessInstance();
        target.setId(source.getProcessInstanceId());
        target.setName((String)historyService.getVariable(target.getId(), "_name"));
        target.setDuration(source.getDuration());
        target.setStartTime(getConverter().convert(source.getStartTime(), XMLGregorianCalendar.class));
        target.setEndTime(getConverter().convert(source.getEndTime(),XMLGregorianCalendar.class));
        target.setProcessDefinitionId(source.getProcessDefinitionId());
        target.setState(source.getState());
        
        // variables
        Map<String, ?> vars = historyService.getVariables(target.getId(), historyService.getVariableNames(target.getId()));
        if (!vars.isEmpty()) {        
            for (String key : vars.keySet()) {
                if (!key.startsWith("_")) { // exclude any special 'system' variables
                    Variable v = new Variable();
                    v.setName(key);
                    v.setValue((String)vars.get(key));
                    if (target.getVariables() == null) {
                        target.setVariables(new Variables());
                    }
                    target.getVariables().getVariables().add(v);
                }
            }
        }
       
        return target;
    }
}
