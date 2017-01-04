# Dropping OSGi Bundles into a Carbon Server
> The process of directly adding OSGi bundles to a Carbon server is explained below. For the full list of capabilities available in this kernel version, see the **features** section in the [root README.md file](../../README.md#key-features-and-tools). 

The 'lib' capability allows you to apply new OSGi bundles by simply adding them to the `<CARBON_HOME>/lib` directory. These bundles will be automatically fetched by the server launcher and executed during server startup. Carbon maintains a `bundles.info` file for every runtime in the server and it contains information about all the bundles that exist in the lib directory. This file is stored in the `<CARBON_HOME>/wso2/<Runtime>/configuration/org.eclipse.equinox.simpleconfigurator` directory.

The server uses the information in the `bundles.info` file of the relevant runtime to install and start the OSGi bundles from the lib directory. Therefore, when you add new OSGi bundles to the lib directory, the `bundles.info` file (relevant to the runtime) should be updated. There are two ways of updating this file as follows:

## Update the `bundles.info` file during server launch

The `bundles.info` file will be automatically updated during server startup if you have the required configurations in the server launcher. That is, you need to have the 'lib' capability enabled as a Carbon startup event listener (implementation of the `org.wso2.carbon.launcher.CarbonServerListener` Java interface) in the `launch.properties` file (stored in the `<CARBON_HOME>/conf/osgi` directory). When the server starts up, the lib capability listens to a `CarbonServerEvent` of type `STARTING` and is thereby executed during server launch.

## Update the `bundles.info` file manually using tool

You can update the `bundles.info` file with the latest bundle information from the lib directory at any given time as explained below. This means, you can decouple the function of updating the `bundles.info` file from the server startup process. Note that this will improve the server startup speed.

> Read more about Carbon tools and the instructions for developing new tools from [here](DevelopingaCarbonTool.md). 

To manually update the `bundles.info` file:

1. Open a command prompt and navigate to the `<CARBON_HOME>/bin` directory.
2. Run the lib tool by executing the following script:

     * *On Unix:* `sh osgi-lib.sh [Carbon_Runtime]`
      
     * *On Windows:* `osgi-lib.bat [Carbon_Runtime]`
      
   > Note that `Carbon_Runtime` should be replaced with the name of the required Carbon runtime. The `bundles.info` file of this runtime will be updated as a result. You can use the ‘ALL’ keyword for the `Carbon_Runtime` if you want to update the `bundles.info` files of all Carbon runtimes. For example, use `sh osgi-lib.sh ALL`.
