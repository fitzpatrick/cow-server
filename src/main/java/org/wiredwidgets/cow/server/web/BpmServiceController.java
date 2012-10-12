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

package org.wiredwidgets.cow.server.web;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import org.springframework.beans.factory.annotation.Autowired;

import org.wiredwidgets.cow.server.service.ProcessService;
import org.wiredwidgets.cow.server.service.TaskService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 *
 * @author JKRANES
 */
@Controller
public class BpmServiceController {

    @Autowired
    ProcessService processService;

    @Autowired
    TaskService taskService;

    static Logger log = Logger.getLogger(BpmServiceController.class);

    /**
     * Used to test the server and make sure it is running
     * @return 
     */
    @RequestMapping("/hello")
    @ResponseBody
    public String hello() {
        return "Hello";
    }

/**
     * Experimental -- WADL is not necessarily kept up to date
     * @return 
     */    
    @RequestMapping("/wadl")
    @ResponseBody
    public String getWadl() {
        Resource resource = new ClassPathResource("bpm-server.wadl");
        try {
            return IOUtils.toString(resource.getInputStream());
        }
        catch (Exception e) {
            return "i/o exception";
        }
    }

}
