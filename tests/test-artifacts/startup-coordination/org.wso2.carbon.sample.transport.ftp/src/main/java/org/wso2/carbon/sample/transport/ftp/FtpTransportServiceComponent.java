/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.sample.transport.ftp;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.sample.transport.mgt.Transport;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.stream.IntStream;

/**
 * OSGi service component that will register FTP transports.
 *
 * @since 5.2.5
 */
@Component(immediate = true)
public class FtpTransportServiceComponent {

    private static final Logger logger = LoggerFactory.getLogger(FtpTransportServiceComponent.class);
    private static final int TRACKED_SERVICE_COUNT = 2;
    private static final int SKIPPED_SERVICE_COUNT = 2;

    @Activate
    protected void activate(BundleContext bundleContext) {
        registerTrackedTransports(bundleContext);
        registerSkippedTransports(bundleContext);
        logger.debug("FTP transport service component activated.");
    }

    @Deactivate
    protected void deactivate(BundleContext bundleContext) {
        logger.debug("FTP transport service component deactivated.");
    }

    private void registerTrackedTransports(BundleContext bundleContext) {
        IntStream.range(0, TRACKED_SERVICE_COUNT)
                .forEach($ -> {
                    Dictionary<String, Object> properties = new Hashtable<>();
                    properties.put("skipCarbonStartupResolver", false);
                    bundleContext.registerService(Transport.class, new FtpTransport(), properties);
                });
    }

    private void registerSkippedTransports(BundleContext bundleContext) {
        IntStream.range(0, SKIPPED_SERVICE_COUNT)
                .forEach($ -> {
                    Dictionary<String, Object> properties = new Hashtable<>();
                    properties.put("skipCarbonStartupResolver", true);
                    bundleContext.registerService(Transport.class, new FtpTransport(), properties);
                });
    }
}
