# Converting JARs to OSGi Bundles
> The process of converting JARs to OSGi bundles in a WSO2 product is explained below. For the full list of capabilities available in this kernel version, see the **features** section in the [root README.md file](../../README.md#key-features-and-tools). 

Java archive (JAR) to OSGi bundle converter is a standalone tool that can be used for products that are based on Carbon 5 (WSO2 Carbon 5 platform). The implementation of this tool has been added to the freshly introduced Tools module (Maven module) of the WSO2 Carbon 5 platform.

> Read more about Carbon tools and the instructions for developing new tools from [here](../KernelFeatures/DevelopingaCarbonTool.md). 

The need for this functionality arose due to the availability of third-party Java archive (JAR) files, which may not have their corresponding OSGi bundle implementation. In order to deploy them in the Carbon platform, these Java archive files need to be in their corresponding OSGi bundle form. The primary purpose of this tool is to convert JAR files to their corresponding OSGi bundle form. 
Unlike a JAR file, an OSGi bundle is simply not a single application packaged into a single JAR file, but utilizes a modular approach in forming an application. An OSGi bundle contains class files and resource files similar to a JAR file. In addition, it must contain extra metadata, which are absent in a JAR file. These metadata are primarily used to declare the contained packages that are externally visible from the bundle and also to declare the external packages on which the bundle depends. Hence, within the implementation, we primarily generate the following files, which are copied along with the original JAR file to the OSGi bundle.

* **A `MANIFEST.MF` file:** This file holds the metadata generated using the original JAR file.
* **A `p2.inf` file:** This file holds simple instructions to run the bundle(s) by default in the OSGi framework.

The folder structure within the OSGi bundle is as follows:

* **META-INF**
* **MANIFEST.MF**
* **p2.inf**
* **original_jar_file.jar**

> This functionality was previously implemented (for example in wso2/carbon-kernel master 4.5.0) as part of the `https://github.com/wso2/carbon-kernel/tree/4.5.x/core/org.wso2.carbon.server` module and was executed during server startup. However, with the increasing need to improve the server startup time, we have decided to separate this functionality from the `https://github.com/wso2/carbon-kernel/tree/4.5.x/core/org.wso2.carbon.server` module and integrate it to the WSO2 Carbon 5 platform as a separate tool under the freshly introduced `https://github.com/wso2/carbon-kernel/tree/master/tools` module.

## To convert JARs to OSGi bundles:

The 'Jar to Bundle Converter' tool that is shipped with Carbon Kernel can be executed to convert the desired Java archive (.jar) file(s) to their corresponding OSGi bundle form, at a desired destination in the file system. You can execute the relevant script using the following steps:

1. Open a terminal.
2. Navigate to the `<PRODUCT_HOME>/bin` directory. The scripts for executing this tool are stored in this folder.
3. Execute the relevant script:

  * In a Unix system:  `sh jartobundle.sh` [source jar file/source directory containing jar files] [destination directory] 
  * In the Windows platform: `jartobundle.bat` [source jar file/source directory containing jar files] [destination directory]

> Restrictions: Note that the required file permissions are considered when reading source JARs and the destination directory.
