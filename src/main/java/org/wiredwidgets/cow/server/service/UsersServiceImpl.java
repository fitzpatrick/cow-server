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
import java.util.List;
import org.jbpm.api.IdentityService;
import org.jbpm.api.identity.User;
import org.jbpm.api.identity.Group;
import org.wiredwidgets.cow.server.api.service.Membership;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author JKRANES
 */
@Transactional
public class UsersServiceImpl extends AbstractCowServiceImpl implements UsersService {
    
    private static TypeDescriptor JBPM_USER_LIST = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(User.class));
    private static TypeDescriptor JBPM_GROUP_LIST = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(Group.class));
    private static TypeDescriptor COW_USER_LIST = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(org.wiredwidgets.cow.server.api.service.User.class));
    private static TypeDescriptor COW_GROUP_LIST = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(org.wiredwidgets.cow.server.api.service.Group.class));

    private static final String ROOT_USER = "root";
    @Autowired
    IdentityService identityService;

//    @PostConstruct
//    public void init() {
//        this.identityService = engine.getIdentityService();
//    }
    @Transactional(readOnly = true)
    @Override
    public List<org.wiredwidgets.cow.server.api.service.User> findAllUsers() {
        return this.convertUsers(this.identityService.findUsers());
    }

    @Transactional(readOnly = true)
    @Override
    public org.wiredwidgets.cow.server.api.service.User findUser(String id) {
        org.jbpm.api.identity.User user = this.identityService.findUserById(id);
        return (user == null) ? null : this.convert(user);
    }

    @Transactional(readOnly = true)
    @Override
    public List<org.wiredwidgets.cow.server.api.service.Group> findAllGroups() {
        
        // NOTE: jpdl API lacks a findGroups() method to find all groups
        // The hack workaround is to ensure the 'root' user is a member of all groups
        // See createGroup method below
        
        List<Group> groups = new ArrayList<Group>();

        for (String groupId : identityService.findGroupIdsByUser(ROOT_USER)) {
            groups.add(identityService.findGroupById(groupId));
        }
        return this.convertGroups(groups);
    }

    @Override
    public String createGroup(String groupName) {

        Group group = this.identityService.findGroupById(groupName);
        
        if (group == null) {
            this.identityService.createGroup(groupName);
            
            // this is a hack to work around the absence of any findAllGroups method in JBPM
            // We have to maintain a special 'root' user who has memberships in all the groups   
            // in order to be able to implement our findAllGroups

            Membership mem = new Membership();
            mem.setGroup(groupName);

            org.wiredwidgets.cow.server.api.service.User root = findUser(ROOT_USER);

            if (root == null) {
                // create the ROOT user
                root = new org.wiredwidgets.cow.server.api.service.User();
                root.setId(ROOT_USER);
                root.setFirstName(ROOT_USER);
                root.setLastName(ROOT_USER);
                root.getMemberships().add(mem);
            } else {
                if (!root.getMemberships().contains(mem)) {
                    root.getMemberships().add(mem);
                }
            }

            createOrUpdateUser(root);
        }

        return groupName;
    }

    @Transactional(readOnly = true)
    @Override
    public org.wiredwidgets.cow.server.api.service.Group findGroup(String id) {
        Group group = this.identityService.findGroupById(id);
        return (group == null) ? null : this.convert(group);
    }

    @Override
    public void createOrUpdateUser(org.wiredwidgets.cow.server.api.service.User user) {
        if (this.identityService.findUserById(user.getId()) != null) {
            this.identityService.deleteUser(user.getId());
        }
        if (user.getEmail() == null) {
            this.identityService.createUser(user.getId(), user.getFirstName(), user.getLastName());
        } else {
            this.identityService.createUser(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail());
        }
        for (Membership mem : user.getMemberships()) {
            this.identityService.createMembership(user.getId(), mem.getGroup());
        }
    }

    @Override
    public boolean deleteUser(String id) {
        // NOTE: JPDL javadoc indicates that no exception is thrown when deleting
        // a non-existent user, but in 4.3 this is not true.
        
        // Prevent deleting ROOT user
        
        if (!id.equals(ROOT_USER) && identityService.findUserById(id) != null) {
            this.identityService.deleteUser(id);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean deleteGroup(String id) {
        // NOTE: JPDL javadoc indicates that no exception is thrown when deleting
        // a non-existent group, but in 4.3 this is not true.
        if (this.identityService.findGroupById(id) != null) {
            this.identityService.deleteGroup(id);
            return true;
        } else {
            return false;
        }
    }

    private org.wiredwidgets.cow.server.api.service.User convert(User source) {
        return this.converter.convert(source, org.wiredwidgets.cow.server.api.service.User.class);
    }

    private List<org.wiredwidgets.cow.server.api.service.User> convertUsers(List<User> source) {
        return (List<org.wiredwidgets.cow.server.api.service.User>) converter.convert(source, JBPM_USER_LIST, COW_USER_LIST);
    }

    private org.wiredwidgets.cow.server.api.service.Group convert(Group source) {
        return this.converter.convert(source, org.wiredwidgets.cow.server.api.service.Group.class);
    }

    private List<org.wiredwidgets.cow.server.api.service.Group> convertGroups(List<Group> source) {
        return (List<org.wiredwidgets.cow.server.api.service.Group>) converter.convert(source, JBPM_GROUP_LIST, COW_GROUP_LIST);
    }
}
