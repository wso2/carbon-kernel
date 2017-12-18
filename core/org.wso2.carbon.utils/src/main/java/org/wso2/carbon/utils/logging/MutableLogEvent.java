/*
 * Copyright 2017 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.utils.logging;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.util.StringMap;

/**
 * Creates a new mutable log event to append to the CircularBuffer
 *
 * Refer {@link org.apache.logging.log4j.core.impl.MutableLogEvent}
 */
public class MutableLogEvent {
    private org.apache.logging.log4j.core.impl.MutableLogEvent mutableLogEvent;

    public MutableLogEvent(LogEvent logEvent) {
        mutableLogEvent = new org.apache.logging.log4j.core.impl.MutableLogEvent();
        mutableLogEvent.setLoggerName(logEvent.getLoggerName());
        mutableLogEvent.setMarker(logEvent.getMarker());
        mutableLogEvent.setLoggerFqcn(logEvent.getLoggerFqcn());
        mutableLogEvent.setLevel(logEvent.getLevel());
        mutableLogEvent.setMessage(logEvent.getMessage());
        mutableLogEvent.setContextData((StringMap) logEvent.getContextData());
        mutableLogEvent.setContextStack(logEvent.getContextStack());
        mutableLogEvent.setTimeMillis(logEvent.getTimeMillis());
        mutableLogEvent.setNanoTime(logEvent.getNanoTime());
        mutableLogEvent.setThreadId(logEvent.getThreadId());
        mutableLogEvent.setThreadName(logEvent.getThreadName());
        setMutableLogEvent(mutableLogEvent);
    }

    public org.apache.logging.log4j.core.impl.MutableLogEvent getMutableLogEvent() {
        return mutableLogEvent;
    }

    public void setMutableLogEvent(org.apache.logging.log4j.core.impl.MutableLogEvent mutableLogEvent) {
        this.mutableLogEvent = mutableLogEvent;
    }
}
