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

import org.wiredwidgets.cow.server.api.service.ProcessDefinition;
import org.springframework.core.convert.converter.Converter;

/**
 *
 * @author JKRANES
 */
public class JbpmToSc2ProcessDefinition implements Converter<org.jbpm.api.ProcessDefinition, ProcessDefinition> {

    @Override
    public ProcessDefinition convert(org.jbpm.api.ProcessDefinition source) {
        if (source == null) {
            return null;
        }
        ProcessDefinition target = new ProcessDefinition();
        target.setDeploymentId(source.getDeploymentId());
        target.setDescription(source.getDescription());
        target.setId(source.getId());
        target.setKey(source.getKey());
        target.setName(source.getName());
        target.setVersion(source.getVersion());
        return target;
    }
}
