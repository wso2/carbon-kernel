package org.wso2.carbon.roles.mgt.ui;

import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.roles.mgt.stub.AddServerRolesServerRolesExceptionException;
import org.wso2.carbon.roles.mgt.stub.ReadServerRolesServerRolesExceptionException;
import org.wso2.carbon.roles.mgt.stub.RemoveServerRolesServerRolesExceptionException;
import org.wso2.carbon.roles.mgt.stub.ServerRolesManagerStub;

import java.rmi.RemoteException;

public class ServerRoleManagerClient {
    private static Log log = LogFactory.getLog(ServerRoleManagerClient.class);

    public ServerRolesManagerStub stub;

    public ServerRoleManagerClient(ConfigurationContext configCtx, String backendServerURL,
                                   String cookie) throws java.lang.Exception {
        String serviceURL = backendServerURL + "ServerRolesManager";
        stub = new ServerRolesManagerStub(configCtx, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }

    public String[] getServerRoles(String serverRoleType) throws java.lang.Exception {
        String[] serverRoles = new String[]{};
        try {
            serverRoles = stub.readServerRoles(serverRoleType);
        } catch (RemoteException e) {
            this.handleException(e.getMessage(), e);
        } catch (ReadServerRolesServerRolesExceptionException e) {
            this.handleException(e.getMessage(), e);
        }
        return serverRoles;
    }

    public boolean deleteServerRoles(String[] serverRolestoDelete, String serverRoleType)
            throws java.lang.Exception {
        boolean status = false;
        try {
            status = stub.removeServerRoles(serverRolestoDelete, serverRoleType);
        } catch (RemoteException e) {
            this.handleException(e.getMessage(), e);
        } catch (RemoveServerRolesServerRolesExceptionException e) {
            this.handleException(e.getMessage(), e);
        }
        return status;
    }

    public boolean addServerRoles(String[] serverRolestoAdd, String serverRoleType)
            throws java.lang.Exception {
        boolean status = false;
        try {
            status = stub.addServerRoles(serverRolestoAdd, serverRoleType);
        } catch (RemoteException e) {
            this.handleException(e.getMessage(), e);
        } catch (AddServerRolesServerRolesExceptionException e) {
            this.handleException(e.getMessage(), e);
        }
        return status;
    }

    public void handleException(String message, java.lang.Exception e) throws java.lang.Exception {
        log.error(message);
        throw new java.lang.Exception(message, e);
    }
}