/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.kernel.internal.command;

import org.apache.felix.scr.annotations.*;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.wso2.carbon.kernel.CarbonRuntime;
import org.wso2.carbon.kernel.tenant.Tenant;
import org.wso2.carbon.kernel.tenant.TenantRuntime;

import java.util.ArrayList;
import java.util.List;


@Component(
        name = "org.wso2.carbon.kernel.internal.CommandProvider",
        description = "This service component is responsible for registering the Carbon kernel command provider",
        immediate = true
)
@Reference(
        name = "carbon.runtime.service",
        referenceInterface = CarbonRuntime.class,
        cardinality = ReferenceCardinality.MANDATORY_UNARY,
        policy = ReferencePolicy.DYNAMIC,
        bind = "setCarbonRuntime",
        unbind = "unsetCarbonRuntime"
)
public class CarbonKernelCommandProvider implements CommandProvider {

    private CarbonRuntime carbonRuntime;
    private ServiceRegistration serviceRegistration;

    @Activate
    public void registerCommandProvider(BundleContext bundleContext) {
        serviceRegistration = bundleContext.registerService(CommandProvider.class.getName(), this, null);
    }

    @Deactivate
    public void unregisterCommandProvider(BundleContext bundleContext) {
        serviceRegistration.unregister();
    }

    public void setCarbonRuntime(CarbonRuntime carbonRuntime) {
        this.carbonRuntime = carbonRuntime;
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

    @Override
    public String getHelp() {
        return "---Tenants---\n" +
                "\taddTenant <domain> <name> <description> <admin username> <admin email address> - Adds a tenant\n" +
                "\t\t<domain> - tenant domain\n" +
                "\t\t<name> - tenant name\n" +
                "\t\t<description> - tenant description\n" +
                "\t\t<admin username> - Administrator's username\n" +
                "\t\t<admin email address> - Administrator's email address\n";

    }

    public void _addTenant(CommandInterpreter ci) throws Exception {
        CarbonRuntime localCarbonRuntime = getCarbonRuntime();
        TenantRuntime tenantRuntime = localCarbonRuntime.getTenantRuntime();
        String[] args = extractArgs(ci);

        if (args.length == 5) {
            try {
                Tenant tenant = tenantRuntime.addTenant(args[0], args[1], args[2], args[3], args[4], null);
                System.out.println("Successfully Created the tenant with the ID " + tenant.getID());
            } catch (Exception e) {
                e.printStackTrace();
            }
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
