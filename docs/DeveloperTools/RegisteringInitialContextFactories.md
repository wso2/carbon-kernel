# Registering InitialContextFactory classes to OSGi env
> The process of registering InitialContextFactory to OSGi env in a WSO2 product is explained below. For the full list of capabilities available in this kernel version, see the **features** section in the [root README.md file](../../README.md#key-features-and-tools). 

ICF Register is a standalone tool that can be used for products that are based on Carbon 5 (WSO2 Carbon 5 platform). The implementation of this tool has been added to the freshly introduced Tools module (Maven module) of the WSO2 Carbon 5 platform.

> Read more about Carbon tools and the instructions for developing new tools from [here](../KernelFeatures/DevelopingaCarbonTool.md). 

The need for this functionality arose due to the OSGi JNDI specification where when you register an InitialContextFactory you need to register it for both the interface and the implementation. The primary purpose of this tool is register InitialContextFactory classes based on the OSGi JNDI spec. 
From the implementation, we primarily generate a Custom BundleActivator and register the user provided InitialContextFactory implementation of the provided jar, under both interface and the implementation. If the provided file is an Jar file this will wrap that as a Bundle as well. 

## To Run the Tool:

The 'ICF Register' tool that is shipped with Carbon Kernel can be executed to create a bundle which will be responsible of registering the provided ICF based on OSGi JNDI spec.
You can execute the relevant script using the following steps:

1. Open a terminal.
2. Navigate to the `<PRODUCT_HOME>/bin` directory. The scripts for executing this tool are stored in this folder.
3. Execute the relevant script:

  * In a Unix system:  `sh icf-provider.sh` [Full qualified name of ICF impl class name] [path to source jar] [destination] 
  * In the Windows platform: `icf-provider.bat` [Full qualified name of ICF impl class name] [path to source jar] [destination] 

> Restrictions: Note that the required file permissions are considered when reading source JARs and the destination directory.
