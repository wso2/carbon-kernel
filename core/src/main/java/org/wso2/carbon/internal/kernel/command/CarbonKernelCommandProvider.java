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
package org.wso2.carbon.internal.kernel.command;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.kernel.CarbonRuntime;
import org.wso2.carbon.kernel.tenant.Tenant;
import org.wso2.carbon.kernel.tenant.TenantRuntime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This service component is responsible for registering the Carbon kernel command provider
 */

@Component(
        name = "org.wso2.carbon.kernel.internal.CommandProvider",
        immediate = true
)

public class CarbonKernelCommandProvider implements CommandProvider {

    private CarbonRuntime carbonRuntime;
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

    public void unsetCarbonRuntime(CarbonRuntime carbonRuntime) {
        this.carbonRuntime = null;
    }

    public CarbonRuntime getCarbonRuntime() throws Exception {
        if (carbonRuntime == null) {
            throw new Exception("CarbonRuntime instance is not available");
        }
        return carbonRuntime;
    }

    @Reference(
            name = "carbon.runtime.service",
            service = CarbonRuntime.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetCarbonRuntime"
    )
    public void setCarbonRuntime(CarbonRuntime carbonRuntime) {
        this.carbonRuntime = carbonRuntime;
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

    public void _addTenant(CommandInterpreter ci) throws Exception {
        CarbonRuntime localCarbonRuntime = getCarbonRuntime();
        TenantRuntime<Tenant> tenantRuntime = localCarbonRuntime.getTenantRuntime();
        String[] args = extractArgs(ci);

        if (args.length == 5) {
            try {
                Tenant tenant = tenantRuntime.addTenant(args[0], args[1], args[2],
                        args[3], args[4], new HashMap<String, String>(0));
                System.out.println("Successfully Created the tenant with the ID " + tenant.getId());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            throw new Exception("Unexpected number of input parameters");
        }

    }

    public void _getTenantInfo(CommandInterpreter ci) throws Exception {
        CarbonRuntime localCarbonRuntime = getCarbonRuntime();
        TenantRuntime<Tenant> tenantRuntime = localCarbonRuntime.getTenantRuntime();
        String[] args = extractArgs(ci);
        if (args.length == 1) {
            try {
                Tenant tenant = tenantRuntime.getTenant(args[0]);
                if (tenant == null) {
                    System.out.println("Tenant with domain " + args[0] + " does not exists");
                    return;
                }
                System.out.println("Tenant data");
                System.out.println("Domain --> " + tenant.getDomain());
                System.out.println("Name --> " + tenant.getName());
                System.out.println("Description --> " + tenant.getDescription());
                System.out.println("Created date --> " + tenant.getCreatedDate());
                System.out.println("Admin Username --> " + tenant.getAdminUsername());
                System.out.println("Admin User Email Address --> " + tenant.getAdminUserEmailAddress());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            throw new Exception("Unexpected number of input parameters");
        }
    }

    private String[] extractArgs(CommandInterpreter ci) {
        List<String> argList = new ArrayList<>();
        String arg = ci.nextArgument();
        while (arg != null) {
            argList.add(arg);
            arg = ci.nextArgument();
        }
        return argList.toArray(new String[argList.size()]);
    }
}
