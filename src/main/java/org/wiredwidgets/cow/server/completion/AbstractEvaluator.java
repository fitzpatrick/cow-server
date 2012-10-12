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
package org.wiredwidgets.cow.server.completion;

import java.math.BigInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.wiredwidgets.cow.server.api.model.v2.Activity;

public abstract class AbstractEvaluator<T extends Activity> implements Evaluator<T> {

    @Autowired
    private EvaluatorFactory factory;
    
    protected T activity;
    protected History history;
    protected int percentComplete;
    protected CompletionState completionState;
    protected String processInstanceId;

    @Override
    public void evaluate() {
        evaluateInternal();
        activity.setPercentComplete(BigInteger.valueOf((long) percentComplete));
        activity.setCompletionState(completionState.getName());
    }

    /*
     * To be implemented by subclasses.  This method should set the values of protected
     * variables.
     */
    protected abstract void evaluateInternal();

    protected final void evaluate(Activity activity) {
        factory.getEvaluator(processInstanceId, activity, history).evaluate();
    }

    protected final void setCompletionState(String name) {
        completionState = CompletionState.forName(name);
    }

    @Override
    public void setActivity(T activity) {
        this.activity = activity;
    }

    @Override
    public void setHistory(History history) {
        this.history = history;
    }

    @Override
    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }
}
