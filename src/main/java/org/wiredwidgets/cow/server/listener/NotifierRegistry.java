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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.wiredwidgets.cow.server.listener;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JKRANES
 */
public class NotifierRegistry {

    private List<Notifier> notifiers = new ArrayList<Notifier>();

    public List<Notifier> getNotifiers() {
        return notifiers;
    }
    public void setNotifiers(List<Notifier> notifiers) {
        this.notifiers = notifiers;
    }

    public void addNotifier(Notifier notifier) {
        notifiers.add(notifier);
    }
   
}
