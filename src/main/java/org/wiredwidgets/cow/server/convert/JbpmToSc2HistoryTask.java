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

import javax.xml.datatype.XMLGregorianCalendar;
import org.wiredwidgets.cow.server.api.service.HistoryTask;
import org.springframework.core.convert.converter.Converter;

/**
 *
 * @author JKRANES
 */
public class JbpmToSc2HistoryTask extends AbstractConverter implements Converter<org.jbpm.api.history.HistoryTask, HistoryTask> {
    
    @Override
    public HistoryTask convert(org.jbpm.api.history.HistoryTask source) {
        HistoryTask target = new HistoryTask();
        target.setAssignee(source.getAssignee());
        if (source.getCreateTime() != null) {
            target.setCreateTime(this.getConverter().convert(source.getCreateTime(), XMLGregorianCalendar.class));
        }
        target.setDuration(source.getDuration());
        if (source.getEndTime() != null) {
            target.setEndTime(this.getConverter().convert(source.getEndTime(), XMLGregorianCalendar.class));
        }
        target.setExecutionId(source.getExecutionId());
        target.setId(source.getId());
        target.setOutcome(source.getOutcome());
        target.setState(source.getState());

        return target;
    }

}
