# Working with tools in Carbon

> This README.md file explains the tools in Carbon 5.x.x that can be used for working with OSGi bundles. For the list of capabilities available in Carbon Kernel 5.1.0, see the [root README.md file](https://github.com/nilminiwso2/carbon-kernel-1/blob/master/README.md).

See the following topics for details on tools:

* **[Converting JARs to OSGi Bundles](#converting-jars-to-osgi-bundles)**
* **[Dropins Support for OSGi Bundles](#dropins-support-for-osgi-bundles)**

See the following topics on Carbon plugin.

* **[Using the Carbon Feature Plugin](#using-the-carbon-feature-plugin)**
* **[Using the Maven Bundle Plugin](#using-the-maven-bundle-plugin)**

## Converting JARs to OSGi Bundles

Java archive (JAR) to OSGi bundle converter is a standalone tool that can be used for products based on Carbon Kernel 5.x.x versions (WSO2 Carbon 5.x.x platform). The implementation of this tool has been added to the freshly introduced tools Maven module of the WSO2 Carbon 5.x.x platform.

> Read more about Carbon tools and the instructions for developing new tools from here. 

The need for this functionality arose due to the availability of third-party Java archive (JAR) files, which may not have their corresponding OSGi bundle implementation. In order to deploy them in the Carbon platform, these Java archive files need to be in their corresponding OSGi bundle form. The primary purpose of this tool is to convert JAR files to their corresponding OSGi bundle form. 
Unlike a JAR file, an OSGi bundle is simply not a single application packaged into a single JAR file, but utilizes a modular approach in forming an application. An OSGi bundle contains class files, resource files similar to a JAR file. In addition, it must contain extra metadata, which are absent in a JAR file. These metadata are primarily used to declare the contained packages that are externally visible from the bundle and also to declare the external packages on which the bundle depends. Hence, within the implementation, we primarily generate the following files, which are copied along with the original JAR file to the OSGi bundle.

* **A `MANIFEST.MF` file:** This file holds the metadata generated using the original JAR file.
* **A `p2.inf` file:** This file holds simple instructions to run the bundle(s) by default in the OSGi framework.

The folder structure within the OSGi bundle is as follows:

* **META-INF**
* **MANIFEST.MF**
* **p2.inf**
* **original_jar_file.jar**

> This functionality was previously implemented (for example in wso2/carbon-kernel master 4.5.0) as part of the `https://github.com/wso2/carbon-kernel/tree/4.5.x/core/org.wso2.carbon.server` module and was executed during server startup. However, with the increasing need to improve the server startup time, we have decided to separate this functionality from the `https://github.com/wso2/carbon-kernel/tree/4.5.x/core/org.wso2.carbon.server` module and integrate it to the WSO2 Carbon 5.x.x platform as a separate tool under the freshly introduced `https://github.com/wso2/carbon-kernel/tree/master/tools` module.

### To convert JARs to OSGi bundles:

The 'Jar to Bundle Converter' tool that is shipped with Carbon Kernel can be executed to convert the desired Java archive (.jar) file(s) to their corresponding OSGi bundle form, at a desired destination in the file system. You can execute the relevant script using the following steps:

1. Open a terminal.
2. Navigate to the `<PRODUCT_HOME>/bin` directory. The scripts for executing this tool are stored in this folder.
3. Execute the relevant script:

  * In a Unix system:  `sh jartobundle.sh` [source jar file/source directory containing jar files] [destination directory] 
  * Windows platform: `jartobundle.bat` [source jar file/source directory containing jar files] [destination directory]

> Restrictions: Note that the required file permissions are considered when reading source JARs and the destination directory.

## Dropins Support for OSGi Bundles

The Dropins capability allows you to apply new OSGi bundles by simply adding them to the `<CARBON_HOME>/osgi/dropins` directory. These bundles will be automatically fetched by the server launcher and executed during server startup. Carbon maintains a `bundles.info` file for every profile in the server and it contains information about all the bundles that exist in the dropins directory. This file is stored in the `<CARBON_HOME>/osgi/profiles/<Profile_Name>/configuration/org.eclipse.equinox.simpleconfigurator` directory.

The server uses the information in the `bundles.info` file of the relevant profile to install and start the OSGi bundles from the dropins directory. Therefore, when you add new OSGi bundles to the dropins directory, the `bundles.info` file (relevant to the profile) should be updated. There are two ways of updating this file as follows:

### Update the `bundles.info` file during server launch

The `bundle.info` file will be automatically updated during server startup if you have the required configurations in the server launcher. That is, you need to have the Dropins capability enabled as a Carbon startup event listener (implementation of the `org.wso2.carbon.launcher.CarbonServerListener` Java interface) in the `launch.properties` file (stored in the `<CARBON_HOME>/osgi/conf` directory). When the server starts up, the dropins capability listens to a `CarbonServerEvent` of type `STARTING` and is thereby executed during server launch.

### Update the `bundles.info` file manually using tool

You can update the `bundles.info` file with the latest bundle information from the dropins directory at any given time as explained below. This means, you can decouple the function of updating the `bundles.info` file from the server startup process. Note that this will improve the server startup speed.

> Read more about Carbon tools and the instructions for developing new tools from here. 

To manually update the `bundles.info` file:

1. Open a command prompt and navigate to the `<CARBON_HOME>/bin` directory.
2. Run the dropins tool by executing the following script:

     * *On Unix:* `sh dropins.sh [Carbon_Profile]`
      
     * *On Windows:* `dropins.bat [Carbon_Profile]`
      
   > Note that `Carbon_Profile` should be replaced with the name of the required Carbon profile. The `bundles.info` file of this profile will be updated as a result. You can use the ‘ALL’ keyword for the `Carbon_Profile` if you want to update the `bundles.info` files of all Carbon profiles. For example, use `sh dropins.sh ALL`.

## Using the Carbon Feature Plugin

The Carbon feature plugin was formerly known as the Carbon P2 plugin. It has gone through several development cycles in the previous WSO2 Carbon versions. However, the plugin introduced from WSO2 Carbon 5.0.0 (Carbon 5.x.x platform) is redesigned to update the Maven APIs, to redefine the configurations by removing any existing inconsistencies, to make its architecture solid, and also to improve the understandability and maintainability of its coding standards.

### Introduction

The WSO2 Carbon feature plugin is a Maven plugin, which is used within the WSO2 Carbon platform. The Maven goals that are achieved through this plugin are explained in the following table:

* `generate`: For generating Carbon features. Formerly known as `P2-feature-gen`.
* `generate-repo`: For generating P2 repositories. Formerly known as 'P2-repo-gen'.
* `publish-product`: For publishing a product into a P2 repository.
* `generate-profile`: For generating product profiles. Formerly known as `materialize-product`.
* `install`: For installing Carbon features into a product profile. Formerly known as `p2-profile-gen`.
* `uninstall`: For uninstalling Carbon features from a product.

All these goals (except the generate Maven goal) are executed during the package phase in the default life cycle of the Maven build. The generate Maven goal follows a different life cycle. You have the flexibility to configure the behavior of the plugin by passing the relevant parameters to these Maven goals.

### What is a Carbon feature?

A Carbon feature is an installable form of one or more logically related Carbon components. You can create a Carbon feature by grouping one or more existing Carbon features together as well. You can install these features into Carbon-based products using the feature manager in the management console of the product, or via the install Maven goal of the Carbon feature plugin.

### What is a P2 repository?

A P2 repository is a collection of Carbon features. It acts as a feature aggregator. You can point a Carbon product to a P2 repository and install one or more feature(s) you find in that repository. Once a feature is installed, the feature is copied into the Carbon product.

### What is a product profile?

A product profile is a logical grouping of a set of features/components that creates a virtual boundary for execution. Every Carbon product has a default profile shipped with it. With the Carbon feature plugin, you can create profiles and install Carbon features to that profile.

A Carbon product (WSO2 ESB, WSO2 AS etc.) is made by installing the relevant features on top of the Carbon kernel using the following steps:

1. Create profiles on top of the Carbon kernel using the generate-profile Maven goal. 
2. Install the relevant features using the install Maven goal.

### Carbon feature plugin configurations

The following sections describe the configurations of the Maven goals that are implemented in the Carbon feature plugin. 

#### Configuring the generate Maven goal

A sample pom.xml file configuration of the generate Maven goal is shown below.

    <build>
    	<plugins>
        	<plugin>
            	<groupId>org.wso2.carbon.maven</groupId>
            	<artifactId>carbon-feature-plugin</artifactId>
            	<version>${carbon.feature.plugin.version}</version>
            	<executions>
                	<execution>
                    	<id>p2-feature-generation</id>
                    	<phase>package</phase>
                    	<goals>
                        	<goal>generate</goal>
                    	</goals>
                    	<configuration>
                        	//plugin configuration goes here.
                    	</configuration>
                	</execution>
            	</executions>
        	</plugin>
     </plugins>
    </build>

You can modify the above file to add the configurations of the plugin by adding the following parameters within the <configuration> element of it. 

* `label`: Label to attach to the `feature.xml` file. Default value is set to the Maven project name of the feature being built. NOT Mandatory. Example: `<label>LABEL</label>`
* `description`: Description to attach to the `feature.xml` file. Default value is set to the Maven project description of the feature being built. NOT Mandatory. Example: `<description>DESCRIPTION</description>`.
* `providerName`: Provider name to attached to the `feature.xml` file. NOT Mandatory. Example: `<providerName>PROVIDER_NAME</providerName>`
* `copyright`: Copyright text to attached to the `feature.xml` file. NOT Mandatory. Example: `<copyright>COPYRIGHT</copyright>`.
* `licenceUrl`: License URL to attach to the `feature.xml` file. NOT Mandatory. Example: `<licenseUrl>LICENSE_URL</licenseUrl>`.
* `licence`: License to attach to the `feature.xml` file. NOT Mandatory. Example: `<license>LICENSE</license>`.
* `manifest`: This points to the location of the manifest file. The content is considered if the manifest is present when generating the `feature.xml` file. This manifest file is copied and its content will be updated as the `feature.xml` file. NOT Mandatory. Example: `<manifest>LOCATION_OF_THE_MANIFEST_FILE</manifest>`.
* `propertyFile`: This points to the location of the properties file. Properties of the property file (if exists) are merged with the properties passed into the Maven goal through the `<properties>` parameter and with the `feature.properties` file located in the resources/ folder (if exists) of the Maven project. These merged properties are written into the `feature.properties` file. 

 > You can provide two optional property files. Define one through this `<propertyFile>` parameter. The plugin expects the other file to reside in the resources/ directory of the Maven project with the name as `feature.properties`. You need at least one file to exist for the tool to function properly. Once properties in these two files are merged, the plugin checks for mandatory keys (i.e. copyright, license keys). If these mandatory fields are not found, the plugin informs you and terminates the execution.

 NOT Mandatory. Example: `<propertyFile>PATH_FOR_THE_PROPERTY_FILE</propertyFile>`.
* `properties`: A collection of key-value pairs passed into the Maven goal to write into the output `feature.properties` file. 

 > These properties are merged with the properties taken from the properties file which you pass into the goal through the <propertyFile> parameter, and with the properties of the feature.properties file (if exists) which resides in the resources/ folder of the Maven project.
 
 MANDATORY property. 
 Example:
 
       <properties>
	       <property>
		     <name>name1</name
	     	     <value>value1</value>
               </property>
	    <property>
	     	     <name>name2</name
		     <value>value2</value>
              </property>
        </properties>

* `adviceFileContents`: Content to write into the output `p2.inf` file are fed into the generate Maven goal through this property. 

 MANDATORY property. 
 Example:

          <adviceFileContent>
	     <advice>
	        <name>org.eclipse.equinox.p2.type.group</name>
	        <value>true</value>
             </advice>	
          </adviceFile>

* `bundles`: OSGI bundles including features in them. Specify the artifacts representing each bundle as a Maven dependency. 

 NOT MANDATORY property. 
 Example:

          <bundles>
	      <bundle>   					     		                  
                  <symbolicName>org.wso2.carbon.student.mgt.stub</symbolicName>
		  <version>4.2.0</version>
	     </bundle>
         </bundles>
	
	
	 > * The `<symbolicName>` element is mandatory. You can find the value of it in the MANIFEST.MF file of the OSGI bundle.
	 
	 > * The `<version>` element is also mandatory. The value of it should be one of the following:
	  1. The bundle version given in the MANIFEST.MF file of it.  
	  2. The Maven artifact version provided that it is convertible to the bundle version. (E.g. If you use the Maven artifact version 5.0.0-SNAPSHOT when the bundle version is 5.0.0.SNAPSHOT, then the plugin will convert the Maven artifact version to the bundle version and continue.)

* `includeFeatures`: Carbon features which you need to include in the feature being built. 

 NOT MANDATORY property. 
 Example:

          <includeFeature>
	     <feature>
	        <id>org.wso2.carbon.jarservices.feature</id>
                <version>4.4.0</version>
             </feature>            
          </includeFeature>
	
	
	 > For any artifact which represents a Carbon feature, the artifact ID of it should end as `.feature`. Also, you need to specify the artifact representing each feature as a Maven dependency.
	 
* `importFeatures`: Features on which the feature being built depends on. These are included in the feature.xml file. However, the Maven artifacts for these features are not getting resolved and they are not copied into the final feature zip file. 

 NOT MANDATORY property. 
 Example:

          <importFeatures>                            		
              <feature>
	        <id>org.wso2.carbon.jarservices.feature</id>
                <version>4.4.0</version>
              </feature>         
          </importFeatures>	
	
	 > For any artifact which represents a Carbon feature, the artifact ID of it should end as .feature. Also, you need to specify the artifact representing each feature as a Maven dependency. 

#### Configuring the generate-repo Maven goal

A sample `pom.xml` file configuration of the `generate-repo` Maven goal is shown below.

      <build>
    	     <plugins>
        	<plugin>
            	<groupId>org.wso2.carbon.maven</groupId>
            	<artifactId>carbon-feature-plugin</artifactId>
            	<version>${carbon.feature.plugin.version}</version>
            	<executions>
                	<execution>
                    	<id>p2-feature-generation</id>
                    	<phase>package</phase>
                    	<goals>
                        	<goal>generate-repo</goal>
                    	</goals>
                    	<configuration>
                        	//plugin configuration goes here.
                    	</configuration>
                	</execution>
            	</executions>
        	</plugin>
              </plugins>
       </build>

You can modify the above file to add the configurations of the plugin by adding the following parameters within the `<configuration>` element of it. 

* `name`: Name of the newly created artifact repository. If you do not specify this, the artifact ID of the project is taken by default. 

 MANDATORY property. 
 Example: `<name>LOCATION_OF_THE_MANIFEST_FILE</name>`	
 
* `targetRepository`: Location to create the repository in the form of a URL. 

 MANDATORY property.
 Example: `<targetRepository>file:/home/p2-repo</targetRepository>`
 
 > Since this is a URL, give a location in the file system in this format.
 
* `features`: The set of features to include in the repository. The Maven goal searches for the given artifact in the local `.m2` repository. If it is not found, then the goal searches for the configured remote repositories. Not finding the artifact may cause the maven goal to terminate with a build failure.

 > Specify artifacts representing each feature as a Maven dependency. The <type> attribute of each dependency should hold the value as zip.
 
 MANDATORY property.
 Example:
 
       <features>
           <feature>
	      <id>org.wso2.carbon.student.mgt.feature</id>
              <version>4.2.0</version>
           </feature>
       <features>

* `bundles`: The set of bundles to include into the repository. The Maven goal searches for the given artifact in the local `.m2` repository. If it is not found, then the goal searches for the configured remote repositories. Not finding the artifact may cause the Maven goal to terminate with a build failure.

 > Specify artifacts representing each bundle as a Maven dependency. 
 
 MANDATORY property.
 Example:
 
      <bundles>
          <bundle>
	       <symbolicName>org.wso2.carbon.student.mgt.stub</symbolicName>
               <version>4.2.0</version>
          </bundle>
      <bundles>
      
* `archive`: Specifies whether the generated artifact should be archived or not. This is a boolean value. Thus, permitted values are either `true` or `false`. The default value is `false`. 

 NOT MANDATORY property.
 Example: `<archive>True</archive>`

#### Configuring the publish-product Maven goal

A sample `pom.xml` file configuration of the `publish-product` Maven goal is shown below. 

      <build>
    	<plugins>
        	<plugin>
            	<groupId>org.wso2.carbon.maven</groupId>
            	<artifactId>carbon-feature-plugin</artifactId>
            	<version>${carbon.feature.plugin.version}</version>
            	<executions>
                	<execution>
                    	<id>p2-feature-generation</id>
                    	<phase>package</phase>
                    	<goals>
                        	      <goal>publish-product</goal>
                    	</goals>
                    	<configuration>
                        	//plugin configuration goes here.
                    	</configuration>
                	</execution>
            	</executions>
        	</plugin>
              </plugins>
         </build>

You can modify the above file to add the configurations of the plugin by adding the following parameters within the <configuration> element of it. 

* `repositoryURL`: Points to the repository where the product needs to be published.

 MANDATORY property.
 Example: `<repositoryURL>file:${basedir}/target/p2-repo</repositoryURL>`
 
* `executable`: Points to the executable file.

 MANDATORY property.
 Example: `<executable>${basedir}/target/org.eclipse.equinox.executable_3.5.0.v20110530-7P7NFUFFLWUl76mart</executable>`
 
* `productConfiguration`: Location of the `.product` file. 

 MANDATORY property.
 Example: `<productConfigurationFile>${basedir}/carbon.product</productConfigurationFile>`.
 
#### Configuring the generate-profile Maven goal 

A sample `pom.xml` file configuration of the `generate-profile` Maven goal is shown below.

      <build>
    	   <plugins>
        	<plugin>
            	<groupId>org.wso2.carbon.maven</groupId>
            	<artifactId>carbon-feature-plugin</artifactId>
            	<version>${carbon.feature.plugin.version}</version>
            	<executions>
                	<execution>
                    	<id>p2-feature-generation</id>
                    	<phase>package</phase>
                    	<goals>
                        	<goal>generate-profile</goal>
                    	</goals>
                    	<configuration>
                        	//plugin configuration goes here.
                    	</configuration>
                	</execution>
            	</executions>
        	</plugin>
           </plugins>
         </build>
	 
You can modify the above file to add the configurations of the plugin by adding the following parameters within the `<configuration>` element of it. 

* `repositoryURL`: Points the P2 repository from which the artifacts are taken from when generating the profile. 
 
 MANDATORY property.
 Example: `<repositoryURL>file://home/p2-repo</repositoryURL>`
 
* `targetPath`: Points to the components folder of the Carbon product of which the profile is being created. 

 MANDATORY property.
 Example: `<targetPath>file:${basedir}/target/wso2carbon-core-${carbon.kernel.version}/repository/components</targetPath>`
 
* `profile`: Name of the profile to be created..

 MANDATORY property.
 Example: `<profile>worker</profile>`
 
* `productConfiguration`: Location of the `.product` file.

 MANDATORY property.
 Example: `<productConfigurationFile>${basedir}/carbon.product</productConfigurationFile>`.

#### Configuring the install Maven goal

A sample `pom.xml` file configuration of the install Maven goal is shown below.

     <build>
    	<plugins>
        	<plugin>
            	<groupId>org.wso2.carbon.maven</groupId>
            	<artifactId>carbon-feature-plugin</artifactId>
            	<version>${carbon.feature.plugin.version}</version>
            	<executions>
                	<execution>
                    	<id>p2-feature-generation</id>
                    	<phase>package</phase>
                    	<goals>
                        	<goal>install</goal>
                    	</goals>
                    	<configuration>
                        	//plugin configuration goes here.
                    	</configuration>
                	</execution>
            	</executions>
        	</plugin>
            </plugins>
        </build> 

You can modify the above file to add the configurations of the plugin by adding the following parameters within the `<configuration>` element of it. 

* `destination`: Points the `<CARBON_HOME>/repository/components/` directory. 

 MANDATORY property.
 Example: `<destination>/home/Carbon/wso2carbon-4.4.0/repository/components</destination>`
 
* `profile`: The profile which needs to be updated with the new set of features in the destination. 

 MANDATORY property.
 Example: `<profile>default</profile>`
 
* `repositoryURL`: Points the p2 repository from which artifacts are picked from to install. 

 MANDATORY property.
 Example: `<repositoryURL>file://home/p2-repo</repositoryURL>`
 
* `features`: Features to be installed in the destination profile. Features that you add here should exist in the P2 repository to which the `<repositoryURL>` parameter of the plugin points. 

 MANDATORY property.
 Example:
 
       <features>
            <feature>
               <id>org.wso2.carbon.registry.contentsearch.feature.group</id>
               <version>4.4.0</version>
               </feature>
            <feature>
               <id>org.wso2.ciphertool.feature.group</id>
               <version>4.4.0</version>
            </feature>
        </features>
 
 > Add the ID of the feature being installed as the value of the <ID> property, and add the version of the feature being installed as the value of the <version> property.
 
* `deleteOldProfileFiles`: Whether to delete old `*.profile` folders located in the `<CARBON_HOME>/repository/components/p2/org.eclipse.equinox.p2.engine/profileRegistry/` directory. The default value is set to true.

 NOT MANDATORY.
 Example: `<deleteOldProfileFiles>False</deleteOldProfileFiles>`

#### Configuring the uninstall Maven goal

A sample pom.xml file configuration of the uninstall Maven goal is shown below.

      <build>
    	<plugins>
        	<plugin>
            	<groupId>org.wso2.carbon.maven</groupId>
            	<artifactId>carbon-feature-plugin</artifactId>
            	<version>${carbon.feature.plugin.version}</version>
            	<executions>
                	<execution>
                    	<id>p2-feature-generation</id>
                    	<phase>package</phase>
                    	<goals>
                        	<goal>uninstall</goal>
                    	</goals>
                    	<configuration>
                        	//plugin configuration goes here.
                    	</configuration>
                	</execution>
            	</executions>
        	</plugin>
            </plugins>
          </build>
	  
You can modify the above file to add the configurations of the plugin by adding the following parameters within the `<configuration>` element of it. 

* `destination`: Points to the `<CARBON_HOME>/repository/components/` directory.

 MANDATORY property.
 Example: `<destination>/home/Carbon/wso2carbon-4.4.0/repository/components</destination>`
 
* `profile`: Which profile in the destination from where you need to uninstall the features. 

 MANDATORY property.
 Example: `<profile>default</profile>`
 
* `features`: List of features to be uninstalled from the destination profile.

 MANDATORY
 Example:
 
       <features>
             <feature>
                <id>org.wso2.carbon.registry.contentsearch.feature.group</id>
                <version>4.4.0</version>
             </feature>
             <feature>
                <id>org.wso2.ciphertool.feature.group</id>
                <version>4.4.0</version>
             </feature>
        </features>
	
## Using the Maven Bundle Plugin

Carbon 5.0.0 Kernel (Carbon 5.x.x platform) introduced a new version of the Carbon parent project, i.e., carbon-parent-2. This new version includes the Maven Bundle plugin within the Carbon parent project. Therefore, it is no longer necessary to repeat all the configurations relevant to this plugin separately for all other child components. Instead, all the child components that use carbon-parent-2 as the parent project will inherit the common configurations of the plugin from the parent. However, even though the common configurations are inherited by all components, it is necessary to be able to change some configurations for each child component separately. It is possible to do this now, because all the changeable configurations are parameterized in the pom.xml of the carbon-parent-2. Therefore, the child components can freely configure the required parameters.

Shown below is how the Maven Bundle plugin is included in the pom.xml file of carbon-parent-2.

     <plugin>
	<groupId>org.apache.felix</groupId>
		<artifactId>maven-bundle-plugin</artifactId>
		<version>${maven.bundle.plugin.version}</version>
		<extensions>${maven.bundle.plugin.extensions}</extensions>
		<configuration>
			<obrRepository>NONE</obrRepository>
			<instructions>
				<Bundle-Activator>${bundle.activator}</Bundle-Activator>
				<Bundle-ActivationPolicy>${bundle.activation.policy}</Bundle-ActivationPolicy>
				<Bundle-ClassPath>${bundle.classpath}</Bundle-ClassPath>
				<Bundle-Contributors>${bundle.contributors}</Bundle-Contributors>
				<Bundle-Copyright>WSO2 Inc</Bundle-Copyright>
				<Bundle-Description>${project.description}</Bundle-Description>
				<Bundle-Developers>${bundle.developers}</Bundle-Developers>
				<Bundle-DocURL>${bundle.docurl}</Bundle-DocURL>
				<Bundle-License>http://www.apache.org/licenses/LICENSE-2.0.txt</Bundle-License>
				<Bundle-Name>${bundle.name}</Bundle-Name>
				<Bundle-SymbolicName>${project.artifactId}</Bundle-SymbolicName>
				<Bundle-Vendor>WSO2 Inc</Bundle-Vendor>
				<Conditional-Package>${conditional.package}</Conditional-Package>
				<DynamicImport-Package>${dynamic.import.package}</DynamicImport-Package>
				<Export-Package>${export.package}</Export-Package>
				<Fragment-Host>${fragment.host}</Fragment-Host>
				<Import-Package>${import.package}</Import-Package>
				<Include-Resource>${include.resource}</Include-Resource>
				<Meta-Persistence>${meta.persistence}</Meta-Persistence>
				<Private-Package>${private.package}</Private-Package>
				<Provide-Capability>${provide.capability}</Provide-Capability>
				<Require-Bundle>${require.bundle}</Require-Bundle>
				<Require-Capability>${require.capability}</Require-Capability>
				<Service-Component>${service.component}</Service-Component>
				<Microservices>${microservices}</Microservices>
				<_dsannotations>${dsannotations}</_dsannotations>
			</instructions>
	            </configuration>
            </plugin>

Following is the list of parameters you can use inside a child POM in order to override the configurations inherited from carbon-parent-2.

| Configuration       | Parameter          | Default value  |
| :-----------: |:-------------:| :----:|
| extensions     | maven.bundle.plugin.extensions | true |
| Bundle-Activator      | bundle.activator     |   - |
| Bundle-ActivationPolicy | bundle.activation.policy      |    The only policy defined is the lazy activation policy. If no Bundle-ActivationPolicy header is speci- fied, the bundle will use eager activation. |
| Bundle-ClassPath    | bundle.classpath | - |
| Bundle-Description      | bundle.description     |   - |
| Bundle-Developers    | bundle.developers | WSO2 Inc |
| Bundle-Contributors      | bundle.contributors     |   WSO2 Inc |
| Bundle-DocURL    | bundle.docurl | https://docs.wso2.com |
| Bundle-Name      | bundle.name     |   project.artifactId |
| Conditional-Package    | conditional.package | - |
| DynamicImport-Package     | dynamic.import.package     |   - |
| Export-Package    | export.package | - |
| Fragment-Host      | fragment.host     |   - |
| Import-Package    | import.package | - |
| Include-Resource      | include.resource     |   {maven-resources} |
| Meta-Persistence    | meta.persistence | - |
| Private-Package     | private.package    |   - |
| Provide-Capability   | provide.capability | - |
| Require-Bundle     | require.bundle     |   - |
| Require-Capability    | require.capability | - |
| Service-Component     | service.component    |   - |
| Microservices   | microservices | - |
| _dsannotations     | dsannotations     |   - |

Shown below is a sample `pom.xml` file of a Carbon component, which has changed the default configurations inherited from the parent by using the parameters given above. 

      <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
            .........................
                 <!-- This is where you have to include your configuration values for maven-bundle-plugin -->
                      <properties>
	                  <bundle.activator>org.wso2.carbon.kernel.internal.CarbonCoreBundleActivator</bundle.activator>
                          <private.package>org.wso2.carbon.kernel.internal.*,</private.package>
                          <export.package>
                              !org.wso2.carbon.kernel.internal,
                              org.wso2.carbon.kernel.*; version="${carbon.kernel.package.export.version}",
                          </export.package>
                          <import.package>
                              org.eclipse.osgi.util,
                              org.slf4j.*;version="${slf4j.logging.package.import.version.range}",
                              org.osgi.framework.*;version="${osgi.framework.package.import.version.range}",
                              org.eclipse.osgi.framework.console;version="${osgi.framework.console.package.import.version.range}",
                              javax.xml.bind.*;version="${osgi.framework.javax.xml.bind.package.import.version.range}",
                              org.osgi.service.cm.*; version="${osgi.services.cm.package.import.version.range}",
                              org.osgi.service.*;version="${equinox.osgi.services.package.import.version.range}",
                              org.osgi.util.tracker; version="${osgi.service.tracker.package.import.version.range}",
                          </import.package>
                         </properties>
       </project> 
