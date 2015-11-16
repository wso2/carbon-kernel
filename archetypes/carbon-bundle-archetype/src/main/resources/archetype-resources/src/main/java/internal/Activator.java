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
package ${package}.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import java.util.logging.Logger;

/**
 * This is a sample bundle activator class.
 */
public class Activator implements BundleActivator {
    Logger logger = Logger.getLogger(SampleBundleActivator.class.getName());

    //TODO: Use the sample class here
    public void start(BundleContext bundleContext) throws Exception {
        logger.info("This bundle is activated");
    }

    public void stop(BundleContext bundleContext) throws Exception {
        logger.info("This bundle is deactivated");
    }
}
