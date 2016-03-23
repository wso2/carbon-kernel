/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
*/
package org.wso2.carbon.kernel.deployment;

/**
 * The deployment lifecycle event. An instance of this is used
 * to fire lifecycle events to deployment lifecycle listeners.
 *
 * This includes the current artifact metadata as well as the
 * currently triggered event.
 *
 * @since 5.1.0
 */
public class LifecycleEvent {

    public static final String BEFORE_START_EVENT = "before_start";

    public static final String AFTER_START_EVENT = "after_start";

    public static final String BEFORE_STOP_EVENT = "before_stop";

    public static final String AFTER_STOP_EVENT = "after_stop";

    private String eventType;

    private Lifecycle lifecycle;

    public LifecycleEvent(Lifecycle lifecycle, String eventType) {
        this.eventType = eventType;
        this.lifecycle = lifecycle;
    }

    public String getEventType() {
        return eventType;
    }

    public Lifecycle getLifecycle() {
        return lifecycle;
    }
}
