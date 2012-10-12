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

import java.util.Date;
import org.apache.log4j.Logger;
import org.jbpm.api.listener.EventListener;
import org.jbpm.api.listener.EventListenerExecution;
import org.jbpm.api.model.Activity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;

/**
 * Abstract base class for BPM engine event listeners
 * Schedules execution for 1 second in the future, in order to allow
 * the activity to be committed to the database before the listener code runs
 * @author JKRANES
 */
public abstract class BpmEventListener implements EventListener, Runnable {

    private static Logger log = Logger.getLogger(BpmEventListener.class);
    
    @Autowired
    ConcurrentTaskScheduler scheduler;
    
    private static int ONE_SECOND = 1000;
    
    // NOTE: in the subclass run() method, execution.getActivity() will refer to the NEXT activity
    // in the workflow.  To get the current activity at the time of notification, use the activity field instead.
    protected EventListenerExecution execution;
    protected Activity activity;
    
    /**
     * Schedules the listener to run in 1 second, giving time for the DB to be updated
     * @param execution
     * @throws Exception 
     */
    @Override
    public void notify(EventListenerExecution execution) throws Exception {
        // log.info("Activity: " + execution.getActivity().getName());
        this.execution = execution;
        // create a reference to the activity that triggered the notification
        this.activity = execution.getActivity();
        long time = new Date().getTime();
        scheduler.schedule(this, new Date(time + ONE_SECOND));        
    }
       
    /**
     * To be implemented by subclasses.  This is where the listener code goes.
     */
    @Override
    abstract public void run();
    
}
