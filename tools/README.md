
# Converting JARs to OSGi Bundles

Java archive (JAR) to OSGi bundle converter is a standalone tool that can be used for products based on Carbon Kernel 5.x.x versions (WSO2 Carbon 5.x.x platform). The implementation of this tool has been added to the freshly introduced tools Maven module of the WSO2 Carbon 5.x.x platform.

Read more about Carbon tools and the instructions for developing new tools from here. 

The need for this functionality arose due to the availability of third-party Java archive (JAR) files, which may not have their corresponding OSGi bundle implementation. In order to deploy them in the Carbon platform, these Java archive files need to be in their corresponding OSGi bundle form. The primary purpose of this tool is to convert JAR files to their corresponding OSGi bundle form. 
Unlike a JAR file, an OSGi bundle is simply not a single application packaged into a single JAR file, but utilizes a modular approach in forming an application. An OSGi bundle contains class files, resource files similar to a JAR file. In addition, it must contain extra metadata, which are absent in a JAR file. These metadata are primarily used to declare the contained packages that are externally visible from the bundle and also to declare the external packages on which the bundle depends. Hence, within the implementation, we primarily generate the following files, which are copied along with the original JAR file to the OSGi bundle.

  **A MANIFEST.MF file:** This file holds the metadata generated using the original JAR file.

  **A p2.inf file:** This file holds simple instructions to run the bundle(s) by default in the OSGi framework.

The folder structure within the OSGi bundle is as follows:

  **META-INF**
 
  **MANIFEST.MF**
 
  **p2.inf**

  **original_jar_file.jar**

This functionality was previously implemented (for example in wso2/carbon-kernel master 4.5.0) as part of the https://github.com/wso2/carbon-kernel/tree/4.5.x/core/org.wso2.carbon.server module and was executed during server startup. However, with the increasing need to improve the server startup time, we have decided to separate this functionality from the https://github.com/wso2/carbon-kernel/tree/4.5.x/core/org.wso2.carbon.server module and integrate it to the WSO2 Carbon 5.x.x platform as a separate tool under the freshly introduced https://github.com/wso2/carbon-kernel/tree/master/tools module.

## To convert JARs to OSGi bundles:

The 'Jar to Bundle Converter' tool that is shipped with Carbon Kernel can be executed to convert the desired Java archive (.jar) file(s) to their corresponding OSGi bundle form, at a desired destination in the file system. You can execute the relevant script using the following steps:

1. Open a terminal.
2. Navigate to the <PRODUCT_HOME>/bin directory. The scripts for executing this tool are stored in this folder.
3. Execute the relevant script:

     In a Unix system:  sh jartobundle.sh [source jar file/source directory containing jar files] [destination directory] 

     Windows platform: jartobundle.bat [source jar file/source directory containing jar files] [destination directory]

Restrictions: Note that the required file permissions are considered when reading source JARs and the destination directory.

# Dropins Support for OSGi Bundles

The Dropins capability allows you to apply new OSGi bundles by simply adding them to the <CARBON_HOME>/osgi/dropins directory. These bundles will be automatically fetched by the server launcher and executed during server startup. Carbon maintains a bundles.info file for every profile in the server and it contains information about all the bundles that exist in the dropins directory. This file is stored in the <CARBON_HOME>/osgi/profiles/<Profile_Name>/configuration/org.eclipse.equinox.simpleconfigurator directory.

The server uses the information in the bundles.info file of the relevant profile to install and start the OSGi bundles from the dropins directory. Therefore, when you add new OSGi bundles to the dropins directory, the bundles.info file (relevant to the profile) should be updated. There are two ways of updating this file as follows:

## Update the bundles.info file during server launch

The bundle.info file will be automatically updated during server startup if you have the required configurations in the server launcher. That is, you need to have the Dropins capability enabled as a Carbon startup event listener (implementation of the org.wso2.carbon.launcher.CarbonServerListener Java interface) in the launch.properties file (stored in the <CARBON_HOME>/osgi/conf directory). When the server starts up, the dropins capability listens to a CarbonServerEvent of type STARTING and is thereby executed during server launch.

## Update the bundles.info file manually using tool

You can update the bundles.info file with the latest bundle information from the dropins directory at any given time as explained below. This means, you can decouple the function of updating the bundles.info file from the server startup process. Note that this will improve the server startup speed.

Read more about Carbon tools and the instructions for developing new tools from here. 

To manually update the bundles.info file:

1. Open a command prompt and navigate to the <CARBON_HOME>/bin directory.
2. Run the dropins tool by executing the following script:

      *On Unix:* sh dropins.sh [Carbon_Profile]
      
      *On Windows:* dropins.bat [Carbon_Profile]
      
Note that Carbon_Profile should be replaced with the name of the required Carbon profile. The bundles.info file of this profile will be updated as a result. You can use the ‘ALL’ keyword for the Carbon_Profile if you want to update the bundles.info files of all Carbon profiles. For example, use sh dropins.sh ALL.

# Developing a Carbon Tool

See the topics given below for information on Carbon tools.

## About Carbon tools

Carbon tools provide you the option of using various features as standalone functionalities that are detached from the server startup process. That is, you will be able to use these functions easily by executing a simple tool. These tools can be executed at any point, irrespective of whether the server is started.  

Carbon tools provide the following benefits:

 Improve the server startup speed by detaching the function from the server startup process.
 
 Allows you to execute the tools as standalone functions without running the Carbon server.
 
 Allows you to extend Carbon tools easily by introducing custom tool(s) in addition to the tools available by default.

Given below are the optional tools that are available by default with the Carbon Kernel distribution. You can find more details on how to use these tools by following the given links.

 Java Archive (JAR) file to OSGi bundle converter

 Dropins deployer tool
 
## Developing a Carbon tool

The Java Archive (JAR) file that contains these tool implementations is org.wso2.carbon.tools*.jar. This JAR is stored in the <CARBON_HOME>/bin/bootstrap/tools directory. 

Given below are the steps for developing a sample custom tool.

1. You must implement the org.wso2.carbon.tools.CarbonTool Java interface in order to develop your custom tool. Given below is an example that implements a tool named CustomTool.

		public class CustomTool implements CarbonTool {
  
   		/**
   		 * Executes the WSO2 Custom Tool based on the specified arguments.
    		*
    		* @param toolArgs the arguments required for the tool execution
    		*/
   		@Override
   		public void execute(String... toolArgs) {
		// the tool execution implementation goes here
    		}
		}
