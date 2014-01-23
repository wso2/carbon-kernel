/*
 *  Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.utils.logging;

import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.TriggeringEventEvaluator;


/**
 * <code>DefaultEvaluator</code> implements the single method
 * <code>TriggeringEventEvaluator</code> interface. This class
 * allows <code>IMAppender</code> to decide when to perform the
 * IM message delivery.
 *
 * @author Rafael Luque & Ruth Zamorano
 * @version $Revision: 1.2 $
 */

public class DefaultEvaluator implements TriggeringEventEvaluator {

    /**
     * Is this <code>event</code> the e-mail triggering event?
     * <p/>
     * <p>This method returns <code>true</code> if the event level
     * has ERROR level or higher. Otherwise it returns
     * <code>false</code>.
     */
    public boolean isTriggeringEvent(LoggingEvent event) {
        return event.getLevel().isGreaterOrEqual(Level.TRACE);
    }
}
