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
package org.wiredwidgets.cow.server.service;

import java.util.Map;
import java.util.Set;
import org.jbpm.api.ExecutionService;
import org.jbpm.api.HistoryService;
import org.jbpm.api.RepositoryService;
import org.jbpm.api.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

/**
 *
 * @author JKRANES
 */
public abstract class AbstractCowServiceImpl {
    
    // include the JBPM services
    @Autowired
    protected ExecutionService executionService;
    @Autowired
    protected RepositoryService repositoryService;
    @Autowired
    protected HistoryService historyService;
    @Autowired
    protected TaskService taskService;
    
    @Autowired
    protected ConversionService converter; 
    @Autowired
    protected Jaxb2Marshaller marshaller;
    
    /*
     * Sets variables.  If a variable is new to this execution, make it permanent.
     */
    protected void setVariables(String executionId, Map<String, String> variables) {
        Set<String> varNames = executionService.getVariableNames(executionId);
        for (String key : variables.keySet()) {
            if (!varNames.contains(key)) {
                executionService.createVariable(executionId, key, variables.get(key), true);
            }
            else {
                executionService.setVariable(executionId, key, variables.get(key));
            }
        }
    }
    
}
