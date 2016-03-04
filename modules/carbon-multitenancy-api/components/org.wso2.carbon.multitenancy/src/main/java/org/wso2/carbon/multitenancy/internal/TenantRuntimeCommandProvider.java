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
package org.wso2.carbon.multitenancy.internal;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.multitenancy.api.Tenant;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO
 */
@Component(
        name = "TenantRuntimeCommandProviderServiceComponent",
        service = CommandProvider.class
)
public class TenantRuntimeCommandProvider implements CommandProvider {

    @Override
    public String getHelp() {
        return "---Tenants---\n" +
                "\tloadTenant <domain> - loads a tenant\n" +
                "\t\t<domain> - tenant domain\n" +
                "\tgetLoadedTenants - get all loaded tenants";

    }

    public void _loadTenant(CommandInterpreter ci) throws Exception {
        OSGiServiceHolder.getInstance().getTenantRuntime()
                .ifPresent(tenantRuntime -> {
                            String[] args = extractArgs(ci);
                            if (args.length == 1) {
                                try {
                                    Tenant tenant = tenantRuntime.loadTenant(args[0]);
                                    if (tenant == null) {
                                        System.out.println("Tenant with domain " + args[0] + " does not exists");
                                        return;
                                    }
                                    System.out.println("Tenant data");
                                    System.out.println("Domain --> " + tenant.getDomain());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                );

    }

    public void _getLoadedTenants(CommandInterpreter ci) throws Exception {
        OSGiServiceHolder.getInstance().getTenantRuntime()
                .ifPresent(tenantRuntime -> {
                    try {
                        tenantRuntime.getLoadedTenants()
                                .forEach(tenant ->
                                        System.out.println("Tenant Domain --> " + tenant.getDomain()));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
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
