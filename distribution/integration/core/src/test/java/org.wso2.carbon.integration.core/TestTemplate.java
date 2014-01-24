package org.wso2.carbon.integration.core;

//Template class for running all the test cases.


import org.wso2.carbon.base.ServerConfigurationException;
import org.wso2.carbon.utils.ServerConstants;

public abstract class TestTemplate extends CarbonIntegrationTestCase {

    protected String sessionCookie = null;
    private ServerLogin serverLogin;
    protected String frameworkPath;
    private final SimpleAxis2ServerManager httpServerManager = new SimpleAxis2ServerManager();


    // The template method

    public void testTemplate() throws Exception {
        FrameworkSettings.getProperty();
        serverLogin = new ServerLogin();
        frameworkPath = FrameworkSettings.getFrameworkPath();

        /* the three different kind of tests that need to tested.
           The common config instantiation for all the tests */
        //   log.debug("set keystores");
        setKeyStore();
        //     log.debug("running init method");
        init();

        //Test without login
        //      log.debug("Run runFailureCase template");
        runFailureCase();

        //    log.debug("Relogin to the server");
        sessionCookie = login();
//        if (System.getProperty(ServerConstants.CARBON_HOME).contains("wso2esb-")) {
//            System.out.println("AXIS2 SERVER STARTUP CALLED***********************");
//            httpServerStart();
//        }

        //Test with login
        //    log.debug("Run runSuccessCase template");
        runSuccessCase();

        //    log.debug("logout from the server");
        logout();
//        if (System.getProperty(ServerConstants.CARBON_HOME).contains("wso2esb-")) {
//            httpServerStop();
//        }

        //Test with logout
        //     log.debug("runFailureCase with null session cookie");
        runFailureCase();

        // Cleaning up the configurations
        //    log.debug("Relogin to the server");
        sessionCookie = login();
        //       log.debug("run cleanup template method");
        cleanup();

    }

    private void setKeyStore() {
        String clientTrustStorePath = FrameworkSettings.TRUSTSTORE_PATH;
        System.setProperty("javax.net.ssl.trustStore", clientTrustStorePath);
        System.setProperty("javax.net.ssl.trustStorePassword", FrameworkSettings.TRUSTSTORE_PASSWORD);
        System.setProperty("javax.net.ssl.trustStoreType", "JKS");
    }

    public abstract void init();


    //for the test that is expected to pass(in login mode)

    public abstract void runSuccessCase();


    //for the test that is expected to fail(without login and after logout)
    public abstract void runFailureCase();


    public abstract void cleanup();


    //the concrete method for login
    public String login() {
        try {
            return serverLogin.login();
        } catch (Exception e) {
//            log.debug("Server login failed");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }


    //the concrete method for logout
    protected void logout() {
        try {
            serverLogin.logout();
        } catch (Exception e) {
            //          log.debug("Server logout failed");
            e.printStackTrace();
        }
    }


    //the concrete method for start simple axisServer
    protected void httpServerStart() {
        try {
            httpServerManager.startServer();
        } catch (ServerConfigurationException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    //the concrete method for stop simple axisServer
    protected void httpServerStop() {
        try {
            httpServerManager.shutdown();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

}
