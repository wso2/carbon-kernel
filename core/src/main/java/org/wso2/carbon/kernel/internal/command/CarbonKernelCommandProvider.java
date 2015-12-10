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
package org.wso2.carbon.kernel.internal.command;

import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

/**
 * This service component is responsible for registering the Carbon kernel command provider.
 *
 * @since 5.0.0
 */

@Component(
        name = "org.wso2.carbon.kernel.internal.command.CarbonKernelCommandProvider",
        immediate = true
)

public class CarbonKernelCommandProvider implements CommandProvider {

    private ServiceRegistration<CommandProvider> serviceRegistration;

    @Activate
    public void registerCommandProvider(BundleContext bundleContext) {
        serviceRegistration = bundleContext.registerService(CommandProvider.class, this, null);
    }

    @Deactivate
    public void unregisterCommandProvider(BundleContext bundleContext) {
        if (serviceRegistration != null) {
            serviceRegistration.unregister();
        }
    }

    @Override
    public String getHelp() {
        return "---Tenants---\n" +
                "\taddTenant <domain> <name> <description> <admin username> <admin email address> - Adds a tenant\n" +
                "\t\t<domain> - tenant domain\n" +
                "\t\t<name> - tenant name\n" +
                "\t\t<description> - tenant description\n" +
                "\t\t<admin username> - Administrator's username\n" +
                "\t\t<admin email address> - Administrator's email address\n" +
                "\tgetTenantInfo <domain> - Retrieve tenant data\n" +
                "\t\t<domain> - Tenant domain\n";

    }

}
