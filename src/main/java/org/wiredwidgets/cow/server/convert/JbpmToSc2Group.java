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

import org.wiredwidgets.cow.server.api.service.Group;
import org.springframework.core.convert.converter.Converter;

/**
 *
 * @author JKRANES
 */
public class JbpmToSc2Group implements Converter<org.jbpm.api.identity.Group, Group> {

    @Override
    public Group convert(org.jbpm.api.identity.Group source) {
        Group target = new Group();
        target.setId(source.getId());
        target.setName(source.getName());
        target.setType(source.getType());
        return target;
    }
}
