
### Setting up the Carbon Launcher

The WSO2 Carbon Launcher is responsible for initializing and booting up the Carbon server. This Launcher implementation resolves the initialization of the Carbon server instance. Before starting the Carbon server, the Launcher component performs a set of steps to load the initial startup configurations given in the default <CARBON_HOME>/bin/bootstrap/org.wso2.carbon.launcher-5.0.0.jar/launch.properties file.
WSO2 Carbon Kernel 5.1.0 includes the <CARBON_HOME>/conf/osgi/launch.properties file, to change the default launch configurations. Therefore, if you want to customize the startup process by updating the configurations in the default launch.properties file, you can do so by updating this second file.
For detailed explanations on configuring the Launcher component, see the following topics.
