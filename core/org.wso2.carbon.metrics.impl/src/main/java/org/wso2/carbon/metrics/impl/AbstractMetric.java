/*
 * Copyright 2014 WSO2 Inc. (http://wso2.org)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.metrics.impl;

import java.util.Observable;
import java.util.Observer;

import org.wso2.carbon.metrics.manager.Level;
import org.wso2.carbon.metrics.manager.Metric;

/**
 * An abstract class to keep generic behavior for metric instances. This class implements {@link Observer} interface and
 * takes part in observer pattern to update the enabled flag
 */
public abstract class AbstractMetric implements Observer, Metric {

    /**
     * A flag to indicate whether the metric is enabled
     */
    private boolean enabled;

    /**
     * The level used when creating the metric
     */
    private final Level level;

    public AbstractMetric(Level level) {
        this.level = level;
    }

    protected final boolean isEnabled() {
        return enabled;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    @Override
    public void update(Observable o, Object arg) {
        Level newLevel = (Level) arg;
        setEnabled(newLevel);
    }

    public void setEnabled(Level newLevel) {
        // Enable if the new threshold level is greater than or equal to current level.
        // This should be done only if the new level is not equal to OFF.
        // Otherwise the condition would fail when comparing two "OFF" levels
        enabled = newLevel.intLevel() >= level.intLevel() && newLevel.intLevel() > Level.OFF.intLevel();
    }

}
