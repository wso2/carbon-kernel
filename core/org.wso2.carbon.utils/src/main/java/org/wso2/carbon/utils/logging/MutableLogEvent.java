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
