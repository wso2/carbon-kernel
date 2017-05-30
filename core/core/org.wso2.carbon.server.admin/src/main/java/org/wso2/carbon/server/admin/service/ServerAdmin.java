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

package org.wso2.carbon.server.admin.service;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.core.ServerManagement;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.dataaccess.DataAccessManager;
import org.wso2.carbon.registry.core.jdbc.dataaccess.JDBCDataAccessManager;
import org.wso2.carbon.server.admin.common.IServerAdmin;
import org.wso2.carbon.server.admin.common.ServerData;
import org.wso2.carbon.server.admin.common.ServerUpTime;
import org.wso2.carbon.server.admin.internal.ServerAdminDataHolder;
import org.wso2.carbon.utils.Controllable;
import org.wso2.carbon.utils.NetworkUtils;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.net.SocketException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Admin service to manage server related operations
 */
public class ServerAdmin extends AbstractAdmin implements ServerAdminMBean, IServerAdmin {
    private static final Log log = LogFactory.getLog(ServerAdmin.class);
    private static final int SECONDS_PER_DAY = 3600 * 24;

    private ServerAdminDataHolder dataHolder = ServerAdminDataHolder.getInstance();

    public ServerAdmin() {
    }

    public ServerData getServerData() throws Exception {
        boolean isRestricted = true;

        MessageContext msgContext = MessageContext.getCurrentMessageContext();
        if (msgContext != null) {
            HttpServletRequest request = (HttpServletRequest) msgContext
                    .getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
            HttpSession httpSession = request.getSession(false);
            if (httpSession != null) {

                String userName = (String) httpSession.getAttribute(ServerConstants.USER_LOGGED_IN);
                isRestricted = !getUserRealm().getAuthorizationManager().isUserAuthorized(userName,
                        "/permission/protected/server-admin/homepage",
                        CarbonConstants.UI_PERMISSION_ACTION);
            }
        } else { // non SOAP call
            isRestricted = false;
        }
        
        String location = null;
        if (!isRestricted) {
            location = getAxisConfig().getRepository().toString();
        }
        ServerData data =
                new ServerData(ServerConstants.SERVER_NAME, location,
                               (getTenantDomain() != null && !getTenantDomain().equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)),
                               isRestricted);

        if (!isRestricted) {
            Parameter systemStartTime =
                    getAxisConfig().getParameter(CarbonConstants.SERVER_START_TIME);
            long startTime = 0;
            if (systemStartTime != null) {
                startTime = Long.parseLong((String) systemStartTime.getValue());
            }
            Date stTime = new Date(startTime);
            SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss");
            data.setServerStartTime(dateFormatter.format(stTime));
            data.setServerUpTime(getTime((System.currentTimeMillis() - startTime) / 1000));
            Parameter systemStartUpDuration =
                    getAxisConfig().getParameter(CarbonConstants.START_UP_DURATION);
            if (systemStartUpDuration != null) {
                data.setServerStartUpDuration((String) systemStartUpDuration.getValue());
            }
            ServerConfigurationService serverConfig = dataHolder.getServerConfig();
            String registryType = serverConfig.getFirstProperty("Registry.Type");

            if (registryType == null) {
                registryType = "embedded";
            }
            data.setRegistryType(registryType);

            // Extract DB related data from RegistryContext
            Connection dbConnection = null;
            if (registryType.equals("embedded")) {
                try {
                    DataAccessManager dataAccessManager =
                            RegistryContext.getBaseInstance().getDataAccessManager();
                    if (!(dataAccessManager instanceof JDBCDataAccessManager)) {
                        String msg = "Failed to obtain DB connection. Invalid data access manager.";
                        log.error(msg);
                        throw new AxisFault(msg);
                    }
                    DataSource dataSource = ((JDBCDataAccessManager)dataAccessManager).getDataSource();
                    dbConnection = dataSource.getConnection();
                    DatabaseMetaData metaData = dbConnection.getMetaData();
                    if (metaData != null) {
                        data.setDbName(metaData.getDatabaseProductName());
                        data.setDbVersion(metaData.getDatabaseProductVersion());
                        data.setDbDriverName(metaData.getDriverName());
                        data.setDbDriverVersion(metaData.getDriverVersion());
                        data.setDbURL(metaData.getURL());
                    }
                } catch (SQLException e) {
                    String msg = "Cannot create DB connection";
                    log.error(msg, e);
                    throw new AxisFault(msg, e);
                } finally {
                    if (dbConnection != null) {
                        dbConnection.close();
                    }
                }
            } else if (registryType.equals("remote")) {
                data.setRemoteRegistryChroot(serverConfig.getFirstProperty("Registry.Chroot"));
                data.setRemoteRegistryURL(serverConfig.getFirstProperty("Registry.Url"));
            }
        }
        try {
            data.setServerIp(NetworkUtils.getLocalHostname());
        } catch (SocketException e) {
            throw new AxisFault(e.getMessage(), e);
        }
        return data;
    }

    public String getServerDataAsString() throws Exception {
        try {
            return getServerData().toString();
        } catch (AxisFault e) {
            String msg = "Cannot get server data";
            log.error(msg, e);
            throw new Exception(msg, e);
        }
    }

    public String getServerVersion() {
        return ServerConfiguration.getInstance().getFirstProperty("Version");
    }

    public boolean restart() throws Exception {
//        checkStandaloneMode();
        Thread thread = Thread.currentThread();
        ClassLoader originalClassloader = thread.getContextClassLoader();
        try {
            thread.setContextClassLoader(dataHolder.getRestartThreadContextClassloader());
            ConfigurationContext configurationContext = getConfigContext();
            final Controllable controllable =
                    (Controllable) configurationContext
                            .getProperty(ServerConstants.CARBON_INSTANCE);
            Thread th = new Thread() {
                public void run() {
                    try {
                        Thread.sleep(1000);
                        controllable.restart();
                    } catch (Exception e) {
                        String msg = "Cannot restart server";
                        log.error(msg, e);
                        throw new RuntimeException(msg, e);
                    }
                }
            };
            th.start();
            invalidateSession();
        } finally {
            thread.setContextClassLoader(originalClassloader);
        }
        return true;
    }

    public boolean restartGracefully() throws Exception {
//        checkStandaloneMode();
        Thread thread = Thread.currentThread();
        ClassLoader originalClassloader = thread.getContextClassLoader();
        try {
            thread.setContextClassLoader(dataHolder.getRestartThreadContextClassloader());
            ConfigurationContext configurationContext = getConfigContext();
            final Controllable controllable = (Controllable) configurationContext
                    .getProperty(ServerConstants.CARBON_INSTANCE);
            Thread th = new Thread() {
                public void run() {
                    try {
                        Thread.sleep(1000);
                        controllable.restartGracefully();
                    } catch (Exception e) {
                        String msg = "Cannot restart server";
                        log.error(msg, e);
                        throw new RuntimeException(msg, e);
                    }
                }
            };
            th.start();
            invalidateSession();
        } finally {
            thread.setContextClassLoader(originalClassloader);
        }
        return true;
    }

    public boolean shutdown() throws AxisFault {
//        checkStandaloneMode();
        Thread thread = Thread.currentThread();
        ClassLoader originalClassloader = thread.getContextClassLoader();
        try {
            thread.setContextClassLoader(dataHolder.getRestartThreadContextClassloader());
            ConfigurationContext configurationContext = getConfigContext();
            final Controllable controllable = (Controllable) configurationContext
                    .getProperty(ServerConstants.CARBON_INSTANCE);
            Thread th = new Thread() {
                public void run() {
                    try {
                        Thread.sleep(1000);
                        controllable.shutdown();
                    } catch (Exception e) {
                        String msg = "Cannot shutdown server";
                        log.error(msg, e);
                        throw new RuntimeException(msg, e);
                    }
                }
            };
            th.start();
            invalidateSession();
        } finally {
            thread.setContextClassLoader(originalClassloader);
        }
        return true;
    }

    public boolean shutdownGracefully() throws AxisFault {
//        checkStandaloneMode();
        Thread thread = Thread.currentThread();
        ClassLoader originalClassloader = thread.getContextClassLoader();
        try {
            thread.setContextClassLoader(dataHolder.getRestartThreadContextClassloader());
            ConfigurationContext configurationContext = getConfigContext();
            final Controllable controllable = (Controllable) configurationContext
                    .getProperty(ServerConstants.CARBON_INSTANCE);
            Thread th = new Thread() {
                public void run() {
                    try {
                        Thread.sleep(1000);
                        controllable.shutdownGracefully();
                    } catch (Exception e) {
                        String msg = "Cannot gracefully shutdown server";
                        log.error(msg, e);
                        throw new RuntimeException(msg, e);
                    }
                }
            };
            th.start();
            invalidateSession();
        } finally {
            thread.setContextClassLoader(originalClassloader);
        }
        return true;
    }

    public void startMaintenance() throws Exception {
        Thread thread = Thread.currentThread();
        ClassLoader originalClassloader = thread.getContextClassLoader();
        try {
            thread.setContextClassLoader(dataHolder.getRestartThreadContextClassloader());
            Map<String, TransportInDescription> inTransports = getAxisConfig().getTransportsIn();
            new ServerManagement(inTransports, getConfigContext()).startMaintenance();
            org.wso2.carbon.core.ServerStatus.setServerInMaintenance();
        } catch (AxisFault e) {
            String msg = "Cannot set server to maintenance mode";
            log.error(msg, e);
        } finally {
            thread.setContextClassLoader(originalClassloader);
        }
    }

    public void endMaintenance() throws Exception {
        Thread thread = Thread.currentThread();
        ClassLoader originalClassloader = thread.getContextClassLoader();
        try {
            thread.setContextClassLoader(dataHolder.getRestartThreadContextClassloader());
            Map<String, TransportInDescription> inTransports = getAxisConfig().getTransportsIn();
            new ServerManagement(inTransports, getConfigContext()).endMaintenance();
            try {
                org.wso2.carbon.core.ServerStatus.setServerRunning();
            } catch (AxisFault e) {
                String msg = "Cannot set server to running mode";
                log.error(msg, e);
            }
        } finally {
            thread.setContextClassLoader(originalClassloader);
        }
    }

    public boolean isAlive() {
        return true;
    }

    public String getServerStatus() throws Exception {
        return org.wso2.carbon.core.ServerStatus.getCurrentStatus();
    }

    private void invalidateSession() {
        MessageContext msgCtx = MessageContext.getCurrentMessageContext();
        if (msgCtx == null) {
            return;
        }
        HttpServletRequest request = (HttpServletRequest) msgCtx
                .getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
        try {
            if (request != null) {
                request.getSession().invalidate();
            }
        } catch (Exception ignored) { // Ignore invalidation of invalidated
                                      // sessions
            if (log.isDebugEnabled()) {
                log.debug("Ignore invalidation of invalidated sessions", ignored);
            }
        }
    }

    private ServerUpTime getTime(long timeInSeconds) {
        long days;
        int hours;
        int minutes;
        int seconds;
        days = timeInSeconds / SECONDS_PER_DAY;
        timeInSeconds = timeInSeconds - (days * SECONDS_PER_DAY);
        hours = (int) (timeInSeconds / 3600);
        timeInSeconds = timeInSeconds - ((long)hours * 3600);
        minutes = (int) (timeInSeconds / 60);
        timeInSeconds = timeInSeconds - ((long)minutes * 60);
        seconds = (int) timeInSeconds;

        return new ServerUpTime(days, hours, minutes, seconds);
    }

//    private void checkStandaloneMode() throws AxisFault {
//        if(!CarbonUtils.isRunningInStandaloneMode()) {
//           throw new AxisFault("Server shutdown & restart is supported only in standalone mode");
//        }
//    }

}
