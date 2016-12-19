# Developing a Carbon Tool
> The process of developing tools for a Carbon product is explained below. For the full list of capabilities available in this kernel version, see the **features** section in the [root README.md file](../../README.md#key-features-and-tools). 

See the topics given below for information on Carbon tools.

* **[About Carbon Tools](#about-carbon-tools)**
* **[Creating a Carbon tool](#creating-a-carbon-tool)**

## About Carbon Tools

Carbon tools provide you the option of using various features as standalone functionalities that are detached from the server startup process. That is, you will be able to use these functions easily by executing a simple tool. These tools can be executed at any point, irrespective of whether the server is started.  

Carbon tools provide the following benefits:

* Improve the server startup speed by detaching the function from the server startup process. 
* Allows you to execute the tools as standalone functions without running the Carbon server. 
* Allows you to extend Carbon tools easily by introducing custom tool(s) in addition to the tools available by default.

Given below are the optional tools that are available by default with the Carbon Kernel distribution. You can find more details on how to use these tools by following the given links.

* [Java Archive (JAR) file to OSGi bundle converter](../DeveloperTools/ConvertingJARsToOSGiBundles.md)
* [Dropins deployer tool](DroppingOSGiBundlesintoaCarbonServer.md)
 
## Creating a Carbon Tool

> The Java Archive (JAR) file that contains these tool implementations is `org.wso2.carbon.tools*.jar`. This JAR is stored in the `<CARBON_HOME>/bin/bootstrap/tools` directory. 

Given below are the steps for developing a sample custom tool.

1. You must implement the [`org.wso2.carbon.tools.CarbonToo`](https://github.com/wso2/carbon-kernel/blob/master/tools/tools-core/src/main/java/org/wso2/carbon/tools/CarbonTool.java) Java interface in order to develop your custom tool. Given below is an example that implements a tool named `CustomTool`.

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

2. To make sure that the custom tool is executable, you need to initialize it within the executeTool(String toolIdentifier, String... toolArgs) method of the [`org.wso2.carbon.tools.CarbonToolExecutor`](https://github.com/wso2/carbon-kernel/blob/master/tools/tools-core/src/main/java/org/wso2/carbon/tools/CarbonToolExecutor.java) Java class. The tool identifier in this example is `custom-tool`, which is indicated using the case property.

		case “custom-tool”:
		carbonTool = new CustomTool();
		break;
		
3. Create a shell script or a batch file that executes the `org.wso2.carbon.tools*.jar` file that is stored in the `<CARBON_HOME>/bin/bootstrap/tools` directory. Make sure that the `wso2.carbon.tool` system property is set to the `custom-tool` identifier (which corresponds to the case value you specified in step 2).

		-Dwso2.carbon.tool="custom-tool"
