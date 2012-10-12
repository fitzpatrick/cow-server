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
 * NOTICE
 * 
 * This software was produced for the U. S. Government
 * under Contract No. W15P7T-10-C-F600, and is
 * subject to the Rights in Noncommercial Computer Software
 * and Noncommercial Computer Software Documentation
 * Clause 252.227-7014 (JUN 1995)
 * 
 * (c) The MITRE Corporation. All Rights Reserved.
 */
package org.wiredwidgets.cow.server.service;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import org.wiredwidgets.cow.server.api.model.v2.Loop;
import org.wiredwidgets.cow.server.api.model.v2.Task;
import org.wiredwidgets.cow.server.api.model.v2.Activity;
import org.wiredwidgets.cow.server.api.model.v2.Process;


/**
 *
 * @author JKRANES
 */
public class TestConverters {

    public TestConverters() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void TestLoopUnmarshal() throws Exception {
        Unmarshaller unmarshaller = JAXBContext.newInstance("org.wiredwidgets.cow.server.api.model.v2").createUnmarshaller();
        Process process = (Process)unmarshaller.unmarshal(this.getClass().getClassLoader().getResource("org/wiredwidgets/cow/server/service/Loop.xml"));
        Loop loop = (Loop) process.getActivity().getValue();
        Task task = loop.getLoopTask();
        Activity activity = loop.getActivity().getValue();
        assertEquals("task", activity.getName());
        assertEquals("loopTask", task.getName());
        assertNotNull(task);
    }
}
