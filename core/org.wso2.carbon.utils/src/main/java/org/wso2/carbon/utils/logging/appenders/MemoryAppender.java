/* 
 * Copyright 2005,2006 WSO2, Inc. http://www.wso2.org
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
package org.wso2.carbon.utils.logging.appenders;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;
import org.wso2.carbon.utils.logging.CircularBuffer;

/**
 * This appender will be used to capture the logs and later send to clients, if requested via the
 * logging web service.
 * This maintains a circular buffer, of some fixed amount (say 100).
 */
public class MemoryAppender extends AppenderSkeleton {

    private CircularBuffer circularBuffer;
    private int bufferSize = -1;

    public MemoryAppender() {
        
    }

    public MemoryAppender(CircularBuffer circularBuffer) {
        this.circularBuffer = circularBuffer;
    }

    protected void append(LoggingEvent loggingEvent) {
        if (circularBuffer != null) {
            circularBuffer.append(loggingEvent);
        }
    }

    public void close() {
        // do we need to do anything here. I hope we do not need to reset the queue
        // as it might still be exposed to others
    }

    public boolean requiresLayout() {
        return true;
    }

    public CircularBuffer getCircularQueue(){
        return circularBuffer;
    }

    public void setCircularBuffer(CircularBuffer circularBuffer) {
        this.circularBuffer = circularBuffer;
    }

    public void activateOptions() {
        if (bufferSize < 0) {
            if (circularBuffer == null) {
                this.circularBuffer = new CircularBuffer();
            }
        } else {
            this.circularBuffer = new CircularBuffer(bufferSize);
        }
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }
}
