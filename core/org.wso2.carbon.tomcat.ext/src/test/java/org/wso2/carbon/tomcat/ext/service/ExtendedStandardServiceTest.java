/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.tomcat.ext.service;

import org.apache.catalina.Container;
import org.apache.catalina.Executor;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import org.powermock.reflect.Whitebox;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.logging.Logger;

import static org.mockito.Mockito.mock;

/**
 * ExtendedStandardServiceTest includes test scenarios for
 * ExtendedStandardService functionality.
 * @since 4.4.19
 */
public class ExtendedStandardServiceTest {

    private static final Logger log = Logger.getLogger("ExtendedStandardServiceTest");

    /**
     * Checks startInternal () functionality with Case 1.
     * Case1: Checks if startInternal () returns LifecycleException
     * when ExtendedStandardService is not already in STARTING_PREP state.
     */
    @Test(groups = {"org.wso2.carbon.tomcat.ext.service"}, expectedExceptions = LifecycleException.class)
    public void testStartInternalWithCase1 () throws LifecycleException {
        log.info("Testing if startInternal () returns returns LifecycleException when " +
                "ExtendedStandardService is not already in STARTING_PREP state");
        ExtendedStandardService extendedStandardService = new ExtendedStandardService();
        extendedStandardService.startInternal();
    }

    /**
     * Checks startInternal () functionality with Case 2.
     * Case2: Checks if startInternal () returns successfully with corresponding side effects applied
     * when ExtendedStandardService is already in STARTING_PREP state.
     */
    @Test(groups = {"org.wso2.carbon.tomcat.ext.service"})
    public void testStartInternalWithCase2 () throws LifecycleException {
        ExtendedStandardService extendedStandardService = new ExtendedStandardService();
        Whitebox.setInternalState(extendedStandardService, "state", LifecycleState.STARTING_PREP);

        Container container = mock(Container.class);
        Executor executor = mock(Executor.class);

        extendedStandardService.setContainer(container);
        extendedStandardService.addExecutor(executor);
        log.info("Testing if startInternal () returns successfully with corresponding side effects applied " +
                "when ExtendedStandardService is already in STARTING_PREP state");
        extendedStandardService.startInternal();

        Assert.assertEquals(extendedStandardService.getState(), LifecycleState.STARTING, "Newly created extended " +
                "standard service does not change its state to 'STARTING' after successfully calling " +
                    "startInternal () method");
    }
}
