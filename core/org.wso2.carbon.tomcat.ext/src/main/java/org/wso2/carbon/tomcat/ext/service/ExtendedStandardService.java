/*
*  Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.tomcat.ext.service;

import org.apache.catalina.Executor;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.core.StandardService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Extending the {@link StandardService} class to stop starting transport while
 * {@link org.apache.catalina.core.StandardEngine#start}
 */
public class ExtendedStandardService extends StandardService {
    private static Log log = LogFactory.getLog(ExtendedStandardService.class);

    /**
     * Exact same functionality as {@link org.apache.catalina.core.StandardService#startInternal()} method.
     * But the overridden method does not start connectors.
     *
     * @throws LifecycleException
     */
    @Override
    protected void startInternal() throws LifecycleException {
        if (log.isDebugEnabled()) {
            log.debug("starting extended standard service  :   " + this);
        }
        setState(LifecycleState.STARTING);
        // Start our defined Container first
        if (container != null) {
            synchronized (container) {
                container.start();
            }
        }
        synchronized (executors) {
            for (Executor executor : executors) {
                executor.start();
            }
        }
    }
}
