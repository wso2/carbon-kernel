/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.securevault.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Activator class for Secure vault. will set bundle context when activator start.
 */
public class SecureVaultActivator implements BundleActivator {
    @Override
    public void start(BundleContext bundleContext) throws Exception {
        SecureVaultDataHolder.getInstance().setBundleContext(bundleContext);
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        SecureVaultDataHolder.getInstance().setBundleContext(null);
    }
}
