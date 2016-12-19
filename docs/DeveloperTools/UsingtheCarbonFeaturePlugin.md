# Using the Carbon Feature Plugin
> The usage of the Carbon Feature Plugin for development in a WSO2 product is explained below. For the full list of capabilities available in this kernel version, see the **features** section in the [root README.md file](../../README.md#key-features-and-tools). 

The Carbon feature plugin was formerly known as the Carbon P2 plugin. It has gone through several development cycles in the previous WSO2 Carbon versions. However, the plugin introduced from WSO2 Carbon 5 is redesigned to update the Maven APIs, to redefine the configurations by removing any existing inconsistencies, to make its architecture solid, and also to improve the understandability and maintainability of its coding standards.

## Introduction

The WSO2 Carbon feature plugin is a Maven plugin, which is used within the WSO2 Carbon platform. The Maven goals that are achieved through this plugin are explained below:

* `generate`: For generating Carbon features. Formerly known as `P2-feature-gen`. See the instructions on [configuring the `generate` Maven goal](#configuring-the-generate-maven-goal). 
* `generate-repo`: For generating P2 repositories. Formerly known as 'P2-repo-gen'. See the instructions on [configuring the `generate-repo` Maven goal](#configuring-the-generate-repo-maven-goal).
* `publish-product`: For publishing a product into a P2 repository. See the instructions on [configuring the `publish-product` Maven goal](#configuring-the-publish-product-maven-goal).
* `generate-profile`: For generating product profiles. Formerly known as `materialize-product`. See the instructions on [configuring the `generate-profile` Maven goal](#configuring-the-generate-profile-maven-goal).
* `install`: For installing Carbon features into a product profile. Formerly known as `p2-profile-gen`. See the instructions on [configuring the install Maven goal](#configuring-the-install-maven-goal).
* `uninstall`: For uninstalling Carbon features from a product. See the instructions on [configuring the uninstall Maven goal](#configuring-the-uninstall-maven-goal).

All these goals (except the generate Maven goal) are executed during the package phase in the default life cycle of the Maven build. The generate Maven goal follows a different life cycle. You have the flexibility to configure the behavior of the plugin by passing the relevant parameters to these Maven goals.

## What is a Carbon feature?

A Carbon feature is an installable form of one or more logically related Carbon components. You can create a Carbon feature by grouping one or more existing Carbon features together as well. You can install these features into Carbon-based products using the feature manager in the management console of the product, or via the [`install` Maven goal](#configuring-the-install-maven-goal) of the Carbon feature plugin.

## What is a P2 repository?

A P2 repository is a collection of Carbon features. It acts as a feature aggregator. You can point a Carbon product to a P2 repository and install one or more feature(s) you find in that repository. Once a feature is installed, the feature is copied into the Carbon product.

## What is a product profile?

A product profile is a logical grouping of a set of features/components that creates a virtual boundary for execution. Every Carbon product has a default profile shipped with it. With the Carbon feature plugin, you can create profiles and install Carbon features to that profile.

A Carbon product (WSO2 ESB, WSO2 AS etc.) is made by installing the relevant features on top of the Carbon kernel using the following steps:

1. Create profiles on top of the Carbon kernel using the [generate-profile Maven goal](#configuring-the-generate-profile-maven-goal). 
2. Install the relevant features using the [`install` Maven goal](#configuring-the-install-maven-goal).

## Carbon feature plugin configurations

The following sections describe the configurations of the Maven goals that are implemented in the Carbon feature plugin. 

### Configuring the generate Maven goal

A sample pom.xml file configuration of the `generate` Maven goal is shown below.

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

You add the configurations of the plugin to the above by adding the following parameters within the `<configuration>` element of it. 

* `label`: Label to attach to the `feature.xml` file. Default value is set to the Maven project name of the feature being built. NOT Mandatory. Example: `<label>LABEL</label>`
* `description`: Description to attach to the `feature.xml` file. Default value is set to the Maven project description of the feature being built. NOT Mandatory. Example: `<description>DESCRIPTION</description>`.
* `providerName`: Provider name to attach to the `feature.xml` file. NOT Mandatory. Example: `<providerName>PROVIDER_NAME</providerName>`
* `copyright`: Copyright text to attach to the `feature.xml` file. NOT Mandatory. Example: `<copyright>COPYRIGHT</copyright>`.
* `licenceUrl`: License URL to attach to the `feature.xml` file. NOT Mandatory. Example: `<licenseUrl>LICENSE_URL</licenseUrl>`.
* `licence`: License to attach to the `feature.xml` file. NOT Mandatory. Example: `<license>LICENSE</license>`.
* `manifest`: This points to the location of the manifest file. The content is considered if the manifest is present when generating the `feature.xml` file. This manifest file is copied and its content will be updated as the `feature.xml` file. NOT Mandatory. Example: `<manifest>LOCATION_OF_THE_MANIFEST_FILE</manifest>`.
* `propertyFile`: This points to the location of the properties file. Properties of the property file (if exists) are merged with the properties passed into the Maven goal through the `<properties>` parameter and with the `feature.properties` file located in the resources/ folder (if exists) of the Maven project. These merged properties are written into the `feature.properties` file. 

 > You can provide two optional property files. Define one through this `<propertyFile>` parameter. The plugin expects the other file to reside in the resources/ directory of the Maven project with the name as `feature.properties`. You need at least one file to exist for the tool to function properly. Once properties in these two files are merged, the plugin checks for mandatory keys (i.e. copyright, license keys). If these mandatory fields are not found, the plugin informs you and terminates the execution.

 NOT Mandatory. Example: `<propertyFile>PATH_FOR_THE_PROPERTY_FILE</propertyFile>`.
* `properties`: A collection of key-value pairs passed into the Maven goal to write into the output `feature.properties` file. 

 > These properties are merged with the properties taken from the properties file that you pass into the goal through the `<propertyFile>` parameter, and with the properties of the `feature.properties` file (if exists), which resides in the resources/ folder of the Maven project.
 
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

* `adviceFileContents`: Content that should be written into the output `p2.inf` file are fed into the generate Maven goal through this property. 

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
	  1. The bundle version given in the MANIFEST.MF file.  
	  2. The Maven artifact version, provided that it is convertible to the bundle version. (E.g. If you use the Maven artifact version 5.0.0-SNAPSHOT when the bundle version is 5.0.0.SNAPSHOT, then the plugin will convert the Maven artifact version to the bundle version and continue.)

* `includeFeatures`: The Carbon features that you need to include in the feature being built. 

 NOT MANDATORY property. 
 Example:

          <includeFeature>
	     <feature>
	        <id>org.wso2.carbon.jarservices.feature</id>
                <version>4.4.0</version>
             </feature>            
          </includeFeature>
	
	
	 > For any artifact which represents a Carbon feature, the artifact ID should end as `.feature`. Also, you need to specify the artifact representing each feature as a Maven dependency.
	 
* `importFeatures`: Other features on which the feature being built depends. These are included in the `feature.xml` file. However, the Maven artifacts for these features are not getting resolved and they are not copied into the final feature zip file. 

 NOT MANDATORY property. 
 Example:

          <importFeatures>                            		
              <feature>
	        <id>org.wso2.carbon.jarservices.feature</id>
                <version>4.4.0</version>
              </feature>         
          </importFeatures>	
	
	 > For any artifact that represents a Carbon feature, the artifact ID should end as .feature. Also, you need to specify the artifact representing each feature as a Maven dependency. 

### Configuring the generate-repo Maven goal

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

You can add the configurations of the plugin to the above by adding the following parameters within the `<configuration>` element. 

* `name`: Name of the newly created artifact repository. If you do not specify this, the artifact ID of the project is taken by default. 

 MANDATORY property. 
 Example: `<name>LOCATION_OF_THE_MANIFEST_FILE</name>`.	
 
* `targetRepository`: Location to create the repository in the form of a URL. 

 MANDATORY property.
 Example: `<targetRepository>file:/home/p2-repo</targetRepository>`.
 
 > Since this is a URL, give a location in the file system in this format.
 
* `features`: The set of features to include in the repository. The Maven goal searches for the given artifact in the local `.m2` repository. If it is not found, the goal searches for the configured remote repositories. Not finding the artifact may cause the maven goal to terminate with a build failure.

 > Specify artifacts representing each feature as a Maven dependency. The `<type>` attribute of each dependency should hold the value as a ZIP.
 
 MANDATORY property.
 Example:
 
       <features>
           <feature>
	      <id>org.wso2.carbon.student.mgt.feature</id>
              <version>4.2.0</version>
           </feature>
       <features>

* `bundles`: The set of bundles to include into the repository. The Maven goal searches for the given artifact in the local `.m2` repository. If it is not found, the goal searches for the configured remote repositories. Not finding the artifact may cause the Maven goal to terminate with a build failure.

 > Specify artifacts representing each bundle as a Maven dependency. 
 
 MANDATORY property.
 Example:
 
      <bundles>
          <bundle>
	       <symbolicName>org.wso2.carbon.student.mgt.stub</symbolicName>
               <version>4.2.0</version>
          </bundle>
      <bundles>
      
* `archive`: Specifies whether or not the generated artifact should be archived. This is a boolean value. Thus, permitted values are either `true` or `false`. The default value is `false`. 

 NOT MANDATORY property.
 Example: `<archive>True</archive>`

### Configuring the publish-product Maven goal

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

You can add the configurations of the plugin to the above file by adding the following parameters within the `<configuration>` element of it. 

* `repositoryURL`: Points to the repository where the product needs to be published.

 MANDATORY property.
 Example: `<repositoryURL>file:${basedir}/target/p2-repo</repositoryURL>`.
 
* `executable`: Points to the executable file.

 MANDATORY property.
 Example: `<executable>${basedir}/target/org.eclipse.equinox.executable_3.5.0.v20110530-7P7NFUFFLWUl76mart</executable>`.
 
* `productConfiguration`: Location of the `.product` file. 

 MANDATORY property.
 Example: `<productConfigurationFile>${basedir}/carbon.product</productConfigurationFile>`.
 
### Configuring the generate-profile Maven goal 

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
	 
You can add the configurations of the plugin to the above by adding the following parameters within the `<configuration>` element. 

* `repositoryURL`: Points to the P2 repository from which the artifacts are taken when generating the profile. 
 
 MANDATORY property.
 Example: `<repositoryURL>file://home/p2-repo</repositoryURL>`.
 
* `targetPath`: Points to the components folder of the Carbon product in which the profile is being created. 

 MANDATORY property.
 Example: `<targetPath>file:${basedir}/target/wso2carbon-core-${carbon.kernel.version}/repository/components</targetPath>`
 
* `profile`: Name of the profile to be created.

 MANDATORY property.
 Example: `<profile>worker</profile>`.
 
* `productConfiguration`: Location of the `.product` file.

 MANDATORY property.
 Example: `<productConfigurationFile>${basedir}/carbon.product</productConfigurationFile>`.

### Configuring the install Maven goal

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

You can add the configurations of the plugin to the above by adding the following parameters within the `<configuration>` element. 

* `destination`: Points to the `<CARBON_HOME>/repository/components/` directory. 

 MANDATORY property.
 Example: `<destination>/home/Carbon/wso2carbon-4.4.0/repository/components</destination>`
 
* `profile`: The profile that needs to be updated with the new set of features in the destination. 

 MANDATORY property.
 Example: `<profile>default</profile>`
 
* `repositoryURL`: Points to the p2 repository from which the artifacts are picked for installation. 

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
 
 > Add the ID of the feature being installed as the value of the `<ID>` property, and add the version of the feature being installed as the value of the `<version>` property.
 
* `deleteOldProfileFiles`: Specifies whether to delete old `*.profile` folders located in the `<CARBON_HOME>/repository/components/p2/org.eclipse.equinox.p2.engine/profileRegistry/` directory. The default value is set to true.

 NOT MANDATORY.
 Example: `<deleteOldProfileFiles>False</deleteOldProfileFiles>`.

### Configuring the uninstall Maven goal

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
	  
You can add the configurations of the plugin to the above by adding the following parameters within the `<configuration>` element. 

* `destination`: Points to the `<CARBON_HOME>/repository/components/` directory.

 MANDATORY property.
 Example: `<destination>/home/Carbon/wso2carbon-4.4.0/repository/components</destination>`
 
* `profile`: The profile in the destination from where you need to uninstall the features. 

 MANDATORY property.
 Example: `<profile>default</profile>`.
 
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
