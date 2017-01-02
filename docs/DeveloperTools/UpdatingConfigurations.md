# Using the global configuration model

> The process of updating configurations in a Carbon product is explained below. For the full list of capabilities available in this kernel version, see the **features** section in the [root README.md file](../../README.md#key-features-and-tools). 

WSO2 Carbon 5 introduces a new configuration deployment model, which allows products to maintain all the server configurations in one configuration file. This global configuration file is named `deployment.yaml` and is stored in the `<PRODUCT_HOME>/conf` directory of your product pack. The below diagram illustrates the high-level picture of the configuration model. As shown below, the global configuration file (`deployment.yaml`) of the server should be updated with the relevant configs from each component (if you want to change the default configurations in that component).

![screen shot 2016-12-22 at 6 34 00 pm](https://cloud.githubusercontent.com/assets/21237558/21426531/60a7c61a-c875-11e6-8a8d-1a2fff9762ff.png)

By default, the following minimal configuration elements are specified in the `deployment.yaml` file of a Carbon product.

```
 # Carbon Configuration Parameters
wso2.carbon:
   # value to uniquely identify a server
 id: carbon-kernel
   # server name
 name: WSO2 Carbon Kernel
   # ports used by this server
 ports:
     # port offset
   offset: 0
   ```

> Note the following changes introduced in this Kernel version:
 * The new `deployment.yaml` file contains all the Carbon runtime configurations and it replaces the `carbon.yaml` file that existed previously.
 * Three new annotations are introduced (Configuration, Element, Ignore) for configuration bean classes. Read below for details.
 * A new annotation processor is introduced (ConfigurationProcessor) for discovering configuration bean classes in the component.
 * Maven plugin (ConfigDocumentMojo) to create the configuration file by reading the configuration bean classes. Read below for details.
 * An OSGI service (ConfigProvider) to provide the relevant object for the given bean class. Read below for details.

## Step 1: Adding configurations to a Carbon component

When you develop a Carbon component, you do not need to bundle separate configuration files with the feature. Instead, the required user configurations should be defined as one or more Java beans annotated with the following three annotations:

 * `org.wso2.carbon.kernel.annotations.Configuration`: This is a class-level annotation, which corresponds to a configuration bean to be used by a component.
 * `org.wso2.carbon.kernel.annotations.Element`: This is a field-level annotation, which corresponds to a field of the class.
 * `org.wso2.carbon.kernel.annotations.Ignore`: This is a field-level annotation, which specifies that the field needs to be ignored when the configuration is generated.
 
If you have the Java beans defined accordingly, a configuration document will be generated when you build your Carbon component later.

See the following example:

```
@Configuration(namespace = "wso2.carbon", description = "Carbon Configuration Parameters")
public class CarbonConfiguration {

   public CarbonConfiguration() {
       // Reads the {@value Constants#PROJECT_DEFAULTS_PROPERTY_FILE} property file and assign project version.
       Properties properties = ConfigurationUtils.loadProjectProperties();
       version = properties.getProperty(Constants.MAVEN_PROJECT_VERSION);
   }

   @Element(description = "value to uniquely identify a server")
   private String id = "carbon-kernel";

   @Element(description = "server name")
   private String name = "WSO2 Carbon Kernel";

   @Ignore
   private String version;

   private String tenant = Constants.DEFAULT_TENANT;

   @Element(description = "ports used by this server")
   private PortsConfig ports = new PortsConfig();

   @Element(description = "StartupOrderResolver related configurations")
   private StartupResolverConfig startupResolver = new StartupResolverConfig();

   @Element(description = "JMX Configuration")
   private JMXConfiguration jmx = new JMXConfiguration();

   public String getId() {
       return id;
   }

  …..
}
```

The elements in the above example are explained below

* **Configuration annotation:**
   * This is a class-level annotation, which needs to be added to all the configuration bean classes in the component.
   * The `namespace` attribute is only needed for the root configuration bean. A unique namespace value needs to be set for the root configuration bean class. The namespace value should be prefixed with wso2.
   * The `description` attribute needs to be set for all bean classes. The description needs to be added in the configuration docs.
 For example:
 
 ```
 @Configuration(namespace = "wso2.carbon", description = "Carbon Configuration Parameters")public class CarbonConfiguration
 ```

* **Element annotation:** 

  This is a field-level annotation, which is not required to be added to all fields in the bean class. You should add this only if you want to have a description for the particular field in the config docs.
  
	 For example: 
  ```
  @Element(description = "value to uniquely identify a server")
   	private String id = "carbon-kernel";
    ```

* Ignore annotation:
  
  This is a field-level annotation. Should only be added if you want to skip the field from the configuration docs. Theoretically, those fields should not be configured by end users. 
	 For example:
  ```
  @Ignore
   	private String version;
    ```

* Every required field should have a default value in the bean class as shown in the above example.
* If you have an Array or Collection as a field type, you need to set the default values inside the bean constructor as shown below.
   
   ```
   @Configuration(namespace = "wso2.transports.netty", description = "Netty Transport Configurations")
public class TransportsConfiguration {

   //default values of an array or collection need to mention in class constructor
   public TransportsConfiguration() {
       ListenerConfiguration listenerConfiguration = ListenerConfiguration();
       listenerConfigurations = new HashSet<>();
       listenerConfigurations.add(listenerConfiguration);

   }

   @Element(description = "listener configurations")
   private Set<ListenerConfiguration> listenerConfigurations;

            }
````

## Step 2: Getting the configuration bean object at runtime

1. Add Reference to the ConfigProvider OSGI service as shown below.

 ```
 /**
* Get the ConfigProvider service.
* This is the bind method that gets called for ConfigProvider service registration that satisfy the policy.
*
* @param configProvider the ConfigProvider service that is registered as a service.
*/
@Reference(
      name = "carbon.config.provider",
      service = ConfigProvider.class,
      cardinality = ReferenceCardinality.MANDATORY,
      policy = ReferencePolicy.DYNAMIC,
      unbind = "unregisterConfigProvider"
)
protected void registerConfigProvider(ConfigProvider configProvider) {
  DataHolder.getInstance().setConfigProvider(configProvider);
}

/**
* This is the unbind method for the above reference that gets called for ConfigProvider instance un-registrations.
*
* @param configProvider the ConfigProvider service that get unregistered.
*/
protected void unregisterConfigProvider(ConfigProvider configProvider) {
  DataHolder.getInstance().setConfigProvider(null);
}
```

2. Get the particular bean object by calling the `getConfigurationObject(Class<T> configClass)` API with bean class as below. This will return the configuration object of the class with the overriding values in the `deployment.yaml` file. If configurations do not exist in the `deployment.yaml`, the object will be returned with default values.

  ```
  <Bean> bean = DataHolder.getInstance().getConfigProvider().getConfigurationObject(<Bean>.class);
```

3. Get the particular configuration map of the namespace in the `deployment.yaml` file by calling the `getConfigurationMap(String namespace)` with the namespace as shown below. This will return the configuration map of the namespace, provided that configurations exist for the given namespace in the `deployment.yaml` file.

 ```
 Map map = DataHolder.getInstance().getConfigProvider().getConfigurationMap(<namespace>);
 ```
 
## Step 3: Building the Carbon feature

1. Add the Carbon core dependency to the component's POM file. This is to get the custom annotations defined in `carbon.core`.

 ```
 <dependencies>
 …
   <dependency>
      <groupId>org.wso2.carbon</groupId>
      <artifactId>org.wso2.carbon.core</artifactId>
   </dependency>
…
</dependencies> 
```

2. Add the Maven plugin to the component's POM file. This is to create the configuration document by reading the configuration bean classes.

 ```
 <build>
  <plugins>
…
      <plugin>
          <groupId>org.wso2.carbon</groupId>
          <artifactId>org.wso2.carbon.extensions.configuration.maven.plugin</artifactId>
          <executions>
              <execution>
                  <goals>
                      <goal>create-doc</goal>
                  </goals>
                  <phase>compile</phase>
              </execution>
          </executions>
      </plugin>
…
  </plugins>
</build>
```

3. Build the component using the `mvn clean install` command and if everything is done correctly, the configuration document file(`<config-namespace-value>.yaml`) will create automatically inside the `<CLASS_OUTPUT_DIRECTORY>/config-docs` directory.

4. Add the Maven dependency plugin to the feature's POM file to copy the configuration document file to the feature. When the feature builds, the configuration document file (`<config-namespace-value>.yaml`) will be copied to the config-docs directory.  
 
 ```
 <plugins>
…
  <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-dependency-plugin</artifactId>
      <executions>
…
          <execution>
              <id>unpack</id>
              <phase>package</phase>
              <goals>
                  <goal>unpack</goal>
              </goals>
              <configuration>
                  <artifactItems>
                      <artifactItem>
                          <groupId><-- component-group-id --></groupId>
                          <artifactId><-- component-artifact-id --></artifactId>
                          <version><-- component-version --></version>
                          <type>bundle</type>
                          <overWrite>true</overWrite>
                          <outputDirectory>${project.build.directory}/docs</outputDirectory>
                          <includes>config-docs/**</includes>
                      </artifactItem>
                  </artifactItems>
              </configuration>
          </execution>
…
      </executions>
  </plugin>
…
</plugins> 
```

5. And add resource as shown below.

  ```  
<build>
…
<resources>
…
  <resource>
      <directory>${project.build.directory}/docs/</directory>
  </resource>
…
</resources>
…
</build> 
```

Shown below is a sample Configuration Doc file. This is generated for the `CarbonConfiguration` class. If you need to override the default configuations, you need to copy this configs to the `deployment.yaml` file in your server.

```
################################################################################
#   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved
#
#   Licensed under the Apache License, Version 2.0 (the \"License\");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an \"AS IS\" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.
################################################################################
 # Carbon Configuration Parameters
wso2.carbon:
   # value to uniquely identify a server
 id: carbon-kernel
   # server name
 name: WSO2 Carbon Kernel
 tenant: default
   # ports used by this server
 ports:
     # port offset
   offset: 0
   # StartupOrderResolver related configurations
 startupResolver:
   capabilityListenerTimer:
       # delay in milliseconds before task is to be executed
     delay: 20
       # time in milliseconds between successive task executions
     period: 20
   pendingCapabilityTimer:
       # delay in milliseconds before task is to be executed
     delay: 60000
       # time in milliseconds between successive task executions
     period: 30000
   # JMX Configuration
 jmx:
     # To enable JMX Monitoring, change this value to true
   enabled: false
     # Server HostName
   hostName: 127.0.0.1
     # The port RMI server should be exposed
   rmiServerPort: 11111
     # The port RMI registry is exposed
   rmiRegistryPort: 9999
   ```

## Step 4: Updating the default configurations

If you need to override the default values, you need to copy  the configuration segment to the `deployment.yaml` file and change the value. The server will pick the new configs from the `deployment.yaml` after restarting the server. For example, if you need to change the update interval of the carbon deployment engine to 20, you need to copy the configuration segment from the configuration document of the component to the `deployment.yaml` file of the server.

Copy the `updateInterval` configuration to the `deployment.yaml` file and change the value to 20, as shown below.

```
# Deployment Engine related configurations
wso2.deployment:
   # Deployment update interval in seconds. This is the interval between repository listener
   # executions.
 updateInterval: 20
 ```

The updated `deployment.yaml` file will be as follows:

```
# Carbon Configuration Parameters
wso2.carbon:
   # value to uniquely identify a server
 id: carbon-kernel
   # server name
 name: WSO2 Carbon Kernel
   # ports used by this server
 ports:
     # port offset
   offset: 0

# Deployment Engine related configurations
wso2.deployment:
   # Deployment update interval in seconds. This is the interval between repository listener
   # executions.
 updateInterval: 20
 ```
