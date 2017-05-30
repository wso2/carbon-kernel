/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
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

package org.wso2.carbon.server.admin.common;

import org.wso2.carbon.base.ServerConfiguration;

public class ServerData {
    private String javaRuntimeName;
    private String javaVMVersion;
    private String javaVMVendor;
    private String javaHome;
    private String javaVersion;
    private String osName;
    private String osVersion;
    private String userHome;
    private String userTimezone;
    private String userName;
    private String userCountry;
    private String axis2Location;
    private String serverName;
    private String repoLocation;
    private String carbonVersion = "undefined";
    private String serverStartTime;
    private String serverStartUpDuration;
    private ServerUpTime serverUpTime;
    private String serverIp;

    private String registryType;

    private String dbName;
    private String dbVersion;
    private String dbDriverName;
    private String dbDriverVersion;
    private String dbURL;

    private String remoteRegistryURL;
    private String remoteRegistryChroot;

//    public static ServerConfiguration serverConfig;

    private boolean isTenantRequest;

    private boolean isRestricted = true;

    public ServerData(String serverName, String repoLocation,
                      boolean isTenantRequest,
                      boolean isRestricted) {
        this.isTenantRequest = isTenantRequest;
        this.isRestricted = isRestricted;
        init();
        this.serverName = serverName;
        if (!isTenantRequest) {
            this.repoLocation = repoLocation;
        }
    }

    public ServerData() {
    }

    private void init() {
        if (!isRestricted) {
            javaRuntimeName = System.getProperty("java.runtime.name");
            javaVMVersion = System.getProperty("java.vm.version");
            javaVMVendor = System.getProperty("java.vm.vendor");
            javaVersion = System.getProperty("java.version");
            carbonVersion = ServerConfiguration.getInstance().getFirstProperty("Version");
        } else {
            carbonVersion = null;
        }

        if (!isTenantRequest && !isRestricted) {
            userCountry = System.getProperty("user.country");
            userHome = System.getProperty("user.home");
            userTimezone = System.getProperty("user.timezone");
            userName = System.getProperty("user.name");
            osName = System.getProperty("os.name");
            osVersion = System.getProperty("os.version");
            javaHome = System.getProperty("java.home");
            axis2Location = axis2Location();
        }
    }

    private String axis2Location() {
        try {
            Class clazz = Class.forName("org.apache.axis2.engine.AxisEngine");
            java.net.URL url = clazz.getProtectionDomain().getCodeSource()
                    .getLocation();
            String location = url.toString();

            if (location.startsWith("jar")) {
                url = ((java.net.JarURLConnection) url.openConnection()).getJarFileURL();
                location = url.toString();
            }

            if (location.startsWith("file")) {
                java.io.File file = new java.io.File(url.getFile());

                return file.getAbsolutePath();
            } else {
                return url.toString();
            }
        } catch (Throwable t) {
            return "an unknown location";
        }
    }

    public String getJavaRuntimeName() {
        return javaRuntimeName;
    }

    public void setJavaRuntimeName(String javaRuntimeName) {
        this.javaRuntimeName = javaRuntimeName;
    }

    public String getJavaVMVersion() {
        return javaVMVersion;
    }

    public void setJavaVMVersion(String javaVMVersion) {
        this.javaVMVersion = javaVMVersion;
    }

    public String getUserCountry() {
        return userCountry;
    }

    public void setUserCountry(String userCountry) {
        if (!isTenantRequest) {
            this.userCountry = userCountry;
        }
    }

    public String getJavaVMVendor() {
        return javaVMVendor;
    }

    public void setJavaVMVendor(String javaVMVendor) {
        this.javaVMVendor = javaVMVendor;
    }

    public String getOsName() {
        return osName;
    }

    public void setOsName(String osName) {
        if (!isTenantRequest) {
            this.osName = osName;
        }
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        if (!isTenantRequest) {
            this.osVersion = osVersion;
        }
    }

    public String getUserHome() {
        return userHome;
    }

    public void setUserHome(String userHome) {
        if (!isTenantRequest) {
            this.userHome = userHome;
        }
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        if (!isTenantRequest) {
            this.userName = userName;
        }
    }

    public String getUserTimezone() {
        return userTimezone;
    }

    public void setUserTimezone(String userTimezone) {
        if (!isTenantRequest) {
            this.userTimezone = userTimezone;
        }
    }

    public String getJavaHome() {
        return javaHome;
    }

    public void setJavaHome(String javaHome) {
        if (!isTenantRequest) {
            this.javaHome = javaHome;
        }
    }

    public String getJavaVersion() {
        return javaVersion;
    }

    public void setJavaVersion(String javaVersion) {
        this.javaVersion = javaVersion;
    }

    public String getAxis2Location() {
        return axis2Location;
    }

    public void setAxis2Location(String axis2Location) {
        if (!isTenantRequest) {
            this.axis2Location = axis2Location;
        }
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getRepoLocation() {
        return repoLocation;
    }

    public void setRepoLocation(String repoLocation) {
        if (!isTenantRequest) {
            this.repoLocation = repoLocation;
        }
    }

    public String getCarbonVersion() {
        return carbonVersion;
    }

    public void setCarbonVersion(String carbonVersion) {
        this.carbonVersion = carbonVersion;
    }

    public String getServerStartTime() {
        return serverStartTime;
    }

    public void setServerStartTime(String serverStartTime) {
        if (!isTenantRequest) {
            this.serverStartTime = serverStartTime;
        }
    }

    public String getServerStartUpDuration() {
	    return serverStartUpDuration;
    }

	public void setServerStartUpDuration(String serverStartUpDuration) {
	    this.serverStartUpDuration = serverStartUpDuration;
    }

	public ServerUpTime getServerUpTime() {
        return serverUpTime;
    }

    public void setServerUpTime(ServerUpTime serverUpTime) {
        if (!isTenantRequest) {
            this.serverUpTime = serverUpTime;
        }
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        if (!isTenantRequest) {
            this.serverIp = serverIp;
        }
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String db) {
        if (!isTenantRequest) {
            this.dbName = db;
        }
    }

    public String getDbURL() {
        return dbURL;
    }

    public void setDbURL(String dbURL) {
        if (!isTenantRequest) {
            this.dbURL = dbURL;
        }
    }

    public String getDbVersion() {
        return dbVersion;
    }

    public void setDbVersion(String dbVersion) {
        if (!isTenantRequest) {
            this.dbVersion = dbVersion;
        }
    }

    public String getDbDriverName() {
        return dbDriverName;
    }

    public void setDbDriverName(String dbDriverName) {
        if (!isTenantRequest) {
            this.dbDriverName = dbDriverName;
        }
    }

    public String getDbDriverVersion() {
        return dbDriverVersion;
    }

    public void setDbDriverVersion(String dbDriverVersion) {
        if (!isTenantRequest) {
            this.dbDriverVersion = dbDriverVersion;
        }
    }

    public String getRegistryType() {
        return registryType;
    }

    public void setRegistryType(String registryType) {
        if (!isTenantRequest) {
            this.registryType = registryType;
        }
    }

    public String getRemoteRegistryURL() {
        return remoteRegistryURL;
    }

    public void setRemoteRegistryURL(String remoteRegistryURL) {
        if (!isTenantRequest) {
            this.remoteRegistryURL = remoteRegistryURL;
        }
    }

    public String getRemoteRegistryChroot() {
        return remoteRegistryChroot;
    }

    public void setRemoteRegistryChroot(String remoteRegistryChroot) {
        if (!isTenantRequest) {
            this.remoteRegistryChroot = remoteRegistryChroot;
        }
    }

    public String toString() {
        ServerConfiguration serverConfig = ServerConfiguration.getInstance();
        return "JVM Name='" + javaRuntimeName + '\'' +
               ", JVM Version='" + javaVMVersion + '\'' +
               ", JVM Vendor='" + javaVMVendor + '\'' +
               ", Java Home='" + javaHome + '\'' +
               ", OS Name='" + osName + '\'' +
               ", OS Version='" + osVersion + '\'' +
               ", user.home='" + userHome + '\'' +
               ", user.timezone='" + userTimezone + '\'' +
               ", username='" + userName + '\'' +
               ", user.country='" + userCountry + '\'' +
               ", Axis2 Location='" + axis2Location + '\'' +
               ", Server Name='" + serverName + '\'' +
               ", Repository Location='" + repoLocation + '\'' +
               ", " + serverConfig.getFirstProperty("Name") + " version ='" +
               serverConfig.getFirstProperty("Version") + '\'';
    }
}
