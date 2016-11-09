
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

