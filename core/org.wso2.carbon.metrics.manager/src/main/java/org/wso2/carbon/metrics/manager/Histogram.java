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
package org.wso2.carbon.metrics.manager;

/**
 * A metric to calculate the distribution of a value.
 */
public interface Histogram extends Metric {

    /**
     * Adds a recorded value.
     *
     * @param value the length of the value
     */
    void update(int value);

    /**
     * Adds a recorded value.
     *
     * @param value the length of the value
     */
    void update(long value);

    /**
     * Returns the number of values recorded.
     *
     * @return the number of values recorded
     */
    long getCount();

}
