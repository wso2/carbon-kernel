/*                                                                             
 * Copyright 2004,2005 The Apache Software Foundation.                         
 *                                                                             
 * Licensed under the Apache License, Version 2.0 (the "License");             
 * you may not use this file except in compliance with the License.            
 * You may obtain a copy of the License at                                     
 *                                                                             
 *      http://www.apache.org/licenses/LICENSE-2.0                             
 *                                                                             
 * Unless required by applicable law or agreed to in writing, software         
 * distributed under the License is distributed on an "AS IS" BASIS,           
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.    
 * See the License for the specific language governing permissions and         
 * limitations under the License.                                              
 */
package org.wso2.carbon.osgi.security;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.service.permissionadmin.PermissionAdmin;
import org.osgi.service.permissionadmin.PermissionInfo;

import java.security.AllPermission;

/**
 * OSGi Security Permission Manager
 */
public class PermissionManager implements BundleListener {

    private BundleContext context;

    public PermissionManager(BundleContext context) {
        this.context = context;
    }

    public void bundleChanged(BundleEvent bundleEvent) {
        int event = bundleEvent.getType();
        if (event == BundleEvent.INSTALLED) {
            Bundle installedBundle = bundleEvent.getBundle();
            String bundleLocation = installedBundle.getLocation();
            PermissionAdmin permissionAdmin = getPermissionAdmin(context);
            if (permissionAdmin != null) {
                if (bundleLocation.startsWith("reference:file:plugins/")) {
                    PermissionInfo[] superTenantPermInfos = {
                            new PermissionInfo(AllPermission.class.getName(), "", ""),
                            //                            new PermissionInfo(PackagePermission.class.getName(), "*", PackagePermission.EXPORTONLY),
                            //                            new PermissionInfo(PackagePermission.class.getName(), "*", PackagePermission.IMPORT),
                            //                            new PermissionInfo(PropertyPermission.class.getName(), "user.home", "read"),
                            //                            new PermissionInfo(
                            //                                    FilePermission.class.getName(), file.getAbsolutePath(), "read"),
                            //                            new PermissionInfo(
                            //                                    ServicePermission.class.getName(), "org.eclipse.osgi.service.environment.EnvironmentInfo", ServicePermission.GET)

                    };
                    permissionAdmin.setPermissions(bundleLocation, superTenantPermInfos);
                } else {
                    // TODO: For non-super-tenant bundles. Should we deal with them at a different level (when installing cApps) 
                }
            }
        }
    }

    public PermissionAdmin getPermissionAdmin(BundleContext context) {
        return (PermissionAdmin) context.getService(context.getServiceReference(PermissionAdmin.class.getName()));
    }


    /*public void bundleChanged(BundleEvent bundleEvent) {
        System.out.println("-------------" + bundleEvent.getBundle().getSymbolicName());
        int event = bundleEvent.getType();

        *//*if (event == BundleEvent.INSTALLED) {
            Bundle installedBundle = bundleEvent.getBundle();
            String bundleLocation = installedBundle.getLocation();

            ConditionalPermissionAdmin admin = getConditionalPermissionAdmin(context);
            ConditionalPermissionInfo permInfo =
                    admin.newConditionalPermissionInfo("permission-01",
                                                       new ConditionInfo[]{
                                                               new ConditionInfo(BundleLocationCondition.class.getName(), new String[]{"fpp"}),
                                                       },
                                                       new PermissionInfo[]{
                                                               new PermissionInfo(AllPermission.class.getName(), "", "")
//                                                               new PermissionInfo(PackagePermission.class.getName(), "*", PackagePermission.IMPORT),
//                                                               new PermissionInfo(PackagePermission.class.getName(), "*", PackagePermission.EXPORTONLY)
                                                       },
                                                       ConditionalPermissionInfo.DENY);
            ConditionalPermissionUpdate update = admin.newConditionalPermissionUpdate();
            update.getConditionalPermissionInfos().add(permInfo);
            try {
                System.out.println(update.commit());
            } catch (Exception e) {
                e.printStackTrace();
            }

//            admin.setPermissions(
//                    bundleLocation,
//                    new PermissionInfo[]{
//                            new PermissionInfo(
//                                    PackagePermission.class.getName(), "*", PackagePermission.IMPORT),
//                            new PermissionInfo(
//                                    PropertyPermission.class.getName(), "user.home", "read"),
//                            new PermissionInfo(
//                                    FilePermission.class.getName(), file.getAbsolutePath(), "read"),
////                            new PermissionInfo(
////                                    ServicePermission.class.getName(), "org.eclipse.osgi.service.environment.EnvironmentInfo", ServicePermission.GET)
//
//                    });
        }*//*
    }*/

    /*private ConditionalPermissionAdmin getConditionalPermissionAdmin(BundleContext context) {
        return (ConditionalPermissionAdmin) context.
                getService(context.getServiceReference(ConditionalPermissionAdmin.class.getName()));
    }*/
}
