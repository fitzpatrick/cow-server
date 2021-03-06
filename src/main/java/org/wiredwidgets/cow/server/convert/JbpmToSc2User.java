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

import java.util.List;
import org.jbpm.api.ProcessEngine;
import org.wiredwidgets.cow.server.api.service.Membership;
import org.wiredwidgets.cow.server.api.service.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;

/**
 *
 * @author JKRANES
 */
public class JbpmToSc2User implements Converter<org.jbpm.api.identity.User, User> {

    @Autowired
    ProcessEngine engine;

    @Override
    public User convert(org.jbpm.api.identity.User source) {
        User target = new User();
        target.setId(source.getId());
        target.setFirstName(source.getGivenName());
        target.setLastName(source.getFamilyName());
        target.setEmail(source.getBusinessEmail());
        List<String> jbpmGroups = this.engine.getIdentityService().findGroupIdsByUser(source.getId());
        for (String groupId : jbpmGroups) {
            Membership mem = new Membership();
            mem.setGroup(groupId);
            target.getMemberships().add(mem);
        }
        return target;
    }
}
