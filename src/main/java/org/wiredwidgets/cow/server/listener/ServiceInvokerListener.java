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

import org.apache.log4j.Logger;
import org.jbpm.api.ExecutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author JKRANES
 */
public class ServiceInvokerListener extends BpmEventListener {

    static Logger log = Logger.getLogger(ServiceInvokerListener.class);
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    ExecutionService executionService;
    private String url = null;
    private String method = null;
    private String content = null;
    private String var = null;
    
    public ServiceInvokerListener() {
        log.info("constructor");
    }

    @Override
    public void run() {

        String result = null;
        if (method.equalsIgnoreCase(HttpMethod.GET.name())) {
            result = restTemplate.getForObject(evaluateExpression(url), String.class);        
        } else if (method.equalsIgnoreCase(HttpMethod.POST.name())) {            
            try {
                // this method expects XML content in the response.  if none if found an exception is thrown
                result = restTemplate.postForObject(evaluateExpression(url), evaluateExpression(content), String.class);
            }
            catch (RestClientException e) {
                // in this case, just log the error and move on.  The result will be null.
                log.error(e);
            }
        }

        // update the result variable, if specified
        if (var != null && result != null) {
            // log.info("execution id: " + execution.getId());
            // log.info("result: " + result);
            // execution.getProcessInstance().setVariable(var, result);
            executionService.setVariable(execution.getId(), var, result);
            log.info(var + ": " + executionService.getVariable(execution.getId(), var));
        }

        // signal the execution to exit this state
        executionService.signalExecutionById(execution.getId());

    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setUrl(String url) {
        this.url = url;
    }
    
    public void setVar(String var) {
        this.var = var;
    }    

    private String evaluateExpression(String expression) {

        // change ${xxx} to #{#xxx} 
        expression = expression.replaceAll("\\$\\{(.*)\\}", "#\\{#$1\\}");

        StandardEvaluationContext context = new StandardEvaluationContext();
        ExpressionParser parser = new SpelExpressionParser();
        context.setVariables((executionService.getVariables(execution.getId(), executionService.getVariableNames(execution.getId()))));
        return parser.parseExpression(expression, new TemplateParserContext()).getValue(context, String.class);
    }
}
