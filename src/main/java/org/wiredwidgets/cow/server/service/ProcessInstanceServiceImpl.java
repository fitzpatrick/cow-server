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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.jbpm.api.JbpmException;
import org.jbpm.api.history.HistoryProcessInstanceQuery;
import org.jbpm.api.model.OpenProcessInstance;

import org.wiredwidgets.cow.server.api.service.ProcessDefinition;
import org.wiredwidgets.cow.server.api.model.v2.Process;
import org.wiredwidgets.cow.server.api.service.ProcessInstance;
import org.wiredwidgets.cow.server.api.service.Variable;
import org.wiredwidgets.cow.server.completion.History;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.transaction.annotation.Transactional;
import org.wiredwidgets.cow.server.completion.EvaluatorFactory;

/**
 *
 * @author JKRANES
 */
@Transactional
public class ProcessInstanceServiceImpl extends AbstractCowServiceImpl implements ProcessInstanceService {

    @Autowired
    ProcessService processService;
    @Autowired
    TaskService cowTaskService;
    @Autowired
    ProcessDefinitionsService processDefinitionsService;
    @Autowired
    EvaluatorFactory evaluatorFactory;
    
    public static Logger log = Logger.getLogger(ProcessInstanceServiceImpl.class);
    private static TypeDescriptor JBPM_PROCESS_INSTANCE_LIST = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(org.jbpm.api.ProcessInstance.class));
    private static TypeDescriptor JBPM_HISTORY_PROCESS_INSTANCE_LIST = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(org.jbpm.api.history.HistoryProcessInstance.class));
    private static TypeDescriptor COW_PROCESS_INSTANCE_LIST = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(ProcessInstance.class));

    @Override
    public String executeProcess(ProcessInstance instance) {
        Map<String, String> vars = new HashMap<String, String>();
        if (instance.getVariables() != null) {
            for (Variable variable : instance.getVariables().getVariables()) {
                vars.put(variable.getName(), variable.getValue());
            }
        }
        // COW-65 save history for all variables
        // org.jbpm.api.ProcessInstance pi = executionService.startProcessInstanceByKey(instance.getProcessDefinitionKey(), vars);
        org.jbpm.api.ProcessInstance pi = executionService.startProcessInstanceByKey(instance.getProcessDefinitionKey());
        instance.setId(pi.getId());
        
        //create the process name as a history-tracked variable
        if (instance.getName() != null) {
            // executionService.createVariable(pi.getId(), "_name", instance.getName(), true);
            vars.put("_name", instance.getName());
        }        

        setVariables(pi.getId(), vars);  // COW-65
        
        // add the instance id as a variable so it can be passed to a subprocess
        // executionService.createVariable(pi.getId(), "_id", pi.getId(), false);
        
        if (instance.getPriority() != null) {
            updateProcessInstancePriority(instance.getPriority().intValue(), pi);
        }

        return pi.getId();
    }

    @Override
    public boolean updateProcessInstance(ProcessInstance source) {
        org.jbpm.api.ProcessInstance target = this.executionService.findProcessInstanceById(source.getId());
        if (target == null) {
            return false;
        }
        updateProcessInstanceVariables(source);
        if (source.getPriority() != null) {
            updateProcessInstancePriority(source.getPriority().intValue(), target);
        }
        return true;
    }

    private void updateProcessInstanceVariables(ProcessInstance source) {
        if (source.getVariables() != null) {
            for (Variable var : source.getVariables().getVariables()) {
                executionService.setVariable(source.getId(), var.getName(), var.getValue());
            }
        }
    }

    private void updateProcessInstancePriority(int priority, org.jbpm.api.Execution target) {
        if (target instanceof OpenProcessInstance) {
            ((OpenProcessInstance) target).setPriority(priority);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public ProcessInstance getProcessInstance(String processInstanceId) {
        org.jbpm.api.ProcessInstance pi = this.executionService.findProcessInstanceById(processInstanceId);
        return (pi == null) ? null : this.converter.convert(pi, ProcessInstance.class);
    }

    @Transactional(readOnly = true)
    @Override
    public Process getProcessInstanceStatus(String processInstanceId) {
        org.jbpm.api.history.HistoryProcessInstance pi = this.historyService.createHistoryProcessInstanceQuery().processInstanceId(processInstanceId).uniqueResult();
        String processDefinitionId = pi.getProcessDefinitionId();
        ProcessDefinition pd = processDefinitionsService.getProcessDefinition(processDefinitionId);
        Process process = getV2Process(processService.getResourceAsStreamByDeploymentId(pd.getDeploymentId(), ProcessService.V2_EXTENSION));
        evaluatorFactory.getProcessEvaluator(processInstanceId, process, new History(cowTaskService.getHistoryActivities(processInstanceId), pi.getState())).evaluate();
        return process;
    }

    private Process getV2Process(InputStream stream) {
        return (Process) marshaller.unmarshal(new StreamSource(stream));
    }

    @Transactional(readOnly = true)
    @Override
    public Process getV2Process(String processInstanceId) {
        org.jbpm.api.history.HistoryProcessInstance pi = this.historyService.createHistoryProcessInstanceQuery().processInstanceId(processInstanceId).uniqueResult();
        String processDefinitionId = pi.getProcessDefinitionId();
        ProcessDefinition pd = processDefinitionsService.getProcessDefinition(processDefinitionId);
        InputStream in = processService.getResourceAsStreamByDeploymentId(pd.getDeploymentId(), ProcessService.V2_EXTENSION);
        if (in != null) {
            return getV2Process(in);
        } else {
            // this will occur if the process is not a cow / v2 process.  I.e. if it is a JPDL loaded directly using the server api.
            return null;
        }
    }

    @Override
    public boolean deleteProcessInstance(String id) {
        return deleteProcessInstanceInternal(id);
    }

    @Override
    public void deleteProcessInstancesByKey(String key) {
        List<org.jbpm.api.ProcessInstance> instances = findJbpmProcessInstancesByKey(key);
        for (org.jbpm.api.ProcessInstance instance : instances) {
            deleteProcessInstanceInternal(instance.getId());
        }
    }

    private boolean deleteProcessInstanceInternal(String id) {
        try {
            executionService.deleteProcessInstanceCascade(id);
        } catch (JbpmException e) {
            // not found
            return false;
        }
        return true;
    }

    @Transactional(readOnly = true)
    @Override
    public List<ProcessInstance> findAllProcessInstances() {
        return this.convertProcessInstances(executionService.createProcessInstanceQuery().list());
    }

    @Transactional(readOnly = true)
    @Override
    public List<ProcessInstance> findAllHistoryProcessInstances() {
        return this.convertHistoryProcessInstances(historyService.createHistoryProcessInstanceQuery().list());
    }

    @Transactional(readOnly = true)
    @Override
    public List<ProcessInstance> findProcessInstancesByKey(String key) {
        return this.convertProcessInstances(findJbpmProcessInstancesByKey(key));
    }

    @Transactional(readOnly = true)
    @Override
    public List<ProcessInstance> findHistoryProcessInstances(String key, Date endedAfter, boolean ended) {
        return this.convertHistoryProcessInstances(findJbpmHistoryProcessInstances(key, endedAfter, ended));
    }

    private List<org.jbpm.api.ProcessInstance> findJbpmProcessInstancesByKey(String key) {
        List<org.jbpm.api.ProcessInstance> instances = new ArrayList<org.jbpm.api.ProcessInstance>();
        // get all versions (it's possible some older version has active process instances)
        List<org.jbpm.api.ProcessDefinition> defs = repositoryService.createProcessDefinitionQuery().processDefinitionKey(key).list();

        for (org.jbpm.api.ProcessDefinition def : defs) {
            instances.addAll(executionService.createProcessInstanceQuery().processDefinitionId(def.getId()).list());
        }
        return instances;
    }

    private List<org.jbpm.api.history.HistoryProcessInstance> findJbpmHistoryProcessInstances(String key, Date endedAfter, boolean ended) {
        List<org.jbpm.api.history.HistoryProcessInstance> instances = new ArrayList<org.jbpm.api.history.HistoryProcessInstance>();

        if (key != null) {
            // get all versions (it's possible some older version has active process instances)
            List<org.jbpm.api.ProcessDefinition> defs = repositoryService.createProcessDefinitionQuery().processDefinitionKey(key).list();


            for (org.jbpm.api.ProcessDefinition def : defs) {
                HistoryProcessInstanceQuery query = historyService.createHistoryProcessInstanceQuery().processDefinitionId(def.getId());

                if (ended) {
                    query = query.ended();
                }
                if (endedAfter != null) {
                    query = query.endedAfter(endedAfter);
                }
                instances.addAll(query.list());
            }
        } else {
            HistoryProcessInstanceQuery query = historyService.createHistoryProcessInstanceQuery();
            if (ended) {
                query = query.ended();
            }
            if (endedAfter != null) {
                query = query.endedAfter(endedAfter);
            }
            instances.addAll(query.list());
        }
        return instances;
    }

    private List<ProcessInstance> convertProcessInstances(List<org.jbpm.api.ProcessInstance> source) {
        return (List<ProcessInstance>) converter.convert(source, JBPM_PROCESS_INSTANCE_LIST, COW_PROCESS_INSTANCE_LIST);
    }

    private List<ProcessInstance> convertHistoryProcessInstances(List<org.jbpm.api.history.HistoryProcessInstance> source) {
        return (List<ProcessInstance>) converter.convert(source, JBPM_HISTORY_PROCESS_INSTANCE_LIST, COW_PROCESS_INSTANCE_LIST);
    }
}
