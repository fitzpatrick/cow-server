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


import org.wiredwidgets.cow.server.api.model.v2.Decision;
import org.wiredwidgets.cow.server.api.model.v2.Option;

public class DecisionEvaluator extends AbstractEvaluator<Decision> {

    @Override
    protected void evaluateInternal() {

        // first, evaluate the decision task and all the option paths
        // this ensures all objects are initialized with some completion status
        evaluate(this.activity.getTask());
        for (Option option : activity.getOptions()) {
            evaluate(option.getActivity().getValue());
        }

        CompletionState decisionTaskCompletionState = CompletionState.forName(this.activity.getTask().getCompletionState());
        if (decisionTaskCompletionState == CompletionState.NOT_STARTED) {
            // the decision task has not been reached in the workflow
            completionState = CompletionState.NOT_STARTED;
        }
        else if (decisionTaskCompletionState == CompletionState.OPEN) {
            completionState = CompletionState.OPEN;
        }
        else {
            // The decision is complete and we are in one of the OPTION branches
            // at a minimum, the status is OPEN
            // assume COMPLETED until we determine otherwise.  If we determine that status is OPEN,
            // we can break from the loop.
            boolean isCompleted = true;
            // check the options.  If at least one is OPEN then the decision is OPEN, else it is COMPLETED
            for (Option option : activity.getOptions()) {
                if (CompletionState.forName(option.getActivity().getValue().getCompletionState()) == CompletionState.OPEN) {
                    isCompleted = false;
                    break;
                }
            } // end for
            completionState = (isCompleted ? CompletionState.COMPLETED : CompletionState.OPEN);
        }
    }
}
