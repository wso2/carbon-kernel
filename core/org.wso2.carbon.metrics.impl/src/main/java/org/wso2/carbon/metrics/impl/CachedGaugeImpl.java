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

import java.util.concurrent.TimeUnit;

import org.wso2.carbon.metrics.manager.Gauge;
import org.wso2.carbon.metrics.manager.Level;

import com.codahale.metrics.CachedGauge;

/**
 * Implementation of cached {@link Gauge} metric
 */
public class CachedGaugeImpl<T> extends AbstractMetric implements com.codahale.metrics.Gauge<T> {

    private CachedGauge<T> gauge;

    public CachedGaugeImpl(Level level, long timeout, TimeUnit timeoutUnit, final Gauge<T> gauge) {
        super(level);
        this.gauge = new CachedGauge<T>(timeout, timeoutUnit) {
            @Override
            protected T loadValue() {
                return gauge.getValue();
            }
        };
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.codahale.metrics.Gauge#getValue()
     */
    @Override
    public T getValue() {
        if (isEnabled()) {
            return gauge.getValue();
        }
        return null;
    }

}
