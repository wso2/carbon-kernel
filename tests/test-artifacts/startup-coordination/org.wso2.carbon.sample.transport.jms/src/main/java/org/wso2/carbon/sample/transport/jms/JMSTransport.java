/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.sample.transport.jms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.sample.transport.mgt.Transport;

/**
 * A sample JMS transport implementation to test the dynamic registration of transport implementations and the
 * startup order resolver implementation.
 *
 * @since 5.0.0
 */
public class JMSTransport implements Transport {
    private static final Logger logger = LoggerFactory.getLogger(JMSTransport.class);

    @Override
    public void start() {
        logger.info("Transport service : " + this.getClass().getName());
    }

    @Override
    public void stop() {

    }
}
