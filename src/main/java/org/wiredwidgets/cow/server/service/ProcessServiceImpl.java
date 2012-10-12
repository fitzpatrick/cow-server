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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;

import org.jbpm._4_4.jpdl.TransitionType;
import org.jbpm._4_4.jpdl.Process;
import org.jbpm.api.NewDeployment;
// import org.wiredwidgets.cow.server.api.model.Activity;
import org.wiredwidgets.cow.server.transform.v2.jpdl.JpdlProcessBuilder;

import org.wiredwidgets.cow.server.api.service.Deployment;
import org.wiredwidgets.cow.server.api.service.ProcessDefinition;
import org.wiredwidgets.cow.server.api.service.ResourceNames;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;

/**
 *
 * @author JKRANES
 */
@Transactional
public class ProcessServiceImpl extends AbstractCowServiceImpl implements ProcessService {

    private static Logger log = Logger.getLogger(ProcessServiceImpl.class);
    @Autowired
    ProcessDefinitionsService processDefService;
    @Autowired
    JpdlProcessBuilder jpdlProcessBuilder;

//    @PostConstruct
//    public void init() {
//        this.repService = engine.getRepositoryService();
//    }
    @Override
    public Deployment createDeployment(StreamSource source, String name) {
        return this.createDeployment(source, name, false);
    }

    @Override
    public Deployment createDeployment(StreamSource source, String name, boolean bpmn2) {
        NewDeployment nd = repositoryService.createDeployment();
        nd.setName(name);
        // Resources ending in .jpdl.xml are automatically recognized
        // as JPDL process definitions
        String extension = (bpmn2 ? BPMN2_EXTENSION : JPDL_EXTENSION);
        String resourceName = name + extension;
        nd.addResourceFromInputStream(resourceName, source.getInputStream());
        String id = nd.deploy();
        return this.getDeployment(id);
    }

    private Deployment createV2Deployment(StreamSource source, StreamSource v2Source, String name) {

        // Resources ending in .jpdl.xml are automatically recognized
        // as JPDL process definitions
        Map<String, InputStream> resources = new HashMap<String, InputStream>();
        resources.put(JPDL_EXTENSION, source.getInputStream());
        resources.put(V2_EXTENSION, v2Source.getInputStream());
        return createV2Deployment(resources, name);
    }

    private Deployment createV2Deployment(Map<String, InputStream> resources, String name) {
        NewDeployment nd = repositoryService.createDeployment();
        nd.setName(name);
        for (String key : resources.keySet()) {
            nd.addResourceFromInputStream(name + key, resources.get(key));
        }
        String id = nd.deploy();
        return this.getDeployment(id);
    }

    @Transactional(readOnly = true)
    @Override
    public Deployment getDeployment(String id) {
        return this.convert(getJbpmDeployment(id));
    }

    private org.jbpm.api.Deployment getJbpmDeployment(String id) {
        return repositoryService.createDeploymentQuery().deploymentId(id).uniqueResult();
    }

    private Deployment convert(org.jbpm.api.Deployment dep) {
        if (dep == null) {
            return null;
        } else {
            Deployment sc2Deployment = new Deployment();
            this.copyProperties(sc2Deployment, dep);
            return sc2Deployment;
        }
    }

    private List<Deployment> convertDeployments(List<org.jbpm.api.Deployment> deps) {
        List<Deployment> sc2Deps = new ArrayList<Deployment>();
        for (org.jbpm.api.Deployment dep : deps) {
            sc2Deps.add(this.convert(dep));
        }
        return sc2Deps;
    }

    /*
     * Wrapper for PropertyUtils to handle declared exceptions
     */
    private void copyProperties(Object dest, Object src) {

        try {
            PropertyUtils.copyProperties(dest, src);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteDeployment(String id) {
        repositoryService.deleteDeployment(id);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Deployment> findAllDeployments() {
        return this.convertDeployments(repositoryService.createDeploymentQuery().list());
    }

    @Transactional(readOnly = true)
    @Override
    public ResourceNames getResourceNames(String deploymentId) {
        Set<String> names = repositoryService.getResourceNames(deploymentId);
        ResourceNames rn = new ResourceNames();
        for (String name : names) {
            rn.getNames().add(name);
        }
        return rn;
    }

    @Transactional(readOnly = true)
    @Override
    public StreamSource getResource(String id, String name) {
        InputStream in = repositoryService.getResourceAsStream(id, name + JPDL_EXTENSION);
        return new StreamSource(in);
    }

    @Override
    public Deployment saveV2Process(org.wiredwidgets.cow.server.api.model.v2.Process v2Process, String deploymentName) {
        Map<String, InputStream> resources = new HashMap<String, InputStream>();
        Process p = jpdlProcessBuilder.build(v2Process);
        p.setKey(v2Process.getKey());
        resources.put(JPDL_EXTENSION, marshalToInputStream(p));
        resources.put(V2_EXTENSION, marshalToInputStream(v2Process));
        return createV2Deployment(resources, deploymentName);
    }

    /*
     * Note that for JPDL output, this method will produce XML using
     * the default namespace (i.e. without prefixes) in order 
     * to work around bug https://issues.jboss.org/browse/JBPM-3392
     */
    private InputStream marshalToInputStream(Object source) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        marshaller.marshal(source, new StreamResult(out));
        return new ByteArrayInputStream(out.toByteArray());

    }

    /*
     * Returns null if no deployment exists for the key (i.e. for first deployment of a process)
     */
    private org.jbpm.api.Deployment getLatestDeployment(String key) {
        ProcessDefinition latestVersion = this.processDefService.findLatestVersionProcessDefinitionByKey(key);
        if (latestVersion != null) {
            return repositoryService.createDeploymentQuery().deploymentId(latestVersion.getDeploymentId()).uniqueResult();
        } else {
            return null;
        }
    }

    @Transactional(readOnly = true)
    @Override
    public InputStream getNativeProcessAsStream(String key) {
        org.jbpm.api.Deployment deployment = this.getLatestDeployment(key);
        return this.getNativeProcessAsStream(deployment);
    }

    @Transactional(readOnly = true)
    @Override
    public InputStream getResourceAsStream(String key, String extension) {
        return getResourceAsStream(this.getLatestDeployment(key), extension);
    }

    @Transactional(readOnly = true)
    @Override
    public org.wiredwidgets.cow.server.api.model.v2.Process getV2Process(String key) {
        return (org.wiredwidgets.cow.server.api.model.v2.Process) marshaller.unmarshal(new StreamSource(getResourceAsStream(key, V2_EXTENSION)));
    }

    @Transactional(readOnly = true)
    @Override
    public InputStream getResourceAsStreamByDeploymentId(String id, String extension) {
        return getResourceAsStream(getJbpmDeployment(id), extension);
    }

    /*
     * returns null if the resource is not found
     */
    private InputStream getResourceAsStream(org.jbpm.api.Deployment deployment, String extension) {
        String resourceName = this.getProcessResourceName(deployment, extension);
        if (resourceName == null) {
            return null;
        } else {
            InputStream is = repositoryService.getResourceAsStream(deployment.getId(), resourceName);

            //To handle XML saved under the previous namespace, we need to do a string search and replace
            //TODO: remove this code once all older namespace processes have been updated, as this extra step slows down the response
            String processString = convertToString(is);
            String modifiedString = processString.replaceAll("http://simplec2\\.mitre\\.org/bpm", "http://www\\.wiredwidgets\\.org/cow/server");
            // return repService.getResourceAsStream(deployment.getId(), this.getProcessResourceName(deployment, extension));
            return new ByteArrayInputStream(modifiedString.getBytes());
        }     
    }

    /*
     * Default to JPDL
     */
    private String getProcessResourceName(org.jbpm.api.Deployment deployment) {
        return getProcessResourceName(deployment, JPDL_EXTENSION);
    }

    private String getProcessResourceName(org.jbpm.api.Deployment deployment, String extension) {
        Set<String> names = repositoryService.getResourceNames(deployment.getId());
        String resourceName = deployment.getName() + extension;
        if (names.contains(resourceName)) {
            return resourceName;
        } else {
            log.error("Resource not found: " + resourceName + " in deployment: " + deployment.getId());
            log.error("Resources found: ");
            for (String name : names) {
                log.error("Resource: " + name);
            }
            return null;
        }
    }

    private InputStream getNativeProcessAsStream(org.jbpm.api.Deployment deployment) {
        return repositoryService.getResourceAsStream(deployment.getId(), this.getProcessResourceName(deployment));
    }

    /*
     * Create a new empty JPDL process with start and end nodes
     */
    private Process newProcessInstance(String name, String key) {
        Process p = new Process();
        p.setName(name);
        p.setKey(key);
        Process.Start start = new Process.Start();
        start.setName("start");
        TransitionType startTrans = new TransitionType();
        startTrans.setName("trans1");
        startTrans.setTo("end");
        start.getOnOrTransition().add(startTrans);
        Process.End end = new Process.End();
        end.setName("end");
        p.getSwimlaneOrOnOrTimer().add(start);
        p.getSwimlaneOrOnOrTimer().add(end);
        return p;
    }

    private Process convertV2Process(org.wiredwidgets.cow.server.api.model.v2.Process source) {
        Process target = this.newProcessInstance(source.getName(), source.getKey());
        org.wiredwidgets.cow.server.api.model.v2.Activity activity = source.getActivity().getValue();
        return null;
    }

    private String convertToString(InputStream in) {
        try {
            return FileCopyUtils.copyToString(new InputStreamReader(in));
        } catch (IOException e) {
            log.error(e);
            throw new RuntimeException(e);
        }
    }
}
