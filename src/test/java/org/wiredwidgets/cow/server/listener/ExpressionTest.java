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

import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.*;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 *
 * @author JKRANES
 */
public class ExpressionTest {
    
    public ExpressionTest() {
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
    public void testExpressions() {
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("test", "xxx");
        ExpressionParser parser = new SpelExpressionParser();
        String result = parser.parseExpression("the value is: #{#test}", new TemplateParserContext()).getValue(context, String.class);
        assertEquals("the value is: xxx", result);
              
    }
    
    @Test
    public void testExpressionsWithMap() {
        StandardEvaluationContext context = new StandardEvaluationContext();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("test1", "xxx");
        map.put("test2", "yyy");
        context.setVariables(map);
        ExpressionParser parser = new SpelExpressionParser();
        String result = parser.parseExpression("the value is: #{#test1}#{#test2}", new TemplateParserContext()).getValue(context, String.class);
        assertEquals("the value is: xxxyyy", result);            
    }    
    
    @Test
    public void testRegEx() {
        String expression = "This is a ${test}.";
        String result = expression.replaceAll("\\$\\{(.*)\\}", "#\\{#$1\\}");
        assertEquals("This is a #{#test}.", result);
    }
}
