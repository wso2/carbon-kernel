/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.security.internal.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.BeanAccess;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

/**
 * Builds DefaultPermissionInfoCollection from the permissions.yml
 */
public class YAMLSecurityConfigBuilder {

    private static final Logger log = LoggerFactory.getLogger(YAMLSecurityConfigBuilder.class);

    public static final String JAAS_PERMISSIONS_CONF = "jaas.permissions.conf";

    private YAMLSecurityConfigBuilder() {
    }

    /**
     * Parses &amp; creates the object model for the DefaultPermissionInfoCollection from the permissions.yml
     *
     * @return DefaultPermissionInfoCollection
     */
    public static DefaultPermissionInfoCollection buildDefaultPermissionInfoCollection() {

        DefaultPermissionInfoCollection permissionInfoCollection;
        String jaasPermissionsConfigFile = System.getProperty(JAAS_PERMISSIONS_CONF,
                           "conf" + File.separator + "security" + File.separator + "permissions.yml");

        File file = new File(jaasPermissionsConfigFile);
        if (file.exists()) {
            try (Reader in = new InputStreamReader(new FileInputStream(file), StandardCharsets.ISO_8859_1)) {
                Yaml yaml = new Yaml();
                yaml.setBeanAccess(BeanAccess.FIELD);
                permissionInfoCollection = yaml.loadAs(in, DefaultPermissionInfoCollection.class);
            } catch (IOException e) {
                String msg = "Error while loading " + jaasPermissionsConfigFile + " configuration file";
                throw new RuntimeException(msg, e);
            }
        } else {
            log.warn("permissions.yml file is not available. Starting server with default permissions.");
            permissionInfoCollection = getDefault();
        }

        return permissionInfoCollection;
    }

    private static DefaultPermissionInfoCollection getDefault() {

        Set<DefaultPermissionInfo> permissionInfoSet = new HashSet<>();
        permissionInfoSet.add(new DefaultPermissionInfo("javax.security.auth.AuthPermission", "createLoginContext",
                                                        null));
        permissionInfoSet.add(new DefaultPermissionInfo("javax.security.auth.AuthPermission", "doAsPrivileged",  null));
        permissionInfoSet.add(new DefaultPermissionInfo("javax.security.auth.AuthPermission", "modifyPrincipals",
                                                        null));
        permissionInfoSet.add(new DefaultPermissionInfo("javax.security.auth.AuthPermission",
                                                        "createLoginContext.CarbonSecurityConfig", null));
        permissionInfoSet.add(new DefaultPermissionInfo("javax.security.auth.AuthPermission", "getSubject", null));
        permissionInfoSet.add(new DefaultPermissionInfo("java.lang.RuntimePermission",
                                                        "accessClassInPackage.sun.security.provider", null));
        permissionInfoSet.add(new DefaultPermissionInfo("java.lang.RuntimePermission", "getProtectionDomain", null));
        permissionInfoSet.add(new DefaultPermissionInfo("java.lang.RuntimePermission", "createSecurityManager", null));
        permissionInfoSet.add(new DefaultPermissionInfo("java.lang.RuntimePermission", "setSecurityManager", null));
        permissionInfoSet.add(new DefaultPermissionInfo("java.lang.RuntimePermission", "getClassLoader", null));
        permissionInfoSet.add(new DefaultPermissionInfo("java.lang.RuntimePermission", "accessDeclaredMembers",  null));
        permissionInfoSet.add(new DefaultPermissionInfo("java.io.FilePermission", "<<ALL FILES>>", "read,write," +
                                                                                                   "delete,execute"));
        permissionInfoSet.add(new DefaultPermissionInfo("org.osgi.framework.AdaptPermission",
                                                        "(adaptClass=org.osgi.framework.wiring.*)",  "adapt"));
        permissionInfoSet.add(new DefaultPermissionInfo("org.osgi.framework.AdaptPermission",
                                                        "(adaptClass=org.eclipse.osgi.container.Module)", "adapt"));
        permissionInfoSet.add(new DefaultPermissionInfo("javax.management.MBeanServerPermission",
                                                        "createMBeanServer",  null));
        permissionInfoSet.add(new DefaultPermissionInfo("javax.management.MBeanPermission", "-#-[-]", "queryNames"));
        permissionInfoSet.add(new DefaultPermissionInfo("java.net.SocketPermission", "*", "accept,connect,listen," +
                                                                                          "resolve"));
        permissionInfoSet.add(new DefaultPermissionInfo("java.net.NetPermission", "specifyStreamHandler", null));
        permissionInfoSet.add(new DefaultPermissionInfo("java.lang.reflect.ReflectPermission",
                                                        "suppressAccessChecks",  null));
        permissionInfoSet.add(new DefaultPermissionInfo("org.osgi.framework.AdminPermission", "*", "*"));
        permissionInfoSet.add(new DefaultPermissionInfo("org.osgi.framework.BundlePermission", "*", "host,provide," +
                                                                                                    "fragment"));
        permissionInfoSet.add(new DefaultPermissionInfo("ava.util.PropertyPermission", "*", "read,write"));
        permissionInfoSet.add(new DefaultPermissionInfo("java.lang.management.ManagementPermission", "control", null));
        permissionInfoSet.add(new DefaultPermissionInfo("java.util.logging.LoggingPermission", "control", null));
        permissionInfoSet.add(new DefaultPermissionInfo("java.security.SecurityPermission", "setPolicy", null));
        permissionInfoSet.add(new DefaultPermissionInfo("org.osgi.framework.PackagePermission", "*", "exportonly," +
                                                                                                     "import"));
        permissionInfoSet.add(new DefaultPermissionInfo("org.osgi.framework.ServicePermission", "*", "get,register"));


        DefaultPermissionInfoCollection permissionInfoCollection = new DefaultPermissionInfoCollection();
        permissionInfoCollection.setPermissions(permissionInfoSet);
        return permissionInfoCollection;
    }

}
