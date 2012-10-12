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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.wiredwidgets.cow.server.api.service.ProcessDefinition;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author JKRANES
 */
@Transactional
public class ProcessDefinitionsServiceImpl extends AbstractCowServiceImpl implements ProcessDefinitionsService {

    private static TypeDescriptor JBPM_PROCESS_DEFINITION_LIST = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(org.jbpm.api.ProcessDefinition.class));
    private static TypeDescriptor COW_PROCESS_DEFINITION_LIST = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(ProcessDefinition.class));

    @Transactional(readOnly = true)
    @Override
    public List<ProcessDefinition> findAllProcessDefinitions() {
        return this.convertProcessDefinitions(repositoryService.createProcessDefinitionQuery().list());
    }

    @Transactional(readOnly = true)
    @Override
    public List<ProcessDefinition> findProcessDefinitionsByKey(String key) {
        return this.convertProcessDefinitions(repositoryService.createProcessDefinitionQuery().processDefinitionKey(key).list());
    }
    
    @Override
    public boolean deleteProcessDefinitionsByKey(String key) {
        boolean found = false;
        for (org.jbpm.api.ProcessDefinition def : repositoryService.createProcessDefinitionQuery().processDefinitionKey(key).list()) {
            found = true;
            repositoryService.deleteDeployment(def.getDeploymentId());
        }
        return found;
    }    

    @Transactional(readOnly = true)
    @Override
    public ProcessDefinition findLatestVersionProcessDefinitionByKey(String key) {
        List<org.jbpm.api.ProcessDefinition> defs = this.filterLatestVersions(repositoryService.createProcessDefinitionQuery().processDefinitionKey(key).list());
        return (defs.isEmpty()? null : this.converter.convert(defs.get(0), ProcessDefinition.class) );
    }

    /** Finds the latest version of each process definition
     *
     * @return
     */
    @Transactional(readOnly = true)
    @Override
    public List<ProcessDefinition> findLatestVersionProcessDefinitions() {
        return this.convertProcessDefinitions(this.filterLatestVersions(repositoryService.createProcessDefinitionQuery().list()));
    }



    @Transactional(readOnly = true)
    @Override
    public ProcessDefinition getProcessDefinition(String id) {
        return this.converter.convert(repositoryService.createProcessDefinitionQuery().processDefinitionId(id).uniqueResult(), ProcessDefinition.class);
    }


    private List<ProcessDefinition> convertProcessDefinitions(List<org.jbpm.api.ProcessDefinition> source) {
        return (List<ProcessDefinition>) converter.convert(source, JBPM_PROCESS_DEFINITION_LIST, COW_PROCESS_DEFINITION_LIST);
    }

    private List<org.jbpm.api.ProcessDefinition> filterLatestVersions(List<org.jbpm.api.ProcessDefinition> candidates) {

        // Create a map of ProcessDefinitions using the process key as the map key
        Map<String, org.jbpm.api.ProcessDefinition> latestDefsMap = new HashMap<String, org.jbpm.api.ProcessDefinition>();

        for (org.jbpm.api.ProcessDefinition def : candidates) {
            org.jbpm.api.ProcessDefinition latestDef = latestDefsMap.get(def.getKey());
            if (latestDef == null) {
                latestDefsMap.put(def.getKey(), def);
            } else {
                // if current item's version  higher than the one in the map
                if (def.getVersion() > latestDef.getVersion()) {
                    // replace the map entry
                    latestDefsMap.put(def.getKey(), def);
                }
            }
        }

        List results = new ArrayList<org.jbpm.api.ProcessDefinition>();
        results.addAll(latestDefsMap.values());
        return results;
    }
}
